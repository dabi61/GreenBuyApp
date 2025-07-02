package com.example.greenbuyapp.ui.shop.addProduct

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.category.model.SubCategory
import com.example.greenbuyapp.databinding.ActivityAddProductBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.util.ImageTransform
import com.example.greenbuyapp.util.loadUrl
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddProductActivity : BaseActivity<ActivityAddProductBinding>() {
    override val viewModel: AddProductViewModel by viewModel()

    override val binding: ActivityAddProductBinding by lazy {
        ActivityAddProductBinding.inflate(layoutInflater)
    }

    private var selectedCoverUri: Uri? = null
    private var selectedSubCategory: SubCategory? = null
    private lateinit var subCategoryAdapter: SubCategoryAdapter

    // Photo picker launcher
    private val photoPicker = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        handleSelectedCoverImage(uri)
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, AddProductActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun initViews() {
        setupToolbar()
        setupCoverImagePicker()
        setupSubCategoryDropdown()
        setupNextButton()
    }

    override fun observeViewModel() {
        observeAddProductState()
        observeSubCategories()
        observeErrorMessages()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupCoverImagePicker() {
        binding.cvCoverImage.setOnClickListener {
            openPhotoPicker()
        }
    }

    private fun setupSubCategoryDropdown() {
        // Initialize with empty adapter
        subCategoryAdapter = SubCategoryAdapter(this, emptyList())
        binding.etSubCategory.setAdapter(subCategoryAdapter)
        
        // Handle item selection
        binding.etSubCategory.setOnItemClickListener { _, _, position, _ ->
            val selectedSubCat = subCategoryAdapter.getItem(position)
            selectedSubCategory = selectedSubCat
            selectedSubCat?.let {
                // Set the display text to subcategory name
                binding.etSubCategory.setText(it.name, false)
                
                // Clear any previous error
                binding.tilSubCategory.error = null
                
                // Update ViewModel
                viewModel.selectSubCategory(it)
                println("📂 User selected subcategory: ${it.name} (ID: ${it.id})")
            }
        }
        
        // Handle dropdown arrow click
        binding.tilSubCategory.setEndIconOnClickListener {
            binding.etSubCategory.showDropDown()
        }
    }

    private fun setupNextButton() {
        binding.btnNext.setOnClickListener {
            createProduct()
        }
    }

    private fun openPhotoPicker() {
        photoPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun handleSelectedCoverImage(uri: Uri?) {
        if (uri != null) {
            selectedCoverUri = uri
            
            // Hiển thị ảnh preview
            binding.ivCoverPreview.loadUrl(
                imageUrl = uri.toString(),
                placeholder = R.drawable.pic_item_product,
                error = R.drawable.pic_item_product,
                transform = ImageTransform.ROUNDED
            )
            
            binding.ivCoverPreview.visibility = View.VISIBLE
            binding.llAddCover.visibility = View.GONE
            
            println("📸 Cover image selected: $uri")
            Toast.makeText(this, "✅ Đã chọn ảnh cover", Toast.LENGTH_SHORT).show()
        } else {
            println("❌ No cover image selected")
            Toast.makeText(this, "❌ Không chọn ảnh nào", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createProduct() {
        val name = binding.etName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val priceText = binding.etPrice.text.toString().trim()

        // Validation
        when {
            name.isEmpty() -> {
                binding.etName.error = "Vui lòng nhập tên sản phẩm"
                return
            }
            description.isEmpty() -> {
                binding.etDescription.error = "Vui lòng nhập mô tả sản phẩm"
                return
            }
            priceText.isEmpty() -> {
                binding.etPrice.error = "Vui lòng nhập giá sản phẩm"
                return
            }
            selectedSubCategory == null -> {
                binding.tilSubCategory.error = "Vui lòng chọn danh mục con"
                return
            }
            selectedCoverUri == null -> {
                Toast.makeText(this, "Vui lòng chọn ảnh cover cho sản phẩm", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Clear error if validation passes
        binding.tilSubCategory.error = null

        // Parse numbers
        val price = try {
            priceText.toDouble()
        } catch (e: NumberFormatException) {
            binding.etPrice.error = "Giá phải là số hợp lệ"
            return
        }

        val subCategoryId = selectedSubCategory!!.id

        // Create product
        viewModel.createProduct(
            context = this,
            name = name,
            description = description,
            price = price,
            subCategoryId = subCategoryId,
            coverUri = selectedCoverUri!!
        )
        
        println("🏭 Creating product: $name, price: $price, subCategoryId: $subCategoryId, subCategoryName: ${selectedSubCategory!!.name}")
    }

    private fun observeAddProductState() {
        lifecycleScope.launch {
            viewModel.addProductState.collect { state ->
                when (state) {
                    is AddProductUiState.Idle -> {
                        binding.btnNext.isEnabled = true
                        binding.btnNext.text = "Tiếp"
                    }
                    is AddProductUiState.Loading -> {
                        binding.btnNext.isEnabled = false
                        binding.btnNext.text = "Đang tạo..."
                        println("⏳ Creating product...")
                    }
                    is AddProductUiState.Success -> {
                        binding.btnNext.isEnabled = true
                        binding.btnNext.text = "Tiếp"
                        Toast.makeText(this@AddProductActivity, "✅ Tạo sản phẩm thành công!", Toast.LENGTH_SHORT).show()
                        
                        // Navigate to variant fragment
                        navigateToVariantFragment(state.productResponse.product_id)
                        
                        println("✅ Product created: ${state.productResponse.name} (ID: ${state.productResponse.product_id})")
                    }
                    is AddProductUiState.Error -> {
                        binding.btnNext.isEnabled = true
                        binding.btnNext.text = "Tiếp"
                        Toast.makeText(this@AddProductActivity, "❌ ${state.message}", Toast.LENGTH_LONG).show()
                        println("❌ Create product error: ${state.message}")
                    }
                }
            }
        }
    }

    private fun observeSubCategories() {
        lifecycleScope.launch {
            viewModel.subCategories.collect { subCategories ->
                // Update adapter with new data
                subCategoryAdapter = SubCategoryAdapter(this@AddProductActivity, subCategories)
                binding.etSubCategory.setAdapter(subCategoryAdapter)
                
                println("📂 Loaded ${subCategories.size} subcategories for dropdown")
            }
        }
        
        lifecycleScope.launch {
            viewModel.subCategoriesLoading.collect { isLoading ->
                binding.tilSubCategory.isEnabled = !isLoading
                if (isLoading) {
                    binding.etSubCategory.setText("Đang tải danh mục...")
                } else {
                    // Chỉ clear text nếu chưa có subcategory được chọn
                    if (selectedSubCategory == null && binding.etSubCategory.text.toString() == "Đang tải danh mục...") {
                        binding.etSubCategory.setText("")
                    }
                }
            }
        }
        
        // Observe selected subcategory from ViewModel
        lifecycleScope.launch {
            viewModel.selectedSubCategory.collect { subCategory ->
                if (subCategory != null && subCategory != selectedSubCategory) {
                    selectedSubCategory = subCategory
                    binding.etSubCategory.setText(subCategory.name, false)
                }
            }
        }
    }

    private fun observeErrorMessages() {
        lifecycleScope.launch {
            viewModel.errorMessage.collect { message ->
                if (!message.isNullOrEmpty()) {
                    Toast.makeText(this@AddProductActivity, message, Toast.LENGTH_LONG).show()
                    viewModel.clearErrorMessage()
                }
            }
        }
    }

    private fun navigateToVariantFragment(productId: Int) {
        val intent = ProductVariantActivity.createIntent(this, productId)
        startActivity(intent)
        // Không finish() để có thể back lại nếu cần
        
        println("🔄 Navigating to ProductVariantActivity with productId: $productId")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Reset ViewModel state when activity is destroyed
        viewModel.resetState()
        // Clear selected subcategory
        selectedSubCategory = null
    }
} 