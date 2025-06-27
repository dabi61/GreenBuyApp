package com.example.greenbuyapp.data.product.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Product(
    val product_id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val cover: String?,
    val shop_id: Int,
    val sub_category_id: Int,
    val is_approved: Boolean,
    val create_at: String
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class TrendingProduct(
    val product_id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val cover: String?,
    val shop_id: Int,
    val sub_category_id: Int,
    val create_at: String
) : Parcelable


@Parcelize
@JsonClass(generateAdapter = true)
data class shopProducts(
    val shop_id: Int,
    val sub_category_id: Int,
    val name: String,
    val description: String,
    val cover: String?,
    val price: Double,
    val product_id: Int,
    val approved_by: Boolean?,
    val create_at: String
) : Parcelable