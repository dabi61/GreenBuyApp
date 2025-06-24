package com.example.greenbuyapp.domain.category

import com.example.greenbuyapp.data.category.CategoryService
import com.example.greenbuyapp.data.category.model.Category
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
            categoryService.getCategories()
        }
    }
}