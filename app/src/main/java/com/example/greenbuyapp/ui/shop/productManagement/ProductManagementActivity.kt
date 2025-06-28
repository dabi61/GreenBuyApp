package com.example.greenbuyapp.ui.shop.productManagement

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.launch
import com.example.greenbuyapp.databinding.ActivityProductManagementBinding
import com.example.greenbuyapp.data.product.model.ProductStatus
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.ui.shop.addProduct.AddProductActivity
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProductManagementActivity : BaseActivity<ActivityProductManagementBinding>() {
    override val viewModel: ProductManagementViewModel by viewModel()

    override val binding: ActivityProductManagementBinding by lazy {
        ActivityProductManagementBinding.inflate(layoutInflater)
    }

    private lateinit var productPagerAdapter: ProductPagerAdapter

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, ProductManagementActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // ✅ setContentView TRƯỚC super.onCreate()
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun initViews() {
        setupToolbar()
        setupViewPager()
        setupTabLayout()
        setupAddProductButton()
    }

    override fun observeViewModel() {
        observeProductCounts()
        observeErrorMessages()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupViewPager() {
        productPagerAdapter = ProductPagerAdapter(this)
        binding.viewPager.adapter = productPagerAdapter
        
        // Page change callback để log
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val productStatus = productPagerAdapter.getProductStatus(position)
                println("📄 ViewPager page changed to: ${productStatus.displayName} (position $position)")
            }
        })
    }

    private fun setupTabLayout() {
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val productStatus = productPagerAdapter.getProductStatus(position)
            
            // Initially set tab text without count
            tab.text = "${productStatus.displayName}\n0"
            
        }.attach()
    }

    private fun setupAddProductButton() {
        binding.btnAddProduct.setOnClickListener {
            val intent = AddProductActivity.createIntent(this)
            startActivity(intent)
            println("➕ Opening AddProductActivity")
        }
    }

    private fun observeProductCounts() {
        lifecycleScope.launch {
            viewModel.productCounts.collect { counts ->
                updateTabsWithCounts(counts)
            }
        }
    }

    /**
     * Update tab titles with product counts
     */
    private fun updateTabsWithCounts(counts: ProductCounts) {
        val tabLayout = binding.tabLayout
        
        // Update each tab with count
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            val productStatus = productPagerAdapter.getProductStatus(i)
            
            val count = when (productStatus) {
                ProductStatus.IN_STOCK -> counts.inStock
                ProductStatus.OUT_OF_STOCK -> counts.outOfStock
                ProductStatus.PENDING_APPROVAL -> counts.pendingApproval
            }
            
            tab?.text = "${productStatus.displayName}\n$count"
        }
        
        println("📊 Tab counts updated: IN_STOCK=${counts.inStock}, OUT_OF_STOCK=${counts.outOfStock}, PENDING=${counts.pendingApproval}")
    }

    private fun observeErrorMessages() {
        lifecycleScope.launch {
            viewModel.errorMessage.collect { errorMessage ->
                if (!errorMessage.isNullOrEmpty()) {
                    Toast.makeText(this@ProductManagementActivity, errorMessage, Toast.LENGTH_LONG).show()
                    viewModel.clearErrorMessage()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload data khi quay lại từ AddProduct flow
        viewModel.loadMyProducts()
        println("🔄 Reloading product data on resume")
    }
} 