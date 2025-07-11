package com.example.greenbuyapp.ui.social.shopReview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.ActivityShopReviewBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.ui.shop.shopDetail.FollowViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.example.greenbuyapp.util.Result

class ShopReviewActivity : BaseActivity<ActivityShopReviewBinding>() {

    override val binding: ActivityShopReviewBinding by lazy {
        ActivityShopReviewBinding.inflate(layoutInflater)
    }

    override val viewModel: ShopReviewViewModel by viewModel()

    private lateinit var reviewAdapter: ShopReviewAdapter
    private val followViewModel: FollowViewModel by viewModel()

    private var shopId: Int = -1

    private val ratingLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Reload đánh giá khi đánh giá mới thành công
            viewModel.loadShopRatings(shopId)
            followViewModel.loadShopRatingStats(shopId)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.main_color)
        setupToolbar()
        setupRecyclerView()
        observeData()
        setupRatingButton()

        shopId = intent.getIntExtra(EXTRA_SHOP_ID, -1)
        if (shopId != -1) {
            viewModel.loadShopRatings(shopId)
        } else {
            Toast.makeText(this, "Không tìm thấy shop", Toast.LENGTH_SHORT).show()
            finish()
        }

        loadShopIdOrFinish()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        reviewAdapter = ShopReviewAdapter()
        binding.reviewRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ShopReviewActivity)
            adapter = reviewAdapter
        }
    }

    private fun observeData() {
        viewModel.reviews.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    reviewAdapter.submitList(result.value)
                }
                is Result.Error -> {
                    Toast.makeText(this, "Lỗi khi tải đánh giá", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    companion object {
        private const val EXTRA_SHOP_ID = "extra_shop_id"

        fun createIntent(context: Context, shopId: Int): Intent {
            return Intent(context, ShopReviewActivity::class.java).apply {
                putExtra(EXTRA_SHOP_ID, shopId)
            }
        }
    }

    private fun setupRatingButton() {
        binding.ratingButton.setOnClickListener {
            val shopId = intent.getIntExtra(EXTRA_SHOP_ID, -1)
            if (shopId != -1) {
                val intent = RatingActivity.createIntent(this, shopId)
//                startActivity(intent)
                ratingLauncher.launch(intent)
            } else {
                Toast.makeText(this, "Không thể đánh giá, thiếu shop ID", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadShopIdOrFinish() {
        shopId = intent.getIntExtra(EXTRA_SHOP_ID, -1)
        if (shopId != -1) {
            viewModel.loadShopRatings(shopId)
        } else {
            Toast.makeText(this, "Không tìm thấy shop", Toast.LENGTH_SHORT).show()
            finish()
        }
    }



}
