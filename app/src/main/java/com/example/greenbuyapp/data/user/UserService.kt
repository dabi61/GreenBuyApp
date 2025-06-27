package com.example.greenbuyapp.data.user

import com.example.greenbuyapp.data.user.model.ChangeRoleRequest
import com.example.greenbuyapp.data.user.model.Me
import com.example.greenbuyapp.data.user.model.User
import com.example.greenbuyapp.data.user.model.UserMeResponse
import retrofit2.http.*

interface UserService {
    @GET("api/user/me")
    suspend fun getUserMe(): UserMeResponse

    @PATCH("api/user/me/change-role")
    suspend fun changeRole(
        @Body request: ChangeRoleRequest
    ): UserMeResponse
}
