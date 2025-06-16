package com.example.greenbuyapp.data.register.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
)