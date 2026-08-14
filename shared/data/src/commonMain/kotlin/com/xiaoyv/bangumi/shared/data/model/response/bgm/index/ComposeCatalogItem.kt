package com.xiaoyv.bangumi.shared.data.model.response.bgm.index

import androidx.compose.runtime.Immutable
import com.xiaoyv.bangumi.shared.core.utils.formatMills
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 精选目录条目（来自离线 protobuf 数据）
 *
 * 对应 election 版 proto schema:
 * message Catalog {
 *   int32 i = 1;   // id
 *   string d = 2;  // description/date
 *   string l = 3;  // last update
 *   string t = 4;  // title
 *   int32 a = 5;   // anime count
 *   int32 b = 6;   // book count
 *   int32 m = 7;   // music count
 *   int32 g = 8;   // game count
 *   int32 r = 9;   // real count
 *   int32 ch = 10; // character count
 *   int32 pe = 11; // person count
 *   int32 to = 12; // topic count
 *   int32 bl = 13; // blog count
 *   int32 ep = 14; // episode count
 * }
 */
@Immutable
@Serializable
data class ComposeCatalogItem(
    @SerialName("i") val id: Int = 0,
    @SerialName("d") val date: String = "",
    @SerialName("l") val lastUpdate: String = "",
    @SerialName("t") val title: String = "",
    @SerialName("a") val anime: Int = 0,
    @SerialName("b") val book: Int = 0,
    @SerialName("m") val music: Int = 0,
    @SerialName("g") val game: Int = 0,
    @SerialName("r") val real: Int = 0,
    @SerialName("ch") val character: Int = 0,
    @SerialName("pe") val person: Int = 0,
    @SerialName("to") val topic: Int = 0,
    @SerialName("bl") val blog: Int = 0,
    @SerialName("ep") val episode: Int = 0,
) {
    /** 总收录数 */
    val total: Int get() = anime + book + music + game + real + character + person + topic + blog + episode

    /** 主要类型（数量最多的类型） */
    val primaryType: String
        get() {
            val types = listOf(
                "anime" to anime,
                "book" to book,
                "music" to music,
                "game" to game,
                "real" to real,
                "character" to character,
                "person" to person,
                "topic" to topic,
                "blog" to blog,
                "episode" to episode,
            )
            return types.maxByOrNull { it.second }?.first ?: ""
        }

    /** 转换为 ComposeIndex 用于列表显示 */
    fun toComposeIndex(): ComposeIndex {
        return ComposeIndex(
            id = id.toLong(),
            title = title,
            desc = date,
            updatedAt = lastUpdate.formatMills(),
            total = total,
            category = kotlinx.collections.immutable.persistentMapOf(
                "anime" to anime,
                "book" to book,
                "music" to music,
                "game" to game,
                "real" to real,
            )
        )
    }
}
