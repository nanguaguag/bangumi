package com.xiaoyv.bangumi.shared

import android.app.Activity
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpHeaders
import java.io.File
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * 下载专用 HttpClient（带超时与重定向）
 */
internal val downloadClient: HttpClient by lazy {
    System.createHttpClient {
        expectSuccess = true
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 60_000
        }
    }
}

/**
 * 持有当前 resumed 的 Activity（用于运行时权限请求）
 */
internal object ActivityHolder {
    private var resumedActivity: Activity? = null
    private var initialized = false

    fun ensureInit(context: Context) {
        if (initialized) return
        initialized = true
        (context as? Application)?.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityResumed(activity: Activity) {
                    resumedActivity = activity
                }

                override fun onActivityPaused(activity: Activity) {
                    if (resumedActivity === activity) resumedActivity = null
                }

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
                override fun onActivityStarted(activity: Activity) = Unit
                override fun onActivityStopped(activity: Activity) = Unit
                override fun onActivityDestroyed(activity: Activity) = Unit
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            }
        )
    }

    fun current(): Activity? = resumedActivity
}

/**
 * 存储权限请求桥（MainActivity 需将 onRequestPermissionsResult 转发到这里）
 */
object StoragePermissionBridge {
    private const val REQUEST_CODE_WRITE_STORAGE = 0x2D01
    private var pendingContinuation: Continuation<Boolean>? = null

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode != REQUEST_CODE_WRITE_STORAGE) return
        val granted = grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        pendingContinuation?.resume(granted)
        pendingContinuation = null
    }

    internal suspend fun requestWriteStorage(activity: Activity): Boolean =
        suspendCancellableCoroutine { continuation ->
            if (pendingContinuation != null) {
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }
            pendingContinuation = continuation
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_WRITE_STORAGE
            )
        }
}

/**
 * Android 10+（API 29+）：通过 MediaStore 写入公共下载目录，无需权限
 */
internal fun saveImageToMediaStore(bytes: ByteArray, fileName: String, subDir: String): String {
    val resolver = application.contentResolver
    val values = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
        put(MediaStore.Downloads.MIME_TYPE, fileName.fileMimeType())
        put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$subDir")
    }
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        ?: error("无法创建下载文件")

    try {
        resolver.openOutputStream(uri)?.use { it.write(bytes) } ?: error("无法写入下载文件")
    } catch (e: Exception) {
        resolver.delete(uri, null, null)
        throw e
    }
    return "${Environment.DIRECTORY_DOWNLOADS}/$subDir/$fileName"
}

/**
 * Android 9-（API 24-28）：需要 WRITE_EXTERNAL_STORAGE 运行时权限，直接写入公共目录
 */
internal suspend fun saveImageToLegacyStorage(bytes: ByteArray, fileName: String, subDir: String): String {
    val granted = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        true
    } else {
        ContextCompat.checkSelfPermission(application, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
                || ActivityHolder.current()?.let { StoragePermissionBridge.requestWriteStorage(it) } == true
    }
    if (!granted) error("未授予存储权限，无法保存到下载目录")

    val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val dir = File(downloadDir, subDir).apply { mkdirs() }
    File(dir, fileName).writeBytes(bytes)
    return "${Environment.DIRECTORY_DOWNLOADS}/$subDir/$fileName"
}

/**
 * 根据扩展名推断 MIME 类型
 */
internal fun String.fileMimeType(): String = when (substringAfterLast('.', "").lowercase()) {
    "png" -> "image/png"
    "gif" -> "image/gif"
    "webp" -> "image/webp"
    "jpeg", "jpg" -> "image/jpeg"
    else -> "application/octet-stream"
}
