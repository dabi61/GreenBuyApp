package com.example.greenbuyapp.ui.admin.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.shop.model.AdminOrderItem
import com.example.greenbuyapp.databinding.ItemAdminOrderItemBinding
import com.example.greenbuyapp.util.loadUrl

class AdminOrderItemAdapter : ListAdapter<AdminOrderItem, AdminOrderItemAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminOrderItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemAdminOrderItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AdminOrderItem) {
            with(binding) {
                // ✅ Product info
                tvProductName.text = item.productName
                tvAttributeDetails.text = item.attributeDetails
                tvQuantity.text = "x${item.quantity}"
                tvUnitPrice.text = item.formattedUnitPrice
                tvTotalPrice.text = item.formattedTotalPrice

                // ✅ Load product image using extension method like other adapters
                val imageUrl = item.getImageUrl()
                println("🖼️ AdminOrderItem Loading image for ${item.productName}: $imageUrl")
                
                if (!imageUrl.isNullOrEmpty()) {
                    ivProductImage.loadUrl(
                        imageUrl = imageUrl,
                        placeholder = R.drawable.pic_item_product,
                        error = R.drawable.pic_item_product
                    )
                    println("✅ AdminOrderItem Loading image: $imageUrl")
                } else {
                    println("❌ AdminOrderItem No image URL for ${item.productName}")
                    ivProductImage.setImageResource(R.drawable.pic_item_product)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<AdminOrderItem>() {
        override fun areItemsTheSame(oldItem: AdminOrderItem, newItem: AdminOrderItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AdminOrderItem, newItem: AdminOrderItem): Boolean {
            return oldItem == newItem
        }
    }
} 