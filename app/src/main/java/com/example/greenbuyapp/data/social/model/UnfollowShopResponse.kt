package com.example.greenbuyapp.data.social.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UnfollowShopResponse (
    val message: String
)
