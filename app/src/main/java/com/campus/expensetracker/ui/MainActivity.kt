package com.campus.expensetracker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.campus.expensetracker.data.database.ExpenseDatabase
import com.campus.expensetracker.data.repository.ExpenseRepository
import com.campus.expensetracker.navigation.AppNavGraph
import com.campus.expensetracker.navigation.Screen
import com.campus.expensetracker.theme.CampusExpenseTrackerTheme
import com.campus.expensetracker.ui.components.BottomNavigationBar
import com.campus.expensetracker.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var repository: ExpenseRepository
    private lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database and repository
        val database = ExpenseDatabase.getDatabase(application)
        repository = ExpenseRepository(
            categoryDao = database.categoryDao(),
            tripDao = database.tripDao(),
            friendDao = database.friendDao(),
            expenseDao = database.expenseDao(),
            splitDao = database.splitDao()
        )
        viewModelFactory = ViewModelFactory(repository)

        setContent {
            CampusExpenseTrackerTheme {
                MainAppContent(repository, viewModelFactory)
            }
        }
    }
}

@Composable
fun MainAppContent(
    repository: ExpenseRepository,
    viewModelFactory: ViewModelFactory
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Routes that should show bottom navigation
    val bottomNavRoutes = listOf(
        Screen.Home.route,
        Screen.Expenses.route,
        Screen.Trips.route,
        Screen.Categories.route
    )

    val showBottomNav = currentRoute in bottomNavRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomNav) {
                BottomNavigationBar(
                    currentRoute = currentRoute ?: Screen.Home.route,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            repository = repository,
            viewModelFactory = viewModelFactory
        )
    }
}
