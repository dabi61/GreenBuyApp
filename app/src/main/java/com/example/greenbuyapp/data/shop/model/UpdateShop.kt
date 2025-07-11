package com.example.greenbuyapp.data.shop.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateShopResponse (
    val user_id: Int,
    val name: String,
    val avatar: String,
    val is_active: Boolean,
    val is_online: Boolean,
    val id: Int,
    val phone_number: String,
    val create_at: String
)