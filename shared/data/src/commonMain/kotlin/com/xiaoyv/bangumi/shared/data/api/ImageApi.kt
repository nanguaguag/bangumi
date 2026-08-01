package com.xiaoyv.bangumi.shared.data.api

import com.xiaoyv.bangumi.shared.core.types.AppJsonApiDsl
import com.xiaoyv.bangumi.shared.data.model.response.image.ComposeAnimePicture
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query

/**
 * [ImageApi]
 *
 * @since 2025/5/22
 */
@AppJsonApiDsl
interface ImageApi {

    /**
     * Anime-Pictures
     */
    @GET("https://api.anime-pictures.net/api/v3/posts")
    suspend fun fetchAnimePictures(
        @Query("search_tag") searchTags: String? = null,
        @Query("denied_tags") deniedTags: String? = null,
        @Query("lang") lang: String = "zh_CN",
        @Query("ldate") lDate: String = "0",
        @Query("order_by") orderBy: String = "date",
        @Query("page") page: Int = 0,
        @Query("posts_per_page") size: Int = 20,
    ): ComposeAnimePicture
}