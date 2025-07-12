package com.example.greenbuyapp.data.shop.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Chi tiết đơn hàng admin từ API
 */
@JsonClass(generateAdapter = true)
data class AdminOrderDetail(
    @Json(name = "id") val id: Int,
    @Json(name = "order_number") val orderNumber: String,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "customer_email") val customerEmail: String,
    @Json(name = "customer_phone") val customerPhone: String,
    @Json(name = "status") val status: String,
    @Json(name = "subtotal") val subtotal: Double,
    @Json(name = "tax_amount") val taxAmount: Double,
    @Json(name = "shipping_fee") val shippingFee: Double,
    @Json(name = "discount_amount") val discountAmount: Double,
    @Json(name = "total_amount") val totalAmount: Double,
    @Json(name = "shipping_address") val shippingAddress: String,
    @Json(name = "billing_address") val billingAddress: String?,
    @Json(name = "delivery_notes") val deliveryNotes: String,
    @Json(name = "payment_status") val paymentStatus: String?,
    @Json(name = "payment_method") val paymentMethod: String?,
    @Json(name = "payment_amount") val paymentAmount: Double?,
    @Json(name = "transaction_id") val transactionId: String?,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "confirmed_at") val confirmedAt: String?,
    @Json(name = "shipped_at") val shippedAt: String?,
    @Json(name = "delivered_at") val deliveredAt: String?,
    @Json(name = "cancelled_at") val cancelledAt: String?,
    @Json(name = "items") val items: List<AdminOrderItem>,
    @Json(name = "notes") val notes: String?,
    @Json(name = "internal_notes") val internalNotes: String?
) {
    
    /**
     * Format created_at để hiển thị
     */
    val formattedCreatedAt: String
        get() = formatDateTime(createdAt)
    
    /**
     * Format updated_at để hiển thị
     */
    val formattedUpdatedAt: String
        get() = formatDateTime(updatedAt)
    
    /**
     * Format confirmed_at để hiển thị
     */
    val formattedConfirmedAt: String?
        get() = confirmedAt?.let { formatDateTime(it) }
    
    /**
     * Format shipped_at để hiển thị
     */
    val formattedShippedAt: String?
        get() = shippedAt?.let { formatDateTime(it) }
    
    /**
     * Format delivered_at để hiển thị
     */
    val formattedDeliveredAt: String?
        get() = deliveredAt?.let { formatDateTime(it) }
    
    /**
     * Format cancelled_at để hiển thị
     */
    val formattedCancelledAt: String?
        get() = cancelledAt?.let { formatDateTime(it) }
    
    private fun formatDateTime(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            if (date != null) outputFormat.format(date) else dateString
        } catch (e: Exception) {
            dateString
        }
    }
    
    /**
     * Format subtotal thành VNĐ
     */
    val formattedSubtotal: String
        get() = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(subtotal)
    
    /**
     * Format tax amount thành VNĐ
     */
    val formattedTaxAmount: String
        get() = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(taxAmount)
    
    /**
     * Format shipping fee thành VNĐ
     */
    val formattedShippingFee: String
        get() = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(shippingFee)
    
    /**
     * Format discount amount thành VNĐ
     */
    val formattedDiscountAmount: String
        get() = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(discountAmount)
    
    /**
     * Format total amount thành VNĐ
     */
    val formattedTotalAmount: String
        get() = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(totalAmount)
    
    /**
     * Format payment amount thành VNĐ
     */
    val formattedPaymentAmount: String
        get() = paymentAmount?.let { NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(it) } ?: "0 ₫"
    
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
        get() = when (paymentStatus?.lowercase()) {
            "pending" -> "Chờ thanh toán"
            "paid" -> "Đã thanh toán"
            "completed" -> "Hoàn thành"
            "failed" -> "Thanh toán thất bại"
            "refunded" -> "Đã hoàn tiền"
            else -> if (paymentStatus != null) {
                paymentStatus.replaceFirstChar { it.uppercase() }
            } else {
                "Chưa thanh toán"
            }
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
        return when (paymentStatus?.lowercase()) {
            "pending" -> android.graphics.Color.parseColor("#FF9800") // Orange
            "paid", "completed" -> android.graphics.Color.parseColor("#4CAF50") // Green
            "failed" -> android.graphics.Color.parseColor("#F44336") // Red
            "refunded" -> android.graphics.Color.parseColor("#795548") // Brown
            else -> android.graphics.Color.parseColor("#757575") // Grey
        }
    }
    
    /**
     * Get total items count
     */
    val totalItems: Int
        get() = items.sumOf { it.quantity }
    
    /**
     * Get next status number for API
     */
    fun getNextStatusNumber(): Int {
        return when (status.lowercase()) {
            "pending" -> 2 // confirmed
            "confirmed" -> 3 // processing
            "processing" -> 4 // shipped
            "shipped" -> 5 // delivered
            else -> 1 // pending
        }
    }
    
    /**
     * Get next status display name
     */
    fun getNextStatusDisplayName(): String {
        return when (status.lowercase()) {
            "pending" -> "Xác nhận đơn hàng"
            "confirmed" -> "Bắt đầu xử lý"
            "processing" -> "Giao hàng"
            "shipped" -> "Hoàn thành"
            else -> "Cập nhật trạng thái"
        }
    }
    
    /**
     * Check if order can be updated to next status
     */
    fun canUpdateToNextStatus(): Boolean {
        return status.lowercase() in listOf("pending", "confirmed", "processing", "shipped")
    }
}

/**
 * Order item trong đơn hàng admin
 */
@JsonClass(generateAdapter = true)
data class AdminOrderItem(
    @Json(name = "id") val id: Int,
    @Json(name = "product_id") val productId: Int,
    @Json(name = "attribute_id") val attributeId: Int,
    @Json(name = "product_name") val productName: String,
    @Json(name = "product_image") val productImage: String?,
    @Json(name = "attribute_details") val attributeDetails: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "unit_price") val unitPrice: Int,
    @Json(name = "total_price") val totalPrice: Int,
    @Json(name = "created_at") val createdAt: String
) {
    /**
     * Get image URL
     */
    fun getImageUrl(): String? {
        val baseUrl = "https://www.utt-school.site"
        
        println("🔍 AdminOrderItem.getImageUrl() - productImage: '$productImage'")
        
        return when {
            productImage.isNullOrBlank() -> {
                println("❌ Product image is null or blank")
                null
            }
            productImage.startsWith("http") -> {
                println("✅ Product image is full URL: $productImage")
                productImage
            }
            else -> {
                val fullUrl = "$baseUrl$productImage"
                println("✅ Product image is relative path, full URL: $fullUrl")
                fullUrl
            }
        }
    }
    
    /**
     * Format unit price thành VNĐ
     */
    val formattedUnitPrice: String
        get() = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(unitPrice)
    
    /**
     * Format total price thành VNĐ
     */
    val formattedTotalPrice: String
        get() = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(totalPrice)
}

/**
 * Request body cho update order status
 */
@JsonClass(generateAdapter = true)
data class UpdateOrderStatusRequest(
    @Json(name = "status") val status: Int,
    @Json(name = "internal_notes") val internalNotes: String?,
    @Json(name = "notify_customer") val notifyCustomer: Boolean = true
) 