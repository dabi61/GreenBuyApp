package com.example.greenbuyapp.data.authorization.model

import com.squareup.moshi.JsonClass
 
@JsonClass(generateAdapter = true)
data class RefreshTokenRequest(
    val old_refresh_data: String
) 