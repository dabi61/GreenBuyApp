package com.example.greenbuyapp.data.user

import com.example.greenbuyapp.data.shop.model.Shop
import com.example.greenbuyapp.data.user.model.AddressAddRequest
import com.example.greenbuyapp.data.user.model.AddressDetailResponse
import com.example.greenbuyapp.data.user.model.AddressResponse
import com.example.greenbuyapp.data.user.model.AddressUpdateRequest
import com.example.greenbuyapp.data.user.model.ChangeRoleRequest
import com.example.greenbuyapp.data.user.model.CustomerOrderDetail
import com.example.greenbuyapp.data.user.model.CustomerOrderResponse
import com.example.greenbuyapp.data.user.model.Me
import com.example.greenbuyapp.data.user.model.User
import com.example.greenbuyapp.data.user.model.UserMe
import com.example.greenbuyapp.data.user.model.UserMeResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface UserService {
    @GET("api/user/me")
    suspend fun getUserMe(): UserMeResponse

    @GET("api/user/me")
    suspend fun getMe(): UserMe

    @PATCH("api/user/me/change-role")
    suspend fun changeRole(
        @Body request: ChangeRoleRequest
    ): UserMeResponse
    
    @GET("api/order/")
    suspend fun getCustomerOrders(
        @Query("status_filter") statusFilter: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): CustomerOrderResponse
    
    @GET("api/order/{orderId}")
    suspend fun getCustomerOrderDetail(
        @Path("orderId") orderId: Int
    ): CustomerOrderDetail

    @GET("api/addresses/")
    suspend fun getAddresses(): List<AddressResponse>

    @POST("api/addresses/")
    suspend fun addAddress(
        @Body request: AddressAddRequest
    ): AddressResponse

    @GET("api/addresses/{id}")
    suspend fun getAddressDetail(
        @Path("id") addressId: Int
    ): AddressDetailResponse

    @PUT("api/addresses/{id}")
    suspend fun updateAddress(
        @Path("id") addressId: Int,
        @Body request: AddressUpdateRequest
    ): AddressDetailResponse
}
