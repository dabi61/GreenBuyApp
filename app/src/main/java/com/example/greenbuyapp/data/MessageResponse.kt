package com.example.greenbuyapp.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessageResponse (
    val message: String
)