package com.example.greenbuyapp.ui.shop.shopDetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.ActivityProductBinding
import com.example.greenbuyapp.databinding.ActivityShopDetailBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.ui.home.HomeViewModel
import com.example.greenbuyapp.ui.home.ProductAdapter
import com.example.greenbuyapp.ui.product.ProductActivity
import com.example.greenbuyapp.ui.product.ProductActivity.Companion
import com.example.greenbuyapp.ui.product.ProductViewModel
import com.example.greenbuyapp.ui.shop.ShopViewModel
import com.example.greenbuyapp.util.loadAvatar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class ShopDetailActivity : BaseActivity<ActivityShopDetailBinding>() {
    override val viewModel: ProductViewModel by viewModel()
    private val productViewModel : HomeViewModel by viewModel()


    override val binding: ActivityShopDetailBinding by lazy {
        ActivityShopDetailBinding.inflate(layoutInflater)
    }

    private var shopId: Int = -1
    private lateinit var productAdapter: ProductAdapter



    companion object {
        private const val EXTRA_SHOP_ID = "extra_shop_id"

        fun createIntent(context: Context, shopId: Int): Intent {
            return Intent(context, ShopDetailActivity::class.java).apply {
                putExtra(EXTRA_SHOP_ID, shopId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // ✅ Get data TRƯỚC
        shopId = intent.getIntExtra(EXTRA_SHOP_ID, -1)
        
        if (shopId == -1) {
            finish()
            return
        }
        
        // ✅ setContentView TRƯỚC super.onCreate()
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun initViews() {
        loadShop()
        setupRecyclerView()
        backEvent()

        viewModel.loadShopProducts(isRefresh = true, shopId = shopId)
    }

    private fun backEvent() {
        binding.icBack.setOnClickListener {
            finish()
        }
    }


    private fun loadShop() {
        println("🔄 Loading shop for ID: $shopId...")
        println("🔍 About to call API with shopId: $shopId")

        if (shopId <= 0) {
            println("❌ Invalid productId: $shopId, cannot load shop")
            return
        }

        viewModel.getShopById(shopId)
    }

    override fun observeViewModel() {
        observeShop()
        observeProduct()
    }

    private fun observeShop() {
        lifecycleScope.launch {
            viewModel.shop.collect { shop ->
                // Hiển thị avatar
                binding.ivShop.loadAvatar(
                    avatarPath =  shop?.avatar,
                    placeholder =  R.drawable.avatar_blank,
                    error =  R.drawable.avatar_blank
                )

                // Hiển thị tên shop
                binding.tvShopName.text = shop?.name

            }
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter { product ->
            // Handle product click - mở ProductActivity
            println("🚀 Opening product ${product.product_id} in ProductActivity")
            println("🔍 Product object: $product")
            println("🔍 Product ID being passed: ${product.product_id}")

            val intent = ProductActivity.createIntent(this, product.product_id, product.shop_id, product.description)
            println("🔍 Intent created: $intent")
            println("🔍 Intent extras after creation: ${intent.extras}")

            startActivity(intent)
            println("✅ ProductActivity started")
        }
        binding.rvProduct.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productAdapter
        }
    }

    private fun observeProduct() {
        lifecycleScope.launch {
            viewModel.shopProducts.collect { products ->
                println("🛍️ Products Shop Activity updated: ${products.size} items")
                productAdapter.submitList(products)

                // Debug: Print first few products
                products.take(3).forEach { product ->
                    println("Product: ${product.name}")
                }
            }
        }
    }

}