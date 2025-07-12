package com.example.greenbuyapp.data.category

import com.example.greenbuyapp.data.MessageResponse
import com.example.greenbuyapp.data.category.model.*
import retrofit2.http.*

interface CategoryService {
    @GET("api/category")
    suspend fun getCategories(): CategoryResponse
    
    @GET("api/category/{category_id}/subcategories")
    suspend fun getSubCategories(@Path("category_id") categoryId: Int): List<SubCategory>
    
    @GET("api/sub_category")
    suspend fun getAllSubCategories(): List<SubCategory>
    
    @POST("api/category")
    suspend fun createCategory(@Body request: CreateCategoryRequest): Category
    
    @PUT("api/category/{category_id}")
    suspend fun updateCategory(
        @Path("category_id") categoryId: Int,
        @Body request: UpdateCategoryRequest
    ): Category
    
    @DELETE("api/category/{category_id}")
    suspend fun deleteCategory(@Path("category_id") categoryId: Int): Category
    
    @POST("api/sub_category")
    suspend fun createSubCategory(
        @Body request: CreateSubCategoryRequest
    ): SubCategory
    
    @PUT("api/sub_category/{subcategory_id}")
    suspend fun updateSubCategory(
        @Path("subcategory_id") subcategoryId: Int,
        @Body request: UpdateSubCategoryRequest
    ): SubCategory
    
    @DELETE("api/sub_category/{subcategory_id}")
    suspend fun deleteSubCategory(@Path("subcategory_id") subcategoryId: Int): MessageResponse
}