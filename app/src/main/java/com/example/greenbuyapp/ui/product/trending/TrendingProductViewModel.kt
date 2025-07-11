package com.example.greenbuyapp.ui.product.trending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.product.model.TrendingProduct
import com.example.greenbuyapp.domain.category.CategoryRepository
import com.example.greenbuyapp.domain.product.ProductRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrendingProductViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {
    private val _trendingProducts = MutableStateFlow<List<TrendingProduct>>(emptyList())
    val trendingProducts: StateFlow<List<TrendingProduct>> = _trendingProducts.asStateFlow()

    private val _trendingLoading = MutableStateFlow(false)
    val trendingLoading: StateFlow<Boolean> = _trendingLoading.asStateFlow()

    private val _trendingError = MutableStateFlow<String?>(null)
    val trendingError: StateFlow<String?> = _trendingError.asStateFlow()

    fun loadTrendingProducts(page: Int = 1, limit: Int = 10) {
        viewModelScope.launch {
            _trendingLoading.value = true
            _trendingError.value = null

            when (val result = productRepository.getTrending(page, limit)) {
                is com.example.greenbuyapp.util.Result.Success -> {
                    _trendingProducts.value = result.value.items
                    println("✅ Trending products loaded: ${result.value.items.size} items")
                }
                is com.example.greenbuyapp.util.Result.Error -> {
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
    fun reloadTrendingProducts() {
        loadTrendingProducts()
    }

    fun refeshTrendingProducts() {
        loadTrendingProducts(page = 1, limit = 10)
    }

}