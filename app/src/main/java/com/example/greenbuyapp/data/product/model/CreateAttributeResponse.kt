package com.example.greenbuyapp.data.product.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
@JsonClass(generateAdapter = true)
data class CreateAttributeResponse(
    val attribute_id: Int,
    val product_id: Int,
    val color: String,
    val size: String,
    val price: Double,
    val image: String,
    val quantity: Int,
    val create_at: String
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class ProductVariant(
    val id: String = UUID.randomUUID().toString(),
    val color: String = "",
    val size: String = "",
    val price: String = "",
    val quantity: String = "",
    val imageUri: String? = null
) : Parcelable {
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductVariant

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
} 