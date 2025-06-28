package com.example.greenbuyapp.ui.shop.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.shop.model.Order
import com.example.greenbuyapp.data.shop.model.OrderStatus
import com.example.greenbuyapp.databinding.FragmentOrderBinding
import com.example.greenbuyapp.ui.base.BaseFragment
import com.example.greenbuyapp.ui.shop.orderDetail.OrderDetailActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class OrderFragment : BaseFragment<FragmentOrderBinding, ShopDashboardDetailViewModel>() {

    override val viewModel: ShopDashboardDetailViewModel by sharedViewModel()
    
    private lateinit var orderAdapter: OrderAdapter
    private var orderStatus: OrderStatus = OrderStatus.PENDING

    companion object {
        private const val ARG_ORDER_STATUS = "order_status"
        
        fun newInstance(orderStatus: OrderStatus): OrderFragment {
            return OrderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ORDER_STATUS, orderStatus.name)
                }
            }
        }
    }

    override fun getLayoutResourceId(): Int = R.layout.fragment_order

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOrderBinding {
        return FragmentOrderBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        // Get order status from arguments
        orderStatus = arguments?.getString(ARG_ORDER_STATUS)?.let { statusName ->
            OrderStatus.valueOf(statusName)
        } ?: OrderStatus.PENDING

        setupRecyclerView()
        setupSwipeRefresh()
        
        // Load orders for this status
        viewModel.loadOrdersByStatus(orderStatus)
    }

    override fun observeViewModel() {
        observeOrders()
        observeLoadingState()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter { order ->
            // Handle order item click - Navigate to order detail
            navigateToOrderDetail(order.id)
            println("📱 Navigating to order detail: ${order.id}")
        }

        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = orderAdapter
        }
    }

    /**
     * Navigate to OrderDetailActivity
     */
    private fun navigateToOrderDetail(orderId: Int) {
        val intent = OrderDetailActivity.createIntent(requireContext(), orderId)
        startActivity(intent)
        
        println("🔍 Order detail activity started for orderId: $orderId")
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadOrdersByStatus(orderStatus)
        }
    }

    private fun observeOrders() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getOrdersByStatus(orderStatus).collect { orders ->
                orderAdapter.submitList(orders)
                
                // Show/hide empty state
                if (orders.isEmpty()) {
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.rvOrders.visibility = View.GONE
                    updateEmptyStateText()
                } else {
                    binding.layoutEmpty.visibility = View.GONE
                    binding.rvOrders.visibility = View.VISIBLE
                }
                
                println("📦 Orders for ${orderStatus.displayName}: ${orders.size}")
            }
        }
    }

    private fun observeLoadingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.swipeRefresh.isRefreshing = isLoading
                binding.progressBar.visibility = if (isLoading && orderAdapter.itemCount == 0) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    private fun updateEmptyStateText() {
        when (orderStatus) {
            OrderStatus.PENDING -> {
                binding.tvEmptyTitle.text = "Chưa có đơn hàng chờ xác nhận"
                binding.tvEmptySubtitle.text = "Khi có đơn hàng mới, chúng sẽ hiển thị ở đây"
            }
            OrderStatus.CONFIRMED -> {
                binding.tvEmptyTitle.text = "Chưa có đơn hàng chờ lấy hàng"
                binding.tvEmptySubtitle.text = "Đơn hàng đã xác nhận sẽ hiển thị ở đây"
            }
            OrderStatus.SHIPPING -> {
                binding.tvEmptyTitle.text = "Chưa có đơn hàng đang giao"
                binding.tvEmptySubtitle.text = "Đơn hàng đang giao sẽ hiển thị ở đây"
            }
            OrderStatus.DELIVERED -> {
                binding.tvEmptyTitle.text = "Chưa có đơn hàng đã giao"
                binding.tvEmptySubtitle.text = "Đơn hàng đã giao thành công sẽ hiển thị ở đây"
            }
            OrderStatus.CANCELLED -> {
                binding.tvEmptyTitle.text = "Chưa có đơn hàng bị hủy"
                binding.tvEmptySubtitle.text = "Đơn hàng bị hủy sẽ hiển thị ở đây"
            }
            else -> {
                binding.tvEmptyTitle.text = "Chưa có đơn hàng chờ xác nhận"
                binding.tvEmptySubtitle.text = "Khi có đơn hàng mới, chúng sẽ hiển thị ở đây"
            }
        }
    }
} 