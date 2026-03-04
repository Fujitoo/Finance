package com.campus.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.campus.expensetracker.data.repository.ExpenseRepository

class ViewModelFactory(
    private val repository: ExpenseRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ExpenseViewModel::class.java) -> {
                ExpenseViewModel(repository) as T
            }
            modelClass.isAssignableFrom(CategoriesViewModel::class.java) -> {
                CategoriesViewModel(repository) as T
            }
            modelClass.isAssignableFrom(TripsViewModel::class.java) -> {
                TripsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(TripDetailViewModel::class.java) -> {
                throw IllegalArgumentException("TripDetailViewModel requires tripId. Use create(tripId) instead.")
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    fun create(modelClass: Class<TripDetailViewModel>, tripId: Long): TripDetailViewModel {
        return TripDetailViewModel(repository, tripId)
    }
}
