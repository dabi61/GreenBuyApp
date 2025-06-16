package com.example.greenbuyapp.data.user

import com.example.greenbuyapp.data.user.model.Me
import com.example.greenbuyapp.data.user.model.User
import retrofit2.http.*

interface UserService {

    @GET("users/{username}")
    suspend fun getUserPublicProfile(
        @Path("username") username: String
    ): User

//    @GET("users/{username}/portfolio")
//    suspend fun getUserPortfolioLink(
//        @Path("username") username: String
//    ): ResponseBody
//
//    @GET("users/{username}/photos")
//    suspend fun getUserPhotos(
//        @Path("username") username: String,
//        @Query("page") page: Int?,
//        @Query("per_page") per_page: Int?,
//        @Query("order_by") order_by: String?,
//        @Query("stats") stats: Boolean?,
//        @Query("resolution") resolution: String?,
//        @Query("quantity") quantity: Int?,
//        @Query("orientation") orientation: String?
//    ): List<Photo>
//
//    @GET("users/{username}/likes")
//    suspend fun getUserLikes(
//        @Path("username") username: String,
//        @Query("page") page: Int?,
//        @Query("per_page") per_page: Int?,
//        @Query("order_by") order_by: String?,
//        @Query("orientation") orientation: String?
//    ): List<Photo>
//
//    @GET("users/{username}/collections")
//    suspend fun getUserCollections(
//        @Path("username") username: String,
//        @Query("page") page: Int?,
//        @Query("per_page") per_page: Int?
//    ): List<Collection>
//
//    @GET("users/{username}/statistics")
//    suspend fun getUserStatistics(
//        @Path("username") username: String,
//        @Query("resolution") resolution: String?,
//        @Query("quantity") quantity: Int?
//    ): UserStatistics

    @GET("me")
    suspend fun getUserPrivateProfile(): Me

    @PUT("me")
    suspend fun updateUserPrivateProfile(
        @Query("username") username: String?,
        @Query("first_name") first_name: String?,
        @Query("last_name") last_name: String?,
        @Query("email") email: String?,
        @Query("url") url: String?,
        @Query("instagram_username") instagram_username: String?,
        @Query("location") location: String?,
        @Query("bio") bio: String?
    ): Me
}
