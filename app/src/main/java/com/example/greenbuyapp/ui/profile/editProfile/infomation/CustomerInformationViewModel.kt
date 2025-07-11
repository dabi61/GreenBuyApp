package com.example.greenbuyapp.ui.profile.editProfile.infomation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.product.model.CreateProductResponse
import com.example.greenbuyapp.data.user.model.UpdateUserProfileRequest
import com.example.greenbuyapp.data.user.model.UpdateUserProfileResponse
import com.example.greenbuyapp.data.user.model.UserMe
import com.example.greenbuyapp.domain.user.UserRepository
import com.example.greenbuyapp.ui.shop.addProduct.AddProductUiState
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed class UpdateInfomationUiState {
    object Idle : UpdateInfomationUiState()
    object Loading : UpdateInfomationUiState()
    data class Success(val infomationResponse: UpdateUserProfileResponse) : UpdateInfomationUiState()
    data class Error(val message: String) : UpdateInfomationUiState()
}

class CustomerInformationViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<UserMe?>(null)
    val userState: StateFlow<UserMe?> = _userState.asStateFlow()

    private val _updateInfomationState = MutableStateFlow<UpdateInfomationUiState>(UpdateInfomationUiState.Idle)
    val updateInfomationState: StateFlow<UpdateInfomationUiState> = _updateInfomationState.asStateFlow()

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
                println("🔄 CustomerInformationViewModel: Loading user info...")
                val response = userRepository.getUserMeDirect()
                _userState.value = response.user
                println("✅ User info loaded successfully:")
                println("   Username: ${response.user.username}")
                println("   First name: ${response.user.first_name}")
                println("   Last name: ${response.user.last_name}")
                println("   Avatar: ${response.user.avatar}")
                println("   Avatar null/empty: ${response.user.avatar.isNullOrEmpty()}")
            } catch (e: Exception) {
                val msg = "❌ Lỗi tải thông tin: ${e.message}"
                println(msg)
                _errorMessage.value = msg
            }

            _isLoading.value = false
        }
    }

    fun updateProfile(
        context: Context,
        avatar: Uri?,
        firstName: String,
        lastName: String,
        phone: String,
        birthDate: String,
    ) {
        viewModelScope.launch {
            runCatching {
                _updateInfomationState.value = UpdateInfomationUiState.Loading

                when (val result = userRepository.updateProfile(
                    context, avatar, firstName, lastName, phone, birthDate
                )) {
                    is Result.Success -> {
                        val response = result.value
                        _updateInfomationState.value = UpdateInfomationUiState.Success(response)
                        println("✅ Profile updated successfully: ${response.first_name} ${response.last_name}")
                    }
                    is Result.Error -> {
                        _updateInfomationState.value = UpdateInfomationUiState.Error(result.error ?: "Lỗi cập nhật thông tin")
                        println("❌ Error updating profile: ${result.error}")
                    }
                    is Result.NetworkError -> {
                        _updateInfomationState.value = UpdateInfomationUiState.Error("Lỗi kết nối mạng")
                        println("❌ Network error updating profile")
                    }
                    is Result.Loading -> {
                        // Already handled
                    }
                }

            }.onFailure { exception ->
                _updateInfomationState.value = UpdateInfomationUiState.Error("Lỗi cập nhật thông tin: ${exception.message}")
                println("❌ Exception updating profile: ${exception.message}")
            }
        }
    }



    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearUpdateSuccess() {
        _updateSuccess.value = false
    }
}
