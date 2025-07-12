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
     * Thêm sản phẩm vào giỏ hàng
     */
    suspend fun addToCart(attributeId: Int, quantity: Int): Result<AddToCartResponse> = withContext(Dispatchers.IO) {
        try {
            val request = AddToCartRequest(attributeId, quantity)
            val response = cartService.addToCart(request)
            if (response.isSuccessful) {
                val result = response.body()
                if (result != null) {
                    Result.Success(result)
                } else {
                    Result.Error(null, "Phản hồi rỗng từ server")
                }
            } else {
                Result.Error(response.code(), "Lỗi thêm vào giỏ hàng: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.NetworkError
        }
    }
    
    /**
     * Thêm sản phẩm vào giỏ hàng với AddToCartRequest
     */
    suspend fun addToCart(request: AddToCartRequest): Result<AddToCartResponse> = withContext(Dispatchers.IO) {
        try {
            val response = cartService.addToCart(request)
            if (response.isSuccessful) {
                val result = response.body()
                if (result != null) {
                    Result.Success(result)
                } else {
                    Result.Error(null, "Phản hồi rỗng từ server")
                }
            } else {
                Result.Error(response.code(), "Lỗi thêm vào giỏ hàng: ${response.message()}")
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

    /**
     * Validate dữ liệu order trước khi gửi request
     */
    private fun validateOrderData(
        cartShop: CartShop,
        shippingAddress: String,
        phoneNumber: String,
        recipientName: String,
        billingAddress: String
    ): String? {
        return when {
            cartShop.items.isEmpty() -> "Giỏ hàng trống"
            shippingAddress.isBlank() -> "Địa chỉ giao hàng không được để trống"
            shippingAddress.length < 10 -> "Địa chỉ giao hàng quá ngắn (cần ít nhất 10 ký tự)"
            shippingAddress.matches(Regex("^[a-zA-Z]*$")) -> "Địa chỉ giao hàng không hợp lệ - vui lòng nhập địa chỉ thực"
            phoneNumber.isBlank() -> "Số điện thoại không được để trống"
            !phoneNumber.matches(Regex("^[0-9+\\-\\s()]+$")) -> "Số điện thoại không hợp lệ"
            recipientName.isBlank() -> "Tên người nhận không được để trống"
            recipientName.length < 2 -> "Tên người nhận quá ngắn"
            billingAddress.isBlank() -> "Địa chỉ thanh toán không được để trống"
            billingAddress.length < 10 -> "Địa chỉ thanh toán quá ngắn (cần ít nhất 10 ký tự)"
            billingAddress.matches(Regex("^[a-zA-Z]*$")) -> "Địa chỉ thanh toán không hợp lệ - vui lòng nhập địa chỉ thực"
            cartShop.items.any { it.quantity <= 0 } -> "Số lượng sản phẩm phải lớn hơn 0"
            cartShop.items.any { it.productId <= 0 } -> "ID sản phẩm không hợp lệ"
            cartShop.items.any { it.attributeId <= 0 } -> "ID thuộc tính không hợp lệ"
            else -> null
        }
    }

suspend fun placeOrderAndPay(
    cartShop: CartShop,
    shippingAddress: String,
    phoneNumber: String,
    recipientName: String,
    deliveryNotes: String,
    billingAddress: String
): Result<PaymentResponse> = withContext(Dispatchers.IO) {
    try {
        // Validate dữ liệu trước khi gửi request
        val validationError = validateOrderData(cartShop, shippingAddress, phoneNumber, recipientName, billingAddress)
        if (validationError != null) {
            println("❌ Validation error: $validationError")
            return@withContext Result.Error(400, validationError)
        }

        // 1. Tạo order
        val orderItems = cartShop.items.map {
            OrderItemRequest(
                product_id = it.productId,
                attribute_id = it.attributeId,
                quantity = it.quantity
            )
        }
        val orderRequest = OrderRequest(
            items = orderItems,
            shipping_address = shippingAddress,
            phone_number = phoneNumber,
            recipient_name = recipientName,
            delivery_notes = deliveryNotes,
            billing_address = billingAddress
        )
        
        println("📤 Creating order with request: $orderRequest")
        val orderResponse = cartService.createOrder(orderRequest)
        println("✅ Order created successfully with ID: ${orderResponse.id}")

        // 2. Thanh toán
        val paymentRequest = PaymentRequest() // giữ nguyên như bạn yêu cầu
        val paymentResponse = cartService.processPayment(orderResponse.id, paymentRequest)
        println("✅ Payment processed successfully")

        Result.Success(paymentResponse)
    } catch (e: retrofit2.HttpException) {
        val errorCode = e.code()
        val errorBody = try {
            e.response()?.errorBody()?.string()
        } catch (ex: Exception) {
            null
        }
        
        println("❌ HTTP Error $errorCode: $errorBody")
        
        val errorMessage = when (errorCode) {
            422 -> {
                if (errorBody != null) {
                    "Dữ liệu không hợp lệ: $errorBody"
                } else {
                    "Dữ liệu không hợp lệ - Vui lòng kiểm tra thông tin đơn hàng"
                }
            }
            400 -> "Yêu cầu không hợp lệ"
            401 -> "Vui lòng đăng nhập lại"
            403 -> "Không có quyền thực hiện"
            404 -> "Sản phẩm không tồn tại"
            500 -> "Lỗi server, vui lòng thử lại sau"
            else -> errorBody ?: "Lỗi tạo đơn hàng: ${e.message}"
        }
        
        Result.Error(errorCode, errorMessage)
    } catch (e: java.io.IOException) {
        println("❌ Network Error: ${e.message}")
        Result.NetworkError
    } catch (e: Exception) {
        println("❌ Unknown Error: ${e.message}")
        Result.Error(null, "Lỗi không xác định: ${e.message}")
    }
}
} 