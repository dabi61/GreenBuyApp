package com.example.greenbuyapp.ui.shop.productManagement

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenbuyapp.databinding.ActivityEditAttributeProductBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.R
import com.example.greenbuyapp.ui.shop.productManagement.ProductManagementViewModel

class EditAttributeProductActivity : BaseActivity<ActivityEditAttributeProductBinding>() {

    override val viewModel: ProductManagementViewModel? = null // Chưa cần ViewModel riêng ở demo

    override val binding: ActivityEditAttributeProductBinding by lazy {
        ActivityEditAttributeProductBinding.inflate(layoutInflater)
    }

    companion object {
        private const val EXTRA_PRODUCT_ID = "extra_product_id"
        fun createIntent(context: Context, productId: Int): Intent {
            return Intent(context, EditAttributeProductActivity::class.java).apply {
                putExtra(EXTRA_PRODUCT_ID, productId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        setupToolbar()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        val adapter = AttributeAdapter(
            onPickImage = { position -> openImagePicker(position) }
        )
        binding.rvAttributes.layoutManager = LinearLayoutManager(this)
        binding.rvAttributes.adapter = adapter
        // TODO load actual attributes via API
    }

    private var pendingImagePosition: Int = -1
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null && pendingImagePosition >= 0) {
            // TODO update adapter item with new image URI
        }
    }

    private fun openImagePicker(position: Int) {
        pendingImagePosition = position
        imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}

// Adapter + ViewHolder đơn giản cho demo
class AttributeAdapter(
    private val onPickImage: (Int) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<AttributeViewHolder>() {

    private val items = mutableListOf<AttributeItem>()

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): AttributeViewHolder {
        val inflater = android.view.LayoutInflater.from(parent.context)
        val binding = com.example.greenbuyapp.databinding.ItemAttributeProductBinding.inflate(inflater, parent, false)
        return AttributeViewHolder(binding, onPickImage)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: AttributeViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun submitList(newList: List<AttributeItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}

data class AttributeItem(
    var color: String = "",
    var size: String = "",
    var price: Double = 0.0,
    var quantity: Int = 0,
    var imageUri: android.net.Uri? = null
)

class AttributeViewHolder(
    private val binding: com.example.greenbuyapp.databinding.ItemAttributeProductBinding,
    private val onPickImage: (Int) -> Unit
) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

    fun bind(item: AttributeItem) {
        binding.etColor.setText(item.color)
        binding.etSize.setText(item.size)
        binding.etPrice.setText(item.price.toString())
        binding.etQuantity.setText(item.quantity.toString())
        if (item.imageUri != null) {
            binding.ivImage.setImageURI(item.imageUri)
        } else {
            binding.ivImage.setImageResource(R.drawable.pic_item_product)
        }
        binding.ivImage.setOnClickListener { onPickImage(adapterPosition) }
    }
} 