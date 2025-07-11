package com.example.greenbuyapp.domain.approve

import com.example.greenbuyapp.data.shop.ShopService
import com.example.greenbuyapp.data.user.UserService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class ApproveRepository(
    private val shopService: ShopService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
}