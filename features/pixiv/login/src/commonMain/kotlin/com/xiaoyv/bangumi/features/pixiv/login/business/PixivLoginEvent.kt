package com.xiaoyv.bangumi.features.pixiv.login.business

import com.xiaoyv.bangumi.shared.ui.component.navigation.Screen

/**
 * [PixivLoginEvent]
 *
 * @author why
 * @since 2025/1/12
 */
sealed class PixivLoginEvent {
    sealed class UI : PixivLoginEvent() {
        data object OnNavUp : UI()
        data class OnNavScreen(val screen: Screen) : UI()
    }

    sealed class Action : PixivLoginEvent() {
        data class OnRefresh(val loading: Boolean) : Action()
        data object OnWebViewLogin : Action()
        data object OnBrowserLogin : Action()
        data object OnShowTokenDialog : Action()
        data object OnDismissTokenDialog : Action()
        data class OnTokenInput(val token: String) : Action()
        data object OnSubmitToken : Action()
        data object OnDismissLoginSuccess : Action()
        data object OnLogout : Action()
    }
}
