package com.example.greenbuyapp.ui.profile.editProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.user.model.AddressResponse
import com.example.greenbuyapp.domain.user.UserRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddressAddViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    // Trạng thái địa chỉ sau khi thêm
    private val _newAddress = MutableStateFlow<AddressResponse?>(null)
    val newAddress: StateFlow<AddressResponse?> = _newAddress.asStateFlow()

    // Trạng thái loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Trạng thái lỗi
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Trạng thái thành công
    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    /**
     * Gọi API để thêm địa chỉ mới
     */
    fun addAddress(
        street: String,
        city: String,
        state: String,
        zipcode: String,
        country: String,
        phone: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isSuccess.value = false

            try {
                when (val result = userRepository.createAddress(
                    street = street,
                    city = city,
                    state = state,
                    zipcode = zipcode,
                    country = country,
                    phone = phone
                )) {
                    is Result.Success -> {
                        _newAddress.value = result.value
                        _isSuccess.value = true
                        println("✅ Địa chỉ được thêm thành công: ${result.value.id}")
                    }

                    is Result.Error -> {
                        val msg = "Lỗi khi thêm địa chỉ: ${result.error}"
                        _errorMessage.value = msg
                        println("❌ $msg")
                    }

                    is Result.NetworkError -> {
                        val msg = "Lỗi mạng, vui lòng kiểm tra kết nối"
                        _errorMessage.value = msg
                        println("🌐 $msg")
                    }

                    is Result.Loading -> {
                        // Loading đã được xử lý bằng biến isLoading
                    }
                }
            } catch (e: Exception) {
                val msg = "Lỗi không xác định: ${e.message}"
                _errorMessage.value = msg
                println("💥 $msg")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Xóa lỗi
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Reset trạng thái thêm thành công (sau khi thông báo xong)
     */
    fun resetSuccessFlag() {
        _isSuccess.value = false
    }
}
