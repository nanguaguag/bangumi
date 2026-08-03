package com.xiaoyv.bangumi.features.gallery.business

import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivComment
import com.xiaoyv.bangumi.shared.ui.component.navigation.Screen

/**
 * [GalleryEvent]
 *
 * @author why
 * @since 2025/1/12
 */
sealed class GalleryEvent {
    sealed class UI : GalleryEvent() {
        data object OnNavUp : UI()
        data class OnNavScreen(val screen: Screen) : UI()
    }

    sealed class Action : GalleryEvent() {
        data class OnRefresh(val loading: Boolean) : Action()
        data object OnOpenInBrowser : Action()
        data object OnToggleBookmark : Action()
        data object OnToggleFollow : Action()
        data object OnToggleShowOriginal : Action()
        data object OnToggleWatchLater : Action()
        data object OnShare : Action()
        data object OnCopyLink : Action()
        data object OnDownload : Action()
        data class OnTagClick(val tag: String) : Action()

        // ---- 评论 ----
        data class OnToggleReplies(val commentId: Long) : Action()
        data class OnCommentInputChange(val text: String) : Action()
        data class OnReplyTarget(val comment: ComposePixivComment?) : Action()
        data object OnSendComment : Action()

        // ---- 标签操作 ----
        data class OnBanTag(val tag: String) : Action()
        data class OnBookmarkTag(val tag: String) : Action()
        data class OnCopyTag(val tag: String) : Action()
    }
}
