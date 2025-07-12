package com.example.greenbuyapp.ui.admin.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.shop.model.AdminOrderDetail
import com.example.greenbuyapp.domain.shop.ShopRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminOrderDetailViewModel(
    private val shopRepository: ShopRepository
) : ViewModel() {

    // ✅ Order detail state
    private val _orderDetail = MutableStateFlow<AdminOrderDetail?>(null)
    val orderDetail: StateFlow<AdminOrderDetail?> = _orderDetail.asStateFlow()

    // ✅ Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ✅ Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ✅ Update status loading
    private val _isUpdatingStatus = MutableStateFlow(false)
    val isUpdatingStatus: StateFlow<Boolean> = _isUpdatingStatus.asStateFlow()

    // ✅ Success message
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    /**
     * ✅ Load order detail by ID
     */
    fun loadOrderDetail(orderId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = shopRepository.getAdminOrderDetail(orderId)) {
                is Result.Success -> {
                    _orderDetail.value = result.value
                    println("✅ Order detail loaded successfully: ${result.value.orderNumber}")
                }
                is Result.Error -> {
                    _errorMessage.value = result.error
                    println("❌ Error loading order detail: ${result.error}")
                }
                is Result.NetworkError -> {
                    _errorMessage.value = "Lỗi kết nối mạng. Vui lòng thử lại."
                    println("🌐 Network error loading order detail")
                }
                is Result.Loading -> {
                    // Keep loading state
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * ✅ Update order status to next step
     */
    fun updateOrderToNextStatus(internalNotes: String? = null) {
        val currentOrder = _orderDetail.value ?: return
        
        if (!currentOrder.canUpdateToNextStatus()) {
            _errorMessage.value = "Không thể cập nhật trạng thái đơn hàng này"
            return
        }

        val nextStatus = currentOrder.getNextStatusNumber()
        updateOrderStatus(nextStatus, internalNotes)
    }

    /**
     * ✅ Update order status with specific status number
     */
    fun updateOrderStatus(status: Int, internalNotes: String? = null, notifyCustomer: Boolean = true) {
        val currentOrder = _orderDetail.value ?: return

        viewModelScope.launch {
            _isUpdatingStatus.value = true
            _errorMessage.value = null

            when (val result = shopRepository.updateAdminOrderStatus(
                orderId = currentOrder.id,
                status = status,
                internalNotes = internalNotes,
                notifyCustomer = notifyCustomer
            )) {
                is Result.Success -> {
                    _orderDetail.value = result.value
                    _successMessage.value = "Cập nhật trạng thái đơn hàng thành công"
                    println("✅ Order status updated successfully: ${result.value.status}")
                }
                is Result.Error -> {
                    _errorMessage.value = result.error
                    println("❌ Error updating order status: ${result.error}")
                }
                is Result.NetworkError -> {
                    _errorMessage.value = "Lỗi kết nối mạng. Vui lòng thử lại."
                    println("🌐 Network error updating order status")
                }
                is Result.Loading -> {
                    // Keep loading state
                }
            }

            _isUpdatingStatus.value = false
        }
    }

    /**
     * ✅ Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * ✅ Clear success message
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    /**
     * ✅ Refresh order detail
     */
    fun refreshOrderDetail() {
        val currentOrder = _orderDetail.value ?: return
        loadOrderDetail(currentOrder.id)
    }
} 