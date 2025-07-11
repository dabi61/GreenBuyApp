package com.example.greenbuyapp.data.cart.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.text.NumberFormat
import java.util.Locale

@Parcelize
@JsonClass(generateAdapter = true)
data class CartItem(
    @Json(name = "attribute_id")
    val attributeId: Int,
    
    @Json(name = "quantity")
    val quantity: Int,
    
    @Json(name = "product_id")
    val productId: Int,
    
    @Json(name = "product_name")
    val productName: String,
    
    @Json(name = "price")
    val price: Double,
    
    @Json(name = "cover")
    val cover: String?,
    
    @Json(name = "color")
    val color: String?,
    
    @Json(name = "size")
    val size: String?,
    
    @Json(name = "attribute_image")
    val attributeImage: String?,
    
    @Json(name = "available_quantity")
    val availableQuantity: Int
) : Parcelable {
    /**
     * Lấy chi tiết thuộc tính (màu sắc, kích thước)
     */
    fun getAttributeDetails(): String {
        val details = mutableListOf<String>()
        color?.let { if (it.isNotBlank()) details.add("Màu: $it") }
        size?.let { if (it.isNotBlank()) details.add("Size: $it") }
        return if (details.isNotEmpty()) details.joinToString(" | ") else ""
    }
    
    /**
     * Lấy URL hình ảnh (ưu tiên attribute_image, fallback về cover)
     */
    fun getImageUrl(): String? {
        val baseUrl = "https://www.utt-school.site"
        
        return when {
            !attributeImage.isNullOrBlank() -> {
                if (attributeImage.startsWith("http")) {
                    attributeImage
                } else {
                    "$baseUrl$attributeImage"
                }
            }
            !cover.isNullOrBlank() -> {
                if (cover.startsWith("http")) {
                    cover
                } else {
                    "$baseUrl$cover"
                }
            }
            else -> null
        }
    }
    
    /**
     * Format giá đơn vị
     */
    fun getFormattedUnitPrice(): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(price)
    }
    
    /**
     * Format tổng giá (giá × số lượng)
     */
    fun getFormattedTotalPrice(): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(price * quantity)
    }
    
    /**
     * Tính tổng giá (giá × số lượng)
     */
    fun getTotalPrice(): Double {
        return price * quantity
    }
    
    /**
     * Kiểm tra có thể tăng số lượng không
     */
    fun canIncreaseQuantity(): Boolean {
        return quantity < availableQuantity
    }
    
    /**
     * Kiểm tra có thể giảm số lượng không
     */
    fun canDecreaseQuantity(): Boolean {
        return quantity > 1
    }
} 