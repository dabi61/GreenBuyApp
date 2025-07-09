package com.example.greenbuyapp.data.social

import com.example.greenbuyapp.data.social.model.FollowShopRequest
import com.example.greenbuyapp.data.social.model.FollowShopResponse
import com.example.greenbuyapp.data.social.model.FollowStatsResponse
import com.example.greenbuyapp.data.social.model.FollowerShop
import com.example.greenbuyapp.data.social.model.FollowingShop
import com.example.greenbuyapp.data.social.model.GetRatingShopResponse
import com.example.greenbuyapp.data.social.model.RatingShopRequest
import com.example.greenbuyapp.data.social.model.RatingSummaryResponse
import com.example.greenbuyapp.data.social.model.UnfollowShopResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SocialService {
    @GET("api/user/follow/stats")
    suspend fun getFollowStats(): FollowStatsResponse

    /**
     * Bỏ theo dõi shop
     */
    @DELETE("api/user/follow/shop/{shop_id}")
    suspend fun unfollowShop(
        @Path("shop_id") shopId: Int
    ): Response<UnfollowShopResponse>

    /**
     * Theo dõi shop
     */
    @POST("api/user/follow/shop")
    suspend fun followShop(
        @Body request: FollowShopRequest
    ): Response<FollowShopResponse>

    /**
     * Lấy danh sách shop follow
     */
    @GET("api/user/following/shops")
    suspend fun followingShops(): List<FollowingShop>

    /**
     * Lấy danh sách người theo dõi của 1 shop cụ the
     */
    @GET("api/user/followers/shop/{shop_id}")
    suspend fun CountFollowerShops(
        @Path("shop_id")shopId: Int?,
        @Query("page")page: Int? = null,
        @Query("limit")limit: Int? = null
    ): List<FollowerShop>

    /**
     * Lấy đánh giá của một shop
     */
    @GET("api/user/rating/shop/{shop_id}")
    suspend fun getRatingShops(
        @Path("shop_id")shopId: Int?,
        @Query("page")page: Int? = null,
        @Query("limit")limit: Int? = null
    ): List<GetRatingShopResponse>

    /**
     * Đánh giá một shop
     */
    @POST("api/user/rating/shop")
    suspend fun ratingShops(
        @Body request: RatingShopRequest
    ): Response<GetRatingShopResponse>

    /**
     * Lấy sao đánh giá trung bình của shop
     */
    @GET("api/user/rating/shop/{shop_id}/stats")
    suspend fun getStarRatingShops(
        @Path("shop_id")shopId: Int?
    ): RatingSummaryResponse

}