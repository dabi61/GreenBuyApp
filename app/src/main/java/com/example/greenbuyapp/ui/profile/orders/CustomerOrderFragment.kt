package com.example.greenbuyapp.ui.profile.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.user.model.CustomerOrder
import com.example.greenbuyapp.data.user.model.CustomerOrderStatus
import com.example.greenbuyapp.databinding.FragmentCustomerOrderBinding
import com.example.greenbuyapp.ui.base.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CustomerOrderFragment : BaseFragment<FragmentCustomerOrderBinding, CustomerOrderViewModel>() {

    override val viewModel: CustomerOrderViewModel by sharedViewModel()
    
    private lateinit var orderAdapter: CustomerOrderAdapter
    private var orderStatus: CustomerOrderStatus = CustomerOrderStatus.PENDING

    companion object {
        private const val ARG_ORDER_STATUS = "order_status"
        
        fun newInstance(orderStatus: CustomerOrderStatus): CustomerOrderFragment {
            return CustomerOrderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ORDER_STATUS, orderStatus.name)
                }
            }
        }
    }

    override fun getLayoutResourceId(): Int = R.layout.fragment_customer_order

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCustomerOrderBinding {
        return FragmentCustomerOrderBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        // Get order status from arguments
        orderStatus = arguments?.getString(ARG_ORDER_STATUS)?.let { statusName ->
            CustomerOrderStatus.valueOf(statusName)
        } ?: CustomerOrderStatus.PENDING

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
        orderAdapter = CustomerOrderAdapter { order ->
            // Handle order item click - Navigate to order detail
            navigateToOrderDetail(order.id)
            println("📱 Navigating to customer order detail: ${order.id}")
        }

        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = orderAdapter
        }
    }

    /**
     * Navigate to CustomerOrderDetailActivity
     */
    private fun navigateToOrderDetail(orderId: Int) {
        val intent = CustomerOrderDetailActivity.createIntent(requireContext(), orderId)
        startActivity(intent)
        
        println("🔍 Navigate to customer order detail for orderId: $orderId")
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
                
                println("📦 Customer orders for ${orderStatus.displayName}: ${orders.size}")
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
            CustomerOrderStatus.PENDING -> {
                binding.tvEmptyTitle.text = "Chưa có đơn hàng chờ xác nhận"
                binding.tvEmptySubtitle.text = "Khi có đơn hàng mới, chúng sẽ hiển thị ở đây"
            }
            CustomerOrderStatus.CONFIRMED -> {
                binding.tvEmptyTitle.text = "Chưa có đơn hàng đã xác nhận"
                binding.tvEmptySubtitle.text = "Đơn hàng đã xác nhận sẽ hiển thị ở đây"
            }
            CustomerOrderStatus.PROCESSING -> {
                binding.tvEmptyTitle.text = "Chưa có đơn hàng đang xử lý"
                binding.tvEmptySubtitle.text = "Đơn hàng đang xử lý sẽ hiển thị ở đây"
            }
            CustomerOrderStatus.SHIPPED -> {
                binding.tvEmptyTitle.text = "Chưa có đơn hàng đang giao"
                binding.tvEmptySubtitle.text = "Đơn hàng đang giao sẽ hiển thị ở đây"
            }
            CustomerOrderStatus.DELIVERED -> {
                binding.tvEmptyTitle.text = "Chưa có đơn hàng đã giao"
                binding.tvEmptySubtitle.text = "Đơn hàng đã giao thành công sẽ hiển thị ở đây"
            }
            CustomerOrderStatus.CANCELLED -> {
                binding.tvEmptyTitle.text = "Chưa có đơn hàng bị hủy"
                binding.tvEmptySubtitle.text = "Đơn hàng bị hủy sẽ hiển thị ở đây"
            }
        }
    }
} 