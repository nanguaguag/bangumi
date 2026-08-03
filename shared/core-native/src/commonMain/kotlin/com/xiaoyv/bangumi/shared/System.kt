package com.xiaoyv.bangumi.shared

import androidx.compose.ui.platform.ClipEntry
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.xiaoyv.bangumi.shared.native.AppDatabase
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import kotlin.coroutines.CoroutineContext

val systemDevice by lazy { SystemDevice() }

expect object System {
    val isDebugType: Boolean
    val uiDispatcher: CoroutineContext
    val database: AppDatabase
    val datastore: DataStore<Preferences>

    suspend fun cleanCache(): Result<Boolean>

    fun createClipEntry(text: String): ClipEntry

    fun userAgent(): String

    fun currentTimeMillis(): Long

    fun launchDeeplinkSettings()

    fun log(tag: String, message: String)

    fun shareText(text: String)

    fun createHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient

    /**
     * 下载图片到公共下载目录的子目录（Android: Download/{subDir}）
     *
     * @param url 完整下载地址（调用方需自行处理防盗链/代理重写）
     * @param fileName 文件名（含扩展名）
     * @param subDir 下载目录子目录名（如 "bangumi"）
     * @return 成功时返回保存的相对路径（如 "Download/bangumi/123.jpg"）
     */
    suspend fun downloadImage(url: String, fileName: String, subDir: String): Result<String>
}