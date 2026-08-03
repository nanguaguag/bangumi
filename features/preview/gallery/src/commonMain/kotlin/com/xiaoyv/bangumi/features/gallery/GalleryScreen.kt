package com.xiaoyv.bangumi.features.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.xiaoyv.bangumi.core_resource.resources.pixiv_download
import com.xiaoyv.bangumi.core_resource.resources.pixiv_follow
import com.xiaoyv.bangumi.core_resource.resources.pixiv_illust_id
import com.xiaoyv.bangumi.core_resource.resources.pixiv_related
import com.xiaoyv.bangumi.core_resource.resources.pixiv_show_original
import com.xiaoyv.bangumi.core_resource.resources.pixiv_unfollow
import com.xiaoyv.bangumi.core_resource.resources.pixiv_watch_later
import com.xiaoyv.bangumi.core_resource.resources.pixiv_watch_later_remove
import com.xiaoyv.bangumi.features.gallery.business.GalleryEvent
import com.xiaoyv.bangumi.features.gallery.business.GallerySideEffect
import com.xiaoyv.bangumi.features.gallery.business.GalleryState
import com.xiaoyv.bangumi.features.gallery.business.GalleryViewModel
import com.xiaoyv.bangumi.shared.core.mvi.BaseState
import com.xiaoyv.bangumi.shared.core.utils.formatFileSize
import com.xiaoyv.bangumi.shared.data.model.response.image.ComposeGallery
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivIllust
import com.xiaoyv.bangumi.shared.ui.component.bar.BgmTopAppBar
import com.xiaoyv.bangumi.shared.ui.component.image.StateImage
import com.xiaoyv.bangumi.shared.ui.component.layout.state.StateLayout
import com.xiaoyv.bangumi.shared.ui.component.navigation.Screen
import com.xiaoyv.bangumi.shared.ui.component.space.BrushVerticalTransparentToHalfBlack
import com.xiaoyv.bangumi.shared.ui.component.space.LayoutPaddingHalf
import com.xiaoyv.bangumi.shared.ui.kts.collectBaseSideEffect
import com.xiaoyv.bangumi.shared.ui.theme.BgmIcons
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun GalleryRoute(
    viewModel: GalleryViewModel,
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
            is GallerySideEffect.OpenDownload -> {
                uriHandler.openUri(it.url)
            }
            is GallerySideEffect.NavigateToTagSearch -> {
                onNavScreen(Screen.PixivSearch(it.tag))
            }
        }
    }

    GalleryScreen(
        baseState = baseState,
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
    onUiEvent: (GalleryEvent.UI) -> Unit,
    onActionEvent: (GalleryEvent.Action) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // 使用 BgmTopAppBar 并添加菜单按钮
            BgmTopAppBar(
                title = baseState.payload.let {
                    if (it == null) stringResource(Res.string.global_artwork)
                    else stringResource(Res.string.global_artwork) + "：${it.id}"
                },
                onNavigationClick = { onUiEvent(GalleryEvent.UI.OnNavUp) },
                actions = {
                    // 菜单按钮
                    GalleryTopBarMenu(state = baseState.payload ?: GalleryState(), onActionEvent)
                }
            )
        }
    ) {
        StateLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            onRefresh = { onActionEvent(GalleryEvent.Action.OnRefresh(it)) },
            baseState = baseState,
        ) { state ->
            if (state.isPixiv) {
                PixivGalleryContent(state, onUiEvent, onActionEvent)
            } else {
                GalleryImageGrid(state, onUiEvent, onActionEvent)
            }
        }
    }
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
    onUiEvent: (GalleryEvent.UI) -> Unit,
    onActionEvent: (GalleryEvent.Action) -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        // 图片区域
        itemsIndexed(state.images) { index, item ->
            GalleryPictureItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(item.aspect),
                item = item,
                showOriginal = state.showOriginal,
                onClick = {
                    onUiEvent(
                        GalleryEvent.UI.OnNavScreen(
                            Screen.PreviewMain(
                                index,
                                state.images.map {
                                    if (state.showOriginal && it.original.isNotBlank()) it.original else it.image
                                }
                            )
                        )
                    )
                }
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
                    uriHandler = uriHandler,
                )
            }
        } ?: item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IllustDetailSection(
    illust: ComposePixivIllust,
    state: GalleryState,
    onActionEvent: (GalleryEvent.Action) -> Unit,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    uriHandler: androidx.compose.ui.platform.UriHandler,
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

        // 作品信息卡片
        IllustInfoCard(
            illust = illust,
            isBookmarked = state.isBookmarked,
            onToggleBookmark = { onActionEvent(GalleryEvent.Action.OnToggleBookmark) },
            clipboardManager = clipboardManager,
        )

        // 标签
        if (illust.tags.isNotEmpty()) {
            TagsCard(
                tags = illust.tags,
                onTagClick = { tag ->
                    onActionEvent(GalleryEvent.Action.OnTagClick(tag))
                }
            )
        }

        // 操作按钮行
        ActionButtonsRow(onActionEvent)
    }
}

@Composable
private fun AuthorCard(
    user: com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivUser,
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
private fun IllustInfoCard(
    illust: ComposePixivIllust,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
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

            // 统计数据行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 浏览量
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = BgmIcons.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${illust.totalView}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                // 收藏量 + 收藏按钮
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = BgmIcons.Bookmark,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${illust.totalBookmarks}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) BgmIcons.Bookmark else BgmIcons.BookmarkBorder,
                            contentDescription = if (isBookmarked)
                                stringResource(Res.string.pixiv_bookmarked)
                            else
                                stringResource(Res.string.pixiv_bookmark),
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                // 发布日期
                illust.createDate?.let { date ->
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsCard(
    tags: List<com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivTag>,
    onTagClick: (String) -> Unit,
) {
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
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            tags.forEach { tag ->
                tag.name?.let { name ->
                    AssistChip(
                        onClick = { onTagClick(name) },
                        label = {
                            Column {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                                tag.translatedName?.let { translated ->
                                    if (translated.isNotBlank() && translated != name) {
                                        Text(
                                            text = translated,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsRow(
    onActionEvent: (GalleryEvent.Action) -> Unit,
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
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            // 下载原图
            androidx.compose.material3.TextButton(
                onClick = { onActionEvent(GalleryEvent.Action.OnDownload) }
            ) {
                Icon(
                    imageVector = BgmIcons.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(Res.string.pixiv_download))
            }
            // 相似插画
            androidx.compose.material3.TextButton(
                onClick = { onActionEvent(GalleryEvent.Action.OnTagClick("similar")) }
            ) {
                Text(stringResource(Res.string.pixiv_related))
            }
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
                    onUiEvent(
                        GalleryEvent.UI.OnNavScreen(
                            Screen.PreviewMain(
                                index,
                                state.images.map {
                                    if (state.showOriginal && it.original.isNotBlank()) it.original else it.image
                                }
                            )
                        )
                    )
                }
            )
        }
    }
}


@Composable
private fun GalleryPictureItem(
    modifier: Modifier,
    item: ComposeGallery,
    showOriginal: Boolean = false,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.clickable(onClick = onClick).then(modifier)) {
        StateImage(
            modifier = Modifier
                .matchParentSize()
                .background(item.uiColor),
            model = if (showOriginal && item.original.isNotBlank()) item.original else item.image
        )

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
