package com.example.greenbuyapp.data.category.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import retrofit2.http.Path

@JsonClass(generateAdapter = true)
data class Category(
    val id: Int,
    val name: String,
    val description: String,
    val created_at: String
)
@JsonClass(generateAdapter = true)
data class SubCategory(
    val id: Int,
    val category_id: Int,
    val name: String,
    val description: String,
    val created_at: String
)
@JsonClass(generateAdapter = true)
data class CreateCategoryRequest(
    val name: String,
    val description: String
)

@JsonClass(generateAdapter = true)
data class UpdateCategoryRequest(
    val name: String,
    val description: String
)

@JsonClass(generateAdapter = true)
data class CreateSubCategoryRequest(
    val category_id: Int,
    val name: String,
    val description: String
)

@JsonClass(generateAdapter = true)
data class UpdateSubCategoryRequest(
    val name: String,
    val description: String
)