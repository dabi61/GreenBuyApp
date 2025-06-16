package com.example.greenbuyapp.data.register.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterResponse (
    val message: String
)