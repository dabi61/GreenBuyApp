package com.example.greenbuyapp.data.cart.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.text.NumberFormat
import java.util.Locale

@Parcelize
@JsonClass(generateAdapter = true)
data class CartShop(
    @Json(name = "shop_id")
    val shopId: Int,
    
    @Json(name = "shop_name")
    val shopName: String,
    
    @Json(name = "items")
    val items: List<CartItem>
) : Parcelable {
    /**
     * Tính tổng tiền của shop
     */
    fun getTotalAmount(): Double {
        return items.sumOf { it.getTotalPrice() }
    }

    /**
     * Tính tổng tiền của shop
     */
    fun getTotalPriceAndPee(): Double {
        return items.sumOf { it.getTotalPrice() } + 45000
    }

    /**
     * Format tổng tiền và ship của shop
     */
    fun getFormattedTotalAndPeeAmount(): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(getTotalPriceAndPee())
    }

    
    /**
     * Format tổng tiền của shop
     */
    fun getFormattedTotalAmount(): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(getTotalAmount())
    }
    
    /**
     * Tính tổng số lượng sản phẩm
     */
    fun getTotalQuantity(): Int {
        return items.sumOf { it.quantity }
    }
    
    /**
     * Kiểm tra shop có sản phẩm không
     */
    fun hasItems(): Boolean {
        return items.isNotEmpty()
    }
} 