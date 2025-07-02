package com.example.greenbuyapp.data.cart.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class DeleteCartShopResponse(
    @Json(name = "message")
    val message: String,
    
    @Json(name = "shop_id")
    val shopId: Int,
    
    @Json(name = "shop_name")
    val shopName: String,
    
    @Json(name = "deleted_count")
    val deletedCount: Int
) : Parcelable 