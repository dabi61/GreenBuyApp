package com.example.greenbuyapp.data.product.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApproveProductRequest(
    val approved: Boolean,
    val approval_note: String
) 