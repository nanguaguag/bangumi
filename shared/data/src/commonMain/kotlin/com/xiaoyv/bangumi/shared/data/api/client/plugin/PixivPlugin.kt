@file:OptIn(ExperimentalStdlibApi::class)
@file:Suppress("SpellCheckingInspection")

package com.xiaoyv.bangumi.shared.data.api.client.plugin

import com.appmattus.crypto.Algorithm
import com.xiaoyv.bangumi.shared.data.model.response.bgm.ComposeSetting
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.utils.io.KtorDsl

/**
 * imp.pximg.net
 * i-f.pximg.net
 * source.pximg.net
 */
@KtorDsl
class PixivImagePluginConfig(
    var os: String = "android",
    var userAgent: String = "PixivAndroidApp/6.141.1 (Android 15; Google Pixel 7);",
    var network: ComposeSetting.NetworkConfig = ComposeSetting.NetworkConfig.Default,
)

/**
 * [PixivProxyPlugin]
 */
val PixivProxyPlugin: ClientPlugin<PixivImagePluginConfig> =
    createClientPlugin("PixivProxyPlugin", ::PixivImagePluginConfig) {
        val config = pluginConfig

        onRequest { request, _ ->
            val url = request.url.toString()

            // Pixiv API 请求头：所有 pixiv.net API 域名都需要这些头（oauth.secure.pixiv.net / app-api.pixiv.net）
            // 排除 pximg.net 等图片 CDN 域名
            if (url.contains("pixiv.net") && !url.contains("pximg.net")) {
                // 清除 bgm.tv 专用头，这些会导致 Pixiv 返回 invalid_request
                request.headers.remove("Cookie")
                request.headers.remove("TE")
                request.headers.remove("Pragma")
                request.headers.remove("Cache-Control")
                // 移除 GET 请求上不必要的 Content-Type（Pixiv 对此严格）
                if (request.method.value == "GET") {
                    request.headers.remove("Content-Type")
                }

                val formatted = kotlin.time.Clock.System.now().toString()
                val hashTime = Algorithm.MD5
                    .hash((formatted + config.network.pixivTimeHashSecret).encodeToByteArray())
                    .toHexString()

                request.headers["x-client-time"] = formatted
                request.headers["x-client-hash"] = hashTime
                request.headers["app-os"] = config.os
                request.headers["app-os-version"] = config.network.pixivVersion
                request.headers["user-agent"] = config.userAgent
            }
        }
    }
