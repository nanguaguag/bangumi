package com.xiaoyv.bangumi.shared.data.repository

import com.xiaoyv.bangumi.shared.data.model.request.ChallengeParam
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivCurrentUser
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivIllust
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivToken
import kotlinx.atomicfu.AtomicRef

/**
 * [PixivRepository]
 *
 * @since 2025/5/26
 */
interface PixivRepository {
    val cacheChallengeParam: AtomicRef<ChallengeParam?>

    suspend fun fetchLoginChallenge(): Result<ChallengeParam>

    suspend fun sendAuthToken(code: String, codeVerifier: String): Result<ComposePixivToken>

    /**
     * 通过 refresh_token 直接登录（Token 登录方式）
     */
    suspend fun loginWithRefreshToken(refreshToken: String): Result<ComposePixivToken>

    /**
     * 获取当前登录的 Pixiv 用户信息
     */
    suspend fun fetchCurrentUser(): Result<ComposePixivCurrentUser>

    /**
     * 获取用户详情
     */
    suspend fun fetchUserDetail(userId: Long): Result<com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivUserDetailResult>

    /**
     * 收藏/取消收藏插画
     */
    suspend fun bookmarkIllust(illustId: Long, isBookmarked: Boolean): Result<Boolean>

    /**
     * 关注/取消关注用户
     */
    suspend fun followUser(userId: Long, isFollowed: Boolean): Result<Boolean>

    /**
     * 获取相关插画
     */
    suspend fun fetchRelatedIllusts(illustId: Long): Result<List<ComposePixivIllust>>

    /**
     * 退出 Pixiv 登录，清除 token
     */
    suspend fun logout()
}
