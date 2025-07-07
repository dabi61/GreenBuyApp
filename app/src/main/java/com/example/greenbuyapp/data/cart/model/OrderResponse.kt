package com.example.greenbuyapp.data.cart.model

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class OrderResponse(
    val id: Int,
    val order_number: String,
    val status: String,
    val subtotal: Double,
    val tax_amount: Double,
    val shipping_fee: Double,
    val discount_amount: Double,
    val total_amount: Double,
    val created_at: String,
    val updated_at: String,
    val confirmed_at: String?,
    val shipped_at: String?,
    val delivered_at: String?,
    val cancelled_at: String?,
    val billing_address: String,
    val shipping_address: String,
    val phone_number: String,
    val recipient_name: String,
    val delivery_notes: String,
    val notes: String?,
    val internal_notes: String?,
    val items: List<Any>
)