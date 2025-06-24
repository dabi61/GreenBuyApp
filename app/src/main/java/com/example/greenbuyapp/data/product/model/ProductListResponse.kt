package com.example.greenbuyapp.data.product.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProductListResponse(
    val items: List<Product>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val total_pages: Int,
    val has_next: Boolean,
    val has_prev: Boolean
)

@JsonClass(generateAdapter = true)
data class TrendingResponse(
    val items: List<Product>,
    val count: Int
)

@JsonClass(generateAdapter = true)
data class TrendingProductResponse(
    val items: List<TrendingProduct>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val total_pages: Int,
    val has_next: Boolean,
    val has_prev: Boolean
)