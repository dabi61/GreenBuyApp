package com.example.greenbuyapp.data.cart.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddToCartResponse(
    val message: String,
    val cart_item_id: Int? = null
) 