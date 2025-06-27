package com.example.greenbuyapp.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.greenbuyapp.ui.main.MainActivity
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.ActivityLoginBinding
import com.example.greenbuyapp.ui.register.RegisterActivity
import com.example.greenbuyapp.util.setupActionBar
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    override val viewModel: LoginViewModel by viewModel()
    
    override val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Phải gọi setContentView trước khi gọi super.onCreate
        setContentView(binding.root)
        super.onCreate(savedInstanceState)



        setupActionBar(R.id.toolbar) {
            title = "Đăng nhập với tài khoản của bạn"
        }
        
        // Check if already logged in
        if ((viewModel as LoginViewModel).checkAuthStatus()) {
            navigateToMain()
        }
    }

    override fun initViews() {

        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        binding.tlUsername.requestFocus()
        
        // Setup text change listeners
        binding.tlUsername.editText?.addTextChangedListener { text ->
            (viewModel as LoginViewModel).onUsernameChanged(text?.toString() ?: "")
        }
        
        binding.tlPassword.editText?.addTextChangedListener { text ->
            (viewModel as LoginViewModel).onPasswordChanged(text?.toString() ?: "")
        }

        binding.tvLinkQuest.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        
        // Setup login button
        binding.btLogin.setOnClickListener {
            (viewModel as LoginViewModel).login()
        }
        
        // Setup forgot password
        binding.tvForgetPassword.setOnClickListener {
            showSnackbar("Tính năng quên mật khẩu sẽ được phát triển trong tương lai")
        }
    }

    override fun observeViewModel() {
        // Observe login state
        launchWhenStarted {
            (viewModel as LoginViewModel).loginState.collect { state ->
                handleLoginState(state)
            }
        }
        
        // Observe username error
        launchWhenStarted {
            (viewModel as LoginViewModel).usernameError.collect { error ->
                binding.tlUsername.error = error
            }
        }
        
        // Observe password error
        launchWhenStarted {
            (viewModel as LoginViewModel).passwordError.collect { error ->
                binding.tlPassword.error = error
            }
        }
        
        // Observe username
        launchWhenStarted {
            (viewModel as LoginViewModel).username.collect { username ->
                if (binding.tlUsername.editText?.text?.toString() != username) {
                    binding.tlUsername.editText?.setText(username)
                }
            }
        }
        
        // Observe password
        launchWhenStarted {
            (viewModel as LoginViewModel).password.collect { password ->
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
            .setTextColor(getColor(R.color.white))
            .setActionTextColor(getColor(R.color.white))
            .setAction("OK") { 
                (viewModel as LoginViewModel).clearError()
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
        if ((viewModel as LoginViewModel).loginState.value !is LoginUiState.Loading) {
            super.onBackPressed()
        }
    }
}