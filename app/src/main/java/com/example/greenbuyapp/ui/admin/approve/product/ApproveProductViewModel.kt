package com.example.greenbuyapp.ui.admin.approve.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.product.model.PendingApprovalProduct
import com.example.greenbuyapp.domain.product.ProductRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ApproveProductViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    // Danh sách sản phẩm chưa duyệt
    private val _pendingProducts = MutableStateFlow<List<PendingApprovalProduct>>(emptyList())
    val pendingProducts: StateFlow<List<PendingApprovalProduct>> = _pendingProducts.asStateFlow()

    // Sản phẩm hiện tại đang xem
    private val _currentProduct = MutableStateFlow<PendingApprovalProduct?>(null)
    val currentProduct: StateFlow<PendingApprovalProduct?> = _currentProduct.asStateFlow()

    // Index của sản phẩm hiện tại
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    // Trạng thái loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Thông báo lỗi
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Trạng thái duyệt
    private val _approvalState = MutableStateFlow<ApprovalState>(ApprovalState.Idle)
    val approvalState: StateFlow<ApprovalState> = _approvalState.asStateFlow()

    // Thống kê
    private val _stats = MutableStateFlow(ApprovalStats())
    val stats: StateFlow<ApprovalStats> = _stats.asStateFlow()

    init {
        loadPendingProducts()
    }

    /**
     * ✅ Load danh sách sản phẩm chưa duyệt từ API mới
     */
    private fun loadPendingProducts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                when (val result = productRepository.getPendingApprovalProducts()) {
                    is Result.Success -> {
                        val pendingProducts = result.value
                        
                        // ✅ Load thông tin shop cho từng sản phẩm
                        loadShopInfoForProducts(pendingProducts)
                        
                        _pendingProducts.value = pendingProducts
                        
                        if (pendingProducts.isNotEmpty()) {
                            _currentProduct.value = pendingProducts[0]
                            _currentIndex.value = 0
                        } else {
                            _currentProduct.value = null
                            _currentIndex.value = 0
                        }
                        
                        println("✅ Loaded ${pendingProducts.size} pending products")
                    }
                    is Result.Error -> {
                        val errorMsg = "Lỗi tải danh sách sản phẩm: ${result.error}"
                        _errorMessage.value = errorMsg
                        println("❌ $errorMsg")
                    }
                    is Result.NetworkError -> {
                        val errorMsg = "Lỗi mạng, vui lòng kiểm tra kết nối"
                        _errorMessage.value = errorMsg
                        println("🌐 $errorMsg")
                    }
                    is Result.Loading -> {
                        // Đã xử lý bằng _isLoading
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Lỗi không xác định: ${e.message}"
                _errorMessage.value = errorMsg
                println("💥 $errorMsg")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Duyệt sản phẩm (approve)
     */
    fun approveProduct(productId: Int, approvalNote: String = "Đã duyệt") {
        viewModelScope.launch {
            try {
                _approvalState.value = ApprovalState.Loading
                
                // ✅ Kiểm tra sản phẩm còn pending không
                val currentProduct = _currentProduct.value
                if (currentProduct == null || currentProduct.product_id != productId) {
                    _errorMessage.value = "Sản phẩm không tồn tại hoặc đã được xử lý"
                    _approvalState.value = ApprovalState.Error
                    return@launch
                }
                
                when (val result = productRepository.approveProduct(productId, approvalNote)) {
                    is Result.Success -> {
                        handleApprovalSuccess(true)
                    }
                    is Result.Error -> {
                        val errorMsg = if (result.error?.contains("already", ignoreCase = true) == true || 
                                         result.error?.contains("đã được", ignoreCase = true) == true) {
                            "Sản phẩm đã được duyệt bởi admin khác"
                        } else {
                            "Lỗi duyệt sản phẩm: ${result.error ?: "Không rõ lỗi"}"
                        }
                        _errorMessage.value = errorMsg
                        _approvalState.value = ApprovalState.Error
                        
                        // ✅ Refresh danh sách nếu bị conflict
                        if (errorMsg.contains("đã được duyệt")) {
                            refreshPendingProducts()
                        }
                    }
                    is Result.NetworkError -> {
                        _errorMessage.value = "Lỗi mạng, vui lòng kiểm tra kết nối"
                        _approvalState.value = ApprovalState.Error
                    }
                    is Result.Loading -> {
                        // Đã xử lý bằng _approvalState
                    }
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi không xác định: ${e.message}"
                _approvalState.value = ApprovalState.Error
            }
        }
    }

    /**
     * Từ chối sản phẩm (reject)
     */
    fun rejectProduct(productId: Int, reason: String = "Đã từ chối") {
        viewModelScope.launch {
            try {
                _approvalState.value = ApprovalState.Loading
                
                // ✅ Kiểm tra sản phẩm còn pending không
                val currentProduct = _currentProduct.value
                if (currentProduct == null || currentProduct.product_id != productId) {
                    _errorMessage.value = "Sản phẩm không tồn tại hoặc đã được xử lý"
                    _approvalState.value = ApprovalState.Error
                    return@launch
                }
                
                when (val result = productRepository.rejectProduct(productId, reason)) {
                    is Result.Success -> {
                        handleApprovalSuccess(false)
                    }
                    is Result.Error -> {
                        val errorMsg = if (result.error?.contains("already", ignoreCase = true) == true || 
                                         result.error?.contains("đã được", ignoreCase = true) == true) {
                            "Sản phẩm đã được xử lý bởi admin khác"
                        } else {
                            "Lỗi từ chối sản phẩm: ${result.error ?: "Không rõ lỗi"}"
                        }
                        _errorMessage.value = errorMsg
                        _approvalState.value = ApprovalState.Error
                        
                        // ✅ Refresh danh sách nếu bị conflict
                        if (errorMsg.contains("đã được")) {
                            refreshPendingProducts()
                        }
                    }
                    is Result.NetworkError -> {
                        _errorMessage.value = "Lỗi mạng, vui lòng kiểm tra kết nối"
                        _approvalState.value = ApprovalState.Error
                    }
                    is Result.Loading -> {
                        // Đã xử lý bằng _approvalState
                    }
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi không xác định: ${e.message}"
                _approvalState.value = ApprovalState.Error
            }
        }
    }

    /**
     * Xử lý khi duyệt/từ chối thành công
     */
    private fun handleApprovalSuccess(isApproved: Boolean) {
        val currentProduct = _currentProduct.value
        if (currentProduct != null) {
            // Cập nhật thống kê
            val currentStats = _stats.value
            _stats.value = if (isApproved) {
                currentStats.copy(approvedCount = currentStats.approvedCount + 1)
            } else {
                currentStats.copy(rejectedCount = currentStats.rejectedCount + 1)
            }
            
            // ✅ Xóa sản phẩm khỏi danh sách và cập nhật current product ngay lập tức
            val updatedProducts = _pendingProducts.value.toMutableList()
            val currentIndex = _currentIndex.value
            
            if (currentIndex >= 0 && currentIndex < updatedProducts.size) {
                updatedProducts.removeAt(currentIndex)
                _pendingProducts.value = updatedProducts
                
                // ✅ Cập nhật current product ngay lập tức
                if (updatedProducts.isNotEmpty()) {
                    // Nếu còn sản phẩm
                    if (currentIndex < updatedProducts.size) {
                        // Nếu index hiện tại vẫn hợp lệ, giữ nguyên index
                        println("🔄 Setting current product to index $currentIndex: ${updatedProducts[currentIndex].product_id}")
                        _currentProduct.value = updatedProducts[currentIndex]
                    } else {
                        // Nếu đã ở cuối danh sách, về đầu danh sách
                        println("🔄 Resetting to first product: ${updatedProducts[0].product_id}")
                        _currentIndex.value = 0
                        _currentProduct.value = updatedProducts[0]
                    }
                    println("✅ Moved to next product: ${_currentIndex.value + 1}/${updatedProducts.size}")
                } else {
                    // Không còn sản phẩm nào
                    _currentProduct.value = null
                    _currentIndex.value = 0
                    println("🏁 No more products to review")
                }
            }
        }
        
        _approvalState.value = ApprovalState.Success(isApproved)
    }

    /**
     * ✅ Refresh danh sách pending products từ server
     */
    private fun refreshPendingProducts() {
        viewModelScope.launch {
            try {
                when (val result = productRepository.getPendingApprovalProducts()) {
                    is Result.Success -> {
                        val pendingProducts = result.value
                        
                        // ✅ Load thông tin shop cho từng sản phẩm
                        loadShopInfoForProducts(pendingProducts)
                        
                        _pendingProducts.value = pendingProducts
                        
                        // ✅ Cập nhật current product
                        val currentIndex = _currentIndex.value
                        if (pendingProducts.isNotEmpty() && currentIndex < pendingProducts.size) {
                            _currentProduct.value = pendingProducts[currentIndex]
                        } else if (pendingProducts.isNotEmpty()) {
                            _currentIndex.value = 0
                            _currentProduct.value = pendingProducts[0]
                        } else {
                            _currentProduct.value = null
                            _currentIndex.value = 0
                        }
                        
                        println("🔄 Refreshed: ${pendingProducts.size} pending products")
                    }
                    is Result.Error -> {
                        println("❌ Failed to refresh: ${result.error}")
                    }
                    is Result.NetworkError -> {
                        println("🌐 Network error during refresh")
                    }
                    is Result.Loading -> {
                        // Skip
                    }
                }
            } catch (e: Exception) {
                println("💥 Exception during refresh: ${e.message}")
            }
        }
    }

    /**
     * Chuyển sang sản phẩm tiếp theo
     */
    fun moveToNextProduct() {
        val currentIndex = _currentIndex.value
        val products = _pendingProducts.value
        
        if (products.isNotEmpty()) {
            if (currentIndex < products.size - 1) {
                val nextIndex = currentIndex + 1
                _currentIndex.value = nextIndex
                _currentProduct.value = products[nextIndex]
                println("➡️ Moved to next product: ${nextIndex + 1}/${products.size}")
            } else {
                // Đã xem hết sản phẩm - reset hoặc load thêm
                _currentProduct.value = null
                _currentIndex.value = 0
                println("🔚 Reached end of products")
            }
        } else {
            _currentProduct.value = null
            _currentIndex.value = 0
            println("📝 No products available")
        }
    }

    /**
     * Chuyển về sản phẩm trước đó
     */
    fun moveToPreviousProduct() {
        val currentIndex = _currentIndex.value
        val products = _pendingProducts.value
        
        if (products.isNotEmpty() && currentIndex > 0) {
            val prevIndex = currentIndex - 1
            _currentIndex.value = prevIndex
            _currentProduct.value = products[prevIndex]
            println("⬅️ Moved to previous product: ${prevIndex + 1}/${products.size}")
        } else {
            println("⏪ Already at first product or no products available")
        }
    }

    /**
     * ✅ Nhảy tới sản phẩm specific
     */
    fun jumpToProduct(index: Int) {
        val products = _pendingProducts.value
        
        if (index >= 0 && index < products.size) {
            _currentIndex.value = index
            _currentProduct.value = products[index]
            println("🎯 Jumped to product: ${index + 1}/${products.size}")
        } else {
            println("❌ Invalid product index: $index (available: ${products.size})")
        }
    }

    /**
     * Clear error message và reset approval state
     */
    fun clearError() {
        _errorMessage.value = null
        _approvalState.value = ApprovalState.Idle
    }

    /**
     * Refresh danh sách sản phẩm (public method)
     */
    fun refresh() {
        // Reset stats when refreshing
        _stats.value = ApprovalStats()
        loadPendingProducts()
    }

    /**
     * ✅ Kiểm tra sản phẩm hiện tại có còn valid không
     */
    fun validateCurrentProduct(): Boolean {
        val currentProduct = _currentProduct.value
        val products = _pendingProducts.value
        val currentIndex = _currentIndex.value
        
        return currentProduct != null && 
               currentIndex >= 0 && 
               currentIndex < products.size &&
               products[currentIndex].product_id == currentProduct.product_id
    }

    /**
     * ✅ Get status summary
     */
    fun getStatusSummary(): String {
        val stats = _stats.value
        val remaining = _pendingProducts.value.size
        return "Đã xử lý: ${stats.totalProcessed} | Còn lại: $remaining"
    }

    /**
     * ✅ Debug method để kiểm tra trạng thái
     */
    fun debugCurrentState(): String {
        val currentProduct = _currentProduct.value
        val products = _pendingProducts.value
        val currentIndex = _currentIndex.value
        
        return """
            Current Product: ${currentProduct?.product_id ?: "null"}
            Current Index: $currentIndex
            Total Products: ${products.size}
            Product IDs: ${products.map { it.product_id }}
        """.trimIndent()
    }

    /**
     * ✅ Load thông tin shop cho danh sách sản phẩm
     */
    private suspend fun loadShopInfoForProducts(products: List<PendingApprovalProduct>) {
        products.forEach { product ->
            try {
                when (val shopResult = productRepository.getShopById(product.shop_id)) {
                    is Result.Success -> {
                        product.shopInfo = shopResult.value
                        println("✅ Loaded shop info for product ${product.product_id}: ${shopResult.value.name}")
                    }
                    is Result.Error -> {
                        println("❌ Failed to load shop info for product ${product.product_id}: ${shopResult.error}")
                    }
                    is Result.NetworkError -> {
                        println("🌐 Network error loading shop info for product ${product.product_id}")
                    }
                    is Result.Loading -> {
                        // Skip
                    }
                }
            } catch (e: Exception) {
                println("💥 Exception loading shop info for product ${product.product_id}: ${e.message}")
            }
        }
    }

    /**
     * ✅ Auto-refresh every 30 seconds to sync with server (with proper cleanup)
     */
    private var autoRefreshJob: kotlinx.coroutines.Job? = null
    
    private fun startAutoRefresh() {
        // Cancel existing job if any
        autoRefreshJob?.cancel()
        
        autoRefreshJob = viewModelScope.launch {
            try {
                while (true) {
                    kotlinx.coroutines.delay(30000) // 30 seconds
                    refreshPendingProducts()
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                println("🔄 Auto-refresh cancelled")
            } catch (e: Exception) {
                println("❌ Auto-refresh error: ${e.message}")
            }
        }
    }

    /**
     * ✅ Enable/disable auto refresh
     */
    fun setAutoRefresh(enabled: Boolean) {
        if (enabled) {
            startAutoRefresh()
        } else {
            autoRefreshJob?.cancel()
            autoRefreshJob = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up auto-refresh when ViewModel is destroyed
        autoRefreshJob?.cancel()
    }
}

/**
 * Trạng thái duyệt sản phẩm
 */
sealed class ApprovalState {
    object Idle : ApprovalState()
    object Loading : ApprovalState()
    data class Success(val isApproved: Boolean) : ApprovalState()
    object Error : ApprovalState()
}

/**
 * Thống kê duyệt sản phẩm
 */
data class ApprovalStats(
    val approvedCount: Int = 0,
    val rejectedCount: Int = 0
) {
    val totalProcessed: Int
        get() = approvedCount + rejectedCount
}