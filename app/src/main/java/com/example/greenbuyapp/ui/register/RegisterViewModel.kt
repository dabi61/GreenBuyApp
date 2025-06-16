package com.example.greenbuyapp.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.authorization.model.LoginResponse
import com.example.greenbuyapp.data.register.model.RegisterResponse
import com.example.greenbuyapp.domain.register.RegisterRepository
import com.example.greenbuyapp.ui.login.LoginUiState
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerRepository: RegisterRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isTermsAccepted = MutableStateFlow(false)
    val isTermsAccepted: StateFlow<Boolean> = _isTermsAccepted.asStateFlow()

    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError: StateFlow<String?> = _usernameError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _termsError = MutableStateFlow<String?>(null)
    val termsError: StateFlow<String?> = _termsError.asStateFlow()

    fun onUsernameChanged(username: String) {
        _username.value = username
        _usernameError.value = null
    }

    fun onGmailChanged(email: String) {
        _email.value = email
        _emailError.value = null
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
        _passwordError.value = null
    }

    fun onTermsAcceptedChanged(isAccepted: Boolean) {
        _isTermsAccepted.value = isAccepted
        _termsError.value = null
    }

    fun register() {
        val username = _username.value.trim()
        val email = _email.value.trim()
        val password = _password.value
        val isTermsAccepted = _isTermsAccepted.value

        // Clear previous errors
        _usernameError.value = null
        _emailError.value = null
        _passwordError.value = null
        _termsError.value = null

        // Validate inputs
        var hasError = false

        if (username.isBlank()) {
            _usernameError.value = "Tên đăng nhập không được để trống"
            hasError = true
        } else if (username.length < 3) {
            _usernameError.value = "Tên đăng nhập phải có ít nhất 3 ký tự"
            hasError = true
        } else if (username.length > 20) {
            _usernameError.value = "Tên đăng nhập không được quá 20 ký tự"
            hasError = true
        } else if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            _usernameError.value = "Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới"
            hasError = true
        }
        
        if (email.isBlank()) {
            _emailError.value = "Email không được để trống"
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailError.value = "Email không hợp lệ"
            hasError = true
        }

        if (password.isBlank()) {
            _passwordError.value = "Mật khẩu không được để trống"
            hasError = true
        } else if (password.length < 6) {
            _passwordError.value = "Mật khẩu phải có ít nhất 6 ký tự"
            hasError = true
        } else if (password.length > 50) {
            _passwordError.value = "Mật khẩu không được quá 50 ký tự"
            hasError = true
        }

        if (!isTermsAccepted) {
            _termsError.value = "Bạn phải đồng ý với điều khoản sử dụng"
            hasError = true
        }

        if (hasError) {
            return
        }

        viewModelScope.launch {
            _registerState.value = RegisterUiState.Loading

            when (val result = registerRepository.RegisterWithCredentials(username, email, password)) {
                is Result.Success -> {
                    _registerState.value = RegisterUiState.Success(result.value)
                }
                is Result.Error -> {
                    val errorMessage = when (result.code) {
                        400 -> result.error ?: "Thông tin đăng ký không hợp lệ"
                        403 -> "Tài khoản đã bị khóa"
                        409 -> "Tên tài khoản hoặc email đã tồn tại!"
                        422 -> result.error ?: "Dữ liệu không hợp lệ - Vui lòng kiểm tra lại thông tin"
                        429 -> "Quá nhiều lần thử. Vui lòng thử lại sau"
                        500 -> "Lỗi server. Vui lòng thử lại sau"
                        else -> result.error ?: "Đăng ký thất bại. Vui lòng thử lại"
                    }
                    _registerState.value = RegisterUiState.Error(errorMessage)
                }
                is Result.NetworkError -> {
                    _registerState.value = RegisterUiState.Error("Không có kết nối mạng. Vui lòng kiểm tra và thử lại")
                }
                is Result.Loading -> {
                    // Already handled above
                }
            }
        }
    }
    fun clearError() {
        if (_registerState.value is RegisterUiState.Error) {
            _registerState.value = RegisterUiState.Idle
        }
    }
}

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val registerResponse: RegisterResponse) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
    object LoggedOut : RegisterUiState()
}