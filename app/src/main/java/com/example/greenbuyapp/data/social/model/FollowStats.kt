package com.example.greenbuyapp.data.social.model

import com.example.greenbuyapp.data.user.model.UserMe
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FollowStatsResponse(
    val followers_count: Int,
    val following_count: Int,
    val shop_following_count: Int,
    val my_shop_followers_count: Int,
)