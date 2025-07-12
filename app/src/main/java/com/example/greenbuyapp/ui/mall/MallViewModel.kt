package com.example.greenbuyapp.ui.mall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.product.model.Product
import com.example.greenbuyapp.data.category.model.Category
import com.example.greenbuyapp.data.cart.model.AddToCartRequest
import com.example.greenbuyapp.data.shop.model.Shop
import com.example.greenbuyapp.domain.product.ProductRepository
import com.example.greenbuyapp.domain.category.CategoryRepository
import com.example.greenbuyapp.domain.cart.CartRepository
import com.example.greenbuyapp.data.shop.ShopService
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MallViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val cartRepository: CartRepository,
    private val shopService: ShopService
) : ViewModel() {

    // Featured Products State
    private val _featuredProducts = MutableStateFlow<List<Product>>(emptyList())
    val featuredProducts: StateFlow<List<Product>> = _featuredProducts.asStateFlow()

    private val _featuredLoading = MutableStateFlow(false)
    val featuredLoading: StateFlow<Boolean> = _featuredLoading.asStateFlow()

    private val _featuredError = MutableStateFlow<String?>(null)
    val featuredError: StateFlow<String?> = _featuredError.asStateFlow()

    // Categories State
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _categoriesLoading = MutableStateFlow(false)
    val categoriesLoading: StateFlow<Boolean> = _categoriesLoading.asStateFlow()

    private val _categoriesError = MutableStateFlow<String?>(null)
    val categoriesError: StateFlow<String?> = _categoriesError.asStateFlow()

    // Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    // Filtered Products (based on search and category)
    private val _filteredProducts = MutableStateFlow<List<Product>>(emptyList())
    val filteredProducts: StateFlow<List<Product>> = _filteredProducts.asStateFlow()

    // Add to Cart State
    private val _addToCartLoading = MutableStateFlow(false)
    val addToCartLoading: StateFlow<Boolean> = _addToCartLoading.asStateFlow()

    private val _addToCartMessage = MutableStateFlow<String?>(null)
    val addToCartMessage: StateFlow<String?> = _addToCartMessage.asStateFlow()

    // Shop Info Cache
    private val _shopInfoCache = MutableStateFlow<Map<Int, Shop>>(emptyMap())
    val shopInfoCache: StateFlow<Map<Int, Shop>> = _shopInfoCache.asStateFlow()

    init {
        loadFeaturedProducts()
        loadCategories()
    }

    /**
     * ✅ Load featured products từ API
     */
    fun loadFeaturedProducts() {
        viewModelScope.launch {
            try {
                _featuredLoading.value = true
                _featuredError.value = null

                when (val result = productRepository.getFeaturedProducts(limit = 20)) {
                    is Result.Success -> {
                        val products = result.value.items
                        _featuredProducts.value = products
                        updateFilteredProducts()
                        
                        // Load shop info for all products
                        loadShopInfoForProducts(products)
                        
                        println("✅ Loaded ${products.size} featured products")
                    }
                    is Result.Error -> {
                        val errorMsg = "Lỗi tải sản phẩm nổi bật: ${result.error}"
                        _featuredError.value = errorMsg
                        println("❌ $errorMsg")
                    }
                    is Result.NetworkError -> {
                        val errorMsg = "Lỗi mạng, vui lòng kiểm tra kết nối"
                        _featuredError.value = errorMsg
                        println("🌐 $errorMsg")
                    }
                    is Result.Loading -> {
                        // Đã xử lý bằng _featuredLoading
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Lỗi không xác định: ${e.message}"
                _featuredError.value = errorMsg
                println("💥 $errorMsg")
            } finally {
                _featuredLoading.value = false
            }
        }
    }

    /**
     * ✅ Load categories
     */
    fun loadCategories() {
        viewModelScope.launch {
            try {
                _categoriesLoading.value = true
                _categoriesError.value = null

                when (val result = categoryRepository.getCategories()) {
                    is Result.Success -> {
                        _categories.value = result.value
                        println("✅ Loaded ${result.value.size} categories")
                    }
                    is Result.Error -> {
                        val errorMsg = "Lỗi tải danh mục: ${result.error}"
                        _categoriesError.value = errorMsg
                        println("❌ $errorMsg")
                    }
                    is Result.NetworkError -> {
                        val errorMsg = "Lỗi mạng khi tải danh mục"
                        _categoriesError.value = errorMsg
                        println("🌐 $errorMsg")
                    }
                    is Result.Loading -> {
                        // Đã xử lý bằng _categoriesLoading
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Lỗi không xác định khi tải danh mục: ${e.message}"
                _categoriesError.value = errorMsg
                println("💥 $errorMsg")
            } finally {
                _categoriesLoading.value = false
            }
        }
    }

    /**
     * ✅ Load shop info for products
     */
    private fun loadShopInfoForProducts(products: List<Product>) {
        viewModelScope.launch {
            val currentCache = _shopInfoCache.value.toMutableMap()
            val shopIdsToLoad = products.map { it.shop_id }.distinct().filter { !currentCache.containsKey(it) }
            
            shopIdsToLoad.forEach { shopId ->
                try {
                    val shop = shopService.getShopById(shopId)
                    currentCache[shopId] = shop
                    println("✅ Loaded shop info for shop_id: $shopId - ${shop.name}")
                } catch (e: Exception) {
                    println("❌ Failed to load shop info for shop_id: $shopId - ${e.message}")
                }
            }
            
            _shopInfoCache.value = currentCache
        }
    }

    /**
     * ✅ Add product to cart
     */
    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            try {
                _addToCartLoading.value = true
                _addToCartMessage.value = null
                
                println("🛒 Starting add to cart for product: ${product.name} (ID: ${product.product_id})")

                // First, get product attributes to find the first available attribute
                println("📦 Fetching product attributes...")
                when (val attributesResult = productRepository.getProductAttributes(product.product_id)) {
                    is Result.Success -> {
                        val attributes = attributesResult.value // This is already List<ProductAttribute>
                        println("✅ Found ${attributes.size} attributes for product")
                        
                        if (attributes.isNotEmpty()) {
                            // Use the first attribute
                            val firstAttribute = attributes.first()
                            println("🎯 Using first attribute: ID=${firstAttribute.attribute_id}, Color=${firstAttribute.color}, Size=${firstAttribute.size}")
                            
                            val request = AddToCartRequest(
                                attribute_id = firstAttribute.attribute_id,
                                quantity = quantity
                            )

                            // Add to cart with the first attribute
                            println("🛒 Adding to cart with attribute_id: ${firstAttribute.attribute_id}")
                            when (val result = cartRepository.addToCart(request)) {
                                is Result.Success -> {
                                    val message = "Đã thêm '${product.name}' vào giỏ hàng"
                                    _addToCartMessage.value = message
                                    println("✅ $message (attribute_id: ${firstAttribute.attribute_id})")
                                }
                                is Result.Error -> {
                                    val errorMsg = "Không thể thêm vào giỏ hàng: ${result.error}"
                                    _addToCartMessage.value = errorMsg
                                    println("❌ $errorMsg")
                                }
                                is Result.NetworkError -> {
                                    val errorMsg = "Lỗi mạng, vui lòng kiểm tra kết nối"
                                    _addToCartMessage.value = errorMsg
                                    println("🌐 $errorMsg")
                                }
                                is Result.Loading -> {
                                    // Đã xử lý bằng _addToCartLoading
                                }
                            }
                        } else {
                            // No attributes found
                            val errorMsg = "Sản phẩm '${product.name}' không có biến thể nào để thêm vào giỏ hàng"
                            _addToCartMessage.value = errorMsg
                            println("❌ $errorMsg")
                        }
                    }
                    is Result.Error -> {
                        val errorMsg = "Không thể lấy thông tin sản phẩm: ${attributesResult.error}"
                        _addToCartMessage.value = errorMsg
                        println("❌ $errorMsg")
                    }
                    is Result.NetworkError -> {
                        val errorMsg = "Lỗi mạng khi lấy thông tin sản phẩm"
                        _addToCartMessage.value = errorMsg
                        println("🌐 $errorMsg")
                    }
                    is Result.Loading -> {
                        // Đã xử lý bằng _addToCartLoading
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Lỗi không xác định: ${e.message}"
                _addToCartMessage.value = errorMsg
                println("💥 $errorMsg")
                e.printStackTrace()
            } finally {
                _addToCartLoading.value = false
                println("🏁 Add to cart process completed")
            }
        }
    }

    /**
     * ✅ Get shop info by shop_id
     */
    fun getShopInfo(shopId: Int): Shop? {
        return _shopInfoCache.value[shopId]
    }

    /**
     * ✅ Update search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        updateFilteredProducts()
    }

    /**
     * ✅ Update selected category
     */
    fun updateSelectedCategory(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
        updateFilteredProducts()
    }

    /**
     * ✅ Filter products based on search query and category
     */
    private fun updateFilteredProducts() {
        try {
            val query = _searchQuery.value.lowercase().trim()
            val categoryId = _selectedCategoryId.value
            val allProducts = _featuredProducts.value

            val filtered = allProducts.filter { product ->
                // Filter by search query
                val matchesSearch = if (query.isEmpty()) {
                    true
                } else {
                    product.name.lowercase().contains(query) ||
                    product.description.lowercase().contains(query)
                }

                // Filter by category
                val matchesCategory = if (categoryId == null) {
                    true
                } else {
                    product.sub_category_id == categoryId
                }

                matchesSearch && matchesCategory
            }

            _filteredProducts.value = filtered
            println("🔍 Filtered ${filtered.size} products from ${allProducts.size} total (query: '$query', category: $categoryId)")
        } catch (e: Exception) {
            println("❌ Error filtering products: ${e.message}")
            // Fallback to all products if filtering fails
            _filteredProducts.value = _featuredProducts.value
        }
    }

    /**
     * ✅ Refresh all data
     */
    fun refresh() {
        loadFeaturedProducts()
        loadCategories()
    }

    /**
     * ✅ Reset search and refresh - dùng cho nút "Thử lại"
     */
    fun resetAndRefresh() {
        // Reset search query
        _searchQuery.value = ""
        // Reset selected category
        _selectedCategoryId.value = null
        // Clear errors
        clearErrors()
        // Refresh data
        refresh()
        println("🔄 Reset search and refresh triggered")
    }

    /**
     * ✅ Clear errors
     */
    fun clearErrors() {
        _featuredError.value = null
        _categoriesError.value = null
        _addToCartMessage.value = null
    }

    /**
     * ✅ Clear add to cart message
     */
    fun clearAddToCartMessage() {
        _addToCartMessage.value = null
    }

    /**
     * ✅ Get summary info
     */
    fun getSummaryInfo(): String {
        val totalProducts = _featuredProducts.value.size
        val filteredCount = _filteredProducts.value.size
        val categoriesCount = _categories.value.size
        
        return "Sản phẩm: $filteredCount/$totalProducts | Danh mục: $categoriesCount"
    }
}