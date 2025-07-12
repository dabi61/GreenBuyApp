package com.example.greenbuyapp.ui.mall

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.FragmentMallBinding
import com.example.greenbuyapp.ui.cart.CartActivity
import com.example.greenbuyapp.ui.product.ProductActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.widget.TextView

/**
 * Fragment hiển thị màn hình mall với sản phẩm nổi bật
 */
class MallFragment : Fragment() {
    
    private var _binding: FragmentMallBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MallViewModel by viewModel()
    
    private lateinit var categoryChipAdapter: CategoryChipAdapter
    private lateinit var featuredProductAdapter: FeaturedProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMallBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupStatusBar()
        setupRecyclerViews()
        setupClickListeners()
        setupSearchView()
        observeViewModel()
    }
    
    private fun setupStatusBar() {
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.main_color)
    }
    
    private fun setupRecyclerViews() {
        // Category chips adapter
        categoryChipAdapter = CategoryChipAdapter { category ->
            viewModel.updateSelectedCategory(category?.id)
        }
        
        // Featured products adapter
        featuredProductAdapter = FeaturedProductAdapter(
            onProductClick = { product ->
                // Navigate to ProductActivity
                val intent = ProductActivity.createIntent(
                    requireContext(),
                    product.product_id,
                    product.shop_id,
                    product.description,
                    product.name
                )
                startActivity(intent)
            },
            onAddToCartClick = { product ->
                // Add to cart using ViewModel
                viewModel.addToCart(product)
            },
            getShopInfo = { shopId ->
                // Get shop info from ViewModel cache
                viewModel.getShopInfo(shopId)
            }
        )
        
        // Setup RecyclerViews
        // Note: rvCategories not available in current layout
        
        binding.rvFeaturedProducts.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = featuredProductAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupClickListeners() {
        // Cart button
        
        
        // Clear filter (if available in layout)
        // binding.tvClearFilter.setOnClickListener {
        //     viewModel.updateSelectedCategory(null)
        //     categoryChipAdapter.updateSelectedCategory(null)
        // }
        
        // Refresh button
        binding.tvRefresh.setOnClickListener {
            viewModel.refresh()
            showMessage("Đang làm mới...")
        }
        
        // Retry button - reset search và refresh
        binding.btnRetry.setOnClickListener {
            // Clear search input
            binding.etSearch.text?.clear()
            // Reset and refresh
            viewModel.resetAndRefresh()
            showMessage("Đang tải lại dữ liệu...")
        }
        
        // Scroll to top FAB - smooth scroll to position 0
        binding.fabScrollTop.setOnClickListener {
            binding.rvFeaturedProducts.smoothScrollToPosition(0)
            showMessage("Cuộn lên đầu trang")
        }
    }
    
    private fun setupSearchView() {
        binding.etSearch.addTextChangedListener { text ->
            val query = text?.toString() ?: ""
            viewModel.updateSearchQuery(query)
            println("🔍 Search input changed: '$query'")
        }
    }
    
    private fun observeViewModel() {
        // Observe featured products
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredProducts.collect { products ->
                featuredProductAdapter.submitList(products)
                updateProductCount(products.size)
                
                // Check if this is an empty result due to search/filter
                val isSearching = viewModel.searchQuery.value.isNotBlank() || viewModel.selectedCategoryId.value != null
                val isEmptyDueToFilter = products.isEmpty() && isSearching
                
                updateUIState(products.isEmpty(), false, isEmptyDueToFilter)
                println("✅ Featured products updated: ${products.size} items, isSearching: $isSearching")
            }
        }
        
        // Observe categories
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collect { categories ->
                categoryChipAdapter.submitList(categories)
                updateCategoryCount(categories.size)
                println("✅ Categories updated: ${categories.size} items")
            }
        }
        
        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.featuredLoading.collect { isLoading ->
                updateUIState(false, isLoading, false)
                println("⏳ Loading state: $isLoading")
            }
        }
        
        // Observe errors
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.featuredError.collect { error ->
                error?.let {
                    showError(it)
                    viewModel.clearErrors()
                }
            }
        }
        
        // Observe selected category (if category chips are available)
        // viewLifecycleOwner.lifecycleScope.launch {
        //     viewModel.selectedCategoryId.collect { categoryId ->
        //         categoryChipAdapter.updateSelectedCategory(categoryId)
        //     }
        // }
        
        // Observe add to cart loading
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addToCartLoading.collect { isLoading ->
                // Show loading indicator for add to cart
                if (isLoading) {
                    showMessage("Đang lấy thông tin sản phẩm và thêm vào giỏ hàng...")
                }
            }
        }
        
        // Observe add to cart messages
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addToCartMessage.collect { message ->
                message?.let {
                    when {
                        it.contains("Đã thêm") -> {
                            showSuccess(it)
                        }
                        it.contains("không có biến thể") -> {
                            showWarning(it)
                        }
                        else -> {
                            showError(it)
                        }
                    }
                    viewModel.clearAddToCartMessage()
                }
            }
        }
        
        // Observe shop info cache updates
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.shopInfoCache.collect { shopCache ->
                // Refresh adapter when shop info is loaded
                if (shopCache.isNotEmpty()) {
                    featuredProductAdapter.notifyDataSetChanged()
                    println("✅ Shop info cache updated: ${shopCache.size} shops")
                }
            }
        }
        
        // Observe search query changes
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchQuery.collect { query ->
                // Update search input if it doesn't match (e.g., when reset)
                if (binding.etSearch.text?.toString() != query) {
                    binding.etSearch.setText(query)
                }
                println("🔍 Search query changed: '$query'")
            }
        }
    }
    
    private fun updateProductCount(count: Int) {
        binding.tvProductCount.text = count.toString()
    }
    
    private fun updateCategoryCount(count: Int) {
        binding.tvCategoryCount.text = count.toString()
    }
    
    private fun updateUIState(isEmpty: Boolean, isLoading: Boolean, isEmptyDueToFilter: Boolean = false) {
        binding.apply {
            // Loading state
            layoutLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            
            // Empty state - chỉ hiển thị khi không có sản phẩm và không đang loading
            layoutEmpty.visibility = if (isEmpty && !isLoading) View.VISIBLE else View.GONE
            
            // Content visibility - hiển thị khi có sản phẩm và không đang loading
            rvFeaturedProducts.visibility = if (!isEmpty && !isLoading) View.VISIBLE else View.GONE
            
            // Update empty state message based on context
            if (isEmpty && !isLoading) {
                val emptyTitle = layoutEmpty.findViewById<TextView>(R.id.tvEmptyTitle)
                val emptyDescription = layoutEmpty.findViewById<TextView>(R.id.tvEmptyDescription)
                
                if (isEmptyDueToFilter) {
                    emptyTitle?.text = "Không tìm thấy sản phẩm nào"
                    emptyDescription?.text = "Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm"
                } else {
                    emptyTitle?.text = "Chưa có sản phẩm nổi bật"
                    emptyDescription?.text = "Vui lòng thử lại sau hoặc kiểm tra kết nối mạng"
                }
            }
            
            // Debug log
            println("🔄 UI State - isEmpty: $isEmpty, isLoading: $isLoading, isEmptyDueToFilter: $isEmptyDueToFilter")
            println("🔄 Visibility - Loading: ${layoutLoading.visibility}, Empty: ${layoutEmpty.visibility}, Products: ${rvFeaturedProducts.visibility}")
        }
    }
    
    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
    
    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.green_600))
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            .setAction("Xem giỏ hàng") {
                val intent = CartActivity.createIntent(requireContext())
                startActivity(intent)
            }
            .show()
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.red_600))
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            .setAction("Đóng") { }
            .show()
    }
    
    private fun showWarning(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.orange_600))
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            .setAction("Đóng") { }
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}