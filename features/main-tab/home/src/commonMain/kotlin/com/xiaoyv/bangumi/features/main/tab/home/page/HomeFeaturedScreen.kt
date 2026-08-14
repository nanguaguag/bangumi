package com.xiaoyv.bangumi.features.main.tab.home.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiaoyv.bangumi.features.main.tab.home.business.HomeEvent
import com.xiaoyv.bangumi.shared.core.utils.formatMills
import com.xiaoyv.bangumi.shared.data.model.response.bgm.index.ComposeCatalogItem
import com.xiaoyv.bangumi.shared.data.repository.IndexRepository
import com.xiaoyv.bangumi.shared.ui.component.navigation.Screen
import com.xiaoyv.bangumi.shared.ui.view.index.IndexPageItem
import org.koin.compose.koinInject

/**
 * 精选目录页面
 *
 * 使用离线目录数据，支持客户端筛选（类型/时间/关键词）
 */
@Composable
fun HomeFeaturedScreen(
    filterType: String,
    filterYear: String,
    filterKeyword: String,
    onUiEvent: (HomeEvent.UI) -> Unit,
) {
    val indexRepository: IndexRepository = koinInject()

    // 加载离线数据
    var allCatalogs by remember { mutableStateOf<List<ComposeCatalogItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        indexRepository.fetchFeaturedCatalogs()
            .onSuccess { allCatalogs = it }
            .onFailure { allCatalogs = emptyList() }
        isLoading = false
    }

    // 客户端筛选
    val filteredCatalogs = remember(allCatalogs, filterType, filterYear, filterKeyword) {
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val oneYearAgo = now - 365L * 24 * 60 * 60 * 1000
        val threeYearsAgo = now - 3L * 365 * 24 * 60 * 60 * 1000

        allCatalogs.filter { item ->
            // 类型筛选：目录可能同时包含多种类型，不能只比较数量最多的类型
            val typeMatch = filterType.isBlank() || when (filterType) {
                "anime" -> item.anime > 0
                "book" -> item.book > 0
                "game" -> item.game > 0
                "music" -> item.music > 0
                "real" -> item.real > 0
                "character" -> item.character > 0
                "person" -> item.person > 0
                "topic" -> item.topic > 0
                "ep" -> item.episode > 0
                "blog" -> item.blog > 0
                else -> false
            }

            // 时间筛选
            val updatedAt = item.lastUpdate.formatMills()
            val yearMatch = when {
                filterYear.isBlank() -> true
                filterYear == "1y" -> updatedAt >= oneYearAgo
                filterYear == "3y" -> updatedAt >= threeYearsAgo
                else -> item.lastUpdate.startsWith("$filterYear-")
            }

            // 关键词筛选
            val keywordMatch = filterKeyword.isBlank() || item.title.contains(filterKeyword, ignoreCase = true)

            typeMatch && yearMatch && keywordMatch
        }
    }

    // 显示列表
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = filteredCatalogs,
            key = { it.id },
        ) { item ->
            IndexPageItem(
                modifier = Modifier.fillMaxWidth(),
                item = item.toComposeIndex(),
                onClick = { onUiEvent(HomeEvent.UI.OnNavScreen(Screen.IndexDetail(item.id.toLong()))) },
            )
        }
    }
}
