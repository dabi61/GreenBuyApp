package com.example.greenbuyapp.domain.login

import com.example.greenbuyapp.data.authorization.AuthorizationService
import com.example.greenbuyapp.data.authorization.model.RefreshTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AccessTokenInterceptor(
    private val accessTokenProvider: AccessTokenProvider,
    private val tokenExpiredManager: TokenExpiredManager
) : Interceptor {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    @Volatile
    private var isRefreshing = false
    
    // ✅ Thêm mutex để đồng bộ hóa refresh token
    private val refreshMutex = kotlinx.coroutines.sync.Mutex()

    // Tạo Retrofit client riêng cho refresh token (không có interceptor để tránh vòng lặp)
    private val authService: AuthorizationService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.utt-school.site/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        
        retrofit.create(AuthorizationService::class.java)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Nếu user đã đăng nhập, kiểm tra token trước khi request
        if (accessTokenProvider.isAuthorized) {
            // Kiểm tra xem token có sắp hết hạn không (còn 5 phút)
            if (accessTokenProvider.isTokenExpired() && !isRefreshing) {
                // Token đã expired, thử refresh token trước
                val refreshResult = tryRefreshToken()
                if (!refreshResult) {
                    // Refresh failed, notify và return 401
                    coroutineScope.launch {
                        tokenExpiredManager.notifyUnauthorizedResponse()
                    }
                }
            }
        }
        
        // Tạo request với token hiện tại
        val requestWithAuth = if (accessTokenProvider.isAuthorized) {
            val token = accessTokenProvider.accessToken
            val tokenType = accessTokenProvider.tokenType ?: "Bearer"
            originalRequest.newBuilder()
                .addHeader("Authorization", "$tokenType $token")
                .build()
        } else {
            val clientId = accessTokenProvider.clientId
            originalRequest.newBuilder()
                .addHeader("Authorization", "Client-ID $clientId")
                .build()
        }
        
        // Thực hiện request
        val response = chain.proceed(requestWithAuth)
        
        // Nếu nhận 401 và user đang logged in, thử refresh token
        if (response.code == 401 && accessTokenProvider.isAuthorized && !isRefreshing) {
            
            val refreshResult = tryRefreshToken()
            if (refreshResult) {
                // ✅ Đóng response cũ trước khi thực hiện request mới
                response.close()
                
                // Refresh thành công, thử lại request với token mới
                val newToken = accessTokenProvider.accessToken
                val newTokenType = accessTokenProvider.tokenType ?: "Bearer"
                val newRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "$newTokenType $newToken")
                    .build()
                
                return chain.proceed(newRequest)
            } else {
                // Refresh failed, notify token expired
                coroutineScope.launch {
                    tokenExpiredManager.notifyUnauthorizedResponse()
                }
            }
        }
        
        return response
    }

    /**
     * ✅ Thử refresh token với IO dispatcher để tránh ANR
     */
    private fun tryRefreshToken(): Boolean {
        if (isRefreshing) return false
        
        return runBlocking(Dispatchers.IO) {
            refreshMutex.withLock {
                if (isRefreshing) return@withLock false
                
                try {
                    isRefreshing = true
                    println("🔄 Attempting to refresh token using AuthorizationService...")
                    
                    val refreshToken = accessTokenProvider.refreshToken
                    if (refreshToken.isNullOrEmpty()) {
                        println("❌ No refresh token available")
                        accessTokenProvider.reset()
                        return@withLock false
                    }
                    
                    // ✅ Tạo request với format đúng: {"old_refresh_data": "token"}
                    val refreshRequest = RefreshTokenRequest(
                        old_refresh_data = refreshToken
                    )
                    
                    // ✅ Thực hiện request
                    val loginResponse = authService.refreshToken(refreshRequest)
                    
                    // Lưu token mới
                    accessTokenProvider.saveLoginResponse(loginResponse)
                    
                    println("✅ Token refreshed successfully using AuthorizationService")
                    true
                    
                } catch (e: Exception) {
                    println("❌ Error refreshing token: ${e.message}")
                    accessTokenProvider.reset()
                    false
                } finally {
                    isRefreshing = false
                }
            }
        }
    }
}
