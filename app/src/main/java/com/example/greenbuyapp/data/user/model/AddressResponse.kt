package com.example.greenbuyapp.data.user.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddressResponse (
    val id: Int,
    val user_id: Int,
    val street: String,
    val city: String,
    val state: String,
    val zipcode: String,
    val country: String,
    @Json(name = "phone_number")
    val phone: String,
    val is_default: Boolean,
    val created_at: String,
)