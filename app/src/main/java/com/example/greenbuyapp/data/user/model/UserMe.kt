package com.example.greenbuyapp.data.user.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserMeResponse(
    val user: UserMe
)

@JsonClass(generateAdapter = true)
data class UserMe(
    val id: Int,
    val username: String,
    val email: String,
    val first_name: String?,
    val last_name: String?,
    val avatar: String?,
    val phone_number: String?,
    val birth_date: String?,
    val bio: String?,
    val role: String,
    val is_active: Boolean,
    val is_online: Boolean,
    val is_verified: Boolean,
    val created_at: String,
    val updated_at: String
)
