package com.example.greenbuyapp.data.authorization.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val grant_type: String = "password",
    val username: String,
    val password: String,
    val scope: String? = null,
    val client_id: String? = null,
    val client_secret: String? = null
) 