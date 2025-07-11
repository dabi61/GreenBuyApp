package com.example.greenbuyapp.data.cart

import com.example.greenbuyapp.data.cart.model.*
import retrofit2.Response
import retrofit2.http.*

interface CartService {
    
    /**
     * Lấy danh sách giỏ hàng
     */
    @GET("api/cart/me")
    suspend fun getCart(): Response<List<CartShop>>
    
    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    @POST("api/cart/add")
    suspend fun addToCart(
        @Body request: AddToCartRequest
    ): Response<AddToCartResponse>
    
    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     */
    @PUT("api/cart/attribute/{attribute_id}")
    suspend fun updateCartItem(
        @Path("attribute_id") attributeId: Int,
        @Body request: UpdateCartRequest
    ): Response<UpdateCartResponse>
    
    /**
     * Xóa toàn bộ sản phẩm của shop
     */
    @DELETE("api/cart/shop/{shop_id}")
    suspend fun deleteShopFromCart(
        @Path("shop_id") shopId: Int
    ): Response<DeleteCartShopResponse>
    
    /**
     * Xóa một sản phẩm khỏi giỏ hàng
     */
    @DELETE("api/cart/attribute/{attribute_id}")
    suspend fun deleteCartItem(
        @Path("attribute_id") attributeId: Int
    ): Response<DeleteCartItemResponse>

    @POST("api/order/")
    suspend fun createOrder(@Body request: OrderRequest): OrderResponse

    @POST("api/payment/process/{order_id}")
    suspend fun processPayment(
        @Path("order_id") orderId: Int,
        @Body request: PaymentRequest
    ): PaymentResponse
} 