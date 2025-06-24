package com.example.greenbuyapp.domain.product

import com.example.greenbuyapp.data.product.ProductService
import com.example.greenbuyapp.data.product.model.Product
import com.example.greenbuyapp.ui.base.BaseDataSource
import com.example.greenbuyapp.util.Result
import com.example.greenbuyapp.util.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ProductDataSource(
    private val productService: ProductService,
    private val search: String? = null,
    private val categoryId: Int? = null,
    private val subCategoryId: Int? = null,
    private val shopId: Int? = null,
    private val minPrice: Double? = null,
    private val maxPrice: Double? = null,
    private val sortBy: String? = null,
    private val sortOrder: String? = null,
    private val approvedOnly: Boolean? = null,
    scope: CoroutineScope
) : BaseDataSource<Product>(scope) {

    override suspend fun getPage(page: Int, perPage: Int): List<Product> {
        println("🔍 ProductDataSource.getPage() called:")
        println("   page: $page, perPage: $perPage")
        println("   search: $search")
        println("   categoryId: $categoryId")
        println("   approvedOnly: $approvedOnly")
        
        val result = safeApiCall(Dispatchers.IO) {
            productService.getProducts(
                page = page,
                limit = perPage,
                search = search,
                categoryId = categoryId,
                subCategoryId = subCategoryId,
                shopId = shopId,
                minPrice = minPrice,
                maxPrice = maxPrice,
                sortBy = sortBy,
                sortOrder = sortOrder,
                approvedOnly = approvedOnly
            )
        }

        return when (result) {
            is Result.Success -> {
                println("✅ Products loaded: ${result.value.items.size} items")
                result.value.items.forEach { product ->
                    println("   Product: ${product.name}")
                }
                result.value.items
            }
            is Result.Error -> {
                println("❌ Products error: ${result.error}")
                emptyList()
            }
            is Result.NetworkError -> {
                println("🌐 Products network error")
                emptyList()
            }
            else -> {
                println("❓ Products unknown error")
                emptyList()
            }
        }
    }
} 