package com.example.greenbuyapp.ui.product

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.product.model.ProductAttribute
import com.example.greenbuyapp.databinding.ItemProductImageBinding

class ProductImageAdapter : ListAdapter<ProductAttribute, ProductImageAdapter.ProductImageViewHolder>(ProductImageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductImageViewHolder {
        val binding = ItemProductImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProductImageViewHolder(
        private val binding: ItemProductImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(attribute: ProductAttribute) {
            binding.apply {
                // Load product image
                Glide.with(root.context)
                    .load(attribute.getImageUrl())
                    .placeholder(R.drawable.pic_item_product)
                    .error(R.drawable.pic_item_product)
                    .centerCrop()
                    .into(ivProductImage)

                // Set content description for accessibility
                ivProductImage.contentDescription = "Hình ảnh sản phẩm ${attribute.color} ${attribute.size}"
            }
        }
    }

    private class ProductImageDiffCallback : DiffUtil.ItemCallback<ProductAttribute>() {
        override fun areItemsTheSame(oldItem: ProductAttribute, newItem: ProductAttribute): Boolean {
            return oldItem.attribute_id == newItem.attribute_id
        }

        override fun areContentsTheSame(oldItem: ProductAttribute, newItem: ProductAttribute): Boolean {
            return oldItem == newItem
        }
    }
} 