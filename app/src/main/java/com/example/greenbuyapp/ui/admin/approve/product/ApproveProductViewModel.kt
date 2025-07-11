package com.example.greenbuyapp.ui.admin.approve.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.product.model.Product
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
    private val _pendingProducts = MutableStateFlow<List<Product>>(emptyList())
    val pendingProducts: StateFlow<List<Product>> = _pendingProducts.asStateFlow()

    // Sản phẩm hiện tại đang xem
    private val _currentProduct = MutableStateFlow<Product?>(null)
    val currentProduct: StateFlow<Product?> = _currentProduct.asStateFlow()

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
     * Load danh sách sản phẩm chưa duyệt
     */
    fun loadPendingProducts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                when (val result = productRepository.getProducts(
                    page = 1,
                    limit = 50, // Load nhiều sản phẩm một lần
                    approvedOnly = false // Chỉ lấy sản phẩm chưa duyệt
                )) {
                    is Result.Success -> {
                        val allProducts = result.value.items
                        // Filter chỉ lấy sản phẩm chưa duyệt
                        val pendingProducts = allProducts.filter { !it.isApproved }
                        _pendingProducts.value = pendingProducts
                        
                        if (pendingProducts.isNotEmpty()) {
                            _currentProduct.value = pendingProducts[0]
                            _currentIndex.value = 0
                        }
                        
                        println("✅ Loaded ${pendingProducts.size} pending products from ${allProducts.size} total")
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
                
                when (val result = productRepository.approveProduct(productId, approvalNote)) {
                    is Result.Success -> {
                        handleApprovalSuccess(true)
                    }
                    is Result.Error -> {
                        _errorMessage.value = "Lỗi duyệt sản phẩm: ${result.error}"
                        _approvalState.value = ApprovalState.Error
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
                
                when (val result = productRepository.rejectProduct(productId, reason)) {
                    is Result.Success -> {
                        handleApprovalSuccess(false)
                    }
                    is Result.Error -> {
                        _errorMessage.value = "Lỗi từ chối sản phẩm: ${result.error}"
                        _approvalState.value = ApprovalState.Error
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
            if (isApproved) {
                _stats.value = _stats.value.copy(
                    approvedCount = _stats.value.approvedCount + 1
                )
            } else {
                _stats.value = _stats.value.copy(
                    rejectedCount = _stats.value.rejectedCount + 1
                )
            }
            
            // Xóa sản phẩm khỏi danh sách
            val updatedProducts = _pendingProducts.value.toMutableList()
            updatedProducts.removeAt(_currentIndex.value)
            _pendingProducts.value = updatedProducts
            
            // Chuyển sang sản phẩm tiếp theo
            moveToNextProduct()
        }
        
        _approvalState.value = ApprovalState.Success(isApproved)
    }

    /**
     * Chuyển sang sản phẩm tiếp theo
     */
    fun moveToNextProduct() {
        val currentIndex = _currentIndex.value
        val products = _pendingProducts.value
        
        if (currentIndex < products.size - 1) {
            _currentIndex.value = currentIndex + 1
            _currentProduct.value = products[currentIndex + 1]
        } else {
            // Đã xem hết sản phẩm
            _currentProduct.value = null
            _currentIndex.value = 0
        }
    }

    /**
     * Chuyển về sản phẩm trước đó
     */
    fun moveToPreviousProduct() {
        val currentIndex = _currentIndex.value
        
        if (currentIndex > 0) {
            _currentIndex.value = currentIndex - 1
            _currentProduct.value = _pendingProducts.value[currentIndex - 1]
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
        _approvalState.value = ApprovalState.Idle
    }

    /**
     * Refresh danh sách sản phẩm
     */
    fun refresh() {
        loadPendingProducts()
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