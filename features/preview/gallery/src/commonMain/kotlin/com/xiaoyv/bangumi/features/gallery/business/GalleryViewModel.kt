package com.xiaoyv.bangumi.features.gallery.business

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.SavedStateHandle
import com.xiaoyv.bangumi.core_resource.resources.Res
import com.xiaoyv.bangumi.core_resource.resources.pixiv_download_fail
import com.xiaoyv.bangumi.core_resource.resources.pixiv_download_success
import com.xiaoyv.bangumi.core_resource.resources.pixiv_watch_later_added
import com.xiaoyv.bangumi.core_resource.resources.pixiv_watch_later_removed
import com.xiaoyv.bangumi.shared.System
import com.xiaoyv.bangumi.shared.core.mvi.BaseSyntax
import com.xiaoyv.bangumi.shared.core.mvi.BaseViewModel
import com.xiaoyv.bangumi.shared.core.types.list.ListAlbumType
import com.xiaoyv.bangumi.shared.core.utils.awaitAll
import com.xiaoyv.bangumi.shared.core.utils.debugLog
import com.xiaoyv.bangumi.shared.core.utils.defaultJson
import com.xiaoyv.bangumi.shared.core.utils.errMsg
import com.xiaoyv.bangumi.shared.core.utils.fromJson
import com.xiaoyv.bangumi.shared.data.manager.app.UserManager
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivComment
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivWatchLaterItem
import com.xiaoyv.bangumi.shared.data.repository.CacheRepository
import com.xiaoyv.bangumi.shared.data.repository.readViewModelCache
import com.xiaoyv.bangumi.shared.data.repository.writeViewModelCache
import com.xiaoyv.bangumi.shared.data.usecase.ImageRepoUseCase
import com.xiaoyv.bangumi.shared.data.usecase.PixivRepoUseCase
import com.xiaoyv.bangumi.shared.ui.component.navigation.Screen
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import org.jetbrains.compose.resources.getString

/**
 * [GalleryViewModel]
 *
 * @author why
 * @since 2025/1/12
 */
class GalleryViewModel(
    savedStateHandle: SavedStateHandle,
    private val args: Screen.Gallery,
    private val imageRepoUseCase: ImageRepoUseCase,
    private val pixivRepoUseCase: PixivRepoUseCase,
    private val cacheRepository: CacheRepository,
    private val userManager: UserManager,
) : BaseViewModel<GalleryState, GallerySideEffect, GalleryEvent.Action>(savedStateHandle) {

    private val cacheKey = stringPreferencesKey(name = "gallery_${args.type}_" + args.id)

    private companion object {
        const val HOST_PIXIV_IMAGE = "i.pximg.net"

        /**
         * 屏蔽标签缓存 Key（本地存储，JSON ListSerializer<String>），
         * ImageRepositoryImpl 的 Pixiv 搜索也会读取同一份用于过滤。
         */
        const val KEY_PIXIV_BANNED_TAGS = "pixiv_banned_tags"

        /**
         * 收藏标签缓存 Key（本地存储，JSON ListSerializer<String>）
         */
        const val KEY_PIXIV_BOOKMARKED_TAGS = "pixiv_bookmarked_tags"
    }

    private fun readTagList(key: String): List<String> {
        val json = cacheRepository.readSync(stringPreferencesKey(key), "")
        return if (json.isBlank()) emptyList() else runCatching {
            json.fromJson<List<String>>()
        }.getOrNull() ?: emptyList()
    }

    private suspend fun writeTagList(key: String, tags: List<String>) {
        cacheRepository.write(stringPreferencesKey(key), defaultJson.encodeToString(ListSerializer(String.serializer()), tags))
    }

    /**
     * Pixiv 稍后再看列表缓存 Key（本地存储，与 [ComposePixivWatchLaterItem] 序列化列表）
     */
    private val watchLaterCacheKey = stringPreferencesKey(name = "pixiv_watch_later")

    private fun readWatchLaterList(): List<ComposePixivWatchLaterItem> {
        val json = cacheRepository.readSync(watchLaterCacheKey, "")
        return if (json.isBlank()) emptyList() else runCatching {
            json.fromJson<List<ComposePixivWatchLaterItem>>()
        }.getOrNull() ?: emptyList()
    }

    override fun initBaseState() = readViewModelCache(
        cacheRepository = cacheRepository,
        cacheKey = cacheKey,
        loadWhenEmpty = true
    )

    override fun initSate(onCreate: Boolean) = GalleryState(
        id = args.id,
        isPixiv = args.type == ListAlbumType.PIVIX,
        isWatchLater = readWatchLaterList().any { it.id == args.id },
        bannedTags = readTagList(KEY_PIXIV_BANNED_TAGS),
        bookmarkedTags = readTagList(KEY_PIXIV_BOOKMARKED_TAGS),
    )

    override suspend fun BaseSyntax<GalleryState, GallerySideEffect>.refreshSync() {
        // 加载图片列表
        imageRepoUseCase.fetchPictureGallery(args.id, args.type)
            .onFailure { reduceError { it } }
            .onSuccess { images ->
                reduceContent { state.copy(images = images) }
            }

        // 如果是 Pixiv，也加载作品详情
        if (args.type == ListAlbumType.PIVIX) {
            imageRepoUseCase.fetchPixivIllustDetail(args.id)
                .onSuccess { illust ->
                    reduceContent {
                        state.copy(
                            illust = illust,
                            isBookmarked = illust.isBookmarked,
                            isFollowed = illust.user?.isFollowed ?: false,
                        )
                    }
                }
                .onFailure {
                    debugLog { "Failed to load pixiv illust detail: ${it.message}" }
                }

            // 并行加载评论与相关图片
            reduceContent { state.copy(commentsLoading = true, relatedLoading = true) }
            awaitAll(
                block1 = { pixivRepoUseCase.fetchIllustComments(args.id.toLongOrNull() ?: 0L) },
                block2 = { pixivRepoUseCase.fetchRelatedIllusts(args.id.toLongOrNull() ?: 0L) },
            ).onSuccess {
                reduceContent {
                    state.copy(
                        comments = it.data1,
                        relatedIllusts = it.data2,
                        commentsLoading = false,
                        relatedLoading = false,
                    )
                }
            }.onFailure {
                reduceContent {
                    state.copy(
                        commentsLoading = false,
                        relatedLoading = false,
                    )
                }
            }
        }

        writeViewModelCache(
            cacheRepository = cacheRepository,
            cacheKey = cacheKey,
        )
    }

    override fun onEvent(event: GalleryEvent.Action) {
        when (event) {
            is GalleryEvent.Action.OnRefresh -> refresh(loading = event.loading)
            is GalleryEvent.Action.OnOpenInBrowser -> onOpenInBrowser()
            is GalleryEvent.Action.OnToggleBookmark -> onToggleBookmark()
            is GalleryEvent.Action.OnToggleFollow -> onToggleFollow()
            is GalleryEvent.Action.OnToggleShowOriginal -> onToggleShowOriginal()
            is GalleryEvent.Action.OnToggleWatchLater -> onToggleWatchLater()
            is GalleryEvent.Action.OnShare -> onShare()
            is GalleryEvent.Action.OnCopyLink -> onCopyLink()
            is GalleryEvent.Action.OnTagClick -> onTagClick(event.tag)
            is GalleryEvent.Action.OnDownload -> onDownload()
            is GalleryEvent.Action.OnToggleReplies -> onToggleReplies(event.commentId)
            is GalleryEvent.Action.OnCommentInputChange -> onCommentInputChange(event.text)
            is GalleryEvent.Action.OnReplyTarget -> onReplyTarget(event.comment)
            is GalleryEvent.Action.OnSendComment -> onSendComment()
            is GalleryEvent.Action.OnBanTag -> onBanTag(event.tag)
            is GalleryEvent.Action.OnBookmarkTag -> onBookmarkTag(event.tag)
            is GalleryEvent.Action.OnCopyTag -> onCopyTag(event.tag)
        }
    }

    private fun onOpenInBrowser() = action {
        val currentState = stateRaw
        val id = currentState.id.toLongOrNull() ?: return@action
        postEffect {
            GallerySideEffect.OpenInBrowser("https://www.pixiv.net/artworks/$id")
        }
    }

    private fun onToggleBookmark() = action {
        val currentState = stateRaw
        val illust = currentState.illust ?: return@action
        val newBookmarked = !currentState.isBookmarked
        reduceContent { state.copy(isLoadingAction = true) }

        val result = pixivRepoUseCase.bookmarkIllust(illust.id, currentState.isBookmarked)
        result.onSuccess {
            reduceContent { state.copy(isBookmarked = newBookmarked, isLoadingAction = false) }
            postToast { if (newBookmarked) "已收藏" else "已取消收藏" }
        }.onFailure {
            reduceContent { state.copy(isLoadingAction = false) }
            postToast { it.errMsg.ifBlank { "操作失败" } }
        }
    }

    private fun onToggleFollow() = action {
        val currentState = stateRaw
        val illust = currentState.illust ?: return@action
        val userId = illust.user?.id ?: return@action
        val newFollowed = !currentState.isFollowed
        reduceContent { state.copy(isLoadingAction = true) }

        val result = pixivRepoUseCase.followUser(userId, currentState.isFollowed)
        result.onSuccess {
            reduceContent { state.copy(isFollowed = newFollowed, isLoadingAction = false) }
            postToast { if (newFollowed) "已关注" else "已取消关注" }
        }.onFailure {
            reduceContent { state.copy(isLoadingAction = false) }
            postToast { it.errMsg.ifBlank { "操作失败" } }
        }
    }

    private fun onToggleShowOriginal() = action {
        reduceContent { state.copy(showOriginal = !state.showOriginal) }
    }

    private fun onToggleWatchLater() = action {
        val currentState = stateRaw
        val illust = currentState.illust ?: return@action
        val newWatchLater = !currentState.isWatchLater

        val currentList = readWatchLaterList()
        val newList = if (newWatchLater) {
            // 移除同 ID 旧条目后追加，保证最新在前
            currentList.filterNot { it.id == currentState.id } + ComposePixivWatchLaterItem(
                id = currentState.id,
                title = illust.title.orEmpty(),
                thumb = illust.imageUrls?.medium.orEmpty(),
                author = illust.user?.name.orEmpty(),
                authorId = illust.user?.id ?: 0,
                time = System.currentTimeMillis(),
            )
        } else {
            currentList.filterNot { it.id == currentState.id }
        }

        cacheRepository.write(
            watchLaterCacheKey,
            defaultJson.encodeToString(ListSerializer(ComposePixivWatchLaterItem.serializer()), newList)
        )
        reduceContent { state.copy(isWatchLater = newWatchLater) }
        postToast {
            getString(if (newWatchLater) Res.string.pixiv_watch_later_added else Res.string.pixiv_watch_later_removed)
        }
    }

    private fun onShare() = action {
        val currentState = stateRaw
        val id = currentState.id.toLongOrNull() ?: return@action
        val title = currentState.illust?.title ?: ""
        System.shareText("$title https://www.pixiv.net/artworks/$id")
    }

    private fun onCopyLink() = action {
        val currentState = stateRaw
        val id = currentState.id.toLongOrNull() ?: return@action
        System.createClipEntry("https://www.pixiv.net/artworks/$id")
        postToast { "已复制链接" }
    }

    private fun onDownload() = action {
        val currentState = stateRaw
        val images = currentState.images
        if (images.isEmpty()) return@action
        // 取当前可见的原图 URL
        val url = if (currentState.showOriginal) {
            images.firstOrNull()?.original?.ifBlank { images.first().image }
        } else {
            images.first().image
        } ?: return@action

        // Pixiv 图片走代理重写，避免 i.pximg.net 防盗链 403
        val downloadUrl = if (url.contains(HOST_PIXIV_IMAGE)) {
            userManager.settings.network.pixivImageHost + url.substringAfter(HOST_PIXIV_IMAGE).trimStart('/')
        } else {
            url
        }

        // 文件名：作品 ID + 原始扩展名（默认 jpg）
        val extension = url.substringAfterLast('.', "jpg").takeIf { it.length in 3..4 } ?: "jpg"
        val fileName = "${currentState.id}.$extension"
        val subDir = userManager.pixivDownloadDir

        System.downloadImage(downloadUrl, fileName, subDir)
            .onSuccess { path ->
                postToast { getString(Res.string.pixiv_download_success, path) }
            }
            .onFailure {
                debugLog { "downloadImage FAILED=${it.message}" }
                postToast { getString(Res.string.pixiv_download_fail, it.errMsg.ifBlank { "unknown" }) }
            }
    }

    private fun onTagClick(tag: String) = action {
        if (tag == "similar") return@action
        postEffect { GallerySideEffect.NavigateToTagSearch(tag) }
    }

    // ---------------- 评论 ----------------

    /**
     * 展开/收起某条评论的回复列表
     */
    private fun onToggleReplies(commentId: Long) = action {
        val currentState = stateRaw
        val expanded = currentState.expandedReplyIds
        if (commentId in expanded) {
            reduceContent { state.copy(expandedReplyIds = expanded - commentId) }
            return@action
        }

        // 已加载过回复则直接展开
        val target = currentState.comments.firstOrNull { it.id == commentId }
        if (target?.replies?.isNotEmpty() == true) {
            reduceContent { state.copy(expandedReplyIds = expanded + commentId) }
            return@action
        }

        pixivRepoUseCase.fetchCommentReplies(commentId)
            .onSuccess { replies ->
                reduceContent {
                    state.copy(
                        comments = state.comments.map { comment ->
                            if (comment.id == commentId) comment.copy(replies = replies) else comment
                        },
                        expandedReplyIds = state.expandedReplyIds + commentId,
                    )
                }
            }
            .onFailure {
                postToast { it.errMsg.ifBlank { "加载回复失败" } }
            }
    }

    private fun onCommentInputChange(text: String) = action {
        reduceContent { state.copy(commentInput = text) }
    }

    private fun onReplyTarget(comment: ComposePixivComment?) = action {
        reduceContent { state.copy(replyTarget = comment) }
    }

    /**
     * 发表评论（或回复评论）
     */
    private fun onSendComment() = action {
        val currentState = stateRaw
        val text = currentState.commentInput.trim()
        if (text.isBlank()) return@action
        val illustId = currentState.id.toLongOrNull() ?: return@action
        val parent = currentState.replyTarget

        withActionLoading {
            pixivRepoUseCase.addIllustComment(illustId, text, parent?.id)
        }.onSuccess { newComment ->
            if (newComment != null) {
                reduceContent {
                    state.copy(
                        comments = if (parent != null) {
                            // 回复：插入到父评论的 replies 中
                            state.comments.map { comment ->
                                if (comment.id == parent.id) {
                                    comment.copy(
                                        replies = comment.replies + newComment,
                                        hasReplies = true,
                                    )
                                } else {
                                    comment
                                }
                            }
                        } else {
                            // 新评论：插入到列表顶部
                            listOf(newComment) + state.comments
                        },
                        commentInput = "",
                        replyTarget = null,
                        expandedReplyIds = if (parent != null) {
                            state.expandedReplyIds + parent.id
                        } else {
                            state.expandedReplyIds
                        },
                    )
                }
                postToast { "评论成功" }
            }
        }.onFailure {
            postToast { it.errMsg.ifBlank { "评论失败" } }
        }
    }

    // ---------------- 标签操作 ----------------

    /**
     * 屏蔽标签：加入本地屏蔽列表，之后 Pixiv 搜索会过滤该标签的作品
     */
    private fun onBanTag(tag: String) = action {
        val currentState = stateRaw
        val newList = (currentState.bannedTags + tag).distinct()
        writeTagList(KEY_PIXIV_BANNED_TAGS, newList)
        reduceContent { state.copy(bannedTags = newList) }
        postToast { "已屏蔽标签：#$tag" }
    }

    /**
     * 收藏标签：加入本地收藏列表
     */
    private fun onBookmarkTag(tag: String) = action {
        val currentState = stateRaw
        val newList = (currentState.bookmarkedTags + tag).distinct()
        writeTagList(KEY_PIXIV_BOOKMARKED_TAGS, newList)
        reduceContent { state.copy(bookmarkedTags = newList) }
        postToast { "已收藏标签：#$tag" }
    }

    /**
     * 复制标签
     */
    private fun onCopyTag(tag: String) = action {
        System.createClipEntry("#$tag")
        postToast { "已复制标签" }
    }
}
