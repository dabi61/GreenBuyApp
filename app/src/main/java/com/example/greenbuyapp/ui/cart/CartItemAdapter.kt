package com.example.greenbuyapp.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.cart.model.CartItem
import com.example.greenbuyapp.databinding.ItemCartProductBinding
import com.example.greenbuyapp.util.loadUrl

class CartItemAdapter(
    private val onUpdateQuantity: (Int, Int) -> Unit, // attributeId, quantity
    private val onDeleteItem: (Int) -> Unit, // attributeId
    private val selectedIds: MutableSet<Int>,
    private val onItemCheckedChanged: (attributeId: Int, checked: Boolean) -> Unit
) : ListAdapter<CartItem, CartItemAdapter.CartItemViewHolder>(CartItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val binding = ItemCartProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartItemViewHolder(
        private val binding: ItemCartProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            binding.apply {
                // Product info
                tvProductName.text = cartItem.productName
                tvAttributeDetails.text = cartItem.getAttributeDetails()
                tvUnitPrice.text = cartItem.getFormattedUnitPrice()
                tvQuantity.text = cartItem.quantity.toString()
                tvTotalPrice.text = cartItem.getFormattedTotalPrice()
                tvAvailableQuantity.text = "Còn lại: ${cartItem.availableQuantity} sản phẩm"

                // Product image
                cartItem.getImageUrl()?.let { imageUrl ->
                    ivProductImage.loadUrl(
                        imageUrl = imageUrl,
                        placeholder = R.drawable.pic_item_product,
                        error = R.drawable.pic_item_product
                    )
                } ?: run {
                    ivProductImage.setImageResource(R.drawable.pic_item_product)
                }

                // Checkbox state
                cbItem.isChecked = selectedIds.contains(cartItem.attributeId)
                cbItem.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedIds.add(cartItem.attributeId)
                    } else {
                        selectedIds.remove(cartItem.attributeId)
                    }
                    onItemCheckedChanged(cartItem.attributeId, isChecked)
                }

                // Quantity controls
                btnDecrease.apply {
                    isEnabled = cartItem.canDecreaseQuantity()
                    alpha = if (isEnabled) 1.0f else 0.5f
                    setOnClickListener {
                        if (cartItem.canDecreaseQuantity()) {
                            onUpdateQuantity(cartItem.attributeId, cartItem.quantity - 1)
                        }
                    }
                }

                btnIncrease.apply {
                    isEnabled = cartItem.canIncreaseQuantity()
                    alpha = if (isEnabled) 1.0f else 0.5f
                    setOnClickListener {
                        if (cartItem.canIncreaseQuantity()) {
                            onUpdateQuantity(cartItem.attributeId, cartItem.quantity + 1)
                        }
                    }
                }

                // Delete button
                btnDelete.setOnClickListener {
                    onDeleteItem(cartItem.attributeId)
                }

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