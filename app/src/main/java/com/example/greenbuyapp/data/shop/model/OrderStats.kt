package com.example.greenbuyapp.data.shop.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OrderStats(
    @Json(name = "total_orders") val totalOrders: Int,
    @Json(name = "pending_orders") val pendingOrders: Int,
    @Json(name = "confirmed_orders") val confirmedOrders: Int,
    @Json(name = "processing_orders") val processingOrders: Int,
    @Json(name = "shipped_orders") val shippedOrders: Int,
    @Json(name = "delivered_orders") val deliveredOrders: Int,
    @Json(name = "cancelled_orders") val cancelledOrders: Int,
    @Json(name = "refunded_orders") val refundedOrders: Int,
    @Json(name = "returned_orders") val returnedOrders: Int,
    @Json(name = "total_revenue") val totalRevenue: Double,
    @Json(name = "pending_revenue") val pendingRevenue: Double,
    @Json(name = "orders_today") val ordersToday: Int,
    @Json(name = "orders_this_week") val ordersThisWeek: Int,
    @Json(name = "orders_this_month") val ordersThisMonth: Int,
    @Json(name = "pending_ratings") val pendingRatings: Int
) 