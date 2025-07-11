package com.example.greenbuyapp.data.product

import com.example.greenbuyapp.data.MessageResponse
import com.example.greenbuyapp.data.product.model.Product
import com.example.greenbuyapp.data.product.model.ProductAttribute
import com.example.greenbuyapp.data.product.model.ProductAttributeList
import com.example.greenbuyapp.data.product.model.ProductListResponse
import com.example.greenbuyapp.data.product.model.TrendingProduct
import com.example.greenbuyapp.data.product.model.TrendingProductResponse
import com.example.greenbuyapp.data.product.model.shopProducts
import com.example.greenbuyapp.data.product.model.InventoryStatsResponse
import com.example.greenbuyapp.data.product.model.ProductsByStatusResponse
import com.example.greenbuyapp.data.product.model.CreateProductResponse
import com.example.greenbuyapp.data.product.model.CreateAttributeResponse
import com.example.greenbuyapp.data.product.model.ApproveProductRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.example.greenbuyapp.domain.product.ProductRepository
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.PATCH

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
     * ✅ Lấy attribute theo ID
     */
    @GET("api/attribute/{attribute_id}")
    suspend fun getAttribute(
        @Path("attribute_id") attributeId: Int
    ): ProductAttribute

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
     * Chỉnh sửa sản phẩm
     */
    @Multipart
    @PUT("api/product/{product_id}")
    suspend fun editProduct(
        @Path("product_id") productId: Int,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("sub_category_id") subCategoryId: RequestBody,
        @Part cover: MultipartBody.Part
    ): CreateProductResponse

    /**
     * ✅ Chỉnh sửa sản phẩm chỉ text fields (không có ảnh mới)
     */
    @Multipart
    @PUT("api/product/{product_id}")
    suspend fun editProductText(
        @Path("product_id") productId: Int,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("sub_category_id") subCategoryId: RequestBody,
        @Part cover: MultipartBody.Part?
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

    /**
     * ✅ Chỉnh sửa attribute của sản phẩm
     */
    @Multipart
    @PUT("api/attribute/{attribute_id}")
    suspend fun editAttribute(
        @Path("attribute_id") attributeId: Int,
        @Part("product_id") productId: RequestBody,
        @Part("color") color: RequestBody,
        @Part("size") size: RequestBody,
        @Part("price") price: RequestBody,
        @Part("quantity") quantity: RequestBody,
        @Part image: MultipartBody.Part
    ): CreateAttributeResponse

    /**
     * ✅ Chỉnh sửa attribute chỉ text fields (không có ảnh mới)
     */
    @Multipart
    @PUT("api/attribute/{attribute_id}")
    suspend fun editAttributeText(
        @Path("attribute_id") attributeId: Int,
        @Part("product_id") productId: RequestBody,
        @Part("color") color: RequestBody,
        @Part("size") size: RequestBody,
        @Part("price") price: RequestBody,
        @Part("quantity") quantity: RequestBody,
        @Part image: MultipartBody.Part?
    ): CreateAttributeResponse

    @DELETE("api/attribute/{attribute_id}")
    suspend fun deleteAttribute(
        @Path("attribute_id") attributeId: Int
    ): MessageResponse

    /**
     * ✅ Duyệt sản phẩm (approve)
     */
    @PATCH("api/product/{product_id}/approve")
    suspend fun approveProduct(
        @Path("product_id") productId: Int,
        @Body requestBody: ApproveProductRequest
    ): Product

    /**
     * ✅ Từ chối sản phẩm (reject)
     */
    @PATCH("api/product/{product_id}/approve")
    suspend fun rejectProduct(
        @Path("product_id") productId: Int,
        @Body requestBody: ApproveProductRequest
    ): Product

}