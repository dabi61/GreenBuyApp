package com.example.greenbuyapp.ui.admin.category

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.category.model.Category
import com.example.greenbuyapp.data.category.model.SubCategory
import com.example.greenbuyapp.databinding.ActivityCategoryManagementBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CategoryManagementActivity : BaseActivity<ActivityCategoryManagementBinding>() {

    override val viewModel: CategoryManagementViewModel by viewModel()
    private lateinit var categoryAdapter: CategoryAdapter

    override val binding: ActivityCategoryManagementBinding by lazy {
        ActivityCategoryManagementBinding.inflate(layoutInflater)
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, CategoryManagementActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        window.statusBarColor = ContextCompat.getColor(this, R.color.main_color)
    }

    override fun observeViewModel() {
        super.observeViewModel()
        
        // Chỉ observe khi categoryAdapter đã được khởi tạo
        if (::categoryAdapter.isInitialized) {
            lifecycleScope.launch {
                viewModel.uiState.collect { state ->
                    binding.swipeRefreshLayout.isRefreshing = state.isLoading
                    
                    binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    
                    state.successMessage?.let { message ->
                        Toast.makeText(this@CategoryManagementActivity, message, Toast.LENGTH_SHORT).show()
                        viewModel.clearMessages()
                    }
                    
                    state.errorMessage?.let { message ->
                        Toast.makeText(this@CategoryManagementActivity, message, Toast.LENGTH_LONG).show()
                        viewModel.clearMessages()
                    }
                }
            }

            lifecycleScope.launch {
                viewModel.categoryItems.collect { items ->
                    categoryAdapter.submitList(items)
                    
                    // Show/hide empty state
                    if (items.isEmpty()) {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    } else {
                        binding.emptyStateLayout.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Quản lý danh mục"
        }
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            onCategoryClick = { category ->
                viewModel.toggleCategory(category.id)
            },
            onEditCategory = { category ->
                showEditCategoryDialog(category)
            },
            onAddSubCategory = { category ->
                showAddSubCategoryDialog(category)
            },
            onEditSubCategory = { subCategory ->
                showEditSubCategoryDialog(subCategory)
            },
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CategoryManagementActivity)
            adapter = categoryAdapter
        }
        
        // Gọi observeViewModel sau khi adapter đã được khởi tạo
        startObservingViewModel()
    }

    private fun startObservingViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.swipeRefreshLayout.isRefreshing = state.isLoading
                
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                
                state.successMessage?.let { message ->
                    Toast.makeText(this@CategoryManagementActivity, message, Toast.LENGTH_SHORT).show()
                    viewModel.clearMessages()
                }
                
                state.errorMessage?.let { message ->
                    Toast.makeText(this@CategoryManagementActivity, message, Toast.LENGTH_LONG).show()
                    viewModel.clearMessages()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.categoryItems.collect { items ->
                categoryAdapter.submitList(items)
                
                // Show/hide empty state
                if (items.isEmpty()) {
                    binding.emptyStateLayout.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                } else {
                    binding.emptyStateLayout.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadCategories()
        }
    }

    private fun showAddCategoryDialog() {
        val dialog = AddEditCategoryDialog.newInstance(
            type = AddEditCategoryDialog.DialogType.ADD_CATEGORY
        )
        
        dialog.setOnSaveListener { name, description ->
            viewModel.createCategory(name, description)
        }
        
        dialog.show(supportFragmentManager, "AddCategoryDialog")
    }

    private fun showEditCategoryDialog(category: Category) {
        val dialog = AddEditCategoryDialog.newInstance(
            type = AddEditCategoryDialog.DialogType.EDIT_CATEGORY,
            category = category
        )
        
        dialog.setOnSaveListener { name, description ->
            viewModel.updateCategory(category.id, name, description)
        }
        
        dialog.show(supportFragmentManager, "EditCategoryDialog")
    }

    private fun showDeleteCategoryDialog(category: Category) {
        AlertDialog.Builder(this)
            .setTitle("Xóa danh mục")
            .setMessage("Bạn có chắc chắn muốn xóa danh mục \"${category.name}\"?\n\nLưu ý: Tất cả danh mục con sẽ bị xóa theo.")
            .setPositiveButton("Xóa") { _, _ ->
                viewModel.deleteCategory(category.id)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showAddSubCategoryDialog(category: Category) {
        val dialog = AddEditCategoryDialog.newInstance(
            type = AddEditCategoryDialog.DialogType.ADD_SUBCATEGORY,
            parentCategoryId = category.id
        )
        
        dialog.setOnSaveListener { name, description ->
            viewModel.createSubCategory(category.id, name, description)
        }
        
        dialog.show(supportFragmentManager, "AddSubCategoryDialog")
    }

    private fun showEditSubCategoryDialog(subCategory: SubCategory) {
        val dialog = AddEditCategoryDialog.newInstance(
            type = AddEditCategoryDialog.DialogType.EDIT_SUBCATEGORY,
            subCategory = subCategory
        )
        
        dialog.setOnSaveListener { name, description ->
            viewModel.updateSubCategory(subCategory.id, name, description)
        }
        
        dialog.show(supportFragmentManager, "EditSubCategoryDialog")
    }

    private fun showDeleteSubCategoryDialog(subCategory: SubCategory) {
        AlertDialog.Builder(this)
            .setTitle("Xóa danh mục con")
            .setMessage("Bạn có chắc chắn muốn xóa danh mục con \"${subCategory.name}\"?")
            .setPositiveButton("Xóa") { _, _ ->
                viewModel.deleteSubCategory(subCategory.id)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
} 