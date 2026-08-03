package com.xiaoyv.bangumi.shared.data.model.response.pixiv

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Pixiv 官方 App API 搜索结果
 *
 * GET https://app-api.pixiv.net/v1/search/illust
 */
@Serializable
@Immutable
data class ComposePixivIllustSearchResult(
    @SerialName("illusts")
    val illusts: List<ComposePixivIllust> = emptyList(),
    @SerialName("next_url")
    val nextUrl: String? = null,
)

/**
 * Pixiv 官方 App API 作品详情
 *
 * GET https://app-api.pixiv.net/v1/illust/detail
 */
@Serializable
@Immutable
data class ComposePixivIllustDetailResult(
    @SerialName("illust")
    val illust: ComposePixivIllust? = null,
)

/**
 * Pixiv 插画作品
 */
@Serializable
@Immutable
data class ComposePixivIllust(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("title")
    val title: String? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("image_urls")
    val imageUrls: ComposePixivImageUrls? = null,
    @SerialName("caption")
    val caption: String? = null,
    @SerialName("restrict")
    val restrict: Int = 0,
    @SerialName("user")
    val user: ComposePixivUser? = null,
    @SerialName("tags")
    val tags: List<ComposePixivTag> = emptyList(),
    @SerialName("create_date")
    val createDate: String? = null,
    @SerialName("page_count")
    val pageCount: Int = 1,
    @SerialName("width")
    val width: Int = 0,
    @SerialName("height")
    val height: Int = 0,
    @SerialName("total_view")
    val totalView: Int = 0,
    @SerialName("total_bookmarks")
    val totalBookmarks: Int = 0,
    @SerialName("is_bookmarked")
    val isBookmarked: Boolean = false,
    @SerialName("visible")
    val visible: Boolean = true,
    @SerialName("meta_single_page")
    val metaSinglePage: ComposePixivMetaSinglePage? = null,
    @SerialName("meta_pages")
    val metaPages: List<ComposePixivMetaPage> = emptyList(),
) {
    /**
     * 获取原图 URL（单页作品）
     */
    val originalUrl: String?
        get() = metaSinglePage?.originalImageUrl
            ?: metaPages.firstOrNull()?.imageUrls?.original
            ?: imageUrls?.large

    /**
     * 获取预览图 URL（large 尺寸）
     */
    val previewUrl: String?
        get() = imageUrls?.large ?: imageUrls?.medium
}

@Serializable
@Immutable
data class ComposePixivImageUrls(
    @SerialName("square_medium")
    val squareMedium: String? = null,
    @SerialName("medium")
    val medium: String? = null,
    @SerialName("large")
    val large: String? = null,
    @SerialName("original")
    val original: String? = null,
)

@Serializable
@Immutable
data class ComposePixivMetaSinglePage(
    @SerialName("original_image_url")
    val originalImageUrl: String? = null,
)

@Serializable
@Immutable
data class ComposePixivMetaPage(
    @SerialName("image_urls")
    val imageUrls: ComposePixivImageUrls? = null,
)

@Serializable
@Immutable
data class ComposePixivTag(
    @SerialName("name")
    val name: String? = null,
    @SerialName("translated_name")
    val translatedName: String? = null,
)

@Serializable
@Immutable
data class ComposePixivUser(
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
data class ComposePixivProfileImageUrls(
    @SerialName("medium")
    val medium: String? = null,
)
