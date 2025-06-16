package com.example.greenbuyapp.data.authorization.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val access_token: String,
    val token_type: String,
    val refresh_token: String
)

