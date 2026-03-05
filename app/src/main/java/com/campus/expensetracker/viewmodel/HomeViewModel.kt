package com.campus.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campus.expensetracker.data.entity.Category
import com.campus.expensetracker.data.entity.Expense
import com.campus.expensetracker.data.entity.Trip
import com.campus.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val totalMonthlyExpenses: Double = 0.0,
    val recentExpenses: List<Expense> = emptyList(),
    val trips: List<Trip> = emptyList(),
    val categorySummary: Map<String, Double> = emptyMap(),
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfMonth = calendar.timeInMillis

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endOfMonth = calendar.timeInMillis

            combine(
                repository.getRecentExpenses(startOfMonth, endOfMonth),
                repository.getAllTrips()
            ) { recentExpenses, trips ->
                Pair(recentExpenses, trips)
            }.collect { (recentExpenses, trips) ->
                val totalExpenses = repository.getTotalMonthlyExpenses(startOfMonth, endOfMonth)
                val categorySummary = calculateCategorySummary(recentExpenses)

                _uiState.value = HomeUiState(
                    totalMonthlyExpenses = totalExpenses,
                    recentExpenses = recentExpenses,
                    trips = trips,
                    categorySummary = categorySummary,
                    isLoading = false
                )
            }
        }
    }

    private fun calculateCategorySummary(expenses: List<Expense>): Map<Long, Double> {
        return expenses.groupBy { it.categoryId }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
    }
}
