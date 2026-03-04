package com.campus.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.campus.expensetracker.data.repository.ExpenseRepository
import com.campus.expensetracker.ui.components.TripCard
import com.campus.expensetracker.viewmodel.TripsViewModel
import com.campus.expensetracker.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(
    onNavigateToTripDetail: (Long) -> Unit,
    repository: ExpenseRepository
) {
    val viewModel: TripsViewModel = viewModel {
        ViewModelFactory(repository).create(TripsViewModel::class.java)
    }
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var tripToDelete by remember { mutableStateOf<com.campus.expensetracker.data.entity.Trip?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trips") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Trip",
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
        } else if (uiState.trips.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✈️", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No trips yet",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Tap + to create your first trip",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.trips, key = { it.trip.id }) { tripWithDetails ->
                    TripCard(
                        trip = tripWithDetails.trip,
                        totalExpense = tripWithDetails.totalExpense,
                        friendCount = tripWithDetails.friendCount,
                        onClick = { onNavigateToTripDetail(tripWithDetails.trip.id) },
                        modifier = Modifier.animateItemPlacement()
                    )
                    
                    // Add delete button row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                tripToDelete = tripWithDetails.trip
                                showDeleteDialog = true
                            }
                        ) {
                            Text("Delete Trip", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    // Add Trip Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create Trip") },
            text = {
                OutlinedTextField(
                    value = uiState.newTripName,
                    onValueChange = { viewModel.updateNewTripName(it) },
                    label = { Text("Trip Name") },
                    placeholder = { Text("e.g., Goa Trip, College Fest") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.createTrip()
                        showAddDialog = false
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog && tripToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Trip") },
            text = { Text("Are you sure you want to delete this trip? All associated friends and expenses will also be deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        tripToDelete?.let { trip ->
                            viewModel.deleteTrip(trip)
                        }
                        showDeleteDialog = false
                        tripToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Error snackbar
    uiState.error?.let { error ->
        Snackbar(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            action = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Dismiss", color = MaterialTheme.colorScheme.onError)
                }
            }
        ) {
            Text(error)
        }
    }
}
