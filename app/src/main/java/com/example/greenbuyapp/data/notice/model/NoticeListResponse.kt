package com.example.greenbuyapp.data.notice.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NoticeListResponse(
    val items: List<Notice>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val total_pages: Int,
    val has_next: Boolean,
    val has_prev: Boolean
)
