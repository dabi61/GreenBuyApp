package com.example.greenbuyapp.domain.category

import com.example.greenbuyapp.data.MessageResponse
import com.example.greenbuyapp.data.category.CategoryService
import com.example.greenbuyapp.data.category.model.*
import com.example.greenbuyapp.util.Result
import com.example.greenbuyapp.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class CategoryRepository(
    private val categoryService: CategoryService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun getCategories(): Result<List<Category>> {
        return safeApiCall(dispatcher) {
            val response = categoryService.getCategories()
            response.items
        }
    }
    
    suspend fun getSubCategories(categoryId: Int): Result<List<SubCategory>> {
        return safeApiCall(dispatcher) {
            categoryService.getSubCategories(categoryId)
        }
    }
    
    suspend fun getAllSubCategories(): Result<List<SubCategory>> {
        return safeApiCall(dispatcher) {
            categoryService.getAllSubCategories()
        }
    }
    
    suspend fun createCategory(request: CreateCategoryRequest): Result<Category> {
        return safeApiCall(dispatcher) {
            categoryService.createCategory(request)
        }
    }
    
    suspend fun updateCategory(categoryId: Int, request: UpdateCategoryRequest): Result<Category> {
        return safeApiCall(dispatcher) {
            categoryService.updateCategory(categoryId, request)
        }
    }
    
    suspend fun deleteCategory(categoryId: Int): Result<Category> {
        return safeApiCall(dispatcher) {
            categoryService.deleteCategory(categoryId)
        }
    }
    
    suspend fun createSubCategory(request: CreateSubCategoryRequest): Result<SubCategory> {
        return safeApiCall(dispatcher) {
            categoryService.createSubCategory(request)
        }
    }
    
    suspend fun updateSubCategory(subcategoryId: Int, request: UpdateSubCategoryRequest): Result<SubCategory> {
        return safeApiCall(dispatcher) {
            categoryService.updateSubCategory(subcategoryId, request)
        }
    }
    
    suspend fun deleteSubCategory(subcategoryId: Int): Result<MessageResponse> {
        return safeApiCall(dispatcher) {
            categoryService.deleteSubCategory(subcategoryId)
        }
    }
}