package com.campus.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campus.expensetracker.data.entity.*
import com.campus.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FriendBalance(
    val friend: Friend,
    val paidAmount: Double,
    val owedAmount: Double,
    val balance: Double // Positive = should receive, Negative = should pay
)

data class TripDetailUiState(
    val trip: Trip? = null,
    val friends: List<Friend> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val totalExpense: Double = 0.0,
    val friendBalances: List<FriendBalance> = emptyList(),
    val newFriendName: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

class TripDetailViewModel(
    private val repository: ExpenseRepository,
    private val tripId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripDetailUiState())
    val uiState: StateFlow<TripDetailUiState> = _uiState.asStateFlow()

    init {
        loadTripDetails()
    }

    private fun loadTripDetails() {
        viewModelScope.launch {
            combine(
                repository.getFriendsByTripId(tripId),
                repository.getExpensesByTripId(tripId)
            ) { friends, expenses ->
                Pair(friends, expenses)
            }.collect { (friends, expenses) ->
                val trip = repository.getTripById(tripId)
                val totalExpense = expenses.sumOf { it.amount }
                val balances = calculateBalances(friends, expenses)

                _uiState.value = _uiState.value.copy(
                    trip = trip,
                    friends = friends,
                    expenses = expenses,
                    totalExpense = totalExpense,
                    friendBalances = balances,
                    isLoading = false
                )
            }
        }
    }

    private fun calculateBalances(friends: List<Friend>, expenses: List<Expense>): List<FriendBalance> {
        val equalShare = if (friends.isNotEmpty()) {
            expenses.sumOf { it.amount } / friends.size
        } else 0.0

        return friends.map { friend ->
            val paidAmount = expenses.filter { it.paidByFriendId == friend.id }.sumOf { it.amount }
            val balance = paidAmount - equalShare
            FriendBalance(friend, paidAmount, equalShare, balance)
        }
    }

    fun updateNewFriendName(name: String) {
        _uiState.value = _uiState.value.copy(newFriendName = name)
    }

    fun addFriend() {
        viewModelScope.launch {
            try {
                val name = _uiState.value.newFriendName.trim()
                if (name.isEmpty()) {
                    _uiState.value = _uiState.value.copy(error = "Please enter a friend name")
                    return@launch
                }

                val friend = Friend(
                    name = name,
                    tripId = tripId
                )
                repository.insertFriend(friend)

                _uiState.value = _uiState.value.copy(
                    newFriendName = "",
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to add friend"
                )
            }
        }
    }

    fun deleteFriend(friend: Friend) {
        viewModelScope.launch {
            try {
                repository.deleteFriend(friend)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete friend"
                )
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(expense)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete expense"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getShareableSummary(): String {
        val state = _uiState.value
        val trip = state.trip ?: return ""

        val sb = StringBuilder()
        sb.appendLine("📍 ${trip.name}")
        sb.appendLine("💰 Total Expense: ₹${String.format("%.2f", state.totalExpense)}")
        sb.appendLine()

        state.friendBalances.forEach { balance ->
            sb.appendLine("${balance.friend.name} paid ₹${String.format("%.2f", balance.paidAmount)}")
        }

        sb.appendLine()
        sb.appendLine("⚖️ Balance:")

        val toReceive = state.friendBalances.filter { it.balance > 0 }
        val toPay = state.friendBalances.filter { it.balance < 0 }

        toReceive.forEach { balance ->
            sb.appendLine("${balance.friend.name} should receive ₹${String.format("%.2f", balance.balance)}")
        }

        toPay.forEach { balance ->
            sb.appendLine("${balance.friend.name} owes ₹${String.format("%.2f", -balance.balance)}")
        }

        return sb.toString()
    }
}
