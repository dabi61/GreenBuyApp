package com.example.greenbuyapp.data.user.model

import android.net.Uri
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateUserProfileRequest(
    val avatar: Uri?,
    val first_name: String?,
    val last_name: String?,
    val phone_number: String?,
    val birth_date: String?
)
