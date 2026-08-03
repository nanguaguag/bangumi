package com.xiaoyv.bangumi.shared.data.model.response.pixiv

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Pixiv 用户详情
 *
 * GET /v1/user/detail
 */
@Serializable
@Immutable
data class ComposePixivUserDetailResult(
    @SerialName("user")
    val user: ComposePixivUserDetail? = null,
    @SerialName("profile")
    val profile: ComposePixivUserProfile? = null,
)

@Serializable
@Immutable
data class ComposePixivUserDetail(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String? = null,
    @SerialName("account")
    val account: String? = null,
    @SerialName("profile_image_urls")
    val profileImageUrls: ComposePixivProfileImageUrls? = null,
    @SerialName("comment")
    val comment: String? = null,
    @SerialName("is_followed")
    val isFollowed: Boolean = false,
)

@Serializable
@Immutable
data class ComposePixivUserProfile(
    @SerialName("webpage")
    val webpage: String? = null,
    @SerialName("gender")
    val gender: String? = null,
    @SerialName("birth")
    val birth: String? = null,
    @SerialName("region")
    val region: String? = null,
    @SerialName("job")
    val job: String? = null,
    @SerialName("country_code")
    val country: String? = null,
    @SerialName("twitter_account")
    val twitterAccount: String? = null,
    @SerialName("twitter_url")
    val twitterUrl: String? = null,
    @SerialName("pawoo_url")
    val pawooUrl: String? = null,
    @SerialName("is_premium")
    val isPremium: Boolean = false,
)

/**
 * 收藏插画结果
 */
@Serializable
@Immutable
data class ComposePixivBookmarkResult(
    @SerialName("is_bookmarked")
    val isBookmarked: Boolean = false,
)

/**
 * 关注用户结果
 */
@Serializable
@Immutable
data class ComposePixivFollowResult(
    @SerialName("is_followed")
    val isFollowed: Boolean = false,
)

/**
 * 相关插画搜索结果
 */
@Serializable
@Immutable
data class ComposePixivRelatedResult(
    @SerialName("illusts")
    val illusts: List<ComposePixivIllust> = emptyList(),
    @SerialName("next_url")
    val nextUrl: String? = null,
)

/**
 * 当前登录用户简要信息 (GET /v1/user/me/state)
 *
 * /v1/user/me 已被 Pixiv 废弃，返回 404。
 * /v1/user/me/state 返回 { "profile": { "user_id": ..., "name": ..., ... } }
 */
@Serializable
@Immutable
data class ComposePixivMeStateResponse(
    @SerialName("profile")
    val profile: ComposePixivMeStateProfile? = null,
)

@Serializable
@Immutable
data class ComposePixivMeStateProfile(
    @SerialName("user_id")
    val userId: Long = 0,
    @SerialName("pixiv_id")
    val pixivId: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("profile_image_urls")
    val profileImageUrls: ComposePixivProfileImageUrls? = null,
    @SerialName("is_premium")
    val isPremium: Boolean = false,
)

/**
 * 当前登录用户信息 — 从 /v1/user/me/state + /v1/user/detail 联合映射后供 UI 使用
 */
@Serializable
@Immutable
data class ComposePixivCurrentUser(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String? = null,
    @SerialName("account")
    val account: String? = null,
    @SerialName("profile_image_urls")
    val profileImageUrls: ComposePixivProfileImageUrls? = null,
    @SerialName("comment")
    val comment: String? = null,
    @SerialName("is_premium")
    val isPremium: Boolean = false,
)
