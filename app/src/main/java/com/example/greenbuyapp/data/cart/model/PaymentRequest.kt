package com.example.greenbuyapp.data.cart.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PaymentMethod(
    val type: String = "credit_card",
    val card_number: String = "string",
    val card_holder_name: String = "string",
    val expiry_month: Int = 0,
    val expiry_year: Int = 0,
    val paypal_email: String = "string",
    val bank_name: String = "string",
    val account_number: String = "string",
    val account_holder: String = "string",
    val is_default: Boolean = false
)

@JsonClass(generateAdapter = true)
data class PaymentRequest(
    val payment_method_id: Int = 0,
    val save_payment_method: Boolean = false,
    val new_payment_method: PaymentMethod = PaymentMethod()
)