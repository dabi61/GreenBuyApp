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
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
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
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

abstract class BaseActivity(@LayoutRes private val contentLayoutId: Int) : AppCompatActivity(contentLayoutId) {

    abstract val viewModel: ViewModel?

    abstract val binding: ViewBinding

    private val sharedPreferencesRepository: SharedPreferencesRepository by inject()
    val notificationManager: NotificationManager by inject()
    private val tokenExpiredManager: TokenExpiredManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setRecentAppsHeaderColor()
        applyLanguage(sharedPreferencesRepository.locale)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemInsets.top, 0, 0)
            WindowInsetsCompat.CONSUMED
        }

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
        
        // Observe token expired events for all activities except LoginActivity
        if (this !is LoginActivity) {
            observeTokenExpired()
        }
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

    private fun AppCompatActivity.setRecentAppsHeaderColor() {
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
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
