package com.example.greenbuyapp.domain.user

import com.example.greenbuyapp.data.search.SearchService
import com.example.greenbuyapp.data.user.UserService
import com.example.greenbuyapp.data.user.model.User
import com.example.greenbuyapp.domain.Listing
import com.example.greenbuyapp.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class UserRepository(
    private val userService: UserService,
    private val searchService: SearchService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun getUserPublicProfile(username: String) =
        safeApiCall(dispatcher) { userService.getUserPublicProfile(username) }

    fun searchUsers(
        query: String,
        scope: CoroutineScope
    ): Listing<User> {
        return SearchUserDataSourceFactory(searchService, query, scope).createListing()
    }
}