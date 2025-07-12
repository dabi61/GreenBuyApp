package com.example.greenbuyapp.ui.mall

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.product.model.Product
import com.example.greenbuyapp.data.shop.model.Shop
import com.example.greenbuyapp.databinding.ItemProductFeaturedBinding
import java.text.NumberFormat
import java.util.Locale

class FeaturedProductAdapter(
    private val onProductClick: (Product) -> Unit,
    private val onAddToCartClick: (Product) -> Unit,
    private val getShopInfo: (Int) -> Shop? = { null }
) : ListAdapter<Product, FeaturedProductAdapter.FeaturedProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedProductViewHolder {
        val binding = ItemProductFeaturedBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FeaturedProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeaturedProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FeaturedProductViewHolder(
        private val binding: ItemProductFeaturedBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                // Product name
                tvProductName.text = product.name

                // Product description
                tvProductDescription.text = product.description

                // Product price
                tvProductPrice.text = formatPrice(product.price)

                // Price badge (short format)
                tvPriceBadge.text = formatPriceShort(product.price)

                // Shop name from cache or API
                val shopInfo = getShopInfo(product.shop_id)
                tvShopName.text = shopInfo?.name ?: "Shop #${product.shop_id}"

                // Load product image
                loadProductImage(product.cover)

                // Click listeners
                root.setOnClickListener {
                    onProductClick(product)
                }

                btnAddToCart.setOnClickListener {
                    onAddToCartClick(product)
                }
            }
        }

        private fun loadProductImage(coverPath: String?) {
            val imageUrl = when {
                coverPath.isNullOrBlank() -> null
                coverPath.startsWith("http") -> coverPath
                else -> "https://www.utt-school.site$coverPath"
            }

            Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_product)
                .error(R.drawable.placeholder_product)
                .centerCrop()
                .into(binding.ivProductImage)
        }

        private fun formatPrice(price: Double): String {
            return try {
                val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
                "${formatter.format(price)} VNĐ"
            } catch (e: Exception) {
                "${price.toLong()} VNĐ"
            }
        }

        private fun formatPriceShort(price: Double): String {
            return try {
                when {
                    price >= 1_000_000 -> {
                        val millions = price / 1_000_000
                        "${String.format("%.1f", millions)}M"
                    }
                    price >= 1_000 -> {
                        val thousands = price / 1_000
                        "${String.format("%.0f", thousands)}K"
                    }
                    else -> {
                        "${price.toInt()}"
                    }
                }
            } catch (e: Exception) {
                "${price.toInt()}"
            }
        }
    }

    private class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.product_id == newItem.product_id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
} 