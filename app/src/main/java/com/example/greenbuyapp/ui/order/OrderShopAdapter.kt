package com.example.greenbuyapp.ui.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.data.cart.model.CartShop
import com.example.greenbuyapp.databinding.ItemOrderShopPaymentBinding

class OrderShopAdapter() : ListAdapter<CartShop, OrderShopAdapter.OrdershopViewHoler>(CartShopDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdershopViewHoler {
        val binding = ItemOrderShopPaymentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrdershopViewHoler(binding)
    }

    override fun onBindViewHolder(holder: OrdershopViewHoler, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrdershopViewHoler(
        private val binding: ItemOrderShopPaymentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var itemAdapter: OrderItemAdapter

        fun bind(cartShop: CartShop) {
            binding.apply {
                // Shop info
                tvShopName.text = cartShop.shopName
                tvShopTotal.text = cartShop.getFormattedTotalAmount()

                // Setup items RecyclerView
                setupItemsRecyclerView(cartShop)
            }
        }

        private fun setupItemsRecyclerView(cartShop: CartShop) {
            itemAdapter = OrderItemAdapter()

            binding.recyclerViewItems.apply {
                adapter = itemAdapter
                layoutManager = LinearLayoutManager(context)
                isNestedScrollingEnabled = false
            }

            itemAdapter.submitList(cartShop.items)
        }
    }

    class CartShopDiffCallback : DiffUtil.ItemCallback<CartShop>() {
        override fun areItemsTheSame(oldItem: CartShop, newItem: CartShop): Boolean {
            return oldItem.shopId == newItem.shopId
        }

        override fun areContentsTheSame(oldItem: CartShop, newItem: CartShop): Boolean {
            return oldItem == newItem
        }
    }
} 