package com.example.greenbuyapp.domain.cart

import com.example.greenbuyapp.data.cart.CartService
import com.example.greenbuyapp.data.cart.model.*
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CartRepository(
    private val cartService: CartService
) {
    
    /**
     * Lấy danh sách giỏ hàng
     */
    suspend fun getCart(): Result<List<CartShop>> = withContext(Dispatchers.IO) {
        try {
            val response = cartService.getCart()
            if (response.isSuccessful) {
                val cartShops = response.body() ?: emptyList()
                Result.Success(cartShops)
            } else {
                Result.Error(response.code(), "Lỗi lấy giỏ hàng: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.NetworkError
        }
    }
    
    /**
     * Cập nhật số lượng sản phẩm
     */
    suspend fun updateCartItem(attributeId: Int, quantity: Int): Result<UpdateCartResponse> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateCartRequest(quantity)
            val response = cartService.updateCartItem(attributeId, request)
            if (response.isSuccessful) {
                val result = response.body()
                if (result != null) {
                    Result.Success(result)
                } else {
                    Result.Error(null, "Phản hồi rỗng từ server")
                }
            } else {
                Result.Error(response.code(), "Lỗi cập nhật giỏ hàng: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.NetworkError
        }
    }
    
    /**
     * Xóa toàn bộ shop khỏi giỏ hàng
     */
    suspend fun deleteShopFromCart(shopId: Int): Result<DeleteCartShopResponse> = withContext(Dispatchers.IO) {
        try {
            val response = cartService.deleteShopFromCart(shopId)
            if (response.isSuccessful) {
                val result = response.body()
                if (result != null) {
                    Result.Success(result)
                } else {
                    Result.Error(null, "Phản hồi rỗng từ server")
                }
            } else {
                Result.Error(response.code(), "Lỗi xóa shop: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.NetworkError
        }
    }
    
    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    suspend fun deleteCartItem(attributeId: Int): Result<DeleteCartItemResponse> = withContext(Dispatchers.IO) {
        try {
            val response = cartService.deleteCartItem(attributeId)
            if (response.isSuccessful) {
                val result = response.body()
                if (result != null) {
                    Result.Success(result)
                } else {
                    Result.Error(null, "Phản hồi rỗng từ server")
                }
            } else {
                Result.Error(response.code(), "Lỗi xóa sản phẩm: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.NetworkError
        }
    }
} 