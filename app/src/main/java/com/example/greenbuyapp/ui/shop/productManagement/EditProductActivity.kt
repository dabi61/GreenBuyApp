package com.example.greenbuyapp.ui.shop.productManagement

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.example.greenbuyapp.util.loadUrl


class EditProductActivity : BaseActivity<ActivityEditProductBinding>() {


    override val viewModel: ProductManagementViewModel by viewModel()


    override val binding: ActivityEditProductBinding by lazy {
        ActivityEditProductBinding.inflate(layoutInflater)
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
        super.onCreate(savedInstanceState)

        val product = intent.getParcelableExtra<Product>(EXTRA_PRODUCT)
        if (product == null) {
            finish()
            return
        }

        setupToolbar()
        populateProduct(product)
        setupListeners(product)
    }

    /** Thiết lập toolbar */
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    /** Đổ dữ liệu sản phẩm lên UI */
    private fun populateProduct(product: Product) {
        binding.etName.setText(product.name)
        binding.etDescription.setText(product.description)
        binding.etPrice.setText(product.price.toString())
        // Load cover (dùng extension loadUrl nếu có)
        val imageUrl = if (!product.cover.isNullOrEmpty()) {
            if (product.cover.startsWith("http")) product.cover else "https://www.utt-school.site${product.cover}"
        } else null
        binding.ivCover.loadUrl(
            imageUrl = imageUrl,
            placeholder = R.drawable.pic_item_product,
            error = R.drawable.pic_item_product
        )
    }

    /** Khởi tạo listener */
    private fun setupListeners(product: Product) {
        // PhotoPicker launcher
        val photoPickerLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                binding.ivCover.setImageURI(uri)
                selectedCoverUri = uri
            }
        }

        binding.btnPickImage.setOnClickListener {
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnEditAttributes.setOnClickListener {
            val intent = EditAttributeProductActivity.createIntent(this, product.product_id)
            startActivity(intent)
        }

        binding.btnSave.setOnClickListener {
            saveProduct(product)
        }
    }

    private var selectedCoverUri: Uri? = null

    /** Gọi API lưu sản phẩm */
    private fun saveProduct(original: Product) {
        val name = binding.etName.text?.toString()?.trim() ?: ""
        val description = binding.etDescription.text?.toString()?.trim() ?: ""
        val priceText = binding.etPrice.text?.toString()?.trim() ?: "0"
        val price = priceText.toDoubleOrNull() ?: 0.0

        // TODO: Call ProductRepository.updateProduct(...)
        // Có thể upload ảnh nếu selectedCoverUri != null

        Toast.makeText(this, "Đã lưu thay đổi (demo)", Toast.LENGTH_SHORT).show()
        finish()
    }
}