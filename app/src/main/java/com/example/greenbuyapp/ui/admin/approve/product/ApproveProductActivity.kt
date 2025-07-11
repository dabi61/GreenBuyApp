package com.example.greenbuyapp.ui.admin.approve.product

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.product.model.PendingApprovalProduct
import com.example.greenbuyapp.databinding.ActivityApproveProductBinding
import com.example.greenbuyapp.util.loadAvatar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import android.widget.EditText
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.abs

class ApproveProductActivity : AppCompatActivity() {

    private val viewModel: ApproveProductViewModel by viewModel()
    private lateinit var binding: ActivityApproveProductBinding
    
    // ✅ Optimized swipe gesture variables
    private var initialX: Float = 0f
    private var initialY: Float = 0f
    private var isSwipeInProgress = false
    private val SWIPE_THRESHOLD = 120f
    private val ROTATION_FACTOR = 0.08f
    private val ALPHA_FACTOR = 0.002f

    // ✅ Animation optimizations
    private var swipeAnimator: ObjectAnimator? = null
    private var lastClickTime: Long = 0

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
            if (currentProduct != null && viewModel.validateCurrentProduct()) {
                showModernApprovalDialog(true, currentProduct.product_id)
            } else {
                showError("Sản phẩm không hợp lệ hoặc đã được xử lý")
                viewModel.refresh()
            }
        }

        // Reject button
        binding.btnReject.setOnClickListener {
            val currentProduct = viewModel.currentProduct.value
            if (currentProduct != null && viewModel.validateCurrentProduct()) {
                showModernApprovalDialog(false, currentProduct.product_id)
            } else {
                showError("Sản phẩm không hợp lệ hoặc đã được xử lý")
                viewModel.refresh()
            }
        }

        // ✅ Double tap để refresh với animation
        binding.cardProduct.setOnClickListener { 
            val clickTime = System.currentTimeMillis()
            if (clickTime - lastClickTime < 500) { // Double tap within 500ms
                performRefreshAnimation()
                viewModel.refresh()
                showSuccessMessage("🔄 Đã làm mới danh sách")
            }
            lastClickTime = clickTime
        }
    }

    private fun performRefreshAnimation() {
        val animator = ObjectAnimator.ofFloat(binding.cardProduct, "rotation", 0f, 360f)
        animator.duration = 600
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }

    private fun showModernApprovalDialog(isApprove: Boolean, productId: Int) {
        val title = if (isApprove) "Duyệt sản phẩm" else "Từ chối sản phẩm"
        val hint = if (isApprove) "Ghi chú duyệt (tùy chọn)" else "Lý do từ chối *"
        val defaultNote = if (isApprove) "Sản phẩm đạt tiêu chuẩn, được duyệt" else ""
        
        // ✅ Simple dialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_approval_note, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextNote)
        
        editText.hint = hint
        editText.setText(defaultNote)
        
        // ✅ Set colors based on action
        val primaryColor = if (isApprove) 
            ContextCompat.getColor(this, R.color.green_600) 
        else 
            ContextCompat.getColor(this, R.color.red_600)
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton(if (isApprove) "Duyệt" else "Từ chối") { _, _ ->
                val note = editText.text.toString().trim()
                
                // ✅ Validation for reject
                if (!isApprove && note.isEmpty()) {
                    showError("Vui lòng nhập lý do từ chối")
                    return@setPositiveButton
                }
                
                val finalNote = if (note.isEmpty()) defaultNote else note
                
                // ✅ Double-check product validity before submitting
                if (!viewModel.validateCurrentProduct()) {
                    showError("Sản phẩm đã được xử lý bởi admin khác")
                    viewModel.refresh()
                    return@setPositiveButton
                }
                
                if (isApprove) {
                    viewModel.approveProduct(productId, finalNote)
                } else {
                    viewModel.rejectProduct(productId, finalNote)
                }
                
                // ✅ Improved card animation
                animateCardSwipeSmooth(isApprove)
            }
            .setNegativeButton("Hủy", null)
            .create()

        // ✅ Dialog styling
        dialog.show()
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(primaryColor)
        dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
            ContextCompat.getColor(this, R.color.grey_600)
        )
        
        // ✅ Auto focus
        editText.requestFocus()
        editText.postDelayed({
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) 
                as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(editText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    private fun setupSwipeGesture() {
        binding.cardProduct.setOnTouchListener { view, event ->
            if (isSwipeInProgress) return@setOnTouchListener true
            
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.x
                    initialY = event.y
                    // ✅ Cancel any ongoing animations
                    swipeAnimator?.cancel()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.x - initialX
                    val deltaY = event.y - initialY
                    
                    // ✅ Only process horizontal swipes
                    if (abs(deltaX) > abs(deltaY)) {
                        // ✅ Smooth movement with optimized calculations
                        view.translationX = deltaX
                        
                        // ✅ Dynamic rotation based on swipe distance
                        val rotation = deltaX * ROTATION_FACTOR
                        view.rotation = rotation.coerceIn(-30f, 30f)
                        
                        // ✅ Dynamic alpha for better visual feedback
                        val alpha = 1f - abs(deltaX) * ALPHA_FACTOR
                        view.alpha = alpha.coerceAtLeast(0.3f)
                        
                        // ✅ Background color hint
                        updateCardBackgroundHint(deltaX)
                        
                        // ✅ Scale effect for depth
                        val scale = 1f - abs(deltaX) * 0.0005f
                        view.scaleX = scale.coerceAtLeast(0.8f)
                        view.scaleY = scale.coerceAtLeast(0.8f)
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val deltaX = event.x - initialX
                    
                    if (abs(deltaX) > SWIPE_THRESHOLD) {
                        // ✅ Trigger swipe action
                        val currentProduct = viewModel.currentProduct.value
                        currentProduct?.let { product ->
                            isSwipeInProgress = true
                            if (deltaX > 0) {
                                showModernApprovalDialog(true, product.product_id)
                            } else {
                                showModernApprovalDialog(false, product.product_id)
                            }
                        }
                    } else {
                        // ✅ Smooth reset animation
                        resetCardPositionSmooth()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun updateCardBackgroundHint(deltaX: Float) {
        val card = binding.cardProduct as MaterialCardView
        val alpha = (abs(deltaX) / 200f).coerceAtMost(0.3f)
        
        if (deltaX > 0) {
            // Approve hint - green
            card.setCardBackgroundColor(
                Color.argb((alpha * 255).toInt(), 0, 200, 0)
            )
        } else {
            // Reject hint - red
            card.setCardBackgroundColor(
                Color.argb((alpha * 255).toInt(), 200, 0, 0)
            )
        }
    }

    private fun animateCardSwipeSmooth(isApprove: Boolean) {
        isSwipeInProgress = true
        val card = binding.cardProduct
        val screenWidth = resources.displayMetrics.widthPixels
        val targetX = if (isApprove) screenWidth.toFloat() else -screenWidth.toFloat()
        
        // ✅ Create smooth property animator
        val translationX = PropertyValuesHolder.ofFloat("translationX", card.translationX, targetX)
        val rotation = PropertyValuesHolder.ofFloat("rotation", card.rotation, if (isApprove) 45f else -45f)
        val alpha = PropertyValuesHolder.ofFloat("alpha", card.alpha, 0f)
        val scaleX = PropertyValuesHolder.ofFloat("scaleX", card.scaleX, 0.8f)
        val scaleY = PropertyValuesHolder.ofFloat("scaleY", card.scaleY, 0.8f)
        
        swipeAnimator = ObjectAnimator.ofPropertyValuesHolder(card, translationX, rotation, alpha, scaleX, scaleY)
        swipeAnimator?.apply {
            duration = 400
            interpolator = DecelerateInterpolator(1.5f)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // ✅ Reset card và cho phép swipe tiếp
                    resetCardPositionImmediate()
                    isSwipeInProgress = false
                    
                    // ✅ Đảm bảo UI được cập nhật nếu có sản phẩm mới
                    val currentProduct = viewModel.currentProduct.value
                    if (currentProduct != null) {
                        updateProductInfo(currentProduct)
                    }
                }
            })
            start()
        }
    }

    private fun resetCardPositionSmooth() {
        val card = binding.cardProduct
        
        // ✅ Smooth reset animation
        val translationX = PropertyValuesHolder.ofFloat("translationX", card.translationX, 0f)
        val rotation = PropertyValuesHolder.ofFloat("rotation", card.rotation, 0f)
        val alpha = PropertyValuesHolder.ofFloat("alpha", card.alpha, 1f)
        val scaleX = PropertyValuesHolder.ofFloat("scaleX", card.scaleX, 1f)
        val scaleY = PropertyValuesHolder.ofFloat("scaleY", card.scaleY, 1f)
        
        val resetAnimator = ObjectAnimator.ofPropertyValuesHolder(card, translationX, rotation, alpha, scaleX, scaleY)
        resetAnimator.apply {
            duration = 200
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // Reset background color
                    (card as MaterialCardView).setCardBackgroundColor(
                        ContextCompat.getColor(this@ApproveProductActivity, R.color.white)
                    )
                }
            })
            start()
        }
    }

    private fun resetCardPositionImmediate() {
        binding.cardProduct.apply {
            translationX = 0f
            translationY = 0f
            rotation = 0f
            alpha = 1f
            scaleX = 1f
            scaleY = 1f
        }
        
        // Reset background color
        (binding.cardProduct as MaterialCardView).setCardBackgroundColor(
            ContextCompat.getColor(this@ApproveProductActivity, R.color.white)
        )
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
                println("🔄 Current product changed: ${product?.product_id}")
                println("📊 ${viewModel.debugCurrentState()}")
                
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
        card.alpha = 0f
        card.scaleX = 0.8f
        card.scaleY = 0.8f
        
        val alpha = PropertyValuesHolder.ofFloat("alpha", 0f, 1f)
        val scaleX = PropertyValuesHolder.ofFloat("scaleX", 0.8f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat("scaleY", 0.8f, 1f)
        
        val animator = ObjectAnimator.ofPropertyValuesHolder(card, alpha, scaleX, scaleY)
        animator.duration = 300
        animator.interpolator = DecelerateInterpolator()
        animator.start()
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
                        setButtonsEnabled(false)
                        showProgressIndicator(true)
                    }
                    is ApprovalState.Success -> {
                        setButtonsEnabled(true)
                        showProgressIndicator(false)
                        
                        val message = if (state.isApproved) "Đã duyệt sản phẩm" else "Đã từ chối sản phẩm"
                        showSuccessMessage(message)
                        
                        performHapticFeedback()
                        
                        // ✅ Product đã được chuyển tự động trong ViewModel, không cần delay
                        // Reset approval state sau khi xử lý xong
                        lifecycleScope.launch {
                            kotlinx.coroutines.delay(500) // Delay ngắn để user thấy message
                            viewModel.clearError() // Reset state về Idle
                        }
                    }
                    is ApprovalState.Error -> {
                        setButtonsEnabled(true)
                        showProgressIndicator(false)
                        resetCardPositionImmediate()
                        isSwipeInProgress = false
                    }
                    else -> {
                        setButtonsEnabled(true)
                        showProgressIndicator(false)
                        isSwipeInProgress = false
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
                
                supportActionBar?.subtitle = viewModel.getStatusSummary()
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
                
                if (total == 0) {
                    showEmptyState()
                }
            }
        }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.btnApprove.isEnabled = enabled
        binding.btnReject.isEnabled = enabled
    }

    private fun showProgressIndicator(show: Boolean) {
        if (show) {
            showLoadingState()
        } else {
            hideLoadingState()
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

    private fun updateProductInfo(product: PendingApprovalProduct) {
        binding.apply {
            try {
                // Product name
                tvProductName.text = product.name.takeIf { it.isNotBlank() } ?: "Tên sản phẩm không rõ"
                
                // Product price
                tvProductPrice.text = try {
                    "Giá: ${product.getFormattedPrice()}"
                } catch (e: Exception) {
                    "Giá: Không rõ"
                }
                
                // ✅ Product image with better error handling
                loadProductImage(product.cover)
                
                // ✅ Show additional info in toolbar subtitle
                supportActionBar?.subtitle = "ID: ${product.product_id} | Chờ duyệt"
                
                // ✅ Load shop info
                updateShopInfo(product.shopInfo)
                
            } catch (e: Exception) {
                println("❌ Error updating product info: ${e.message}")
                showError("Lỗi hiển thị thông tin sản phẩm")
            }
        }
    }

    private fun loadProductImage(coverPath: String?) {
        try {
            val imageUrl = when {
                coverPath.isNullOrBlank() -> null
                coverPath.startsWith("http") -> coverPath
                else -> "https://www.utt-school.site$coverPath"
            }
            
            val glideRequest = Glide.with(this@ApproveProductActivity)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_product)
                .error(R.drawable.placeholder_product)
                .timeout(10000)
            
            glideRequest.listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                override fun onLoadFailed(
                    e: com.bumptech.glide.load.engine.GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    println("❌ Failed to load product image: ${e?.message}")
                    return false
                }
                
                override fun onResourceReady(
                    resource: android.graphics.drawable.Drawable?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    println("✅ Product image loaded successfully")
                    return false
                }
            })
            
            glideRequest.into(binding.ivProductImage)
            
        } catch (e: Exception) {
            println("❌ Exception loading product image: ${e.message}")
            binding.ivProductImage.setImageResource(R.drawable.placeholder_product)
        }
    }

    private fun updateShopInfo(shop: com.example.greenbuyapp.data.shop.model.Shop?) {
        binding.apply {
            if (shop != null) {
                tvShopName.text = shop.name.takeIf { it.isNotBlank() } ?: "Tên shop không rõ"
                
                // Load shop avatar
                try {
                    ivShopAvatar.loadAvatar(
                        avatarPath = shop.avatar,
                        placeholder = R.drawable.avatar_blank,
                        error = R.drawable.avatar_blank
                    )
                } catch (e: Exception) {
                    println("❌ Error loading shop avatar: ${e.message}")
                    ivShopAvatar.setImageResource(R.drawable.avatar_blank)
                }
            } else {
                tvShopName.text = "Đang tải thông tin shop..."
                ivShopAvatar.setImageResource(R.drawable.avatar_blank)
            }
        }
    }

    private fun showError(message: String) {
        try {
            val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.red_600))
            snackbar.setTextColor(ContextCompat.getColor(this, R.color.white))
            snackbar.setAction("Đóng") { snackbar.dismiss() }
            snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.white))
            snackbar.show()
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun showSuccessMessage(message: String) {
        try {
            val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.green_600))
            snackbar.setTextColor(ContextCompat.getColor(this, R.color.white))
            snackbar.show()
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun performHapticFeedback() {
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

    override fun onDestroy() {
        super.onDestroy()
        swipeAnimator?.cancel()
    }
}