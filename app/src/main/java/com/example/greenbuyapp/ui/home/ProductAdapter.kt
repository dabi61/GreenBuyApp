package com.example.greenbuyapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.product.model.Product
import com.example.greenbuyapp.databinding.ItemProductBinding
import java.text.NumberFormat
import java.util.*

class ProductAdapter(
    private val onProductClick: (Product) -> Unit = {}
) : PagedListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        if (product != null) {
            holder.bind(product, onProductClick)
        }
    }

    class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product, onProductClick: (Product) -> Unit) {
            binding.apply {
                // Set product name
                tvName.text = product.name

                // Format and set price
                val formattedPrice = NumberFormat.getNumberInstance(Locale("vi", "VN"))
                    .format(product.price.toLong())
                tvPrice.text = formattedPrice

                // Load product image
                val imageView = binding.root.findViewById<android.widget.ImageView>(R.id.img_product)
                if (!product.cover.isNullOrEmpty()) {
                    val imageUrl = if (product.cover.startsWith("http")) {
                        product.cover
                    } else {
                        "https://www.utt-school.site${product.cover}"
                    }
                    
                    Glide.with(itemView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.pic_item_product)
                        .error(R.drawable.pic_item_product)
                        .into(imageView)
                } else {
                    // Use placeholder image
                    imageView.setImageResource(R.drawable.pic_item_product)
                }

                // Set click listener
                root.setOnClickListener {
                    onProductClick(product)
                }

                // Hide sold info for now (we don't have this data from API)
//                tvSelled.text = ""
            }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.product_id == newItem.product_id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
} 