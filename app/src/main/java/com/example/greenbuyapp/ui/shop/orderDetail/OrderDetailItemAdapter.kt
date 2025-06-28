package com.example.greenbuyapp.ui.shop.orderDetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.shop.model.OrderItem
import com.example.greenbuyapp.databinding.ItemOrderDetailProductBinding
import com.example.greenbuyapp.util.loadUrl

class OrderDetailItemAdapter : ListAdapter<OrderItem, OrderDetailItemAdapter.OrderItemViewHolder>(OrderItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val binding = ItemOrderDetailProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OrderItemViewHolder(
        private val binding: ItemOrderDetailProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OrderItem) {
            binding.apply {
                // Product info
                tvProductName.text = item.productName
                tvAttributeDetails.text = item.attributeDetails
                tvQuantity.text = "x${item.quantity}"
                
                // Prices
                tvUnitPrice.text = item.getFormattedUnitPrice()
                tvTotalPrice.text = item.getFormattedTotalPrice()
                
                // Product image
                if (!item.productImage.isNullOrEmpty()) {
                    ivProductImage.loadUrl(
                        imageUrl = item.productImage,
                        placeholder = R.drawable.pic_item_product,
                        error = R.drawable.pic_item_product
                    )
                } else {
                    ivProductImage.setImageResource(R.drawable.pic_item_product)
                }
                
                println("📦 Order item bound: ${item.productName} x${item.quantity} = ${item.getFormattedTotalPrice()}")
            }
        }
    }

    class OrderItemDiffCallback : DiffUtil.ItemCallback<OrderItem>() {
        override fun areItemsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean {
            return oldItem == newItem
        }
    }
} 