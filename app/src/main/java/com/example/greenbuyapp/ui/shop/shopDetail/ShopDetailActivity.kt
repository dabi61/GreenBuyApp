package com.example.greenbuyapp.ui.shop.shopDetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.ActivityShopDetailBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.ui.home.ProductAdapter
import com.example.greenbuyapp.ui.product.ProductActivity
import com.example.greenbuyapp.ui.product.ProductViewModel
import com.example.greenbuyapp.ui.profile.ProfileViewModel
import com.example.greenbuyapp.ui.social.shopReview.ShopReviewActivity
import com.example.greenbuyapp.util.Result
import com.example.greenbuyapp.util.loadAvatar
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ShopDetailActivity : BaseActivity<ActivityShopDetailBinding>() {
    override val viewModel: ProductViewModel by viewModel()
    private val followViewModel: FollowViewModel by viewModel()
    private val profileViewModel: ProfileViewModel by viewModel()

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
        shopId = intent.getIntExtra(EXTRA_SHOP_ID, -1)
        if (shopId == -1) {
            finish()
            return
        }
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.main_color)
        profileViewModel.loadUserProfile()
    }

    override fun initViews() {
        loadShop()
        setupRecyclerView()
        backEvent()
        handleFollowEvent()
        handleReviewButtonClick()
        handleUpdateButtonClick()
        viewModel.loadShopProducts(isRefresh = true, shopId = shopId)
    }

    private fun backEvent() {
        binding.icBack.setOnClickListener {
            finish()
        }
    }

    private fun loadShop() {
        viewModel.getShopById(shopId)
    }

    override fun observeViewModel() {
        observeShopAndUser()
        observeProduct()
        observeFollowResult()
        observeUnfollowResult()
        observeFollowingShops()
        observeFollowerCount()
        loadInitialData()
        observeRatingSummary()
    }

    private fun observeShopAndUser() {
        lifecycleScope.launch {
            combine(viewModel.shop, profileViewModel.userProfile) { shop, userResult ->
                Pair(shop, userResult)
            }.collect { (shop, userResult) ->
                if (shop == null) return@collect

                // Hiển thị avatar và tên
                binding.ivShop.loadAvatar(
                    avatarPath = shop.avatar,
                    placeholder = R.drawable.avatar_blank,
                    error = R.drawable.avatar_blank
                )
                binding.tvShopName.text = shop.name

                val userId = if (userResult is Result.Success) userResult.value.id else null
                val isMyShop = shop.user_id == userId

                binding.btFollow.visibility = if (isMyShop) View.GONE else View.VISIBLE
                binding.btReview.visibility = if (isMyShop) View.GONE else View.VISIBLE
                binding.btUpdate.visibility = if (isMyShop) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter { product ->
            val intent = ProductActivity.createIntent(
                this,
                product.product_id,
                product.shop_id,
                product.description,
                product.name
            )
            startActivity(intent)
        }

        binding.rvProduct.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productAdapter
        }
    }

    private fun observeProduct() {
        lifecycleScope.launch {
            viewModel.shopProducts.collect { products ->
                productAdapter.submitList(products)
            }
        }
    }

    private fun observeFollowResult() {
        lifecycleScope.launch {
            followViewModel.followResult.collect { result ->
                when (result) {
                    is Result.Success -> {
                        isFollowed = true
                        binding.btFollow.apply {
                            text = "Đang theo dõi"
                            setTextColor(ContextCompat.getColor(context, R.color.green_600))
                        }
                        followViewModel.loadFollowerCount(shopId)
                        Toast.makeText(this@ShopDetailActivity, "Đang theo dõi shop", Toast.LENGTH_SHORT).show()
                    }
                    is Result.Error -> {
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
                    is Result.Success -> {
                        isFollowed = false
                        binding.btFollow.apply {
                            text = "Theo dõi"
                            setTextColor(ContextCompat.getColor(context, R.color.color_on_background))
                        }
                        followViewModel.loadFollowerCount(shopId)
                        Toast.makeText(this@ShopDetailActivity, "Đã bỏ theo dõi shop", Toast.LENGTH_SHORT).show()
                    }
                    is Result.Error -> {
                        Toast.makeText(this@ShopDetailActivity, "Bỏ theo dõi thất bại", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun handleFollowEvent() {
        binding.btFollow.setOnClickListener {
            if (isFollowed) followViewModel.unfollow(shopId)
            else followViewModel.follow(shopId)
        }
    }

    override fun onResume() {
        super.onResume()
        followViewModel.loadShopRatingStats(shopId)
    }

    private fun observeFollowingShops() {
        lifecycleScope.launch {
            followViewModel.followingShops.collect { result ->
                when (result) {
                    is Result.Success -> {
                        val followingList = result.value
                        isFollowed = followingList.any { it.shop_id == shopId }
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
                    is Result.Error -> {
                        Toast.makeText(this@ShopDetailActivity, "Không thể kiểm tra trạng thái theo dõi", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun loadInitialData() {
        followViewModel.loadFollowingShops()
        followViewModel.loadFollowerCount(shopId)
        followViewModel.loadShopRatingStats(shopId)
        viewModel.loadShopProducts(isRefresh = true, shopId = shopId)
    }

    private fun observeFollowerCount() {
        lifecycleScope.launch {
            followViewModel.followerCount.collect { result ->
                when (result) {
                    is Result.Success -> {
                        val count = result.value
                        binding.tvFollower.text = "${formatFollowerCount(count)} người theo dõi"
                    }
                    is Result.Error -> {
                        Toast.makeText(this@ShopDetailActivity, "Không thể tải số lượng người theo dõi", Toast.LENGTH_SHORT).show()
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

    private fun handleReviewButtonClick() {
        binding.btReview.setOnClickListener {
            val intent = ShopReviewActivity.createIntent(this, shopId)
            reviewLauncher.launch(intent)
        }
    }

    private fun handleUpdateButtonClick() {
        binding.btUpdate.setOnClickListener {
            val intent = EditShopActivity.createIntent(this, shopId)
//            startActivity(intent)
            editLauncher.launch(intent)
        }
    }

    private fun observeRatingSummary() {
        lifecycleScope.launch {
            followViewModel.ratingStats.collect { result ->
                when (result) {
                    is Result.Success -> {
                        val avg = result.value.average_rating
                        binding.tvStar.text = String.format("%.1f", avg)
                    }
                    is Result.Error -> {
                        Toast.makeText(this@ShopDetailActivity, "Không thể tải đánh giá", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private val reviewLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            followViewModel.loadShopRatingStats(shopId)
            lifecycleScope.launch {
                delay(300)
                followViewModel.ratingStats.value.let { ratingResult ->
                    if (ratingResult is Result.Success) {
                        val avg = ratingResult.value.average_rating
                        binding.tvStar.text = String.format("%.1f", avg)
                    }
                }
            }
        }
    }

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Reload lại thông tin shop
            viewModel.getShopById(shopId)
        }
    }


}
