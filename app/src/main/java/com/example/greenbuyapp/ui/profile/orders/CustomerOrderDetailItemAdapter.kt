package com.example.greenbuyapp.ui.profile.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.user.model.CustomerOrderItem
import com.example.greenbuyapp.databinding.ItemOrderDetailProductBinding
import com.example.greenbuyapp.util.loadUrl

class CustomerOrderDetailItemAdapter : ListAdapter<CustomerOrderItem, CustomerOrderDetailItemAdapter.CustomerOrderItemViewHolder>(CustomerOrderItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerOrderItemViewHolder {
        val binding = ItemOrderDetailProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CustomerOrderItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerOrderItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CustomerOrderItemViewHolder(
        private val binding: ItemOrderDetailProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CustomerOrderItem) {
            binding.apply {
                // Product info
                tvProductName.text = item.productName
                tvAttributeDetails.text = item.attributeDetails ?: "Không có thông tin biến thể"
                tvQuantity.text = "x${item.quantity}"
                
                // Prices
                tvUnitPrice.text = item.getFormattedUnitPrice()
                tvTotalPrice.text = item.getFormattedTotalPrice()
                
                // Product image
                if (!item.productImage.isNullOrEmpty()) {
                    val imageUrl = if (item.productImage.startsWith("http")) {
                        item.productImage
                    } else {
                        "https://www.utt-school.site${item.productImage}"
                    }
                    
                    ivProductImage.loadUrl(
                        imageUrl = imageUrl,
                        placeholder = R.drawable.pic_item_product,
                        error = R.drawable.pic_item_product
                    )
                } else {
                    ivProductImage.setImageResource(R.drawable.pic_item_product)
                }
                
                println("📦 Customer order item bound: ${item.productName} x${item.quantity} = ${item.getFormattedTotalPrice()}")
            }
        }
    }

    class CustomerOrderItemDiffCallback : DiffUtil.ItemCallback<CustomerOrderItem>() {
        override fun areItemsTheSame(oldItem: CustomerOrderItem, newItem: CustomerOrderItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CustomerOrderItem, newItem: CustomerOrderItem): Boolean {
            return oldItem == newItem
        }
    }
} 