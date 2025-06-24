package com.example.greenbuyapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.product.model.TrendingProduct
import com.example.greenbuyapp.databinding.ItemTrendingBinding
import java.text.NumberFormat
import java.util.*

class TrendingAdapter(
    private val onTrendingClick: (TrendingProduct) -> Unit = {}
) : ListAdapter<TrendingProduct, TrendingAdapter.TrendingViewHolder>(TrendingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendingViewHolder {
        val binding = ItemTrendingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrendingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrendingViewHolder, position: Int) {
        val trendingProduct = getItem(position)
        holder.bind(trendingProduct, onTrendingClick)
    }

    class TrendingViewHolder(
        private val binding: ItemTrendingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(trendingProduct: TrendingProduct, onTrendingClick: (TrendingProduct) -> Unit) {
            binding.apply {
                // Set trending product name
//                tvName.text = trendingProduct.name
                
                // Format and set price
                val formattedPrice = NumberFormat.getNumberInstance(Locale("vi", "VN"))
                    .format(trendingProduct.price.toLong())
//                tvPrice.text = formattedPrice
                
                // Load product image
                val imageView = binding.root.findViewById<android.widget.ImageView>(R.id.img_product)
                if (!trendingProduct.cover.isNullOrEmpty()) {
                    val imageUrl = if (trendingProduct.cover.startsWith("http")) {
                        trendingProduct.cover
                    } else {
                        "https://www.utt-school.site${trendingProduct.cover}"
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
                    onTrendingClick(trendingProduct)
                }
            }
        }
    }

    class TrendingDiffCallback : DiffUtil.ItemCallback<TrendingProduct>() {
        override fun areItemsTheSame(oldItem: TrendingProduct, newItem: TrendingProduct): Boolean {
            return oldItem.product_id == newItem.product_id
        }

        override fun areContentsTheSame(oldItem: TrendingProduct, newItem: TrendingProduct): Boolean {
            return oldItem == newItem
        }
    }
}