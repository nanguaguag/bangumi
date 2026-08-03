package com.xiaoyv.bangumi.features.pixiv.login.business

/**
 * [PixivLoginSideEffect]
 *
 * @author why
 * @since 2025/1/12
 */
sealed class PixivLoginSideEffect {
    data object OnNavUp : PixivLoginSideEffect()
    data class OnToast(val message: String) : PixivLoginSideEffect()
}
