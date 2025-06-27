package com.example.greenbuyapp.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.product.model.Product
import com.example.greenbuyapp.data.product.model.ProductAttribute
import com.example.greenbuyapp.data.product.model.ProductListResponse
import com.example.greenbuyapp.data.product.model.shopProducts
import com.example.greenbuyapp.data.shop.model.Shop
import com.example.greenbuyapp.domain.product.ProductRepository
import com.example.greenbuyapp.domain.shop.ShopRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel(
    private val productRepository: ProductRepository,
    private val shopRepository: ShopRepository
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
    
    // Product attributes state
    private val _productAttributes = MutableStateFlow<List<ProductAttribute>>(emptyList())
    val productAttributes: StateFlow<List<ProductAttribute>> = _productAttributes.asStateFlow()

    // Product state
    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product.asStateFlow()

    // Product of shop state
    private val _shopProducts = MutableStateFlow<List<Product>>(emptyList())
    val shopProducts: StateFlow<List<Product>> = _shopProducts.asStateFlow()

    // Shop state
    private val _shop = MutableStateFlow<Shop?>(null)
    val shop: StateFlow<Shop?> = _shop.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Selected attribute index for ViewPager
    private val _selectedAttributeIndex = MutableStateFlow(0)
    val selectedAttributeIndex: StateFlow<Int> = _selectedAttributeIndex.asStateFlow()

    /**
     * Load product từ API
     */
    fun loadProduct(productId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                when (val result = productRepository.getProduct(productId)) {
                    is Result.Success -> {
                        _product.value = result.value
                        println("✅ Loaded ${result.value} product")
                    }
                    is Result.Error -> {
                        val errorMsg = "Lỗi tải thông tin sản phẩm: ${result.error}"
                        _error.value = errorMsg
                        println("❌ $errorMsg")
                    }
                    is Result.NetworkError -> {
                        val errorMsg = "Lỗi mạng, vui lòng kiểm tra kết nối"
                        _error.value = errorMsg
                        println("🌐 $errorMsg")
                    }
                    is Result.Loading -> {
                        // Keep loading state
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Lỗi không xác định: ${e.message}"
                _error.value = errorMsg
                println("💥 $errorMsg")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load product attributes từ API
     */
    fun loadProductAttributes(productId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                when (val result = productRepository.getProductAttributes(productId)) {
                    is Result.Success -> {
                        _productAttributes.value = result.value
                        println("✅ Loaded ${result.value.size} product attributes")
                        result.value.forEach { attr ->
                            println("   - ${attr.color} ${attr.size}: ${attr.getFormattedPrice()}")
                        }
                    }
                    is Result.Error -> {
                        val errorMsg = "Lỗi tải thông tin sản phẩm: ${result.error}"
                        _error.value = errorMsg
                        println("❌ $errorMsg")
                    }
                    is Result.NetworkError -> {
                        val errorMsg = "Lỗi mạng, vui lòng kiểm tra kết nối"
                        _error.value = errorMsg
                        println("🌐 $errorMsg")
                    }
                    is Result.Loading -> {
                        // Keep loading state
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Lỗi không xác định: ${e.message}"
                _error.value = errorMsg
                println("💥 $errorMsg")
            } finally {
                _isLoading.value = false
            }
        }
    }
    /**
    * Get Shop by ID từ API
    */
    fun getShopById(shopId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                when(val result = shopRepository.getShop(shopId)) {
                    is Result.Success -> {
                        _shop.value = result.value
                        println("✅ Loaded shop: ${result.value.name}")

                    }
                    is Result.Error -> {
                        val errorMsg = "Lỗi tải thông tin cửa hàng: ${result.error}"
                        _error.value = errorMsg
                        println("❌ $errorMsg")
                    }
                    is Result.NetworkError -> {
                        val errorMsg = "Lỗi mạng, vui lòng kiểm tra kết nối"
                        _error.value = errorMsg
                        println("🌐 $errorMsg")
                    }
                    is Result.Loading -> {
                    }
                }
            }
            catch (e : Exception) {
                val errorMsg = "Lỗi không xác định: ${e.message}"
                _error.value = errorMsg
                println("💥 $errorMsg")
            }
            finally {
                _isLoading.value = false
            }
        }
    }


    /**
     * Get list product by ShopID từ API
     * ✅ MODERN: Load products với StateFlow architecture
     */

    // Current page for pagination
    private var currentPage = 1
    private var isLoadingMore = false
    private var hasMoreProducts = true


    fun loadShopProducts(shopId: Int, isRefresh: Boolean = false) {
        viewModelScope.launch {
            // Nếu đang loading more thì không load nữa
            if (isLoadingMore && !isRefresh) return@launch

            if (isRefresh) {
                currentPage = 1
                hasMoreProducts = true
                _shopProducts.value = emptyList()
            }


            println("🛍️ Loading products - page: $currentPage, refresh: $isRefresh")
            println("   search: ${_searchQuery.value}")
            println("   categoryId: ${_categoryId.value}")

            when (val result = productRepository.getProductsByShopId(
                shopId = shopId,
                page = currentPage,
                limit = 10,
                search = _searchQuery.value.takeIf { it.isNotBlank() },
                sortBy = _sortBy.value,
                sortOrder = _sortOrder.value,
                approvedOnly = _approvedOnly.value
            )) {
                is Result.Success -> {
                    val newProducts = result.value.items

                    _shopProducts.value = if (isRefresh) {
                        newProducts
                    } else {
                        _shopProducts.value + newProducts
                    }

                    // Check if có thêm data không
                    hasMoreProducts = newProducts.size == 10
                    currentPage++

                    println("✅ Products loaded: ${newProducts.size} new items, total: ${_shopProducts.value.size}")
                }
                is Result.Error -> {
                    println("❌ Products error: ${result.error}")
                }
                is Result.NetworkError -> {
                    println("🌐 Products network error")
                }
                else -> {
                    println("❓ Products unknown error")
                }
            }

        }
    }

    /**
     * Update selected attribute index khi user swipe ViewPager
     */
    fun updateSelectedAttributeIndex(index: Int) {
        if (index >= 0 && index < _productAttributes.value.size) {
            _selectedAttributeIndex.value = index
        }
    }
    
    /**
     * Get currently selected attribute
     */
    fun getCurrentAttribute(): ProductAttribute? {
        val attributes = _productAttributes.value
        val index = _selectedAttributeIndex.value
        return if (attributes.isNotEmpty() && index < attributes.size) {
            attributes[index]
        } else null
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }
}