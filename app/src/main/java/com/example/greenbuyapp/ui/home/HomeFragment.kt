package com.example.greenbuyapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.FragmentHomeBinding
import com.example.greenbuyapp.ui.base.BaseFragment
import com.example.greenbuyapp.ui.product.ProductActivity
import com.example.greenbuyapp.ui.cart.CartActivity
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
    private lateinit var categoryAdapter: SimpleCategoryAdapter
    private lateinit var trendingAdapter: TrendingAdapter
    private lateinit var bannerAdapter: BannerAdapter
    
    // ✅ Sử dụng Handler thay vì Timer để tránh ANR
    private val bannerHandler = Handler(Looper.getMainLooper())
    private var bannerRunnable: Runnable? = null
    private var isUserScrolling = false
    
    // ✅ Throttling cho infinite scroll
    private var lastScrollTime = 0L
    private val scrollThrottleMs = 500L // 500ms throttle
    
    // ✅ Thêm flag để tránh duplicate load more calls
    private var isLoadMoreTriggered = false

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
            // viewModel.loadCategories()
            
            // Tạm thời sử dụng test data để kiểm tra UI
            println("📂 HomeFragment: Loading test categories...")
            viewModel.loadCategoriesTest()
            
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
            
            val intent = ProductActivity.createIntent(requireContext(), product.product_id, product.shop_id, product.description, product.name)
            println("🔍 Intent created: $intent")
            println("🔍 Intent extras after creation: ${intent.extras}")
            
            startActivity(intent)
            println("✅ ProductActivity started")
        }
        
        categoryAdapter = SimpleCategoryAdapter { category ->
            // Handle category click
            if (category != null) {
                println("Category clicked: ${category.name}")
                viewModel.updateCategoryId(category.id)
                categoryAdapter.updateSelectedCategory(category.id)
            } else {
                println("Category unselected -> showing all products")
                viewModel.updateCategoryId(null)
                categoryAdapter.updateSelectedCategory(null)
            }
        }

        trendingAdapter = TrendingAdapter { trendingProduct ->
            val intent = ProductActivity.createIntent(
                requireContext(),
                trendingProduct.product_id,
                trendingProduct.shop_id,
                trendingProduct.description,
                trendingProduct.name
            )
            startActivity(intent)

        }
        
        // ✅ Setup product RecyclerView với performance optimizations
        binding.rvProduct.apply {
            val gridLayoutManager = GridLayoutManager(context, 2)
            layoutManager = gridLayoutManager
            adapter = productAdapter
            
            // ✅ Performance optimizations
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
            
            // ✅ Thêm hardware acceleration để tránh lỗi OpenGL
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            
            // ✅ Tối ưu hóa drawing
            setItemViewCacheSize(20)
            setDrawingCacheEnabled(true)
            drawingCacheQuality = android.view.View.DRAWING_CACHE_QUALITY_HIGH
            
            // ✅ Disable over scroll để tránh lỗi render
            overScrollMode = android.view.View.OVER_SCROLL_NEVER
        }

        // ✅ Setup category RecyclerView với horizontal orientation
        binding.rvCategory.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
            setHasFixedSize(true)
            
            // ✅ Performance optimizations
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            overScrollMode = android.view.View.OVER_SCROLL_NEVER
            
            println("📂 Category RecyclerView setup completed")
            println("📂 RecyclerView visibility: ${visibility}")
            println("📂 RecyclerView adapter: ${adapter}")
            println("📂 RecyclerView layout manager: ${layoutManager}")
        }

        // ✅ Setup trending RecyclerView với horizontal orientation
        binding.rvTrending.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = trendingAdapter
            setHasFixedSize(true)
            
            // ✅ Performance optimizations
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            overScrollMode = android.view.View.OVER_SCROLL_NEVER
        }
    }
    
    /**
     * ✅ Setup infinite scrolling cho NestedScrollView
     */
    private fun setupInfiniteScrolling() {
        println("🔧 Setting up infinite scrolling...")
        
        // Tìm NestedScrollView trong view hierarchy
        val nestedScrollView = findNestedScrollViewInHierarchy(binding.root)
        
        if (nestedScrollView != null) {
            println("✅ Found NestedScrollView, setting up scroll listener")
            
            nestedScrollView.setOnScrollChangeListener { _: androidx.core.widget.NestedScrollView, 
                scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
                
                val currentTime = System.currentTimeMillis()
                
                // ✅ Throttling - chỉ process scroll event mỗi 500ms
                if (currentTime - lastScrollTime < scrollThrottleMs) {
                    return@setOnScrollChangeListener
                }
                lastScrollTime = currentTime
                
                val child = nestedScrollView.getChildAt(0)
                val childHeight = child.height
                val scrollViewHeight = nestedScrollView.height
                val scrollPosition = scrollY
                
                // Tính toán khoảng cách đến cuối
                val distanceToBottom = childHeight - scrollViewHeight - scrollPosition
                
                // ✅ Tăng threshold lên 800px để dễ trigger hơn
                val threshold = 800
                val isScrollingDown = scrollY > oldScrollY
                
                println("📊 Scroll info: scrollY=$scrollY, childHeight=$childHeight, scrollViewHeight=$scrollViewHeight, distanceToBottom=$distanceToBottom")
                println("🔍 Infinite scroll check: distanceToBottom=$distanceToBottom, threshold=$threshold, isScrollingDown=$isScrollingDown")
                
                // ✅ Chỉ trigger khi đang scroll xuống và gần cuối
                if (distanceToBottom <= threshold && !isLoadMoreTriggered) {
                    println("🔄 NestedScrollView reached near end, triggering load more...")
                    println("📊 Current products count: ${productAdapter.getCurrentItemCount()}")
                    println("📊 Distance to bottom: $distanceToBottom, threshold: $threshold")
                    println("📊 isScrollingDown: $isScrollingDown")
                    isLoadMoreTriggered = true
                    viewModel.loadMoreProducts()
                    
                    // ✅ Reset flag sau 2 giây
                    view?.postDelayed({
                        isLoadMoreTriggered = false
                        println("🔄 Reset load more flag")
                    }, 2000)
                } else if (distanceToBottom <= threshold && isLoadMoreTriggered) {
                    println("⏸️ Load more already triggered, waiting...")
                }
                
                // ✅ Debug: Log scroll info để theo dõi
                if (distanceToBottom <= 1000) {
                    println("🔍 Near bottom: distanceToBottom=$distanceToBottom, isScrollingDown=$isScrollingDown, isLoadMoreTriggered=$isLoadMoreTriggered")
                }
            }
        } else {
            println("❌ NestedScrollView not found!")
            // Fallback: Thử setup sau 1 giây
            view?.postDelayed({
                setupInfiniteScrolling()
            }, 1000)
        }
    }
    
    /**
     * Tìm NestedScrollView trong view hierarchy
     */
    private fun findNestedScrollViewInHierarchy(view: android.view.View): androidx.core.widget.NestedScrollView? {
        if (view is androidx.core.widget.NestedScrollView) {
            println("🎯 Found NestedScrollView: $view")
            return view
        }
        
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val found = findNestedScrollViewInHierarchy(view.getChildAt(i))
                if (found != null) return found
            }
        }
        
        return null
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
        
        // ✅ Setup pull-to-refresh cho NestedScrollView (nếu có trong layout)
    }

    
    /**
     * Observe products với StateFlow architecture
     */
    private fun observeProducts() {
        // Observe product data
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.products.collect { products ->
                println("🛍️ Products updated: ${products.size} items")
                println("🛍️ Products list: ${products.map { it.name }}")
                println("🛍️ Products IDs: ${products.map { it.product_id }}")
                
                // ✅ Xử lý trường hợp không có sản phẩm
                if (products.isEmpty()) {
                    println("📭 No products available")
                    // TODO: Hiển thị empty state
                    // binding.emptyState.visibility = View.VISIBLE
                    // binding.rvProduct.visibility = View.GONE
                } else {
                    println("📦 Products available: ${products.size} items")
                    // TODO: Ẩn empty state
                    // binding.emptyState.visibility = View.GONE
                    // binding.rvProduct.visibility = View.VISIBLE
                }
                
                // Submit new list
                productAdapter.submitList(products) {
                    // Callback được gọi khi submitList hoàn tất
                    println("✅ ProductAdapter submitList completed")
                    println("📊 Current adapter item count: ${productAdapter.getCurrentItemCount()}")
                    println("📊 Last item position: ${productAdapter.getLastItemPosition()}")
                    println("🟢 Adapter list size: ${productAdapter.currentList.size}")
                    println("🟢 All product IDs: ${productAdapter.currentList.map { it.product_id }}")
                    println("🟢 All product names: ${productAdapter.currentList.map { it.name }}")
                }
            }
        }
        
        // Observe loading state  
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.productsLoading.collect { isLoading ->
                println("⏳ Products loading: $isLoading")
                // TODO: Show/hide loading indicator
                // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                
                // ✅ Visual feedback cho loading state
                if (isLoading) {
                    println("🔄 Showing loading indicator...")
                    // ✅ Reset load more flag khi bắt đầu loading
                    isLoadMoreTriggered = false
                } else {
                    println("✅ Hiding loading indicator...")
                }
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
                println("📂 UI: Submitting ${categories.size} categories to adapter")
                categories.forEachIndexed { index, category ->
                    println("📂 Category [$index]: ${category.name} (ID: ${category.id})")
                }
                
                // Check if adapter is initialized
                if (::categoryAdapter.isInitialized) {
                    categoryAdapter.submitList(categories)
                    println("✅ Categories submitted to adapter successfully")
                } else {
                    println("❌ Category adapter not initialized!")
                }
                
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
        // ❌ Không gọi lại loadProducts(isRefresh = true) ở đây để tránh reset danh sách khi quay lại màn hình
        // viewModel.loadProducts(isRefresh = true)
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
    
    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        println("🎯 onViewCreated called")
        
        // ✅ Setup infinite scrolling sau khi view hierarchy hoàn tất
        view.post {
            println("🎯 Post runnable executing - setting up infinite scrolling")
            setupInfiniteScrolling()
        }
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