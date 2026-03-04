package com.campus.expensetracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.campus.expensetracker.data.repository.ExpenseRepository
import com.campus.expensetracker.ui.screens.AddExpenseScreen
import com.campus.expensetracker.ui.screens.CategoriesScreen
import com.campus.expensetracker.ui.screens.ExpensesScreen
import com.campus.expensetracker.ui.screens.HomeScreen
import com.campus.expensetracker.ui.screens.TripDetailScreen
import com.campus.expensetracker.ui.screens.TripsScreen
import com.campus.expensetracker.viewmodel.ViewModelFactory

@Composable
fun AppNavGraph(
    navController: NavHostController,
    repository: ExpenseRepository,
    viewModelFactory: ViewModelFactory
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToExpenses = { navController.navigate(Screen.Expenses.route) },
                onNavigateToTrips = { navController.navigate(Screen.Trips.route) },
                onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.route) },
                onNavigateToTripDetail = { tripId ->
                    navController.navigate(Screen.TripDetail.createRoute(tripId))
                },
                repository = repository
            )
        }

        composable(Screen.Expenses.route) {
            ExpensesScreen(
                onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.route) },
                onNavigateToEditExpense = { expenseId ->
                    navController.navigate(Screen.EditExpense.createRoute(expenseId))
                },
                repository = repository
            )
        }

        composable(Screen.Trips.route) {
            TripsScreen(
                onNavigateToTripDetail = { tripId ->
                    navController.navigate(Screen.TripDetail.createRoute(tripId))
                },
                repository = repository
            )
        }

        composable(Screen.Categories.route) {
            CategoriesScreen(repository = repository)
        }

        composable(Screen.AddExpense.route) {
            AddExpenseScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() },
                repository = repository
            )
        }

        composable(
            route = Screen.EditExpense.route,
            arguments = listOf(
                navArgument("expenseId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getLong("expenseId") ?: return@composable
            AddExpenseScreen(
                expenseId = expenseId,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() },
                repository = repository
            )
        }

        composable(
            route = Screen.TripDetail.route,
            arguments = listOf(
                navArgument("tripId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: return@composable
            TripDetailScreen(
                tripId = tripId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.route) },
                repository = repository
            )
        }
    }
}
