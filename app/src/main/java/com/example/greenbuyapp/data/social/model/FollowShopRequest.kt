package com.example.greenbuyapp.data.social.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FollowShopRequest (
    val shop_id: Int
)

