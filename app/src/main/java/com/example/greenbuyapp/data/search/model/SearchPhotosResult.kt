package com.example.greenbuyapp.data.search.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchPhotosResult(
    val total: Int,
    val total_pages: Int,
//    val results: List<Photo>
)
