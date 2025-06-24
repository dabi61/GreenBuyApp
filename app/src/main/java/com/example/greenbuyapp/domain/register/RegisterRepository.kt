package com.example.greenbuyapp.domain.register

import android.util.Log
import com.example.greenbuyapp.data.register.RegisterService
import com.example.greenbuyapp.data.register.model.RegisterRequest
import com.example.greenbuyapp.data.register.model.RegisterResponse
import com.example.greenbuyapp.util.Result
import com.example.greenbuyapp.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class RegisterRepository(
    private val registerService: RegisterService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun RegisterWithCredentials(
        username: String,
        email: String,
        password: String
    ): Result<RegisterResponse> {
        Log.d("RegisterRepository", "Starting registration with:")
        Log.d("RegisterRepository", "Username: '$username'")
        Log.d("RegisterRepository", "Email: '$email'")
        Log.d("RegisterRepository", "Password length: ${password.length}")
        
        // Validate input
        if (username.isBlank()) {
            Log.e("RegisterRepository", "Username is blank")
            return Result.Error(400, "Tên đăng nhập không được để trống")
        }

        if (password.isBlank()) {
            Log.e("RegisterRepository", "Password is blank")
            return Result.Error(400, "Mật khẩu không được để trống")
        }

        if (password.length < 6) {
            Log.e("RegisterRepository", "Password too short: ${password.length}")
            return Result.Error(400, "Mật khẩu phải có ít nhất 6 ký tự")
        }

        if (email.isBlank()) {
            Log.e("RegisterRepository", "Email is blank")
            return Result.Error(400, "Email không được để trống")
        }

        if (!email.contains("@")) {
            Log.e("RegisterRepository", "Email missing @")
            return Result.Error(400, "Email không hợp lệ")
        }

        if (!email.contains(".")) {
            Log.e("RegisterRepository", "Email missing .")
            return Result.Error(400, "Email không hợp lệ")
        }

        val trimmedUsername = username.trim()
        val trimmedEmail = email.trim()
        
        Log.d("RegisterRepository", "Calling API with:")
        Log.d("RegisterRepository", "Trimmed Username: '$trimmedUsername'")
        Log.d("RegisterRepository", "Trimmed Email: '$trimmedEmail'")
        Log.d("RegisterRepository", "Password: [HIDDEN]")


        val request = RegisterRequest(trimmedUsername, trimmedEmail, password)

        val result = safeApiCall(dispatcher) {
            registerService.register(
                request
            )
        }

        when (result) {
            is Result.Success -> {
                Log.d("RegisterRepository", "Registration successful: ${result.value}")
            }
            is Result.Error -> {
                Log.e("RegisterRepository", "Registration error - Code: ${result.code}, Message: ${result.error}")
            }
            is Result.NetworkError -> {
                Log.e("RegisterRepository", "Network error during registration")
            }
            else -> {
                Log.w("RegisterRepository", "Unexpected result type: $result")
            }
        }

        return result
    }
}