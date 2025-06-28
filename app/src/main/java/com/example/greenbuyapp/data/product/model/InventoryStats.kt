package com.example.greenbuyapp.data.product.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InventoryStatsResponse(
    val summary: InventoryStatsSummary,
    val shop_id: Int,
    val shop_name: String
)

@JsonClass(generateAdapter = true)
data class InventoryStatsSummary(
    val total_products: Int,
    val pending_approval: Int? = null,  // API có thể trả về pending_approval
    val pending: Int? = null,           // hoặc pending
    val in_stock: Int,
    val out_of_stock: Int
) {
    // Helper property để lấy số lượng pending bất kể API trả về field nào
    val pendingCount: Int
        get() = pending_approval ?: pending ?: 0
} 