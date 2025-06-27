package com.example.greenbuyapp.data.social.model

import com.example.greenbuyapp.data.user.model.UserMe
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class FollowStatsResponse(
    val followers_count: Int,
    val following_count: Int,
    val shop_following_count: Int,
    val my_shop_followers_count: Int,
)

@JsonClass(generateAdapter = true)
data class FollowerShop(
    val follower_id: Int,
    val user_id: Int,
    val username: String,
    val first_name: String,
    val last_name: String,
    val avatar: String,
    val follower_at: Date,
    val shop_id: Int,
    val shop_name: String,
)