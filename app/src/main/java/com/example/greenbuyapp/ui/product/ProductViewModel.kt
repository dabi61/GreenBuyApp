package com.example.greenbuyapp.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.product.model.ProductAttribute
import com.example.greenbuyapp.domain.product.ProductRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {
    
    // Product attributes state
    private val _productAttributes = MutableStateFlow<List<ProductAttribute>>(emptyList())
    val productAttributes: StateFlow<List<ProductAttribute>> = _productAttributes.asStateFlow()
    
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