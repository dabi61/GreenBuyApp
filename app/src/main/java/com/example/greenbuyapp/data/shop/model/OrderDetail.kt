package com.example.greenbuyapp.data.shop.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Order detail response từ API
 */
@JsonClass(generateAdapter = true)
data class OrderDetail(
    @Json(name = "id") val id: Int,
    @Json(name = "order_number") val orderNumber: String,
    @Json(name = "status") val status: String,
    @Json(name = "subtotal") val subtotal: Int,
    @Json(name = "tax_amount") val taxAmount: Int,
    @Json(name = "shipping_fee") val shippingFee: Int,
    @Json(name = "discount_amount") val discountAmount: Int,
    @Json(name = "total_amount") val totalAmount: Int,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "confirmed_at") val confirmedAt: String?,
    @Json(name = "shipped_at") val shippedAt: String?,
    @Json(name = "delivered_at") val deliveredAt: String?,
    @Json(name = "cancelled_at") val cancelledAt: String?,
    @Json(name = "billing_address") val billingAddress: String,
    @Json(name = "shipping_address") val shippingAddress: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "recipient_name") val recipientName: String,
    @Json(name = "delivery_notes") val deliveryNotes: String,
    @Json(name = "notes") val notes: String?,
    @Json(name = "internal_notes") val internalNotes: String?,
    @Json(name = "items") val items: List<OrderItem>
) {
    /**
     * Format subtotal thành VND
     */
    fun getFormattedSubtotal(): String {
        return "${subtotal.toString().replace(Regex("(\\d)(?=(\\d{3})+(?!\\d))"), "$1.")} VND"
    }
    
    /**
     * Format shipping fee thành VND
     */
    fun getFormattedShippingFee(): String {
        return "${shippingFee.toString().replace(Regex("(\\d)(?=(\\d{3})+(?!\\d))"), "$1.")} VND"
    }
    
    /**
     * Format total amount thành VND
     */
    fun getFormattedTotalAmount(): String {
        return "${totalAmount.toString().replace(Regex("(\\d)(?=(\\d{3})+(?!\\d))"), "$1.")} VND"
    }
    
    /**
     * Format discount amount thành VND
     */
    fun getFormattedDiscountAmount(): String {
        return "${discountAmount.toString().replace(Regex("(\\d)(?=(\\d{3})+(?!\\d))"), "$1.")} VND"
    }
    
    /**
     * Get order status display name
     */
    fun getStatusDisplayName(): String {
        return when (status.lowercase()) {
            "pending" -> "Chờ xác nhận"
            "confirmed" -> "Đã xác nhận"
            "processing" -> "Đang xử lý"
            "shipped" -> "Đang giao"
            "delivered" -> "Đã giao"
            "cancelled" -> "Đã hủy"
            "refunded" -> "Đã hoàn tiền"
            "returned" -> "Đã trả hàng"
            else -> status
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
}

/**
 * Order item trong đơn hàng
 */
@JsonClass(generateAdapter = true)
data class OrderItem(
    @Json(name = "id") val id: Int,
    @Json(name = "product_id") val productId: Int,
    @Json(name = "attribute_id") val attributeId: Int,
    @Json(name = "product_name") val productName: String,
    @Json(name = "product_image") val productImage: String?,
    @Json(name = "attribute_image") val attributeImage: String? = null, // ✅ Thêm attribute_image
    @Json(name = "attribute_details") val attributeDetails: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "unit_price") val unitPrice: Int,
    @Json(name = "total_price") val totalPrice: Int,
    @Json(name = "created_at") val createdAt: String
) {
    /**
     * ✅ Lấy URL hình ảnh - ưu tiên attribute_image trước, fallback về product_image
     */
    fun getImageUrl(): String? {
        val baseUrl = "https://www.utt-school.site"
        
        return when {
            // ✅ Ưu tiên attribute_image trước
            !attributeImage.isNullOrBlank() -> {
                if (attributeImage.startsWith("http")) {
                    attributeImage
                } else {
                    "$baseUrl$attributeImage"
                }
            }
            // ✅ Fallback về product_image
            !productImage.isNullOrBlank() -> {
                if (productImage.startsWith("http")) {
                    productImage
                } else {
                    "$baseUrl$productImage"
                }
            }
            else -> null
        }
    }
    
    /**
     * Format unit price thành VND
     */
    fun getFormattedUnitPrice(): String {
        return "${unitPrice.toString().replace(Regex("(\\d)(?=(\\d{3})+(?!\\d))"), "$1.")} VND"
    }
    
    /**
     * Format total price thành VND
     */
    fun getFormattedTotalPrice(): String {
        return "${totalPrice.toString().replace(Regex("(\\d)(?=(\\d{3})+(?!\\d))"), "$1.")} VND"
    }
} 