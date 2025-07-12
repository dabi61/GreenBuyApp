package com.example.greenbuyapp.data.shop

import com.example.greenbuyapp.data.shop.model.AdminOrderResponse
import com.example.greenbuyapp.data.shop.model.OrderDetail
import com.example.greenbuyapp.data.shop.model.OrderStats
import com.example.greenbuyapp.data.shop.model.Shop
import com.example.greenbuyapp.data.shop.model.ShopOrderResponse
import com.example.greenbuyapp.data.shop.model.UpdateShopResponse
import com.example.greenbuyapp.data.user.model.UpdateUserProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ShopService {
    @GET("api/shops/{shop_id}")
    suspend fun getShopById(
        @Path("shop_id") shopId: Int
    ): Shop

    @GET("api/shops/me")
    suspend fun getMyShop(): Shop

    @GET("api/order/shop-stats")
    suspend fun getMyShopStats(): OrderStats

    /**
     * Lấy danh sách đơn hàng của shop
     */
    @GET("api/order/shop-orders")
    suspend fun getShopOrders(
        @Query("status_filter") statusFilter: Int? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): ShopOrderResponse

    /**
     * ✅ Lấy danh sách đơn hàng admin
     */
    @GET("api/order/admin/orders")
    suspend fun getAdminOrders(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("status") status: String? = null,
        @Query("payment_status") paymentStatus: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("customer_search") customerSearch: String? = null,
        @Query("order_number") orderNumber: String? = null,
        @Query("min_amount") minAmount: Double? = null,
        @Query("max_amount") maxAmount: Double? = null
    ): AdminOrderResponse

    /**
     * Lấy chi tiết đơn hàng theo ID
     */
    @GET("api/order/{order_id}")
    suspend fun getOrderDetail(
        @Path("order_id") orderId: Int
    ): OrderDetail

    /**
     * Tạo shop mới với multipart form-data
     * @param name Tên shop (text field)
     * @param phoneNumber Số điện thoại (text field)
     * @param isActive Trạng thái hoạt động (text field)
     * @param isOnline Trạng thái online (text field)
     * @param avatar File avatar (image upload)
     */
    @Multipart
    @POST("api/shops")
    suspend fun createShop(
        @Part("name") name: RequestBody,
        @Part("phone_number") phoneNumber: RequestBody,
        @Part("is_active") isActive: RequestBody,
        @Part("is_online") isOnline: RequestBody,
        @Part avatar: MultipartBody.Part
    ): Shop

    /**
     * Cập nhật thông tin shop
     */
    @Multipart
    @PUT("api/shops/me")
    suspend fun updateShop(
        @Part("name") name: RequestBody?,
        @Part("phone_number") phoneNumber: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("address") address: RequestBody?,
        @Part("is_active") isActive: RequestBody?,
        @Part("is_online") isOnline: RequestBody?,
        @Part avatar: MultipartBody.Part?
    ): UpdateShopResponse

    /**
     * Cập nhật trạng thái đơn hàng
     * @param orderId ID đơn hàng
     * @param status Trạng thái mới
     */
    @PATCH("api/order/{order_id}/status")
    suspend fun updateOrderStatus(
        @Path("order_id") orderId: Int,
        @Query("status") status: String
    ): OrderDetail
}