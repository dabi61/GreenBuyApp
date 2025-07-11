package com.example.greenbuyapp.ui.product.trending

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenbuyapp.databinding.ActivityTrendingProductBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.ui.product.ProductActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class TrendingProductActivity : BaseActivity<ActivityTrendingProductBinding>() {

    override val viewModel: TrendingProductViewModel by viewModel()

    override val binding: ActivityTrendingProductBinding by lazy {
        ActivityTrendingProductBinding.inflate(layoutInflater)
    }

    private lateinit var trendingProductAdapter : TrendingProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        initRecyclerView()
        observeData()

        viewModel.loadTrendingProducts()
    }

    override fun initViews() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        trendingProductAdapter = TrendingProductAdapter{ trendingProduct ->
            val intent = ProductActivity.createIntent(
                this@TrendingProductActivity,
                trendingProduct.product_id,
                trendingProduct.shop_id,
                trendingProduct.description
            )
            startActivity(intent)
        }
    }

    private fun initRecyclerView() {
        binding.recyclerViewTrendingProduct.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTrendingProduct.adapter = trendingProductAdapter


        // (Optional) Nếu bạn muốn thêm sự kiện khi click sản phẩm
        // adapter.onItemClick = { product -> ... }
    }

    private fun observeData() {
        lifecycleScope.launch {
            launch {
                viewModel.trendingProducts.collectLatest { productList ->
                    trendingProductAdapter.submitList(productList)
                    println("🔥 Trending products updated: ${productList.size}")
                }
            }

            launch {
                viewModel.trendingLoading.collectLatest { isLoading ->
                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    println(if (isLoading) "⏳ Loading trending products..." else "✅ Trending loaded")
                }
            }

            launch {
                viewModel.trendingError.collectLatest { error ->
                    error?.let {
                        Toast.makeText(this@TrendingProductActivity, "❌ $it", Toast.LENGTH_SHORT).show()
                        println("❌ Error loading trending products: $it")
                    }
                }
            }
        }
    }
}
