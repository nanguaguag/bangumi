package com.xiaoyv.bangumi.features.gallery.business

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.SavedStateHandle
import com.xiaoyv.bangumi.core_resource.resources.Res
import com.xiaoyv.bangumi.core_resource.resources.pixiv_watch_later_added
import com.xiaoyv.bangumi.core_resource.resources.pixiv_watch_later_removed
import com.xiaoyv.bangumi.shared.System
import com.xiaoyv.bangumi.shared.core.mvi.BaseSyntax
import com.xiaoyv.bangumi.shared.core.mvi.BaseViewModel
import com.xiaoyv.bangumi.shared.core.types.list.ListAlbumType
import com.xiaoyv.bangumi.shared.core.utils.debugLog
import com.xiaoyv.bangumi.shared.core.utils.defaultJson
import com.xiaoyv.bangumi.shared.core.utils.errMsg
import com.xiaoyv.bangumi.shared.core.utils.fromJson
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivWatchLaterItem
import com.xiaoyv.bangumi.shared.data.repository.CacheRepository
import com.xiaoyv.bangumi.shared.data.repository.readViewModelCache
import com.xiaoyv.bangumi.shared.data.repository.writeViewModelCache
import com.xiaoyv.bangumi.shared.data.usecase.ImageRepoUseCase
import com.xiaoyv.bangumi.shared.data.usecase.PixivRepoUseCase
import com.xiaoyv.bangumi.shared.ui.component.navigation.Screen
import kotlinx.serialization.builtins.ListSerializer
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
) : BaseViewModel<GalleryState, GallerySideEffect, GalleryEvent.Action>(savedStateHandle) {

    private val cacheKey = stringPreferencesKey(name = "gallery_${args.type}_" + args.id)

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
        postEffect { GallerySideEffect.OpenDownload(url) }
    }

    private fun onTagClick(tag: String) = action {
        if (tag == "similar") return@action
        postEffect { GallerySideEffect.NavigateToTagSearch(tag) }
    }
}
