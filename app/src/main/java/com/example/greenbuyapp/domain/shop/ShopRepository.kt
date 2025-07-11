package com.example.greenbuyapp.domain.shop

import android.content.Context
import android.net.Uri
import com.example.greenbuyapp.data.product.model.Product
import com.example.greenbuyapp.data.shop.ShopService
import com.example.greenbuyapp.data.shop.model.AdminOrderDetail
import com.example.greenbuyapp.data.shop.model.OrderDetail
import com.example.greenbuyapp.data.shop.model.OrderStats
import com.example.greenbuyapp.data.shop.model.Shop
import com.example.greenbuyapp.data.shop.model.ShopOrderResponse
import com.example.greenbuyapp.data.shop.model.UpdateOrderStatusRequest
import com.example.greenbuyapp.data.shop.model.UpdateShopResponse
import com.example.greenbuyapp.data.user.UserService
import com.example.greenbuyapp.data.user.model.ChangeRoleRequest
import com.example.greenbuyapp.data.user.model.UserMeResponse
import com.example.greenbuyapp.util.MultipartUtils
import com.example.greenbuyapp.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import com.example.greenbuyapp.util.Result
import java.io.File


class ShopRepository(
    private val shopService: ShopService,
    private val userService: UserService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun getShop(shopId: Int) : Result<Shop> {
        return safeApiCall(dispatcher) {
            shopService.getShopById(shopId)
        }
    }

    suspend fun getUserMe() : Result<UserMeResponse> {
        return safeApiCall(dispatcher) {
            userService.getUserMe()
        }
    }

    suspend fun getMyShop() : Result<Shop> {
        return safeApiCall(dispatcher) {
            shopService.getMyShop()
        }
    }

    suspend fun changeRole(newRole: String) : Result<UserMeResponse> {
        println("📡 ShopRepository.changeRole called with: '$newRole'")
        val request = ChangeRoleRequest(newRole)
        println("📋 Created request object: $request")
        
        return safeApiCall(dispatcher) {
            println("🔗 About to call UserService.changeRole API")
            userService.changeRole(request)
        }
    }

    /**
     * Tạo shop mới với avatar file
     */
    suspend fun createShop(
        context: Context,
        name: String,
        phoneNumber: String,
        avatarFile: File?
    ): Result<Shop> {
        return safeApiCall(dispatcher) {
            val namePart = MultipartUtils.createTextPart(name)
            val phonePart = MultipartUtils.createTextPart(phoneNumber)
            val isActivePart = MultipartUtils.createTextPart("true")  // ✅ Default active
            val isOnlinePart = MultipartUtils.createTextPart("true")  // ✅ Default online
            val avatarPart = avatarFile?.let { 
                MultipartUtils.createImagePart("avatar", it) 
            } ?: MultipartUtils.createDummyImagePart(context, "avatar")
            
            shopService.createShop(
                name = namePart,
                phoneNumber = phonePart,
                isActive = isActivePart,
                isOnline = isOnlinePart,
                avatar = avatarPart
            )
        }
    }

    /**
     * Tạo shop mới với avatar Uri
     */
    suspend fun createShop(
        context: Context,
        name: String,
        phoneNumber: String,
        avatarUri: Uri?
    ): Result<Shop> {
        return safeApiCall(dispatcher) {
            val namePart = MultipartUtils.createTextPart(name)
            val phonePart = MultipartUtils.createTextPart(phoneNumber)
            val isActivePart = MultipartUtils.createTextPart("true")  // ✅ Default active
            val isOnlinePart = MultipartUtils.createTextPart("true")  // ✅ Default online
            val avatarPart = avatarUri?.let { 
                MultipartUtils.createImagePart(context, "avatar", it) 
            } ?: MultipartUtils.createDummyImagePart(context, "avatar")
            
            shopService.createShop(
                name = namePart,
                phoneNumber = phonePart,
                isActive = isActivePart,
                isOnline = isOnlinePart,
                avatar = avatarPart
            )
        }
    }

    suspend fun getMyShopStats() : Result<OrderStats> {
        return safeApiCall(dispatcher) {
            shopService.getMyShopStats()
        }
    }

    suspend fun getShopOrders(
        statusFilter: Int? = null,
        page: Int = 1,
        limit: Int = 10,
        dateFrom: String? = null,
        dateTo: String? = null
    ) : Result<ShopOrderResponse> {
        return safeApiCall(dispatcher) {
            shopService.getShopOrders(statusFilter, page, limit, dateFrom, dateTo)
        }
    }

    /**
     * Lấy chi tiết đơn hàng theo ID
     */
    suspend fun getOrderDetail(orderId: Int): Result<OrderDetail> {
        return safeApiCall(dispatcher) {
            shopService.getOrderDetail(orderId)
        }
    }

    /**
     * Cập nhật shop của tôi (gửi lên name, phone_number, is_active, is_online, avatar)
     */
    suspend fun updateMyShop(
        context: Context,
        avatarUri: Uri?,
        name: String,
        phoneNumber: String,
        description: String? = null,
        address: String? = null,
        isActive: Boolean = true,
        isOnline: Boolean = true
    ): Result<UpdateShopResponse> {
        return safeApiCall(dispatcher) {
            val namePart = MultipartUtils.createTextPart(name)
            val phonePart = MultipartUtils.createTextPart(phoneNumber)
            val descriptionPart = description?.let { MultipartUtils.createTextPart(it) }
            val addressPart = address?.let { MultipartUtils.createTextPart(it) }
            val isActivePart = MultipartUtils.createTextPart(isActive.toString())
            val isOnlinePart = MultipartUtils.createTextPart(isOnline.toString())
            val avatarPart = avatarUri?.let {
                MultipartUtils.createImagePart(context, "avatar", it)
            }

            shopService.updateShop(
                name = namePart,
                phoneNumber = phonePart,
                description = descriptionPart,
                address = addressPart,
                isActive = isActivePart,
                isOnline = isOnlinePart,
                avatar = avatarPart
            )
        }
    }

    /**
     * ✅ Lấy danh sách đơn hàng admin với pagination
     */
    suspend fun getAdminOrders(
        page: Int = 1,
        limit: Int = 10,
        status: String? = null,
        paymentStatus: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        customerSearch: String? = null,
        orderNumber: String? = null,
        minAmount: Double? = null,
        maxAmount: Double? = null
    ): Result<com.example.greenbuyapp.data.shop.model.AdminOrderResponse> {
        return safeApiCall(dispatcher) {
            shopService.getAdminOrders(
                page = page,
                limit = limit,
                status = status,
                paymentStatus = paymentStatus,
                dateFrom = dateFrom,
                dateTo = dateTo,
                customerSearch = customerSearch,
                orderNumber = orderNumber,
                minAmount = minAmount,
                maxAmount = maxAmount
            )
        }
    }

    /**
     * ✅ Cập nhật trạng thái đơn hàng
     */
    suspend fun updateOrderStatus(
        orderId: Int,
        status: String
    ): Result<com.example.greenbuyapp.data.shop.model.OrderDetail> {
        return safeApiCall(dispatcher) {
            shopService.updateOrderStatus(orderId, status)
        }
    }

    /**
     * ✅ Lấy chi tiết đơn hàng admin
     */
    suspend fun getAdminOrderDetail(orderId: Int): Result<AdminOrderDetail> {
        return safeApiCall(dispatcher) {
            shopService.getAdminOrderDetail(orderId)
        }
    }

    /**
     * ✅ Cập nhật trạng thái đơn hàng admin
     */
    suspend fun updateAdminOrderStatus(
        orderId: Int,
        status: Int,
        internalNotes: String? = null,
        notifyCustomer: Boolean = true
    ): Result<AdminOrderDetail> {
        return safeApiCall(dispatcher) {
            val request = UpdateOrderStatusRequest(
                status = status,
                internalNotes = internalNotes,
                notifyCustomer = notifyCustomer
            )
            shopService.updateAdminOrderStatus(orderId, request)
        }
    }

}