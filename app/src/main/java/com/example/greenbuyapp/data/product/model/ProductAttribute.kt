package com.example.greenbuyapp.data.product.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class ProductAttribute(
    val attribute_id: Int,
    val product_id: Int,
    val color: String,
    val size: String,
    val price: Double,
    val image: String,
    val quantity: Int,
    val create_at: String
) : Parcelable {
    /**
     * Get full image URL
     */
    fun getImageUrl(): String {
        return if (image.startsWith("http")) {
            image
        } else {
            "https://www.utt-school.site$image"
        }
    }
    
    /**
     * Format price to VND
     */
    fun getFormattedPrice(): String {
        return "₫${String.format("%,.0f", price)}"
    }
}

typealias ProductAttributeList = List<ProductAttribute> 