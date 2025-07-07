package com.example.greenbuyapp.data.user

import com.example.greenbuyapp.data.user.model.ChangeRoleRequest
import com.example.greenbuyapp.data.user.model.CustomerOrderDetail
import com.example.greenbuyapp.data.user.model.CustomerOrderResponse
import com.example.greenbuyapp.data.user.model.Me
import com.example.greenbuyapp.data.user.model.User
import com.example.greenbuyapp.data.user.model.AddressResponse
import com.example.greenbuyapp.data.user.model.UserMeResponse
import retrofit2.http.*

interface UserService {
    @GET("api/user/me")
    suspend fun getUserMe(): UserMeResponse

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
    suspend fun getAddress(
    ): AddressResponse
}
