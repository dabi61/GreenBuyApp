package com.example.greenbuyapp.ui.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.data.cart.model.CartItem
import com.example.greenbuyapp.databinding.ItemOrderSummaryBinding

class OrderItemSummaryAdapter : ListAdapter<CartItem, OrderItemSummaryAdapter.SummaryViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        val binding = ItemOrderSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SummaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SummaryViewHolder(private val binding: ItemOrderSummaryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CartItem) {
            binding.tvProductName.text = item.productName
            binding.tvQuantity.text = "x${item.quantity}"
        }
    }

    class Diff : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean = oldItem.attributeId == newItem.attributeId
        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean = oldItem == newItem
    }
} 