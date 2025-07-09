package com.example.greenbuyapp.ui.social.shopReview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.social.model.RatingShopRequest
import com.example.greenbuyapp.databinding.ActivityRatingBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RatingActivity : BaseActivity<ActivityRatingBinding>() {

    override val binding: ActivityRatingBinding by lazy {
        ActivityRatingBinding.inflate(layoutInflater)
    }

    override val viewModel: RatingShopViewModel by viewModel()

    private var shopId: Int = -1
    private var selectedRating = 0
    private lateinit var starViews: List<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        shopId = intent.getIntExtra(EXTRA_SHOP_ID, -1)
        if (shopId == -1) {
            Toast.makeText(this, "Không tìm thấy shop", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupStarViews()
        setupListeners()
        observeSubmitResult()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupStarViews() {
        starViews = listOf(
            binding.star1, binding.star2, binding.star3, binding.star4, binding.star5
        )

        starViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                updateStars(index + 1)
            }
        }
    }

    private fun updateStars(rating: Int) {
        selectedRating = rating
        for (i in starViews.indices) {
            val starDrawable = if (i < rating) {
                R.drawable.ic_star
            } else {
                R.drawable.ic_star_grey
            }
            starViews[i].setImageResource(starDrawable)
        }
    }

    private fun setupListeners() {
        binding.bottomButton.setOnClickListener {
            val comment = binding.commentEditText.text.toString().trim()

            if (selectedRating == 0) {
                Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = RatingShopRequest(
                shop_id = shopId,
                rating = selectedRating,
                comment = comment
            )

            viewModel.submitRating(request)
        }
    }

    private fun observeSubmitResult() {
        lifecycleScope.launch {
            viewModel.submitResult.collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        viewModel.clearResult()
                        showSuccessLayout()
                    }
                    is Result.Error -> {
                        Toast.makeText(this@RatingActivity, "Đánh giá thất bại", Toast.LENGTH_SHORT).show()
                        viewModel.clearResult()
                    }
                    else -> {}
                }
            }
        }
    }


    companion object {
        private const val EXTRA_SHOP_ID = "extra_shop_id"

        fun createIntent(context: Context, shopId: Int): Intent {
            return Intent(context, RatingActivity::class.java).apply {
                putExtra(EXTRA_SHOP_ID, shopId)
            }
        }
    }

    private fun showSuccessLayout() {
        // Ẩn layout đánh giá
        binding.ratingCard.visibility = View.GONE
        binding.commentCard.visibility = View.GONE
        binding.bottomButton.visibility = View.GONE

        // Hiện layout thành công
        binding.successLayout.visibility = View.VISIBLE

        // Tự động đóng sau 2s và trả kết quả
        Handler(Looper.getMainLooper()).postDelayed({
            setResult(RESULT_OK)
            finish()
        }, 1500)
    }

}
