package com.example.greenbuyapp.data.category

import com.example.greenbuyapp.data.category.model.Category
import retrofit2.http.GET

interface CategoryService {
    @GET("api/category")
    suspend fun getCategories(): List<Category>
}