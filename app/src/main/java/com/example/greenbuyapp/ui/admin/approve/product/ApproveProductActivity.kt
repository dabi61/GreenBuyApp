package com.example.greenbuyapp.ui.admin.approve.product

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.product.model.Product
import com.example.greenbuyapp.databinding.ActivityApproveProductBinding
import com.example.greenbuyapp.util.loadAvatar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.abs

class ApproveProductActivity : AppCompatActivity() {

    private val viewModel: ApproveProductViewModel by viewModel()
    private lateinit var binding: ActivityApproveProductBinding
    
    // Swipe gesture variables
    private var initialX: Float = 0f
    private var initialY: Float = 0f
    private val SWIPE_THRESHOLD = 100f
    private val SWIPE_VELOCITY_THRESHOLD = 100f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        binding = ActivityApproveProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup status bar
        window.statusBarColor = ContextCompat.getColor(this, R.color.main_color)
        
        setupToolbar()
        setupClickListeners()
        setupSwipeGesture()
        observeViewModel()
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Duyệt sản phẩm"
        }
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupClickListeners() {
        // Approve button
        binding.btnApprove.setOnClickListener {
            val currentProduct = viewModel.currentProduct.value
            currentProduct?.let { product ->
                showApprovalDialog(true, product.product_id)
            }
        }

        // Reject button
        binding.btnReject.setOnClickListener {
            val currentProduct = viewModel.currentProduct.value
            currentProduct?.let { product ->
                showApprovalDialog(false, product.product_id)
            }
        }
    }

    private fun showApprovalDialog(isApprove: Boolean, productId: Int) {
        val title = if (isApprove) "Duyệt sản phẩm" else "Từ chối sản phẩm"
        var hint = if (isApprove) "Ghi chú duyệt (tùy chọn)" else "Lý do từ chối"
        val defaultNote = if (isApprove) "Đã duyệt" else "Đã từ chối"
        
        val editText = android.widget.EditText(this).apply {
            hint = hint
            setText(defaultNote)
        }
        
        android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setView(editText)
            .setPositiveButton("Xác nhận") { _, _ ->
                val note = editText.text.toString().ifEmpty { defaultNote }
                if (isApprove) {
                    viewModel.approveProduct(productId, note)
                    animateCardSwipe(true)
                } else {
                    viewModel.rejectProduct(productId, note)
                    animateCardSwipe(false)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun setupSwipeGesture() {
        binding.cardProduct.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.x
                    initialY = event.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.x - initialX
                    val deltaY = event.y - initialY
                    
                    // Move card with finger
                    view.translationX = deltaX
                    view.translationY = deltaY
                    
                    // Rotate card based on swipe direction
                    val rotation = deltaX * 0.1f
                    view.rotation = rotation
                    
                    // Change alpha based on swipe distance
                    val alpha = 1f - abs(deltaX) / 500f
                    view.alpha = alpha.coerceAtLeast(0.3f)
                    
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = event.x - initialX
                    val deltaY = event.y - initialY
                    
                    if (abs(deltaX) > SWIPE_THRESHOLD) {
                        // Swipe right = approve, Swipe left = reject
                        val currentProduct = viewModel.currentProduct.value
                        currentProduct?.let { product ->
                            if (deltaX > 0) {
                                showApprovalDialog(true, product.product_id)
                            } else {
                                showApprovalDialog(false, product.product_id)
                            }
                        }
                    } else {
                        // Reset card position
                        resetCardPosition()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun animateCardSwipe(isApprove: Boolean) {
        val card = binding.cardProduct
        val screenWidth = resources.displayMetrics.widthPixels
        val targetX = if (isApprove) screenWidth.toFloat() else -screenWidth.toFloat()
        
        card.animate()
            .translationX(targetX)
            .translationY(0f)
            .rotation(if (isApprove) 30f else -30f)
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                resetCardPosition()
            }
            .start()
    }

    private fun resetCardPosition() {
        binding.cardProduct.apply {
            translationX = 0f
            translationY = 0f
            rotation = 0f
            alpha = 1f
        }
    }

    private fun observeViewModel() {
        observeCurrentProduct()
        observeLoading()
        observeError()
        observeApprovalState()
        observeStats()
        observeProgress()
    }

    private fun observeCurrentProduct() {
        lifecycleScope.launch {
            viewModel.currentProduct.collect { product ->
                if (product != null) {
                    showProductCard()
                    updateProductInfo(product)
                    // Add entrance animation
                    animateCardEntrance()
                } else {
                    showEmptyState()
                }
            }
        }
    }

    private fun animateCardEntrance() {
        val card = binding.cardProduct
        val animation = AnimationUtils.loadAnimation(this, R.anim.item_animation_scale_in)
        card.startAnimation(animation)
    }

    private fun observeLoading() {
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                if (isLoading) {
                    showLoadingState()
                } else {
                    hideLoadingState()
                }
            }
        }
    }

    private fun observeError() {
        lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    showError(it)
                    viewModel.clearError()
                }
            }
        }
    }

    private fun observeApprovalState() {
        lifecycleScope.launch {
            viewModel.approvalState.collect { state ->
                when (state) {
                    is ApprovalState.Loading -> {
                        // Disable buttons during loading
                        binding.btnApprove.isEnabled = false
                        binding.btnReject.isEnabled = false
                    }
                    is ApprovalState.Success -> {
                        // Re-enable buttons
                        binding.btnApprove.isEnabled = true
                        binding.btnReject.isEnabled = true
                        
                        // Show success message with animation
                        val message = if (state.isApproved) "Đã duyệt sản phẩm" else "Đã từ chối sản phẩm"
                        showSuccessMessage(message)
                        
                        // Add haptic feedback
                        performHapticFeedback()
                    }
                    is ApprovalState.Error -> {
                        // Re-enable buttons
                        binding.btnApprove.isEnabled = true
                        binding.btnReject.isEnabled = true
                    }
                    else -> {
                        // Re-enable buttons
                        binding.btnApprove.isEnabled = true
                        binding.btnReject.isEnabled = true
                    }
                }
            }
        }
    }

    private fun observeStats() {
        lifecycleScope.launch {
            viewModel.stats.collect { stats ->
                binding.tvApprovedCount.text = stats.approvedCount.toString()
                binding.tvRejectedCount.text = stats.rejectedCount.toString()
                binding.tvTotalCount.text = stats.totalProcessed.toString()
            }
        }
    }

    private fun observeProgress() {
        lifecycleScope.launch {
            viewModel.pendingProducts.collect { products ->
                val currentIndex = viewModel.currentIndex.value
                val total = products.size
                val current = if (total > 0) currentIndex + 1 else 0
                
                binding.tvProgress.text = "$current / $total"
                binding.progressIndicator.max = total
                binding.progressIndicator.progress = current
            }
        }
    }

    private fun showProductCard() {
        binding.apply {
            layoutLoading.visibility = View.GONE
            layoutEmpty.visibility = View.GONE
            cardProduct.visibility = View.VISIBLE
        }
    }

    private fun showEmptyState() {
        binding.apply {
            layoutLoading.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
            cardProduct.visibility = View.GONE
        }
    }

    private fun showLoadingState() {
        binding.apply {
            layoutLoading.visibility = View.VISIBLE
            layoutEmpty.visibility = View.GONE
            cardProduct.visibility = View.GONE
        }
    }

    private fun hideLoadingState() {
        binding.layoutLoading.visibility = View.GONE
    }

    private fun updateProductInfo(product: Product) {
        binding.apply {
            // Product name
            tvProductName.text = product.name
            
            // Product price
            tvProductPrice.text = "Giá: ${product.getFormattedPrice()}"
            
            // Product image - thêm base URL
            val imageUrl = if (product.cover?.startsWith("http") == true) {
                product.cover
            } else {
                "https://www.utt-school.site${product.cover}"
            }
            
            Glide.with(this@ApproveProductActivity)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_product)
                .error(R.drawable.placeholder_product)
                .into(ivProductImage)
            
            // Shop info
            tvShopName.text = product.shop?.name ?: "Không xác định"
            ivShopAvatar.loadAvatar(
                avatarPath = product.shop?.avatar,
                placeholder = R.drawable.avatar_blank,
                error = R.drawable.avatar_blank
            )
        }
    }

    private fun showError(message: String) {
        // Show error using Snackbar or Toast
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    }

    private fun showSuccessMessage(message: String) {
        // Show success message
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun performHapticFeedback() {
        // Add haptic feedback for better UX
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val haptic = getSystemService(android.os.Vibrator::class.java)
            haptic?.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val haptic = getSystemService(android.os.Vibrator::class.java)
            haptic?.vibrate(50)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}