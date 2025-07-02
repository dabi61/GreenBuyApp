package com.example.greenbuyapp.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.data.cart.model.CartShop
import com.example.greenbuyapp.databinding.ItemCartShopBinding

class CartShopAdapter(
    private val onDeleteShop: (CartShop) -> Unit,
    private val onUpdateQuantity: (Int, Int) -> Unit, // attributeId, quantity
    private val onDeleteItem: (Int) -> Unit, // attributeId
    private val selectedIds: MutableSet<Int>,
    private val onShopCheckedChanged: (shop: CartShop, checked: Boolean) -> Unit,
    private val onItemCheckedChanged: (attributeId: Int, checked: Boolean) -> Unit
) : ListAdapter<CartShop, CartShopAdapter.CartShopViewHolder>(CartShopDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartShopViewHolder {
        val binding = ItemCartShopBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartShopViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartShopViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartShopViewHolder(
        private val binding: ItemCartShopBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var itemAdapter: CartItemAdapter

        fun bind(cartShop: CartShop) {
            binding.apply {
                // Shop info
                tvShopName.text = cartShop.shopName
                tvShopTotal.text = cartShop.getFormattedTotalAmount()

                // Delete shop button
                btnDeleteShop.setOnClickListener {
                    onDeleteShop(cartShop)
                }

                // Checkbox state
                cbShop.setOnCheckedChangeListener(null)
                val allSelected = cartShop.items.all { selectedIds.contains(it.attributeId) }
                cbShop.isChecked = allSelected
                cbShop.setOnCheckedChangeListener { _, isChecked ->
                    // Update local selectedIds set
                    if (isChecked) {
                        cartShop.items.forEach { selectedIds.add(it.attributeId) }
                    } else {
                        cartShop.items.forEach { selectedIds.remove(it.attributeId) }
                    }

                    onShopCheckedChanged(cartShop, isChecked)
                    // Refresh child items to reflect checkbox state change
                    itemAdapter.notifyDataSetChanged()
                }

                // Setup items RecyclerView
                setupItemsRecyclerView(cartShop)
            }
        }

        private fun setupItemsRecyclerView(cartShop: CartShop) {
            itemAdapter = CartItemAdapter(
                onUpdateQuantity = onUpdateQuantity,
                onDeleteItem = onDeleteItem,
                selectedIds = selectedIds,
                onItemCheckedChanged = onItemCheckedChanged
            )

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