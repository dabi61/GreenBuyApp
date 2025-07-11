package com.example.greenbuyapp.ui.shop.orderDetail

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.ActivityOrderDetailBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.domain.product.ProductRepository
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*

class OrderDetailActivity : BaseActivity<ActivityOrderDetailBinding>() {

    override val viewModel: OrderDetailViewModel by viewModel()
    override val binding: ActivityOrderDetailBinding by lazy { 
        ActivityOrderDetailBinding.inflate(layoutInflater) 
    }
    
    // ✅ Inject ProductRepository từ Koin
    private val productRepository: ProductRepository by inject()
    private lateinit var itemAdapter: OrderDetailItemAdapter

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
        
        println("🔍 OrderDetailActivity initialized with orderId: $orderId")
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        // ✅ Truyền ProductRepository vào adapter
        itemAdapter = OrderDetailItemAdapter(productRepository)
        
        binding.recyclerViewItems.apply {
            adapter = itemAdapter
            layoutManager = LinearLayoutManager(this@OrderDetailActivity)
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
                    println("✅ Order detail bound: ${orderDetail.orderNumber}")
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
                    Toast.makeText(this@OrderDetailActivity, message, Toast.LENGTH_LONG).show()
                    viewModel.clearErrorMessage()
                    println("❌ Error: $message")
                }
            }
        }
    }

    private fun bindOrderDetail(orderDetail: com.example.greenbuyapp.data.shop.model.OrderDetail) {
        binding.apply {
            // Order header
            tvOrderNumber.text = orderDetail.orderNumber
            tvStatus.text = orderDetail.getStatusDisplayName()
            tvStatus.setBackgroundColor(orderDetail.getStatusColor())
            tvCreatedAt.text = formatDate(orderDetail.createdAt)

            // Customer info
            tvRecipientName.text = orderDetail.recipientName
            tvPhoneNumber.text = orderDetail.phoneNumber
            tvShippingAddress.text = orderDetail.shippingAddress

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
            
            println("📋 Order detail UI updated successfully")
        }
    }

    /**
     * Format ISO date string to dd/MM/yyyy HH:mm
     */
    private fun formatDate(isoDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(isoDate)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            println("❌ Date parsing error: ${e.message}")
            isoDate // Return original if parsing fails
        }
    }

    companion object {
        private const val EXTRA_ORDER_ID = "order_id"

        /**
         * Tạo intent để mở OrderDetailActivity
         */
        fun createIntent(context: Context, orderId: Int): Intent {
            return Intent(context, OrderDetailActivity::class.java).apply {
                putExtra(EXTRA_ORDER_ID, orderId)
            }
        }
    }
} 