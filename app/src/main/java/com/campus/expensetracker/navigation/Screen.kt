package com.campus.expensetracker.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Expenses : Screen("expenses")
    object Trips : Screen("trips")
    object Categories : Screen("categories")
    object AddExpense : Screen("add_expense")
    object EditExpense : Screen("edit_expense/{expenseId}") {
        fun createRoute(expenseId: Long) = "edit_expense/$expenseId"
    }
    object TripDetail : Screen("trip_detail/{tripId}") {
        fun createRoute(tripId: Long) = "trip_detail/$tripId"
    }
}
