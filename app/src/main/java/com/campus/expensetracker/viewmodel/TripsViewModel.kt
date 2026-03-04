package com.campus.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campus.expensetracker.data.entity.Friend
import com.campus.expensetracker.data.entity.Trip
import com.campus.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TripWithDetails(
    val trip: Trip,
    val totalExpense: Double,
    val friendCount: Int
)

data class TripsUiState(
    val trips: List<TripWithDetails> = emptyList(),
    val newTripName: String = "",
    val isLoading: Boolean = true,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

class TripsViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripsUiState())
    val uiState: StateFlow<TripsUiState> = _uiState.asStateFlow()

    init {
        loadTrips()
    }

    private fun loadTrips() {
        viewModelScope.launch {
            repository.getAllTrips().collect { trips ->
                val tripsWithDetails = trips.map { trip ->
                    val totalExpense = repository.getTotalTripExpenses(trip.id)
                    val friendCount = repository.getFriendsByTripId(trip.id).first().size
                    TripWithDetails(trip, totalExpense, friendCount)
                }
                _uiState.value = _uiState.value.copy(
                    trips = tripsWithDetails,
                    isLoading = false
                )
            }
        }
    }

    fun updateNewTripName(name: String) {
        _uiState.value = _uiState.value.copy(newTripName = name)
    }

    fun createTrip() {
        viewModelScope.launch {
            try {
                val name = _uiState.value.newTripName.trim()
                if (name.isEmpty()) {
                    _uiState.value = _uiState.value.copy(error = "Please enter a trip name")
                    return@launch
                }

                val trip = Trip(name = name)
                repository.insertTrip(trip)

                _uiState.value = _uiState.value.copy(
                    newTripName = "",
                    saveSuccess = true,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to create trip"
                )
            }
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            try {
                repository.deleteTrip(trip)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete trip"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
