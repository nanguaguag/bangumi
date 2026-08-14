package com.xiaoyv.bangumi.features.main.tab.home.business

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import com.xiaoyv.bangumi.shared.core.types.IndexHomepageType
import com.xiaoyv.bangumi.shared.core.types.SubjectWebPath
import com.xiaoyv.bangumi.shared.core.types.list.ListBlogType
import com.xiaoyv.bangumi.shared.core.types.list.ListGroupType
import com.xiaoyv.bangumi.shared.core.types.list.ListIndexType
import com.xiaoyv.bangumi.shared.core.utils.serialization.SerializeList
import com.xiaoyv.bangumi.shared.data.model.emnu.GroupFilterMode
import com.xiaoyv.bangumi.shared.data.model.request.list.blog.ListBlogParam
import com.xiaoyv.bangumi.shared.data.model.request.list.group.ListGroupBrowserParam
import com.xiaoyv.bangumi.shared.data.model.request.list.group.ListGroupParam
import com.xiaoyv.bangumi.shared.data.model.request.list.index.IndexSearchBody
import com.xiaoyv.bangumi.shared.data.model.request.list.index.ListIndexParam
import com.xiaoyv.bangumi.shared.data.model.response.bgm.ComposeHomeSection
import com.xiaoyv.bangumi.shared.data.model.response.bgm.ComposeHomepageCard
import com.xiaoyv.bangumi.shared.data.model.response.bgm.index.ComposeIndexFocus
import com.xiaoyv.bangumi.shared.data.model.response.bgm.subject.ComposeSubjectDisplay
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [HomeState]
 *
 * @author why
 * @since 2025/1/12
 */
@Serializable
@Immutable
data class HomeState(
    /**
     * 首页
     */
    @SerialName("hotSubjects") val hotSubjects: SerializeList<ComposeSubjectDisplay> = persistentListOf(),

    /**
     * 下发横向条目数据
     */
    @SerialName("sections") val sections: List<ComposeHomepageCard> = emptyList(),

    /**
     * 追番日程
     */
    @SerialName("todayTotal") val todayTotal: Int = 0,
    @SerialName("todayCalendar") val todayCalendar: SerializeList<ComposeHomeSection> = persistentListOf(),
    @SerialName("tomorrow") val tomorrow: Int = 0,
    @SerialName("tomorrowCalendar") val tomorrowCalendar: SerializeList<ComposeHomeSection> = persistentListOf(),

    /**
     * 目录TAB首页
     */
    @SerialName("indexFocus") val indexFocus: SerializeList<ComposeIndexFocus> = persistentListOf(),
) {

    @Composable
    fun rememberListBlogParam(@SubjectWebPath type: String): ListBlogParam {
        return remember(type) {
            ListBlogParam(
                type = ListBlogType.BROWSER,
                browser = type
            )
        }
    }

    @Composable
    fun rememberListIndexParam(
        order: String,
        filterType: String = "",
        filterYear: String = "",
        filterKeyword: String = "",
    ): ListIndexParam {
        return remember(order, filterType, filterYear, filterKeyword) {
            val hasFilters = filterType.isNotBlank() ||
                filterYear.isNotBlank() ||
                filterKeyword.isNotBlank()

            if (hasFilters) {
                // 最热/最新的筛选在网页目录结果上执行，支持空关键词及类型、时间组合筛选
                ListIndexParam(
                    type = ListIndexType.BROWSER,
                    browserOrder = order,
                    browserFilter = IndexSearchBody(
                        keyword = filterKeyword,
                        exact = false,
                        order = if (order == IndexHomepageType.HOT) "collects" else "updated_at",
                        type = filterType,
                        year = filterYear,
                    )
                )
            } else {
                // 没有筛选条件时保留网页浏览 API，维持最热/最新原有排序和分页行为
                ListIndexParam(
                    type = ListIndexType.BROWSER,
                    browserOrder = order
                )
            }
        }
    }

    @Composable
    fun rememberListGroupParam(sort: String): ListGroupParam {
        return remember {
            ListGroupParam(
                type = ListGroupType.BROWSER,
                browser = ListGroupBrowserParam(
                    sort = sort,
                    mode = GroupFilterMode.All
                )
            )
        }
    }
}
