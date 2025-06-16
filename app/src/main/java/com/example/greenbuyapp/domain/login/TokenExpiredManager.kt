package com.example.greenbuyapp.domain.login

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Manager để handle token expired events trong toàn bộ app
 */
class TokenExpiredManager {
    
    private val _tokenExpiredEvent = MutableSharedFlow<TokenExpiredEvent>()
    val tokenExpiredEvent: SharedFlow<TokenExpiredEvent> = _tokenExpiredEvent.asSharedFlow()
    
    /**
     * Emit token expired event khi token hết hạn
     * @param message thông báo cho user
     * @param shouldShowDialog có hiển thị dialog không
     */
    suspend fun notifyTokenExpired(
        message: String = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.",
        shouldShowDialog: Boolean = true
    ) {
        _tokenExpiredEvent.emit(
            TokenExpiredEvent(
                message = message,
                shouldShowDialog = shouldShowDialog
            )
        )
    }
    
    /**
     * Emit token expired event do refresh token thất bại
     */
    suspend fun notifyRefreshTokenFailed() {
        _tokenExpiredEvent.emit(
            TokenExpiredEvent(
                message = "Không thể làm mới phiên đăng nhập. Vui lòng đăng nhập lại.",
                shouldShowDialog = true
            )
        )
    }
    
    /**
     * Emit token expired event do unauthorized response từ API
     */
    suspend fun notifyUnauthorizedResponse() {
        _tokenExpiredEvent.emit(
            TokenExpiredEvent(
                message = "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.",
                shouldShowDialog = true
            )
        )
    }
}

/**
 * Event được emit khi token expired
 */
data class TokenExpiredEvent(
    val message: String,
    val shouldShowDialog: Boolean = true
) 