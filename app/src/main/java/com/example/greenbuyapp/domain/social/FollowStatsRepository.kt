package com.example.greenbuyapp.domain.social

import com.example.greenbuyapp.data.social.SocialService
import com.example.greenbuyapp.data.social.model.FollowStatsResponse
import com.example.greenbuyapp.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import com.example.greenbuyapp.util.Result


class FollowStatsRepository(
    private val socialService: SocialService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun getFollowStats(): Result<FollowStatsResponse> {
        val result = safeApiCall(dispatcher) {
            socialService.getFollowStats()
        }
        return result
    }
}