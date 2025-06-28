package com.example.greenbuyapp.data.product

import com.example.greenbuyapp.data.product.model.Product
import com.example.greenbuyapp.data.product.model.ProductAttributeList
import com.example.greenbuyapp.data.product.model.ProductListResponse
import com.example.greenbuyapp.data.product.model.TrendingProduct
import com.example.greenbuyapp.data.product.model.TrendingProductResponse
import com.example.greenbuyapp.data.product.model.shopProducts
import com.example.greenbuyapp.data.product.model.InventoryStatsResponse
import com.example.greenbuyapp.data.product.model.ProductsByStatusResponse
import com.example.greenbuyapp.data.product.model.CreateProductResponse
import com.example.greenbuyapp.data.product.model.CreateAttributeResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.example.greenbuyapp.domain.product.ProductRepository
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Multipart
import retrofit2.http.Part
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

    /**
     * Lấy product theo ID
     */
    @GET("api/product/{product_id}")
    suspend fun getProduct(
        @Path("product_id") productId: Int
    ): Product


    /**
     * Lấy product theo shopId
     */
    @GET("api/product/shop/{shop_id}")
    suspend fun getProductsByShopId(
        @Path("shop_id") shopId: Int,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("search") search: String? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null,
        @Query("approved_only") approvedOnly: Boolean? = null
    ): ProductListResponse

    /**
     * Lấy thống kê inventory của shop hiện tại
     */
    @GET("api/product/inventory-stats")
    suspend fun getInventoryStats(): InventoryStatsResponse

    /**
     * Lấy sản phẩm theo trạng thái
     */
    @GET("api/product/by-status/{status}")
    suspend fun getProductsByStatus(
        @Path("status") status: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null
    ): ProductsByStatusResponse

    /**
     * Tạo sản phẩm mới
     */
    @Multipart
    @POST("api/product/")
    suspend fun createProduct(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("sub_category_id") subCategoryId: RequestBody,
        @Part cover: MultipartBody.Part
    ): CreateProductResponse

    /**
     * Tạo attribute cho sản phẩm
     */
    @Multipart
    @POST("api/attribute")
    suspend fun createAttribute(
        @Part("product_id") productId: RequestBody,
        @Part("color") color: RequestBody,
        @Part("size") size: RequestBody,
        @Part("price") price: RequestBody,
        @Part("quantity") quantity: RequestBody,
        @Part image: MultipartBody.Part
    ): CreateAttributeResponse
}