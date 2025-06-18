package com.example.greenbuyapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.ActivityMainBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.ui.login.LoginActivity
import com.example.greenbuyapp.ui.login.LoginUiState
import com.example.greenbuyapp.ui.login.LoginViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity(R.layout.activity_main) {

    override val viewModel: LoginViewModel by viewModel()
    override val binding: ActivityMainBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate - Auth status: ${viewModel.checkAuthStatus()}")
        window?.statusBarColor = ContextCompat.getColor(this, R.color.main_color)
        setupViews()
        observeViewModel()

    }
    
    private fun setupViews() {
//        binding.btLogout.setOnClickListener {
//            Log.d("MainActivity", "Logout button clicked")
//            Toast.makeText(this, "Đang đăng xuất...", Toast.LENGTH_SHORT).show()
//            viewModel.logout()
//        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                Log.d("MainActivity", "Login state changed: $state")
//                handleLoginState(state)
            }
        }
    }
    
//    private fun handleLoginState(state: LoginUiState) {
//        when (state) {
//            is LoginUiState.Loading -> {
//                Log.d("MainActivity", "Loading state")
//                // Show loading state for logout
//                binding.btLogout.isEnabled = false
//                binding.btLogout.text = "Đang đăng xuất..."
//            }
//            is LoginUiState.LoggedOut -> {
//                Log.d("MainActivity", "LoggedOut state - navigating to login")
//                // Reset button state
//                binding.btLogout.isEnabled = true
//                binding.btLogout.text = "Đăng xuất"
//
//                // Show success message
//                Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show()
//
//                // Navigate to LoginActivity
//                navigateToLogin()
//            }
//            else -> {
//                Log.d("MainActivity", "Other state: $state")
//                // Reset button state for other states
//                binding.btLogout.isEnabled = true
//                binding.btLogout.text = "Đăng xuất"
//            }
//        }
//    }

    private fun navigateToLogin() {
        Log.d("MainActivity", "Navigating to LoginActivity")
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

}