package com.example.greenbuyapp.data.cart.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OrderItemRequest(
    val product_id: Int,
    val attribute_id: Int,
    val quantity: Int
)

@JsonClass(generateAdapter = true)
data class OrderRequest(
    val items: List<OrderItemRequest>,
    val shipping_address: String,
    val phone_number: String,
    val recipient_name: String,
    val delivery_notes: String,
    val billing_address: String
)