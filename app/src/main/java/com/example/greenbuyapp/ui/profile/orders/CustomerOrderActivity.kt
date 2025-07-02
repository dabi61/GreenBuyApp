package com.example.greenbuyapp.ui.profile.orders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.launch
import com.example.greenbuyapp.databinding.ActivityCustomerOrderBinding
import com.example.greenbuyapp.data.user.model.CustomerOrderStatus
import com.example.greenbuyapp.ui.base.BaseActivity
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel

class CustomerOrderActivity : BaseActivity<ActivityCustomerOrderBinding>() {
    override val viewModel: CustomerOrderViewModel by viewModel()

    override val binding: ActivityCustomerOrderBinding by lazy {
        ActivityCustomerOrderBinding.inflate(layoutInflater)
    }

    private lateinit var orderPagerAdapter: CustomerOrderPagerAdapter
    private var initialPosition: Int = 0

    companion object {
        private const val EXTRA_POSITION = "extra_position"

        fun createIntent(context: Context, position: Int = 0): Intent {
            return Intent(context, CustomerOrderActivity::class.java).apply {
                putExtra(EXTRA_POSITION, position)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // ✅ Get data TRƯỚC
        initialPosition = intent.getIntExtra(EXTRA_POSITION, 0)

        // ✅ setContentView TRƯỚC super.onCreate()
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun initViews() {
        setupToolbar()
        setupViewPager()
        setupTabLayout()

        // Set initial position if specified
        if (initialPosition > 0 && initialPosition < orderPagerAdapter.itemCount) {
            binding.viewPager.setCurrentItem(initialPosition, false)
        }
    }

    override fun observeViewModel() {
        // Load all orders when activity starts
        viewModel.loadAllOrders()
        
        // Error handling
        observeErrorMessages()
    }

    private fun observeErrorMessages() {
        lifecycleScope.launch {
            viewModel.errorMessage.collect { errorMessage ->
                if (!errorMessage.isNullOrEmpty()) {
                    // Show error message
                    println("❌ Error in customer orders: $errorMessage")
                    // TODO: Show toast or snackbar
                    viewModel.clearErrorMessage()
                }
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupViewPager() {
        orderPagerAdapter = CustomerOrderPagerAdapter(this)
        binding.viewPager.adapter = orderPagerAdapter
        
        // Optional: Disable swipe nếu muốn chỉ cho phép chuyển tab bằng click
        // binding.viewPager.isUserInputEnabled = false
        
        // Page change callback để log
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val orderStatus = orderPagerAdapter.getOrderStatus(position)
                println("📄 ViewPager page changed to: ${orderStatus.displayName} (position $position)")
            }
        })
    }

    private fun setupTabLayout() {
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val orderStatus = orderPagerAdapter.getOrderStatus(position)
            tab.text = orderStatus.displayName
        }.attach()
    }
} 