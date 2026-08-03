package com.xiaoyv.bangumi.features.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Visibility
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
import com.xiaoyv.bangumi.shared.data.model.response.image.ComposeGallery
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivComment
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivIllust
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivTag
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivUser
import com.xiaoyv.bangumi.shared.ui.component.bar.BgmTopAppBar
import com.xiaoyv.bangumi.shared.ui.component.image.StateImage
import com.xiaoyv.bangumi.shared.ui.component.layout.state.StateLayout
import com.xiaoyv.bangumi.shared.ui.component.navigation.Screen
import com.xiaoyv.bangumi.shared.ui.component.space.BrushVerticalTransparentToHalfBlack
import com.xiaoyv.bangumi.shared.ui.component.space.LayoutPaddingHalf
import com.xiaoyv.bangumi.shared.ui.kts.collectBaseSideEffect
import com.xiaoyv.bangumi.shared.ui.theme.BgmIcons
import org.jetbrains.compose.resources.stringResource
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
    val state = baseState.payload
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
    ) {
        StateLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            onRefresh = { onActionEvent(GalleryEvent.Action.OnRefresh(it)) },
            baseState = baseState,
        ) { contentState ->
            if (contentState.isPixiv) {
                PixivGalleryContent(contentState, onUiEvent, onActionEvent)
            } else {
                GalleryImageGrid(contentState, onUiEvent, onActionEvent)
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
                )
            }
        } ?: item {
            Spacer(modifier = Modifier.height(48.dp))
        }

        // 评论区
        if (state.isPixiv) {
            item(key = "comments") {
                CommentsSection(
                    state = state,
                    onActionEvent = onActionEvent,
                )
            }
        }

        // 相关图片
        if (state.relatedIllusts.isNotEmpty()) {
            item(key = "related_illusts") {
                RelatedIllustsSection(
                    relatedIllusts = state.relatedIllusts,
                    relatedLoading = state.relatedLoading,
                    onNavScreen = { screen ->
                        onUiEvent(GalleryEvent.UI.OnNavScreen(screen))
                    },
                )
            }
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

        // 下载原图
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            TextButton(
                modifier = Modifier.fillMaxWidth(),
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

            // 统计数据：浏览量 / 收藏量 / 日期紧凑一行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 浏览量
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = BgmIcons.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${illust.totalView}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                // 收藏量
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = BgmIcons.Bookmark,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${illust.totalBookmarks}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
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
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "#${tag.name.orEmpty()}",
            style = MaterialTheme.typography.labelMedium,
            color = if (isBanned) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary,
        )
        tag.translatedName?.let { translated ->
            if (translated.isNotBlank() && translated != tag.name) {
                Text(
                    text = " $translated",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                )
                comment.date?.let { date ->
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // 评论内容
            comment.comment?.let { content ->
                if (content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
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
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                if (!isReply && comment.hasReplies) {
                    TextButton(onClick = onToggleReplies) {
                        Text(
                            text = stringResource(Res.string.pixiv_comment_view_replies),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
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
    relatedLoading: Boolean,
    onNavScreen: (Screen) -> Unit,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 标题
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

        // 3 列网格（chunked 布局，避免嵌套同向滚动）
        relatedIllusts.chunked(3).forEach { rowIllusts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                rowIllusts.forEach { illust ->
                    val squareMedium = illust.imageUrls?.squareMedium
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onNavScreen(
                                    Screen.Gallery(illust.id.toString(), ListAlbumType.PIVIX)
                                )
                            },
                    ) {
                        AsyncImage(
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            model = squareMedium,
                            contentDescription = illust.title,
                        )
                    }
                }
                // 补足一行中的空位
                repeat(3 - rowIllusts.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // 加载中
        if (relatedLoading && relatedIllusts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp))
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
