package com.example.greenbuyapp.ui.shop.addProduct

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenbuyapp.databinding.FragmentProductVariantBinding
import com.example.greenbuyapp.data.product.model.ProductVariant
import com.example.greenbuyapp.domain.product.ProductRepository
import com.example.greenbuyapp.ui.base.BaseFragment
import com.example.greenbuyapp.ui.shop.productManagement.ProductManagementViewModel
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.android.ext.android.inject

class ProductVariantFragment : BaseFragment<FragmentProductVariantBinding, AddProductViewModel>() {

    override val viewModel: AddProductViewModel by viewModel()
    
    // ViewModel để reload ProductManagement sau khi hoàn thành
    private val productManagementViewModel: ProductManagementViewModel by viewModel()
    
    // Inject ProductRepository để tạo variants trực tiếp
    private val productRepository: ProductRepository by inject()
    
    private lateinit var variantAdapter: ProductVariantAdapter
    private var productId: Int = -1
    private var selectedVariantIndex: Int = -1

    // Local cache để track real-time variant changes
    private val variantCache = mutableMapOf<Int, ProductVariant>()
    
    // Photo picker launcher cho variant images
    private val variantPhotoPicker = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        handleSelectedVariantImage(uri)
    }

    companion object {
        private const val ARG_PRODUCT_ID = "arg_product_id"
        
        fun newInstance(productId: Int): ProductVariantFragment {
            return ProductVariantFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PRODUCT_ID, productId)
                }
            }
        }
    }

    override fun getLayoutResourceId(): Int = com.example.greenbuyapp.R.layout.fragment_product_variant

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProductVariantBinding {
        return FragmentProductVariantBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        productId = arguments?.getInt(ARG_PRODUCT_ID, -1) ?: -1
        if (productId == -1) {
            requireActivity().finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupButtons()
        
        // Set product ID in ViewModel
        viewModel.setProductId(productId)
        
        println("🏷️ ProductVariantFragment initialized with productId: $productId")
    }

    override fun observeViewModel() {
        observeVariants()
        observeAddVariantState()
        observeErrorMessages()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }
    }

    private fun setupRecyclerView() {
        variantAdapter = ProductVariantAdapter(
            onVariantChanged = { index, variant ->
                // Update local cache immediately for real-time tracking
                variantCache[index] = variant
                println("🗃️ Cached variant at index $index: color='${variant.color}', size='${variant.size}', price='${variant.price}', quantity='${variant.quantity}'")
                
                // Update ViewModel (async)
                println("📤 Fragment received variant update for index $index: color='${variant.color}', size='${variant.size}', price='${variant.price}', quantity='${variant.quantity}'")
                viewModel.updateVariant(index, variant)
            },
            onDeleteVariant = { index ->
                // Remove from cache
                variantCache.remove(index)
                // Shift cache indices down
                val keysToUpdate = variantCache.keys.filter { it > index }.sorted()
                keysToUpdate.forEach { oldIndex ->
                    val variant = variantCache.remove(oldIndex)
                    if (variant != null) {
                        variantCache[oldIndex - 1] = variant
                    }
                }
                
                viewModel.removeVariant(index)
                println("🗑️ Variant removed at index $index, cache updated")
            },
            onImagePicker = { index ->
                selectedVariantIndex = index
                openVariantPhotoPicker()
                println("📸 Opening photo picker for variant $index")
            },
            onForceRefresh = null, // Remove force refresh to avoid conflicts
            getCurrentVariant = { index ->
                // Check cache first, then ViewModel
                val cachedVariant = variantCache[index]
                val viewModelVariant = viewModel.variants.value.getOrNull(index)
                
                val finalVariant = cachedVariant ?: viewModelVariant
                println("🔍 Getting current variant at index $index:")
                println("   Cache: color='${cachedVariant?.color}', size='${cachedVariant?.size}', price='${cachedVariant?.price}', quantity='${cachedVariant?.quantity}'")
                println("   ViewModel: color='${viewModelVariant?.color}', size='${viewModelVariant?.size}', price='${viewModelVariant?.price}', quantity='${viewModelVariant?.quantity}'")
                println("   Final: color='${finalVariant?.color}', size='${finalVariant?.size}', price='${finalVariant?.price}', quantity='${finalVariant?.quantity}'")
                
                finalVariant
            },
            onForceItemUpdate = { position ->
                // Force update specific RecyclerView item với current cache data
                val cachedVariant = variantCache[position]
                if (cachedVariant != null) {
                    // Create new list để trigger DiffUtil
                    val currentList = variantAdapter.currentList.toMutableList()
                    if (position < currentList.size) {
                        currentList[position] = cachedVariant
                        variantAdapter.submitList(currentList.toList())
                        println("🔄 Force updated RecyclerView item at position $position with cache data")
                    }
                } else {
                    println("❌ No cache data found for position $position, cannot force update")
                }
            }
        )

        binding.rvVariants.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = variantAdapter
            // Tối ưu RecyclerView performance
            setHasFixedSize(false) // Allow dynamic size changes
            setItemViewCacheSize(10) // Increase cache size
        }
    }

    private fun setupButtons() {
        binding.btnAddVariant.setOnClickListener {
            // First, sync all current cache data to ViewModel before adding new variant
            variantCache.forEach { (index, cachedVariant) ->
                val viewModelVariant = viewModel.variants.value.getOrNull(index)
                if (viewModelVariant != null && cachedVariant != viewModelVariant) {
                    println("🔄 Syncing cache to ViewModel before add: index=$index")
                    viewModel.updateVariant(index, cachedVariant)
                }
            }
            
            viewModel.addVariant()
            println("➕ Add variant button clicked")
        }

        binding.btnFinish.setOnClickListener {
            createAllVariants()
            println("✅ Finish button clicked")
        }
    }

    private fun openVariantPhotoPicker() {
        variantPhotoPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun handleSelectedVariantImage(uri: Uri?) {
        println("📸 handleSelectedVariantImage called - uri: $uri, selectedVariantIndex: $selectedVariantIndex")
        
        if (uri != null && selectedVariantIndex >= 0) {
            // Update cache immediately
            val currentVariant = variantCache[selectedVariantIndex] ?: 
                viewModel.variants.value.getOrNull(selectedVariantIndex) ?: 
                return
            
            val updatedVariant = currentVariant.copy(imageUri = uri.toString())
            variantCache[selectedVariantIndex] = updatedVariant
            println("🗃️ Updated image in cache for variant $selectedVariantIndex: ${uri}")
            
            // Update ViewModel to sync data
            viewModel.updateVariant(selectedVariantIndex, updatedVariant)
            
            // Force refresh adapter với cache data
            val currentVariants = viewModel.variants.value.toMutableList()
            if (selectedVariantIndex < currentVariants.size) {
                val displayVariants = currentVariants.mapIndexed { index, viewModelVariant ->
                    variantCache[index] ?: viewModelVariant
                }
                variantAdapter.submitList(displayVariants)
                println("🔄 Force refreshed adapter with cache data")
            }
            
            Toast.makeText(context, "✅ Đã chọn ảnh cho loại sản phẩm #${selectedVariantIndex + 1}", Toast.LENGTH_SHORT).show()
            println("📸 Image selected for variant $selectedVariantIndex: $uri")
            selectedVariantIndex = -1 // Reset after successful update
        } else {
            Toast.makeText(context, "❌ Không chọn ảnh nào", Toast.LENGTH_SHORT).show()
            println("❌ No image selected for variant $selectedVariantIndex, uri: $uri")
            selectedVariantIndex = -1 // Reset on failure too
        }
    }

    private fun createAllVariants() {
        // Debug: Log tất cả variants hiện tại
        viewModel.logCurrentVariants()
        
        // Debug: Log cache data
        println("🗃️ Current cache data:")
        variantCache.forEach { (index, variant) ->
            println("   [$index] color='${variant.color}', size='${variant.size}', price='${variant.price}', quantity='${variant.quantity}', imageUri='${variant.imageUri}'")
        }
        
        // Use cache data for validation (more up-to-date than ViewModel)
        val variantsToValidate = if (variantCache.isNotEmpty()) {
            // Use cache data
            variantCache.values.toList()
        } else {
            // Fallback to ViewModel data
            viewModel.variants.value
        }
        
        if (variantsToValidate.isEmpty()) {
            Toast.makeText(context, "Cần ít nhất 1 loại sản phẩm", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate từng variant using cache data
        for (i in variantsToValidate.indices) {
            val variant = variantsToValidate.elementAt(i)
            val error = viewModel.validateVariant(variant)
            if (error != null) {
                Toast.makeText(context, "Loại sản phẩm #${i + 1}: $error", Toast.LENGTH_LONG).show()
                return
            }
        }

        // Create variants directly from cache data, bypass ViewModel sync issues
        lifecycleScope.launch {
            println("🚀 Creating variants directly from cache data...")
            
            // Filter valid variants from cache
            val validCacheVariants = variantCache.values.filter { variant ->
                variant.color.isNotBlank() && 
                variant.size.isNotBlank() && 
                variant.price.isNotBlank() && 
                variant.quantity.isNotBlank() && 
                variant.imageUri != null && variant.imageUri != "null"
            }
            
            println("🔍 Valid cache variants: ${validCacheVariants.size}")
            validCacheVariants.forEachIndexed { index, variant ->
                println("   [$index] color='${variant.color}', size='${variant.size}', price='${variant.price}', quantity='${variant.quantity}', imageUri='${variant.imageUri}'")
            }
            
            if (validCacheVariants.isEmpty()) {
                Toast.makeText(context, "❌ Cần ít nhất 1 loại sản phẩm hợp lệ", Toast.LENGTH_LONG).show()
                return@launch
            }
            
            // Create variants directly using cache data
            createVariantsFromCache(validCacheVariants)
        }
    }

    /**
     * Create variants directly from cache data without ViewModel sync
     */
    private suspend fun createVariantsFromCache(cacheVariants: List<ProductVariant>) {
        val productId = viewModel.productId.value
        if (productId == null) {
            Toast.makeText(context, "❌ Product ID không tồn tại", Toast.LENGTH_LONG).show()
            return
        }
        
        println("🔄 Creating ${cacheVariants.size} variants from cache for product ID: $productId")
        
        // Set loading state manually
        binding.btnFinish.isEnabled = false
        binding.btnFinish.text = "Đang tạo..."
        binding.btnAddVariant.isEnabled = false
        
        var successCount = 0
        var hasError = false
        
        for ((index, variant) in cacheVariants.withIndex()) {
            println("🔄 Creating variant ${index + 1}/${cacheVariants.size}: ${variant.color} ${variant.size}")
            
            try {
                when (val result = productRepository.createAttribute(
                    context = requireContext(),
                    productId = productId,
                    color = variant.color,
                    size = variant.size,
                    price = variant.price.toDouble(),
                    quantity = variant.quantity.toInt(),
                    imageUri = android.net.Uri.parse(variant.imageUri)
                )) {
                    is Result.Success -> {
                        successCount++
                        println("✅ Variant created: ${variant.color} ${variant.size} (${successCount}/${cacheVariants.size})")
                    }
                    is Result.Error -> {
                        hasError = true
                        Toast.makeText(context, "❌ Lỗi tạo variant: ${result.error}", Toast.LENGTH_LONG).show()
                        println("❌ Error creating variant: ${result.error}")
                        break
                    }
                    is Result.NetworkError -> {
                        hasError = true
                        Toast.makeText(context, "❌ Lỗi kết nối mạng", Toast.LENGTH_LONG).show()
                        println("❌ Network error creating variant")
                        break
                    }
                    is Result.Loading -> {
                        // Ignore loading state
                    }
                }
            } catch (e: Exception) {
                hasError = true
                Toast.makeText(context, "❌ Lỗi tạo variant: ${e.message}", Toast.LENGTH_LONG).show()
                println("❌ Exception creating variant: ${e.message}")
                break
            }
        }
        
        // Restore button state
        binding.btnFinish.isEnabled = true
        binding.btnAddVariant.isEnabled = true
        
        if (!hasError && successCount == cacheVariants.size) {
            binding.btnFinish.text = "Hoàn thành"
            Toast.makeText(context, "🎉 Tạo tất cả loại sản phẩm thành công!", Toast.LENGTH_LONG).show()
            
            // Finish and return to ProductManagement
            requireActivity().finish()
            
            println("✅ All variants created successfully from cache!")
        } else {
            binding.btnFinish.text = "Hoàn thành"
            println("❌ Some variants failed to create")
        }
    }

    private fun observeVariants() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.variants.collect { variants ->
                // Sync cache với ViewModel data
                syncCacheWithViewModel(variants)
                
                // Create display list using cache data when available
                val displayVariants = variants.mapIndexed { index, viewModelVariant ->
                    variantCache[index] ?: viewModelVariant
                }
                
                variantAdapter.submitList(displayVariants)
                
                // Update button state
                binding.btnFinish.isEnabled = variants.isNotEmpty()
                
                println("📦 Variants updated: ${variants.size}")
                println("📦 Cache entries: ${variantCache.size}")
                displayVariants.forEachIndexed { index, variant ->
                    println("   [$index] Display: color='${variant.color}', size='${variant.size}', price='${variant.price}', quantity='${variant.quantity}', imageUri='${variant.imageUri}'")
                }
            }
        }
    }

    /**
     * Sync local cache với ViewModel data
     */
    private fun syncCacheWithViewModel(variants: List<ProductVariant>) {
        println("🔄 Syncing cache with ViewModel...")
        
        // Clear old cache entries that don't exist in ViewModel
        val indicesToRemove = variantCache.keys.filter { it >= variants.size }
        indicesToRemove.forEach { variantCache.remove(it) }
        
        // Update existing cache entries if ViewModel has newer data
        variants.forEachIndexed { index, viewModelVariant ->
            val cachedVariant = variantCache[index]
            if (cachedVariant == null) {
                // No cache entry, use ViewModel data as initial
                variantCache[index] = viewModelVariant
                println("   [$index] Initialized cache from ViewModel: color='${viewModelVariant.color}', size='${viewModelVariant.size}', imageUri='${viewModelVariant.imageUri}'")
            } else {
                // Cache exists - check if we should update it
                val cacheHasData = cachedVariant.color.isNotBlank() || 
                                 cachedVariant.size.isNotBlank() || 
                                 cachedVariant.price.isNotBlank() || 
                                 cachedVariant.quantity.isNotBlank() || 
                                 (cachedVariant.imageUri != null && cachedVariant.imageUri != "null")
                
                val viewModelHasData = viewModelVariant.color.isNotBlank() || 
                                     viewModelVariant.size.isNotBlank() || 
                                     viewModelVariant.price.isNotBlank() || 
                                     viewModelVariant.quantity.isNotBlank() || 
                                     (viewModelVariant.imageUri != null && viewModelVariant.imageUri != "null")
                
                if (cacheHasData && !viewModelHasData) {
                    // Keep cache data as it's more complete, and update ViewModel with cache data
                    println("   [$index] Keeping cached data (more complete): color='${cachedVariant.color}', size='${cachedVariant.size}', imageUri='${cachedVariant.imageUri}'")
                    // Update ViewModel to match cache
                    viewModel.updateVariant(index, cachedVariant)
                } else if (!cacheHasData && viewModelHasData) {
                    // Update cache with ViewModel data
                    variantCache[index] = viewModelVariant
                    println("   [$index] Updated cache from ViewModel: color='${viewModelVariant.color}', size='${viewModelVariant.size}', imageUri='${viewModelVariant.imageUri}'")
                } else {
                    // Both have data or both empty - keep cache as source of truth for user input
                    println("   [$index] Keeping cached data (source of truth): color='${cachedVariant.color}', size='${cachedVariant.size}', imageUri='${cachedVariant.imageUri}'")
                    
                    // If cache has data but different from ViewModel, update ViewModel
                    if (cacheHasData && cachedVariant != viewModelVariant) {
                        println("   [$index] Syncing ViewModel with cache data")
                        viewModel.updateVariant(index, cachedVariant)
                    }
                }
            }
        }
    }

    private fun observeAddVariantState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addVariantState.collect { state ->
                when (state) {
                    is AddVariantUiState.Idle -> {
                        binding.btnFinish.isEnabled = true
                        binding.btnFinish.text = "Hoàn thành"
                        binding.btnAddVariant.isEnabled = true
                    }
                    is AddVariantUiState.Loading -> {
                        binding.btnFinish.isEnabled = false
                        binding.btnFinish.text = "Đang tạo..."
                        binding.btnAddVariant.isEnabled = false
                        println("⏳ Creating variants...")
                    }
                    is AddVariantUiState.Success -> {
                        binding.btnFinish.isEnabled = true
                        binding.btnFinish.text = "Hoàn thành"
                        binding.btnAddVariant.isEnabled = true
                        
                        Toast.makeText(context, "🎉 Tạo tất cả loại sản phẩm thành công!", Toast.LENGTH_LONG).show()
                        
                        // Reload ProductManagement data
                        reloadProductManagement()
                        
                        // Finish and return to ProductManagement
                        requireActivity().finish()
                        
                        println("✅ All variants created successfully!")
                    }
                    is AddVariantUiState.Error -> {
                        binding.btnFinish.isEnabled = true
                        binding.btnFinish.text = "Hoàn thành"
                        binding.btnAddVariant.isEnabled = true
                        
                        Toast.makeText(context, "❌ ${state.message}", Toast.LENGTH_LONG).show()
                        println("❌ Create variants error: ${state.message}")
                    }
                }
            }
        }
    }

    private fun observeErrorMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { message ->
                if (!message.isNullOrEmpty()) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    viewModel.clearErrorMessage()
                }
            }
        }
    }

    /**
     * Reload ProductManagement inventory stats và by-status data
     */
    private fun reloadProductManagement() {
        // Không cần reload trong fragment này vì ProductManagementActivity 
        // sẽ tự động reload trong onResume()
        println("🔄 ProductManagement will reload automatically on resume")
    }

    override fun onDestroy() {
        super.onDestroy()
        
        // Clean up safely
        try {
            // Clear cache to prevent memory leaks
            variantCache.clear()
            println("🗃️ Variant cache cleared")
            
            // Reset ViewModel state if needed
            if (isAdded && viewModel.addVariantState.value !is AddVariantUiState.Loading) {
                viewModel.resetState()
            }
        } catch (e: Exception) {
            println("❌ Error in onDestroy: ${e.message}")
        }
    }

    override fun onPause() {
        super.onPause()
        // DON'T reset selectedVariantIndex here - photo picker causes pause!
        // selectedVariantIndex = -1
    }

    override fun onDestroyView() {
        // Clear adapter reference để tránh memory leak TRƯỚC khi gọi super
        if (::variantAdapter.isInitialized && isBindingInitialized()) {
            binding.rvVariants.adapter = null
        }
        super.onDestroyView()
    }
} 