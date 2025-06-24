package com.example.greenbuyapp.domain.product

import com.example.greenbuyapp.data.product.ProductService
import com.example.greenbuyapp.data.product.model.Product
import com.example.greenbuyapp.ui.base.BaseDataSource
import com.example.greenbuyapp.ui.base.BaseDataSourceFactory
import kotlinx.coroutines.CoroutineScope

class ProductDataSourceFactory(
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
    private val scope: CoroutineScope
) : BaseDataSourceFactory<Product>() {

    override fun createDataSource(): BaseDataSource<Product> {
        return ProductDataSource(
            productService = productService,
            search = search,
            categoryId = categoryId,
            subCategoryId = subCategoryId,
            shopId = shopId,
            minPrice = minPrice,
            maxPrice = maxPrice,
            sortBy = sortBy,
            sortOrder = sortOrder,
            approvedOnly = approvedOnly,
            scope = scope
        )
    }
} 