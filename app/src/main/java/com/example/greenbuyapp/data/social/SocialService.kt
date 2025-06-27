package com.example.greenbuyapp.data.social

import com.example.greenbuyapp.data.social.model.FollowStatsResponse
import com.example.greenbuyapp.data.user.model.UserMeResponse
import retrofit2.http.GET

interface SocialService {
    @GET("api/user/follow/stats")
    suspend fun getFollowStats(): FollowStatsResponse


}