package com.example.greenbuyapp.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.ItemUtilBinding


/**
 * Adapter cho ViewPager2 Util
 */

data class UtilProfile(
    val image: Int,
    val title: String,
)

class UtilAdapter(
    private val onUtilClick: (UtilProfile) -> Unit = {}
) : ListAdapter<UtilProfile, UtilAdapter.UtilViewHolder>(UtilDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UtilViewHolder {
        val binding = ItemUtilBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UtilViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UtilViewHolder, position: Int) {
        val utilProfile = getItem(position)
        holder.bind(utilProfile, onUtilClick)
    }

    class UtilViewHolder(
        private val binding: ItemUtilBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(utilProfile: UtilProfile, onUtilClick: (UtilProfile) -> Unit) {
            binding.apply {
                // Load image from resource or URL
                Glide.with(itemView.context)
                    .load(utilProfile.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_util_1)
                    .error(R.drawable.ic_util_1)
                    .into(ivImage)

                // Set title and description if available
                tvName.text = utilProfile.title
                // Set click listener
                root.setOnClickListener {
                    onUtilClick(utilProfile)
                }
            }
        }
    }

    class UtilDiffCallback : DiffUtil.ItemCallback<UtilProfile>() {
        override fun areItemsTheSame(oldItem: UtilProfile, newItem: UtilProfile): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: UtilProfile, newItem: UtilProfile): Boolean {
            return oldItem == newItem
        }
    }
}