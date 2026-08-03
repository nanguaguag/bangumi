package com.xiaoyv.bangumi.shared.data.api

import com.xiaoyv.bangumi.shared.core.types.AppDsl
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivBookmarkResult
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivFollowResult
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivIllustDetailResult
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivIllustSearchResult
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivMeStateResponse
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivRelatedResult
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivToken
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivUserDetailResult
import de.jensklingenberg.ktorfit.http.Field
import de.jensklingenberg.ktorfit.http.FormUrlEncoded
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query

/**
 * [PixivApi]
 *
 * @since 2025/5/26
 */
@AppDsl
interface PixivApi {
    /**
     * 搜索插画作品。
     *
     * @param word 搜索关键词，必填。
     * @param searchTarget 搜索目标类型，默认为 `partial_match_for_tags`。
     *  - `partial_match_for_tags`: 标签部分匹配
     *  - `exact_match_for_tags`: 标签完全匹配
     *  - `title_and_caption`: 标题和说明匹配
     * @param sort 排序方式，默认为 `date_desc`。
     *  - `date_desc`: 按日期降序
     *  - `date_asc`: 按日期升序
     *  - `popular_desc`: 热门（需会员）
     * @param duration 时间范围过滤，可选。
     *  - `within_last_day`: 最近一天
     *  - `within_last_week`: 最近一周
     *  - `within_last_month`: 最近一月
     * @param startDate 起始日期，格式为 `yyyy-MM-dd`，可选。
     * @param endDate 截止日期，格式为 `yyyy-MM-dd`，可选。
     * @param filter 过滤器，默认为 `for_ios`。
     * @param searchAiType 是否显示 AI 生成作品，0 过滤，1 显示，默认为 null（不指定）。
     * @param offset 翻页偏移量，用于分页查询，默认为 null。
     */
    @GET("https://app-api.pixiv.net/v1/search/illust")
    suspend fun searchIllust(
        @Query("word") word: String,
        @Query("search_target") searchTarget: String = "exact_match_for_tags",
        @Query("sort") sort: String = "date_desc",
        @Query("duration") duration: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("filter") filter: String = "for_android",
        @Query("include_translated_tag_results") includeTranslatedTagResults: Boolean = true,
        @Query("search_ai_type") searchAiType: Int? = null,
        @Query("offset") offset: Int? = null,
    ): ComposePixivIllustSearchResult

    /**
     * 获取插画作品详情（含多页原图信息）。
     *
     * @param illustId 作品 ID。
     */
    @GET("https://app-api.pixiv.net/v1/illust/detail")
    suspend fun getIllustDetail(
        @Query("illust_id") illustId: Long,
    ): ComposePixivIllustDetailResult

    /**
     * 获取当前登录用户简要信息。
     *
     * /v1/user/me 已被 Pixiv 废弃（返回 404），改用 /v1/user/me/state。
     */
    @GET("v1/user/me/state")
    suspend fun getCurrentUser(): ComposePixivMeStateResponse

    /**
     * 获取用户详情。
     *
     * @param userId 用户 ID。
     */
    @GET("v1/user/detail")
    suspend fun getUserDetail(
        @Query("user_id") userId: Long,
    ): ComposePixivUserDetailResult

    /**
     * 收藏插画。
     *
     * @param illustId 作品 ID。
     * @param restrict 公开范围: public / private。
     */
    @FormUrlEncoded
    @POST("https://app-api.pixiv.net/v2/illust/bookmark/add")
    suspend fun bookmarkIllustAdd(
        @Field("illust_id") illustId: Long,
        @Field("restrict") restrict: String = "public",
    ): ComposePixivBookmarkResult

    /**
     * 取消收藏插画。
     *
     * @param illustId 作品 ID。
     */
    @FormUrlEncoded
    @POST("https://app-api.pixiv.net/v1/illust/bookmark/delete")
    suspend fun bookmarkIllustDelete(
        @Field("illust_id") illustId: Long,
    ): ComposePixivBookmarkResult

    /**
     * 关注用户。
     *
     * @param userId 用户 ID。
     * @param restrict 关注类型: public / private。
     */
    @FormUrlEncoded
    @POST("https://app-api.pixiv.net/v1/user/follow/add")
    suspend fun followUserAdd(
        @Field("user_id") userId: Long,
        @Field("restrict") restrict: String = "public",
    ): ComposePixivFollowResult

    /**
     * 取消关注用户。
     *
     * @param userId 用户 ID。
     */
    @FormUrlEncoded
    @POST("https://app-api.pixiv.net/v1/user/follow/delete")
    suspend fun followUserDelete(
        @Field("user_id") userId: Long,
    ): ComposePixivFollowResult

    /**
     * 获取相关插画。
     *
     * @param illustId 作品 ID。
     */
    @GET("https://app-api.pixiv.net/v2/illust/related")
    suspend fun getIllustRelated(
        @Query("illust_id") illustId: Long,
    ): ComposePixivRelatedResult

    @FormUrlEncoded
    @POST("https://oauth.secure.pixiv.net/auth/token")
    suspend fun sendAuthToken(
        @Field("client_id") clientId: String?,
        @Field("client_secret") clientSecret: String?,
        @Field("grant_type") grantType: String?,
        @Field("code") code: String?,
        @Field("code_verifier") codeVerifier: String?,
        @Field("redirect_uri") redirectUri: String?,
        @Field("include_policy") includePolicy: Boolean,
    ): ComposePixivToken

    @FormUrlEncoded
    @POST("https://oauth.secure.pixiv.net/auth/token")
    suspend fun sendAuthTokenRefresh(
        @Field("client_id") clientId: String?,
        @Field("client_secret") clientSecret: String?,
        @Field("refresh_token") refreshToken: String?,
        @Field("include_policy") includePolicy: Boolean,
        @Field("grant_type") grantType: String? = "refresh_token",
    ): ComposePixivToken
}
