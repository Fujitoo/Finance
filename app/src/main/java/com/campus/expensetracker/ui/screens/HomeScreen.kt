package com.campus.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.campus.expensetracker.data.entity.Category
import com.campus.expensetracker.data.entity.Expense
import com.campus.expensetracker.data.entity.Trip
import com.campus.expensetracker.data.repository.ExpenseRepository
import com.campus.expensetracker.ui.components.ExpenseCard
import com.campus.expensetracker.ui.components.SummaryCard
import com.campus.expensetracker.ui.components.TripCard
import com.campus.expensetracker.viewmodel.HomeViewModel
import com.campus.expensetracker.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToExpenses: () -> Unit,
    onNavigateToTrips: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToTripDetail: (Long) -> Unit,
    repository: ExpenseRepository
) {
    val viewModel: HomeViewModel = viewModel {
        ViewModelFactory(repository).create(HomeViewModel::class.java)
    }
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Campus Expense Tracker") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onNavigateToAddExpense) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Expense",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Summary Card
                SummaryCard(
                    title = "This Month's Spending",
                    amount = uiState.totalMonthlyExpenses
                )

                // Trips Section
                if (uiState.trips.isNotEmpty()) {
                    Text(
                        text = "Your Trips",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    uiState.trips.take(3).forEach { trip ->
                        TripCard(
                            trip = trip,
                            totalExpense = 0.0,
                            friendCount = 0,
                            onClick = { onNavigateToTripDetail(trip.id) }
                        )
                    }
                    
                    if (uiState.trips.size > 3) {
                        TextButton(
                            onClick = onNavigateToTrips,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("View All Trips")
                        }
                    }
                }

                // Recent Expenses Section
                if (uiState.recentExpenses.isNotEmpty()) {
                    Text(
                        text = "Recent Expenses",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    uiState.recentExpenses.take(5).forEach { expense ->
                        ExpenseCard(
                            expense = expense,
                            category = null,
                            onClick = { },
                            onEditClick = { },
                            onDeleteClick = { }
                        )
                    }
                    
                    if (uiState.recentExpenses.size > 5) {
                        TextButton(
                            onClick = onNavigateToExpenses,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("View All Expenses")
                        }
                    }
                }

                // Empty state
                if (uiState.trips.isEmpty() && uiState.recentExpenses.isEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👋",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Welcome!",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start by adding your first expense or creating a trip.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateToAddExpense) {
                            Text("Add Expense")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
