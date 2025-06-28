package com.example.greenbuyapp.data.shop.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Shop (
    val id: Int,
    val user_id: Int,
    val avatar: String,
    val name: String,
    val phone_number: String,
    val is_active: Boolean,
    val is_online: Boolean,
    val create_at: String
) : Parcelable


@Parcelize
@JsonClass(generateAdapter = true)
data class MyShopStats (
    val shop_id: Int,
    val shop_name: String,
    val pending_pickup: Int,
    val cancelled_orders: Int,
    val ratings_count: Int,
    val total_orders: Int,
    val delivered_orders: Int,
    val average_rating: Float,
    val stats_generated_at: String
) : Parcelable