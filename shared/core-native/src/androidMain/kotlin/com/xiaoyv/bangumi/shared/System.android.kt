package com.xiaoyv.bangumi.shared

import android.app.Application
import android.content.ClipData
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ClipEntry
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.xiaoyv.bangumi.shared.database.DatabaseDriverFactory
import com.xiaoyv.bangumi.shared.native.AppDatabase
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath
import java.lang.System
import java.net.InetAddress
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime

/**
 * Pixiv 大陆直连 DNS 解析器（参考 pixez-flutter 的 lib/er/hoster.dart）。
 *
 * 国内网络环境下 app-api.pixiv.net / i.pximg.net 等域名会被 DNS 污染，
 * pixez 的做法是硬编码 Pixiv 真实 IP（210.140.139.x），连接时直接使用 IP，
 * TLS SNI 仍保留原域名，从而绕过污染直连。
 *
 * OkHttp 会按返回顺序依次尝试连接地址，连接失败后自动回退到后续地址，
 * 因此这里把硬编码 IP 放在系统解析结果之前：硬编码 IP 可用时直连成功；
 * 不可用时（IP 变更/海外网络）自动回退到系统 DNS 的正常解析，不影响使用。
 */
private object PixivDirectDns : okhttp3.Dns {
    // 参考 pixez-flutter lib/er/hoster.dart 的硬编码 IP 表
    private val PINNED_IPS = mapOf(
        "app-api.pixiv.net" to listOf("210.140.139.155"),
        "oauth.secure.pixiv.net" to listOf("210.140.139.155"),
        "i.pximg.net" to listOf("210.140.139.133"),
        "s.pximg.net" to listOf("210.140.139.133"),
    )

    override fun lookup(hostname: String): List<InetAddress> {
        val pinned = PINNED_IPS[hostname]?.mapNotNull { ip ->
            runCatching { InetAddress.getByName(ip) }.getOrNull()
        }.orEmpty()
        return pinned + okhttp3.Dns.SYSTEM.lookup(hostname)
    }
}

lateinit var application: Application

actual object System {
    actual val isDebugType: Boolean
        get() = (application.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    actual val uiDispatcher: CoroutineContext = AndroidUiDispatcher.Main

    actual val database: AppDatabase by lazy {
        AppDatabase(DatabaseDriverFactory.createDriver())
    }

    actual val datastore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                application.filesDir.resolve("bgm.preferences_pb").absolutePath.toPath()
            }
        )
    }

    @OptIn(ExperimentalTime::class)
    actual fun currentTimeMillis() = kotlin.time.Clock.System.now().toEpochMilliseconds()

    actual fun log(tag: String, message: String) {
        Log.d(tag, message)
    }

    actual fun launchDeeplinkSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(
                Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                "package:com.xiaoyv.bangumi.multiplatform".toUri()
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            application.startActivity(intent)
        }
    }

    actual fun userAgent(): String {
        return System.getProperty("http.agent").orEmpty()
    }

    actual fun createClipEntry(text: String): ClipEntry {
        return ClipEntry(ClipData.newPlainText("Copy", text))
    }

    actual fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, "分享内容")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        application.startActivity(chooser)
    }

    actual fun createHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient {
        return HttpClient(OkHttp) {
            engine {
                // Pixiv 大陆直连：硬编码真实 IP 优先 + 系统 DNS 回退
                config { dns(PixivDirectDns) }
            }
            block()
        }
    }

    actual suspend fun cleanCache(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching { application.cacheDir.deleteRecursively() }
        }
    }

    actual suspend fun downloadImage(url: String, fileName: String, subDir: String): Result<String> {
        return withContext(Dispatchers.IO) {
            runCatching {
                requireSafeDownloadSubDir(subDir)
                ActivityHolder.ensureInit(application)

                // 下载字节流（带 Referer 与 UA，避免 Pixiv 防盗链 403）
                val bytes = downloadClient.get(url) {
                    header(HttpHeaders.Referrer, "https://www.pixiv.net/")
                    header(HttpHeaders.UserAgent, userAgent())
                }.bodyAsBytes()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveImageToMediaStore(bytes, fileName, subDir)
                } else {
                    saveImageToLegacyStorage(bytes, fileName, subDir)
                }
            }
        }
    }
}
