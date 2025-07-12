package com.example.greenbuyapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.category.model.Category

class SimpleCategoryAdapter(
    private val onCategoryClick: (Category?) -> Unit = {}
) : ListAdapter<Category, SimpleCategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    private var selectedCategoryId: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_simple, parent, false) as TextView
        println("📂 SimpleCategoryAdapter: onCreateViewHolder called")
        return CategoryViewHolder(textView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        println("📂 SimpleCategoryAdapter: onBindViewHolder called for position $position, category: ${category.name}")
        holder.bind(category, onCategoryClick, selectedCategoryId == category.id)
    }

    override fun getItemCount(): Int {
        val count = super.getItemCount()
        println("📂 SimpleCategoryAdapter: getItemCount = $count")
        return count
    }

    fun updateSelectedCategory(categoryId: Int?) {
        selectedCategoryId = categoryId
        notifyDataSetChanged()
    }

    class CategoryViewHolder(
        private val textView: TextView
    ) : RecyclerView.ViewHolder(textView) {

        fun bind(category: Category, onCategoryClick: (Category?) -> Unit, isSelected: Boolean) {
            textView.text = category.name
            
            // Update selection state
            textView.isSelected = isSelected
            
            textView.setOnClickListener {
                if (isSelected) {
                    // Nếu đã được chọn, bỏ chọn -> hiển thị tất cả sản phẩm
                    println("📂 Category ${category.name} unselected -> showing all products")
                    onCategoryClick(null)
                } else {
                    // Nếu chưa được chọn, chọn category này
                    println("📂 Category ${category.name} selected")
                    onCategoryClick(category)
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