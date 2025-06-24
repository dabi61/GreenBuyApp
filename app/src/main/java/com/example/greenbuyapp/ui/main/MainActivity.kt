package com.example.greenbuyapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.ActivityMainBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.ui.home.HomeFragment
import com.example.greenbuyapp.ui.login.LoginActivity
import com.example.greenbuyapp.ui.login.LoginUiState
import com.example.greenbuyapp.ui.login.LoginViewModel
import com.example.greenbuyapp.ui.mall.MallFragment
import com.example.greenbuyapp.ui.notification.NotificationFragment
import com.example.greenbuyapp.ui.profile.ProfileFragment
import com.example.greenbuyapp.ui.shop.ShopFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override val viewModel: LoginViewModel by viewModel()
    
    override val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    // Cache fragments để tránh tạo lại
    private val fragmentMap = mutableMapOf<Int, Fragment>()
    private var currentFragmentPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        // Phải gọi setContentView trước khi gọi super.onCreate
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        
        val isAuthenticated = (viewModel as LoginViewModel).checkAuthStatus()
        Log.d("MainActivity", "onCreate - Auth status: $isAuthenticated")

        // Kiểm tra authentication status trước khi hiển thị UI
        if (!isAuthenticated) {
            Log.d("MainActivity", "User not authenticated, navigating to login")
            navigateToLogin()
            return
        }

        // Hiển thị HomeFragment mặc định khi mở app (chỉ khi đã authenticated)
        if (savedInstanceState == null) {
            navigateToFragment(0) // HomeFragment position
            binding.bottomNavigation.itemActiveIndex = 0
        }
    }
    
    override fun initViews() {
        // Setup bottom navigation
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { position ->
            Log.d("MainActivity", "Bottom navigation item selected: $position")
            navigateToFragment(position)
        }
    }

    /**
     * Điều hướng đến fragment được chỉ định với cache
     * @param position Vị trí của tab (0-4)
     */
    private fun navigateToFragment(position: Int) {
        // Nếu đang ở cùng tab thì không làm gì
        if (currentFragmentPosition == position) {
            Log.d("MainActivity", "Already on position $position, skipping navigation")
            return
        }

        when (position) {
            0 -> {
                Log.d("MainActivity", "Creating HomeFragment")
                window.statusBarColor = ContextCompat.getColor(this, R.color.main_color)
            }

            1 -> {
                Log.d("MainActivity", "Creating MallFragment")
                window.statusBarColor = ContextCompat.getColor(this, R.color.main_color)
            }

            2 -> {
                Log.d("MainActivity", "Creating ShopFragment")
                window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            }

            3 -> {
                Log.d("MainActivity", "Creating NotificationFragment")
                window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            }

            4 -> {
                Log.d("MainActivity", "Creating ProfileFragment")
                window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            }

            else -> {
                Log.w("MainActivity", "Unknown position $position, defaulting to HomeFragment")
                window.statusBarColor = ContextCompat.getColor(this, R.color.main_color)
            }
        }
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        // Ẩn fragment hiện tại nếu có
        if (currentFragmentPosition != -1) {
            val currentFragment = fragmentMap[currentFragmentPosition]
            currentFragment?.let {
                Log.d("MainActivity", "Hiding fragment at position $currentFragmentPosition")
                fragmentTransaction.hide(it)
            }
        }

        // Lấy hoặc tạo fragment mới
        val targetFragment = getOrCreateFragment(position)
        
        if (targetFragment.isAdded) {
            // Fragment đã được add, chỉ cần show
            Log.d("MainActivity", "Showing existing fragment at position $position")
            fragmentTransaction.show(targetFragment)
        } else {
            // Fragment chưa được add, add vào
            Log.d("MainActivity", "Adding new fragment at position $position")
            fragmentTransaction.add(R.id.fl_main, targetFragment, getFragmentTag(position))
        }

        fragmentTransaction.commit()
        currentFragmentPosition = position
    }

    /**
     * Lấy fragment từ cache hoặc tạo mới nếu chưa có
     * @param position Vị trí của tab
     * @return Fragment tương ứng
     */
    private fun getOrCreateFragment(position: Int): Fragment {
        return fragmentMap[position] ?: run {
            val newFragment = createFragmentByPosition(position)
            fragmentMap[position] = newFragment
            Log.d("MainActivity", "Created new fragment for position $position: ${newFragment::class.simpleName}")
            newFragment
        }
    }

    /**
     * Tạo fragment mới theo vị trí
     * @param position Vị trí của tab
     * @return Fragment mới
     */
    private fun createFragmentByPosition(position: Int): Fragment {
        return when (position) {
            0 -> {
                Log.d("MainActivity", "Creating HomeFragment")
                HomeFragment()
            }
            1 -> {
                Log.d("MainActivity", "Creating MallFragment")
                MallFragment()
            }
            2 -> {
                Log.d("MainActivity", "Creating ShopFragment")
                ShopFragment()
            }
            3 -> {
                Log.d("MainActivity", "Creating NotificationFragment")
                NotificationFragment()
            }
            4 -> {
                Log.d("MainActivity", "Creating ProfileFragment")
                ProfileFragment()
            }
            else -> {
                Log.w("MainActivity", "Unknown position $position, defaulting to HomeFragment")
                HomeFragment()
            }
        }
    }

    /**
     * Tạo tag cho fragment theo vị trí
     * @param position Vị trí của tab
     * @return Tag string
     */
    private fun getFragmentTag(position: Int): String {
        return when (position) {
            0 -> "HomeFragment"
            1 -> "MallFragment"
            2 -> "ShopFragment"
            3 -> "NotificationFragment"
            4 -> "ProfileFragment"
            else -> "UnknownFragment_$position"
        }
    }

    /**
     * Lấy fragment hiện tại đang hiển thị
     * @return Fragment hiện tại hoặc null
     */
    fun getCurrentFragment(): Fragment? {
        return if (currentFragmentPosition != -1) {
            fragmentMap[currentFragmentPosition]
        } else null
    }

    /**
     * Clear cache fragments (sử dụng khi cần reset)
     */
    fun clearFragmentCache() {
        Log.d("MainActivity", "Clearing fragment cache")
        fragmentMap.clear()
        currentFragmentPosition = -1
    }
    
    override fun observeViewModel() {
        launchWhenStarted {
            (viewModel as LoginViewModel).loginState.collect { state ->
                Log.d("MainActivity", "Login state changed: $state")
                handleLoginState(state)
            }
        }
    }
    
    private fun handleLoginState(state: LoginUiState) {
        when (state) {
            is LoginUiState.Loading -> {
                Log.d("MainActivity", "Loading state")
                // Show loading state
                showLoading()
            }
            is LoginUiState.LoggedOut -> {
                Log.d("MainActivity", "LoggedOut state - navigating to login")
                // Hide loading
                hideLoading()
                
                // Show success message
                Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show()

                // Navigate to LoginActivity
                navigateToLogin()
            }
            is LoginUiState.Error -> {
                // Hide loading
                hideLoading()
                
                // Show error
                showError(state.message)
            }
            else -> {
                Log.d("MainActivity", "Other state: $state")
                // Hide loading for other states
                hideLoading()
            }
        }
    }

    private fun navigateToLogin() {
        Log.d("MainActivity", "Navigating to LoginActivity")
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        println("🔄 MainActivity onDestroy called")
        
        // Clear cache khi destroy activity
        clearFragmentCache()
        
        super.onDestroy()
    }
}