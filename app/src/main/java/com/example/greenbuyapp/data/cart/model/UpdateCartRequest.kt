package com.example.greenbuyapp.data.cart.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class UpdateCartRequest(
    @Json(name = "quantity")
    val quantity: Int
) : Parcelable 