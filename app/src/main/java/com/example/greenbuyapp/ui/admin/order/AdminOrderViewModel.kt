package com.example.greenbuyapp.ui.admin.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.shop.model.AdminOrder
import com.example.greenbuyapp.domain.shop.ShopRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminOrderViewModel(
    private val shopRepository: ShopRepository
) : ViewModel() {

    // ✅ Orders StateFlow
    private val _orders = MutableStateFlow<List<AdminOrder>>(emptyList())
    val orders: StateFlow<List<AdminOrder>> = _orders.asStateFlow()

    // ✅ Loading state
    private val _ordersLoading = MutableStateFlow(false)
    val ordersLoading: StateFlow<Boolean> = _ordersLoading.asStateFlow()

    // ✅ Error state
    private val _ordersError = MutableStateFlow<String?>(null)
    val ordersError: StateFlow<String?> = _ordersError.asStateFlow()

    // ✅ Pagination state
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _hasNextPage = MutableStateFlow(false)
    val hasNextPage: StateFlow<Boolean> = _hasNextPage.asStateFlow()

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private val _totalOrders = MutableStateFlow(0)
    val totalOrders: StateFlow<Int> = _totalOrders.asStateFlow()

    // ✅ Search and filter states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    private val _paymentStatusFilter = MutableStateFlow<String?>(null)
    val paymentStatusFilter: StateFlow<String?> = _paymentStatusFilter.asStateFlow()

    // ✅ Pagination constants
    private val pageSize = 10
    private var isLoadingMore = false

    /**
     * ✅ Load orders với pagination
     */
    fun loadOrders(isRefresh: Boolean = false) {
        if (isLoadingMore && !isRefresh) return

        viewModelScope.launch {
            try {
                val targetPage = if (isRefresh) 1 else _currentPage.value

                _ordersLoading.value = true
                _ordersError.value = null
                isLoadingMore = true

                println("🔄 Loading admin orders - page: $targetPage, refresh: $isRefresh")

                val result = shopRepository.getAdminOrders(
                    page = targetPage,
                    limit = pageSize,
                    status = _statusFilter.value,
                    paymentStatus = _paymentStatusFilter.value,
                    customerSearch = _searchQuery.value.takeIf { it.isNotBlank() }
                )

                when (result) {
                    is Result.Success -> {
                        val response = result.value
                        
                        // ✅ Update pagination info
                        _currentPage.value = response.page
                        _hasNextPage.value = response.hasNext
                        _totalPages.value = response.totalPages
                        _totalOrders.value = response.total

                        // ✅ Update orders list
                        if (isRefresh) {
                            _orders.value = response.items
                            println("✅ Refreshed orders: ${response.items.size} items")
                        } else {
                            val currentOrders = _orders.value.toMutableList()
                            currentOrders.addAll(response.items)
                            _orders.value = currentOrders
                            println("✅ Added more orders: ${response.items.size} items, total: ${currentOrders.size}")
                        }

                        println("📊 Pagination info: page=${response.page}, hasNext=${response.hasNext}, total=${response.total}")
                    }
                    is Result.Error -> {
                        _ordersError.value = result.error ?: "Lỗi khi tải danh sách đơn hàng"
                        println("❌ Load orders error: ${result.error}")
                    }
                    is Result.Loading -> {
                        // Already handled by _ordersLoading
                    }
                    is Result.NetworkError -> {
                        _ordersError.value = "Lỗi kết nối mạng"
                        println("❌ Network error")
                    }
                }
            } catch (e: Exception) {
                _ordersError.value = "Lỗi không xác định: ${e.message}"
                println("❌ Load orders exception: ${e.message}")
            } finally {
                _ordersLoading.value = false
                isLoadingMore = false
            }
        }
    }

    /**
     * ✅ Load more orders (infinite scroll)
     */
    fun loadMoreOrders() {
        if (!_hasNextPage.value || isLoadingMore) {
            println("⏸️ Cannot load more: hasNext=${_hasNextPage.value}, isLoading=$isLoadingMore")
            return
        }

        val nextPage = _currentPage.value + 1
        _currentPage.value = nextPage
        
        println("🔄 Loading more orders - page: $nextPage")
        loadOrders(isRefresh = false)
    }

    /**
     * ✅ Update search query
     */
    fun updateSearchQuery(query: String) {
        if (_searchQuery.value != query) {
            _searchQuery.value = query
            println("🔍 Search query updated: '$query'")
            
            // ✅ Reset pagination và reload
            _currentPage.value = 1
            loadOrders(isRefresh = true)
        }
    }

    /**
     * ✅ Update status filter
     */
    fun updateStatusFilter(status: String?) {
        if (_statusFilter.value != status) {
            _statusFilter.value = status
            println("🏷️ Status filter updated: '$status'")
            
            // ✅ Reset pagination và reload
            _currentPage.value = 1
            loadOrders(isRefresh = true)
        }
    }

    /**
     * ✅ Update payment status filter
     */
    fun updatePaymentStatusFilter(paymentStatus: String?) {
        if (_paymentStatusFilter.value != paymentStatus) {
            _paymentStatusFilter.value = paymentStatus
            println("💳 Payment status filter updated: '$paymentStatus'")
            
            // ✅ Reset pagination và reload
            _currentPage.value = 1
            loadOrders(isRefresh = true)
        }
    }

    /**
     * ✅ Clear all filters
     */
    fun clearFilters() {
        _searchQuery.value = ""
        _statusFilter.value = null
        _paymentStatusFilter.value = null
        _currentPage.value = 1
        
        println("🧹 All filters cleared")
        loadOrders(isRefresh = true)
    }

    /**
     * ✅ Refresh orders
     */
    fun refreshOrders() {
        println("🔄 Refreshing orders...")
        loadOrders(isRefresh = true)
    }

    /**
     * ✅ Update order status
     */
    fun updateOrderStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch {
            try {
                println("🔄 Updating order $orderId status to: $newStatus")
                
                val result = shopRepository.updateOrderStatus(orderId, newStatus)
                
                when (result) {
                    is Result.Success -> {
                        println("✅ Order status updated successfully")
                        
                        // ✅ Update local list
                        val currentOrders = _orders.value.toMutableList()
                        val index = currentOrders.indexOfFirst { it.id == orderId }
                        if (index != -1) {
                            val updatedOrder = currentOrders[index].copy(status = newStatus)
                            currentOrders[index] = updatedOrder
                            _orders.value = currentOrders
                        }
                        
                        // ✅ Optionally refresh to get latest data
                        refreshOrders()
                    }
                    is Result.Error -> {
                        _ordersError.value = result.error ?: "Lỗi khi cập nhật trạng thái đơn hàng"
                        println("❌ Update order status error: ${result.error}")
                    }
                    is Result.Loading -> {
                        // Loading state handled elsewhere
                    }
                    is Result.NetworkError -> {
                        _ordersError.value = "Lỗi kết nối mạng"
                        println("❌ Network error")
                    }
                }
            } catch (e: Exception) {
                _ordersError.value = "Lỗi không xác định: ${e.message}"
                println("❌ Update order status exception: ${e.message}")
            }
        }
    }

    /**
     * ✅ Clear error message
     */
    fun clearErrorMessage() {
        _ordersError.value = null
    }
} 