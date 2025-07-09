package com.example.greenbuyapp.ui.profile.editProfile

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.ActivityEditProfileBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.ui.profile.AuthState
import com.example.greenbuyapp.ui.profile.ProfileViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditProfileActivity :  BaseActivity<ActivityEditProfileBinding>() {


    // Dialog references để manage lifecycle
    private var tokenExpiredDialog: AlertDialog? = null
    private var loginRequiredDialog: AlertDialog? = null
    private var logoutDialog: AlertDialog? = null

    override val binding: ActivityEditProfileBinding by lazy {
        ActivityEditProfileBinding.inflate(layoutInflater)
    }

    override val viewModel: ProfileViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Gọi setContentView trước
        setContentView(binding.root)

        // Gọi super sau khi binding đã có
        super.onCreate(savedInstanceState)

        // Gọi lại hideNavigationBar tạm thời nếu cần
        hideNavigationBarTemporarily()
    }

    override fun initViews() {
        //Nút back
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        //chuyen sang form dia chi
        binding.llAddress.setOnClickListener {
            val intent = Intent(this, AddressActivity::class.java)
            startActivity(intent)

        }
        binding.imgclickAddress.setOnClickListener {
            val intent = Intent(this, AddressActivity::class.java)
            startActivity(intent)
        }
        binding.txtAddress.setOnClickListener {
            val intent = Intent(this, AddressActivity::class.java)
            startActivity(intent)
        }
        setupLogoutAction()
        viewModel.checkAuthStatus()
    }



    private fun observeAuthState() {
        lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Unknown -> {
                        println("🔄 Auth state: Unknown")
                        // Initial state, do nothing
                    }
                    is AuthState.Authenticated -> {
                        println("✅ Auth state: Authenticated - loading profile data...")

                        // Serialize việc load data thay vì gọi đồng thời để tránh race condition
                    }
                    is AuthState.NotAuthenticated -> {
                        println("❌ Auth state: Not Authenticated")
                        // User not authenticated, redirect to login
                        showLoginRequiredDialog()
                    }
                }
            }
        }
    }

    private fun showLoginRequiredDialog() {

        // Dismiss existing dialog
        loginRequiredDialog?.dismiss()

        loginRequiredDialog = AlertDialog.Builder(this)
            .setTitle("Yêu cầu đăng nhập")
            .setMessage("Bạn cần đăng nhập để xem thông tin cá nhân. Đăng nhập ngay để trải nghiệm đầy đủ tính năng.")
            .setPositiveButton("Đăng nhập") { _, _ ->
                navigateToLogin()
            }
            .setCancelable(false)
            .setOnDismissListener {
                loginRequiredDialog = null
            }
            .show()
    }

    private fun setupLogoutAction() {
        // Có thể thêm logout button vào menu hoặc profile UI
        binding.btLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {

        // Dismiss existing dialog
        logoutDialog?.dismiss()

        logoutDialog = AlertDialog.Builder(this)
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc muốn đăng xuất?")
            .setPositiveButton("Đăng xuất") { _, _ ->
                viewModel.logout()
            }
            .setOnDismissListener {
                logoutDialog = null
            }
            .show()
    }

    override fun observeViewModel() {
        observeTokenExpiredEvents()
        observeAuthState()
    }

    private fun observeTokenExpiredEvents() {
        lifecycleScope.launch {
            viewModel.tokenExpiredEvent.collect { event ->
                event?.let {
                    if (it.shouldShowDialog) {
                        showTokenExpiredDialog(it.message)
                    } else {
                        showError(it.message)
                        navigateToLogin()
                    }
                    viewModel.clearTokenExpiredEvent()
                }
            }
        }
    }

    private fun showTokenExpiredDialog(message: String) {

        // Dismiss existing dialog
        tokenExpiredDialog?.dismiss()

        tokenExpiredDialog = AlertDialog.Builder(this)
            .setTitle("Phiên đăng nhập hết hạn")
            .setMessage(message)
            .setPositiveButton("Đăng nhập lại") { _, _ ->
                navigateToLogin()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .setOnDismissListener {
                tokenExpiredDialog = null
            }
            .show()
    }

}