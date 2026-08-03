package com.xiaoyv.bangumi.features.gallery.business

import androidx.compose.runtime.Immutable
import com.xiaoyv.bangumi.shared.data.model.response.image.ComposeGallery
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivIllust
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivUserDetail
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    @SerialName("isLoadingAction") val isLoadingAction: Boolean = false,
)
