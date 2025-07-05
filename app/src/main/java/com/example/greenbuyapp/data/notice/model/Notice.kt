package com.example.greenbuyapp.data.notice.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Notice(
    val id: Int,
    val order_number: String,
    val status: String,
    val total_items: Int,
    val total_amount: Int,
    val created_at: String
)