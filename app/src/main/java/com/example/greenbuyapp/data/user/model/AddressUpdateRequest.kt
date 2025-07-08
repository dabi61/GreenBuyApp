package com.example.greenbuyapp.data.user.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddressUpdateRequest(
    @Json(name = "street") val street: String,
    @Json(name = "city") val city: String,
    @Json(name = "state") val state: String,
    @Json(name = "zipcode") val zipcode: String,
    @Json(name = "country") val country: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "is_default") val isDefault: Boolean
)

