package com.example.greenbuyapp.ui.shop.productManagement

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.product.model.ProductAttribute
import com.example.greenbuyapp.databinding.ItemEditAttributeBinding
import com.example.greenbuyapp.util.ImageTransform
import com.example.greenbuyapp.util.safeLoadImage

class EditAttributeAdapter(
    private val onPickImage: (Int) -> Unit,
    private val onDeleteAttribute: (Int) -> Unit,
    private val onSaveAttribute: (ProductAttribute, Int) -> Unit
) : ListAdapter<ProductAttribute, EditAttributeAdapter.EditAttributeViewHolder>(EditAttributeDiffCallback()) {

    // Track new images selected by user
    private val newImageUris = mutableMapOf<Int, String>()
    // Track if user has selected new image for each position
    private val hasSelectedNewImage = mutableMapOf<Int, Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditAttributeViewHolder {
        val binding = ItemEditAttributeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EditAttributeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EditAttributeViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    fun addEmptyAttribute() {
        val currentList = currentList.toMutableList()
        val emptyAttribute = ProductAttribute(
            attribute_id = -1, // Temporary ID for new attribute
            product_id = 0,
            color = "",
            size = "",
            price = 0.0,
            image = "",
            quantity = 0,
            create_at = ""
        )
        currentList.add(emptyAttribute)
        submitList(currentList)
    }

    fun removeAttribute(position: Int) {
        val currentList = currentList.toMutableList()
        if (position >= 0 && position < currentList.size) {
            currentList.removeAt(position)
            submitList(currentList)
        }
        // Remove new image URI if exists
        newImageUris.remove(position)
        hasSelectedNewImage.remove(position)
    }

    fun updateAttributeImage(position: Int, imagePath: String) {
        val currentList = currentList.toMutableList()
        if (position >= 0 && position < currentList.size) {
            val updatedAttribute = currentList[position].copy(image = imagePath)
            currentList[position] = updatedAttribute
            submitList(currentList)
            
            // Track this as a new image
            newImageUris[position] = imagePath
            hasSelectedNewImage[position] = true
        }
    }

    fun getNewImageUri(position: Int): String? {
        return newImageUris[position]
    }

    fun hasNewImage(position: Int): Boolean {
        return hasSelectedNewImage[position] == true
    }

    inner class EditAttributeViewHolder(
        private val binding: ItemEditAttributeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(attribute: ProductAttribute, position: Int) {
            binding.apply {
                // Set current values
                etColor.setText(attribute.color)
                etSize.setText(attribute.size)
                etPrice.setText(attribute.price.toString())
                etQuantity.setText(attribute.quantity.toString())
                
                // Load image if exists
                val imageUrl = if (attribute.image.isNotEmpty()) {
                    if (attribute.image.startsWith("http")) {
                        attribute.image
                    } else if (attribute.image.startsWith("content://")) {
                        // Handle content URI from image picker
                        attribute.image
                    } else {
                        "https://www.utt-school.site${attribute.image}"
                    }
                } else null
                
                ivAttributeImage.safeLoadImage(
                    imageUrl = imageUrl,
                    placeholder = R.drawable.pic_item_product,
                    error = R.drawable.pic_item_product,
                    transform = ImageTransform.ROUNDED
                )
                
                // Click listeners
                ivAttributeImage.setOnClickListener {
                    onPickImage(position)
                }
                
                btnDelete.setOnClickListener {
                    onDeleteAttribute(position)
                }

                // Save button click listener
                btnSave.setOnClickListener {
                    val updatedAttribute = attribute.copy(
                        color = etColor.text.toString(),
                        size = etSize.text.toString(),
                        price = etPrice.text.toString().toDoubleOrNull() ?: 0.0,
                        quantity = etQuantity.text.toString().toIntOrNull() ?: 0
                    )
                    onSaveAttribute(updatedAttribute, position)
                }
            }
        }
    }

    class EditAttributeDiffCallback : DiffUtil.ItemCallback<ProductAttribute>() {
        override fun areItemsTheSame(oldItem: ProductAttribute, newItem: ProductAttribute): Boolean {
            return oldItem.attribute_id == newItem.attribute_id
        }

        override fun areContentsTheSame(oldItem: ProductAttribute, newItem: ProductAttribute): Boolean {
            return oldItem == newItem
        }
    }
} 