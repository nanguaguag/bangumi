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

fun <T : Any, K : Any, ItemKey> createNetworkKeyLimitPagingPager(
    pagingConfig: PagingConfig,
    keySelector: ((T) -> ItemKey)? = null,
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

class KeyLimitDataSource<T : Any, K : Any, ItemKey>(
    private val onLoadData: suspend (K?) -> Pair<List<T>, K?>,
    private val keySelector: ((T) -> ItemKey)?,
) : PagingSource<K, T>() {
    private companion object {
        // 被本地规则过滤的连续空页不应提前终止分页，同时限制异常游标造成的请求链。
        const val MAX_SKIPPED_EMPTY_PAGES = 10
    }

    private val seen = mutableSetOf<ItemKey>()

    override fun getRefreshKey(state: PagingState<K, T>) = null

    override suspend fun load(params: LoadParams<K>): LoadResult<K, T> {
        try {
            if (params is LoadParams.Refresh) seen.clear()

            var currentKey = params.key
            var skippedPages = 0

            while (true) {
                val (data, nextKey) = onLoadData(currentKey)
                val loadData = if (keySelector == null) data else data.filter { seen.add(keySelector(it)) }

                // 服务端 cursor 是唯一的结束条件；空的可见结果可能只是被过滤或去重。
                if (loadData.isNotEmpty() || nextKey == null) {
                    return LoadResult.Page(
                        data = loadData,
                        prevKey = null,
                        nextKey = nextKey,
                    )
                }

                if (nextKey == currentKey || skippedPages++ >= MAX_SKIPPED_EMPTY_PAGES) {
                    debugLog { "Paging cursor stalled after skipped empty pages: key=$currentKey, next=$nextKey" }
                    return LoadResult.Page(
                        data = emptyList(),
                        prevKey = null,
                        nextKey = null,
                    )
                }

                currentKey = nextKey
            }
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
