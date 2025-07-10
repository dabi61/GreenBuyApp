package com.example.greenbuyapp.data.approve

import com.example.greenbuyapp.data.shop.model.Shop
import org.intellij.lang.annotations.Pattern
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface ApproveService {
    @PATCH("api/shops/{shop_id}")
    suspend fun getShopById(
        @Path("shop_id") shopId: Int
    ): Shop
}