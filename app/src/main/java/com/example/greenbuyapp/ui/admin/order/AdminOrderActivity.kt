package com.example.greenbuyapp.ui.admin.order

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.shop.model.AdminOrder
import com.example.greenbuyapp.databinding.ActivityAdminOrderBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AdminOrderActivity : BaseActivity<ActivityAdminOrderBinding>() {

    override val viewModel: AdminOrderViewModel by viewModel()
    
    override val binding: ActivityAdminOrderBinding by lazy {
        ActivityAdminOrderBinding.inflate(layoutInflater)
    }
    
    private lateinit var adminOrderAdapter: AdminOrderAdapter
    
    // ✅ Throttling cho infinite scroll
    private var lastScrollTime = 0L
    private val scrollThrottleMs = 500L
    private var isLoadMoreTriggered = false

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, AdminOrderActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun initViews() {
        setupToolbar()
        setupRecyclerView()
        setupSearchView()
        setupFilters()
        setupInfiniteScrolling()
        
        // ✅ Load initial data
        viewModel.loadOrders(isRefresh = true)
    }

    override fun observeViewModel() {
        observeOrders()
        observeLoadingState()
        observeErrorState()
        observePaginationState()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Duyệt đơn hàng"
        }
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adminOrderAdapter = AdminOrderAdapter(
            onOrderClick = { order ->
                // ✅ Handle order click - mở chi tiết đơn hàng
                println("🔍 Order clicked: ${order.orderNumber}")
                // TODO: Mở OrderDetailActivity
                // val intent = OrderDetailActivity.createIntent(this, order.id)
                // startActivity(intent)
            },
            onStatusClick = { order ->
                // ✅ Handle status change click
                showStatusChangeDialog(order)
            }
        )
        
        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(this@AdminOrderActivity)
            adapter = adminOrderAdapter
            
            // ✅ Performance optimizations
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            setDrawingCacheEnabled(true)
            drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    }

    private fun setupSearchView() {
        binding.etSearch.addTextChangedListener { text ->
            val query = text?.toString() ?: ""
            
            // ✅ Debounce search - chờ 500ms sau khi user ngừng gõ
            Handler(Looper.getMainLooper()).removeCallbacksAndMessages(null)
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.updateSearchQuery(query)
            }, 500)
        }
    }

    private fun setupFilters() {
        // ✅ Status filter
        binding.btnStatusFilter.setOnClickListener {
            showStatusFilterDialog()
        }
        
        // ✅ Payment status filter
        binding.btnPaymentFilter.setOnClickListener {
            showPaymentStatusFilterDialog()
        }
        
        // ✅ Clear filters
        binding.btnClearFilters.setOnClickListener {
            binding.etSearch.setText("")
            viewModel.clearFilters()
            Toast.makeText(this, "Đã xóa tất cả bộ lọc", Toast.LENGTH_SHORT).show()
        }
        
        // ✅ Refresh button
        binding.btnRefresh.setOnClickListener {
            viewModel.refreshOrders()
        }
    }

    private fun setupInfiniteScrolling() {
        binding.rvOrders.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                val currentTime = System.currentTimeMillis()
                
                // ✅ Throttling
                if (currentTime - lastScrollTime < scrollThrottleMs) {
                    return
                }
                lastScrollTime = currentTime
                
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                
                val isScrollingDown = dy > 0
                val isNearBottom = (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3
                
                if (isScrollingDown && isNearBottom && !isLoadMoreTriggered) {
                    println("🔄 RecyclerView reached near end, triggering load more...")
                    isLoadMoreTriggered = true
                    viewModel.loadMoreOrders()
                    
                    // ✅ Reset flag sau 2 giây
                    Handler(Looper.getMainLooper()).postDelayed({
                        isLoadMoreTriggered = false
                    }, 2000)
                }
            }
        })
    }

    private fun observeOrders() {
        lifecycleScope.launch {
            viewModel.orders.collect { orders ->
                println("📋 Orders updated: ${orders.size} items")
                
                if (orders.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvOrders.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvOrders.visibility = View.VISIBLE
                }
                
                adminOrderAdapter.submitList(orders) {
                    println("✅ AdminOrderAdapter submitList completed")
                }
            }
        }
    }

    private fun observeLoadingState() {
        lifecycleScope.launch {
            viewModel.ordersLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.btnRefresh.isEnabled = !isLoading
                
                if (isLoading) {
                    isLoadMoreTriggered = false
                }
            }
        }
    }

    private fun observeErrorState() {
        lifecycleScope.launch {
            viewModel.ordersError.collect { error ->
                error?.let {
                    Toast.makeText(this@AdminOrderActivity, it, Toast.LENGTH_LONG).show()
                    viewModel.clearErrorMessage()
                }
            }
        }
    }

    private fun observePaginationState() {
        lifecycleScope.launch {
            viewModel.totalOrders.collect { total ->
                binding.tvTotalCount.text = "Tổng: $total đơn hàng"
            }
        }
    }

    private fun showStatusFilterDialog() {
        val statusOptions = arrayOf(
            "Tất cả",
            "Chờ xác nhận",
            "Đã xác nhận", 
            "Đang xử lý",
            "Đang giao",
            "Đã giao",
            "Đã hủy"
        )
        
        val statusValues = arrayOf(
            null,
            "pending",
            "confirmed",
            "processing", 
            "shipped",
            "delivered",
            "cancelled"
        )
        
        AlertDialog.Builder(this)
            .setTitle("Lọc theo trạng thái")
            .setItems(statusOptions) { _, which ->
                val selectedStatus = statusValues[which]
                viewModel.updateStatusFilter(selectedStatus)
                
                binding.btnStatusFilter.text = if (selectedStatus == null) {
                    "Trạng thái"
                } else {
                    statusOptions[which]
                }
            }
            .show()
    }

    private fun showPaymentStatusFilterDialog() {
        val paymentOptions = arrayOf(
            "Tất cả",
            "Chờ thanh toán",
            "Đã thanh toán",
            "Thanh toán thất bại",
            "Đã hoàn tiền"
        )
        
        val paymentValues = arrayOf(
            null,
            "pending",
            "paid",
            "failed",
            "refunded"
        )
        
        AlertDialog.Builder(this)
            .setTitle("Lọc theo thanh toán")
            .setItems(paymentOptions) { _, which ->
                val selectedPayment = paymentValues[which]
                viewModel.updatePaymentStatusFilter(selectedPayment)
                
                binding.btnPaymentFilter.text = if (selectedPayment == null) {
                    "Thanh toán"
                } else {
                    paymentOptions[which]
                }
            }
            .show()
    }

    private fun showStatusChangeDialog(order: AdminOrder) {
        val statusOptions = arrayOf(
            "Chờ xác nhận",
            "Đã xác nhận",
            "Đang xử lý", 
            "Đang giao",
            "Đã giao",
            "Đã hủy"
        )
        
        val statusValues = arrayOf(
            "pending",
            "confirmed",
            "processing",
            "shipped", 
            "delivered",
            "cancelled"
        )
        
        AlertDialog.Builder(this)
            .setTitle("Thay đổi trạng thái đơn hàng")
            .setMessage("Đơn hàng: ${order.orderNumber}")
            .setItems(statusOptions) { _, which ->
                val newStatus = statusValues[which]
                
                // ✅ Confirm dialog
                AlertDialog.Builder(this)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc chắn muốn thay đổi trạng thái đơn hàng thành \"${statusOptions[which]}\"?")
                    .setPositiveButton("Xác nhận") { _, _ ->
                        viewModel.updateOrderStatus(order.id, newStatus)
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
} 