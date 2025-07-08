package com.example.greenbuyapp.data.user.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddressAddRequest(
    val street: String,
    val city: String,
    val state: String,
    val zipcode: String,
    val country: String,
    @field:Json(name = "phone_number")
    val phone: String
)