package com.example.greenbuyapp.data.cart.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PaymentResponse(
    val success: Boolean,
    val payment_id: Int,
    val transaction_id: String,
    val status: String,
    val amount: Double,
    val message: String,
    val redirect_url: String? = null
)