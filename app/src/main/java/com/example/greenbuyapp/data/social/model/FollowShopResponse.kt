package com.example.greenbuyapp.data.social.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FollowShopResponse (
    val id: Int,
    val user_id: Int,
    val shop_id: Int,
    val created_at: String,
    val shop_name: String
)