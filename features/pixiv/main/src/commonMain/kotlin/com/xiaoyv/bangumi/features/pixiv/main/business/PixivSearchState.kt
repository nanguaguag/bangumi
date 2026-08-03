package com.xiaoyv.bangumi.features.pixiv.main.business

import androidx.compose.runtime.Immutable

/**
 * [PixivSearchState]
 *
 * @author why
 * @since 2025/1/12
 */
@Immutable
data class PixivSearchState(
    val query: String = "",
)
