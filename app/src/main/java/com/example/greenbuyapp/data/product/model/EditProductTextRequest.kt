package com.example.greenbuyapp.data.product.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EditProductTextRequest(
    val name: String,
    val description: String,
    val price: Double,
    val sub_category_id: Int
) 