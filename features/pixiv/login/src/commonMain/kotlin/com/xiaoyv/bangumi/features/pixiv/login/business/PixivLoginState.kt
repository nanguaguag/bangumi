package com.xiaoyv.bangumi.features.pixiv.login.business

import androidx.compose.runtime.Immutable
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivCurrentUser
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivUserProfile

/**
 * [PixivLoginState]
 *
 * @author why
 * @since 2025/1/12
 */
@Immutable
data class PixivLoginState(
    val isLoggedIn: Boolean = false,
    val isLoggingIn: Boolean = false,
    val currentUser: ComposePixivCurrentUser? = null,
    val userProfile: ComposePixivUserProfile? = null,
    val pixivUserName: String = "",
    val pixivUserAvatar: String = "",
    val loginSuccess: Boolean = false,
    val showTokenDialog: Boolean = false,
    val tokenInput: String = "",
    val tokenError: String? = null,
)
