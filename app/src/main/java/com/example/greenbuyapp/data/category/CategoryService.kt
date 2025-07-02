package com.example.greenbuyapp.data.category

import com.example.greenbuyapp.data.category.model.Category
import com.example.greenbuyapp.data.category.model.SubCategory
import retrofit2.http.GET

interface CategoryService {
    @GET("api/category")
    suspend fun getCategories(): List<Category>
    
    @GET("api/sub_category")
    suspend fun getSubCategories(): List<SubCategory>
}