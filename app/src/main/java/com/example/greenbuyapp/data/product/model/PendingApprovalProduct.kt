package com.example.greenbuyapp.data.product.model

import com.squareup.moshi.JsonClass
import java.text.NumberFormat
import java.util.Locale

@JsonClass(generateAdapter = true)
data class PendingApprovalProduct(
    val shop_id: Int,
    val sub_category_id: Int,
    val name: String,
    val description: String,
    val cover: String?,
    val price: Double,
    val product_id: Int,
    val approved_by: Int?,
    val create_at: String,
    // ✅ Thêm thông tin shop (sẽ được load riêng)
    var shopInfo: com.example.greenbuyapp.data.shop.model.Shop? = null
) {
    fun getFormattedPrice(): String {
        return try {
            val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
            "${formatter.format(price)} VNĐ"
        } catch (e: Exception) {
            "${price.toLong()} VNĐ"
        }
    }

    // Để tương thích với Product model hiện tại
    val product_name: String get() = name
    val isApproved: Boolean get() = approved_by != null
} 