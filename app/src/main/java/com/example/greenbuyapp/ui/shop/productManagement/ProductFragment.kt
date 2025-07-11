package com.example.greenbuyapp.ui.shop.productManagement

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.product.model.Product
import com.example.greenbuyapp.data.product.model.ProductStatus
import com.example.greenbuyapp.databinding.FragmentProductManagementBinding
import com.example.greenbuyapp.ui.base.BaseFragment
import com.example.greenbuyapp.ui.shop.addProduct.ProductVariantActivity
import com.example.greenbuyapp.ui.shop.productManagement.EditProductActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ProductFragment : BaseFragment<FragmentProductManagementBinding, ProductManagementViewModel>() {

    override val viewModel: ProductManagementViewModel by sharedViewModel()
    
    private lateinit var productAdapter: ProductManagementAdapter
    private var productStatus: ProductStatus = ProductStatus.IN_STOCK

    companion object {
        private const val ARG_PRODUCT_STATUS = "product_status"
        
        fun newInstance(productStatus: ProductStatus): ProductFragment {
            return ProductFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PRODUCT_STATUS, productStatus.name)
                }
            }
        }
    }

    override fun getLayoutResourceId(): Int = R.layout.fragment_product_management

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProductManagementBinding {
        return FragmentProductManagementBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        // Get product status from arguments
        productStatus = arguments?.getString(ARG_PRODUCT_STATUS)?.let { statusName ->
            ProductStatus.valueOf(statusName)
        } ?: ProductStatus.IN_STOCK

        setupRecyclerView()
        setupSwipeRefresh()
        
        println("🏷️ ProductFragment initialized for status: ${productStatus.displayName}")
    }

    override fun observeViewModel() {
        observeProducts()
        observeLoadingState()
        observeErrorMessages()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductManagementAdapter(
            onItemClick = { product ->
                // Handle product item click - Navigate to product detail
                navigateToProductDetail(product)
                println("📱 Navigating to product detail: ${product.name}")
            },
            onMoreClick = { product ->
                // Handle more button click - Show menu
                showProductMenu(product)
                println("⋮ More menu for product: ${product.name}")
            }
        )

        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productAdapter
        }
    }

    /**
     * Navigate to product detail (placeholder)
     */
    private fun navigateToProductDetail(product: Product) {
        val context = requireContext()
        val intent = EditProductActivity.createIntent(context, product)
        startActivity(intent)
        println("📱 Opening EditProductActivity for product: ${product.name}")
    }

    /**
     * Show product menu (placeholder)
     */
    private fun showProductMenu(product: Product) {
        // TODO: Show bottom sheet menu with options (Edit, Delete, etc.)
        println("⋮ Menu cho sản phẩm: ${product.name}")
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            // ✅ Gọi method reload
            refreshProductsForCurrentStatus()
        }
    }

    /**
     * ✅ Refresh products for current status - Fixed version
     */
    private fun refreshProductsForCurrentStatus() {
        println("🔄 Refreshing products for status: ${productStatus.displayName}")
        
        // ✅ Gọi method reload thay vì chỉ collect StateFlow
        viewModel.reloadProductsByStatus(productStatus)
    }

    private fun observeProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getProductsByStatus(productStatus).collect { products ->
                println("📦 ProductFragment received ${products.size} products for ${productStatus.displayName}")
                products.forEach { product ->
                    println("   - ID: ${product.product_id}, Name: '${product.name}', Price: ${product.price}")
                }
                
                productAdapter.submitList(products)
                
                // Show/hide empty state
                if (products.isEmpty()) {
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.rvProducts.visibility = View.GONE
                    updateEmptyStateText()
                } else {
                    binding.layoutEmpty.visibility = View.GONE
                    binding.rvProducts.visibility = View.VISIBLE
                }
                
                println("📦 Products for ${productStatus.displayName}: ${products.size}")
            }
        }
    }

    private fun observeLoadingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.swipeRefresh.isRefreshing = isLoading
                binding.progressBar.visibility = if (isLoading && productAdapter.itemCount == 0) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    private fun observeErrorMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { errorMessage ->
                if (!errorMessage.isNullOrEmpty()) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    viewModel.clearErrorMessage()
                }
            }
        }
    }

    private fun updateEmptyStateText() {
        when (productStatus) {
            ProductStatus.IN_STOCK -> {
                binding.tvEmptyTitle.text = "Chưa có sản phẩm còn hàng"
                binding.tvEmptySubtitle.text = "Sản phẩm có sẵn sẽ hiển thị ở đây"
            }
            ProductStatus.OUT_OF_STOCK -> {
                binding.tvEmptyTitle.text = "Chưa có sản phẩm hết hàng"
                binding.tvEmptySubtitle.text = "Sản phẩm hết hàng sẽ hiển thị ở đây"
            }
            ProductStatus.PENDING_APPROVAL -> {
                binding.tvEmptyTitle.text = "Chưa có sản phẩm chờ duyệt"
                binding.tvEmptySubtitle.text = "Sản phẩm chờ phê duyệt sẽ hiển thị ở đây"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("ProductFragment", "onResume() called for status: ${productStatus.displayName}")
        
        // ✅ Reload data when fragment resumes
        refreshProductsForCurrentStatus()
    }
} 