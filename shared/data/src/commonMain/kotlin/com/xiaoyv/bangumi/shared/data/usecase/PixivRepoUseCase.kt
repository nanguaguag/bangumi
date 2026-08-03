package com.xiaoyv.bangumi.shared.data.usecase

import com.xiaoyv.bangumi.shared.data.api.client.cookie.BgmCookieStorage
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivCurrentUser
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivIllust
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivToken
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivUserDetailResult
import com.xiaoyv.bangumi.shared.data.repository.PixivRepository

class PixivRepoUseCase(
    private val pixivRepository: PixivRepository,
    private val cookieStorage: BgmCookieStorage,
) {

    suspend fun sendAuthToken(code: String): Result<ComposePixivToken> {
        val param = pixivRepository.cacheChallengeParam.value
        if (param == null) return Result.failure(Exception("未获取到登录参数"))
        val token = pixivRepository.sendAuthToken(code, param.codeVerifier)
        return token
    }

    suspend fun loginWithRefreshToken(refreshToken: String): Result<ComposePixivToken> {
        return pixivRepository.loginWithRefreshToken(refreshToken)
    }

    suspend fun fetchCurrentUser(): Result<ComposePixivCurrentUser> {
        return pixivRepository.fetchCurrentUser()
    }

    suspend fun fetchUserDetail(userId: Long): Result<ComposePixivUserDetailResult> {
        return pixivRepository.fetchUserDetail(userId)
    }

    suspend fun bookmarkIllust(illustId: Long, isBookmarked: Boolean): Result<Boolean> {
        return pixivRepository.bookmarkIllust(illustId, isBookmarked)
    }

    suspend fun followUser(userId: Long, isFollowed: Boolean): Result<Boolean> {
        return pixivRepository.followUser(userId, isFollowed)
    }

    suspend fun fetchRelatedIllusts(illustId: Long): Result<List<ComposePixivIllust>> {
        return pixivRepository.fetchRelatedIllusts(illustId)
    }

    suspend fun logout() {
        pixivRepository.logout()
    }
}
