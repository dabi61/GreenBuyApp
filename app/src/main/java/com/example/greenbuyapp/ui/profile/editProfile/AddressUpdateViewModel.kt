package com.example.greenbuyapp.ui.profile.editProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.user.model.AddressDetailResponse
import com.example.greenbuyapp.data.user.model.AddressResponse
import com.example.greenbuyapp.data.user.model.AddressUpdateRequest
import com.example.greenbuyapp.domain.user.UserRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddressUpdateViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _address = MutableStateFlow<AddressDetailResponse?>(null)
    val address: StateFlow<AddressDetailResponse?> = _address.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Lấy thông tin địa chỉ theo ID
     */
    fun getAddressById(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                when (val result = userRepository.getAddressById(id)) {
                    is Result.Success -> {
                        _address.value = result.value
                        println("✅ Lấy địa chỉ thành công: ID $id")
                    }
                    is Result.Error -> {
                        _errorMessage.value = "Lỗi khi lấy địa chỉ: ${result.error}"
                    }
                    is Result.NetworkError -> {
                        _errorMessage.value = "Lỗi mạng khi lấy địa chỉ"
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi không xác định: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cập nhật địa chỉ
     */
    fun updateAddress(id: Int, request: AddressUpdateRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _updateSuccess.value = false

            try {
                // Nếu đang đặt địa chỉ này làm mặc định
                if (request.isDefault) {
                    // Bước 1: Lấy danh sách tất cả địa chỉ
                    when (val allAddressesResult = userRepository.getListAddress()) {
                        is Result.Success -> {
                            // Bước 2: Tìm các địa chỉ khác đang là mặc định
                            val otherDefaultAddresses = allAddressesResult.value
                                .filter { it.id != id && it.is_default }

                            // Bước 3: Cập nhật các địa chỉ khác thành không mặc định
                            for (address in otherDefaultAddresses) {
                                val updateOtherRequest = AddressUpdateRequest(
                                    street = address.street,
                                    city = address.city,
                                    state = address.state,
                                    zipcode = address.zipcode,
                                    country = address.country,
                                    phoneNumber = address.phone,
                                    isDefault = false // Đặt thành không mặc định
                                )

                                // Gọi API cập nhật cho từng địa chỉ
                                userRepository.updateAddress(address.id, updateOtherRequest)
                            }
                        }
                        is Result.Error -> {
                            _errorMessage.value = "Lỗi khi lấy danh sách địa chỉ: ${allAddressesResult.error}"
                            return@launch
                        }
                        else -> {
                            // Xử lý các trường hợp khác nếu cần
                        }
                    }
                }

                // Bước 4: Cập nhật địa chỉ hiện tại
                when (val result = userRepository.updateAddress(id, request)) {
                    is Result.Success -> {
                        _address.value = result.value
                        _updateSuccess.value = true
                    }
                    is Result.Error -> {
                        _errorMessage.value = "Lỗi khi cập nhật: ${result.error}"
                    }
                    is Result.NetworkError -> {
                        _errorMessage.value = "Lỗi mạng khi cập nhật"
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi không xác định: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    /**
     * Xoá thông báo lỗi (nếu có)
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Gọi lại nếu cần reset trạng thái cập nhật
     */
    fun resetUpdateStatus() {
        _updateSuccess.value = false
    }
}
