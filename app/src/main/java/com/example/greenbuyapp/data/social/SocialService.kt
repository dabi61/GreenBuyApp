package com.example.greenbuyapp.data.social

import com.example.greenbuyapp.data.social.model.FollowShopRequest
import com.example.greenbuyapp.data.social.model.FollowShopResponse
import com.example.greenbuyapp.data.social.model.FollowStatsResponse
import com.example.greenbuyapp.data.social.model.FollowingShop
import com.example.greenbuyapp.data.social.model.UnfollowShopResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

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
}