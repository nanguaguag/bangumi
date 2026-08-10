package com.xiaoyv.bangumi.shared.data.model.response.pixiv

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Pixiv 作品评论
 *
 * GET https://app-api.pixiv.net/v3/illust/comments
 * GET https://app-api.pixiv.net/v2/illust/comment/replies
 */
@Serializable
@Immutable
data class ComposePixivComment(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("comment")
    val comment: String? = null,
    @SerialName("date")
    val date: String? = null,
    @SerialName("user")
    val user: ComposePixivUser? = null,
    @SerialName("has_replies")
    val hasReplies: Boolean = false,
    @SerialName("stamp")
    val stamp: ComposePixivStamp? = null,
    @SerialName("replies")
    val replies: List<ComposePixivComment> = emptyList(),
)

/**
 * Pixiv 评论列表响应
 */
@Serializable
@Immutable
data class ComposePixivStamp(
    @SerialName("stamp_id")
    val id: Long = 0,
    @SerialName("stamp_url")
    val url: String? = null,
)

/**
 * Pixiv 评论列表响应
 */
@Serializable
@Immutable
data class ComposePixivCommentsResult(
    @SerialName("comments")
    val comments: List<ComposePixivComment> = emptyList(),
    @SerialName("next_url")
    val nextUrl: String? = null,
)

/**
 * Pixiv 发表评论响应
 *
 * POST https://app-api.pixiv.net/v1/illust/comment/add
 */
@Serializable
@Immutable
data class ComposePixivAddCommentResult(
    @SerialName("comment")
    val comment: ComposePixivComment? = null,
)
