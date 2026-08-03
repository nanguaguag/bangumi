package com.xiaoyv.bangumi.features.pixiv.main.business

import com.xiaoyv.bangumi.shared.ui.component.navigation.Screen

/**
 * [PixivSearchEvent]
 *
 * @author why
 * @since 2025/1/12
 */
sealed class PixivSearchEvent {
    sealed class UI : PixivSearchEvent() {
        data object OnNavUp : UI()
        data class OnNavScreen(val screen: Screen) : UI()
    }

    sealed class Action : PixivSearchEvent() {
        data class OnRefresh(val loading: Boolean) : Action()
    }
}
