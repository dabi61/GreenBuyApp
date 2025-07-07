package com.example.greenbuyapp.ui.product

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.product.model.ProductAttribute
import com.example.greenbuyapp.databinding.ActivityProductBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.ui.home.HomeViewModel
import com.example.greenbuyapp.ui.home.ProductAdapter
import com.example.greenbuyapp.ui.shop.shopDetail.ShopDetailActivity
import com.example.greenbuyapp.util.loadAvatar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProductActivity : BaseActivity<ActivityProductBinding>() {

    override val viewModel: ProductViewModel by viewModel()
    private val productViewModel : HomeViewModel by viewModel()

    private lateinit var productAdapter: ProductAdapter

    override val binding: ActivityProductBinding by lazy {
        ActivityProductBinding.inflate(layoutInflater)
    }

    private var productId: Int = -1
    private var shopId: Int = -1
    private var description: String = ""
    
    // Adapters
    private lateinit var imageAdapter: ProductImageAdapter
    private lateinit var indicatorAdapter: ProductIndicatorAdapter

    companion object {
        private const val EXTRA_PRODUCT_ID = "extra_product_id"
        private const val EXTRA_SHOP_ID = "extra_shop_id"
        private const val EXTRA_DESCRIPTION = "extra_description"
        
        fun createIntent(context: Context, productId: Int, shopId: Int, description: String): Intent {
            println("🏭 Creating intent for productId: $productId")
            println("🏭 EXTRA_PRODUCT_ID key: $EXTRA_PRODUCT_ID")
            
            return Intent(context, ProductActivity::class.java).apply {
                putExtra(EXTRA_PRODUCT_ID, productId)
                putExtra(EXTRA_SHOP_ID, shopId)
                putExtra(EXTRA_DESCRIPTION, description)

                println("🏭 Intent created with extras: ${this.extras}")
                println("🏭 Verifying extra value: ${this.getIntExtra(EXTRA_PRODUCT_ID, -999)}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Get product ID từ intent TRƯỚC KHI gọi super.onCreate()
        productId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)
        shopId = intent.getIntExtra(EXTRA_SHOP_ID, -1)
        description = intent.getStringExtra(EXTRA_DESCRIPTION) ?: "Sản phẩm này hiện chưa có thông tin chi tiết. Vui lòng liên hệ để biết thêm thông tin."
        
        println("🔍 Intent extras: ${intent.extras}")
        println("🔍 EXTRA_PRODUCT_ID value: ${intent.getIntExtra(EXTRA_PRODUCT_ID, -999)}")
        println("🔍 ProductId received: $productId")
        
        if (productId == -1) {
            println("❌ Invalid product ID, closing activity")
            finish()
            return
        }

        if (shopId == -1) {
            println("❌ Invalid shop ID, closing activity")
            finish()
            return
        }


        println("📦 ProductActivity opened with ID: $productId")
        println("📦 ProductActivity opened with ID: $shopId")



        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun initViews() {
        setupUI()
        setupToolbar()
        setupViewPager()
        setupImageIndicator()
        setupRecyclerView()
        loadProduct()
        loadShop()
        openShopDetail()
        //Load Product
        productViewModel.loadProducts()

    }

    private fun openShopDetail() {
        binding.apply {
            ivShop.setOnClickListener {
                val intent = ShopDetailActivity.createIntent(this@ProductActivity, shopId)
                startActivity(intent)
            }
            tvShopName.setOnClickListener {
                val intent = ShopDetailActivity.createIntent(this@ProductActivity, shopId)
                startActivity(intent)
            }
            btShopView.setOnClickListener {
                val intent = ShopDetailActivity.createIntent(this@ProductActivity, shopId)
                startActivity(intent)
            }
        }

    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter { product ->
            // Handle product click - mở ProductActivity
            println("🚀 Opening product ${product.product_id} in ProductActivity")
            println("🔍 Product object: $product")
            println("🔍 Product ID being passed: ${product.product_id}")

            val intent = createIntent(this, product.product_id, product.shop_id, product.description)
            println("🔍 Intent created: $intent")
            println("🔍 Intent extras after creation: ${intent.extras}")

            startActivity(intent)
            println("✅ ProductActivity started")
        }
        binding.rvProduct.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productAdapter
        }
    }

    override fun observeViewModel() {
        observeProductAttributes()
        observeShop()
        observeSelectedIndex()
        observeLoading()
        observeError()
        observeProductsViewModel()
    }

    private fun observeShop() {
        lifecycleScope.launch {
            viewModel.shop.collect { shop ->
                // Hiển thị avatar
                binding.ivShop.loadAvatar(
                    avatarPath =  shop?.avatar,
                    placeholder =  R.drawable.avatar_blank,
                    error =  R.drawable.avatar_blank
                )

                // Hiển thị tên shop
                binding.tvShopName.text = shop?.name

            }
        }
    }

    private fun observeProductsViewModel() {
        observeProduct()
    }

    private fun observeProduct() {
        lifecycleScope.launch {
            productViewModel.products.collect { products ->
                println("🛍️ Products Activity updated: ${products.size} items")
                productAdapter.submitList(products)

                // Debug: Print first few products
                products.take(3).forEach { product ->
                    println("   Product: ${product.name}")
                }
            }
        }
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

    private fun loadShop() {

        println("🔄 Loading shop for ID: $shopId...")
        println("🔍 About to call API with shopId: $shopId")

        if (shopId <= 0) {
            println("❌ Invalid productId: $shopId, cannot load shop")
            return
        }

        viewModel.getShopById(shopId)
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
            tvProductDescription.text = description
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