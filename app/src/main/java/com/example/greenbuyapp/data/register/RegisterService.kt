package com.example.greenbuyapp.data.register


import com.example.greenbuyapp.data.register.model.RegisterRequest
import com.example.greenbuyapp.data.register.model.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface RegisterService {

    @POST("api/user/register")
    suspend fun register(
        @Query("username") username: String,
        @Query("email") email: String,
        @Query("password") password: String
    ): RegisterResponse
}