@file:OptIn(InternalAPI::class)
@file:Suppress("SpellCheckingInspection")

package com.xiaoyv.bangumi.shared.data.api.client.plugin

import com.xiaoyv.bangumi.shared.System
import com.xiaoyv.bangumi.shared.core.exception.ApiHttpException
import com.xiaoyv.bangumi.shared.core.utils.debugLog
import com.xiaoyv.bangumi.shared.data.manager.app.PreferenceStore
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivToken
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.KtorDsl
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * [PixivAuthConfig]
 */
@KtorDsl
class PixivAuthConfig {
    var preferenceStore: PreferenceStore? = null

    /**
     * 刷新 token 的挂起函数，入参为当前 token，返回新 token；刷新失败返回 null。
     */
    var refreshBlock: (suspend (ComposePixivToken) -> ComposePixivToken?)? = null
}

/**
 * 全局刷新互斥锁：避免并发请求同时刷新 token
 */
private val pixivRefreshMutex = Mutex()

/**
 * [PixivAuthPlugin]
 *
 * 参考 pixko 的 TokenAutoRefreshPluginV2，解决内置 Auth 插件的缺陷：
 * Pixiv API 对过期的 access_token 返回 HTTP 400（而非 401），内置 Auth 插件
 * 只在 401 时触发刷新，导致过期后所有请求永久 400。
 *
 * 本插件流程：
 * 1. onRequest：为 app-api.pixiv.net 请求附加 `Authorization: Bearer <access_token>`（OAuth 端点除外）
 * 2. on(Send)：请求前检查本地 `expiresAt` 是否过期，过期先刷新再请求
 * 3. 响应 400 且 body 含 OAuth 错误（invalid_grant / invalid_request / OAuth）时，
 *    自动刷新 token 并重建请求重试一次
 * 4. 其余非 2xx/3xx 错误抛出 [ApiHttpException]，保留响应 body 供上层排查
 */
val PixivAuthPlugin: ClientPlugin<PixivAuthConfig> =
    createClientPlugin("PixivAuthPlugin", ::PixivAuthConfig) {
        val preferenceStore = pluginConfig.preferenceStore
        val refreshBlock = pluginConfig.refreshBlock
        var lastRefreshedToken: ComposePixivToken? = null

        onRequest { request, _ ->
            // OAuth token 端点不需要 Bearer
            if (request.url.host.contains("oauth.secure.pixiv.net")) return@onRequest

            val token = lastRefreshedToken ?: preferenceStore?.pixivToken
            val accessToken = token?.accessToken.orEmpty()
            if (accessToken.isNotBlank()) {
                // 使用 set 运算符（替换而非追加），避免重复 Authorization 头
                request.headers[HttpHeaders.Authorization] = "Bearer $accessToken"
            }
        }

        on(Send) { originalRequest ->
            // OAuth token 端点直接放行（refreshBlock 内部会调用 sendAuthTokenRefresh，避免递归）
            if (originalRequest.url.host.contains("oauth.secure.pixiv.net")) {
                return@on proceed(originalRequest)
            }

            val stored = preferenceStore?.pixivToken ?: return@on proceed(originalRequest)

            // 请求前预检：本地 token 已过期则先刷新
            var currentToken = stored
            val hasExpired = stored.expiresAt > 0 && System.currentTimeMillis() >= stored.expiresAt
            if (hasExpired && stored.refreshToken.isNotBlank()) {
                debugLog { "PixivAuthPlugin: access_token expired (expiresAt=${stored.expiresAt}), refreshing before request" }
                currentToken = refreshPixivToken(preferenceStore, refreshBlock, stored) ?: stored
                lastRefreshedToken = currentToken
            }

            // 预检刷新成功后，重建请求以携带新 token（takeFrom 会复制旧 Authorization，需用 set 覆盖）
            val firstRequest = if (currentToken.accessToken != stored.accessToken) {
                HttpRequestBuilder().takeFromWithExecutionContext(originalRequest).apply {
                    headers[HttpHeaders.Authorization] = "Bearer ${currentToken.accessToken}"
                }
            } else {
                originalRequest
            }

            val origin = proceed(firstRequest)
            if (origin.response.status.value in 200 until 400) return@on origin

            // 出错时消费 body，避免 converter 二次读取失败
            val body = origin.response.bodyAsBytes().decodeToString()
            val isOAuthError = origin.response.status == HttpStatusCode.BadRequest && isOAuthErrorMessage(body)
            if (!isOAuthError) {
                debugLog { "PixivAuthPlugin: HTTP ${origin.response.status.value} non-OAuth error, body=${body.take(500)}" }
                throw ApiHttpException(origin.response.status.value, body)
            }

            debugLog { "PixivAuthPlugin: HTTP ${origin.response.status.value} OAuth error, refreshing token and retrying once" }
            val newToken = refreshPixivToken(preferenceStore, refreshBlock, stored)
                ?: throw ApiHttpException(origin.response.status.value, body)
            lastRefreshedToken = newToken

            val retryRequest = HttpRequestBuilder().takeFromWithExecutionContext(originalRequest).apply {
                headers[HttpHeaders.Authorization] = "Bearer ${newToken.accessToken}"
            }
            val retried = proceed(retryRequest)
            if (retried.response.status.value !in 200 until 400) {
                throw ApiHttpException(retried.response.status.value, retried.response.bodyAsBytes().decodeToString())
            }
            return@on retried
        }
    }

/**
 * 判断是否为可刷新的 OAuth 错误（过期/非法 token 等）
 */
private fun isOAuthErrorMessage(body: String): Boolean {
    val lower = body.lowercase()
    return lower.contains("oauth") ||
        lower.contains("invalid_grant") ||
        lower.contains("invalid_request") ||
        lower.contains("expired")
}

/**
 * 刷新 token（互斥 + 双重检查）
 *
 * @param expiredToken 触发刷新的旧 token；若其他协程已刷新成功，直接返回新 token。
 */
private suspend fun refreshPixivToken(
    preferenceStore: PreferenceStore,
    refreshBlock: (suspend (ComposePixivToken) -> ComposePixivToken?)?,
    expiredToken: ComposePixivToken,
): ComposePixivToken? {
    if (refreshBlock == null) return null
    return pixivRefreshMutex.withLock {
        val current = preferenceStore.pixivToken

        // 其他协程已刷新成功，直接复用
        if (current.accessToken != expiredToken.accessToken) {
            return@withLock current
        }

        // 刚刷新过（离过期还有余量），无需重复刷新
        if (current.expiresAt > 0 && System.currentTimeMillis() < current.expiresAt - 5_000) {
            return@withLock current
        }

        debugLog { "PixivAuthPlugin: executing refresh_block" }
        val refreshed = refreshBlock(expiredToken)
        if (refreshed != null && refreshed.accessToken.isNotBlank()) {
            preferenceStore.pixivToken = refreshed.copy(
                expiresAt = System.currentTimeMillis() + refreshed.expiresIn * 1000
            )
            debugLog { "PixivAuthPlugin: refresh SUCCESS, new access_token=${refreshed.accessToken.take(8)}..., expiresAt=${preferenceStore.pixivToken.expiresAt}" }
        } else {
            debugLog { "PixivAuthPlugin: refresh FAILED (null result)" }
        }
        refreshed
    }
}
