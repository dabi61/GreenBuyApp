package com.example.greenbuyapp.ui.admin.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.category.model.Category
import com.example.greenbuyapp.data.category.model.SubCategory
import com.example.greenbuyapp.databinding.ItemCategoryBinding
import com.example.greenbuyapp.databinding.ItemSubcategoryBinding

class CategoryAdapter(
    private val onCategoryClick: (Category) -> Unit,
    private val onEditCategory: (Category) -> Unit,
    private val onDeleteCategory: (Category) -> Unit,
    private val onAddSubCategory: (Category) -> Unit,
    private val onEditSubCategory: (SubCategory) -> Unit,
    private val onDeleteSubCategory: (SubCategory) -> Unit
) : ListAdapter<CategoryItem, RecyclerView.ViewHolder>(CategoryDiffCallback()) {

    companion object {
        private const val TYPE_CATEGORY = 0
        private const val TYPE_SUBCATEGORY = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CategoryItem.CategoryType -> TYPE_CATEGORY
            is CategoryItem.SubCategoryType -> TYPE_SUBCATEGORY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_CATEGORY -> {
                val binding = ItemCategoryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                CategoryViewHolder(binding)
            }
            TYPE_SUBCATEGORY -> {
                val binding = ItemSubcategoryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SubCategoryViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CategoryViewHolder -> {
                val item = getItem(position) as CategoryItem.CategoryType
                holder.bind(item.category, item.isExpanded)
            }
            is SubCategoryViewHolder -> {
                val item = getItem(position) as CategoryItem.SubCategoryType
                holder.bind(item.subCategory)
            }
        }
    }

    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category, isExpanded: Boolean) {
            binding.apply {
                tvCategoryName.text = category.name
                tvCategoryDescription.text = category.description
                tvCategoryCreatedAt.text = "Tạo lúc: ${category.created_at}"
                
                // Set expand/collapse icon
                ivExpand.setImageResource(
                    if (isExpanded) R.drawable.ic_keyboard_arrow_up_24
                    else R.drawable.ic_keyboard_arrow_down_24
                )

                // Click listeners
                root.setOnClickListener {
                    onCategoryClick(category)
                }

                btnEditCategory.setOnClickListener {
                    onEditCategory(category)
                }

                btnDeleteCategory.setOnClickListener {
                    onDeleteCategory(category)
                }

                btnAddSubCategory.setOnClickListener {
                    onAddSubCategory(category)
                }
            }
        }
    }

    inner class SubCategoryViewHolder(
        private val binding: ItemSubcategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(subCategory: SubCategory) {
            binding.apply {
                tvSubCategoryName.text = subCategory.name
                tvSubCategoryDescription.text = subCategory.description
                tvSubCategoryCreatedAt.text = "Tạo lúc: ${subCategory.created_at}"

                // Click listeners
                btnEditSubCategory.setOnClickListener {
                    onEditSubCategory(subCategory)
                }

                btnDeleteSubCategory.setOnClickListener {
                    onDeleteSubCategory(subCategory)
                }
            }
        }
    }
}

sealed class CategoryItem {
    data class CategoryType(
        val category: Category,
        val isExpanded: Boolean = false
    ) : CategoryItem()

    data class SubCategoryType(
        val subCategory: SubCategory
    ) : CategoryItem()
}

class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryItem>() {
    override fun areItemsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean {
        return when {
            oldItem is CategoryItem.CategoryType && newItem is CategoryItem.CategoryType ->
                oldItem.category.id == newItem.category.id
            oldItem is CategoryItem.SubCategoryType && newItem is CategoryItem.SubCategoryType ->
                oldItem.subCategory.id == newItem.subCategory.id
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean {
        return oldItem == newItem
    }
} 