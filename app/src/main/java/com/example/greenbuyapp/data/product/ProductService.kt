package com.example.greenbuyapp.data.product

import com.example.greenbuyapp.data.product.model.ProductAttributeList
import com.example.greenbuyapp.data.product.model.ProductListResponse
import com.example.greenbuyapp.data.product.model.TrendingProduct
import com.example.greenbuyapp.data.product.model.TrendingProductResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductService {
    
    @GET("api/product/")
    suspend fun getProducts(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("search") search: String? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("sub_category_id") subCategoryId: Int? = null,
        @Query("shop_id") shopId: Int? = null,
        @Query("min_price") minPrice: Double? = null,
        @Query("max_price") maxPrice: Double? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null,
        @Query("approved_only") approvedOnly: Boolean? = null
    ): ProductListResponse

    @GET("api/product/trending")
    suspend fun getTrending(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): TrendingProductResponse

    /**
     * Lấy attributes của sản phẩm theo ID
     */
    @GET("api/attribute/product/{product_id}")
    suspend fun getProductAttributes(
        @Path("product_id") productId: Int
    ): ProductAttributeList
}