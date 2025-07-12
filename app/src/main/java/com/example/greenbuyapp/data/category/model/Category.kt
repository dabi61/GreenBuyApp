package com.example.greenbuyapp.data.category.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

data class Category(
    val id: Int,
    val name: String,
    val description: String,
    val created_at: String
)

data class SubCategory(
    val id: Int,
    val category_id: Int,
    val name: String,
    val description: String,
    val created_at: String
)

data class CreateCategoryRequest(
    val name: String,
    val description: String
)

data class UpdateCategoryRequest(
    val name: String,
    val description: String
)

data class CreateSubCategoryRequest(
    val name: String,
    val description: String
)

data class UpdateSubCategoryRequest(
    val name: String,
    val description: String
)