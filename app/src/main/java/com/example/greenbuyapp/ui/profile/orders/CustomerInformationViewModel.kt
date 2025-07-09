package com.example.greenbuyapp.ui.profile.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.user.model.UpdateUserProfileRequest
import com.example.greenbuyapp.data.user.model.UserMe
import com.example.greenbuyapp.domain.user.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CustomerInformationViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<UserMe?>(null)
    val userState: StateFlow<UserMe?> = _userState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    fun getInfor() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = userRepository.getUserMeDirect()
                _userState.value = response.user
                println("✅ Tải thông tin user thành công: ${response.user.username}")
            } catch (e: Exception) {
                val msg = "❌ Lỗi tải thông tin: ${e.message}"
                println(msg)
                _errorMessage.value = msg
            }

            _isLoading.value = false
        }
    }

    fun updateProfile(request: UpdateUserProfileRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _updateSuccess.value = false

            try {
                println("🚀 Đang gọi PUT updateUserProfile...")
                println("📤 Payload gửi: $request")

                userRepository.updateUserProfile(request)

                // ❌ BỎ: Không cần gọi lại GET nếu UI đã có thông tin mới
                // val refreshedUser = userRepository.getUserMeDirect()
                // _userState.value = refreshedUser.user

                _updateSuccess.value = true
                println("✅ Cập nhật thành công")
            } catch (e: Exception) {
                val msg = "❌ Lỗi khi cập nhật: ${e.message}"
                println(msg)
                _errorMessage.value = msg
            }

            _isLoading.value = false
        }
    }



    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearUpdateSuccess() {
        _updateSuccess.value = false
    }
}
