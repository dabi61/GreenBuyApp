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
                _products.value = emptyList()
            }
            
            _productsLoading.value = true
            _productsError.value = null
            
            println("🛍️ Loading products - page: $currentPage, refresh: $isRefresh")
            println("   search: ${_searchQuery.value}")
            println("   categoryId: ${_categoryId.value}")
            
            when (val result = productRepository.getProducts(
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
            )) {
                is Result.Success -> {
                    val newProducts = result.value.items
                    
                    _products.value = if (isRefresh) {
                        newProducts
                    } else {
                        _products.value + newProducts
                    }
                    
                    // Check if có thêm data không
                    hasMoreProducts = newProducts.size == 10
                    currentPage++
                    
                    println("✅ Products loaded: ${newProducts.size} new items, total: ${_products.value.size}")
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
            
            _productsLoading.value = false
        }
    }
    
    /**
     * Load more products cho infinite scrolling
     */
    fun loadMoreProducts() {
        if (!hasMoreProducts || isLoadingMore || _productsLoading.value) {
            println("🚫 Cannot load more: hasMore=$hasMoreProducts, isLoading=$isLoadingMore, loading=${_productsLoading.value}")
            return
        }
        
        isLoadingMore = true
        viewModelScope.launch {
            loadProducts(isRefresh = false)
            isLoadingMore = false
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
}