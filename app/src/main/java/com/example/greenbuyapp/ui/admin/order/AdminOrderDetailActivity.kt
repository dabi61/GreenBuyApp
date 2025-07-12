package com.example.greenbuyapp.ui.admin.order

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.shop.model.AdminOrderDetail
import com.example.greenbuyapp.databinding.ActivityAdminOrderDetailBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AdminOrderDetailActivity : BaseActivity<ActivityAdminOrderDetailBinding>() {

    override val viewModel: AdminOrderDetailViewModel by viewModel()
    
    override val binding: ActivityAdminOrderDetailBinding by lazy {
        ActivityAdminOrderDetailBinding.inflate(layoutInflater)
    }
    
    private lateinit var orderItemAdapter: AdminOrderItemAdapter
    private var orderId: Int = -1

    companion object {
        private const val EXTRA_ORDER_ID = "extra_order_id"
        
        fun createIntent(context: Context, orderId: Int): Intent {
            return Intent(context, AdminOrderDetailActivity::class.java).apply {
                putExtra(EXTRA_ORDER_ID, orderId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        
        // ✅ Get order ID from intent
        orderId = intent.getIntExtra(EXTRA_ORDER_ID, -1)
        if (orderId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // ✅ Load order detail
        viewModel.loadOrderDetail(orderId)
    }

    override fun initViews() {
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
    }

    override fun observeViewModel() {
        observeOrderDetail()
        observeLoadingState()
        observeErrorState()
        observeSuccessState()
        observeUpdateStatusState()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Chi tiết đơn hàng"
        }
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        orderItemAdapter = AdminOrderItemAdapter()
        
        binding.rvOrderItems.apply {
            layoutManager = LinearLayoutManager(this@AdminOrderDetailActivity)
            adapter = orderItemAdapter
            
            // ✅ Performance optimizations
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners() {
        // ✅ Update status button
        binding.btnUpdateStatus.setOnClickListener {
            showUpdateStatusDialog()
        }
        
        // ✅ Refresh button
        binding.btnRefresh.setOnClickListener {
            viewModel.refreshOrderDetail()
        }
    }

    private fun observeOrderDetail() {
        lifecycleScope.launch {
            viewModel.orderDetail.collect { orderDetail ->
                orderDetail?.let { 
                    bindOrderDetail(it)
                }
            }
        }
    }

    private fun observeLoadingState() {
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                
                // ✅ Disable actions during loading
                binding.layoutActions.visibility = if (isLoading) View.GONE else View.VISIBLE
            }
        }
    }

    private fun observeErrorState() {
        lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    Toast.makeText(this@AdminOrderDetailActivity, it, Toast.LENGTH_LONG).show()
                    viewModel.clearErrorMessage()
                }
            }
        }
    }

    private fun observeSuccessState() {
        lifecycleScope.launch {
            viewModel.successMessage.collect { message ->
                message?.let {
                    Toast.makeText(this@AdminOrderDetailActivity, it, Toast.LENGTH_SHORT).show()
                    viewModel.clearSuccessMessage()
                }
            }
        }
    }

    private fun observeUpdateStatusState() {
        lifecycleScope.launch {
            viewModel.isUpdatingStatus.collect { isUpdating ->
                binding.btnUpdateStatus.isEnabled = !isUpdating
                binding.btnUpdateStatus.text = if (isUpdating) {
                    "Đang cập nhật..."
                } else {
                    viewModel.orderDetail.value?.getNextStatusDisplayName() ?: "Cập nhật trạng thái"
                }
            }
        }
    }

    private fun bindOrderDetail(orderDetail: AdminOrderDetail) {
        with(binding) {
            // ✅ Order info
            tvOrderNumber.text = orderDetail.orderNumber
            tvOrderStatus.text = orderDetail.statusDisplayName
            tvOrderStatus.setBackgroundColor(orderDetail.getStatusColor())
            
            // ✅ Customer info
            tvCustomerName.text = orderDetail.customerName
            tvCustomerContact.text = "${orderDetail.customerEmail} • ${orderDetail.customerPhone}"
            
            // ✅ Dates
            tvCreatedAt.text = orderDetail.formattedCreatedAt
            tvUpdatedAt.text = orderDetail.formattedUpdatedAt
            
            // ✅ Address
            tvShippingAddress.text = orderDetail.shippingAddress
            if (orderDetail.deliveryNotes.isNotEmpty()) {
                tvDeliveryNotes.text = "Ghi chú: ${orderDetail.deliveryNotes}"
                tvDeliveryNotes.visibility = View.VISIBLE
            } else {
                tvDeliveryNotes.visibility = View.GONE
            }
            
            // ✅ Payment info - Show payment section only if payment status exists
            if (orderDetail.paymentStatus != null) {
                tvPaymentStatus.text = orderDetail.paymentStatusDisplayName
                tvPaymentStatus.setBackgroundColor(orderDetail.getPaymentStatusColor())
                tvPaymentMethod.text = orderDetail.paymentMethod ?: "COD/Chuyển khoản"
                tvTransactionId.text = orderDetail.transactionId ?: "Chưa có mã giao dịch"
            } else {
                // ✅ Hide payment section if no payment info
                tvPaymentStatus.text = "Chưa thanh toán"
                tvPaymentStatus.setBackgroundColor(orderDetail.getPaymentStatusColor())
                tvPaymentMethod.text = "Thanh toán khi nhận hàng"
                tvTransactionId.text = "Không áp dụng"
            }
            
            // ✅ Order summary
            tvSubtotal.text = orderDetail.formattedSubtotal
            tvShippingFee.text = orderDetail.formattedShippingFee
            tvTotalAmount.text = orderDetail.formattedTotalAmount
            
            // ✅ Tax amount (show if > 0)
            if (orderDetail.taxAmount > 0) {
                layoutTax.visibility = View.VISIBLE
                tvTaxAmount.text = orderDetail.formattedTaxAmount
            } else {
                layoutTax.visibility = View.GONE
            }
            
            // ✅ Discount amount (show if > 0)
            if (orderDetail.discountAmount > 0) {
                layoutDiscount.visibility = View.VISIBLE
                tvDiscountAmount.text = "-${orderDetail.formattedDiscountAmount}"
            } else {
                layoutDiscount.visibility = View.GONE
            }
            
            // ✅ Notes
            val hasNotes = !orderDetail.notes.isNullOrEmpty() || !orderDetail.internalNotes.isNullOrEmpty()
            if (hasNotes) {
                layoutNotes.visibility = View.VISIBLE
                
                if (!orderDetail.notes.isNullOrEmpty()) {
                    tvNotes.text = orderDetail.notes
                    tvNotes.visibility = View.VISIBLE
                } else {
                    tvNotes.visibility = View.GONE
                }
                
                if (!orderDetail.internalNotes.isNullOrEmpty()) {
                    tvInternalNotes.text = "Ghi chú nội bộ: ${orderDetail.internalNotes}"
                    tvInternalNotes.visibility = View.VISIBLE
                } else {
                    tvInternalNotes.visibility = View.GONE
                }
            } else {
                layoutNotes.visibility = View.GONE
            }
            
            // ✅ Update status button
            if (orderDetail.canUpdateToNextStatus()) {
                btnUpdateStatus.text = orderDetail.getNextStatusDisplayName()
                btnUpdateStatus.visibility = View.VISIBLE
            } else {
                btnUpdateStatus.visibility = View.GONE
            }
        }
        
        // ✅ Bind order items - Handle empty items list
        if (orderDetail.items.isNotEmpty()) {
            binding.rvOrderItems.visibility = View.VISIBLE
            binding.layoutEmptyItems.visibility = View.GONE
            orderItemAdapter.submitList(orderDetail.items)
            println("✅ AdminOrderDetail: ${orderDetail.items.size} items loaded")
            orderDetail.items.forEachIndexed { index, item ->
                println("   Item $index: ${item.productName} - Image: ${item.productImage}")
            }
        } else {
            // ✅ Show empty state for items
            binding.rvOrderItems.visibility = View.GONE
            binding.layoutEmptyItems.visibility = View.VISIBLE
            orderItemAdapter.submitList(emptyList())
            println("⚠️ Order ${orderDetail.orderNumber} has no items")
            
            // ✅ TODO: For testing - create mock items (remove this in production)
            // val mockItems = listOf(
            //     AdminOrderItem(
            //         id = 1,
            //         productId = 1,
            //         attributeId = 1,
            //         productName = "Test Product",
            //         productImage = "/uploads/products/test.jpg",
            //         attributeDetails = "Màu: Đỏ | Size: L",
            //         quantity = 2,
            //         unitPrice = 100000,
            //         totalPrice = 200000,
            //         createdAt = "2025-07-12T10:00:00"
            //     )
            // )
            // orderItemAdapter.submitList(mockItems)
            // binding.rvOrderItems.visibility = View.VISIBLE
            // binding.layoutEmptyItems.visibility = View.GONE
        }
    }

    private fun showUpdateStatusDialog() {
        val orderDetail = viewModel.orderDetail.value ?: return
        
        if (!orderDetail.canUpdateToNextStatus()) {
            Toast.makeText(this, "Không thể cập nhật trạng thái đơn hàng này", Toast.LENGTH_SHORT).show()
            return
        }
        
        // ✅ Create dialog with input for internal notes
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_order_status, null)
        val etInternalNotes = dialogView.findViewById<TextInputEditText>(R.id.etInternalNotes)
        val tilInternalNotes = dialogView.findViewById<TextInputLayout>(R.id.tilInternalNotes)
        
        AlertDialog.Builder(this)
            .setTitle("Cập nhật trạng thái đơn hàng")
            .setMessage("Bạn có chắc chắn muốn cập nhật trạng thái đơn hàng thành \"${orderDetail.getNextStatusDisplayName()}\"?")
            .setView(dialogView)
            .setPositiveButton("Xác nhận") { _, _ ->
                val internalNotes = etInternalNotes.text?.toString()?.trim()
                viewModel.updateOrderToNextStatus(internalNotes)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
} 