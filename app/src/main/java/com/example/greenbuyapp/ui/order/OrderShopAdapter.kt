package com.example.greenbuyapp.ui.order

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.cart.model.CartShop
import com.example.greenbuyapp.databinding.ItemOrderShopPaymentBinding

class OrderShopAdapter(
    private var isFastShipping: Boolean = true,
    private val onShippingChanged: ((Boolean) -> Unit)? = null
) : ListAdapter<CartShop, OrderShopAdapter.OrdershopViewHoler>(CartShopDiffCallback()) {

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
                tvShopTotal.text = if (cartShop.getTotalAmount() < 500000) {
                    "${cartShop.getFormattedTotalAndPeeAmount()}"
                } else {
                    "${cartShop.getFormattedTotalAmount()}"
                }
                // Setup items RecyclerView
                setupItemsRecyclerView(cartShop)

                updateShippingUI()

                binding.cvShippingFast.setOnClickListener {
                    if (!isFastShipping) {
                        isFastShipping = true
                        updateShippingUI()
                        onShippingChanged?.invoke(true)
                    }
                }
                binding.cvShippingSlow.setOnClickListener {
                    if (isFastShipping) {
                        isFastShipping = false
                        updateShippingUI()
                        onShippingChanged?.invoke(false)
                    }
                }
                Log.d("OrderShopAdapter", "isFastShipping: ${cartShop.getTotalAmount()}")
                if (cartShop.getTotalAmount() < 500000) {
                    tvShippingFree1.visibility = View.GONE
                    tvShippingFree2.visibility = View.GONE
                    item1.visibility = View.GONE
                    item2.visibility = View.GONE
                } else {
                    tvShippingFree1.visibility = View.VISIBLE
                    tvShippingFree2.visibility = View.VISIBLE
                    item1.visibility = View.VISIBLE
                    item2.visibility = View.VISIBLE
                }
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

        private fun updateShippingUI() {
            if (isFastShipping) {
                binding.apply {
                    // Fast: nền xanh, chữ trắng
                    cvShippingFast.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.main_color_dark))
                    tvTitleItem1.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.white))
                    tvShippingFree1.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.white))
                    tvShippingPrice1.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.white))
                    tvDescriptionItem1.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.white))
                    item1.background = ContextCompat.getDrawable(binding.root.context, R.color.white)
                    // Slow: nền trắng, chữ đen
                    cvShippingSlow.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.white))
                    tvTitleItem2.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.black))
                    tvShippingFree2.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.black))
                    tvShippingPrice2.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.black))
                    tvDescriptionItem2.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.black))
                    item2.background = ContextCompat.getDrawable(binding.root.context, R.color.black)
                }
            } else {
                binding.apply {
                    // Fast: nền trắng, chữ đen
                    cvShippingFast.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.white))
                    tvTitleItem1.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.black))
                    tvShippingFree1.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.black))
                    tvShippingPrice1.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.black))
                    tvDescriptionItem1.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.black))
                    item1.background = ContextCompat.getDrawable(binding.root.context, R.color.black)
                    // Slow: nền xanh, chữ trắng
                    cvShippingSlow.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.main_color_dark))
                    tvTitleItem2.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.white))
                    tvShippingFree2.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.white))
                    tvShippingPrice2.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.white))
                    tvDescriptionItem2.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.white))
                    item2.background = ContextCompat.getDrawable(binding.root.context, R.color.white)
                }
            }
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