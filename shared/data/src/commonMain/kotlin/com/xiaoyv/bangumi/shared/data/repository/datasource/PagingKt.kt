package com.xiaoyv.bangumi.shared.data.repository.datasource

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.xiaoyv.bangumi.shared.core.utils.debugLog

fun createPagingConfig(pageSize: Int): PagingConfig {
    return PagingConfig(
        pageSize = pageSize,
        initialLoadSize = pageSize,
        enablePlaceholders = false,
    )
}

fun <T : Any, K> createNetworkPageLimitPagingPager(
    pagingConfig: PagingConfig,
    onLoadData: suspend (Int) -> List<T>,
    keySelector: ((T) -> K)? = null,
    onlyOnePage: Boolean = false,
): Pager<Int, T> = Pager(
    config = pagingConfig,
    pagingSourceFactory = {
        PageLimitDataSource(
            onLoadData = onLoadData,
            onlyOnePage = onlyOnePage,
            keySelector = keySelector
        )
    }
)

fun <T : Any, K> createNetworkFilteredPageLimitPagingPager(
    pagingConfig: PagingConfig,
    onLoadData: suspend (Int) -> Pair<List<T>, Boolean>,
    keySelector: ((T) -> K)? = null,
    maxPagesPerLoad: Int = 100,
): Pager<Int, T> = Pager(
    config = pagingConfig,
    pagingSourceFactory = {
        FilteredPageLimitDataSource(
            onLoadData = onLoadData,
            keySelector = keySelector,
            maxPagesPerLoad = maxPagesPerLoad,
        )
    }
)

fun <T : Any, K> createNetworkOffsetLimitPagingPager(
    pagingConfig: PagingConfig,
    keySelector: ((T) -> K)? = null,
    onLoadData: suspend (Int) -> List<T>,
): Pager<Int, T> = Pager(
    config = pagingConfig,
    pagingSourceFactory = {
        OffsetLimitDataSource(
            onLoadData = onLoadData,
            keySelector = keySelector
        )
    }
)

fun <T : Any, K : Any> createNetworkKeyLimitPagingPager(
    pagingConfig: PagingConfig,
    keySelector: ((T) -> K)? = null,
    onLoadData: suspend (K?) -> Pair<List<T>, K?>,
): Pager<K, T> = Pager(
    config = pagingConfig,
    pagingSourceFactory = {
        KeyLimitDataSource(
            onLoadData = onLoadData,
            keySelector = keySelector
        )
    }
)


class PageLimitDataSource<T : Any, K>(
    private val onLoadData: suspend (Int) -> List<T>,
    private val keySelector: ((T) -> K)? = null,
    private val onlyOnePage: Boolean = false,
) : PagingSource<Int, T>() {
    private val initialKey = 1
    private val seen = mutableSetOf<K>()

    override fun getRefreshKey(state: PagingState<Int, T>) = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        try {
            val page = params.key ?: initialKey
            if (page <= 1) seen.clear()
            val data = onLoadData(page)

            // 是否去重
            val loadData = if (keySelector == null) data else data.filter { seen.add(keySelector(it)) }
            return LoadResult.Page(
                data = loadData,
                prevKey = if (page > 1) page - 1 else null,
                nextKey = if (loadData.isEmpty()) null else page + 1,
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}

class FilteredPageLimitDataSource<T : Any, K>(
    private val onLoadData: suspend (Int) -> Pair<List<T>, Boolean>,
    private val keySelector: ((T) -> K)? = null,
    private val maxPagesPerLoad: Int = 100,
) : PagingSource<Int, T>() {
    private val initialKey = 1
    private val seen = mutableSetOf<K>()

    override fun getRefreshKey(state: PagingState<Int, T>) = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        try {
            val firstPage = params.key ?: initialKey
            if (firstPage <= 1) seen.clear()

            // 过滤后页面可能为空，继续读取原始网页的后续页面，避免首屏无匹配结果时分页提前结束。
            var page = firstPage
            var pagesLoaded = 0
            while (pagesLoaded < maxPagesPerLoad) {
                val (data, hasMore) = onLoadData(page)
                pagesLoaded++
                val loadData = if (keySelector == null) data else data.filter { seen.add(keySelector(it)) }
                if (loadData.isNotEmpty() || !hasMore) {
                    return LoadResult.Page(
                        data = loadData,
                        prevKey = if (firstPage > 1) firstPage - 1 else null,
                        nextKey = if (hasMore) page + 1 else null,
                    )
                }
                page++
            }

            // 异常情况下服务端可能持续返回非空页面，防止过滤请求无界增长。
            return LoadResult.Page(
                data = emptyList(),
                prevKey = if (firstPage > 1) firstPage - 1 else null,
                nextKey = null,
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}

class KeyLimitDataSource<T : Any, K : Any>(
    private val onLoadData: suspend (K?) -> Pair<List<T>, K?>,
    private val keySelector: ((T) -> K)?,
) : PagingSource<K, T>() {
    private val initialKey = null
    private val seen = mutableSetOf<K>()

    override fun getRefreshKey(state: PagingState<K, T>) = null

    override suspend fun load(params: LoadParams<K>): LoadResult<K, T> {
        try {
            val offset = params.key ?: initialKey
            val res = onLoadData(offset)
            val data = res.first
            val nextKey = res.second
            val end = nextKey == null

            // 是否去重
            val loadData = if (keySelector == null) data else data.filter { seen.add(keySelector(it)) }
            return LoadResult.Page(
                data = loadData,
                prevKey = null,
                nextKey = if (end) null else nextKey,
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}


class OffsetLimitDataSource<T : Any, K>(
    private val onLoadData: suspend (Int) -> List<T>,
    private val keySelector: ((T) -> K)?,
) : PagingSource<Int, T>() {
    private val initialKey = 0
    private val seen = mutableSetOf<K>()

    override fun getRefreshKey(state: PagingState<Int, T>) = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        try {
            val offset = params.key ?: initialKey
            val data = onLoadData(offset)
            val end = data.size < params.loadSize
            debugLog { "end:$end,${data.size}" }
            // 是否去重
            val loadData = if (keySelector == null) data else data.filter { seen.add(keySelector(it)) }
            return LoadResult.Page(
                data = loadData,
                prevKey = if (offset >= params.loadSize) offset - params.loadSize else null,
                nextKey = if (end) null else offset + params.loadSize,
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}
