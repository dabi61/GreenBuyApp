package com.example.greenbuyapp.data.user.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateUserProfileRequest(
     val avatar: String?,
    val first_name: String?,
    val last_name: String?,
    val phone_number: String?,
    val birth_date: String?
)
