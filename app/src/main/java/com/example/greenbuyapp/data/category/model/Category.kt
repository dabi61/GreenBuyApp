package com.example.greenbuyapp.data.category.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Category(
    val id: Int,
    val name: String,
    val description: String,
    val created_at: String
) : Parcelable