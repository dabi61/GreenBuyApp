package com.example.greenbuyapp.data.product.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateProductResponse(
    val shop_id: Int,
    val sub_category_id: Int,
    val name: String,
    val description: String,
    val cover: String,
    val price: Double,
    val product_id: Int,
    val approved_by: Int? = null,
    val create_at: String
) 