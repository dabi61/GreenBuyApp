package com.example.greenbuyapp.domain.login

import com.example.greenbuyapp.data.authorization.AuthorizationService
import com.example.greenbuyapp.data.authorization.model.AccessToken
import com.example.greenbuyapp.data.authorization.model.LoginResponse
import com.example.greenbuyapp.data.authorization.model.RefreshTokenRequest
import com.example.greenbuyapp.data.user.UserService
import com.example.greenbuyapp.data.user.model.Me
import com.example.greenbuyapp.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import com.example.greenbuyapp.util.Result

class LoginRepository(
    private val authorizationService: AuthorizationService,
    private val accessTokenProvider: AccessTokenProvider,
    private val tokenExpiredManager: TokenExpiredManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    // Synchronization object để tránh multiple refresh token calls
    @Volatile
    private var isRefreshing = false

    /**
     * Đăng nhập bằng username và password
     * @param username tên đăng nhập
     * @param password mật khẩu
     * @return Result<LoginResponse> chứa access token, refresh token và thông tin user
     */
    suspend fun loginWithCredentials(
        username: String,
        password: String
    ): Result<LoginResponse> {
        // Validate input
        if (username.isBlank()) {
            return Result.Error(400, "Tên đăng nhập không được để trống")
        }
        
        if (password.isBlank()) {
            return Result.Error(400, "Mật khẩu không được để trống")
        }
        
        if (password.length < 6) {
            return Result.Error(400, "Mật khẩu phải có ít nhất 6 ký tự")
        }

        val result = safeApiCall(dispatcher) {
            authorizationService.login(
                grantType = "password",
                username = username.trim(),
                password = password,
                scope = null,
                clientId = accessTokenProvider.clientId,
                clientSecret = accessTokenProvider.clientSecret
            )
        }

        if (result is Result.Success) {
            accessTokenProvider.saveLoginResponse(result.value)
        }

        return result
    }

    /**
     * Refresh access token bằng refresh token
     * @return Result<LoginResponse> chứa access token mới
     */
    suspend fun refreshAccessToken(): Result<LoginResponse> {
        val refreshToken = accessTokenProvider.refreshToken
            ?: return Result.Error(401, "Không tìm thấy refresh token")

        val refreshRequest = RefreshTokenRequest(
            refresh_token = refreshToken
        )

        val result = safeApiCall(dispatcher) {
            authorizationService.refreshToken(refreshRequest)
        }

        if (result is Result.Success) {
            accessTokenProvider.saveLoginResponse(result.value)
        } else {
            // Refresh token failed, notify token expired
            tokenExpiredManager.notifyRefreshTokenFailed()
            // Clear all tokens
            accessTokenProvider.reset()
        }

        return result
    }

    /**
     * Đăng xuất
     * @return Result<Any> kết quả đăng xuất
     */
    suspend fun logout(): Result<Any> {
        val result = safeApiCall(dispatcher) {
            authorizationService.logout()
        }

        // Luôn clear token local dù API có thành công hay không
        accessTokenProvider.reset()

        return result
    }

    /**
     * Kiểm tra và refresh token nếu cần
     * @return true nếu token hợp lệ, false nếu cần đăng nhập lại
     */
    suspend fun ensureValidToken(): Boolean {
        if (!isAuthorized()) {
            println("🔒 Not authorized, returning false")
            return false
        }

        if (accessTokenProvider.isTokenExpired()) {
            println("⏰ Token expired, attempting refresh...")
            
            // Avoid multiple refresh calls
            if (isRefreshing) {
                println("🔄 Already refreshing, waiting...")
                // Wait a bit for the ongoing refresh
                kotlinx.coroutines.delay(1000)
                return isAuthorized() && !accessTokenProvider.isTokenExpired()
            }
            
            return try {
                isRefreshing = true
                when (val refreshResult = refreshAccessToken()) {
                    is Result.Success -> {
                        println("✅ Token refresh successful")
                        true
                    }
                    else -> {
                        println("❌ Token refresh failed: $refreshResult")
                        // Refresh failed, tokens already cleared in refreshAccessToken()
                        false
                    }
                }
            } finally {
                isRefreshing = false
            }
        }

        println("✅ Token still valid")
        return true
    }


    fun isAuthorized() = accessTokenProvider.isAuthorized

    fun getUsername() = accessTokenProvider.username

    fun getEmail() = accessTokenProvider.email

    fun getProfilePicture() = accessTokenProvider.profilePicture

    fun getUserId() = accessTokenProvider.userId

    fun getFullName() = accessTokenProvider.fullName


}