package com.xiaoyv.bangumi.features.preivew.main.business

import androidx.lifecycle.SavedStateHandle
import com.xiaoyv.bangumi.shared.core.mvi.BaseViewModel
import com.xiaoyv.bangumi.shared.ui.component.navigation.Screen
import kotlinx.collections.immutable.toPersistentList

/**
 * [PreviewMainViewModel]
 *
 * @author why
 * @since 2025/1/12
 */
class PreviewMainViewModel(
    savedStateHandle: SavedStateHandle,
    private val args: Screen.PreviewMain,
) : BaseViewModel<PreviewMainState, PreviewMainSideEffect, PreviewMainEvent.Action>(savedStateHandle) {

    override fun initSate(onCreate: Boolean): PreviewMainState {
        // 图片列表可能来自网络响应，过滤空 URL，避免预览组件收到无效模型。
        val items = args.items.filter(String::isNotBlank)
        val selectedIndex = args.items
            .take(args.index.coerceAtLeast(0))
            .count(String::isNotBlank)
            .coerceIn(0, (items.size - 1).coerceAtLeast(0))
        return PreviewMainState(
            items = items.toPersistentList(),
            index = selectedIndex,
        )
    }

    override fun onEvent(event: PreviewMainEvent.Action) {
        when (event) {
            is PreviewMainEvent.Action.OnRefresh -> refresh(loading = event.loading)
            is PreviewMainEvent.Action.OnPageSelected -> onPageSelected(event.index)
        }
    }

    private fun onPageSelected(index: Int) = action {
        reduceContent { state.copy(index = index) }
    }
}
