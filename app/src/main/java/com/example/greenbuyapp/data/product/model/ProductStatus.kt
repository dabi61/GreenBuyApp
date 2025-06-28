package com.example.greenbuyapp.data.product.model

/**
 * Enum định nghĩa các trạng thái sản phẩm
 */
enum class ProductStatus(val displayName: String, val apiValue: String) {
    IN_STOCK("Còn hàng", "in_stock"),
    OUT_OF_STOCK("Hết hàng", "out_of_stock"), 
    PENDING_APPROVAL("Chờ duyệt", "pending");
    
    companion object {
        fun fromApiValue(apiValue: String): ProductStatus? {
            return values().find { it.apiValue == apiValue }
        }
        
        /**
         * Helper để mapping status từ stock_info hoặc các API khác
         */
        fun fromStockStatus(status: String): ProductStatus? {
            return when (status) {
                "in_stock" -> IN_STOCK
                "out_of_stock" -> OUT_OF_STOCK
                "pending", "pending_approval" -> PENDING_APPROVAL
                else -> null
            }
        }
    }
} 