package com.xiaoyv.bangumi.features.gallery.business

import androidx.compose.runtime.Immutable
import com.xiaoyv.bangumi.shared.data.model.response.image.ComposeGallery
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivComment
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivIllust
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivUserDetail
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val MAX_PIXIV_COMMENT_LENGTH = 140

/**
 * [GalleryState]
 *
 * @author why
 * @since 2025/1/12
 */
@Immutable
@Serializable
data class GalleryState(
    @SerialName("id") val id: String = "",
    @SerialName("images") val images: List<ComposeGallery> = emptyList(),
    @SerialName("isPixiv") val isPixiv: Boolean = false,
    @SerialName("illust") val illust: ComposePixivIllust? = null,
    @SerialName("showOriginal") val showOriginal: Boolean = false,
    @SerialName("isBookmarked") val isBookmarked: Boolean = false,
    @SerialName("isFollowed") val isFollowed: Boolean = false,
    @SerialName("isWatchLater") val isWatchLater: Boolean = false,
    @SerialName("isLoadingAction") val isLoadingAction: Boolean = false,

    // ---- 评论 ----
    @SerialName("comments") val comments: List<ComposePixivComment> = emptyList(),
    @SerialName("commentsLoading") val commentsLoading: Boolean = false,
    @SerialName("expandedReplyIds") val expandedReplyIds: Set<Long> = emptySet(),
    @SerialName("commentInput") val commentInput: String = "",
    @SerialName("isSendingComment") val isSendingComment: Boolean = false,
    @SerialName("replyTarget") val replyTarget: ComposePixivComment? = null,

    // ---- 相关图片 ----
    @SerialName("relatedIllusts") val relatedIllusts: List<ComposePixivIllust> = emptyList(),
    @SerialName("relatedLoading") val relatedLoading: Boolean = false,
    @SerialName("relatedHasMore") val relatedHasMore: Boolean = true,
    @SerialName("relatedNextUrl") val relatedNextUrl: String? = null,

    // ---- 标签 ----
    @SerialName("bannedTags") val bannedTags: List<String> = emptyList(),
    @SerialName("bookmarkedTags") val bookmarkedTags: List<String> = emptyList(),
)
