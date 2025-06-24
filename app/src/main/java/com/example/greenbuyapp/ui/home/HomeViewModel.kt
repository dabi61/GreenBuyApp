package com.example.greenbuyapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.category.model.Category
import com.example.greenbuyapp.data.product.model.Product
import com.example.greenbuyapp.data.product.model.TrendingProduct
import com.example.greenbuyapp.data.product.model.TrendingProductResponse
import com.example.greenbuyapp.domain.Listing
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

    private var _productListing: Listing<Product>? = null

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

    fun getProductListing(): Listing<Product> {
        println("🔄 getProductListing() called")
        if (_productListing == null) {
            println("📦 Creating new product listing with params:")
            println("   search: ${_searchQuery.value}")
            println("   categoryId: ${_categoryId.value}")
            println("   approvedOnly: ${_approvedOnly.value}")
            
            _productListing = productRepository.getProductsPaged(
                search = _searchQuery.value.takeIf { it.isNotBlank() },
                categoryId = _categoryId.value,
                subCategoryId = _subCategoryId.value,
                shopId = _shopId.value,
                minPrice = _minPrice.value,
                maxPrice = _maxPrice.value,
                sortBy = _sortBy.value,
                sortOrder = _sortOrder.value,
                approvedOnly = _approvedOnly.value,
                scope = viewModelScope
            )
        } else {
            println("♻️ Reusing existing product listing")
        }
        return _productListing!!
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

    fun refreshProducts() {
        _productListing = null
        // The listing will be recreated on next getProductListing() call
    }

    fun retryFailedRequest() {
        _productListing?.retry?.invoke()
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