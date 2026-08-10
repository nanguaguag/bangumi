package com.xiaoyv.bangumi.features.pixiv.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import com.xiaoyv.bangumi.core_resource.resources.Res
import com.xiaoyv.bangumi.core_resource.resources.global_file_size
import com.xiaoyv.bangumi.core_resource.resources.global_resolution
import com.xiaoyv.bangumi.core_resource.resources.pixiv_search_title
import com.xiaoyv.bangumi.features.pixiv.main.business.PixivSearchEvent
import com.xiaoyv.bangumi.features.pixiv.main.business.PixivSearchState
import com.xiaoyv.bangumi.features.pixiv.main.business.PixivSearchViewModel
import com.xiaoyv.bangumi.shared.core.mvi.BaseState
import com.xiaoyv.bangumi.shared.core.utils.formatFileSize
import com.xiaoyv.bangumi.shared.core.utils.formatShort
import com.xiaoyv.bangumi.shared.data.model.response.image.ComposeGallery
import com.xiaoyv.bangumi.shared.ui.component.bar.BgmTopAppBar
import com.xiaoyv.bangumi.shared.ui.component.image.StateImage
import com.xiaoyv.bangumi.shared.ui.component.layout.state.StateLazyVerticalStaggeredGrid
import com.xiaoyv.bangumi.shared.ui.component.layout.state.StateLayout
import com.xiaoyv.bangumi.shared.ui.component.navigation.Screen
import com.xiaoyv.bangumi.shared.ui.component.navigation.pixivArtworkSharedElement
import com.xiaoyv.bangumi.shared.ui.component.paging.LazyPagingItems
import com.xiaoyv.bangumi.shared.ui.component.paging.collectAsLazyPagingItems
import com.xiaoyv.bangumi.shared.ui.component.space.BrushVerticalTransparentToHalfBlack
import com.xiaoyv.bangumi.shared.ui.component.space.LayoutPaddingHalf
import com.xiaoyv.bangumi.shared.ui.kts.collectBaseSideEffect
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

/**
 * [PixivSearchScreen]
 *
 * @author why
 * @since 2025/1/12
 */
@Composable
fun PixivSearchRoute(
    viewModel: PixivSearchViewModel,
    onNavUp: () -> Unit,
    onNavScreen: (Screen) -> Unit,
) {
    val baseState by viewModel.collectAsState()
    val imageItems = viewModel.images.collectAsLazyPagingItems()

    viewModel.collectBaseSideEffect {

    }

    PixivSearchScreen(
        baseState = baseState,
        imageItems = imageItems,
        onActionEvent = viewModel::onEvent,
        onUiEvent = {
            when (it) {
                is PixivSearchEvent.UI.OnNavUp -> onNavUp()
                is PixivSearchEvent.UI.OnNavScreen -> onNavScreen(it.screen)
            }
        },
    )
}

@Composable
private fun PixivSearchScreen(
    baseState: BaseState<PixivSearchState>,
    imageItems: LazyPagingItems<ComposeGallery>,
    onUiEvent: (PixivSearchEvent.UI) -> Unit,
    onActionEvent: (PixivSearchEvent.Action) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            BgmTopAppBar(
                title = stringResource(
                    Res.string.pixiv_search_title,
                    baseState.payload?.query.orEmpty()
                ),
                onNavigationClick = { onUiEvent(PixivSearchEvent.UI.OnNavUp) }
            )
        }
    ) {
        StateLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            onRefresh = { onActionEvent(PixivSearchEvent.Action.OnRefresh(it)) },
            baseState = baseState,
        ) { _ ->
            StateLazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(180.dp),
                pagingItems = imageItems,
                key = { item, _ -> item.id }
            ) { item, _ ->
                PixivSearchPictureItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(item.aspect),
                    item = item,
                    onClick = {
                        onUiEvent(
                            PixivSearchEvent.UI.OnNavScreen(
                                Screen.Gallery(
                                    id = item.id,
                                    type = item.type,
                                    transitionImage = item.image,
                                    transitionAspect = item.aspect,
                                ),
                            ),
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PixivSearchPictureItem(
    modifier: Modifier,
    item: ComposeGallery,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.clickable(onClick = onClick).then(modifier)) {
        StateImage(
            modifier = Modifier
                .matchParentSize()
                .background(item.uiColor)
                .pixivArtworkSharedElement(item.id),
            model = item.image
        )

        if (item.count > 1) Text(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(LayoutPaddingHalf)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.Black.copy(0.5f))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            color = Color.White,
            text = item.count.formatShort(1),
            style = MaterialTheme.typography.bodySmall
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
