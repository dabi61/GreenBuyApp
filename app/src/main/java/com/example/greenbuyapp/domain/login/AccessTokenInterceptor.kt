package com.example.greenbuyapp.domain.login

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response

class AccessTokenInterceptor(
    private val accessTokenProvider: AccessTokenProvider,
    private val tokenExpiredManager: TokenExpiredManager
) : Interceptor {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = if (accessTokenProvider.isAuthorized) {
            val token = accessTokenProvider.accessToken
            val tokenType = accessTokenProvider.tokenType ?: "Bearer"
            chain.request()
                .newBuilder()
                .addHeader("Authorization", "$tokenType $token")
                .build()
        } else {
            val clientId = accessTokenProvider.clientId
            chain.request()
                .newBuilder()
                .addHeader("Authorization", "Client-ID $clientId")
                .build()
        }
        
        val response = chain.proceed(request)
        
        // Check if response is 401 Unauthorized và user đang logged in
        if (response.code == 401 && accessTokenProvider.isAuthorized) {
            // Clear tokens immediately
            accessTokenProvider.reset()
            
            // Notify token expired
            coroutineScope.launch {
                tokenExpiredManager.notifyUnauthorizedResponse()
            }
        }
        
        return response
    }
}
