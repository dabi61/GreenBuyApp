package com.example.greenbuyapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.social.model.FollowStatsResponse
import com.example.greenbuyapp.data.user.model.UserMe
import com.example.greenbuyapp.domain.user.UserRepository
import com.example.greenbuyapp.domain.SharedPreferencesRepository
import com.example.greenbuyapp.domain.login.LoginRepository
import com.example.greenbuyapp.domain.login.TokenExpiredManager
import com.example.greenbuyapp.domain.login.TokenExpiredEvent
import com.example.greenbuyapp.domain.social.FollowStatsRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val followStatsRepository: FollowStatsRepository,
    private val loginRepository: LoginRepository,
    private val tokenExpiredManager: TokenExpiredManager
) : ViewModel() {
    
    // Authentication state
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unknown)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    // User profile state
    private val _userProfile = MutableStateFlow<Result<UserMe>?>(null)
    val userProfile: StateFlow<Result<UserMe>?> = _userProfile.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Token expired events
    private val _tokenExpiredEvent = MutableStateFlow<TokenExpiredEvent?>(null)
    val tokenExpiredEvent: StateFlow<TokenExpiredEvent?> = _tokenExpiredEvent.asStateFlow()
    
    // util items
    private val _utilProfile = MutableStateFlow<List<UtilProfile>>(emptyList())
    val utilProfile: StateFlow<List<UtilProfile>> = _utilProfile.asStateFlow()

    // follow
    private val _followStats = MutableStateFlow<Result<FollowStatsResponse>?>(null)
    val followStats: StateFlow<Result<FollowStatsResponse>?> = _followStats.asStateFlow()

    init {
        // Observe token expired events
        viewModelScope.launch {
            tokenExpiredManager.tokenExpiredEvent.collect { event ->
                _tokenExpiredEvent.value = event
                _authState.value = AuthState.NotAuthenticated
            }
        }
    }

    /**
     * Kiểm tra trạng thái đăng nhập
     */
    fun checkAuthStatus() {
        _authState.value = if (loginRepository.isAuthorized()) {
            AuthState.Authenticated
        } else {
            AuthState.NotAuthenticated
        }
    }

    /**
     * Load user profile với auto token validation
     */
    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                println("👤 Loading user profile...")
                
                // Kiểm tra và ensure valid token trước khi gọi API
                if (!loginRepository.ensureValidToken()) {
                    println("❌ Token validation failed for user profile")
                    _userProfile.value = Result.Error(401, "Token không hợp lệ. Vui lòng đăng nhập lại.")
                    _authState.value = AuthState.NotAuthenticated
                    return@launch
                }
                
                println("✅ Token valid, calling getUserMe API...")
                val result = userRepository.getUserMe()
                _userProfile.value = when (result) {
                    is Result.Success -> {
                        println("✅ User profile loaded successfully")
                        _authState.value = AuthState.Authenticated
                        Result.Success(result.value.user)
                    }
                    is Result.Error -> {
                        println("❌ User profile error: ${result.code} - ${result.error}")
                        if (result.code == 401) {
                            _authState.value = AuthState.NotAuthenticated
                        }
                        Result.Error(result.code, result.error)
                    }
                    is Result.NetworkError -> {
                        println("🌐 User profile network error")
                        Result.NetworkError
                    }
                    is Result.Loading -> Result.Loading
                }
            } catch (e: Exception) {
                println("💥 User profile exception: ${e.message}")
                _userProfile.value = Result.Error(null, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Logout user
     */
    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                loginRepository.logout()
                _authState.value = AuthState.NotAuthenticated
                _userProfile.value = null
            } catch (e: Exception) {
                // Even if logout API fails, clear local state
                _authState.value = AuthState.NotAuthenticated
                _userProfile.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFollowStats() {
        viewModelScope.launch {
            try {
                println("📊 Loading follow stats...")
                
                // Kiểm tra và ensure valid token trước khi gọi API
                if (!loginRepository.ensureValidToken()) {
                    println("❌ Token validation failed for follow stats")
                    _followStats.value = Result.Error(401, "Token không hợp lệ. Vui lòng đăng nhập lại.")
                    _authState.value = AuthState.NotAuthenticated
                    return@launch
                }
                
                println("✅ Token valid, calling getFollowStats API...")
                val result = followStatsRepository.getFollowStats()
                println("🔍 FollowStats API result: $result")
                _followStats.value = when (result) {
                    is Result.Success -> {
                        println("✅ FollowStats success: ${result.value}")
                        _authState.value = AuthState.Authenticated
                        Result.Success(result.value)
                    }
                    is Result.Error -> {
                        println("❌ FollowStats error: ${result.code} - ${result.error}")
                        if (result.code == 401) {
                            _authState.value = AuthState.NotAuthenticated
                        }
                        Result.Error(result.code, result.error)
                    }
                    is Result.NetworkError -> {
                        println("🌐 FollowStats network error")
                        Result.NetworkError
                    }
                    is Result.Loading -> Result.Loading
                }
            } catch (e: Exception) {
                println("💥 FollowStats exception: ${e.message}")
                _followStats.value = Result.Error(null, e.message)
            }
        }
    }
    /**
     * Clear token expired event after handling
     */
    fun clearTokenExpiredEvent() {
        _tokenExpiredEvent.value = null
    }

    fun loadUtilProfile() {
        // Tạo dữ liệu util mẫu
        val utilData = listOf(
            UtilProfile(R.drawable.ic_util_1, "Yêu thích"),
            UtilProfile(R.drawable.ic_util_2, "Đánh giá của tôi"),
            UtilProfile(R.drawable.ic_util_3, "Tư cách thành viên"),
            UtilProfile(R.drawable.ic_util_4, "Trung tâm trợ giúp"),
            UtilProfile(R.drawable.ic_util_5, "Top cửa hàng"),
            UtilProfile(R.drawable.ic_util_6, "Cửa hàng theo dõi"),
            UtilProfile(R.drawable.ic_util_7, "Giỏ hàng"),
            UtilProfile(R.drawable.ic_util_8, "Chat"),
            UtilProfile(R.drawable.ic_util_9, "Khuyến mãi"),
        )

        _utilProfile.value = utilData
        println("✅ util items loaded: ${utilData.size} items")
    }
}

/**
 * Authentication state của user
 */
sealed class AuthState {
    object Unknown : AuthState()
    object Authenticated : AuthState()
    object NotAuthenticated : AuthState()
}