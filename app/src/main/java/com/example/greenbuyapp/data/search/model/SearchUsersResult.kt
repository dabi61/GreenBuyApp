package com.example.greenbuyapp.data.search.model

import com.example.greenbuyapp.data.user.model.User
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchUsersResult(
    val total: Int,
    val total_pages: Int,
    val results: List<User>
)
