package com.example.greenbuyapp.domain.login

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.example.greenbuyapp.data.authorization.model.AccessToken
import com.example.greenbuyapp.data.authorization.model.LoginResponse
import com.example.greenbuyapp.data.user.model.Me
import com.example.greenbuyapp.BuildConfig


class AccessTokenProvider(context: Context) {

    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    val clientId = if (BuildConfig.DEBUG) {
        if (sharedPreferences.getString(DEBUG_APP_ID_KEY, null).isNullOrBlank()) {
            BuildConfig.DEV_APP_ID
        } else {
            sharedPreferences.getString(DEBUG_APP_ID_KEY, null) ?: BuildConfig.DEV_APP_ID
        }
    } else {
        BuildConfig.RELEASE_APP_ID
    }

    val clientSecret = if (BuildConfig.DEBUG) {
        if (sharedPreferences.getString(DEBUG_APP_SECRET_KEY, null).isNullOrBlank()) {
            BuildConfig.DEV_SECRET
        } else {
            sharedPreferences.getString(DEBUG_APP_SECRET_KEY, null) ?: BuildConfig.DEV_SECRET
        }
    } else {
        BuildConfig.RELEASE_SECRET
    }

    val accessToken: String?
        get() = sharedPreferences.getString(ACCESS_TOKEN_KEY, null)

    val refreshToken: String?
        get() = sharedPreferences.getString(REFRESH_TOKEN_KEY, null)

    val tokenType: String?
        get() = sharedPreferences.getString(TOKEN_TYPE_KEY, null)

    val username: String?
        get() = sharedPreferences.getString(USERNAME_KEY, null)

    val email: String?
        get() = sharedPreferences.getString(EMAIL_KEY, null)

    val profilePicture: String?
        get() = sharedPreferences.getString(PROFILE_PICTURE_KEY, null)

    val userId: String?
        get() = sharedPreferences.getString(USER_ID_KEY, null)

    val fullName: String?
        get() = sharedPreferences.getString(FULL_NAME_KEY, null)

    val isAuthorized: Boolean
        get() = !accessToken.isNullOrEmpty()

    fun saveAccessToken(accessToken: AccessToken) = sharedPreferences.edit {
        putString(ACCESS_TOKEN_KEY, accessToken.access_token)
        putString(TOKEN_TYPE_KEY, accessToken.token_type)
        accessToken.create_at?.let { createdAt ->
            val expiryTime = (createdAt + 3600) * 1000L
            putLong(TOKEN_EXPIRES_AT_KEY, expiryTime)
        }
    }

    fun saveLoginResponse(loginResponse: LoginResponse) = sharedPreferences.edit {
        putString(ACCESS_TOKEN_KEY, loginResponse.access_token)
        putString(REFRESH_TOKEN_KEY, loginResponse.refresh_token)
        putString(TOKEN_TYPE_KEY, loginResponse.token_type)
        
        val expiresInSeconds = loginResponse.expires_in ?: 1800 // Default 1 hour nếu không có
        val expiryTime = System.currentTimeMillis() + (expiresInSeconds * 1000L)
        putLong(TOKEN_EXPIRES_AT_KEY, expiryTime)
        
        println("💾 Token saved - expires in: ${expiresInSeconds}s, expiry time: $expiryTime")
    }

    fun saveUserProfile(me: Me) = sharedPreferences.edit {
        putString(USERNAME_KEY, me.username)
        putString(EMAIL_KEY, me.email)
        putString(PROFILE_PICTURE_KEY, me.profile_image?.large)
    }

    fun isTokenExpired(): Boolean {
        val expiryTime = sharedPreferences.getLong(TOKEN_EXPIRES_AT_KEY, 0)
        val currentTime = System.currentTimeMillis()
        
        // Thêm buffer 5 phút (300,000ms) để refresh token trước khi thật sự expired
        val bufferTime = 5 * 60 * 1000L
        val isExpired = if (expiryTime > 0) {
            currentTime >= (expiryTime - bufferTime)
        } else {
            false
        }
        
        if (isExpired) {
            val remainingTime = (expiryTime - currentTime) / 1000
            println("⏰ Token expired check: expired=$isExpired, remaining=${remainingTime}s")
        }
        
        return isExpired
    }

    fun simulateTokenExpired() {
        if (BuildConfig.DEBUG) {
            sharedPreferences.edit {
                putLong(TOKEN_EXPIRES_AT_KEY, System.currentTimeMillis() - 1000)
            }
        }
    }

    fun reset() = sharedPreferences.edit {
        putString(ACCESS_TOKEN_KEY, null)
        putString(REFRESH_TOKEN_KEY, null)
        putString(TOKEN_TYPE_KEY, null)
        putString(USERNAME_KEY, null)
        putString(EMAIL_KEY, null)
        putString(PROFILE_PICTURE_KEY, null)
        putString(USER_ID_KEY, null)
        putString(FULL_NAME_KEY, null)
        putLong(TOKEN_EXPIRES_AT_KEY, 0)
    }

    companion object {

        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val TOKEN_TYPE_KEY = "token_type"
        private const val TOKEN_EXPIRES_AT_KEY = "token_expires_at"

        private const val USER_ID_KEY = "user_id"
        private const val USERNAME_KEY = "user_username"
        private const val EMAIL_KEY = "user_email"
        private const val FULL_NAME_KEY = "user_full_name"
        private const val PROFILE_PICTURE_KEY = "user_profile_picture"

        const val DEBUG_APP_ID_KEY = "debug_app_id"
        const val DEBUG_APP_SECRET_KEY = "debug_app_secret"
    }
}
