package com.example.greenbuyapp.ui.product

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.product.model.ProductAttribute
import com.example.greenbuyapp.databinding.ItemProductIndicatorBinding

class ProductIndicatorAdapter(
    private val onItemClick: (Int) -> Unit
) : ListAdapter<ProductAttribute, ProductIndicatorAdapter.IndicatorViewHolder>(ProductImageDiffCallback()) {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IndicatorViewHolder {
        val binding = ItemProductIndicatorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IndicatorViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: IndicatorViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedPosition)
    }

    fun updateSelectedPosition(position: Int) {
        val oldPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(oldPosition)
        notifyItemChanged(selectedPosition)
    }

    class IndicatorViewHolder(
        private val binding: ItemProductIndicatorBinding,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onItemClick(adapterPosition)
            }
        }

        fun bind(attribute: ProductAttribute, isSelected: Boolean) {
            binding.apply {
                // Load thumbnail image
                Glide.with(root.context)
                    .load(attribute.getImageUrl())
                    .placeholder(R.drawable.pic_item_product)
                    .error(R.drawable.pic_item_product)
                    .centerCrop()
                    .into(ivThumbnail)

                // Update border based on selection
                if (isSelected) {
                    cardThumbnail.strokeColor = ContextCompat.getColor(root.context, R.color.main_color)
                    cardThumbnail.strokeWidth = 6
                } else {
                    cardThumbnail.strokeColor = ContextCompat.getColor(root.context, R.color.grey_300)
                    cardThumbnail.strokeWidth = 2
                }

                // Set content description
                ivThumbnail.contentDescription = "Thumbnail ${attribute.color} ${attribute.size}"
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