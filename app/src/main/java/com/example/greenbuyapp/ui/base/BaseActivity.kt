package com.example.greenbuyapp.ui.base

import android.app.ActivityManager.TaskDescription
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.example.greenbuyapp.ui.main.MainActivity
import com.example.greenbuyapp.R
import com.example.greenbuyapp.domain.SharedPreferencesRepository
import com.example.greenbuyapp.domain.login.TokenExpiredManager
import com.example.greenbuyapp.ui.login.LoginActivity
import com.example.greenbuyapp.util.applyLanguage
import com.example.greenbuyapp.util.getThemeAttrColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * BaseActivity với ViewBinding và ViewModel hiện đại
 * @param VB ViewBinding type
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity {

    constructor() : super()
    
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    abstract val viewModel: ViewModel?

    // ViewBinding instance
    abstract val binding: VB

    // Các dependencies
    private val sharedPreferencesRepository: SharedPreferencesRepository by inject()
    val notificationManager: NotificationManager by inject()
    private val tokenExpiredManager: TokenExpiredManager by inject()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setRecentAppsHeaderColor()
        applyLanguage(sharedPreferencesRepository.locale)

        // Setup edge-to-edge display
        setupEdgeToEdge()
        
        // Setup back navigation
        setupBackNavigation()
        
        // Observe token expired events for all activities except LoginActivity
        if (this !is LoginActivity) {
            observeTokenExpired()
        }
        
        // Khởi tạo các thành phần UI
        initViews()
        
        // Observe ViewModel
        observeViewModel()
        
        // Ẩn navigation bar tạm thời
        hideNavigationBarTemporarily()
    }
    
    /**
     * Khởi tạo views và listeners
     */
    protected open fun initViews() {
        // Override trong các lớp con
    }
    
    /**
     * Observe ViewModel
     */
    protected open fun observeViewModel() {
        // Override trong các lớp con
    }
    
    /**
     * Setup edge-to-edge display
     */
    private fun setupEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemInsets.top, 0, systemInsets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }
    
    /**
     * Setup back navigation
     */
    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Nếu là LoginActivity và là task root, thì thoát app
                if (this@BaseActivity is LoginActivity && isTaskRoot) {
                    finishAffinity() // Thoát hoàn toàn app
                    return
                }
                
                // Nếu là task root và không phải MainActivity, navigate về MainActivity
                if (isTaskRoot && this@BaseActivity !is MainActivity) {
                    startActivity(Intent(this@BaseActivity, MainActivity::class.java))
                }
                finish()
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyLanguage(sharedPreferencesRepository.locale)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.applyLanguage(sharedPreferencesRepository.locale))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setRecentAppsHeaderColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val taskDescription = TaskDescription(
                getString(R.string.app_name),
                R.mipmap.ic_launcher,
                getThemeAttrColor(this, com.google.android.material.R.attr.colorSurface)
            )
            setTaskDescription(taskDescription)
        } else {
            val icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            val taskDescription = TaskDescription(
                getString(R.string.app_name),
                icon,
                getThemeAttrColor(this, com.google.android.material.R.attr.colorSurface)
            )
            setTaskDescription(taskDescription)
            icon?.recycle()
        }
    }
    
    /**
     * Observe token expired events
     */
    private fun observeTokenExpired() {
        lifecycleScope.launch {
            tokenExpiredManager.tokenExpiredEvent.collect { event ->
                if (event.shouldShowDialog) {
                    showTokenExpiredDialog(event.message)
                } else {
                    navigateToLogin()
                }
            }
        }
    }
    
    /**
     * Show token expired dialog
     */
    private fun showTokenExpiredDialog(message: String) {
        // Kiểm tra activity có bị destroyed không
        if (!isFinishing && !isDestroyed) {
            AlertDialog.Builder(this)
                .setTitle("Phiên đăng nhập hết hạn")
                .setMessage(message)
                .setPositiveButton("Đăng nhập lại") { _, _ ->
                    navigateToLogin()
                }
                .setCancelable(false)
                .show()
        }
    }
    
    /**
     * Navigate to login screen
     */
    fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Hide navigation bar temporarily
     */
    fun hideNavigationBarTemporarily() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.navigationBars())
                it.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }
    
    /**
     * Show loading dialog/indicator
     */
    protected fun showLoading() {
        _isLoading.value = true
        // Implement loading UI here
    }
    
    /**
     * Hide loading dialog/indicator
     */
    protected fun hideLoading() {
        _isLoading.value = false
        // Hide loading UI here
    }
    
    /**
     * Show error message
     */
    protected fun showError(message: String) {
        // Implement error UI here, e.g. Snackbar
        AlertDialog.Builder(this)
            .setTitle("Lỗi")
            .setMessage(message)
            .setPositiveButton("Đồng ý", null)
            .show()
    }
    
    /**
     * Navigate to a fragment
     */
    protected fun navigateTo(
        fragment: Fragment,
        containerId: Int = android.R.id.content,
        addToBackStack: Boolean = true,
        tag: String? = null
    ) {
        val transaction = supportFragmentManager.beginTransaction()
        
        // Add animation nếu cần
        transaction.setCustomAnimations(
            android.R.anim.fade_in,
            android.R.anim.fade_out,
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
        
        transaction.replace(containerId, fragment, tag)
        
        if (addToBackStack) {
            transaction.addToBackStack(tag)
        }
        
        transaction.commit()
    }
    
    /**
     * Convenience method để launch coroutine trong activity lifecycle scope
     */
    protected fun launchWhenStarted(block: suspend () -> Unit) {
        lifecycleScope.launchWhenStarted {
            block()
        }
    }
    
    /**
     * Convenience method để launch coroutine trong activity lifecycle scope
     */
    protected fun launchWhenResumed(block: suspend () -> Unit) {
        lifecycleScope.launchWhenResumed {
            block()
        }
    }
}
