package com.example.greenbuyapp.data.shop.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Shop (
    val id: Int,
    val user_id: Int,
    val avatar: String,
    val name: String,
    val phone_number: String,
    val is_active: Boolean,
    val is_online: Boolean,
    val create_at: String
) : Parcelable