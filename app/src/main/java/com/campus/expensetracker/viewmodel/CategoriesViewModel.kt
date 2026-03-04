package com.campus.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campus.expensetracker.data.entity.Category
import com.campus.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val newCategoryName: String = "",
    val newCategoryIcon: String = "📁",
    val isLoading: Boolean = true,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

class CategoriesViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    val icons = listOf("🍔", "🚗", "🏠", "📚", "🎬", "👕", "💊", "📱", "🎮", "✈️", "💡", "📁")

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getAllCategories().collect { categories ->
                _uiState.value = _uiState.value.copy(
                    categories = categories,
                    isLoading = false
                )
            }
        }
    }

    fun updateNewCategoryName(name: String) {
        _uiState.value = _uiState.value.copy(newCategoryName = name)
    }

    fun updateNewCategoryIcon(icon: String) {
        _uiState.value = _uiState.value.copy(newCategoryIcon = icon)
    }

    fun addCategory() {
        viewModelScope.launch {
            try {
                val name = _uiState.value.newCategoryName.trim()
                if (name.isEmpty()) {
                    _uiState.value = _uiState.value.copy(error = "Please enter a category name")
                    return@launch
                }

                val category = Category(
                    name = name,
                    icon = _uiState.value.newCategoryIcon
                )
                repository.insertCategory(category)

                _uiState.value = _uiState.value.copy(
                    newCategoryName = "",
                    saveSuccess = true,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to add category"
                )
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(category)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete category"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
