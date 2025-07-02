package com.example.greenbuyapp.ui.profile.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.user.model.CustomerOrder
import com.example.greenbuyapp.data.user.model.CustomerOrderStatus
import com.example.greenbuyapp.domain.user.UserRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CustomerOrderViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Orders by status - sử dụng Map để quản lý dễ hơn
    private val _ordersByStatus = MutableStateFlow<Map<CustomerOrderStatus, List<CustomerOrder>>>(emptyMap())

    /**
     * Get orders by status
     */
    fun getOrdersByStatus(status: CustomerOrderStatus): StateFlow<List<CustomerOrder>> {
        val statusFlow = MutableStateFlow<List<CustomerOrder>>(emptyList())
        
        viewModelScope.launch {
            _ordersByStatus.collect { ordersMap ->
                statusFlow.value = ordersMap[status] ?: emptyList()
            }
        }
        
        return statusFlow.asStateFlow()
    }

    /**
     * Load orders by status từ API
     */
    fun loadOrdersByStatus(status: CustomerOrderStatus) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                println("📦 Loading customer orders for status: ${status.displayName} (filter: ${status.statusFilter})")
                
                when (val result = userRepository.getCustomerOrders(
                    statusFilter = status.statusFilter,
                    page = 1,
                    limit = 50 // Load nhiều hơn để có đủ data
                )) {
                    is Result.Success -> {
                        val response = result.value
                        
                        // Update orders for this status
                        val currentMap = _ordersByStatus.value.toMutableMap()
                        currentMap[status] = response.items
                        _ordersByStatus.value = currentMap
                        
                        println("✅ Customer orders loaded successfully for ${status.displayName}: ${response.items.size} items")
                        println("📊 Total: ${response.total}, Page: ${response.page}/${response.totalPages}")
                    }
                    is Result.Error -> {
                        val errorMsg = "Lỗi tải đơn hàng: ${result.error}"
                        _errorMessage.value = errorMsg
                        println("❌ $errorMsg")
                    }
                    is Result.NetworkError -> {
                        val errorMsg = "Lỗi mạng, vui lòng kiểm tra kết nối"
                        _errorMessage.value = errorMsg
                        println("🌐 $errorMsg")
                    }
                    is Result.Loading -> {
                        // Already handled above
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
     * Load all orders for all statuses
     */
    fun loadAllOrders() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                println("📦 Loading all customer orders...")
                
                // Load từng status một
                val allStatuses = CustomerOrderStatus.getAllStatuses()
                val allOrdersMap = mutableMapOf<CustomerOrderStatus, List<CustomerOrder>>()
                
                allStatuses.forEach { status ->
                    when (val result = userRepository.getCustomerOrders(
                        statusFilter = status.statusFilter,
                        page = 1,
                        limit = 50
                    )) {
                        is Result.Success -> {
                            allOrdersMap[status] = result.value.items
                            println("   ${status.displayName}: ${result.value.items.size} orders")
                        }
                        is Result.Error -> {
                            println("❌ Error loading ${status.displayName}: ${result.error}")
                            allOrdersMap[status] = emptyList()
                        }
                        is Result.NetworkError -> {
                            println("🌐 Network error loading ${status.displayName}")
                            allOrdersMap[status] = emptyList()
                        }
                        is Result.Loading -> {
                            // Skip
                        }
                    }
                }
                
                _ordersByStatus.value = allOrdersMap
                println("✅ All customer orders loaded successfully")
                
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
     * Refresh orders for specific status
     */
    fun refreshOrdersByStatus(status: CustomerOrderStatus) {
        loadOrdersByStatus(status)
    }
} 