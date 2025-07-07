package com.example.greenbuyapp.ui.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.cart.model.CartItem
import com.example.greenbuyapp.databinding.ItemOrderProductBinding
import com.example.greenbuyapp.util.loadUrl

class OrderItemAdapter(
) : ListAdapter<CartItem, OrderItemAdapter.OrderItemViewHolder>(CartItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val binding = ItemOrderProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderItemViewHolder(
        private val binding: ItemOrderProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            binding.apply {
                // Product info
                tvProductName.text = cartItem.productName
                tvAttributeDetails.text = cartItem.getAttributeDetails()
                tvUnitPrice.text = cartItem.getFormattedUnitPrice()
                tvQuantity.text = "Số lượng: " + cartItem.quantity.toString()

                // Product image
                ivProductImage.setImageResource(R.drawable.pic_item_product)

                println("🛒 Cart item bound: ${cartItem.productName} x${cartItem.quantity}")
            }
        }
    }

    class CartItemDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.attributeId == newItem.attributeId
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
} 