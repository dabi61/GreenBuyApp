package com.example.greenbuyapp.data.user.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class AddressDetailResponse(
    val id: Int,
    @Json(name = "user_id") val userId: Int,
    val street: String,
    val city: String,
    val state: String,
    val zipcode: String,
    val country: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "is_default") val isDefault: Boolean,
    @Json(name = "created_at") val createdAt: String
)

