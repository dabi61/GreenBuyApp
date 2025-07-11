package com.example.greenbuyapp.ui.product.trending

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.product.model.TrendingProduct
import com.example.greenbuyapp.databinding.ItemProductTrendingBinding
import java.text.NumberFormat
import java.util.*

class TrendingProductAdapter(
    private val onTrendingClick: (TrendingProduct) -> Unit = {}
) : ListAdapter<TrendingProduct, TrendingProductAdapter.TrendingProductViewHolder>(TrendingProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendingProductViewHolder {
        val binding = ItemProductTrendingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrendingProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrendingProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product, position, onTrendingClick)
    }

    inner class TrendingProductViewHolder(
        private val binding: ItemProductTrendingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: TrendingProduct, position: Int, onTrendingClick: (TrendingProduct) -> Unit) {
            binding.tvTopRank.text = "TOP ${position + 1}"
            binding.tvName.text = product.name

            val formattedPrice = NumberFormat.getNumberInstance(Locale("vi", "VN"))
                .format(product.price.toLong())
            binding.tvPrice.text = "${formattedPrice}đ"

            val context = binding.root.context

            // 👉 Sử dụng cardView thay vì root để giữ bo góc
            when (position) {
                0 -> {
                    binding.tvTopRank.setTextColor(context.getColor(R.color.orange_red))
                    binding.tvTopRank.textSize = 20f // to hơn
                    binding.tvTopRank.setTypeface(null, Typeface.BOLD)
                    binding.cardView.setCardBackgroundColor(context.getColor(R.color.green1_background))
                }
                1 -> {
                    binding.tvTopRank.setTextColor(context.getColor(R.color.silver_text))
                    binding.tvTopRank.textSize = 18f // to hơn
                    binding.tvTopRank.setTypeface(null, Typeface.BOLD)
                    binding.cardView.setCardBackgroundColor(context.getColor(R.color.green2_background))
                }
                2 -> {
                    binding.tvTopRank.setTextColor(context.getColor(R.color.bronze_text))
                    binding.tvTopRank.textSize = 16f // to hơn
                    binding.tvTopRank.setTypeface(null, Typeface.BOLD)
                    binding.cardView.setCardBackgroundColor(context.getColor(R.color.green3_background))
                }
                else -> {
                    binding.tvTopRank.setTextColor(context.getColor(android.R.color.black))
                    binding.cardView.setCardBackgroundColor(context.getColor(android.R.color.white))
                }
            }
            binding.imgHot.visibility = if (position in 0..2) View.VISIBLE else View.GONE
            // Load ảnh
            val imageUrl = if (product.cover?.startsWith("http") == true) {
                product.cover
            } else {
                "https://www.utt-school.site${product.cover}"
            }

            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.pic_item_product)
                .error(R.drawable.pic_item_product)
                .into(binding.imgProduct)

            binding.root.setOnClickListener {
                onTrendingClick(product)
            }
        }

    }

    class TrendingProductDiffCallback : DiffUtil.ItemCallback<TrendingProduct>() {
        override fun areItemsTheSame(oldItem: TrendingProduct, newItem: TrendingProduct): Boolean {
            return oldItem.product_id == newItem.product_id
        }

        override fun areContentsTheSame(oldItem: TrendingProduct, newItem: TrendingProduct): Boolean {
            return oldItem == newItem
        }
    }
}
