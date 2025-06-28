package com.example.greenbuyapp.ui.shop.productManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.product.model.Product
import com.example.greenbuyapp.data.product.model.ProductStatus
import com.example.greenbuyapp.data.product.model.InventoryStatsResponse
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
        // Tạo StateFlow mới cho mỗi status
        val statusFlow = MutableStateFlow<List<Product>>(emptyList())
        
        // Load products khi StateFlow được tạo
        viewModelScope.launch {
            loadProductsByStatus(status, statusFlow)
        }
        
        return statusFlow.asStateFlow()
    }

    /**
     * Load products by status from API
     */
    private suspend fun loadProductsByStatus(status: ProductStatus, statusFlow: MutableStateFlow<List<Product>>) {
        runCatching {
            val apiStatus = status.apiValue
            
            when (val result = productRepository.getProductsByStatus(apiStatus)) {
                is Result.Success -> {
                    val products = result.value.items
                    println("🔍 API Request - by-status: requesting status='${apiStatus}' for ${status.displayName}")
                    println("🔍 API Response - by-status: received ${products.size} products")
                    products.forEach { product ->
                        println("   📦 Product: ${product.name}, is_approved=${product.is_approved}, isApproved=${product.isApproved}, stock_status=${product.stock_info?.status}")
                    }
                    statusFlow.value = products
                    println("✅ Loaded ${products.size} products for status: ${status.displayName}")
                }
                is Result.Error -> {
                    _errorMessage.value = "Lỗi khi tải sản phẩm ${status.displayName}: ${result.error}"
                    println("❌ Error loading products for ${status.displayName}: ${result.error}")
                }
                is Result.Loading -> {
                    // Loading state can be handled if needed
                }
                is Result.NetworkError -> {
                    _errorMessage.value = "Lỗi kết nối mạng khi tải sản phẩm ${status.displayName}"
                    println("❌ Network error loading products for ${status.displayName}")
                }
            }
        }.onFailure { exception ->
            _errorMessage.value = "Lỗi khi tải sản phẩm ${status.displayName}: ${exception.message}"
            println("❌ Error loading products for ${status.displayName}: ${exception.message}")
        }
    }

    /**
     * Update product counts from API stats
     */
    private fun updateProductCountsFromAPI(stats: com.example.greenbuyapp.data.product.model.InventoryStatsSummary) {
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


}

/**
 * Data class for product counts
 */
data class ProductCounts(
    val inStock: Int = 0,
    val outOfStock: Int = 0,
    val pendingApproval: Int = 0
) 