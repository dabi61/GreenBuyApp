package com.example.greenbuyapp.ui.profile.orders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.user.model.CustomerOrderDetail
import com.example.greenbuyapp.databinding.ActivityOrderDetailBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.domain.product.ProductRepository
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*

class CustomerOrderDetailActivity : BaseActivity<ActivityOrderDetailBinding>() {

    override val viewModel: CustomerOrderDetailViewModel by viewModel()
    override val binding: ActivityOrderDetailBinding by lazy { 
        ActivityOrderDetailBinding.inflate(layoutInflater) 
    }
    
    // ✅ Inject ProductRepository từ Koin
    private val productRepository: ProductRepository by inject()
    private lateinit var itemAdapter: CustomerOrderDetailItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun initViews() {
        setupToolbar()
        setupRecyclerView()
        
        // Get order ID từ intent
        val orderId = intent.getIntExtra(EXTRA_ORDER_ID, -1)
        if (orderId == -1) {
            Toast.makeText(this, "❌ Không tìm thấy thông tin đơn hàng", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Load order detail
        viewModel.loadOrderDetail(orderId)
        
        println("🔍 CustomerOrderDetailActivity initialized with orderId: $orderId")
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // Cập nhật title toolbar
        binding.toolbar.title = "Chi tiết đơn hàng"
    }

    private fun setupRecyclerView() {
        // ✅ Truyền ProductRepository vào adapter
        itemAdapter = CustomerOrderDetailItemAdapter(productRepository)
        
        binding.recyclerViewItems.apply {
            adapter = itemAdapter
            layoutManager = LinearLayoutManager(this@CustomerOrderDetailActivity)
        }
        
        println("✅ RecyclerView setup completed with ProductRepository injection")
    }

    override fun observeViewModel() {
        observeOrderDetail()
        observeLoadingState()
        observeErrorMessages()
    }

    private fun observeOrderDetail() {
        lifecycleScope.launch {
            viewModel.orderDetail.collect { orderDetail ->
                if (orderDetail != null) {
                    bindOrderDetail(orderDetail)
                    println("✅ Customer order detail bound: ${orderDetail.orderNumber}")
                }
            }
        }
    }

    private fun observeLoadingState() {
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.isVisible = isLoading
                println("⏳ Loading state: $isLoading")
            }
        }
    }

    private fun observeErrorMessages() {
        lifecycleScope.launch {
            viewModel.errorMessage.collect { message ->
                if (!message.isNullOrEmpty()) {
                    Toast.makeText(this@CustomerOrderDetailActivity, message, Toast.LENGTH_LONG).show()
                    viewModel.clearErrorMessage()
                    println("❌ Error: $message")
                }
            }
        }
    }

    private fun bindOrderDetail(orderDetail: CustomerOrderDetail) {
        binding.apply {
            // Order header
            tvOrderNumber.text = orderDetail.orderNumber
            tvStatus.text = orderDetail.getStatusDisplayName()
            tvStatus.setBackgroundColor(orderDetail.getStatusColor())
            tvCreatedAt.text = orderDetail.formattedCreatedAt

            // Customer info - hiển thị thông tin người nhận
            tvRecipientName.text = orderDetail.recipientName ?: "Chưa có thông tin người nhận"
            tvPhoneNumber.text = orderDetail.phoneNumber ?: "Chưa có số điện thoại"
            tvShippingAddress.text = orderDetail.shippingAddress ?: "Chưa có địa chỉ giao hàng"

            // Delivery notes (hiển thị nếu có)
            if (!orderDetail.deliveryNotes.isNullOrEmpty() && orderDetail.deliveryNotes.isNotBlank()) {
                llDeliveryNotes.isVisible = true
                tvDeliveryNotes.text = orderDetail.deliveryNotes
            } else {
                llDeliveryNotes.isVisible = false
            }

            // Payment summary
            tvSubtotal.text = orderDetail.getFormattedSubtotal()
            tvShippingFee.text = orderDetail.getFormattedShippingFee()
            tvTotalAmount.text = orderDetail.getFormattedTotalAmount()

            // Discount (hiển thị nếu có)
            if (orderDetail.discountAmount > 0) {
                llDiscount.isVisible = true
                tvDiscountAmount.text = "-${orderDetail.getFormattedDiscountAmount()}"
            } else {
                llDiscount.isVisible = false
            }

            // Product items
            itemAdapter.submitList(orderDetail.items)
            
            println("📋 Customer order detail UI updated successfully")
        }
    }

    companion object {
        private const val EXTRA_ORDER_ID = "order_id"

        /**
         * Tạo intent để mở CustomerOrderDetailActivity
         */
        fun createIntent(context: Context, orderId: Int): Intent {
            return Intent(context, CustomerOrderDetailActivity::class.java).apply {
                putExtra(EXTRA_ORDER_ID, orderId)
            }
        }
    }
} 