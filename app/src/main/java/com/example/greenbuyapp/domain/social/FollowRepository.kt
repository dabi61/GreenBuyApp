package com.example.greenbuyapp.domain.social

import com.example.greenbuyapp.data.social.SocialService
import com.example.greenbuyapp.data.social.model.FollowShopRequest
import com.example.greenbuyapp.data.social.model.FollowShopResponse
import com.example.greenbuyapp.data.social.model.FollowingShop
import com.example.greenbuyapp.data.social.model.UnfollowShopResponse
import com.example.greenbuyapp.util.Result
import com.example.greenbuyapp.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class FollowRepository(
    private val socialService: SocialService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun followShop(request: FollowShopRequest): Result<FollowShopResponse> {
        return safeApiCall(dispatcher) {
            val response = socialService.followShop(request)
            response.body() ?: throw Exception("Empty response from followShop")
        }
    }

    suspend fun unfollowShop(shopId: Int): Result<UnfollowShopResponse> {
        return safeApiCall(dispatcher) {
            val response = socialService.unfollowShop(shopId)
            response.body() ?: throw Exception("Empty response from unfollowShop")
        }
    }

    suspend fun getFollowingShops(): Result<List<FollowingShop>> {
        return safeApiCall(dispatcher) {
            socialService.followingShops()
        }
    }

    // đếm số lợng follow cho 1 shop bất kì
    suspend fun getFollowerCount(shopId: Int): Result<Int> {
        return safeApiCall(dispatcher) {
            socialService.CountFollowerShops(shopId).size
        }
    }
}