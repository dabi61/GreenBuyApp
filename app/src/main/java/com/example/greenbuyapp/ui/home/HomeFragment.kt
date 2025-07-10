package com.example.greenbuyapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.FragmentHomeBinding
import com.example.greenbuyapp.ui.base.BaseFragment
import com.example.greenbuyapp.ui.product.ProductActivity
import com.example.greenbuyapp.ui.cart.CartActivity
import com.example.greenbuyapp.util.NetworkState
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.os.Handler
import android.os.Looper
import com.example.greenbuyapp.ui.product.trending.TrendingProductActivity
import com.example.greenbuyapp.ui.product.trending.TrendingProductViewModel
import kotlinx.coroutines.flow.combineTransform

class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    override val viewModel: HomeViewModel by viewModel()
    
    private lateinit var productAdapter: ProductAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var trendingAdapter: TrendingAdapter
    private lateinit var bannerAdapter: BannerAdapter
    
    // ✅ Sử dụng Handler thay vì Timer để tránh ANR
    private val bannerHandler = Handler(Looper.getMainLooper())
    private var bannerRunnable: Runnable? = null
    private var isUserScrolling = false

    override fun getLayoutResourceId(): Int = R.layout.fragment_home

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        // Khởi tạo các view và thiết lập sự kiện
        try {
            setupRecyclerView()
            setupBanner()
            setupSearchView()
            
            // Load categories khi init
            viewModel.loadCategories()
            
            // Load products với StateFlow architecture
            println("🚀 Triggering product loading...")
            viewModel.loadProducts(isRefresh = true)
            
            // Load trending products
            viewModel.loadTrendingProducts()
            
            // Load banner items
            viewModel.loadBannerItems()
            //chuyen sang form TrendingProductActivity
            binding.contraintTrending.setOnClickListener {
                val intent = Intent(requireContext(), TrendingProductActivity::class.java)
                startActivity(intent)

            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun observeViewModel() {
        // Observe các StateFlow từ ViewModel
        try {
            observeProducts()
            observeCategories()
            observeTrendingProducts()
            observeBanner()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun setupRecyclerView() {
        productAdapter = ProductAdapter { product ->
            // Handle product click - mở ProductActivity
            println("🚀 Opening product ${product.product_id} in ProductActivity")
            println("🔍 Product object: $product")
            println("🔍 Product ID being passed: ${product.product_id}")
            
            val intent = ProductActivity.createIntent(requireContext(), product.product_id, product.shop_id, product.description)
            println("🔍 Intent created: $intent")
            println("🔍 Intent extras after creation: ${intent.extras}")
            
            startActivity(intent)
            println("✅ ProductActivity started")
        }
        
        categoryAdapter = CategoryAdapter { category ->
            // Handle category click
            println("Category clicked: ${category.name}")
            viewModel.updateCategoryId(category.id)
        }

        trendingAdapter = TrendingAdapter { trendingProduct ->
            val intent = ProductActivity.createIntent(
                requireContext(),
                trendingProduct.product_id,
                trendingProduct.shop_id,
                trendingProduct.description
            )
            startActivity(intent)

        }
        
        binding.rvProduct.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productAdapter
        }

        // ✅ Setup category RecyclerView with horizontal orientation
        binding.rvCategory.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }

        // ✅ Setup trending RecyclerView with horizontal orientation
        binding.rvTrending.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = trendingAdapter
        }
    }

    private fun setupBanner() {
        // ✅ Null check
        if (!isAdded || activity == null) return
        
        // Setup banner adapter
        bannerAdapter = BannerAdapter { banner ->
            // Handle banner click
            println("Banner clicked: ${banner}")
            // TODO: Handle banner action
        }
        
        // Setup ViewPager2
        binding.bannerView.apply {
            adapter = bannerAdapter
            offscreenPageLimit = 3
            
            // Register page change callback để detect user scrolling
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    isUserScrolling = when (state) {
                        ViewPager2.SCROLL_STATE_DRAGGING -> {
                            stopAutoScroll()
                            true
                        }
                        ViewPager2.SCROLL_STATE_IDLE -> {
                            startAutoScroll()
                            false
                        }
                        else -> isUserScrolling
                    }
                }
                
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    // ✅ Null check cho binding
                    if (isAdded && isBindingInitialized()) {
                    binding.indicatorView.onPageSelected(position)
                    }
                }
            })
        }
        
        // Setup indicator
        binding.indicatorView.apply {
            setSlideMode(IndicatorSlideMode.WORM)
            setIndicatorStyle(IndicatorStyle.ROUND_RECT)
            setSliderColor(
                ContextCompat.getColor(requireContext(), R.color.grey_400),
                ContextCompat.getColor(requireContext(), R.color.green_900)
            )
            setSliderWidth(30f)
            setSliderHeight(12f)
            setSlideMode(IndicatorSlideMode.WORM)
            setupWithViewPager(binding.bannerView)
        }
    }
    
    private fun setupSearchView() {
        binding.svProduct.addTextChangedListener { text ->
            viewModel.updateSearchQuery(text?.toString() ?: "")
        }


        // Setup cart button click
        binding.icCart.setOnClickListener {
            val intent = CartActivity.createIntent(requireContext())
            startActivity(intent)
            println("🛒 Opening CartActivity")
        }
    }
    
    /**
     * Observe products với StateFlow architecture
     */
    private fun observeProducts() {
        // Observe product data
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.products.collect { products ->
                println("🛍️ Products updated: ${products.size} items")
                productAdapter.submitList(products)
                
                // Debug: Print first few products
                products.take(3).forEach { product ->
                    println("   Product: ${product.name}")
                }
            }
        }
        
        // Observe loading state  
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.productsLoading.collect { isLoading ->
                println("⏳ Products loading: $isLoading")
                // TODO: Show/hide loading indicator
                // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
        
        // Observe error state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.productsError.collect { error ->
                error?.let {
                    println("❌ Products error: $it")
                    // TODO: Show error message và retry button
                    // Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeCategories() {
        // Observe categories data using StateFlow
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collect { categories ->
                categoryAdapter.submitList(categories)
                
                // Tạm thời log để test
                println("Categories loaded: ${categories.size}")
                categories.forEach { category ->
                    println("Category: ${category.name}")
                }
            }
        }
        
        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categoriesLoading.collect { isLoading ->
                // Show/hide loading indicator
                if (isLoading) {
                    println("Loading categories...")
                } else {
                    println("Categories loading finished")
                }
            }
        }
        
        // Observe error state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categoriesError.collect { error ->
                error?.let {
                    println("Categories error: $it")
                    // TODO: Show error message
                    // Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeTrendingProducts() {
        // Observe trending products
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.trendingProducts.collect { trendingProducts ->
                // Update UI với trending products
                trendingAdapter.submitList(trendingProducts)
                println("Trending products updated: ${trendingProducts.size}")
            }
        }
        
        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.trendingLoading.collect { isLoading ->
                // Show/hide loading indicator
                if (isLoading) {
                    println("Loading trending products...")
                } else {
                    println("Trending products loading finished")
                }
            }
        }
        
        // Observe error state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.trendingError.collect { error ->
                error?.let {
                    println("Trending products error: $it")
                    // Show error message
                }
            }
        }
    }

    private fun observeBanner() {
        // ✅ Null check trước khi observe
        if (!isAdded) return
        
        // Observe banner items
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bannerItems.collect { bannerItems ->
                // ✅ Null check trong collect
                if (isAdded && isBindingInitialized() && ::bannerAdapter.isInitialized) {
                bannerAdapter.submitList(bannerItems)
                
                // Setup indicator với số lượng items
                if (bannerItems.isNotEmpty()) {
                    binding.indicatorView.setPageSize(bannerItems.size)
                    startAutoScroll()
                }
                
                println("Banner items updated: ${bannerItems.size}")
                }
            }
        }
    }

    /**
     * ✅ Bắt đầu auto scroll với Handler thay vì Timer
     */
    private fun startAutoScroll() {
        // ✅ Null checks
        if (!isAdded || activity == null || !isBindingInitialized() || !::bannerAdapter.isInitialized) {
            return
        }
        
        stopAutoScroll() // Stop existing handler first

        bannerRunnable = object : Runnable {
            override fun run() {
                try {
                    // ✅ Kiểm tra lifecycle trước khi update UI
                    if (isAdded && activity != null && !isUserScrolling && 
                        isBindingInitialized() && bannerAdapter.itemCount > 0) {
                        
                        val currentItem = binding.bannerView.currentItem
                        val nextItem = (currentItem + 1) % bannerAdapter.itemCount
                        binding.bannerView.setCurrentItem(nextItem, true)
                        
                        // ✅ Schedule next scroll
                        bannerHandler.postDelayed(this, 3000) // 3 giây
                    }
                } catch (e: Exception) {
                    println("❌ Error in banner auto scroll: ${e.message}")
                }
            }
        }
        
        bannerRunnable?.let { runnable ->
            bannerHandler.postDelayed(runnable, 3000)
        }
    }

    /**
     * ✅ Dừng auto scroll với Handler
     */
    private fun stopAutoScroll() {
        bannerRunnable?.let { runnable ->
            bannerHandler.removeCallbacks(runnable)
        }
        bannerRunnable = null
    }

    override fun onResume() {
        super.onResume()
        if (::bannerAdapter.isInitialized && bannerAdapter.itemCount > 0) {
        startAutoScroll()
        }

        viewModel.loadProducts(isRefresh = true)
    }
    
    override fun onPause() {
        super.onPause()
        stopAutoScroll()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopAutoScroll()
        // ✅ Clear handler để tránh memory leak
        bannerHandler.removeCallbacksAndMessages(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                // arguments setup if needed
            }
    }
}