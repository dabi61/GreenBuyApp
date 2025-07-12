package com.example.greenbuyapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.data.category.model.Category
import com.example.greenbuyapp.databinding.ItemHomeCategoryBinding

class CategoryAdapter(
    private val onCategoryClick: (Category) -> Unit = {}
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    private var selectedCategoryId: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemHomeCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category, onCategoryClick)
    }

    fun updateSelectedCategory(categoryId: Int?) {
        selectedCategoryId = categoryId
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(
        private val binding: ItemHomeCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category, onCategoryClick: (Category) -> Unit) {
            binding.apply {
                // Set category name
                chipCategory.text = category.name
                
                // Set selected state
                chipCategory.isChecked = selectedCategoryId == category.id
                
                // Set click listener
                chipCategory.setOnClickListener {
                    if (chipCategory.isChecked) {
                        // Nếu đang được chọn, bỏ chọn
                        selectedCategoryId = null
                        onCategoryClick(category)
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

    class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
}