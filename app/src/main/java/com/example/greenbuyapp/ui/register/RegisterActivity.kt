package com.example.greenbuyapp.ui.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope

import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.ActivityRegisterBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.ui.login.LoginActivity
import com.example.greenbuyapp.util.setupActionBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterActivity : BaseActivity(R.layout.activity_register) {
    override val viewModel: RegisterViewModel by viewModel()
    override val binding: ActivityRegisterBinding by viewBinding()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupActionBar(R.id.toolbar) {
            title = "Đăng nhập với tài khoản của bạn"
            setDisplayHomeAsUpEnabled(true)
        }

        setupViews()
        observeViewModel()


    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            // Observe login state
            viewModel.registerState.collect { state ->
                handleRegisterState(state)
            }
        }

        lifecycleScope.launch {
            // Observe username error
            viewModel.usernameError.collect { error ->
                binding.tlUsername.error = error
            }
        }

        lifecycleScope.launch {
            // Observe email error
            viewModel.emailError.collect { error ->
                binding.tlEmail.error = error
            }
        }

        lifecycleScope.launch {
            // Observe password error
            viewModel.passwordError.collect { error ->
                binding.tlPassword.error = error
            }
        }

        lifecycleScope.launch {
            // Observe terms error
            viewModel.termsError.collect { error ->
                if (error != null) {
                    showSnackbar(error)
                }
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
            // Observe email
            viewModel.email.collect { email ->
                if (binding.tlEmail.editText?.text?.toString() != email) {
                    binding.tlEmail.editText?.setText(email)
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

        lifecycleScope.launch {
            // Observe terms accepted
            viewModel.isTermsAccepted.collect { isAccepted ->
                if (binding.cbRegister.isChecked != isAccepted) {
                    binding.cbRegister.isChecked = isAccepted
                }
            }
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

        binding.tlEmail.editText?.addTextChangedListener { text ->
            viewModel.onGmailChanged(text?.toString() ?: "")
        }

        // Setup checkbox listener
        binding.cbRegister.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onTermsAcceptedChanged(isChecked)
        }

        // Setup login button
        binding.btRegister.setOnClickListener {
            viewModel.register()
        }
        binding.tvLinkQuest.setOnClickListener {
            navigateToLogin()
        }
    }
    private fun setLoading(isLoading: Boolean) {
        binding.btRegister.isEnabled = !isLoading
        binding.tlUsername.isEnabled = !isLoading
        binding.tlPassword.isEnabled = !isLoading
        binding.tlEmail.isEnabled = !isLoading
        binding.cbRegister.isEnabled = !isLoading

        if (isLoading) {
            binding.btRegister.text = "Đang đăng ký..."
        } else {
            binding.btRegister.text = "Đăng ký"
        }
    }

    private fun handleRegisterState(state: RegisterUiState) {
        when (state) {
            is RegisterUiState.Idle -> {
                setLoading(false)
            }
            is RegisterUiState.Loading -> {
                setLoading(true)
            }
            is RegisterUiState.Success -> {
                setLoading(false)
                showSnackbar("Đăng ký thành công!")
            }
            is RegisterUiState.Error -> {
                setLoading(false)
                showSnackbar(state.message)
                Log.e("RegisterActivity", "Error: ${state.message}")
            }
            is RegisterUiState.LoggedOut -> {
                //Logout
            }
        }
    }
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    override fun onBackPressed() {
        // Prevent going back if loading
        if (viewModel.registerState.value !is RegisterUiState.Loading) {
            super.onBackPressed()
        }
    }
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("OK") {
                viewModel.clearError()
                onBackPressed()
            }
            .show()
    }
}