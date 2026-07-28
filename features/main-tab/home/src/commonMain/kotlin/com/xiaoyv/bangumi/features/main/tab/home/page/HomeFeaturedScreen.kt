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
import com.xiaoyv.bangumi.shared.data.model.response.bgm.index.ComposeCatalogItem
import com.xiaoyv.bangumi.shared.data.repository.IndexRepository
import com.xiaoyv.bangumi.shared.ui.component.navigation.Screen
import com.xiaoyv.bangumi.shared.ui.view.index.IndexPageItem
import org.koin.compose.koinInject

/**
 * 精选目录页面
 *
 * 使用离线 protobuf 数据，支持客户端筛选（类型/时间/关键词）
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
        allCatalogs.filter { item ->
            // 类型筛选
            val typeMatch = filterType.isBlank() || item.primaryType == filterType

            // 时间筛选
            val yearMatch = when {
                filterYear.isBlank() -> true
                filterYear == "1y" -> {
                    val lastYear = java.time.LocalDate.now().minusYears(1)
                    parseDate(item.lastUpdate) >= lastYear
                }
                filterYear == "3y" -> {
                    val threeYearsAgo = java.time.LocalDate.now().minusYears(3)
                    parseDate(item.lastUpdate) >= threeYearsAgo
                }
                else -> item.lastUpdate.contains(filterYear)
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

/** 解析日期字符串为 LocalDate */
private fun parseDate(dateStr: String): java.time.LocalDate {
    return try {
        // 格式: "2026-4-7" 或 "2026-04-07"
        val parts = dateStr.split("-")
        if (parts.size == 3) {
            java.time.LocalDate.of(
                parts[0].toInt(),
                parts[1].toInt(),
                parts[2].toInt()
            )
        } else {
            java.time.LocalDate.MIN
        }
    } catch (e: Exception) {
        java.time.LocalDate.MIN
    }
}
