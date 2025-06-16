package com.example.greenbuyapp.data.search.model

import com.squareup.moshi.JsonClass
import com.example.greenbuyapp.data.collection.model.Collection

@JsonClass(generateAdapter = true)
data class SearchCollectionsResult(
    val total: Int,
    val total_pages: Int,
    val results: List<Collection>
)
