package com.xiaoyv.bangumi.features.main.tab.home.page

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.xiaoyv.bangumi.core_resource.resources.Res
import com.xiaoyv.bangumi.core_resource.resources.index_filter_keyword
import com.xiaoyv.bangumi.core_resource.resources.index_filter_type
import com.xiaoyv.bangumi.core_resource.resources.index_filter_year
import com.xiaoyv.bangumi.features.index.page.page.IndexPageRoute
import com.xiaoyv.bangumi.features.main.tab.home.business.HomeEvent
import com.xiaoyv.bangumi.features.main.tab.home.business.HomeState
import com.xiaoyv.bangumi.shared.core.types.IndexHomepageType
import com.xiaoyv.bangumi.shared.ui.component.chip.DropMenuChip
import com.xiaoyv.bangumi.shared.ui.component.navigation.Screen
import com.xiaoyv.bangumi.shared.ui.component.pager.BgmChipHorizontalPager
import com.xiaoyv.bangumi.shared.ui.component.tab.ComposeTextTab
import com.xiaoyv.bangumi.shared.ui.composition.TabTokens.mainHomeIndexFilters
import com.xiaoyv.bangumi.shared.ui.view.index.IndexFocusCard
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.stringResource

// ---- 目录筛选选项 ----

private val indexFilterTypeOptions = persistentListOf(
    ComposeTextTab(type = "", labelText = "不限"),
    ComposeTextTab(type = "anime", labelText = "动画"),
    ComposeTextTab(type = "book", labelText = "书籍"),
    ComposeTextTab(type = "game", labelText = "游戏"),
    ComposeTextTab(type = "music", labelText = "音乐"),
    ComposeTextTab(type = "real", labelText = "三次元"),
    ComposeTextTab(type = "character", labelText = "角色"),
    ComposeTextTab(type = "person", labelText = "人物"),
    ComposeTextTab(type = "topic", labelText = "小组"),
    ComposeTextTab(type = "ep", labelText = "章节"),
    ComposeTextTab(type = "blog", labelText = "日志"),
)

private val indexFilterYearOptions = persistentListOf(
    ComposeTextTab(type = "", labelText = "不限"),
    ComposeTextTab(type = "1y", labelText = "近1年"),
    ComposeTextTab(type = "3y", labelText = "近3年"),
    ComposeTextTab(type = "2026", labelText = "2026"),
    ComposeTextTab(type = "2025", labelText = "2025"),
    ComposeTextTab(type = "2024", labelText = "2024"),
    ComposeTextTab(type = "2023", labelText = "2023"),
    ComposeTextTab(type = "2022", labelText = "2022"),
    ComposeTextTab(type = "2021", labelText = "2021"),
    ComposeTextTab(type = "2020", labelText = "2020"),
    ComposeTextTab(type = "2019", labelText = "2019"),
    ComposeTextTab(type = "2018", labelText = "2018"),
    ComposeTextTab(type = "2017", labelText = "2017"),
    ComposeTextTab(type = "2016", labelText = "2016"),
    ComposeTextTab(type = "2015", labelText = "2015"),
    ComposeTextTab(type = "2014", labelText = "2014"),
    ComposeTextTab(type = "2013", labelText = "2013"),
    ComposeTextTab(type = "2012", labelText = "2012"),
    ComposeTextTab(type = "2011", labelText = "2011"),
    ComposeTextTab(type = "2010", labelText = "2010"),
)

private val indexFilterKeywordOptions = persistentListOf(
    ComposeTextTab(type = "", labelText = "不限"),
    ComposeTextTab(type = "动画", labelText = "动画"),
    ComposeTextTab(type = "漫画", labelText = "漫画"),
    ComposeTextTab(type = "作品", labelText = "作品"),
    ComposeTextTab(type = "个人", labelText = "个人"),
    ComposeTextTab(type = "游戏", labelText = "游戏"),
    ComposeTextTab(type = "日本", labelText = "日本"),
    ComposeTextTab(type = "推荐", labelText = "推荐"),
    ComposeTextTab(type = "小说", labelText = "小说"),
    ComposeTextTab(type = "系列", labelText = "系列"),
    ComposeTextTab(type = "百合", labelText = "百合"),
    ComposeTextTab(type = "排行榜", labelText = "排行榜"),
    ComposeTextTab(type = "自用", labelText = "自用"),
    ComposeTextTab(type = "汉化", labelText = "汉化"),
    ComposeTextTab(type = "年代", labelText = "年代"),
    ComposeTextTab(type = "销量", labelText = "销量"),
    ComposeTextTab(type = "女性", labelText = "女性"),
    ComposeTextTab(type = "黄油", labelText = "黄油"),
    ComposeTextTab(type = "名作", labelText = "名作"),
    ComposeTextTab(type = "世界", labelText = "世界"),
    ComposeTextTab(type = "合集", labelText = "合集"),
    ComposeTextTab(type = "动漫", labelText = "动漫"),
    ComposeTextTab(type = "画师", labelText = "画师"),
    ComposeTextTab(type = "佳作", labelText = "佳作"),
    ComposeTextTab(type = "国产", labelText = "国产"),
    ComposeTextTab(type = "这本", labelText = "这本"),
    ComposeTextTab(type = "最佳", labelText = "最佳"),
    ComposeTextTab(type = "实用", labelText = "实用"),
    ComposeTextTab(type = "排名", labelText = "排名"),
    ComposeTextTab(type = "剧场版", labelText = "剧场版"),
    ComposeTextTab(type = "综合", labelText = "综合"),
    ComposeTextTab(type = "公司", labelText = "公司"),
    ComposeTextTab(type = "美少女", labelText = "美少女"),
    ComposeTextTab(type = "厉害", labelText = "厉害"),
    ComposeTextTab(type = "冷门", labelText = "冷门"),
    ComposeTextTab(type = "剧情", labelText = "剧情"),
    ComposeTextTab(type = "制作", labelText = "制作"),
    ComposeTextTab(type = "漫画家", labelText = "漫画家"),
    ComposeTextTab(type = "监督", labelText = "监督"),
    ComposeTextTab(type = "动画短片", labelText = "动画短片"),
    ComposeTextTab(type = "世纪", labelText = "世纪"),
    ComposeTextTab(type = "持续", labelText = "持续"),
    ComposeTextTab(type = "资源", labelText = "资源"),
    ComposeTextTab(type = "作画", labelText = "作画"),
    ComposeTextTab(type = "后宫", labelText = "后宫"),
    ComposeTextTab(type = "本子", labelText = "本子"),
    ComposeTextTab(type = "批评", labelText = "批评"),
    ComposeTextTab(type = "声优", labelText = "声优"),
    ComposeTextTab(type = "二次元", labelText = "二次元"),
    ComposeTextTab(type = "历年", labelText = "历年"),
    ComposeTextTab(type = "少女", labelText = "少女"),
    ComposeTextTab(type = "音乐", labelText = "音乐"),
    ComposeTextTab(type = "人气", labelText = "人气"),
    ComposeTextTab(type = "整理", labelText = "整理"),
    ComposeTextTab(type = "短篇", labelText = "短篇"),
    ComposeTextTab(type = "动画电影", labelText = "动画电影"),
    ComposeTextTab(type = "中日", labelText = "中日"),
    ComposeTextTab(type = "适合", labelText = "适合"),
    ComposeTextTab(type = "恋爱", labelText = "恋爱"),
    ComposeTextTab(type = "空间", labelText = "空间"),
    ComposeTextTab(type = "推理", labelText = "推理"),
    ComposeTextTab(type = "优秀", labelText = "优秀"),
    ComposeTextTab(type = "元素", labelText = "元素"),
    ComposeTextTab(type = "经典", labelText = "经典"),
    ComposeTextTab(type = "补充", labelText = "补充"),
    ComposeTextTab(type = "会社", labelText = "会社"),
    ComposeTextTab(type = "评选", labelText = "评选"),
    ComposeTextTab(type = "纯爱", labelText = "纯爱"),
    ComposeTextTab(type = "猎奇", labelText = "猎奇"),
    ComposeTextTab(type = "完结", labelText = "完结"),
    ComposeTextTab(type = "大陆", labelText = "大陆"),
    ComposeTextTab(type = "作者", labelText = "作者"),
    ComposeTextTab(type = "演出", labelText = "演出"),
    ComposeTextTab(type = "群友", labelText = "群友"),
    ComposeTextTab(type = "超级", labelText = "超级"),
    ComposeTextTab(type = "独立", labelText = "独立"),
    ComposeTextTab(type = "含有", labelText = "含有"),
    ComposeTextTab(type = "韩国", labelText = "韩国"),
    ComposeTextTab(type = "主角", labelText = "主角"),
    ComposeTextTab(type = "艺术", labelText = "艺术"),
    ComposeTextTab(type = "排行", labelText = "排行"),
    ComposeTextTab(type = "媒体", labelText = "媒体"),
    ComposeTextTab(type = "日常", labelText = "日常"),
    ComposeTextTab(type = "作品集", labelText = "作品集"),
    ComposeTextTab(type = "看过", labelText = "看过"),
    ComposeTextTab(type = "要素", labelText = "要素"),
    ComposeTextTab(type = "引进", labelText = "引进"),
    ComposeTextTab(type = "精选", labelText = "精选"),
    ComposeTextTab(type = "成人", labelText = "成人"),
    ComposeTextTab(type = "中文", labelText = "中文"),
    ComposeTextTab(type = "名单", labelText = "名单"),
    ComposeTextTab(type = "资深", labelText = "资深"),
    ComposeTextTab(type = "受赏", labelText = "受赏"),
    ComposeTextTab(type = "偶像", labelText = "偶像"),
    ComposeTextTab(type = "大师", labelText = "大师"),
    ComposeTextTab(type = "脚本", labelText = "脚本"),
    ComposeTextTab(type = "大全", labelText = "大全"),
    ComposeTextTab(type = "提及", labelText = "提及"),
    ComposeTextTab(type = "观看", labelText = "观看"),
    ComposeTextTab(type = "顺序", labelText = "顺序"),
    ComposeTextTab(type = "妹妹", labelText = "妹妹"),
)

@Composable
fun HomeIndexScreen(
    state: HomeState,
    onUiEvent: (HomeEvent.UI) -> Unit,
    onActionEvent: (HomeEvent.Action) -> Unit,
) {
    // 当前选中的 Tab 索引
    var selectedTabIndex by remember { mutableStateOf(0) }

    // 筛选状态（精选、最热和最新模式共用）
    var filterType by remember { mutableStateOf("") }
    var filterYear by remember { mutableStateOf("") }
    var filterKeyword by remember { mutableStateOf("") }

    // 当前 Tab 类型
    val currentTabType = if (mainHomeIndexFilters.isNotEmpty() && selectedTabIndex < mainHomeIndexFilters.size) {
        mainHomeIndexFilters[selectedTabIndex].type
    } else ""

    // 筛选工具栏在精选、最热和最新模式显示
    val showFilters = currentTabType == IndexHomepageType.FEATURED ||
        currentTabType == IndexHomepageType.HOT ||
        currentTabType == IndexHomepageType.NEWEST

    val typePrefix = stringResource(Res.string.index_filter_type)
    val yearPrefix = stringResource(Res.string.index_filter_year)
    val keywordPrefix = stringResource(Res.string.index_filter_keyword)

    Column(modifier = Modifier.fillMaxSize()) {
        BgmChipHorizontalPager(
            modifier = Modifier.fillMaxSize(),
            tabs = mainHomeIndexFilters,
            onTabSelected = { selectedTabIndex = it },
            extra = if (showFilters) {
                {
                    // 筛选工具栏：类型、时间、关键词
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        DropMenuChip(
                            options = indexFilterTypeOptions,
                            current = filterType,
                            labelPrefix = typePrefix,
                            onOptionClick = { filterType = it.type },
                        )
                        DropMenuChip(
                            options = indexFilterYearOptions,
                            current = filterYear,
                            labelPrefix = yearPrefix,
                            onOptionClick = { filterYear = it.type },
                        )
                        DropMenuChip(
                            options = indexFilterKeywordOptions,
                            current = filterKeyword,
                            labelPrefix = keywordPrefix,
                            onOptionClick = { filterKeyword = it.type },
                        )
                    }
                }
            } else null,
        ) { page ->
            val order = mainHomeIndexFilters[page].type

            when (order) {
                // 精选模式：使用离线数据 + 客户端筛选
                IndexHomepageType.FEATURED -> {
                    HomeFeaturedScreen(
                        filterType = filterType,
                        filterYear = filterYear,
                        filterKeyword = filterKeyword,
                        onUiEvent = onUiEvent,
                    )
                }
                // 最热/最新：无筛选时使用网页浏览结果，有筛选时在网页结果上客户端过滤
                else -> {
                    val param = state.rememberListIndexParam(
                        order = order,
                        filterType = filterType,
                        filterYear = filterYear,
                        filterKeyword = filterKeyword,
                    )
                    IndexPageRoute(
                        param = param,
                        onNavScreen = { screen -> onUiEvent(HomeEvent.UI.OnNavScreen(screen)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeIndexFocusHeader(
    state: HomeState,
    onUiEvent: (HomeEvent.UI) -> Unit,
    onActionEvent: (HomeEvent.Action) -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
            .aspectRatio(3f),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val items = remember(state.indexFocus) { state.indexFocus.take(3) }

        items.forEach {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .border(4.dp, MaterialTheme.colorScheme.onSurface)
            ) {
                IndexFocusCard(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(4.dp, MaterialTheme.colorScheme.onSurface)
                        .shadow(1.dp),
                    images = it.images.take(13).toPersistentList()
                )

                Text(text = it.title)
            }
        }
    }
}
