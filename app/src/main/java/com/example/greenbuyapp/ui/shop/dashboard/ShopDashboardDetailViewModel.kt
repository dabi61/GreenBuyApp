package com.example.greenbuyapp.ui.shop.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.shop.model.Order
import com.example.greenbuyapp.data.shop.model.OrderStats
import com.example.greenbuyapp.data.shop.model.OrderStatus
import com.example.greenbuyapp.domain.shop.ShopRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShopDashboardDetailViewModel(
    private val shopRepository: ShopRepository
) : ViewModel() {

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Order stats
    private val _orderStats = MutableStateFlow<OrderStats?>(null)
    val orderStats: StateFlow<OrderStats?> = _orderStats.asStateFlow()

    // Orders by status - sử dụng Map để quản lý dễ hơn
    private val _ordersByStatus = MutableStateFlow<Map<OrderStatus, List<Order>>>(emptyMap())

    /**
     * Get orders by status
     */
    fun getOrdersByStatus(status: OrderStatus): StateFlow<List<Order>> {
        val statusFlow = MutableStateFlow<List<Order>>(emptyList())
        
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
    fun loadOrdersByStatus(status: OrderStatus) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                println("📦 Loading orders for status: ${status.displayName} (filter: ${status.statusFilter})")
                
                when (val result = shopRepository.getShopOrders(
                    statusFilter = status.statusFilter,
                    page = 1,
                    limit = 50 // Load nhiều hơn để có đủ data
                )) {
                    is Result.Success -> {
                        val response = result.value
                        
                        // Update stats
                        _orderStats.value = response.stats
                        
                        // Update orders for this status
                        val currentMap = _ordersByStatus.value.toMutableMap()
                        currentMap[status] = response.items
                        _ordersByStatus.value = currentMap
                        
                        println("✅ Orders loaded successfully for ${status.displayName}: ${response.items.size} items")
                        println("📊 Stats: Total=${response.stats.totalOrders}, Pending=${response.stats.pendingOrders}")
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
                
                println("📦 Loading all orders...")
                
                // Load orders without status filter để lấy tất cả
                when (val result = shopRepository.getShopOrders(
                    statusFilter = null, // Load tất cả
                    page = 1,
                    limit = 100
                )) {
                    is Result.Success -> {
                        val response = result.value
                        
                        // Update stats
                        _orderStats.value = response.stats
                        
                        // Group orders by status
                        val groupedOrders = response.items.groupBy { it.orderStatus }
                        _ordersByStatus.value = groupedOrders
                        
                        println("✅ All orders loaded successfully: ${response.items.size} total items")
                        groupedOrders.forEach { (status, orders) ->
                            println("   ${status.displayName}: ${orders.size} orders")
                        }
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
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Refresh orders for specific status
     */
    fun refreshOrdersByStatus(status: OrderStatus) {
        loadOrdersByStatus(status)
    }

    /**
     * Search orders by date range
     */
    fun searchOrdersByDateRange(
        status: OrderStatus,
        dateFrom: String? = null,
        dateTo: String? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                when (val result = shopRepository.getShopOrders(
                    statusFilter = status.statusFilter,
                    page = 1,
                    limit = 50,
                    dateFrom = dateFrom,
                    dateTo = dateTo
                )) {
                    is Result.Success -> {
                        val currentMap = _ordersByStatus.value.toMutableMap()
                        currentMap[status] = result.value.items
                        _ordersByStatus.value = currentMap
                        
                        println("🔍 Search completed for ${status.displayName}: ${result.value.items.size} items")
                    }
                    is Result.Error -> {
                        _errorMessage.value = "Lỗi tìm kiếm: ${result.error}"
                    }
                    is Result.NetworkError -> {
                        _errorMessage.value = "Lỗi mạng"
                    }
                    is Result.Loading -> {
                        // Handled above
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi tìm kiếm: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}