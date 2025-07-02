package com.example.greenbuyapp.data.user.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
@JsonClass(generateAdapter = true)
data class CustomerOrder(
    @Json(name = "id") val id: Int,
    @Json(name = "order_number") val orderNumber: String,
    @Json(name = "status") val status: String,
    @Json(name = "total_items") val totalItems: Int,
    @Json(name = "total_amount") val totalAmount: Double,
    @Json(name = "created_at") val createdAt: String
) : Parcelable {
    
    /**
     * Format created_at để hiển thị
     */
    val formattedCreatedAt: String
        get() = try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(createdAt)
            if (date != null) outputFormat.format(date) else createdAt
        } catch (e: Exception) {
            createdAt
        }
    
    /**
     * Get status display name
     */
    val statusDisplayName: String
        get() = when (status) {
            "pending" -> "Chờ xác nhận"
            "confirmed" -> "Đã xác nhận"
            "processing" -> "Đang xử lý"
            "shipped" -> "Đang giao"
            "delivered" -> "Đã giao"
            "cancelled" -> "Đã hủy"
            "refunded" -> "Đã hoàn tiền"
            "returned" -> "Đã trả hàng"
            else -> status.replaceFirstChar { it.uppercase() }
        }
}

@Parcelize
@JsonClass(generateAdapter = true)
data class CustomerOrderResponse(
    @Json(name = "items") val items: List<CustomerOrder>,
    @Json(name = "total") val total: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "limit") val limit: Int,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "has_next") val hasNext: Boolean,
    @Json(name = "has_prev") val hasPrev: Boolean
) : Parcelable 