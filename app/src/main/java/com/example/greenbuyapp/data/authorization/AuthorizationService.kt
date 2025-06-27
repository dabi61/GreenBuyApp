package com.example.greenbuyapp.data.authorization

import com.example.greenbuyapp.data.authorization.model.AccessToken
import com.example.greenbuyapp.data.authorization.model.LoginResponse
import com.example.greenbuyapp.data.authorization.model.RefreshTokenRequest
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthorizationService {

    @FormUrlEncoded
    @POST("token")
    suspend fun login(
        @Field("grant_type") grantType: String = "password",
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("scope") scope: String? = null,
        @Field("client_id") clientId: String? = null,
        @Field("client_secret") clientSecret: String? = null
    ): LoginResponse

    @POST("token/refresh")
    suspend fun refreshToken(
        @Body refreshTokenRequest: RefreshTokenRequest
    ): LoginResponse

    @POST("logout")
    suspend fun logout(): Any
}
