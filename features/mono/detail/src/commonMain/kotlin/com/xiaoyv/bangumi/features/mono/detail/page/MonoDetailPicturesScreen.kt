package com.xiaoyv.bangumi.features.mono.detail.page

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.xiaoyv.bangumi.core_resource.resources.Res
import com.xiaoyv.bangumi.core_resource.resources.global_file_size
import com.xiaoyv.bangumi.core_resource.resources.global_resolution
import com.xiaoyv.bangumi.features.mono.detail.business.MonoDetailEvent
import com.xiaoyv.bangumi.features.mono.detail.business.MonoDetailState
import com.xiaoyv.bangumi.shared.core.types.list.ListAlbumType
import com.xiaoyv.bangumi.shared.core.utils.formatFileSize
import com.xiaoyv.bangumi.shared.core.utils.formatShort
import com.xiaoyv.bangumi.shared.data.model.response.image.ComposeGallery
import com.xiaoyv.bangumi.shared.ui.component.image.StateImage
import com.xiaoyv.bangumi.shared.ui.component.layout.state.StateLazyVerticalStaggeredGrid
import com.xiaoyv.bangumi.shared.ui.component.navigation.Screen
import com.xiaoyv.bangumi.shared.ui.component.navigation.pixivArtworkSharedElement
import com.xiaoyv.bangumi.shared.ui.component.paging.LazyPagingItems
import com.xiaoyv.bangumi.shared.ui.component.space.BrushVerticalTransparentToHalfBlack
import com.xiaoyv.bangumi.shared.ui.component.space.LayoutPaddingHalf
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max

/**
 * 作品瀑布流。Pixiv 项目通过 Navigation 3 的共享元素直接进入作品页，
 * 不再先播放覆盖层动画再切换页面。
 */
@Composable
fun MonoDetailPicturesScreen(
    state: MonoDetailState,
    imageItems: LazyPagingItems<ComposeGallery>,
    onUiEvent: (MonoDetailEvent.UI) -> Unit,
    onActionEvent: (MonoDetailEvent.Action) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val columnCount = max(2, (maxWidth / 180.dp).toInt())

        StateLazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(columnCount),
            pagingItems = imageItems,
            key = { item, _ -> item.id },
        ) { item, _ ->
            MonoDetailPictureItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(item.aspect),
                item = item,
                onClick = {
                    onUiEvent(
                        MonoDetailEvent.UI.OnNavScreen(
                            Screen.Gallery(
                                id = item.id,
                                type = item.type,
                                transitionImage = if (item.type == ListAlbumType.PIVIX) item.image else "",
                                transitionAspect = item.aspect,
                            ),
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun MonoDetailPictureItem(
    modifier: Modifier,
    item: ComposeGallery,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier.clickable(onClick = onClick),
    ) {
        StateImage(
            modifier = Modifier
                .fillMaxSize()
                .background(item.uiColor)
                .let {
                    if (item.type == ListAlbumType.PIVIX) it.pixivArtworkSharedElement(item.id) else it
                },
            model = item.image,
            contentScale = ContentScale.Crop,
        )

        if (item.count > 1) {
            Text(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(LayoutPaddingHalf)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.Black.copy(0.5f))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                color = Color.White,
                text = item.count.formatShort(1),
                style = MaterialTheme.typography.bodySmall,
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
                    blurRadius = 4f,
                ),
            ),
            color = Color.White,
            text = buildString {
                append(stringResource(Res.string.global_resolution, item.width, item.height))
                if (item.size > 0) {
                    appendLine()
                    append(stringResource(Res.string.global_file_size, item.size.formatFileSize()))
                }
            },
        )
    }
}
