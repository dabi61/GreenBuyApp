package com.example.greenbuyapp.ui.shop.addProduct

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.product.model.ProductVariant
import com.example.greenbuyapp.databinding.ItemProductVariantBinding
import com.example.greenbuyapp.util.ImageTransform
import com.example.greenbuyapp.util.loadUrl

class ProductVariantAdapter(
    private val onVariantChanged: (Int, ProductVariant) -> Unit,
    private val onDeleteVariant: (Int) -> Unit,
    private val onImagePicker: (Int) -> Unit,
    private val onForceRefresh: (() -> Unit)? = null,
    private val getCurrentVariant: (Int) -> ProductVariant? = { null },
    private val onForceItemUpdate: ((Int) -> Unit)? = null
) : ListAdapter<ProductVariant, ProductVariantAdapter.VariantViewHolder>(VariantDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VariantViewHolder {
        val binding = ItemProductVariantBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VariantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VariantViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    override fun onBindViewHolder(
        holder: VariantViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty() && payloads.contains("image_update")) {
            holder.bindImage(getItem(position))
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    inner class VariantViewHolder(
        private val binding: ItemProductVariantBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var isUpdating = false

        fun bind(variant: ProductVariant, position: Int) {
            println("🔗 Binding variant at position $position - isUpdating: $isUpdating")
            println("🔗 Variant data: color='${variant.color}', size='${variant.size}', price='${variant.price}', quantity='${variant.quantity}', imageUri='${variant.imageUri}'")
            
            isUpdating = true
            
            binding.apply {
                // Set title
                tvVariantTitle.text = "Loại sản phẩm #${position + 1}"
                
                // Show/hide delete button (không thể xóa nếu chỉ có 1 variant)
                btnDelete.visibility = if (itemCount > 1) View.VISIBLE else View.GONE
                
                // Set initial values without triggering listeners
                etColor.setText(variant.color)
                etSize.setText(variant.size)
                etVariantPrice.setText(variant.price)
                etQuantity.setText(variant.quantity)
                
                // Load image if available
                bindImage(variant)
                
                // Setup listeners after setting values
                setupTextChangeListeners(variant)
                setupClickListeners()
            }
            
            isUpdating = false
            println("🔗 Binding completed - isUpdating: $isUpdating")
        }

        fun bindImage(variant: ProductVariant) {
            binding.apply {
                println("🖼️ bindImage called for variant: imageUri='${variant.imageUri}'")
                
                // Load image if available
                if (variant.imageUri != null && variant.imageUri != "null") {
                    println("🖼️ Loading image: ${variant.imageUri}")
                    ivVariantPreview.loadUrl(
                        imageUrl = variant.imageUri,
                        placeholder = R.drawable.pic_item_product,
                        error = R.drawable.pic_item_product,
                        transform = ImageTransform.ROUNDED
                    )
                    ivVariantPreview.visibility = View.VISIBLE
                    llAddVariantImage.visibility = View.GONE
                    println("🖼️ Image UI updated: preview visible, add button hidden")
                } else {
                    println("🖼️ No image, showing add button")
                    ivVariantPreview.visibility = View.GONE
                    llAddVariantImage.visibility = View.VISIBLE
                }
            }
        }

        private fun clearTextChangeListeners() {
            // Skip clearing since it's complex and not working properly
            // Instead rely on isUpdating flag
        }
        
        private fun setupTextChangeListeners(variant: ProductVariant) {
            binding.apply {
                etColor.doOnTextChanged { text, _, _, _ ->
                    if (isUpdating) {
                        println("⏸️ Skipping color update - isUpdating: $isUpdating")
                        return@doOnTextChanged
                    }
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        // Get latest variant from ViewModel to avoid stale data
                        val currentVariant = getCurrentVariant(position) ?: getItem(position)
                        val updatedVariant = currentVariant.copy(color = text.toString())
                        onVariantChanged(position, updatedVariant)
                        println("🎨 Color updated at position $position: '${text.toString()}' (based on current: color='${currentVariant.color}', size='${currentVariant.size}')")
                    } else {
                        println("❌ Invalid position for color update: $position")
                    }
                }
                
                etSize.doOnTextChanged { text, _, _, _ ->
                    if (isUpdating) {
                        println("⏸️ Skipping size update - isUpdating: $isUpdating")
                        return@doOnTextChanged
                    }
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val currentVariant = getCurrentVariant(position) ?: getItem(position)
                        val updatedVariant = currentVariant.copy(size = text.toString())
                        onVariantChanged(position, updatedVariant)
                        println("📏 Size updated at position $position: '${text.toString()}' (based on current: color='${currentVariant.color}', size='${currentVariant.size}')")
                    } else {
                        println("❌ Invalid position for size update: $position")
                    }
                }
                
                etVariantPrice.doOnTextChanged { text, _, _, _ ->
                    if (isUpdating) {
                        println("⏸️ Skipping price update - isUpdating: $isUpdating")
                        return@doOnTextChanged
                    }
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val currentVariant = getCurrentVariant(position) ?: getItem(position)
                        val updatedVariant = currentVariant.copy(price = text.toString())
                        onVariantChanged(position, updatedVariant)
                        println("💰 Price updated at position $position: '${text.toString()}' (based on current: color='${currentVariant.color}', size='${currentVariant.size}')")
                    } else {
                        println("❌ Invalid position for price update: $position")
                    }
                }
                
                etQuantity.doOnTextChanged { text, _, _, _ ->
                    if (isUpdating) {
                        println("⏸️ Skipping quantity update - isUpdating: $isUpdating")
                        return@doOnTextChanged
                    }
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val currentVariant = getCurrentVariant(position) ?: getItem(position)
                        val updatedVariant = currentVariant.copy(quantity = text.toString())
                        onVariantChanged(position, updatedVariant)
                        println("🔢 Quantity updated at position $position: '${text.toString()}' (based on current: color='${currentVariant.color}', size='${currentVariant.size}')")
                    } else {
                        println("❌ Invalid position for quantity update: $position")
                    }
                }
            }
        }
        
        private fun setupClickListeners() {
            binding.apply {
                // Delete button
                btnDelete.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onDeleteVariant(position)
                    }
                }
                
                // Image picker
                cvVariantImage.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onImagePicker(position)
                    }
                }
            }
        }
    }

    /**
     * Update image for specific variant
     */
    fun updateVariantImage(position: Int, imageUri: Uri) {
        if (position >= 0 && position < itemCount) {
            // Get latest variant from ViewModel to avoid stale data
            val currentVariant = getCurrentVariant(position) ?: getItem(position)
            val updatedVariant = currentVariant.copy(imageUri = imageUri.toString())
            
            // Update ViewModel data
            onVariantChanged(position, updatedVariant)
            
            // Force update UI through Fragment callback
            onForceItemUpdate?.invoke(position)
            
            println("📸 Updated variant image at position $position: ${imageUri} (based on current: color='${currentVariant.color}', size='${currentVariant.size}')")
        }
    }

    class VariantDiffCallback : DiffUtil.ItemCallback<ProductVariant>() {
        override fun areItemsTheSame(oldItem: ProductVariant, newItem: ProductVariant): Boolean {
            // Sử dụng unique ID thay vì hash code
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProductVariant, newItem: ProductVariant): Boolean {
            return oldItem.color == newItem.color &&
                    oldItem.size == newItem.size &&
                    oldItem.price == newItem.price &&
                    oldItem.quantity == newItem.quantity &&
                    oldItem.imageUri == newItem.imageUri
        }
    }
} 