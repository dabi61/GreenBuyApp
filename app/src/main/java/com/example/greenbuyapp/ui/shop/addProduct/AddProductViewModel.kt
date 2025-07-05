package com.example.greenbuyapp.ui.shop.addProduct

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.category.model.SubCategory
import com.example.greenbuyapp.data.product.model.CreateAttributeResponse
import com.example.greenbuyapp.data.product.model.CreateProductResponse
import com.example.greenbuyapp.data.product.model.ProductVariant
import com.example.greenbuyapp.domain.category.CategoryRepository
import com.example.greenbuyapp.domain.product.ProductRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed class AddProductUiState {
    object Idle : AddProductUiState()
    object Loading : AddProductUiState() 
    data class Success(val productResponse: CreateProductResponse) : AddProductUiState()
    data class Error(val message: String) : AddProductUiState()
}

sealed class AddVariantUiState {
    object Idle : AddVariantUiState()
    object Loading : AddVariantUiState()
    data class Success(val attributeResponse: CreateAttributeResponse) : AddVariantUiState()
    data class Error(val message: String) : AddVariantUiState()
}

class AddProductViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // UI State cho việc tạo sản phẩm
    private val _addProductState = MutableStateFlow<AddProductUiState>(AddProductUiState.Idle)
    val addProductState: StateFlow<AddProductUiState> = _addProductState.asStateFlow()

    // UI State cho việc tạo variants
    private val _addVariantState = MutableStateFlow<AddVariantUiState>(AddVariantUiState.Idle)
    val addVariantState: StateFlow<AddVariantUiState> = _addVariantState.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Product ID sau khi tạo thành công
    private val _productId = MutableStateFlow<Int?>(null)
    val productId: StateFlow<Int?> = _productId.asStateFlow()

    // Danh sách variants
    private val _variants = MutableStateFlow<List<ProductVariant>>(listOf(ProductVariant()))
    val variants: StateFlow<List<ProductVariant>> = _variants.asStateFlow()

    // SubCategories state
    private val _subCategories = MutableStateFlow<List<SubCategory>>(emptyList())
    val subCategories: StateFlow<List<SubCategory>> = _subCategories.asStateFlow()
    
    private val _subCategoriesLoading = MutableStateFlow(false)
    val subCategoriesLoading: StateFlow<Boolean> = _subCategoriesLoading.asStateFlow()
    
    private val _selectedSubCategory = MutableStateFlow<SubCategory?>(null)
    val selectedSubCategory: StateFlow<SubCategory?> = _selectedSubCategory.asStateFlow()

    // Counter để track số variants đã tạo thành công
    private val _completedVariants = MutableStateFlow(0)
    private val _totalVariants = MutableStateFlow(0)

    // Mutex để synchronize variant updates
    private val variantUpdateMutex = Mutex()

    init {
        // Load subcategories khi ViewModel được khởi tạo
        loadSubCategories()
    }

    /**
     * Load danh sách subcategories
     */
    fun loadSubCategories() {
        viewModelScope.launch {
            _subCategoriesLoading.value = true
            
            when (val result = categoryRepository.getSubCategories()) {
                is Result.Success -> {
                    _subCategories.value = result.value
                    println("✅ Loaded ${result.value.size} subcategories")
                }
                is Result.Error -> {
                    _errorMessage.value = "Lỗi tải danh mục: ${result.error}"
                    println("❌ Error loading subcategories: ${result.error}")
                }
                is Result.NetworkError -> {
                    _errorMessage.value = "Lỗi kết nối mạng khi tải danh mục"
                    println("❌ Network error loading subcategories")
                }
                is Result.Loading -> {
                    // Already handled by _subCategoriesLoading
                }
            }
            
            _subCategoriesLoading.value = false
        }
    }

    /**
     * Chọn subcategory
     */
    fun selectSubCategory(subCategory: SubCategory) {
        _selectedSubCategory.value = subCategory
        println("📂 Selected subcategory: ${subCategory.name} (ID: ${subCategory.id})")
    }

    /**
     * Clear selected subcategory
     */
    fun clearSelectedSubCategory() {
        _selectedSubCategory.value = null
    }

    /**
     * Tạo sản phẩm mới
     */
    fun createProduct(
        context: Context,
        name: String,
        description: String,
        price: Double,
        subCategoryId: Int,
        coverUri: Uri
    ) {
        viewModelScope.launch {
            runCatching {
                _addProductState.value = AddProductUiState.Loading
                
                when (val result = productRepository.createProduct(
                    context, name, description, price, subCategoryId, coverUri
                )) {
                    is Result.Success -> {
                        val response = result.value
                        _productId.value = response.product_id
                        _addProductState.value = AddProductUiState.Success(response)
                        println("✅ Product created successfully: ${response.name} (ID: ${response.product_id})")
                    }
                    is Result.Error -> {
                        _addProductState.value = AddProductUiState.Error(result.error ?: "Lỗi tạo sản phẩm")
                        println("❌ Error creating product: ${result.error}")
                    }
                    is Result.NetworkError -> {
                        _addProductState.value = AddProductUiState.Error("Lỗi kết nối mạng")
                        println("❌ Network error creating product")
                    }
                    is Result.Loading -> {
                        // Already handled
                    }
                }
                
            }.onFailure { exception ->
                _addProductState.value = AddProductUiState.Error("Lỗi tạo sản phẩm: ${exception.message}")
                println("❌ Exception creating product: ${exception.message}")
            }
        }
    }

    /**
     * Chỉnh sửa sản phẩm mới
     */
    fun editProduct(
        context: Context,
        productId: Int,
        name: String,
        description: String,
        price: Double,
        subCategoryId: Int,
        coverUri: Uri
    ) {
        viewModelScope.launch {
            runCatching {
                _addProductState.value = AddProductUiState.Loading

                when (val result = productRepository.editProduct(
                    context, productId, name, description, price, subCategoryId, coverUri
                )) {
                    is Result.Success -> {
                        val response = result.value
                        _productId.value = response.product_id
                        _addProductState.value = AddProductUiState.Success(response)
                        println("✅ Product created successfully: ${response.name} (ID: ${response.product_id})")
                    }
                    is Result.Error -> {
                        _addProductState.value = AddProductUiState.Error(result.error ?: "Lỗi tạo sản phẩm")
                        println("❌ Error creating product: ${result.error}")
                    }
                    is Result.NetworkError -> {
                        _addProductState.value = AddProductUiState.Error("Lỗi kết nối mạng")
                        println("❌ Network error creating product")
                    }
                    is Result.Loading -> {
                        // Already handled
                    }
                }

            }.onFailure { exception ->
                _addProductState.value = AddProductUiState.Error("Lỗi tạo sản phẩm: ${exception.message}")
                println("❌ Exception creating product: ${exception.message}")
            }
        }
    }

    /**
     * ✅ Chỉnh sửa sản phẩm không có ảnh mới (chỉ cập nhật text fields)
     */
    fun editProductWithoutNewImage(
        productId: Int,
        name: String,
        description: String,
        price: Double,
        subCategoryId: Int
    ) {
        viewModelScope.launch {
            runCatching {
                _addProductState.value = AddProductUiState.Loading
                
                // ✅ Gọi API thực sự thay vì dummy response
                when (val result = productRepository.editProductText(
                    productId = productId,
                    name = name,
                    description = description,
                    price = price,
                    subCategoryId = subCategoryId
                )) {
                    is Result.Success -> {
                        val response = result.value
                        _productId.value = response.product_id
                        _addProductState.value = AddProductUiState.Success(response)
                        println("✅ Product edited successfully (text only): ${response.name} (ID: ${response.product_id})")
                    }
                    is Result.Error -> {
                        _addProductState.value = AddProductUiState.Error(result.error ?: "Lỗi chỉnh sửa sản phẩm")
                        println("❌ Error editing product: ${result.error}")
                    }
                    is Result.NetworkError -> {
                        _addProductState.value = AddProductUiState.Error("Lỗi kết nối mạng")
                        println("❌ Network error editing product")
                    }
                    is Result.Loading -> {
                        // Already handled
                    }
                }

            }.onFailure { exception ->
                _addProductState.value = AddProductUiState.Error("Lỗi chỉnh sửa sản phẩm: ${exception.message}")
                println("❌ Exception editing product: ${exception.message}")
            }
        }
    }

    /**
     * Thêm variant mới vào danh sách
     */
    fun addVariant() {
        viewModelScope.launch {
            variantUpdateMutex.withLock {
                val currentVariants = _variants.value.toMutableList()
                currentVariants.add(ProductVariant())
                _variants.value = currentVariants
                println("➕ Added new variant. Total: ${currentVariants.size}")
            }
        }
    }

    /**
     * Xóa variant khỏi danh sách
     */
    fun removeVariant(index: Int) {
        viewModelScope.launch {
            variantUpdateMutex.withLock {
                val currentVariants = _variants.value.toMutableList()
                if (index >= 0 && index < currentVariants.size && currentVariants.size > 1) {
                    currentVariants.removeAt(index)
                    _variants.value = currentVariants
                    println("🗑️ Removed variant at index $index. Total: ${currentVariants.size}")
                }
            }
        }
    }

    /**
     * Cập nhật variant tại vị trí index (synchronized để tránh race conditions)
     */
    fun updateVariant(index: Int, variant: ProductVariant) {
        viewModelScope.launch {
            variantUpdateMutex.withLock {
                val currentVariants = _variants.value.toMutableList()
                println("🔄 updateVariant called - index: $index, total variants: ${currentVariants.size}")
                println("🔄 Old variant at $index: color='${currentVariants.getOrNull(index)?.color}', size='${currentVariants.getOrNull(index)?.size}'")
                println("🔄 New variant: color='${variant.color}', size='${variant.size}', price='${variant.price}', quantity='${variant.quantity}'")
                
                if (index >= 0 && index < currentVariants.size) {
                    currentVariants[index] = variant
                    _variants.value = currentVariants
                    println("✅ Variant updated successfully at index $index")
                } else {
                    println("❌ Invalid index $index for variants list of size ${currentVariants.size}")
                }
            }
        }
    }

    /**
     * Synchronous variant update để đảm bảo instant update
     */
    suspend fun updateVariantSync(index: Int, variant: ProductVariant) {
        variantUpdateMutex.withLock {
            val currentVariants = _variants.value.toMutableList()
            println("🔄 updateVariantSync called - index: $index, total variants: ${currentVariants.size}")
            println("🔄 Old variant at $index: color='${currentVariants.getOrNull(index)?.color}', size='${currentVariants.getOrNull(index)?.size}'")
            println("🔄 New variant: color='${variant.color}', size='${variant.size}', price='${variant.price}', quantity='${variant.quantity}', imageUri='${variant.imageUri}'")
            
            if (index >= 0 && index < currentVariants.size) {
                currentVariants[index] = variant
                _variants.value = currentVariants
                println("✅ Variant updated synchronously at index $index")
            } else {
                println("❌ Invalid index $index for variants list of size ${currentVariants.size}")
            }
        }
    }

    /**
     * Tạo tất cả variants cho sản phẩm
     */
    fun createAllVariants(context: Context) {
        val productId = _productId.value
        if (productId == null) {
            _addVariantState.value = AddVariantUiState.Error("Product ID không tồn tại")
            return
        }

        viewModelScope.launch {
            runCatching {
                _addVariantState.value = AddVariantUiState.Loading
                
                println("🔍 Filtering variants for validation...")
                val allVariants = _variants.value
                println("🔍 Total variants before filter: ${allVariants.size}")
                
                allVariants.forEachIndexed { index, variant ->
                    println("🔍 Variant [$index]: color='${variant.color}', size='${variant.size}', price='${variant.price}', quantity='${variant.quantity}', imageUri='${variant.imageUri}'")
                    println("   - color.isNotBlank(): ${variant.color.isNotBlank()}")
                    println("   - size.isNotBlank(): ${variant.size.isNotBlank()}")
                    println("   - price.isNotBlank(): ${variant.price.isNotBlank()}")
                    println("   - quantity.isNotBlank(): ${variant.quantity.isNotBlank()}")
                    println("   - imageUri != null: ${variant.imageUri != null}")
                    println("   - imageUri != \"null\": ${variant.imageUri != "null"}")
                    println("   - imageUri valid: ${variant.imageUri != null && variant.imageUri != "null"}")
                }
                
                val validVariants = allVariants.filter { variant ->
                    variant.color.isNotBlank() && 
                    variant.size.isNotBlank() && 
                    variant.price.isNotBlank() && 
                    variant.quantity.isNotBlank() && 
                    variant.imageUri != null && variant.imageUri != "null"
                }

                println("🔍 Valid variants after filter: ${validVariants.size}")
                
                if (validVariants.isEmpty()) {
                    _addVariantState.value = AddVariantUiState.Error("Cần ít nhất 1 loại sản phẩm hợp lệ")
                    println("❌ No valid variants found!")
                    return@runCatching
                }

                _totalVariants.value = validVariants.size
                _completedVariants.value = 0

                // Tạo từng variant
                for (variant in validVariants) {
                    createSingleVariant(context, productId, variant)
                }
                
            }.onFailure { exception ->
                _addVariantState.value = AddVariantUiState.Error("Lỗi tạo variants: ${exception.message}")
                println("❌ Exception creating variants: ${exception.message}")
            }
        }
    }

    /**
     * Tạo một variant đơn lẻ
     */
    private suspend fun createSingleVariant(context: Context, productId: Int, variant: ProductVariant) {
        when (val result = productRepository.createAttribute(
            context = context,
            productId = productId,
            color = variant.color,
            size = variant.size,
            price = variant.price.toDouble(),
            quantity = variant.quantity.toInt(),
            imageUri = Uri.parse(variant.imageUri)
        )) {
            is Result.Success -> {
                val completed = _completedVariants.value + 1
                _completedVariants.value = completed
                
                println("✅ Variant created: ${variant.color} ${variant.size} (${completed}/${_totalVariants.value})")
                
                // Nếu đã tạo xong tất cả variants
                if (completed == _totalVariants.value) {
                    _addVariantState.value = AddVariantUiState.Success(result.value)
                    println("🎉 All variants created successfully!")
                }
            }
            is Result.Error -> {
                _addVariantState.value = AddVariantUiState.Error(result.error ?: "Lỗi tạo variant")
                println("❌ Error creating variant: ${result.error}")
            }
            is Result.NetworkError -> {
                _addVariantState.value = AddVariantUiState.Error("Lỗi kết nối mạng khi tạo variant")
                println("❌ Network error creating variant")
            }
            is Result.Loading -> {
                // Loading handled by parent function
            }
        }
    }

    /**
     * Validate variant data
     */
    fun validateVariant(variant: ProductVariant): String? {
        println("🔍 Validating variant: color='${variant.color}', size='${variant.size}', price='${variant.price}', quantity='${variant.quantity}', imageUri='${variant.imageUri}'")
        
        return when {
            variant.color.isBlank() -> {
                println("❌ Validation failed: color is blank")
                "Vui lòng nhập màu sắc"
            }
            variant.size.isBlank() -> {
                println("❌ Validation failed: size is blank")
                "Vui lòng nhập kích thước"
            }
            variant.price.isBlank() -> {
                println("❌ Validation failed: price is blank")
                "Vui lòng nhập giá"
            }
            variant.quantity.isBlank() -> {
                println("❌ Validation failed: quantity is blank")
                "Vui lòng nhập số lượng"
            }
            variant.imageUri == null || variant.imageUri == "null" -> {
                println("❌ Validation failed: imageUri is null or 'null' string")
                "Vui lòng chọn ảnh"
            }
            else -> {
                try {
                    variant.price.toDouble()
                    variant.quantity.toInt()
                    println("✅ Variant validation passed")
                    null
                } catch (e: NumberFormatException) {
                    println("❌ Validation failed: number format exception - ${e.message}")
                    "Giá và số lượng phải là số hợp lệ"
                }
            }
        }
    }

    /**
     * Debug function để log tất cả variants hiện tại
     */
    fun logCurrentVariants() {
        val currentVariants = _variants.value
        println("📋 Current variants (${currentVariants.size}):")
        currentVariants.forEachIndexed { index, variant ->
            println("   [$index] ID: ${variant.id}, color: '${variant.color}', size: '${variant.size}', price: '${variant.price}', quantity: '${variant.quantity}', imageUri: '${variant.imageUri}'")
        }
    }

    /**
     * Reset state
     */
    fun resetState() {
        _addProductState.value = AddProductUiState.Idle
        _addVariantState.value = AddVariantUiState.Idle
        _errorMessage.value = null
        _productId.value = null
        _variants.value = listOf(ProductVariant())
        _completedVariants.value = 0
        _totalVariants.value = 0
        _selectedSubCategory.value = null
    }   

    /**
     * Set product ID
     */
    fun setProductId(productId: Int) {
        _productId.value = productId
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
} 