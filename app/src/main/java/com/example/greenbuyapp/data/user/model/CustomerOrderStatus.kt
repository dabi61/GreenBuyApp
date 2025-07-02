package com.example.greenbuyapp.data.user.model

import com.example.greenbuyapp.data.product.model.ProductStatus

/**
 * Enum cho trạng thái đơn hàng của khách hàng
 * Sử dụng ProductStatus làm base và map với status_filter
 */
enum class CustomerOrderStatus(
    val displayName: String, 
    val statusFilter: Int,
    val apiValue: String
) {
    PENDING("Chờ xác nhận", 1, "pending"),
    CONFIRMED("Đã xác nhận", 2, "confirmed"), 
    PROCESSING("Đang xử lý", 3, "processing"),
    SHIPPED("Đang giao", 4, "shipped"),
    DELIVERED("Đã giao", 5, "delivered"),
    CANCELLED("Đã hủy", 6, "cancelled");
    
    companion object {
        fun fromApiValue(apiValue: String): CustomerOrderStatus? {
            return values().find { it.apiValue == apiValue }
        }
        
        fun fromStatusFilter(statusFilter: Int): CustomerOrderStatus? {
            return values().find { it.statusFilter == statusFilter }
        }
        
        /**
         * Get all order statuses để hiển thị tabs
         */
        fun getAllStatuses(): List<CustomerOrderStatus> {
            return listOf(PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
        }
    }
} 