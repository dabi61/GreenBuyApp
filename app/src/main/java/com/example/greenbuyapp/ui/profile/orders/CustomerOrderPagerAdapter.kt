package com.example.greenbuyapp.ui.profile.orders

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.greenbuyapp.data.user.model.CustomerOrderStatus

class CustomerOrderPagerAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    // Sử dụng tất cả order statuses
    private val orderStatuses = CustomerOrderStatus.getAllStatuses()

    override fun getItemCount(): Int = orderStatuses.size

    override fun createFragment(position: Int): Fragment {
        val orderStatus = orderStatuses[position]
        return CustomerOrderFragment.newInstance(orderStatus)
    }

    fun getOrderStatus(position: Int): CustomerOrderStatus {
        return orderStatuses.getOrElse(position) { CustomerOrderStatus.PENDING }
    }

    fun getPosition(orderStatus: CustomerOrderStatus): Int {
        return orderStatuses.indexOf(orderStatus)
    }
} 