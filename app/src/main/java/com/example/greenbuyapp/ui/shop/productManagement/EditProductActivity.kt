package com.example.greenbuyapp.ui.shop.productManagement

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.product.model.Product
import com.example.greenbuyapp.databinding.ActivityEditProductBinding
import com.example.greenbuyapp.databinding.ActivityProductManagementBinding
import com.example.greenbuyapp.databinding.ActivityProductVariantBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.activity.result.PickVisualMediaRequest
import androidx.core.net.toUri
import com.example.greenbuyapp.data.category.model.SubCategory
import com.example.greenbuyapp.ui.shop.addProduct.SubCategoryAdapter
import com.example.greenbuyapp.util.ImageTransform
import com.example.greenbuyapp.util.loadUrl
import com.example.greenbuyapp.domain.category.CategoryRepository
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import androidx.lifecycle.lifecycleScope
import com.example.greenbuyapp.ui.shop.addProduct.AddProductViewModel
import com.example.greenbuyapp.util.Result
import com.example.greenbuyapp.ui.shop.addProduct.AddProductUiState


class EditProductActivity : BaseActivity<ActivityEditProductBinding>() {


    override val viewModel: AddProductViewModel by viewModel()


    override val binding: ActivityEditProductBinding by lazy {
        ActivityEditProductBinding.inflate(layoutInflater)
    }

    private var selectedCoverUri: Uri? = null
    private var selectedSubCategory: SubCategory? = null
    private lateinit var subCategoryAdapter: SubCategoryAdapter
    private lateinit var currentProduct: Product
    private var hasSelectedNewImage = false

    // Photo picker launcher
    private val photoPicker = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        handleSelectedCoverImage(uri)
    }

    private val categoryRepository: CategoryRepository by inject()

    private fun handleSelectedCoverImage(uri: Uri?) {
        if (uri != null) {
            selectedCoverUri = uri
            // ✅ Đánh dấu user đã chọn ảnh mới
            hasSelectedNewImage = true

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

    companion object {

        private const val EXTRA_PRODUCT = "extra_product"

        fun createIntent(context: Context, product: Product): Intent {
            return Intent(context, EditProductActivity::class.java).apply {
                putExtra(EXTRA_PRODUCT, product)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(binding.root)
        
        // ✅ Khởi tạo currentProduct TRƯỚC super.onCreate()
        val product = intent.getParcelableExtra<Product>(EXTRA_PRODUCT)
        if (product == null) {
            finish()
            return
        }
        currentProduct = product
        // ✅ KHÔNG set selectedCoverUri từ URL string nữa
        selectedCoverUri = null // Bắt đầu với null, chỉ set khi user chọn ảnh mới
        
        // ✅ Bây giờ mới gọi super.onCreate() - initViews() sẽ có currentProduct
        super.onCreate(savedInstanceState)
        
        // ✅ Logic riêng sau khi BaseActivity setup xong
        populateProduct(product)
    }


    /** Đổ dữ liệu sản phẩm lên UI */
    private fun populateProduct(product: Product) {
        // ✅ Hiển thị dữ liệu text
        binding.etName.setText(product.name)
        binding.etDescription.setText(product.description)
        binding.etPrice.setText(product.price.toString())
        
        // ✅ Hiển thị ảnh cover
        val imageUrl = if (!product.cover.isNullOrEmpty()) {
            if (product.cover.startsWith("http")) product.cover else "https://www.utt-school.site${product.cover}"
        } else null
        
        if (imageUrl != null) {
            binding.ivCoverPreview.loadUrl(
                imageUrl = imageUrl,
                placeholder = R.drawable.pic_item_product,
                error = R.drawable.pic_item_product,
                transform = ImageTransform.ROUNDED
            )
            // ✅ Hiển thị ảnh, ẩn placeholder
            binding.ivCoverPreview.visibility = View.VISIBLE
            binding.llAddCover.visibility = View.GONE
        } else {
            // ✅ Không có ảnh, hiển thị placeholder
            binding.ivCoverPreview.visibility = View.GONE
            binding.llAddCover.visibility = View.VISIBLE
        }
    }


    /** Thiết lập toolbar */
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun initViews() {
        setupToolbar()
        setupCoverImagePicker()
        setupSubCategoryDropdown()
        setupNextButton()
    }

    private fun setupCoverImagePicker() {
        binding.cvCoverImage.setOnClickListener {
            openPhotoPicker()
        }
    }

    private fun setupNextButton() {
        binding.btnNext.setOnClickListener {
            editProduct()
        }
    }

    private fun editProduct() {
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
            // ✅ Chỉ yêu cầu ảnh mới nếu sản phẩm chưa có ảnh cũ HOẶC user muốn thay đổi
            selectedCoverUri == null && currentProduct.cover.isNullOrEmpty() -> {
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

        // ✅ Phân biệt 2 trường hợp: có ảnh mới hoặc chỉ edit text
        if (hasSelectedNewImage && selectedCoverUri != null) {
            // User đã chọn ảnh mới - gọi editProduct với ảnh
            viewModel.editProduct(
                context = this,
                productId = currentProduct.product_id,
                name = name,
                description = description,
                price = price,
                subCategoryId = subCategoryId,
                coverUri = selectedCoverUri!!
            )
            println("🏭 Editing product with new image: $name, price: $price, subCategoryId: $subCategoryId")
        } else {
            // User không chọn ảnh mới - chỉ edit text fields
            viewModel.editProductWithoutNewImage(
                productId = currentProduct.product_id,
                name = name,
                description = description,
                price = price,
                subCategoryId = subCategoryId
            )
            println("🏭 Editing product without new image: $name, price: $price, subCategoryId: $subCategoryId")
        }
    }

    private fun openPhotoPicker() {
        photoPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun setupSubCategoryDropdown() {
        // Khởi tạo adapter rỗng ban đầu
        subCategoryAdapter = SubCategoryAdapter(this, emptyList())
        binding.etSubCategory.setAdapter(subCategoryAdapter)

        // Xử lý khi user chọn một subcategory khác
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

        // Mở dropdown khi bấm icon
        binding.tilSubCategory.setEndIconOnClickListener {
            binding.etSubCategory.showDropDown()
        }

        // Tải danh sách subcategory
        viewModel.loadSubCategories()
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.subCategories.collect { subs ->
                subCategoryAdapter = SubCategoryAdapter(this@EditProductActivity, subs)
                binding.etSubCategory.setAdapter(subCategoryAdapter)
                // chọn subcategory trùng product id
                val match = subs.firstOrNull { it.id == currentProduct.sub_category_id }
                if (match != null) {
                    selectedSubCategory = match
                    binding.etSubCategory.setText(match.name, false)
                }
            }
        }
        lifecycleScope.launch {
            viewModel.errorMessage.collect { msg ->
                if (!msg.isNullOrEmpty()) {
                    Toast.makeText(this@EditProductActivity, msg, Toast.LENGTH_LONG).show()
                    // clear after show
                    viewModel.clearErrorMessage()
                }
            }
        }
        
        // ✅ Observe edit product state
        lifecycleScope.launch {
            viewModel.addProductState.collect { state ->
                when (state) {
                    is AddProductUiState.Loading -> {
                        binding.btnNext.isEnabled = false
                        binding.btnNext.text = "Đang lưu..."
                    }
                    is AddProductUiState.Success -> {
                        binding.btnNext.isEnabled = true
                        binding.btnNext.text = "Tiếp"
                        Toast.makeText(this@EditProductActivity, "✅ Lưu sản phẩm thành công!", Toast.LENGTH_SHORT).show()
                        
                        // ✅ Chuyển sang EditProductVariantActivity
                        val intent = EditProductVariantActivity.createIntent(
                            context = this@EditProductActivity,
                            productId = currentProduct.product_id
                        )
                        startActivity(intent)
                        finish() // Đóng EditProductActivity
                    }
                    is AddProductUiState.Error -> {
                        binding.btnNext.isEnabled = true
                        binding.btnNext.text = "Tiếp"
                        Toast.makeText(this@EditProductActivity, "❌ ${state.message}", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        binding.btnNext.isEnabled = true
                        binding.btnNext.text = "Tiếp"
                    }
                }
            }
        }
    }



    /** Gọi API lưu sản phẩm */
//    private fun saveProduct(original: Product) {
//        val name = binding.etName.text?.toString()?.trim() ?: ""
//        val description = binding.etDescription.text?.toString()?.trim() ?: ""
//        val priceText = binding.etPrice.text?.toString()?.trim() ?: "0"
//        val price = priceText.toDoubleOrNull() ?: 0.0
//
//        // TODO: Call ProductRepository.updateProduct(...)
//        // Có thể upload ảnh nếu selectedCoverUri != null
//
//        Toast.makeText(this, "Đã lưu thay đổi (demo)", Toast.LENGTH_SHORT).show()
//        finish()
//    }
}