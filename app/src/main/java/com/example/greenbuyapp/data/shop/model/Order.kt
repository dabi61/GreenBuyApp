package com.example.greenbuyapp.data.shop.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ShopOrderResponse(
    val items: List<Order>,
    val stats: OrderStats,
    val total: Int,
    val page: Int,
    val limit: Int,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "has_next") val hasNext: Boolean,
    @Json(name = "has_prev") val hasPrev: Boolean
)

@JsonClass(generateAdapter = true)
data class Order(
    val id: Int,
    @Json(name = "order_number") val orderNumber: String,
    val status: String, // "pending", "confirmed", "processing", "shipped", "delivered", "cancelled", "refunded", "returned"
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "customer_phone") val customerPhone: String,
    @Json(name = "shipping_address") val shippingAddress: String,
    @Json(name = "total_items") val totalItems: Int,
    @Json(name = "total_amount") val totalAmount: Double,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "confirmed_at") val confirmedAt: String?,
    @Json(name = "shipped_at") val shippedAt: String?,
    @Json(name = "delivered_at") val deliveredAt: String?,
    @Json(name = "cancelled_at") val cancelledAt: String?,
    @Json(name = "shop_items") val shopItems: List<ShopItem>,
    @Json(name = "shop_subtotal") val shopSubtotal: Double
) {
    // Helper property để convert string status thành OrderStatus enum
    val orderStatus: OrderStatus
        get() = OrderStatus.fromApiStatus(status)

    // Helper property để format ngày tạo
    val formattedCreatedAt: String
        get() = formatDate(createdAt)

    private fun formatDate(dateString: String): String {
        return try {
            // Convert từ "2025-06-28T08:43:39.143597" thành "28/06/2025 08:43"
            val parts = dateString.split("T")
            val datePart = parts[0] // "2025-06-28"
            val timePart = parts[1].split(".")[0] // "08:43:39"
            
            val dateComponents = datePart.split("-")
            val timeComponents = timePart.split(":")
            
            "${dateComponents[2]}/${dateComponents[1]}/${dateComponents[0]} ${timeComponents[0]}:${timeComponents[1]}"
        } catch (e: Exception) {
            dateString
        }
    }
}

@JsonClass(generateAdapter = true)
data class ShopItem(
    @Json(name = "product_name") val productName: String,
    @Json(name = "attribute_name") val attributeName: String,
    val quantity: Int,
    val price: Double,
    val total: Double
)



enum class OrderStatus(val displayName: String, val apiStatus: String, val statusFilter: Int) {
    PENDING("Chờ xác nhận", "pending", 1),
    CONFIRMED("Chờ lấy hàng", "confirmed", 2),
    PROCESSING("Đang xử lý", "processing", 3),
    SHIPPING("Đang giao", "shipped", 4),
    DELIVERED("Đã giao", "delivered", 5),
    CANCELLED("Đơn hủy", "cancelled", 6),
    REFUNDED("Đã hoàn tiền", "refunded", 7),
    RETURNED("Đã trả hàng", "returned", 8);

    companion object {
        fun getAll() = listOf(PENDING, CONFIRMED, PROCESSING, SHIPPING, DELIVERED, CANCELLED, REFUNDED, RETURNED)
        
        fun fromPosition(position: Int): OrderStatus {
            return getAll().getOrNull(position) ?: PENDING
        }
        
        fun toPosition(status: OrderStatus): Int {
            return getAll().indexOf(status)
        }

        fun fromApiStatus(apiStatus: String): OrderStatus {
            return values().find { it.apiStatus == apiStatus } ?: PENDING
        }

        fun fromStatusFilter(statusFilter: Int): OrderStatus {
            return values().find { it.statusFilter == statusFilter } ?: PENDING
        }
    }
} 