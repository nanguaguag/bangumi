package com.xiaoyv.bangumi.features.pixiv.main.business

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.xiaoyv.bangumi.shared.core.mvi.BaseViewModel
import com.xiaoyv.bangumi.shared.data.model.response.image.ComposeGallery
import com.xiaoyv.bangumi.shared.data.repository.ImageRepository
import com.xiaoyv.bangumi.shared.ui.component.navigation.Screen
import kotlinx.coroutines.flow.Flow

/**
 * [PixivSearchViewModel]
 *
 * @author why
 * @since 2025/1/12
 */
class PixivSearchViewModel(
    savedStateHandle: SavedStateHandle,
    private val args: Screen.PixivSearch,
    imageRepository: ImageRepository,
) : BaseViewModel<PixivSearchState, PixivSearchSideEffect, PixivSearchEvent.Action>(savedStateHandle) {

    /**
     * Pixiv 标签搜索分页结果（partial_match_for_tags）
     */
    internal val images: Flow<PagingData<ComposeGallery>> = imageRepository
        .fetchPixivPictures(tag = args.query)
        .flow
        .cachedIn(viewModelScope)

    override fun initSate(onCreate: Boolean) = PixivSearchState(query = args.query)

    override fun onEvent(event: PixivSearchEvent.Action) {
        when (event) {
            is PixivSearchEvent.Action.OnRefresh -> refresh(loading = event.loading)
        }
    }
}
