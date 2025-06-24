package com.example.greenbuyapp.ui.product

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.product.model.ProductAttribute
import com.example.greenbuyapp.databinding.ActivityProductBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProductActivity : BaseActivity<ActivityProductBinding>() {

    override val viewModel: ProductViewModel by viewModel()
    
    override val binding: ActivityProductBinding by lazy {
        ActivityProductBinding.inflate(layoutInflater)
    }

    private var productId: Int = -1
    
    // Adapters
    private lateinit var imageAdapter: ProductImageAdapter
    private lateinit var indicatorAdapter: ProductIndicatorAdapter

    companion object {
        private const val EXTRA_PRODUCT_ID = "extra_product_id"
        
        fun createIntent(context: Context, productId: Int): Intent {
            println("🏭 Creating intent for productId: $productId")
            println("🏭 EXTRA_PRODUCT_ID key: $EXTRA_PRODUCT_ID")
            
            return Intent(context, ProductActivity::class.java).apply {
                putExtra(EXTRA_PRODUCT_ID, productId)
                println("🏭 Intent created with extras: ${this.extras}")
                println("🏭 Verifying extra value: ${this.getIntExtra(EXTRA_PRODUCT_ID, -999)}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Get product ID từ intent TRƯỚC KHI gọi super.onCreate()
        productId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)
        
        println("🔍 Intent extras: ${intent.extras}")
        println("🔍 EXTRA_PRODUCT_ID value: ${intent.getIntExtra(EXTRA_PRODUCT_ID, -999)}")
        println("🔍 ProductId received: $productId")
        
        if (productId == -1) {
            println("❌ Invalid product ID, closing activity")
            finish()
            return
        }
        
        println("📦 ProductActivity opened with ID: $productId")



        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun initViews() {
        setupUI()
        setupToolbar()
        setupViewPager()
        setupImageIndicator()
        loadProduct()
    }

    override fun observeViewModel() {
        observeProductAttributes()
        observeSelectedIndex()
        observeLoading()
        observeError()
    }

    private fun setupUI() {
        // Setup status bar cho product detail
        window.statusBarColor = ContextCompat.getColor(this, R.color.main_color)

        binding.apply {
            // Hiển thị product ID tạm thời
            tvProductId.text = "Product ID: $productId"
            
            // Setup button actions
//            btnAddToCart.setOnClickListener {
//                println("🛒 Add to cart clicked for product $productId")
//                // TODO: Add to cart logic
//            }
//
//            btnBuyNow.setOnClickListener {
//                println("💰 Buy now clicked for product $productId")
//                // TODO: Buy now logic
//            }
        }
    }

    private fun setupToolbar() {
        // Setup custom toolbar
        setSupportActionBar(binding.toolbar)
        
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Chi tiết sản phẩm"
        }
        
        // Handle back button click
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadProduct() {
        // Load product attributes từ API
        println("🔄 Loading product attributes for ID: $productId...")
        println("🔍 About to call API with productId: $productId")
        
        if (productId <= 0) {
            println("❌ Invalid productId: $productId, cannot load attributes")
            return
        }
        
        viewModel.loadProductAttributes(productId)
    }

    private fun setupViewPager() {
        imageAdapter = ProductImageAdapter()
        binding.vpProductImages.adapter = imageAdapter
        
        // Apply 3D depth effect transformer giống như trong hình
        // Có thể thay đổi hiệu ứng bằng cách comment/uncomment các dòng dưới:
        binding.vpProductImages.setPageTransformer(HeroCardPageTransformer())       // Giống hình user (Recommended)
        // binding.vpProductImages.setPageTransformer(EnhancedDepthPageTransformer()) // Enhanced depth
        // binding.vpProductImages.setPageTransformer(DepthPageTransformer())        // Simple depth
        // binding.vpProductImages.setPageTransformer(CubePageTransformer())         // Cube 3D
        // binding.vpProductImages.setPageTransformer(null)                          // No effect
        
        // Set offscreen page limit để hiệu ứng hoạt động mượt và có thể thấy các trang bên cạnh
        binding.vpProductImages.offscreenPageLimit = 3
        
        // Tạo effect để các trang bên cạnh visible
        binding.vpProductImages.clipToPadding = false
        binding.vpProductImages.clipChildren = false
        
        // Register ViewPager callback
        binding.vpProductImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.updateSelectedAttributeIndex(position)
                indicatorAdapter.updateSelectedPosition(position)
                println("📱 ViewPager page selected: $position")
            }
            
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                // Có thể thêm animation cho indicator ở đây nếu muốn
            }
        })
    }

    private fun setupImageIndicator() {
        indicatorAdapter = ProductIndicatorAdapter { position ->
            // Handle indicator click
            binding.vpProductImages.setCurrentItem(position, true)
        }
        
        binding.rvImageIndicator.apply {
            layoutManager = LinearLayoutManager(this@ProductActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = indicatorAdapter
        }
    }

    private fun observeProductAttributes() {
        lifecycleScope.launch {
            viewModel.productAttributes.collect { attributes ->
                if (attributes.isNotEmpty()) {
                    println("📦 Received ${attributes.size} product attributes")
                    
                    // Show normal UI
                    showProductContent()
                    
                    // Update adapters
                    imageAdapter.submitList(attributes)
                    indicatorAdapter.submitList(attributes)
                    
                    // Update UI với first attribute
                    updateProductInfo(attributes.first())
                } else {
                    println("📭 No product attributes available")
                    // Show fallback UI
                    showEmptyState()
                }
            }
        }
    }

    private fun showProductContent() {
        binding.apply {
            vpProductImages.visibility = View.VISIBLE
            rvImageIndicator.visibility = View.VISIBLE
            layoutAttributes.visibility = View.VISIBLE
//            layoutActions.visibility = View.VISIBLE
        }
    }

    private fun showEmptyState() {
        binding.apply {
            // Hide main content
            vpProductImages.visibility = View.GONE
            rvImageIndicator.visibility = View.GONE
            layoutAttributes.visibility = View.GONE
//            layoutActions.visibility = View.GONE
            
            // Show fallback info
            tvProductPrice.text = "Giá: Liên hệ"
            tvProductColor.text = "N/A"
            tvProductSize.text = "N/A" 
            tvProductQuantity.text = "N/A"
            tvProductDescription.text = "Sản phẩm này hiện chưa có thông tin chi tiết. Vui lòng liên hệ để biết thêm thông tin."
        }
    }

    private fun observeSelectedIndex() {
        lifecycleScope.launch {
            viewModel.selectedAttributeIndex.collect { index ->
                val currentAttribute = viewModel.getCurrentAttribute()
                currentAttribute?.let { 
                    updateProductInfo(it)
                    println("🔄 Selected attribute index: $index - ${it.color} ${it.size}")
                }
            }
        }
    }

    private fun observeLoading() {
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                // Show/hide loading indicator
                // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                
                if (isLoading) {
                    println("⏳ Loading product attributes...")
                } else {
                    println("✅ Loading completed")
                }
            }
        }
    }

    private fun observeError() {
        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    println("❌ Error: $it")
                    
                    // Check activity state trước khi show error
                    if (!isFinishing && !isDestroyed) {
                        showError(it)
                    } else {
                        println("⚠️ Activity is finishing/destroyed, skipping error dialog")
                    }
                    
                    viewModel.clearError()
                }
            }
        }
    }

    private fun updateProductInfo(attribute: ProductAttribute) {
        binding.apply {
            // Update price
            tvProductPrice.text = attribute.getFormattedPrice()
            
            // Update attributes
            tvProductColor.text = attribute.color.uppercase()
            tvProductSize.text = attribute.size.uppercase()
            tvProductQuantity.text = "${attribute.quantity} sp"
            
            // Update product ID for debugging
            tvProductId.text = "Product ID: ${attribute.product_id} | Attribute ID: ${attribute.attribute_id}"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // Handle back button trong toolbar
        onBackPressedDispatcher.onBackPressed()
        return true
    }
    
    override fun onDestroy() {
        super.onDestroy()
        println("🔄 ProductActivity onDestroy called")
    }
} 