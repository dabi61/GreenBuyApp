package com.example.greenbuyapp.data.user.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
@JsonClass(generateAdapter = true)
data class CustomerOrderDetail(
    @Json(name = "id") val id: Int,
    @Json(name = "order_number") val orderNumber: String,
    @Json(name = "status") val status: String,
    @Json(name = "subtotal") val subtotal: Double,
    @Json(name = "tax_amount") val taxAmount: Double = 0.0,
    @Json(name = "shipping_fee") val shippingFee: Double = 0.0,
    @Json(name = "discount_amount") val discountAmount: Double = 0.0,
    @Json(name = "total_amount") val totalAmount: Double,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "confirmed_at") val confirmedAt: String? = null,
    @Json(name = "shipped_at") val shippedAt: String? = null,
    @Json(name = "delivered_at") val deliveredAt: String? = null,
    @Json(name = "cancelled_at") val cancelledAt: String? = null,
    @Json(name = "billing_address") val billingAddress: String? = null,
    @Json(name = "shipping_address") val shippingAddress: String? = null,
    @Json(name = "phone_number") val phoneNumber: String? = null,
    @Json(name = "recipient_name") val recipientName: String? = null,
    @Json(name = "delivery_notes") val deliveryNotes: String? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "internal_notes") val internalNotes: String? = null,
    @Json(name = "items") val items: List<CustomerOrderItem> = emptyList()
) : Parcelable {
    
    /**
     * Tính tổng số items từ danh sách items
     */
    val totalItems: Int
        get() = items.sumOf { it.quantity }
    
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
    fun getStatusDisplayName(): String {
        return when (status) {
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
    
    /**
     * Get status color
     */
    fun getStatusColor(): Int {
        return when (status) {
            "pending" -> android.graphics.Color.parseColor("#FFA726") // Orange
            "confirmed" -> android.graphics.Color.parseColor("#42A5F5") // Blue
            "processing" -> android.graphics.Color.parseColor("#AB47BC") // Purple
            "shipped" -> android.graphics.Color.parseColor("#26A69A") // Teal
            "delivered" -> android.graphics.Color.parseColor("#66BB6A") // Green
            "cancelled" -> android.graphics.Color.parseColor("#EF5350") // Red
            "refunded" -> android.graphics.Color.parseColor("#FF7043") // Deep Orange
            "returned" -> android.graphics.Color.parseColor("#8D6E63") // Brown
            else -> android.graphics.Color.parseColor("#78909C") // Blue Grey
        }
    }
    
    fun getFormattedSubtotal(): String = getCurrencyFormatter().format(subtotal)
    fun getFormattedShippingFee(): String = getCurrencyFormatter().format(shippingFee)
    fun getFormattedDiscountAmount(): String = getCurrencyFormatter().format(discountAmount)
    fun getFormattedTotalAmount(): String = getCurrencyFormatter().format(totalAmount)
    
    companion object {
        private fun getCurrencyFormatter() = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).apply {
            currency = Currency.getInstance("VND")
        }
    }
}

@Parcelize
@JsonClass(generateAdapter = true)
data class CustomerOrderItem(
    @Json(name = "id") val id: Int,
    @Json(name = "product_id") val productId: Int,
    @Json(name = "attribute_id") val attributeId: Int,
    @Json(name = "product_name") val productName: String,
    @Json(name = "product_image") val productImage: String? = null,
    @Json(name = "attribute_image") val attributeImage: String? = null, // ✅ Thêm attribute_image
    @Json(name = "attribute_details") val attributeDetails: String? = null,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "unit_price") val unitPrice: Double,
    @Json(name = "total_price") val totalPrice: Double,
    @Json(name = "created_at") val createdAt: String
) : Parcelable {
    
    /**
     * ✅ Lấy URL hình ảnh - ưu tiên attribute_image trước, fallback về product_image
     */
    fun getImageUrl(): String? {
        val baseUrl = "https://www.utt-school.site"
        
        return when {
            // ✅ Ưu tiên attribute_image trước (giống CartItem)
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
    
    fun getFormattedUnitPrice(): String = getCurrencyFormatter().format(unitPrice)
    fun getFormattedTotalPrice(): String = getCurrencyFormatter().format(totalPrice)
    
    companion object {
        private fun getCurrencyFormatter() = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).apply {
            currency = Currency.getInstance("VND")
        }
    }
} 