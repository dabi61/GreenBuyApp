package com.example.greenbuyapp.ui.admin.category

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.category.model.Category
import com.example.greenbuyapp.data.category.model.SubCategory
import com.example.greenbuyapp.databinding.DialogAddEditCategoryBinding

class AddEditCategoryDialog : DialogFragment() {

    private var _binding: DialogAddEditCategoryBinding? = null
    private val binding get() = _binding!!

    private var dialogType: DialogType = DialogType.ADD_CATEGORY
    private var category: Category? = null
    private var subCategory: SubCategory? = null
    private var parentCategoryId: Int? = null

    private var onSaveListener: ((String, String) -> Unit)? = null

    companion object {
        fun newInstance(
            type: DialogType,
            category: Category? = null,
            subCategory: SubCategory? = null,
            parentCategoryId: Int? = null
        ): AddEditCategoryDialog {
            return AddEditCategoryDialog().apply {
                this.dialogType = type
                this.category = category
                this.subCategory = subCategory
                this.parentCategoryId = parentCategoryId
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddEditCategoryBinding.inflate(LayoutInflater.from(context))
        
        setupUI()
        
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun setupUI() {
        when (dialogType) {
            DialogType.ADD_CATEGORY -> {
                binding.tvTitle.text = "Thêm danh mục mới"
            }
            DialogType.EDIT_CATEGORY -> {
                binding.tvTitle.text = "Sửa danh mục"
                category?.let {
                    binding.etName.setText(it.name)
                    binding.etDescription.setText(it.description)
                }
            }
            DialogType.ADD_SUBCATEGORY -> {
                binding.tvTitle.text = "Thêm danh mục con"
            }
            DialogType.EDIT_SUBCATEGORY -> {
                binding.tvTitle.text = "Sửa danh mục con"
                subCategory?.let {
                    binding.etName.setText(it.name)
                    binding.etDescription.setText(it.description)
                }
            }
        }

        // Set up click listeners
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            
            if (validateInput(name, description)) {
                onSaveListener?.invoke(name, description)
                dismiss()
            }
        }
    }

    private fun validateInput(name: String, description: String): Boolean {
        return when {
            name.isEmpty() -> {
                binding.etName.error = "Vui lòng nhập tên"
                false
            }
            description.isEmpty() -> {
                binding.etDescription.error = "Vui lòng nhập mô tả"
                false
            }
            else -> true
        }
    }

    fun setOnSaveListener(listener: (String, String) -> Unit) {
        this.onSaveListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class DialogType {
        ADD_CATEGORY,
        EDIT_CATEGORY,
        ADD_SUBCATEGORY,
        EDIT_SUBCATEGORY
    }
} 