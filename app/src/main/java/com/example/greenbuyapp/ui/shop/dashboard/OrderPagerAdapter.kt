package com.example.greenbuyapp.ui.shop.dashboard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.greenbuyapp.data.shop.model.OrderStatus

class OrderPagerAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    // Chỉ hiển thị 5 tab chính để tránh quá nhiều tab
    private val orderStatuses = listOf(
        OrderStatus.PENDING,    // Chờ xác nhận
        OrderStatus.CONFIRMED,  // Chờ lấy hàng  
        OrderStatus.SHIPPING,    // Đang giao
        OrderStatus.DELIVERED,  // Đã giao
        OrderStatus.CANCELLED   // Đơn hủy
    )

    override fun getItemCount(): Int = orderStatuses.size

    override fun createFragment(position: Int): Fragment {
        val orderStatus = orderStatuses[position]
        return OrderFragment.newInstance(orderStatus)
    }

    fun getOrderStatus(position: Int): OrderStatus {
        return orderStatuses.getOrElse(position) { OrderStatus.PENDING }
    }

    fun getPosition(orderStatus: OrderStatus): Int {
        return orderStatuses.indexOf(orderStatus)
    }
} 