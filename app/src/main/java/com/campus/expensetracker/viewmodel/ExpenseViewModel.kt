package com.campus.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campus.expensetracker.data.entity.Category
import com.campus.expensetracker.data.entity.Expense
import com.campus.expensetracker.data.entity.Friend
import com.campus.expensetracker.data.entity.Split
import com.campus.expensetracker.data.entity.Trip
import com.campus.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ExpenseFormState(
    val amount: String = "",
    val categoryId: Long? = null,
    val note: String = "",
    val tripId: Long? = null,
    val paidByFriendId: Long? = null,
    val imagePath: String? = null,
    val isEditing: Boolean = false,
    val expenseId: Long? = null
)

data class ExpenseUiState(
    val formState: ExpenseFormState = ExpenseFormState(),
    val categories: List<Category> = emptyList(),
    val trips: List<Trip> = emptyList(),
    val friendsForSelectedTrip: List<Friend> = emptyList(),
    val isLoading: Boolean = true,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

class ExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        loadFormData()
    }

    private fun loadFormData() {
        viewModelScope.launch {
            combine(
                repository.getAllCategories(),
                repository.getAllTrips()
            ) { categories, trips ->
                Pair(categories, trips)
            }.collect { (categories, trips) ->
                _uiState.value = _uiState.value.copy(
                    categories = categories,
                    trips = trips,
                    isLoading = false
                )
            }
        }
    }

    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(amount = amount)
        )
    }

    fun updateCategoryId(categoryId: Long?) {
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(categoryId = categoryId)
        )
    }

    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(note = note)
        )
    }

    fun updateTripId(tripId: Long?) {
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(tripId = tripId)
        )
        // Load friends for selected trip
        if (tripId != null) {
            viewModelScope.launch {
                repository.getFriendsByTripId(tripId).collect { friends ->
                    _uiState.value = _uiState.value.copy(
                        friendsForSelectedTrip = friends
                    )
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(
                friendsForSelectedTrip = emptyList()
            )
        }
    }

    fun updatePaidByFriendId(friendId: Long?) {
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(paidByFriendId = friendId)
        )
    }

    fun updateImagePath(imagePath: String?) {
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(imagePath = imagePath)
        )
    }

    fun setEditingExpense(expense: Expense) {
        _uiState.value = _uiState.value.copy(
            formState = ExpenseFormState(
                amount = expense.amount.toString(),
                categoryId = expense.categoryId,
                note = expense.note,
                tripId = expense.tripId,
                paidByFriendId = expense.paidByFriendId,
                imagePath = expense.imagePath,
                isEditing = true,
                expenseId = expense.id
            )
        )
        // Load friends if expense is for a trip
        expense.tripId?.let { tripId ->
            viewModelScope.launch {
                repository.getFriendsByTripId(tripId).collect { friends ->
                    _uiState.value = _uiState.value.copy(
                        friendsForSelectedTrip = friends
                    )
                }
            }
        }
    }

    fun saveExpense() {
        viewModelScope.launch {
            try {
                val amount = _uiState.value.formState.amount.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    _uiState.value = _uiState.value.copy(error = "Please enter a valid amount")
                    return@launch
                }

                val expense = Expense(
                    id = _uiState.value.formState.expenseId ?: 0,
                    amount = amount,
                    categoryId = _uiState.value.formState.categoryId,
                    note = _uiState.value.formState.note,
                    date = System.currentTimeMillis(),
                    imagePath = _uiState.value.formState.imagePath,
                    tripId = _uiState.value.formState.tripId,
                    paidByFriendId = _uiState.value.formState.paidByFriendId
                )

                if (_uiState.value.formState.isEditing) {
                    repository.updateExpense(expense)
                } else {
                    repository.insertExpense(expense)
                }

                _uiState.value = _uiState.value.copy(
                    saveSuccess = true,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to save expense"
                )
            }
        }
    }

    fun resetForm() {
        _uiState.value = _uiState.value.copy(
            formState = ExpenseFormState(),
            saveSuccess = false,
            error = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
