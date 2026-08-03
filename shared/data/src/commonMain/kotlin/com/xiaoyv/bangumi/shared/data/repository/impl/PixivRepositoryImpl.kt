@file:OptIn(ExperimentalStdlibApi::class)

package com.xiaoyv.bangumi.shared.data.repository.impl

import com.appmattus.crypto.Algorithm
import com.xiaoyv.bangumi.shared.System
import com.xiaoyv.bangumi.shared.core.utils.runResult
import com.xiaoyv.bangumi.shared.core.utils.debugLog
import com.xiaoyv.bangumi.shared.data.api.client.BgmApiClient
import com.xiaoyv.bangumi.shared.data.manager.app.PreferenceStore
import com.xiaoyv.bangumi.shared.data.model.request.ChallengeParam
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivCurrentUser
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivIllust
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivMeStateResponse
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivToken
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivUserDetailResult
import com.xiaoyv.bangumi.shared.data.repository.PixivRepository
import io.ktor.client.statement.bodyAsText
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.kotlincrypto.random.CryptoRand
import kotlin.io.encoding.Base64

/**
 * [PixivRepositoryImpl]
 *
 * @since 2025/5/26
 */
class PixivRepositoryImpl(
    private val client: BgmApiClient,
    private val preferenceStore: PreferenceStore,
) : PixivRepository {
    override val cacheChallengeParam = atomic<ChallengeParam?>(null)

    override suspend fun fetchLoginChallenge(): Result<ChallengeParam> = runResult {
        val encoder = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
        val codeVerifier = encoder.encode(CryptoRand.nextBytes(ByteArray(32)))
        val codeChallenge = encoder.encode(Algorithm.SHA_256.hash(codeVerifier.encodeToByteArray()))
        ChallengeParam(codeVerifier, codeChallenge).apply {
            cacheChallengeParam.update { this }
        }
    }

    override suspend fun sendAuthToken(code: String, codeVerifier: String) = run {
        val result = client.requestPixivApi {
            sendAuthToken(
                code = code,
                codeVerifier = codeVerifier,
                grantType = "authorization_code",
                clientId = preferenceStore.settings.network.pixivClientId,
                clientSecret = preferenceStore.settings.network.pixivClientSecret,
                includePolicy = true,
                redirectUri = "https://app-api.pixiv.net/web/v1/users/auth/pixiv/callback"
            ).let { it.copy(expiresAt = System.currentTimeMillis() + it.expiresIn * 1000) }
        }
        // 保存 token 到 PreferenceStore，供 Bearer Auth 插件使用
        result.onSuccess { preferenceStore.pixivToken = it }
        result
    }

    override suspend fun loginWithRefreshToken(refreshToken: String): Result<ComposePixivToken> {
        debugLog { "Pixiv loginWithRefreshToken: calling OAuth with refresh_token" }
        val config = preferenceStore.settings.network
        val result = client.requestPixivApi {
            sendAuthTokenRefresh(
                grantType = "refresh_token",
                clientId = config.pixivClientId,
                clientSecret = config.pixivClientSecret,
                includePolicy = true,
                refreshToken = refreshToken,
            ).let { it.copy(expiresAt = System.currentTimeMillis() + it.expiresIn * 1000) }
        }
        result.onSuccess {
            debugLog { "Pixiv loginWithRefreshToken SUCCESS: access_token=${it.accessToken.take(8)}..., refresh_token=${it.refreshToken.take(8)}..." }
            preferenceStore.pixivToken = it
            debugLog { "Pixiv loginWithRefreshToken: token saved to preferenceStore" }
        }.onFailure {
            debugLog { "Pixiv loginWithRefreshToken FAILED: ${it.message}" }
        }
        return result
    }

    override suspend fun fetchCurrentUser(): Result<ComposePixivCurrentUser> {
        debugLog { "Pixiv fetchCurrentUser: calling /v1/user/me/state" }
        val token = preferenceStore.pixivToken
        debugLog { "Pixiv fetchCurrentUser: current access_token=${token.accessToken.take(8)}..., isBlank=${token.accessToken.isBlank()}" }

        // /v1/user/me/state 返回基本 profile（不含 account/comment）
        // 直接调用 api 以便捕获响应体
        var meStateResponse: ComposePixivMeStateResponse? = null
        var lastError: Throwable? = null
        try {
            meStateResponse = withContext(Dispatchers.IO) {
                client.pixivApi.getCurrentUser()
            }
        } catch (e: Throwable) {
            lastError = e
            debugLog { "Pixiv fetchCurrentUser FAILED: ${e.message}" }
            debugLog { "Pixiv fetchCurrentUser exception type: ${e::class.qualifiedName}" }
            // ApiHttpException 有 bodyAsText 字段
            if (e is com.xiaoyv.bangumi.shared.core.exception.ApiHttpException) {
                debugLog { "Pixiv fetchCurrentUser response code=${e.code} body=${e.bodyAsText.take(1000)}" }
            }
            var current: Throwable? = e.cause
            while (current != null) {
                val c = current
                debugLog { "Pixiv fetchCurrentUser cause: ${c::class.qualifiedName}: ${c.message}" }
                if (c is com.xiaoyv.bangumi.shared.core.exception.ApiHttpException) {
                    debugLog { "Pixiv fetchCurrentUser cause response code=${c.code} body=${c.bodyAsText.take(1000)}" }
                }
                current = c.cause
            }
        }

        val profile = meStateResponse?.profile ?: return Result.failure(
            lastError ?: Exception("获取用户信息失败")
        )

        debugLog { "Pixiv fetchCurrentUser: /v1/user/me/state SUCCESS, userId=${profile.userId}, name=${profile.name}" }

        // 再用 /v1/user/detail 获取完整信息（account, comment）
        val detailResult = fetchUserDetail(profile.userId)
        val detail = detailResult.getOrNull()
        val user = detail?.user

        val currentUser = ComposePixivCurrentUser(
            id = profile.userId,
            name = user?.name ?: profile.name,
            account = user?.account ?: profile.pixivId,
            profileImageUrls = profile.profileImageUrls,
            comment = user?.comment,
            isPremium = profile.isPremium,
        )

        debugLog { "Pixiv fetchCurrentUser SUCCESS: name=${currentUser.name}, id=${currentUser.id}, account=${currentUser.account}" }
        return Result.success(currentUser)
    }

    override suspend fun fetchUserDetail(userId: Long): Result<ComposePixivUserDetailResult> {
        return client.requestPixivApi { getUserDetail(userId) }
    }

    override suspend fun bookmarkIllust(illustId: Long, isBookmarked: Boolean): Result<Boolean> {
        return if (isBookmarked) {
            client.requestPixivApi { bookmarkIllustDelete(illustId) }
                .map { it.isBookmarked }
        } else {
            client.requestPixivApi { bookmarkIllustAdd(illustId) }
                .map { it.isBookmarked }
        }
    }

    override suspend fun followUser(userId: Long, isFollowed: Boolean): Result<Boolean> {
        return if (isFollowed) {
            client.requestPixivApi { followUserDelete(userId) }
                .map { it.isFollowed }
        } else {
            client.requestPixivApi { followUserAdd(userId) }
                .map { it.isFollowed }
        }
    }

    override suspend fun fetchRelatedIllusts(illustId: Long): Result<List<ComposePixivIllust>> {
        return client.requestPixivApi { getIllustRelated(illustId) }
            .map { it.illusts }
    }

    override suspend fun logout() {
        debugLog { "Pixiv logout: clearing token" }
        preferenceStore.pixivToken = ComposePixivToken.Empty
        debugLog { "Pixiv logout: token cleared, accessToken now blank=${preferenceStore.pixivToken.accessToken.isBlank()}" }
    }
}
