package com.example.greenbuyapp.ui.profile.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.user.model.CustomerOrderDetail
import com.example.greenbuyapp.domain.user.UserRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CustomerOrderDetailViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    // Order detail state
    private val _orderDetail = MutableStateFlow<CustomerOrderDetail?>(null)
    val orderDetail: StateFlow<CustomerOrderDetail?> = _orderDetail.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()


    /**
     * Load chi tiết đơn hàng từ API
     */
    fun loadOrderDetail(orderId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                when (val result = userRepository.getCustomerOrderDetail(orderId)) {
                    is Result.Success -> {
                        _orderDetail.value = result.value
                        println("✅ Customer order detail loaded successfully: ${result.value.orderNumber}")
                    }
                    is Result.Error -> {
                        val errorMsg = "Lỗi tải chi tiết đơn hàng: ${result.error}"
                        _errorMessage.value = errorMsg
                        println("❌ $errorMsg")
                    }
                    is Result.NetworkError -> {
                        val errorMsg = "Lỗi mạng, vui lòng kiểm tra kết nối"
                        _errorMessage.value = errorMsg
                        println("🌐 $errorMsg")
                    }
                    is Result.Loading -> {
                        // Loading được handle bởi isLoading state
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
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Refresh order detail
     */
    fun refresh() {
        _orderDetail.value?.let { order ->
            loadOrderDetail(order.id)
        }
    }
} 