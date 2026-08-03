package com.xiaoyv.bangumi.shared.data.model.response.pixiv

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Pixiv 稍后再看条目（本地存储）
 *
 * 参考 Pixiv-MultiPlatform 的 WatchLaterItem：本地保存作品 ID 与展示元数据，
 * 用户可在稍后再看列表中重新打开作品。
 */
@Serializable
@Immutable
data class ComposePixivWatchLaterItem(
    @SerialName("id") val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("thumb") val thumb: String = "",
    @SerialName("author") val author: String = "",
    @SerialName("authorId") val authorId: Long = 0,
    @SerialName("time") val time: Long = 0,
)
