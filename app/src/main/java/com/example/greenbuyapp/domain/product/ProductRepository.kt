package com.example.greenbuyapp.domain.product

import com.example.greenbuyapp.data.product.ProductService
import com.example.greenbuyapp.data.product.model.Product
import com.example.greenbuyapp.data.product.model.ProductAttributeList
import com.example.greenbuyapp.data.product.model.ProductListResponse
import com.example.greenbuyapp.data.product.model.TrendingProduct
import com.example.greenbuyapp.data.product.model.TrendingProductResponse
import com.example.greenbuyapp.domain.Listing
import com.example.greenbuyapp.util.Result
import com.example.greenbuyapp.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ProductRepository(
    private val productService: ProductService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun getProducts(
        page: Int? = null,
        limit: Int? = null,
        search: String? = null,
        categoryId: Int? = null,
        subCategoryId: Int? = null,
        shopId: Int? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
        approvedOnly: Boolean? = null
    ): Result<ProductListResponse> {
        return safeApiCall(dispatcher) {
            productService.getProducts(
                page = page,
                limit = limit,
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
    }

    /**
     * Lấy danh sách sản phẩm trending
     * @param page Số trang (mặc định là 1)
     * @param limit Số lượng sản phẩm trên một trang (mặc định là 10)
     * @return Result<TrendingProductResponse> chứa danh sách sản phẩm trending
     */
    suspend fun getTrending(
        page: Int? = null,
        limit: Int? = null
    ): Result<TrendingProductResponse> {
        return safeApiCall(dispatcher) {
            productService.getTrending(
                page = page,
                limit = limit
            )
        }
    }

    /**
     * Lấy attributes của sản phẩm theo ID
     * @param productId ID của sản phẩm
     * @return Result<ProductAttributeList> chứa danh sách attributes
     */
    suspend fun getProductAttributes(productId: Int): Result<ProductAttributeList> {
        return safeApiCall(dispatcher) {
            productService.getProductAttributes(productId)
        }
    }

    fun getProductsPaged(
        search: String? = null,
        categoryId: Int? = null,
        subCategoryId: Int? = null,
        shopId: Int? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
        approvedOnly: Boolean? = null,
        scope: CoroutineScope
    ): Listing<Product> {
        return ProductDataSourceFactory(
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
        ).createListing()
    }
} 