package com.example.greenbuyapp.data.shop.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Response từ API admin orders
 */
@JsonClass(generateAdapter = true)
data class AdminOrderResponse(
    @Json(name = "items") val items: List<AdminOrder>,
    @Json(name = "total") val total: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "limit") val limit: Int,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "has_next") val hasNext: Boolean,
    @Json(name = "has_prev") val hasPrev: Boolean
)

/**
 * Admin order item
 */
@JsonClass(generateAdapter = true)
data class AdminOrder(
    @Json(name = "id") val id: Int,
    @Json(name = "order_number") val orderNumber: String,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "customer_phone") val customerPhone: String,
    @Json(name = "status") val status: String,
    @Json(name = "total_amount") val totalAmount: Double,
    @Json(name = "payment_status") val paymentStatus: String,
    @Json(name = "payment_method") val paymentMethod: String?,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String
) {
    
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
     * Format updated_at để hiển thị
     */
    val formattedUpdatedAt: String
        get() = try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(updatedAt)
            if (date != null) outputFormat.format(date) else updatedAt
        } catch (e: Exception) {
            updatedAt
        }
    
    /**
     * Format total amount thành VNĐ
     */
    val formattedTotalAmount: String
        get() = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(totalAmount)
    
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
    
    /**
     * Get payment status display name
     */
    val paymentStatusDisplayName: String
        get() = when (paymentStatus) {
            "pending" -> "Chờ thanh toán"
            "paid" -> "Đã thanh toán"
            "failed" -> "Thanh toán thất bại"
            "refunded" -> "Đã hoàn tiền"
            else -> paymentStatus.replaceFirstChar { it.uppercase() }
        }
    
    /**
     * Get status color
     */
    fun getStatusColor(): Int {
        return when (status.lowercase()) {
            "pending" -> android.graphics.Color.parseColor("#FF9800") // Orange
            "confirmed" -> android.graphics.Color.parseColor("#2196F3") // Blue
            "processing" -> android.graphics.Color.parseColor("#9C27B0") // Purple
            "shipped" -> android.graphics.Color.parseColor("#00BCD4") // Cyan
            "delivered" -> android.graphics.Color.parseColor("#4CAF50") // Green
            "cancelled" -> android.graphics.Color.parseColor("#F44336") // Red
            "refunded" -> android.graphics.Color.parseColor("#795548") // Brown
            "returned" -> android.graphics.Color.parseColor("#607D8B") // Blue Grey
            else -> android.graphics.Color.parseColor("#757575") // Grey
        }
    }
    
    /**
     * Get payment status color
     */
    fun getPaymentStatusColor(): Int {
        return when (paymentStatus.lowercase()) {
            "pending" -> android.graphics.Color.parseColor("#FF9800") // Orange
            "paid" -> android.graphics.Color.parseColor("#4CAF50") // Green
            "failed" -> android.graphics.Color.parseColor("#F44336") // Red
            "refunded" -> android.graphics.Color.parseColor("#795548") // Brown
            else -> android.graphics.Color.parseColor("#757575") // Grey
        }
    }
} 