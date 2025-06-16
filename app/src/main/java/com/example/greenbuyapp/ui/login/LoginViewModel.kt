package com.example.greenbuyapp.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.authorization.model.LoginResponse
import com.example.greenbuyapp.domain.login.LoginRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginRepository: LoginRepository,
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError: StateFlow<String?> = _usernameError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    fun onUsernameChanged(username: String) {
        _username.value = username
        _usernameError.value = null
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
        _passwordError.value = null
    }

    fun login() {
        val username = _username.value.trim()
        val password = _password.value

        // Clear previous errors
        _usernameError.value = null
        _passwordError.value = null

        // Validate inputs
        var hasError = false

        if (username.isBlank()) {
            _usernameError.value = "Tên đăng nhập không được để trống"
            hasError = true
        }

        if (password.isBlank()) {
            _passwordError.value = "Mật khẩu không được để trống"
            hasError = true
        } else if (password.length < 6) {
            _passwordError.value = "Mật khẩu phải có ít nhất 6 ký tự"
            hasError = true
        }

        if (hasError) {
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading

            when (val result = loginRepository.loginWithCredentials(username, password)) {
                is Result.Success -> {
                    _loginState.value = LoginUiState.Success(result.value)
                }
                is Result.Error -> {
                    val errorMessage = when (result.code) {
                        400 -> result.error ?: "Thông tin đăng nhập không hợp lệ"
                        401 -> "Tên đăng nhập hoặc mật khẩu không đúng"
                        403 -> "Tài khoản đã bị khóa"
                        404 -> "Tài khoản không tồn tại"
                        422 -> "Dữ liệu không hợp lệ"
                        429 -> "Quá nhiều lần thử. Vui lòng thử lại sau"
                        500 -> "Lỗi server. Vui lòng thử lại sau"
                        else -> result.error ?: "Đăng nhập thất bại. Vui lòng thử lại"
                    }
                    _loginState.value = LoginUiState.Error(errorMessage)
                }
                is Result.NetworkError -> {
                    _loginState.value = LoginUiState.Error("Không có kết nối mạng. Vui lòng kiểm tra và thử lại")
                }
                is Result.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun logout() {
        Log.d("LoginViewModel", "logout() called")
        viewModelScope.launch {
            Log.d("LoginViewModel", "Setting state to Loading")
            _loginState.value = LoginUiState.Loading
            
            Log.d("LoginViewModel", "Calling loginRepository.logout()")
            when (val result = loginRepository.logout()) {
                is Result.Success -> {
                    Log.d("LoginViewModel", "Logout successful")
                    _loginState.value = LoginUiState.LoggedOut
                    // Clear form
                    _username.value = ""
                    _password.value = ""
                }
                is Result.Error -> {
                    Log.d("LoginViewModel", "Logout error: ${result.error}")
                    // Even if logout fails, clear local data
                    _loginState.value = LoginUiState.LoggedOut
                    _username.value = ""
                    _password.value = ""
                }
                else -> {
                    Log.d("LoginViewModel", "Logout other result: $result")
                    _loginState.value = LoginUiState.LoggedOut
                    _username.value = ""
                    _password.value = ""
                }
            }
            Log.d("LoginViewModel", "Logout process completed")
        }
    }

    fun clearError() {
        if (_loginState.value is LoginUiState.Error) {
            _loginState.value = LoginUiState.Idle
        }
    }

    fun checkAuthStatus(): Boolean {
        val isAuth = loginRepository.isAuthorized()
        Log.d("LoginViewModel", "checkAuthStatus: $isAuth")
        return isAuth
    }

    fun ensureValidToken(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isValid = loginRepository.ensureValidToken()
            onResult(isValid)
        }
    }
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val loginResponse: LoginResponse) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
    object LoggedOut : LoginUiState()
}