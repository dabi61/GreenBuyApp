package com.example.greenbuyapp.domain.user

import com.example.greenbuyapp.data.search.SearchService
import com.example.greenbuyapp.data.user.UserService
import com.example.greenbuyapp.data.user.model.AddressResponse
import com.example.greenbuyapp.data.user.model.CustomerOrderDetail
import com.example.greenbuyapp.data.user.model.CustomerOrderResponse
import com.example.greenbuyapp.data.user.model.User
import com.example.greenbuyapp.data.user.model.UserMeResponse
import com.example.greenbuyapp.domain.Listing
import com.example.greenbuyapp.util.Result
import com.example.greenbuyapp.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class UserRepository(
    private val userService: UserService,
    private val searchService: SearchService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {


    suspend fun getUserMe() =
        safeApiCall(dispatcher) { userService.getUserMe() }

    fun searchUsers(
        query: String,
        scope: CoroutineScope
    ): Listing<User> {
        return SearchUserDataSourceFactory(searchService, query, scope).createListing()
    }
    
    /**
     * Lấy đơn hàng của khách hàng
     */
    suspend fun getCustomerOrders(
        statusFilter: Int,
        page: Int = 1,
        limit: Int = 10
    ): Result<CustomerOrderResponse> {
        return safeApiCall(dispatcher) {
            userService.getCustomerOrders(statusFilter, page, limit)
        }
    }
    
    /**
     * Lấy chi tiết đơn hàng của khách hàng
     */
    suspend fun getCustomerOrderDetail(orderId: Int): Result<CustomerOrderDetail> {
        return safeApiCall(dispatcher) {
            userService.getCustomerOrderDetail(orderId)
        }
    }
    suspend fun getListAddress(): Result<List<AddressResponse>> {
        return safeApiCall(dispatcher) {
            userService.getAddresses()
        }
    }
}