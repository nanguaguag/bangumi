package com.xiaoyv.bangumi.features.pixiv.login.business

import com.xiaoyv.bangumi.shared.ui.component.navigation.Screen

/**
 * [PixivLoginSideEffect]
 *
 * @author why
 * @since 2025/1/12
 */
sealed class PixivLoginSideEffect {
    data object OnNavUp : PixivLoginSideEffect()
    data class OnNavScreen(val screen: Screen) : PixivLoginSideEffect()
    data class OpenExternalUrl(val url: String) : PixivLoginSideEffect()
    data class OnToast(val message: String) : PixivLoginSideEffect()
}
