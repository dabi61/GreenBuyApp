package com.example.greenbuyapp.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.greenbuyapp.MainActivity
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.ActivityLoginBinding
import com.example.greenbuyapp.ui.register.RegisterActivity
import com.example.greenbuyapp.util.setupActionBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : BaseActivity(R.layout.activity_login) {

    override val viewModel: LoginViewModel by viewModel()
    override val binding: ActivityLoginBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupActionBar(R.id.toolbar) {
            title = "Đăng nhập với tài khoản của bạn"
        }
        
        setupViews()
        observeViewModel()
        
        // Check if already logged in
        if (viewModel.checkAuthStatus()) {
            navigateToMain()
        }
    }

    private fun setupViews() {
        binding.tlUsername.requestFocus()
        
        // Setup text change listeners
        binding.tlUsername.editText?.addTextChangedListener { text ->
            viewModel.onUsernameChanged(text?.toString() ?: "")
        }
        
        binding.tlPassword.editText?.addTextChangedListener { text ->
            viewModel.onPasswordChanged(text?.toString() ?: "")
        }

        binding.tvLinkQuest.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        
        // Setup login button
        binding.btLogin.setOnClickListener {
            viewModel.login()
        }
        
        // Setup forgot password
        binding.tvForgetPassword.setOnClickListener {
            showSnackbar("Tính năng quên mật khẩu sẽ được phát triển trong tương lai")
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            // Observe login state
            viewModel.loginState.collect { state ->
                handleLoginState(state)
            }
        }
        
        lifecycleScope.launch {
            // Observe username error
            viewModel.usernameError.collect { error ->
                binding.tlUsername.error = error
            }
        }
        
        lifecycleScope.launch {
            // Observe password error
            viewModel.passwordError.collect { error ->
                binding.tlPassword.error = error
            }
        }
        
        lifecycleScope.launch {
            // Observe username
            viewModel.username.collect { username ->
                if (binding.tlUsername.editText?.text?.toString() != username) {
                    binding.tlUsername.editText?.setText(username)
                }
            }
        }
        
        lifecycleScope.launch {
            // Observe password
            viewModel.password.collect { password ->
                if (binding.tlPassword.editText?.text?.toString() != password) {
                    binding.tlPassword.editText?.setText(password)
                }
            }
        }
    }

    private fun handleLoginState(state: LoginUiState) {
        when (state) {
            is LoginUiState.Idle -> {
                setLoading(false)
            }
            is LoginUiState.Loading -> {
                setLoading(true)
            }
            is LoginUiState.Success -> {
                setLoading(false)
                showSnackbar("Đăng nhập thành công!")
                navigateToMain()
            }
            is LoginUiState.Error -> {
                setLoading(false)
                showSnackbar(state.message)
            }
            is LoginUiState.LoggedOut -> {
                setLoading(false)
                showSnackbar("Đã đăng xuất")
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btLogin.isEnabled = !isLoading
        binding.tlUsername.isEnabled = !isLoading
        binding.tlPassword.isEnabled = !isLoading
        binding.tvForgetPassword.isEnabled = !isLoading
        
        if (isLoading) {
            binding.btLogin.text = "Đang đăng nhập..."
        } else {
            binding.btLogin.text = "Đăng nhập"
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("OK") { 
                viewModel.clearError()
            }
            .show()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Prevent going back if loading
        if (viewModel.loginState.value !is LoginUiState.Loading) {
            super.onBackPressed()
        }
    }
}