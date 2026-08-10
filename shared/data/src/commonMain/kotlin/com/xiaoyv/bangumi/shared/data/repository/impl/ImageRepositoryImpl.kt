package com.xiaoyv.bangumi.shared.data.repository.impl

import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.xiaoyv.bangumi.shared.core.types.list.ListAlbumType
import com.xiaoyv.bangumi.shared.core.utils.debugLog
import com.xiaoyv.bangumi.shared.core.utils.fromJson
import com.xiaoyv.bangumi.shared.core.utils.parseHtmlHexColor
import com.xiaoyv.bangumi.shared.core.utils.runResult
import com.xiaoyv.bangumi.shared.core.utils.toApiOffset
import com.xiaoyv.bangumi.shared.data.api.client.BgmApiClient
import com.xiaoyv.bangumi.shared.data.model.request.list.album.ListAlbumParam
import com.xiaoyv.bangumi.shared.data.model.response.bgm.ComposeMono
import com.xiaoyv.bangumi.shared.data.model.response.image.ComposeGallery
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivIllust
import com.xiaoyv.bangumi.shared.data.parser.bgm.SubjectParser
import com.xiaoyv.bangumi.shared.data.repository.CacheRepository
import com.xiaoyv.bangumi.shared.data.repository.ImageRepository
import com.xiaoyv.bangumi.shared.data.repository.datasource.createNetworkKeyLimitPagingPager
import com.xiaoyv.bangumi.shared.data.repository.datasource.createNetworkPageLimitPagingPager
import com.xiaoyv.bangumi.shared.data.repository.datasource.createPagingConfig
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.roundToInt

/**
 * [ImageRepositoryImpl]
 *
 * @since 2025/5/22
 */
class ImageRepositoryImpl(
    private val client: BgmApiClient,
    private val pagingConfig: PagingConfig,
    private val subjectParser: SubjectParser,
    private val cacheRepository: CacheRepository,
) : ImageRepository {

    private companion object {
        /**
         * 屏蔽标签缓存 Key（与 GalleryViewModel 中一致），
         * Pixiv 搜索结果中命中屏蔽标签的作品会被过滤
         */
        const val KEY_PIXIV_BANNED_TAGS = "pixiv_banned_tags"
    }

    /**
     * 读取本地屏蔽标签列表（JSON ListSerializer<String>）
     */
    private fun readBannedTags(): Set<String> {
        val json = cacheRepository.readSync(stringPreferencesKey(KEY_PIXIV_BANNED_TAGS), "")
        if (json.isBlank()) return emptySet()
        return runCatching { json.fromJson<List<String>>() }.getOrNull()?.toSet() ?: emptySet()
    }

    override fun fetchAlbumPager(param: ListAlbumParam): Pager<Int, ComposeGallery> {
        return createNetworkPageLimitPagingPager(
            pagingConfig = pagingConfig,
            keySelector = { it.id },
            onLoadData = { page ->
                fetchAlbumList(param, page, pagingConfig.pageSize).getOrThrow()
            }
        )
    }


    override fun fetchAnimePictures(
        searchTags: String?,
        deniedTags: String?,
    ): Pager<Int, ComposeGallery> {
        return createNetworkPageLimitPagingPager(
            pagingConfig = pagingConfig,
            keySelector = { it.id },
            onLoadData = { page ->
                val picture = client.imageApi.fetchAnimePictures(
                    searchTags = searchTags,
                    deniedTags = deniedTags,
                    page = page,
                    size = pagingConfig.pageSize
                )
                picture.posts.orEmpty().map {
                    ComposeGallery(
                        id = it.id,
                        type = ListAlbumType.ANIME_PICTURES,
                        image = it.url,
                        original = it.largeUrl,
                        width = it.width,
                        height = it.height,
                        size = it.size,
                        color = it.color.orEmpty()
                    )
                }
            }
        )
    }

    override fun fetchPixivPictures(tag: String): Pager<String, ComposeGallery> {
        return createNetworkKeyLimitPagingPager(
            pagingConfig = createPagingConfig(30),
            keySelector = { it.id },
            onLoadData = { nextUrl ->
                debugLog { "Pixiv search: tag=$tag, nextUrl=${nextUrl ?: "initial"}" }
                try {
                    val bannedTags = readBannedTags()
                    val result = if (nextUrl == null) {
                        client.pixivApi.searchIllust(
                            word = tag,
                            searchTarget = "partial_match_for_tags",
                            sort = "date_desc",
                        )
                    } else {
                        require(nextUrl.startsWith("https://app-api.pixiv.net/")) {
                            "Unexpected Pixiv search cursor URL"
                        }
                        client.pixivApi.searchIllustByUrl(nextUrl)
                    }
                    val followingUrl = result.nextUrl?.takeIf { it.isNotBlank() && it != nextUrl }
                    debugLog {
                        "Pixiv search success: count=${result.illusts.size}, " +
                            "nextUrl=$followingUrl, bannedTags=${bannedTags.size}"
                    }
                    result.illusts
                        .filter { it.visible }
                        // 过滤命中屏蔽标签的作品（匹配日文原 tag 或翻译名）
                        .filter { illust ->
                            bannedTags.isEmpty() || illust.tags.none { tagItem ->
                                val name = tagItem.name
                                val translated = tagItem.translatedName
                                (name != null && name in bannedTags) ||
                                    (translated != null && translated in bannedTags)
                            }
                        }
                        .map { illust ->
                            val originalUrl = illust.originalUrl.orEmpty()
                            val previewUrl = illust.previewUrl.orEmpty()

                            ComposeGallery(
                                id = illust.id.toString(),
                                type = ListAlbumType.PIVIX,
                                image = previewUrl,
                                original = originalUrl,
                                width = illust.width,
                                height = illust.height,
                                count = illust.pageCount
                            )
                        } to followingUrl
                } catch (e: Exception) {
                    debugLog { "Pixiv search error: ${e.message}" }
                    throw e
                }
            }
        )
    }

    override suspend fun fetchAlbumList(param: ListAlbumParam, page: Int, size: Int): Result<List<ComposeGallery>> {
        return when (param.type) {
            ListAlbumType.CHARACTER_ALBUM -> client.requestWebApi {
                with(subjectParser) {
                    fetchCharacterAlbum(param.characterId, page)
                        .fetchCharacterAlbumCoverted()
                        .map { it.copy(type = param.type) }
                }
            }

            ListAlbumType.SUBJECT_PREVIEW -> client.requestDouBanApi {
                client.dbApi
                    .queryDouBanPhotoList(param.doubanId, param.doubanType, page.toApiOffset(size), size)
                    .copy(doubanMediaId = param.doubanId)
                    .photos.map {
                        val hexColor = parseHtmlHexColor(it.image.primaryColor.orEmpty()) ?: Color.LightGray
                        val largeImage = it.displayLargeImage

                        ComposeGallery(
                            id = largeImage.url.orEmpty(),
                            type = param.type,
                            width = largeImage.width,
                            height = largeImage.height,
                            image = largeImage.url.orEmpty(),
                            original = largeImage.url.orEmpty(),
                            color = listOf(
                                (hexColor.red * 255).roundToInt().coerceIn(0, 255),
                                (hexColor.green * 255).roundToInt().coerceIn(0, 255),
                                (hexColor.blue * 255).roundToInt().coerceIn(0, 255),
                            )
                        )
                    }
            }

            else -> error("not support")
        }
    }


    override suspend fun fetchPixivPictureDetail(id: String): Result<List<ComposeGallery>> =
        runResult {
            val illust = client.pixivApi.getIllustDetail(illustId = id.toLong()).illust
                ?: error("Illust not found: $id")

            // 多页作品：从 metaPages 获取每页原图
            if (illust.metaPages.isNotEmpty()) {
                illust.metaPages.mapIndexed { index, page ->
                    ComposeGallery(
                        id = "${illust.id}_$index",
                        type = ListAlbumType.PIVIX,
                        image = page.imageUrls?.large.orEmpty(),
                        original = page.imageUrls?.original.orEmpty(),
                        width = illust.width,
                        height = illust.height,
                        count = illust.metaPages.size
                    )
                }
            } else {
                // 单页作品
                listOf(
                    ComposeGallery(
                        id = illust.id.toString(),
                        type = ListAlbumType.PIVIX,
                        image = illust.previewUrl.orEmpty(),
                        original = illust.originalUrl.orEmpty(),
                        width = illust.width,
                        height = illust.height,
                        count = 1
                    )
                )
            }
        }

    override suspend fun fetchPixivIllust(id: String): Result<ComposePixivIllust> =
        runResult {
            val illust = client.pixivApi.getIllustDetail(illustId = id.toLong()).illust
                ?: error("Illust not found: $id")
            val webStats = client.requestPixivApi {
                getIllustWebDetail(illustId = id.toLong())
            }.getOrNull()
                ?.takeUnless { it.error }
                ?.body

            // App API 没有 likeCount，使用网页端 ajax 统计补齐；网页请求失败时保留 App API 数据。
            webStats?.let { stats ->
                illust.copy(
                    totalBookmarks = stats.bookmarkCount,
                    likeCount = stats.likeCount,
                    commentCount = stats.commentCount,
                    responseCount = stats.responseCount,
                    totalView = stats.viewCount,
                )
            } ?: illust
        }

    override suspend fun fetchAnimePictureTag(data: ComposeMono): Result<List<String>> =
        runResult {
            val names = arrayListOf<String>()
            names.add(data.name)

            val nameInfo = data.infobox.find { it.key == "别名" }?.value
            if (nameInfo !is JsonArray) {
                val string = nameInfo?.jsonPrimitive?.contentOrNull.orEmpty()
                if (string.isNotBlank()) names.add(string)
            } else {
                names.addAll(nameInfo.mapNotNull {
                    (it as? JsonObject)?.getValue("v")?.jsonPrimitive?.contentOrNull
                })
            }

            // 原始的 Tag
            names
                .filterNot { it.isBlank() || it.matches(Regex("^[\\u4e00-\\u9fa5]+$")) }
                .distinct()
        }
}
