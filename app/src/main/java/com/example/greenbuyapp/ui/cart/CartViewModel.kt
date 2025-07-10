package com.example.greenbuyapp.ui.cart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.cart.model.CartShop
import com.example.greenbuyapp.data.cart.model.PaymentResponse
import com.example.greenbuyapp.data.product.model.CreateAttributeResponse
import com.example.greenbuyapp.domain.cart.CartRepository
import com.example.greenbuyapp.ui.shop.addProduct.AddVariantUiState
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

sealed class PaymentUiState {
    object Idle : PaymentUiState()
    object Loading : PaymentUiState()
    data class Success(val paymentResponse: PaymentResponse) : PaymentUiState()
    data class Error(val message: String) : PaymentUiState()
}

class CartViewModel(
    private val cartRepository: CartRepository
) : ViewModel() {

    // Cart data state
    private val _cartShops = MutableStateFlow<List<CartShop>>(emptyList())
    val cartShops: StateFlow<List<CartShop>> = _cartShops.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Success message state
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Selected attribute IDs
    private val _selectedAttributeIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedAttributeIds: StateFlow<Set<Int>> = _selectedAttributeIds.asStateFlow()

    // UI State cho việc tạo variants
    private val _paymentState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
    val paymentState: StateFlow<PaymentUiState> = _paymentState.asStateFlow()

    init {
        loadCart()
    }

    /**
    * Payment
    */
    fun payment(cartShops: CartShop) {
        viewModelScope.launch {

        }
    }

    /**
     * Load giỏ hàng từ API
     */
    fun loadCart() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                when (val result = cartRepository.getCart()) {
                    is Result.Success -> {
                        _cartShops.value = result.value
                        println("✅ Cart loaded successfully: ${result.value.size} shops")
                    }
                    is Result.Error -> {
                        val errorMsg = result.error ?: "Lỗi không xác định"
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
     * Cập nhật số lượng sản phẩm
     */
    fun updateCartItemQuantity(attributeId: Int, newQuantity: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                when (val result = cartRepository.updateCartItem(attributeId, newQuantity)) {
                    is Result.Success -> {
                        _successMessage.value = result.value.message
                        // Reload cart để cập nhật UI
                        loadCart()
                        println("✅ Cart item updated: ${result.value.message}")
                    }
                    is Result.Error -> {
                        val errorMsg = result.error ?: "Lỗi không xác định"
                        _errorMessage.value = errorMsg
                        println("❌ $errorMsg")
                    }
                    is Result.NetworkError -> {
                        val errorMsg = "Lỗi mạng khi cập nhật sản phẩm"
                        _errorMessage.value = errorMsg
                        println("🌐 $errorMsg")
                    }
                    is Result.Loading -> {
                        // Loading được handle bởi isLoading state
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Lỗi cập nhật sản phẩm: ${e.message}"
                _errorMessage.value = errorMsg
                println("💥 $errorMsg")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Xóa shop khỏi giỏ hàng
     */
    fun deleteShopFromCart(shopId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                when (val result = cartRepository.deleteShopFromCart(shopId)) {
                    is Result.Success -> {
                        _successMessage.value = result.value.message
                        // Reload cart để cập nhật UI
                        loadCart()
                        println("✅ Shop deleted: ${result.value.message}")
                    }
                    is Result.Error -> {
                        val errorMsg = result.error ?: "Lỗi không xác định"
                        _errorMessage.value = errorMsg
                        println("❌ $errorMsg")
                    }
                    is Result.NetworkError -> {
                        val errorMsg = "Lỗi mạng khi xóa shop"
                        _errorMessage.value = errorMsg
                        println("🌐 $errorMsg")
                    }
                    is Result.Loading -> {
                        // Loading được handle bởi isLoading state
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Lỗi xóa shop: ${e.message}"
                _errorMessage.value = errorMsg
                println("💥 $errorMsg")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    fun deleteCartItem(attributeId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                when (val result = cartRepository.deleteCartItem(attributeId)) {
                    is Result.Success -> {
                        _successMessage.value = result.value.message
                        // Reload cart để cập nhật UI
                        loadCart()
                        println("✅ Cart item deleted: ${result.value.message}")
                    }
                    is Result.Error -> {
                        val errorMsg = result.error ?: "Lỗi không xác định"
                        _errorMessage.value = errorMsg
                        println("❌ $errorMsg")
                    }
                    is Result.NetworkError -> {
                        val errorMsg = "Lỗi mạng khi xóa sản phẩm"
                        _errorMessage.value = errorMsg
                        println("🌐 $errorMsg")
                    }
                    is Result.Loading -> {
                        // Loading được handle bởi isLoading state
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Lỗi xóa sản phẩm: ${e.message}"
                _errorMessage.value = errorMsg
                println("💥 $errorMsg")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Xóa tất cả giỏ hàng (xóa từng shop một)
     */
    fun clearAllCart() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val shops = _cartShops.value

                // Xóa từng shop
                for (shop in shops) {
                    cartRepository.deleteShopFromCart(shop.shopId)
                }

                _successMessage.value = "Đã xóa tất cả sản phẩm khỏi giỏ hàng"
                loadCart()
                println("✅ All cart cleared")
            } catch (e: Exception) {
                val errorMsg = "Lỗi xóa giỏ hàng: ${e.message}"
                _errorMessage.value = errorMsg
                println("💥 $errorMsg")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Tính tổng số lượng sản phẩm
     */
    fun getTotalItemCount(): Int {
        val selected = _selectedAttributeIds.value
        if (selected.isEmpty()) return 0

        return _cartShops.value.flatMap { it.items }
            .filter { selected.contains(it.attributeId) }
            .sumOf { it.quantity }
    }

    /**
     * Tính tổng tiền của giỏ hàng
     */
    fun getTotalAmount(): Double {
        val selected = _selectedAttributeIds.value
        if (selected.isEmpty()) return 0.0

        return _cartShops.value.flatMap { it.items }
            .filter { selected.contains(it.attributeId) }
            .sumOf { it.getTotalPrice() }
    }

    /**
     * Format tổng tiền
     */
    fun getFormattedTotalAmount(): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(getTotalAmount())
    }

    /**
     * Kiểm tra giỏ hàng có rỗng không
     */
    fun isCartEmpty(): Boolean {
        return _cartShops.value.isEmpty() || _cartShops.value.all { !it.hasItems() }
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    /**
     * Refresh cart
     */
    fun refresh() {
        loadCart()
    }

    private fun updateSelection(newSet: Set<Int>) {
        _selectedAttributeIds.value = newSet
    }

    fun toggleAttributeSelection(attributeId: Int, checked: Boolean) {
        val current = _selectedAttributeIds.value.toMutableSet()
        if (checked) current.add(attributeId) else current.remove(attributeId)
        updateSelection(current)
    }

    fun toggleShopSelection(shop: CartShop, checked: Boolean) {
        val current = _selectedAttributeIds.value.toMutableSet()
        if (checked) {
            shop.items.forEach { current.add(it.attributeId) }
        } else {
            shop.items.forEach { current.remove(it.attributeId) }
        }
        updateSelection(current)
    }

    fun clearSelection() { updateSelection(emptySet()) }

    fun getSelectedCartItems(): List<com.example.greenbuyapp.data.cart.model.CartItem> {
        val ids = _selectedAttributeIds.value
        return _cartShops.value.flatMap { it.items }.filter { ids.contains(it.attributeId) }
    }


    fun getSelectedCartShops(): List<CartShop> {
        val selectedIds = _selectedAttributeIds.value
        if (selectedIds.isEmpty()) return emptyList()
        
        return _cartShops.value.mapNotNull { shop ->
            val selectedItems = shop.items.filter { selectedIds.contains(it.attributeId) }
            if (selectedItems.isNotEmpty()) {
                shop.copy(items = selectedItems)
            } else {
                null
            }
        }
    }

    fun placeOrderAndPayAll(
        cartShops: List<CartShop>,
        shippingAddress: String,
        phoneNumber: String,
        recipientName: String,
        deliveryNotes: String,
        billingAddress: String
    ) {
        viewModelScope.launch {
            val paymentResults = mutableListOf<PaymentResponse>()
            for (shop in cartShops) {
                when (val result = cartRepository.placeOrderAndPay(
                    shop, shippingAddress, phoneNumber, recipientName, deliveryNotes, billingAddress
                )) {
                    is Result.Success -> {
                        _paymentState.value = PaymentUiState.Success(result.value)
                        paymentResults.add(result.value)
                        Log.d("Payment", result.value.toString())
                    }
                    is Result.Error -> {
                        _paymentState.value = PaymentUiState.Error(result.error ?: "Lỗi tạo payment")
                        Log.d("Payment", result.error.toString())
                        // Xử lý lỗi từng shop nếu cần
                    }
                    is Result.NetworkError -> {
                        _paymentState.value = PaymentUiState.Error("Lỗi kết nối mạng khi tạo variant")
                        Log.d("Payment", "Network error")
                        // Xử lý lỗi mạng nếu cần
                    }
                    is Result.Loading -> {}
                }
            }
            // paymentResults chứa kết quả thanh toán của tất cả shop
            // Có thể update UI hoặc thông báo thành công ở đây
        }
    }
} 