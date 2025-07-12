package com.example.greenbuyapp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.data.category.model.Category

class CategoryTextAdapter(
    private val onCategoryClick: (Category) -> Unit = {}
) : ListAdapter<Category, CategoryTextAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    private var selectedCategoryId: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val textView = TextView(parent.context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 24
                bottomMargin = 16
            }
            setPadding(32, 16, 32, 16)
            textSize = 14f
            setTextColor(android.graphics.Color.BLACK)
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 40f
                setStroke(2, android.graphics.Color.parseColor("#4CAF50"))
                setColor(android.graphics.Color.WHITE)
            }
            isClickable = true
            isFocusable = true
        }
        return CategoryViewHolder(textView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category, onCategoryClick, selectedCategoryId == category.id)
    }

    fun updateSelectedCategory(categoryId: Int?) {
        selectedCategoryId = categoryId
        notifyDataSetChanged()
    }

    class CategoryViewHolder(
        private val textView: TextView
    ) : RecyclerView.ViewHolder(textView) {

        fun bind(category: Category, onCategoryClick: (Category) -> Unit, isSelected: Boolean) {
            textView.text = category.name
            
            // Update appearance based on selected state
            val background = textView.background as android.graphics.drawable.GradientDrawable
            if (isSelected) {
                background.setColor(android.graphics.Color.parseColor("#4CAF50"))
                textView.setTextColor(android.graphics.Color.WHITE)
            } else {
                background.setColor(android.graphics.Color.WHITE)
                textView.setTextColor(android.graphics.Color.BLACK)
            }
            
            textView.setOnClickListener {
                onCategoryClick(category)
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