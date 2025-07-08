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
import com.example.greenbuyapp.data.user.model.AddressAddRequest
import com.example.greenbuyapp.data.user.model.AddressDetailResponse
import com.example.greenbuyapp.data.user.model.AddressUpdateRequest



class UserRepository(
    private val userService: UserService,
    private val searchService: SearchService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {


    suspend fun getUserMe() =
        safeApiCall(dispatcher) { userService.getUserMe() }

//    suspend fun getMe() =
//        safeApiCall(dispatcher) { userService.getMe() }

    suspend fun createAddress(
        street: String,
        city: String,
        state: String,
        zipcode: String,
        country: String,
        phone: String
    ): Result<AddressResponse> {
        return try {
            val request = AddressAddRequest(
                street = street,
                city = city,
                state = state,
                zipcode = zipcode,
                country = country,
                phone = phone
            )
            val response = userService.addAddress(request)
            Result.Success(response)
        } catch (e: Exception) {
            Result.Error(null,e.message ?: "Unknown")
        }
    }

    suspend fun getAddressById(id: Int): Result<AddressDetailResponse> {
        return safeApiCall(dispatcher) {

            userService.getAddressDetail(id)
        }
    }

    suspend fun updateAddress(
        id: Int,
        request: AddressUpdateRequest
    ): Result<AddressDetailResponse> {
        return try {
            val response = userService.updateAddress(id, request)
            Result.Success(response)
        } catch (e: Exception) {
            Result.Error(null, e.message ?: "Unknown")
        }
    }





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