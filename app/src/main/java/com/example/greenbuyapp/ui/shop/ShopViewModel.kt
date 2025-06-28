package com.example.greenbuyapp.ui.shop

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.shop.model.OrderStats
import com.example.greenbuyapp.data.shop.model.Shop
import com.example.greenbuyapp.domain.shop.ShopRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShopViewModel(
    private val shopRepository: ShopRepository
) : ViewModel() {

    // ✅ Cleaned up - removed unused register-related StateFlows

    // Shop state
    private val _isShop = MutableStateFlow<Role>(Role.BUYER)
    val isShop: StateFlow<Role?> = _isShop.asStateFlow()

    // Shop state
    private val _shopInfo = MutableStateFlow<Shop?>(null)
    val shopInfo: StateFlow<Shop?> = _shopInfo.asStateFlow()

    // Shop state
    private val _shop = MutableStateFlow<Result<Shop>?>(null)
    val shop: StateFlow<Result<Shop>?> = _shop.asStateFlow()

    // isShop have info state
    private val _isShopInfo = MutableStateFlow<Boolean?>(null)
    val isShopInfo: StateFlow<Boolean?> = _isShopInfo.asStateFlow()
    
    // Create shop state
    private val _createShopState = MutableStateFlow<CreateShopUiState>(CreateShopUiState.Idle)
    val createShopState: StateFlow<CreateShopUiState> = _createShopState.asStateFlow()
    
    // Error message for UI
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Banner items
    private val _bannerItems = MutableStateFlow<List<Int>>(emptyList())
    val bannerItems: StateFlow<List<Int>> = _bannerItems.asStateFlow()

    // My Shop Stats
    private val _myShopStats = MutableStateFlow<OrderStats?>(null)
    val myShopStats: StateFlow<OrderStats?> = _myShopStats.asStateFlow()

    /**
     * Check isShop từ API
     */
    fun checkShop() {
        viewModelScope.launch {
            try {
                when(val result = shopRepository.getUserMe()) {
                    is Result.Success -> {
                        if (result.value.user.role == "seller") {
                            _isShop.value = Role.SELLER
                            return@launch
                        } else if (result.value.user.role == "buyer"){
                            _isShop.value = Role.BUYER
                            return@launch
                        } else if (result.value.user.role == "admin"){
                            _isShop.value = Role.ADMIN
                            return@launch
                        } else if (result.value.user.role == "moderator"){
                            _isShop.value = Role.MODERATOR
                            return@launch
                        }
                    }
                    is Result.Error -> {
                        val errorMsg = "Lỗi tải thông tin cửa hàng: ${result.error}"
                        println("❌ $errorMsg")
                    }
                    is Result.NetworkError -> {
                        val errorMsg = "Lỗi mạng, vui lòng kiểm tra kết nối"
                        println("🌐 $errorMsg")
                    }
                    is Result.Loading -> {
                    }
                }
            }
            catch (e : Exception) {
                val errorMsg = "Lỗi không xác định: ${e.message}"
                println("💥 $errorMsg")
            }
        }
    }
    /**
     * Load banner items - có thể từ API hoặc dữ liệu cố định
     */
    fun loadBannerItems() {
        // Tạo dữ liệu banner mẫu với 3 ảnh
        val bannerData = listOf(
            R.drawable.banner_1,
            R.drawable.banner_1,
            R.drawable.banner_1
        )

        _bannerItems.value = bannerData
        println("✅ Banner items loaded: ${bannerData.size} items")
    }

    /**
     * Check Shop Info từ API
     */
    fun shopInfo() {
        viewModelScope.launch {
            try {
                when(val result = shopRepository.getMyShop()) {
                    is Result.Success -> {
                        _shopInfo.value = result.value
                        _isShopInfo.value = true
                        return@launch
                    }
                    is Result.Error -> {
                        val errorMsg = "Lỗi tải thông tin cửa hàng: ${result.error}"
                        _isShopInfo.value = false
                        println("❌ $errorMsg")
                    }
                    is Result.NetworkError -> {
                        val errorMsg = "Lỗi mạng, vui lòng kiểm tra kết nối"
                        println("🌐 $errorMsg")
                    }
                    is Result.Loading -> {
                    }
                }
            }
            catch (e : Exception) {
                val errorMsg = "Lỗi không xác định: ${e.message}"
                println("💥 $errorMsg")
            }
        }
    }


    /**
     * Change Role tu API
     */
    fun changeRole() {
        viewModelScope.launch {
            try {
                println("🔄 Starting changeRole API call with newRole = 'seller'")
                when(val result = shopRepository.changeRole(newRole = "seller")) {
                    is Result.Success -> {
                        if (result.value.user.role == "seller") {
                            _isShop.value = Role.SELLER
                            return@launch
                        } else {
                            _isShop.value = Role.BUYER
                            return@launch
                        }
                        val successMsg = "Thay đổi vai trò thành công"
                        println("✅ $successMsg")
                    }
                    is Result.Error -> {
                        val errorMsg = "Lỗi tải thông tin change Role: ${result.error}"
                        println("❌ $errorMsg")
                    }
                    is Result.NetworkError -> {
                        val errorMsg = "Lỗi mạng, vui lòng kiểm tra kết nối"
                        println("🌐 $errorMsg")
                    }
                    is Result.Loading -> {
                    }
                }
            }
            catch (e : Exception) {
                val errorMsg = "Lỗi không xác định: ${e.message}"
                println("💥 $errorMsg")
            }
        }
    }


    // ✅ Cleaned up - removed unused form change handlers

    // ❌ Removed old register() function - replaced by createShop()
    
    /**
     * Tạo shop mới với avatar
     */
    fun createShop(
        context: Context,
        name: String,
        phoneNumber: String,
        avatarUri: Uri?
    ) {
        viewModelScope.launch {
            try {
                _createShopState.value = CreateShopUiState.Loading
                
                val result = shopRepository.createShop(
                    context = context,
                    name = name,
                    phoneNumber = phoneNumber,
                    avatarUri = avatarUri
                )
                
                when (result) {
                    is Result.Success -> {
                        _createShopState.value = CreateShopUiState.Success(result.value)
                        _shop.value = result
                        _isShopInfo.value = true
                        println("✅ Shop created successfully: ${result.value.name}")
                    }
                    is Result.Error -> {
                        val errorMsg = "Lỗi tạo shop: ${result.error}"
                        _createShopState.value = CreateShopUiState.Error(errorMsg)
                        _errorMessage.value = errorMsg
                        println("❌ $errorMsg")
                    }
                    is Result.NetworkError -> {
                        val errorMsg = "Lỗi mạng, vui lòng kiểm tra kết nối"
                        _createShopState.value = CreateShopUiState.Error(errorMsg)
                        _errorMessage.value = errorMsg
                        println("🌐 $errorMsg")
                    }
                    is Result.Loading -> {
                        // Already handled above
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Lỗi không xác định: ${e.message}"
                _createShopState.value = CreateShopUiState.Error(errorMsg)
                _errorMessage.value = errorMsg
                println("💥 $errorMsg")
            }
        }
    }

    /**
     * Lay thong ke shop tu api
     */
    fun getMyShopStats() {
        viewModelScope.launch {
            val result = shopRepository.getMyShopStats()
            when(result) {
                is Result.Success -> {
                    _myShopStats.value = result.value
                }
                is Result.Error -> {
                    val errorMsg = "Lỗi tải thông tin thống kê cửa hàng: ${result.error}"
                    println("❌ $errorMsg")
                }
                is Result.NetworkError -> {
                    val errorMsg = "Lỗi mạng, vui lòng kiểm tra kết nối"
                    println("🌐 $errorMsg")
                }
                is Result.Loading -> {
                }
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}

/**
 * UI State cho việc tạo shop
 */
sealed class CreateShopUiState {
    object Idle : CreateShopUiState()
    object Loading : CreateShopUiState()
    data class Success(val shop: Shop) : CreateShopUiState()
    data class Error(val message: String) : CreateShopUiState()
}

enum class Role {
    BUYER,
    SELLER,
    ADMIN,
    MODERATOR,
}