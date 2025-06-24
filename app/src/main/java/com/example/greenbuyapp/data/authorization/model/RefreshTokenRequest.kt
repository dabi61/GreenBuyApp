package com.example.greenbuyapp.data.authorization.model

import com.squareup.moshi.JsonClass
 
@JsonClass(generateAdapter = true)
data class RefreshTokenRequest(
    val refresh_token: String,
    val grant_type: String = "refresh_token"
) 