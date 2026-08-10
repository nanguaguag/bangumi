package com.xiaoyv.bangumi.features.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xiaoyv.bangumi.core_resource.resources.Res
import com.xiaoyv.bangumi.core_resource.resources.global_artwork
import com.xiaoyv.bangumi.core_resource.resources.global_copy_link
import com.xiaoyv.bangumi.core_resource.resources.global_file_size
import com.xiaoyv.bangumi.core_resource.resources.global_open_browser
import com.xiaoyv.bangumi.core_resource.resources.global_resolution
import com.xiaoyv.bangumi.core_resource.resources.global_share
import com.xiaoyv.bangumi.core_resource.resources.pixiv_bookmark
import com.xiaoyv.bangumi.core_resource.resources.pixiv_bookmarked
import com.xiaoyv.bangumi.core_resource.resources.pixiv_cancel
import com.xiaoyv.bangumi.core_resource.resources.pixiv_comment_empty
import com.xiaoyv.bangumi.core_resource.resources.pixiv_comment_reply_to
import com.xiaoyv.bangumi.core_resource.resources.pixiv_comment_view_replies
import com.xiaoyv.bangumi.core_resource.resources.pixiv_comments
import com.xiaoyv.bangumi.core_resource.resources.pixiv_download
import com.xiaoyv.bangumi.core_resource.resources.pixiv_follow
import com.xiaoyv.bangumi.core_resource.resources.pixiv_illust_id
import com.xiaoyv.bangumi.core_resource.resources.pixiv_related_illusts
import com.xiaoyv.bangumi.core_resource.resources.pixiv_show_original
import com.xiaoyv.bangumi.core_resource.resources.pixiv_tag_ban
import com.xiaoyv.bangumi.core_resource.resources.pixiv_tag_bookmark
import com.xiaoyv.bangumi.core_resource.resources.pixiv_tag_copy
import com.xiaoyv.bangumi.core_resource.resources.pixiv_unfollow
import com.xiaoyv.bangumi.core_resource.resources.pixiv_watch_later
import com.xiaoyv.bangumi.core_resource.resources.pixiv_watch_later_remove
import com.xiaoyv.bangumi.core_resource.resources.reply_comment
import com.xiaoyv.bangumi.core_resource.resources.reply_comment_hint
import com.xiaoyv.bangumi.core_resource.resources.reply_comment_send
import com.xiaoyv.bangumi.features.gallery.business.GalleryEvent
import com.xiaoyv.bangumi.features.gallery.business.GallerySideEffect
import com.xiaoyv.bangumi.features.gallery.business.GalleryState
import com.xiaoyv.bangumi.features.gallery.business.GalleryViewModel
import com.xiaoyv.bangumi.shared.core.mvi.BaseState
import com.xiaoyv.bangumi.shared.core.types.list.ListAlbumType
import com.xiaoyv.bangumi.shared.core.utils.formatFileSize
import com.xiaoyv.bangumi.shared.core.utils.formatPixivDateTime
import com.xiaoyv.bangumi.shared.data.model.response.image.ComposeGallery
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivComment
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivIllust
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivTag
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivUser
import com.xiaoyv.bangumi.shared.ui.component.bar.BgmTopAppBar
import com.xiaoyv.bangumi.shared.ui.component.image.StateImage
import com.xiaoyv.bangumi.shared.ui.component.layout.state.StateLayout
import com.xiaoyv.bangumi.shared.ui.component.navigation.Screen
import com.xiaoyv.bangumi.shared.ui.component.navigation.pixivArtworkSharedElement
import com.xiaoyv.bangumi.shared.ui.component.space.BrushVerticalTransparentToHalfBlack
import com.xiaoyv.bangumi.shared.ui.component.space.LayoutPaddingHalf
import com.xiaoyv.bangumi.shared.ui.kts.collectBaseSideEffect
import com.xiaoyv.bangumi.shared.ui.theme.BgmIcons
import org.jetbrains.compose.resources.stringResource
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun GalleryRoute(
    viewModel: GalleryViewModel,
    transitionImage: String,
    transitionAspect: Float,
    transitionArtworkId: String,
    onNavUp: () -> Unit,
    onNavScreen: (Screen) -> Unit,
) {
    val baseState by viewModel.collectAsState()
    val uriHandler = LocalUriHandler.current

    viewModel.collectBaseSideEffect {
        when (it) {
            is GallerySideEffect.OpenInBrowser -> {
                uriHandler.openUri(it.url)
            }
            is GallerySideEffect.NavigateToTagSearch -> {
                onNavScreen(Screen.PixivSearch(it.tag))
            }
        }
    }

    GalleryScreen(
        baseState = baseState,
        transitionImage = transitionImage,
        transitionAspect = transitionAspect,
        transitionArtworkId = transitionArtworkId,
        onActionEvent = viewModel::onEvent,
        onUiEvent = {
            when (it) {
                is GalleryEvent.UI.OnNavUp -> onNavUp()
                is GalleryEvent.UI.OnNavScreen -> onNavScreen(it.screen)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryScreen(
    baseState: BaseState<GalleryState>,
    transitionImage: String,
    transitionAspect: Float,
    transitionArtworkId: String,
    onUiEvent: (GalleryEvent.UI) -> Unit,
    onActionEvent: (GalleryEvent.Action) -> Unit,
) {
    val state = baseState.payload
    // 数据到达后，真实首图会接管同一共享 key；在此之前始终保留已缓存的列表图。
    val showTransitionHero = transitionImage.isNotBlank() && state?.images.isNullOrEmpty()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // 使用 BgmTopAppBar 并添加菜单按钮
            BgmTopAppBar(
                title = state.let {
                    if (it == null) stringResource(Res.string.global_artwork)
                    else stringResource(Res.string.global_artwork) + "：${it.id}"
                },
                onNavigationClick = { onUiEvent(GalleryEvent.UI.OnNavUp) },
                actions = {
                    // 菜单按钮
                    GalleryTopBarMenu(state = state ?: GalleryState(), onActionEvent)
                }
            )
        },
        bottomBar = {
            // Pixiv 作品页底部评论输入栏
            val current = state
            if (current?.isPixiv == true && current.illust != null) {
                CommentInputBar(state = current, onActionEvent = onActionEvent)
            }
        },
        floatingActionButton = {
            // 右下角收藏按钮（elevated button）
            val current = state
            if (current?.isPixiv == true && current.illust != null) {
                ExtendedFloatingActionButton(
                    onClick = { onActionEvent(GalleryEvent.Action.OnToggleBookmark) },
                    containerColor = if (current.isBookmarked) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.primaryContainer,
                    icon = {
                        Icon(
                            imageVector = if (current.isBookmarked) BgmIcons.Bookmark else BgmIcons.BookmarkBorder,
                            contentDescription = null,
                        )
                    },
                    text = {
                        Text(
                            stringResource(
                                if (current.isBookmarked) Res.string.pixiv_bookmarked
                                else Res.string.pixiv_bookmark
                            )
                        )
                    },
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            StateLayout(
                modifier = Modifier.fillMaxSize(),
                onRefresh = { onActionEvent(GalleryEvent.Action.OnRefresh(it)) },
                baseState = baseState,
            ) { contentState ->
                if (contentState.isPixiv) {
                    PixivGalleryContent(
                        state = contentState,
                        transitionImage = transitionImage,
                        transitionArtworkId = transitionArtworkId,
                        onUiEvent = onUiEvent,
                        onActionEvent = onActionEvent,
                    )
                } else {
                    GalleryImageGrid(contentState, onUiEvent, onActionEvent)
                }
            }

            if (showTransitionHero) {
                PixivGalleryTransitionHero(
                    image = transitionImage,
                    aspect = transitionAspect,
                    artworkId = transitionArtworkId,
                )
            }
        }
    }
}

@Composable
private fun PixivGalleryTransitionHero(
    image: String,
    aspect: Float,
    artworkId: String,
) {
    StateImage(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspect.coerceIn(0.25f, 4f))
            .pixivArtworkSharedElement(artworkId),
        model = image,
        contentDescription = null,
        blurLoading = false,
    )
}

@Composable
private fun GalleryTopBarMenu(
    state: GalleryState,
    onActionEvent: (GalleryEvent.Action) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = BgmIcons.MoreVert,
                contentDescription = "More",
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.global_open_browser)) },
                leadingIcon = { Icon(BgmIcons.OpenInBrowser, null) },
                onClick = {
                    expanded = false
                    onActionEvent(GalleryEvent.Action.OnOpenInBrowser)
                }
            )
            if (state.isPixiv) {
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(
                                if (state.isWatchLater) Res.string.pixiv_watch_later_remove
                                else Res.string.pixiv_watch_later
                            )
                        )
                    },
                    leadingIcon = {
                        Icon(
                            if (state.isWatchLater) BgmIcons.Bookmark else BgmIcons.BookmarkBorder,
                            null
                        )
                    },
                    onClick = {
                        expanded = false
                        onActionEvent(GalleryEvent.Action.OnToggleWatchLater)
                    }
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.global_copy_link)) },
                leadingIcon = { Icon(BgmIcons.ContentCopy, null) },
                onClick = {
                    expanded = false
                    onActionEvent(GalleryEvent.Action.OnCopyLink)
                }
            )
            if (state.isPixiv) {
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(Res.string.pixiv_show_original),
                            color = if (state.showOriginal) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            BgmIcons.Visibility,
                            null,
                            tint = if (state.showOriginal) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    onClick = {
                        expanded = false
                        onActionEvent(GalleryEvent.Action.OnToggleShowOriginal)
                    }
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.global_share)) },
                leadingIcon = { Icon(BgmIcons.Share, null) },
                onClick = {
                    expanded = false
                    onActionEvent(GalleryEvent.Action.OnShare)
                }
            )
        }
    }
}

@Composable
private fun PixivGalleryContent(
    state: GalleryState,
    transitionImage: String,
    transitionArtworkId: String,
    onUiEvent: (GalleryEvent.UI) -> Unit,
    onActionEvent: (GalleryEvent.Action) -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
    var showSelectionDialog by remember { mutableStateOf(false) }
    var selectionAnchorIndex by remember { mutableStateOf<Int?>(null) }
    var selectedIndexes by remember { mutableStateOf(emptySet<Int>()) }

    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
    ) {
        // 图片区域：长按图片进入保存菜单
        itemsIndexed(state.images) { index, item ->
            GalleryPictureItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(item.aspect),
                item = item,
                showOriginal = state.showOriginal,
                fallbackImage = if (index == 0) transitionImage else "",
                sharedArtworkId = transitionArtworkId.takeIf { index == 0 && transitionImage.isNotBlank() },
                onClick = {
                    val previewItems = state.images.mapNotNull { image ->
                        (if (state.showOriginal && image.original.isNotBlank()) image.original else image.image)
                            .takeIf(String::isNotBlank)
                    }
                    if (previewItems.isNotEmpty()) {
                        val previewIndex = previewItems.indexOf(
                            if (state.showOriginal && item.original.isNotBlank()) item.original else item.image
                        ).coerceAtLeast(0)
                        onUiEvent(
                            GalleryEvent.UI.OnNavScreen(
                                Screen.PreviewMain(previewIndex, previewItems)
                            )
                        )
                    }
                },
                onLongClick = {
                    selectionAnchorIndex = index
                    selectedIndexes = state.images.indices.toSet()
                    showSelectionDialog = true
                },
            )
        }

        // 作品详情区域
        state.illust?.let { illust ->
            item(key = "illust_detail") {
                IllustDetailSection(
                    illust = illust,
                    state = state,
                    onActionEvent = onActionEvent,
                    clipboardManager = clipboardManager,
                )
            }
        } ?: item {
            Spacer(modifier = Modifier.height(48.dp))
        }

        // 没有评论时不占用作品页空间；发表评论后 ViewModel 会立即插入新评论并显示
        if (state.comments.isNotEmpty()) {
            item(key = "comments") {
                CommentsSection(
                    state = state,
                    onActionEvent = onActionEvent,
                )
            }
        }

        if (state.relatedIllusts.isNotEmpty() || state.relatedLoading) {
            item(key = "related_illusts") {
                RelatedIllustsSection(
                    relatedIllusts = state.relatedIllusts,
                    onNavScreen = { screen ->
                        onUiEvent(GalleryEvent.UI.OnNavScreen(screen))
                    },
                )
            }

            if (state.relatedHasMore || state.relatedLoading) {
                item(key = "related_illusts_footer") {
                    RelatedIllustsFooter(
                        relatedLoading = state.relatedLoading,
                        hasMore = state.relatedHasMore,
                        onLoadMore = { onActionEvent(GalleryEvent.Action.OnLoadMoreRelated) },
                    )
                }
            }
        }
    }

    if (showSelectionDialog) {
        AlertDialog(
            onDismissRequest = { showSelectionDialog = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "保存图片",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = "已选择 ${selectedIndexes.size}/${state.images.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.widthIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        selectionAnchorIndex?.let { anchor ->
                            TextButton(
                                onClick = { selectedIndexes = setOf(anchor) },
                                contentPadding = PaddingValues(horizontal = 8.dp),
                            ) { Text("仅保存当前") }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = { selectedIndexes = state.images.indices.toSet() },
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) { Text("全选") }
                        TextButton(
                            onClick = { selectedIndexes = emptySet() },
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) { Text("全不选") }
                    }

                    // 使用缩略图网格替代文字列表；每张图的勾选按钮固定在右上角。
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.images.chunked(2)) { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                rowItems.forEachIndexed { rowIndex, image ->
                                    val index = state.images.indexOf(image)
                                    val checked = index in selectedIndexes
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(0.78f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .border(
                                                width = if (checked) 2.dp else 1.dp,
                                                color = if (checked) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(16.dp),
                                            )
                                            .clickable {
                                                selectedIndexes = if (checked) {
                                                    selectedIndexes - index
                                                } else {
                                                    selectedIndexes + index
                                                }
                                            },
                                    ) {
                                        StateImage(
                                            modifier = Modifier.fillMaxSize(),
                                            model = image.image.ifBlank { image.original },
                                            contentDescription = "第 ${index + 1} 张图片",
                                            contentScale = ContentScale.Crop,
                                            blurLoading = false,
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            androidx.compose.material3.Checkbox(
                                                modifier = Modifier.size(30.dp),
                                                checked = checked,
                                                onCheckedChange = { value ->
                                                    selectedIndexes = if (value) {
                                                        selectedIndexes + index
                                                    } else {
                                                        selectedIndexes - index
                                                    }
                                                },
                                            )
                                        }
                                        Text(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(8.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Black.copy(alpha = 0.58f))
                                                .padding(horizontal = 7.dp, vertical = 3.dp),
                                            text = "${index + 1}",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    }
                                }
                                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = selectedIndexes.isNotEmpty(),
                    onClick = {
                        showSelectionDialog = false
                        onActionEvent(GalleryEvent.Action.OnDownload(selectedIndexes.sorted()))
                    },
                ) {
                    Text("保存${selectedIndexes.size.takeIf { it > 0 }?.let { " ($it)" }.orEmpty()}")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSelectionDialog = false }) { Text("取消") }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IllustDetailSection(
    illust: ComposePixivIllust,
    state: GalleryState,
    onActionEvent: (GalleryEvent.Action) -> Unit,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 作者卡片
        illust.user?.let { user ->
            AuthorCard(
                user = user,
                isFollowed = state.isFollowed,
                onToggleFollow = { onActionEvent(GalleryEvent.Action.OnToggleFollow) },
            )
        }

        // 作品信息卡片（浏览量/收藏量/日期紧凑一行）
        IllustInfoCard(
            illust = illust,
            clipboardManager = clipboardManager,
        )

        // 标签（圆角胶囊 + 长按菜单）
        if (illust.tags.isNotEmpty()) {
            TagsCard(
                tags = illust.tags,
                bannedTags = state.bannedTags,
                bookmarkedTags = state.bookmarkedTags,
                onTagClick = { tag ->
                    onActionEvent(GalleryEvent.Action.OnTagClick(tag))
                },
                onBanTag = { tag ->
                    onActionEvent(GalleryEvent.Action.OnBanTag(tag))
                },
                onBookmarkTag = { tag ->
                    onActionEvent(GalleryEvent.Action.OnBookmarkTag(tag))
                },
                onCopyTag = { tag ->
                    onActionEvent(GalleryEvent.Action.OnCopyTag(tag))
                },
            )
        }

    }
}

@Composable
private fun AuthorCard(
    user: ComposePixivUser,
    isFollowed: Boolean,
    onToggleFollow: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 头像
            AsyncImage(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                model = user.profileImageUrls?.medium,
                contentDescription = user.name,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "@${user.account.orEmpty()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                user.comment?.let { comment ->
                    if (comment.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = comment,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                        )
                    }
                }
            }
            // 关注按钮
            IconButton(onClick = onToggleFollow) {
                Icon(
                    imageVector = if (isFollowed) BgmIcons.Favorite else BgmIcons.FavoriteBorder,
                    contentDescription = if (isFollowed)
                        stringResource(Res.string.pixiv_unfollow)
                    else
                        stringResource(Res.string.pixiv_follow),
                    tint = if (isFollowed) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IllustStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun IllustInfoCard(
    illust: ComposePixivIllust,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            // 标题
            illust.title?.let { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 作品 ID
            Text(
                modifier = Modifier.clickable {
                    clipboardManager.setText(AnnotatedString(illust.id.toString()))
                },
                text = stringResource(Res.string.pixiv_illust_id, illust.id),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 点赞 / 收藏 / 浏览 / 发布时间：小字号、左对齐，避免统计信息被挤成多行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IllustStat(icon = BgmIcons.Favorite, value = illust.likeCount.toString())
                IllustStat(icon = BgmIcons.Bookmark, value = illust.totalBookmarks.toString())
                IllustStat(icon = BgmIcons.Visibility, value = illust.totalView.toString())
                illust.createDate?.takeIf { it.isNotBlank() }?.let { date ->
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = BgmIcons.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = date.formatPixivDateTime(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // 说明文字
            illust.caption?.let { caption ->
                if (caption.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 5,
                    )
                }
            }
        }
    }
}

/**
 * 标签卡片：圆角胶囊，#tag 用主题色、翻译名用默认色，
 * 单击跳搜索，长按弹出 屏蔽/收藏/复制 菜单
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun TagsCard(
    tags: List<ComposePixivTag>,
    bannedTags: List<String>,
    bookmarkedTags: List<String>,
    onTagClick: (String) -> Unit,
    onBanTag: (String) -> Unit,
    onBookmarkTag: (String) -> Unit,
    onCopyTag: (String) -> Unit,
) {
    var longPressTag by remember { mutableStateOf<ComposePixivTag?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tags.forEach { tag ->
                val name = tag.name.orEmpty()
                if (name.isNotBlank()) {
                    val isBanned = name in bannedTags
                    val isBookmarked = name in bookmarkedTags
                    TagPill(
                        tag = tag,
                        isBanned = isBanned,
                        isBookmarked = isBookmarked,
                        onClick = { onTagClick(name) },
                        onLongClick = { longPressTag = tag },
                    )
                }
            }
        }
    }

    // 长按菜单
    longPressTag?.let { tag ->
        val name = tag.name.orEmpty()
        AlertDialog(
            onDismissRequest = { longPressTag = null },
            title = { Text("#$name") },
            text = {
                Column {
                    TagActionRow(
                        icon = BgmIcons.Block,
                        title = stringResource(Res.string.pixiv_tag_ban),
                    ) {
                        longPressTag = null
                        onBanTag(name)
                    }
                    TagActionRow(
                        icon = BgmIcons.Star,
                        title = stringResource(Res.string.pixiv_tag_bookmark),
                    ) {
                        longPressTag = null
                        onBookmarkTag(name)
                    }
                    TagActionRow(
                        icon = BgmIcons.ContentCopy,
                        title = stringResource(Res.string.pixiv_tag_copy),
                    ) {
                        longPressTag = null
                        onCopyTag(name)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { longPressTag = null }) {
                    Text(stringResource(Res.string.pixiv_cancel))
                }
            },
        )
    }
}

/**
 * 单个标签圆角胶囊：#名 用主题色，翻译名用默认色
 */
@Composable
private fun TagPill(
    tag: ComposePixivTag,
    isBanned: Boolean,
    isBookmarked: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f))
            .widthIn(max = 280.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f, fill = false),
            text = "#${tag.name.orEmpty()}",
            style = MaterialTheme.typography.labelMedium,
            color = if (isBanned) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        tag.translatedName?.let { translated ->
            if (translated.isNotBlank() && translated != tag.name) {
                Text(
                    modifier = Modifier.weight(1f, fill = false),
                    text = " $translated",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (isBookmarked) {
            Icon(
                imageVector = BgmIcons.Star,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun TagActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
    }
}

// ---------------- 评论区 ----------------

@Composable
private fun CommentsSection(
    state: GalleryState,
    onActionEvent: (GalleryEvent.Action) -> Unit,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 标题
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.pixiv_comments),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            if (state.comments.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(${state.comments.size})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        when {
            state.commentsLoading && state.comments.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }
            state.comments.isEmpty() -> {
                Text(
                    text = stringResource(Res.string.pixiv_comment_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                )
            }
            else -> {
                state.comments.forEach { comment ->
                    CommentItem(
                        comment = comment,
                        isReply = false,
                        expanded = comment.id in state.expandedReplyIds,
                        onToggleReplies = { onActionEvent(GalleryEvent.Action.OnToggleReplies(comment.id)) },
                        onReply = { target ->
                            onActionEvent(GalleryEvent.Action.OnReplyTarget(target))
                        },
                    )
                }
            }
        }
    }
}

private val pixivEmojiFiles = mapOf(
    "(normal)" to "101.png",
    "(surprise)" to "102.png",
    "(serious)" to "103.png",
    "(heaven)" to "104.png",
    "(happy)" to "105.png",
    "(excited)" to "106.png",
    "(sing)" to "107.png",
    "(cry)" to "108.png",
    "(normal2)" to "201.png",
    "(shame2)" to "202.png",
    "(love2)" to "203.png",
    "(interesting2)" to "204.png",
    "(blush2)" to "205.png",
    "(fire2)" to "206.png",
    "(angry2)" to "207.png",
    "(shine2)" to "208.png",
    "(panic2)" to "209.png",
    "(normal3)" to "301.png",
    "(satisfaction3)" to "302.png",
    "(surprise3)" to "303.png",
    "(smile3)" to "304.png",
    "(shock3)" to "305.png",
    "(gaze3)" to "306.png",
    "(wink3)" to "307.png",
    "(happy3)" to "308.png",
    "(excited3)" to "309.png",
    "(love3)" to "310.png",
    "(normal4)" to "401.png",
    "(surprise4)" to "402.png",
    "(serious4)" to "403.png",
    "(love4)" to "404.png",
    "(shine4)" to "405.png",
    "(sweat4)" to "406.png",
    "(shame4)" to "407.png",
    "(sleep4)" to "408.png",
    "(heart)" to "501.png",
    "(teardrop)" to "502.png",
    "(star)" to "503.png",
)

private data class PixivCommentPart(val text: String? = null, val emoji: String? = null)

private fun parsePixivComment(text: String): List<PixivCommentPart> {
    val result = mutableListOf<PixivCommentPart>()
    var cursor = 0
    val pattern = "\\([^()]+\\)".toRegex()
    pattern.findAll(text).forEach { match ->
        if (match.range.first > cursor) {
            result += PixivCommentPart(text = text.substring(cursor, match.range.first))
        }
        val token = match.value
        if (token in pixivEmojiFiles) result += PixivCommentPart(emoji = token)
        else result += PixivCommentPart(text = token)
        cursor = match.range.last + 1
    }
    if (cursor < text.length) result += PixivCommentPart(text = text.substring(cursor))
    return result.ifEmpty { listOf(PixivCommentPart(text = text)) }
}

@Composable
private fun CommentEmojiText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
) {
    val parts = remember(text) { parsePixivComment(text) }
    val inlineContent = parts.mapIndexedNotNull { index, part ->
        val file = part.emoji?.let { pixivEmojiFiles[it] } ?: return@mapIndexedNotNull null
        "pixiv-emoji-$index" to InlineTextContent(
            placeholder = Placeholder(20.sp, 20.sp, PlaceholderVerticalAlign.Center),
            children = {
                StateImage(
                    modifier = Modifier.fillMaxSize(),
                    model = "https://s.pximg.net/common/images/emoji/$file",
                    contentScale = ContentScale.Fit,
                    blurLoading = false,
                )
            },
        )
    }.toMap()
    val annotatedText = buildAnnotatedString {
        parts.forEachIndexed { index, part ->
            if (part.emoji != null) {
                appendInlineContent("pixiv-emoji-$index", part.emoji)
            } else {
                append(part.text.orEmpty())
            }
        }
    }
    Text(
        text = annotatedText,
        inlineContent = inlineContent,
        style = style,
    )
}

@Composable
private fun CommentItem(
    comment: ComposePixivComment,
    isReply: Boolean,
    expanded: Boolean,
    onToggleReplies: () -> Unit,
    onReply: (ComposePixivComment) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        // 头像
        AsyncImage(
            modifier = Modifier
                .size(if (isReply) 28.dp else 36.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            model = comment.user?.profileImageUrls?.medium,
            contentDescription = comment.user?.name,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            // 用户名 + 日期
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = comment.user?.name.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                )
                comment.date?.let { date ->
                    Text(
                        text = date.formatPixivDateTime(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // 评论内容
            comment.comment?.let { content ->
                if (content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    CommentEmojiText(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            comment.stamp?.url?.takeIf { it.isNotBlank() }?.let { stampUrl ->
                Spacer(modifier = Modifier.height(4.dp))
                StateImage(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    model = stampUrl,
                    contentScale = ContentScale.Fit,
                    blurLoading = false,
                )
            }

            // 操作：回复 / 查看回复
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TextButton(onClick = { onReply(comment) }) {
                    Text(
                        text = stringResource(Res.string.reply_comment),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (!isReply && comment.hasReplies) {
                    TextButton(onClick = onToggleReplies) {
                        Text(
                            text = stringResource(Res.string.pixiv_comment_view_replies),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            // 内联回复列表
            if (expanded && comment.replies.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    comment.replies.forEach { reply ->
                        CommentItem(
                            comment = reply,
                            isReply = true,
                            expanded = false,
                            onToggleReplies = {},
                            onReply = onReply,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 底部评论输入栏：回复模式显示"回复 @xxx"+ 取消
 */
@Composable
private fun CommentInputBar(
    state: GalleryState,
    onActionEvent: (GalleryEvent.Action) -> Unit,
) {
    val replyTarget = state.replyTarget
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding(),
        ) {
            if (replyTarget != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(
                            Res.string.pixiv_comment_reply_to,
                            replyTarget.user?.name.orEmpty()
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = { onActionEvent(GalleryEvent.Action.OnReplyTarget(null)) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = BgmIcons.Close,
                            contentDescription = stringResource(Res.string.pixiv_cancel),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                TextField(
                    value = state.commentInput,
                    onValueChange = { onActionEvent(GalleryEvent.Action.OnCommentInputChange(it)) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(Res.string.reply_comment_hint)) },
                    maxLines = 4,
                    textStyle = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { onActionEvent(GalleryEvent.Action.OnSendComment) },
                    enabled = state.commentInput.isNotBlank(),
                ) {
                    Icon(
                        imageVector = BgmIcons.Send,
                        contentDescription = stringResource(Res.string.reply_comment_send),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

// ---------------- 相关图片 ----------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RelatedIllustsSection(
    relatedIllusts: List<ComposePixivIllust>,
    onNavScreen: (Screen) -> Unit,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.pixiv_related_illusts),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            if (relatedIllusts.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(${relatedIllusts.size})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            relatedIllusts.forEach { illust ->
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onNavScreen(Screen.Gallery(illust.id.toString(), ListAlbumType.PIVIX))
                        },
                ) {
                    StateImage(
                        modifier = Modifier.fillMaxSize(),
                        model = illust.imageUrls?.squareMedium,
                        contentDescription = illust.title,
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }
}

@Composable
private fun RelatedIllustsFooter(
    relatedLoading: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
) {
    // 该 item 只有接近列表末尾才会进入组合；新页插入后它被推离视口，
    // 后续再次滑到底会重新组合并请求下一页。
    LaunchedEffect(Unit) {
        if (hasMore && !relatedLoading) onLoadMore()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (relatedLoading) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun GalleryImageGrid(
    state: GalleryState,
    onUiEvent: (GalleryEvent.UI) -> Unit,
    onActionEvent: (GalleryEvent.Action) -> Unit,
) {
    LazyVerticalStaggeredGrid(
        modifier = Modifier.fillMaxSize(),
        columns = StaggeredGridCells.Adaptive(350.dp)
    ) {
        itemsIndexed(state.images) { index, item ->
            GalleryPictureItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(item.aspect),
                item = item,
                showOriginal = state.showOriginal,
                onClick = {
                    val previewItems = state.images.mapNotNull { image ->
                        (if (state.showOriginal && image.original.isNotBlank()) image.original else image.image)
                            .takeIf(String::isNotBlank)
                    }
                    if (previewItems.isNotEmpty()) {
                        val previewIndex = previewItems.indexOf(
                            if (state.showOriginal && item.original.isNotBlank()) item.original else item.image
                        ).coerceAtLeast(0)
                        onUiEvent(
                            GalleryEvent.UI.OnNavScreen(
                                Screen.PreviewMain(previewIndex, previewItems)
                            )
                        )
                    }
                }
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GalleryPictureItem(
    modifier: Modifier,
    item: ComposeGallery,
    showOriginal: Boolean = false,
    fallbackImage: String = "",
    sharedArtworkId: String? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val image = if (showOriginal && item.original.isNotBlank()) item.original else item.image
    val baseImageModifier = Modifier
        .fillMaxSize()
        .background(item.uiColor)
    val imageModifier = if (sharedArtworkId == null) {
        baseImageModifier
    } else {
        baseImageModifier.pixivArtworkSharedElement(sharedArtworkId)
    }

    Box(
        modifier = Modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .then(modifier)
    ) {
        // 首图在高分图尚未解码时保留列表缩略图，避免共享转场结束后闪回加载态。
        StateImage(
            modifier = imageModifier,
            model = fallbackImage.ifBlank { image },
            blurLoading = fallbackImage.isBlank(),
        )
        if (fallbackImage.isNotBlank() && fallbackImage != image) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        }

        Text(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(BrushVerticalTransparentToHalfBlack)
                .padding(LayoutPaddingHalf),
            style = MaterialTheme.typography.bodySmall.copy(
                shadow = Shadow(
                    color = if (item.uiColor != Color.Unspecified) item.uiColor else Color.White,
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                )
            ),
            color = Color.White,
            text = buildString {
                append(stringResource(Res.string.global_resolution, item.width, item.height))
                if (item.size > 0) {
                    appendLine()
                    append(stringResource(Res.string.global_file_size, item.size.formatFileSize()))
                }
            }
        )
    }
}
