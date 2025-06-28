package com.example.greenbuyapp.ui.shop.productManagement

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.greenbuyapp.data.product.model.ProductStatus

class ProductPagerAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    // 3 tabs: Còn hàng, Hết hàng, Chờ duyệt
    private val productStatuses = listOf(
        ProductStatus.IN_STOCK,        // Còn hàng
        ProductStatus.OUT_OF_STOCK,    // Hết hàng  
        ProductStatus.PENDING_APPROVAL // Chờ duyệt
    )

    override fun getItemCount(): Int = productStatuses.size

    override fun createFragment(position: Int): Fragment {
        val productStatus = productStatuses[position]
        return ProductFragment.newInstance(productStatus)
    }

    fun getProductStatus(position: Int): ProductStatus {
        return productStatuses.getOrElse(position) { ProductStatus.IN_STOCK }
    }

    fun getPosition(productStatus: ProductStatus): Int {
        return productStatuses.indexOf(productStatus)
    }
} 