package com.example.greenbuyapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.category.model.Category
import com.example.greenbuyapp.data.product.model.Product
import com.example.greenbuyapp.data.product.model.TrendingProduct
import com.example.greenbuyapp.data.product.model.TrendingProductResponse
import com.example.greenbuyapp.domain.category.CategoryRepository
import com.example.greenbuyapp.domain.product.ProductRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class HomeViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categoryId = MutableStateFlow<Int?>(null)
    val categoryId: StateFlow<Int?> = _categoryId.asStateFlow()

    private val _subCategoryId = MutableStateFlow<Int?>(null)
    val subCategoryId: StateFlow<Int?> = _subCategoryId.asStateFlow()

    private val _shopId = MutableStateFlow<Int?>(null)
    val shopId: StateFlow<Int?> = _shopId.asStateFlow()

    private val _minPrice = MutableStateFlow<Double?>(null)
    val minPrice: StateFlow<Double?> = _minPrice.asStateFlow()

    private val _maxPrice = MutableStateFlow<Double?>(null)
    val maxPrice: StateFlow<Double?> = _maxPrice.asStateFlow()

    private val _sortBy = MutableStateFlow("created_at")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    private val _sortOrder = MutableStateFlow("desc")
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    private val _approvedOnly = MutableStateFlow(true)
    val approvedOnly: StateFlow<Boolean> = _approvedOnly.asStateFlow()

    // Trending products
    private val _trendingProducts = MutableStateFlow<List<TrendingProduct>>(emptyList())
    val trendingProducts: StateFlow<List<TrendingProduct>> = _trendingProducts.asStateFlow()
    
    private val _trendingLoading = MutableStateFlow(false)
    val trendingLoading: StateFlow<Boolean> = _trendingLoading.asStateFlow()
    
    private val _trendingError = MutableStateFlow<String?>(null)
    val trendingError: StateFlow<String?> = _trendingError.asStateFlow()

    // Banner items
    private val _bannerItems = MutableStateFlow<List<Int>>(emptyList())
    val bannerItems: StateFlow<List<Int>> = _bannerItems.asStateFlow()

    // ✅ MODERN PRODUCTS ARCHITECTURE - StateFlow based
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()
    
    private val _productsLoading = MutableStateFlow(false)
    val productsLoading: StateFlow<Boolean> = _productsLoading.asStateFlow()
    
    private val _productsError = MutableStateFlow<String?>(null)
    val productsError: StateFlow<String?> = _productsError.asStateFlow()
    
    // Current page for pagination
    private var currentPage = 1
    private var isLoadingMore = false
    private var hasMoreProducts = true
    private var loadedPages = mutableSetOf<Int>()
    
    // ✅ Debug: Thêm method để reset pagination
    fun resetPagination() {
        currentPage = 1
        hasMoreProducts = true
        isLoadingMore = false
        loadedPages.clear()
        _products.value = emptyList()
        println("🔄 Pagination reset: currentPage=$currentPage, hasMoreProducts=$hasMoreProducts")
    }

    /**
     * ✅ MODERN: Load products với StateFlow architecture
     */
    fun loadProducts(isRefresh: Boolean = false) {
        viewModelScope.launch {
            // Nếu đang loading more thì không load nữa
            if (isLoadingMore && !isRefresh) return@launch
            
            if (isRefresh) {
                currentPage = 1
                hasMoreProducts = true
                loadedPages.clear()
                _products.value = emptyList()
            }
            
            _productsLoading.value = true
            _productsError.value = null
            
            println("🛍️ Loading products - page: $currentPage, refresh: $isRefresh")
            println("   search: ${_searchQuery.value}")
            println("   categoryId: ${_categoryId.value}")
            println("   current products before API call: ${_products.value.size}")
            println("   loadedPages before API call: $loadedPages")
            println("   hasMoreProducts before API call: $hasMoreProducts")
            
            try {
                // ✅ Thêm timeout cho API call
                val result = withTimeoutOrNull(30000L) { // 30 giây timeout
                    productRepository.getProducts(
                        page = currentPage,
                        limit = 10,
                        search = _searchQuery.value.takeIf { it.isNotBlank() },
                        categoryId = _categoryId.value,
                        subCategoryId = _subCategoryId.value,
                        shopId = _shopId.value,
                        minPrice = _minPrice.value,
                        maxPrice = _maxPrice.value,
                        sortBy = _sortBy.value,
                        sortOrder = _sortOrder.value,
                        approvedOnly = _approvedOnly.value
                    )
                }
                
                if (result == null) {
                    println("⏰ API call timeout after 30 seconds")
                    _productsError.value = "Timeout: Không thể kết nối đến server"
                    return@launch
                }
                
                when (result) {
                    is Result.Success -> {
                        val newProducts = result.value.items
                        val response = result.value
                        
                        println("📦 API returned ${newProducts.size} new products")
                        println("📦 API response details: page=${response.page}, total=${response.total}, total_pages=${response.total_pages}")
                        println("📦 Current products before append: ${_products.value.size}")
                        
                        // ✅ Luôn cộng dồn sản phẩm, kể cả khi has_next = false (trang cuối)
                        if (newProducts.isEmpty()) {
                            println("⚠️ API returned 0 items for page $currentPage")
                            println("🔍 Debug: currentPage=$currentPage, totalPages=${response.total_pages}, total=${response.total}")

                            // ✅ Luôn thêm trang đã gọi vào loadedPages
                            loadedPages.add(currentPage)
                            val oldPage = currentPage
                            currentPage++

                            // ✅ Nếu là trang đầu tiên và không có sản phẩm nào
                            if (oldPage == 1) {
                                println("🚫 No products available in the system")
                                _products.value = emptyList()
                                hasMoreProducts = false
                            } else {
                                // ✅ Nếu là trang tiếp theo, dừng phân trang
                                println("🏁 Reached end of products, stopping pagination")
                                hasMoreProducts = false
                            }
                            println("📄 Page updated: $oldPage -> $currentPage")
                            println("📄 Loaded pages: $loadedPages")
                        } else {
                            println("🔍 Processing ${newProducts.size} new products...")
                            println("🔍 Current products count before append: ${_products.value.size}")
                            println("🔍 Current product IDs: ${_products.value.map { it.product_id }}")
                            println("🔍 New product IDs: ${newProducts.map { it.product_id }}")
                            
                            _products.value = if (isRefresh) {
                                println("🔄 Refresh mode: replacing all products")
                                newProducts
                            } else {
                                val currentProductIds = _products.value.map { it.product_id }.toSet()
                                val uniqueNewProducts = newProducts.filter { it.product_id !in currentProductIds }
                                println("🔍 Unique new products: ${uniqueNewProducts.size} (filtered from ${newProducts.size})")
                                println("🔍 Unique product IDs: ${uniqueNewProducts.map { it.product_id }}")
                                _products.value + uniqueNewProducts
                            }
                            
                            println("🔍 Final products count: ${_products.value.size}")
                            println("🔍 Final product IDs: ${_products.value.map { it.product_id }}")
                            
                            // ✅ Luôn thêm trang đã gọi vào loadedPages
                            loadedPages.add(currentPage)
                            val oldPage = currentPage
                            currentPage++
                            // ✅ Sử dụng has_next để dừng phân trang, nhưng luôn cộng dồn sản phẩm trang cuối
                            hasMoreProducts = response.has_next
                            println("📄 Page updated: $oldPage -> $currentPage")
                            println("📄 Loaded pages: $loadedPages")
                        }
                        
                        println("✅ Products loaded: ${newProducts.size} new items, total: ${_products.value.size}")
                        println("   hasNext: ${response.has_next}, totalPages: ${response.total_pages}, currentPage: ${currentPage-1}")
                        println("   API response: page=${response.page}, total=${response.total}, has_next=${response.has_next}")
                    }
                    is Result.Error -> {
                        _productsError.value = result.error ?: "Lỗi tải sản phẩm"
                        println("❌ Products error: ${result.error}")
                    }
                    is Result.NetworkError -> {
                        _productsError.value = "Không có kết nối mạng"
                        println("🌐 Products network error")
                    }
                    else -> {
                        _productsError.value = "Lỗi không xác định"
                        println("❓ Products unknown error")
                    }
                }
            } catch (e: Exception) {
                println("❌ Exception in loadProducts: ${e.message}")
                e.printStackTrace()
                _productsError.value = "Lỗi: ${e.message}"
            } finally {
                _productsLoading.value = false
            }
        }
    }
    
    /**
     * Load more products cho infinite scrolling
     */
    fun loadMoreProducts() {
        println("🔄 loadMoreProducts() called")
        println("   hasMoreProducts: $hasMoreProducts")
        println("   isLoadingMore: $isLoadingMore") 
        println("   productsLoading: ${_productsLoading.value}")
        println("   currentPage: $currentPage")
        println("   current products count: ${_products.value.size}")
        println("   loadedPages: $loadedPages")
        
        // ✅ Enhanced condition check to prevent duplicate calls
        if (!hasMoreProducts) {
            println("🚫 Cannot load more: No more products available (hasNext=false)")
            println("🏁 Đã load hết tất cả products có sẵn")
            return
        }
        
        if (isLoadingMore) {
            println("🚫 Cannot load more: Already loading more products")
            return
        }
        
        if (_productsLoading.value) {
            println("🚫 Cannot load more: Products already loading")
            return
        }
        
        // ✅ Bỏ kiểm tra loadedPages để tránh skip trang
        // Vì có thể có race condition
        
        println("✅ Starting to load more products...")
        isLoadingMore = true
        viewModelScope.launch {
            try {
                loadProducts(isRefresh = false)
            } catch (e: Exception) {
                println("❌ Exception in loadMoreProducts: ${e.message}")
                e.printStackTrace()
            } finally {
                isLoadingMore = false
                println("✅ loadMoreProducts completed")
            }
        }
    }
    
    /**
     * Refresh products
     */
    fun refreshProducts() {
        loadProducts(isRefresh = true)
    }
    
    /**
     * Retry loading products
     */
    fun retryLoadProducts() {
        loadProducts(isRefresh = true)
    }

    /**
     * Load trending products
     * @param page Số trang (mặc định là 1)
     * @param limit Số lượng sản phẩm (mặc định là 10)
     */
    fun loadTrendingProducts(page: Int = 1, limit: Int = 10) {
        viewModelScope.launch {
            _trendingLoading.value = true
            _trendingError.value = null
            
            when (val result = productRepository.getTrending(page, limit)) {
                is Result.Success -> {
                    _trendingProducts.value = result.value.items
                    println("✅ Trending products loaded: ${result.value.items.size} items")
                }
                is Result.Error -> {
                    _trendingError.value = result.error ?: "Lỗi tải sản phẩm trending"
                    println("❌ Trending products error: ${result.error}")
                }
                is Result.NetworkError -> {
                    _trendingError.value = "Không có kết nối mạng"
                    println("🌐 Trending products network error")
                }
                else -> {
                    _trendingError.value = "Lỗi không xác định"
                    println("❓ Trending products unknown error")
                }
            }
            
            _trendingLoading.value = false
        }
    }

    /**
     * Load banner items - có thể từ API hoặc dữ liệu cố định
     */
    fun loadBannerItems() {
        // Tạo dữ liệu banner mẫu với 3 ảnh
        val bannerData = listOf(
            R.drawable.banner_1,
            R.drawable.banner_1,
            R.drawable.banner_1
        )
        
        _bannerItems.value = bannerData
        println("✅ Banner items loaded: ${bannerData.size} items")
    }

    /**
     * Load banner từ API (có thể implement sau)
     */
    fun loadBannerFromApi() {
        viewModelScope.launch {
            // TODO: Implement API call to get banner data
            // Tạm thời sử dụng dữ liệu cố định
            loadBannerItems()
        }
    }

    fun updateSearchQuery(query: String) {
        if (_searchQuery.value != query) {
            _searchQuery.value = query
            refreshProducts()
        }
    }

    fun updateCategoryId(categoryId: Int?) {
        if (_categoryId.value != categoryId) {
            _categoryId.value = categoryId
            refreshProducts()
        }
    }

    fun updateSubCategoryId(subCategoryId: Int?) {
        if (_subCategoryId.value != subCategoryId) {
            _subCategoryId.value = subCategoryId
            refreshProducts()
        }
    }

    fun updateShopId(shopId: Int?) {
        if (_shopId.value != shopId) {
            _shopId.value = shopId
            refreshProducts()
        }
    }

    fun updatePriceRange(minPrice: Double?, maxPrice: Double?) {
        if (_minPrice.value != minPrice || _maxPrice.value != maxPrice) {
            _minPrice.value = minPrice
            _maxPrice.value = maxPrice
            refreshProducts()
        }
    }

    fun updateSorting(sortBy: String, sortOrder: String) {
        if (_sortBy.value != sortBy || _sortOrder.value != sortOrder) {
            _sortBy.value = sortBy
            _sortOrder.value = sortOrder
            refreshProducts()
        }
    }

    fun updateApprovedOnly(approvedOnly: Boolean) {
        if (_approvedOnly.value != approvedOnly) {
            _approvedOnly.value = approvedOnly
            refreshProducts()
        }
    }

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    private val _categoriesLoading = MutableStateFlow(false)
    val categoriesLoading: StateFlow<Boolean> = _categoriesLoading.asStateFlow()
    
    private val _categoriesError = MutableStateFlow<String?>(null)
    val categoriesError: StateFlow<String?> = _categoriesError.asStateFlow()

    fun loadCategories() {
        viewModelScope.launch {
            _categoriesLoading.value = true
            _categoriesError.value = null
            
            when (val result = categoryRepository.getCategories()) {
                is Result.Success -> {
                    _categories.value = result.value
                }
                is Result.Error -> {
                    _categoriesError.value = result.error ?: "Lỗi tải danh mục"
                }
                is Result.NetworkError -> {
                    _categoriesError.value = "Không có kết nối mạng"
                }
                else -> {
                    _categoriesError.value = "Lỗi không xác định"
                }
            }
            
            _categoriesLoading.value = false
        }
    }
    
    fun retryLoadCategories() {
        loadCategories()
    }

    /**
     * ✅ Debug function để test API với các tham số khác nhau
     */
    fun testApiWithParams(
        page: Int = 1,
        limit: Int = 15,
        search: String = "",
        categoryId: Int? = null,
        approvedOnly: Boolean = true
    ) {
        viewModelScope.launch {
            println("🧪 Testing API with params:")
            println("   page: $page")
            println("   limit: $limit")
            println("   search: '$search'")
            println("   categoryId: $categoryId")
            println("   approvedOnly: $approvedOnly")
            
            try {
                val result = withTimeoutOrNull(10000L) { // 10 giây timeout cho test
                    productRepository.getProducts(
                        page = page,
                        limit = limit,
                        search = search.takeIf { it.isNotBlank() },
                        categoryId = categoryId,
                        approvedOnly = approvedOnly
                    )
                }
                
                if (result == null) {
                    println("⏰ Test API timeout")
                    return@launch
                }
                
                when (result) {
                    is Result.Success -> {
                        val response = result.value
                        println("✅ Test API success:")
                        println("   items: ${response.items.size}")
                        println("   total: ${response.total}")
                        println("   page: ${response.page}")
                        println("   total_pages: ${response.total_pages}")
                        println("   has_next: ${response.has_next}")
                        println("   has_prev: ${response.has_prev}")
                        
                        // Log first few items
                        response.items.take(3).forEach { product ->
                            println("   Product: ${product.name} (ID: ${product.product_id})")
                        }
                    }
                    is Result.Error -> {
                        println("❌ Test API error: ${result.error}")
                    }
                    is Result.NetworkError -> {
                        println("🌐 Test API network error")
                    }
                    else -> {
                        println("❓ Test API unknown error")
                    }
                }
            } catch (e: Exception) {
                println("❌ Test API exception: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}