package com.example.greenbuyapp.ui.shop.shopDetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.core.content.ContextCompat
import com.example.greenbuyapp.ui.social.shopReview.ShopReviewActivity
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.delay


class ShopDetailActivity : BaseActivity<ActivityShopDetailBinding>() {
    override val viewModel: ProductViewModel by viewModel()
    private val followViewModel: FollowViewModel by viewModel()


    override val binding: ActivityShopDetailBinding by lazy {
        ActivityShopDetailBinding.inflate(layoutInflater)
    }

    private var shopId: Int = -1
    private lateinit var productAdapter: ProductAdapter
    private var isFollowed = false

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
        handleFollowEvent()
        handleReviewButtonClick()

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
        observeFollowResult()
        observeUnfollowResult()
        observeFollowingShops()
        observeFollowerCount()
        loadInitialData()
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

    private fun observeFollowResult() {
        lifecycleScope.launch {
            followViewModel.followResult.collect { result ->
                when (result) {
                    is com.example.greenbuyapp.util.Result.Success -> {
                        isFollowed = true
                        binding.btFollow.apply {
                            text = "Đang theo dõi"
                            setTextColor(ContextCompat.getColor(context, R.color.green_600))
                        }
                        Toast.makeText(this@ShopDetailActivity, "Đang theo dõi shop", Toast.LENGTH_SHORT).show()
                        followViewModel.loadFollowerCount(shopId)
                    }
                    is com.example.greenbuyapp.util.Result.Error -> {
                        Toast.makeText(this@ShopDetailActivity, "Theo dõi thất bại", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun observeUnfollowResult() {
        lifecycleScope.launch {
            followViewModel.unfollowResult.collect { result ->
                when (result) {
                    is com.example.greenbuyapp.util.Result.Success -> {
                        isFollowed = false
                        binding.btFollow.apply {
                            text = "Theo dõi"
                            setTextColor(ContextCompat.getColor(context, R.color.color_on_background))
                        }
                        Toast.makeText(this@ShopDetailActivity, "Đã bỏ theo dõi shop", Toast.LENGTH_SHORT).show()
                        followViewModel.loadFollowerCount(shopId)
                    }
                    is com.example.greenbuyapp.util.Result.Error -> {
                        Toast.makeText(this@ShopDetailActivity, "Bỏ theo dõi thất bại", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun handleFollowEvent() {
        binding.btFollow.setOnClickListener {
            if (isFollowed) {
                followViewModel.unfollow(shopId)
            } else {
                followViewModel.follow(shopId)
            }
        }
    }

    private fun observeFollowingShops() {
        lifecycleScope.launch {
            followViewModel.followingShops.collect { result ->
                when (result) {
                    is com.example.greenbuyapp.util.Result.Success -> {
                        val followingList = result.value
                        isFollowed = followingList.any { shop -> shop.shop_id == shopId }
                        binding.btFollow.apply {
                            text = if (isFollowed) "Đang theo dõi" else "Theo dõi"
                            setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    if (isFollowed) R.color.green_600 else R.color.color_on_background
                                )
                            )
                        }
                    }
                    is com.example.greenbuyapp.util.Result.Error -> {
                        Toast.makeText(
                            this@ShopDetailActivity,
                            "Không thể kiểm tra trạng thái theo dõi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {}
                }
            }
        }
    }


    private fun loadInitialData() {
        followViewModel.loadFollowingShops()
        followViewModel.loadFollowerCount(shopId)
        viewModel.loadShopProducts(isRefresh = true, shopId = shopId)
    }

    private fun observeFollowerCount() {
        lifecycleScope.launch {
            followViewModel.followerCount.collect { result ->
                when (result) {
                    is com.example.greenbuyapp.util.Result.Success -> {
                        val count = result.value
                        binding.tvFollower.text = "${formatFollowerCount(count)} người theo dõi"
                        println("🧮 Follower count: $count")

                    }
                    is com.example.greenbuyapp.util.Result.Error -> {
                        Toast.makeText(
                            this@ShopDetailActivity,
                            "Không thể tải số lượng người theo dõi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun formatFollowerCount(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000f)
            count >= 1_000 -> String.format("%.1fk", count / 1_000f)
            else -> count.toString()
        }
    }

    // Nhấn vào đánh giá
    private fun handleReviewButtonClick() {
        binding.btReview.setOnClickListener {
            val intent = ShopReviewActivity.createIntent(this, shopId)
            startActivity(intent)
        }
    }


}