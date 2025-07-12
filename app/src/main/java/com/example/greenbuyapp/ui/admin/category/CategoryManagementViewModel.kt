package com.example.greenbuyapp.ui.admin.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.category.model.*
import com.example.greenbuyapp.domain.category.CategoryRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryManagementViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryManagementUiState())
    val uiState: StateFlow<CategoryManagementUiState> = _uiState

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _categoryItems = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categoryItems: StateFlow<List<CategoryItem>> = _categoryItems

    private val _expandedCategories = MutableStateFlow<Set<Int>>(emptySet())
    private val _subCategories = MutableStateFlow<Map<Int, List<SubCategory>>>(emptyMap())

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = categoryRepository.getCategories()) {
                is Result.Success -> {
                    _categories.value = result.value
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                    updateCategoryItems()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.error ?: "Lỗi không xác định"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                is Result.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Lỗi kết nối mạng"
                    )
                }
            }
        }
    }

    fun toggleCategory(categoryId: Int) {
        val currentExpanded = _expandedCategories.value.toMutableSet()
        if (currentExpanded.contains(categoryId)) {
            currentExpanded.remove(categoryId)
        } else {
            currentExpanded.add(categoryId)
            loadSubCategories(categoryId)
        }
        _expandedCategories.value = currentExpanded
        updateCategoryItems()
    }

    private fun loadSubCategories(categoryId: Int) {
        viewModelScope.launch {
            when (val result = categoryRepository.getSubCategories(categoryId)) {
                is Result.Success -> {
                    val currentSubCategories = _subCategories.value.toMutableMap()
                    currentSubCategories[categoryId] = result.value
                    _subCategories.value = currentSubCategories
                    updateCategoryItems()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Lỗi tải danh mục con: ${result.error}"
                    )
                }
                is Result.Loading -> {
                    // Loading state handled by parent
                }
                is Result.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Lỗi kết nối mạng khi tải danh mục con"
                    )
                }
            }
        }
    }

    private fun updateCategoryItems() {
        val items = mutableListOf<CategoryItem>()
        val expandedSet = _expandedCategories.value
        val subCategoriesMap = _subCategories.value

        _categories.value.forEach { category ->
            val isExpanded = expandedSet.contains(category.id)
            items.add(CategoryItem.CategoryType(category, isExpanded))
            
            if (isExpanded) {
                subCategoriesMap[category.id]?.forEach { subCategory ->
                    items.add(CategoryItem.SubCategoryType(subCategory))
                }
            }
        }
        
        _categoryItems.value = items
    }

    fun createCategory(name: String, description: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val request = CreateCategoryRequest(name, description)
            when (val result = categoryRepository.createCategory(request)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Tạo danh mục thành công"
                    )
                    loadCategories() // Reload categories
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Lỗi tạo danh mục: ${result.error}"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                is Result.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Lỗi kết nối mạng"
                    )
                }
            }
        }
    }

    fun updateCategory(categoryId: Int, name: String, description: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val request = UpdateCategoryRequest(name, description)
            when (val result = categoryRepository.updateCategory(categoryId, request)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Cập nhật danh mục thành công"
                    )
                    loadCategories() // Reload categories
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Lỗi cập nhật danh mục: ${result.error}"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                is Result.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Lỗi kết nối mạng"
                    )
                }
            }
        }
    }

    fun deleteCategory(categoryId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = categoryRepository.deleteCategory(categoryId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Xóa danh mục thành công"
                    )
                    loadCategories() // Reload categories
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Lỗi xóa danh mục: ${result.error}"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                is Result.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Lỗi kết nối mạng"
                    )
                }
            }
        }
    }

    fun createSubCategory(categoryId: Int, name: String, description: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val request = CreateSubCategoryRequest(categoryId, name, description)
            when (val result = categoryRepository.createSubCategory(request)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Tạo danh mục con thành công"
                    )
                    loadSubCategories(categoryId) // Reload subcategories
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Lỗi tạo danh mục con: ${result.error}"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                is Result.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Lỗi kết nối mạng"
                    )
                }
            }
        }
    }

    fun updateSubCategory(subcategoryId: Int, name: String, description: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val request = UpdateSubCategoryRequest(name, description)
            when (val result = categoryRepository.updateSubCategory(subcategoryId, request)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Cập nhật danh mục con thành công"
                    )
                    // Reload subcategories for all expanded categories
                    _expandedCategories.value.forEach { categoryId ->
                        loadSubCategories(categoryId)
                    }
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Lỗi cập nhật danh mục con: ${result.error}"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                is Result.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Lỗi kết nối mạng"
                    )
                }
            }
        }
    }

    fun deleteSubCategory(subcategoryId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = categoryRepository.deleteSubCategory(subcategoryId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Xóa danh mục con thành công"
                    )
                    // Reload subcategories for all expanded categories
                    _expandedCategories.value.forEach { categoryId ->
                        loadSubCategories(categoryId)
                    }
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Lỗi xóa danh mục con: ${result.error}"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                is Result.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Lỗi kết nối mạng"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }
}

data class CategoryManagementUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
) 