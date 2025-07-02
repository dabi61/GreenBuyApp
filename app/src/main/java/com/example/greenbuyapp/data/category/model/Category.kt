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

@Parcelize
@JsonClass(generateAdapter = true)
data class SubCategory(
    val category_id: Int,
    val name: String,
    val description: String,
    val id: Int,
    val created_at: String
) : Parcelable