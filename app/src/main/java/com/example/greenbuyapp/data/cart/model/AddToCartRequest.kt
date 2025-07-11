package com.example.greenbuyapp.data.cart.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddToCartRequest(
    val attribute_id: Int,
    val quantity: Int
) 