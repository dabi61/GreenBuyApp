package com.example.greenbuyapp.data.product.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProductsByStatusResponse(
    val items: List<Product>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val total_pages: Int,
    val has_next: Boolean,
    val has_prev: Boolean,
    val status: String,
    val shop_id: Int,
    val shop_name: String
) 