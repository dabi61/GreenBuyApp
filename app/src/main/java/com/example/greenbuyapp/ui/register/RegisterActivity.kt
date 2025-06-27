package com.example.greenbuyapp.ui.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.ActivityRegisterBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.ui.login.LoginActivity
import com.example.greenbuyapp.ui.login.LoginViewModel
import com.example.greenbuyapp.util.setupActionBar
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterActivity : BaseActivity<ActivityRegisterBinding>() {
    override val viewModel: RegisterViewModel by viewModel()
    
    override val binding: ActivityRegisterBinding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Phải gọi setContentView trước khi gọi super.onCreate
        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        setupActionBar(R.id.toolbar) {
            title = "Đăng ký tài khoản mới"
            setDisplayHomeAsUpEnabled(true)
        }

        initViews()
        observeViewModel()
    }

    override fun observeViewModel() {
        // Observe register state
        launchWhenStarted {
            (viewModel as RegisterViewModel).registerState.collect { state ->
                handleRegisterState(state)
            }
        }

        // Observe username error
        launchWhenStarted {
            (viewModel as RegisterViewModel).usernameError.collect { error ->
                binding.tlUsername.error = error
            }
        }

        // Observe email error
        launchWhenStarted {
            (viewModel as RegisterViewModel).emailError.collect { error ->
                binding.tlEmail.error = error
            }
        }

        // Observe password error
        launchWhenStarted {
            (viewModel as RegisterViewModel).passwordError.collect { error ->
                binding.tlPassword.error = error
            }
        }

        // Observe terms error
        launchWhenStarted {
            (viewModel as RegisterViewModel).termsError.collect { error ->
                if (error != null) {
                    showSnackbar(error)
                }
            }
        }

        // Observe username
        launchWhenStarted {
            (viewModel as RegisterViewModel).username.collect { username ->
                if (binding.tlUsername.editText?.text?.toString() != username) {
                    binding.tlUsername.editText?.setText(username)
                }
            }
        }

        // Observe email
        launchWhenStarted {
            (viewModel as RegisterViewModel).email.collect { email ->
                if (binding.tlEmail.editText?.text?.toString() != email) {
                    binding.tlEmail.editText?.setText(email)
                }
            }
        }

        // Observe password
        launchWhenStarted {
            (viewModel as RegisterViewModel).password.collect { password ->
                if (binding.tlPassword.editText?.text?.toString() != password) {
                    binding.tlPassword.editText?.setText(password)
                }
            }
        }

        // Observe terms accepted
        launchWhenStarted {
            (viewModel as RegisterViewModel).isTermsAccepted.collect { isAccepted ->
                if (binding.cbRegister.isChecked != isAccepted) {
                    binding.cbRegister.isChecked = isAccepted
                }
            }
        }
    }

    override fun initViews() {

        window.statusBarColor = ContextCompat.getColor(this, R.color.white)


        binding.tlUsername.requestFocus()

        // Setup text change listeners
        binding.tlUsername.editText?.addTextChangedListener { text ->
            (viewModel as RegisterViewModel).onUsernameChanged(text?.toString() ?: "")
        }

        binding.tlPassword.editText?.addTextChangedListener { text ->
            (viewModel as RegisterViewModel).onPasswordChanged(text?.toString() ?: "")
        }

        binding.tlEmail.editText?.addTextChangedListener { text ->
            (viewModel as RegisterViewModel).onGmailChanged(text?.toString() ?: "")
        }

        // Setup checkbox listener
        binding.cbRegister.setOnCheckedChangeListener { _, isChecked ->
            (viewModel as RegisterViewModel).onTermsAcceptedChanged(isChecked)
        }

        // Setup register button
        binding.btRegister.setOnClickListener {
            (viewModel as RegisterViewModel).register()
        }
        
        // Setup login link
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
                // Có thể thêm delay trước khi chuyển về màn hình đăng nhập
                launchWhenStarted {
                    kotlinx.coroutines.delay(1500)
                    navigateToLogin()
                }
            }
            is RegisterUiState.Error -> {
                setLoading(false)
                showSnackbar(state.message)
                Log.e("RegisterActivity", "Error: ${state.message}")
            }
            is RegisterUiState.LoggedOut -> {
                // Không cần xử lý trong màn hình đăng ký
            }
        }
    }
    
    private fun navigateToLogin() {
        onBackPressed()
    }
    
    override fun onBackPressed() {
        // Prevent going back if loading
        if ((viewModel as RegisterViewModel).registerState.value !is RegisterUiState.Loading) {
            super.onBackPressed()
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setTextColor(getColor(R.color.white))
            .setActionTextColor(getColor(R.color.white))
            .setAction("OK") {
                (viewModel as RegisterViewModel).clearError()
            }
            .show()
    }
}