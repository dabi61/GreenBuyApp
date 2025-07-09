package com.example.greenbuyapp.data.social.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetRatingShopResponse(
    val id: Int,
    val user_id: Int,
    val shop_id: Int,
    val rating: Int,
    val comment: String,
    val created_at: String,
    val updated_at: String,
    val user_username: String,
    val shop_name: String,
)

@JsonClass(generateAdapter = true)
data class RatingShopRequest(
    val shop_id: Int,
    val rating: Int,
    val comment: String
)

@JsonClass(generateAdapter = true)
data class RatingSummaryResponse(
    val total_ratings: Int,
    val average_rating: Double,
    val rating_breakdown: Map<String, Int>
)