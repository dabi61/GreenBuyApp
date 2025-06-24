package com.example.greenbuyapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.ItemBannerBinding


/**
 * Adapter cho ViewPager2 banner
 */
class BannerAdapter(
    private val onBannerClick: (Int) -> Unit = {}
) : ListAdapter<Int, BannerAdapter.BannerViewHolder>(BannerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemBannerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BannerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val banner = getItem(position)
        holder.bind(banner, onBannerClick)
    }

    class BannerViewHolder(
        private val binding: ItemBannerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(banner: Int, onBannerClick: (Int) -> Unit) {
            binding.apply {
                // Load image from resource or URL
                Glide.with(itemView.context)
                    .load(banner)
                    .centerCrop()
                    .placeholder(R.drawable.banner_1)
                    .error(R.drawable.banner_1)
                    .into(ivBanner)

                // Set title and description if available

                // Set click listener
                root.setOnClickListener {
                    onBannerClick(banner)
                }
            }
        }
    }

    class BannerDiffCallback : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }
    }
} 