package com.example.greenbuyapp.ui.shop.productManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.product.model.Product
import com.example.greenbuyapp.data.product.model.ProductStatus
import com.example.greenbuyapp.data.product.model.InventoryStatsResponse
import com.example.greenbuyapp.data.product.model.InventoryStatsSummary
import com.example.greenbuyapp.domain.product.ProductRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductManagementViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Product counts for each status
    private val _productCounts = MutableStateFlow(ProductCounts())
    val productCounts: StateFlow<ProductCounts> = _productCounts.asStateFlow()

    // ✅ Cache StateFlow instances
    private val _productsByStatus = mutableMapOf<ProductStatus, MutableStateFlow<List<Product>>>()

    init {
        loadMyProducts()
    }

    /**
     * Load inventory stats and update product counts
     */
    fun loadMyProducts() {
        viewModelScope.launch {
            runCatching {
                _isLoading.value = true
                
                // ✅ Gọi API lấy thống kê inventory
                when (val result = productRepository.getInventoryStats()) {
                    is Result.Success -> {
                        val stats = result.value.summary
                        println("🔍 API Response - inventory-stats: pending_approval=${stats.pending_approval}, pending=${stats.pending}, in_stock=${stats.in_stock}, out_of_stock=${stats.out_of_stock}")
                        updateProductCountsFromAPI(stats)
                        println("✅ Loaded inventory stats: ${stats}")
                    }
                    is Result.Error -> {
                        _errorMessage.value = "Lỗi khi tải thống kê: ${result.error}"
                        println("❌ Error loading inventory stats: ${result.error}")
                    }
                    is Result.Loading -> {
                        // Loading state already handled by _isLoading
                    }
                    is Result.NetworkError -> {
                        _errorMessage.value = "Lỗi kết nối mạng khi tải thống kê"
                        println("❌ Network error loading inventory stats")
                    }
                }
                
            }.onFailure { exception ->
                _errorMessage.value = "Lỗi khi tải sản phẩm: ${exception.message}"
                println("❌ Error loading products: ${exception.message}")
            }.also {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get products by status using API
     */
    fun getProductsByStatus(status: ProductStatus): StateFlow<List<Product>> {
        return _productsByStatus.getOrPut(status) {
            MutableStateFlow<List<Product>>(emptyList()).also { flow ->
                loadProductsByStatus(status, flow)
            }
        }.asStateFlow()
    }

    /**
     * Load products by status from API
     */
    private fun loadProductsByStatus(status: ProductStatus, flow: MutableStateFlow<List<Product>>) {
        viewModelScope.launch {
            // ✅ Convert ProductStatus enum to String using apiValue
            when (val result = productRepository.getProductsByStatus(status.apiValue)) {
                is Result.Success -> flow.value = result.value.items
                else -> flow.value = emptyList()
            }
        }
    }

    /**
     * Update product counts from API stats
     */
    private fun updateProductCountsFromAPI(stats: InventoryStatsSummary) {
        val counts = ProductCounts(
            inStock = stats.in_stock,
            outOfStock = stats.out_of_stock,
            pendingApproval = stats.pendingCount  // Sử dụng helper property
        )
        _productCounts.value = counts
        println("📊 Updated product counts: IN_STOCK=${counts.inStock}, OUT_OF_STOCK=${counts.outOfStock}, PENDING=${counts.pendingApproval}")
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * ✅ Reload products for specific status
     */
    fun reloadProductsByStatus(status: ProductStatus) {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Get existing StateFlow or create new one
            val flow = _productsByStatus.getOrPut(status) {
                MutableStateFlow<List<Product>>(emptyList())
            }
            
            // ✅ Force reload from API - Convert enum to String
            when (val result = productRepository.getProductsByStatus(status.apiValue)) {
                is Result.Success -> {
                    flow.value = result.value.items
                    println("✅ Reloaded products for ${status.displayName}: ${result.value.items.size}")
                }
                is Result.Error -> {
                    _errorMessage.value = "Lỗi khi tải sản phẩm: ${result.error}"
                    println("❌ Error reloading products for ${status.displayName}: ${result.error}")
                }
                is Result.NetworkError -> {
                    _errorMessage.value = "Lỗi kết nối mạng"
                    println("❌ Network error reloading products for ${status.displayName}")
                }
                else -> {
                    println("⚠️ Unknown result type for ${status.displayName}")
                }
            }
            
            _isLoading.value = false
        }
    }

    /**
     * ✅ Reload all products (for refresh all tabs)
     */
    fun reloadAllProducts() {
        viewModelScope.launch {
            // Reload inventory stats first
            loadMyProducts()
            
            // Then reload products for each cached status
            _productsByStatus.keys.forEach { status ->
                reloadProductsByStatus(status)
            }
        }
    }
}

/**
 * Data class for product counts
 */
data class ProductCounts(
    val inStock: Int = 0,
    val outOfStock: Int = 0,
    val pendingApproval: Int = 0
) 