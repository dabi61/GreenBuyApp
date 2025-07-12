package com.example.greenbuyapp.ui.mall

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.data.category.model.Category
import com.example.greenbuyapp.databinding.ItemCategoryChipBinding

class CategoryChipAdapter(
    private val onCategoryClick: (Category?) -> Unit
) : ListAdapter<Category, CategoryChipAdapter.CategoryChipViewHolder>(CategoryDiffCallback()) {

    private var selectedCategoryId: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryChipViewHolder {
        val binding = ItemCategoryChipBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryChipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryChipViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateSelectedCategory(categoryId: Int?) {
        selectedCategoryId = categoryId
        notifyDataSetChanged()
    }

    inner class CategoryChipViewHolder(
        private val binding: ItemCategoryChipBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.apply {
                chipCategory.text = category.name
                chipCategory.isChecked = selectedCategoryId == category.id
                
                chipCategory.setOnClickListener {
                    if (chipCategory.isChecked) {
                        // Nếu đang được chọn, bỏ chọn
                        selectedCategoryId = null
                        onCategoryClick(null)
                    } else {
                        // Chọn category mới
                        selectedCategoryId = category.id
                        onCategoryClick(category)
                    }
                    updateSelectedCategory(selectedCategoryId)
                }
            }
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
} 