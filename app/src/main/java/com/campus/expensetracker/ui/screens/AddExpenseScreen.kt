package com.campus.expensetracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campus.expensetracker.data.entity.Expense
import com.campus.expensetracker.data.repository.ExpenseRepository
import com.campus.expensetracker.ui.components.*
import com.campus.expensetracker.viewmodel.ExpenseViewModel
import com.campus.expensetracker.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    expenseId: Long? = null,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    repository: ExpenseRepository
) {
    val viewModel: ExpenseViewModel = viewModel {
        ViewModelFactory(repository).create(ExpenseViewModel::class.java)
    }
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Load expense if editing
    LaunchedEffect(expenseId) {
        if (expenseId != null) {
            val expense = repository.getExpenseById(expenseId)
            expense?.let { viewModel.setEditingExpense(it) }
        }
    }

    // Handle save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaveSuccess()
            viewModel.resetForm()
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            scope.launch {
                try {
                    // Save image to app's internal storage
                    val inputStream = context.contentResolver.openInputStream(selectedUri)
                    val imageFile = File(context.filesDir, "expense_images")
                    imageFile.mkdirs()
                    val outputFile = File(imageFile, "${System.currentTimeMillis()}.jpg")
                    
                    inputStream?.use { input ->
                        FileOutputStream(outputFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    viewModel.updateImagePath(outputFile.absolutePath)
                } catch (e: Exception) {
                    // Handle error silently - image is optional
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (expenseId != null) "Edit Expense" else "Add Expense") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
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
                Spacer(modifier = Modifier.height(16.dp))

                // Amount Input - Large numeric layout
                AmountInput(
                    amount = uiState.formState.amount,
                    onAmountChange = { viewModel.updateAmount(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Category Selector
                val categories = uiState.categories.map { 
                    CategoryWithIcon(it.id, it.name, it.icon) 
                }
                CategorySelector(
                    selectedCategoryId = uiState.formState.categoryId,
                    categories = categories,
                    onCategorySelected = { viewModel.updateCategoryId(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Note Input
                NoteInput(
                    note = uiState.formState.note,
                    onNoteChange = { viewModel.updateNote(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Trip Selector
                if (uiState.trips.isNotEmpty()) {
                    val trips = uiState.trips.map { TripWithIcon(it.id, it.name) }
                    TripSelector(
                        selectedTripId = uiState.formState.tripId,
                        trips = trips,
                        onTripSelected = { 
                            viewModel.updateTripId(it)
                        }
                    )
                }

                // Friend Selector (only if trip is selected)
                if (uiState.formState.tripId != null && uiState.friendsForSelectedTrip.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    val friends = uiState.friendsForSelectedTrip.map { 
                        FriendWithIcon(it.id, it.name) 
                    }
                    FriendSelector(
                        selectedFriendId = uiState.formState.paidByFriendId,
                        friends = friends,
                        onFriendSelected = { viewModel.updatePaidByFriendId(it) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Attach Photo Button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = { imagePickerLauncher.launch("image/*") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.formState.imagePath != null) 
                                "Change Receipt Photo" 
                            else 
                                "Attach Receipt Photo (optional)",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (uiState.formState.imagePath != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Photo attached",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            TextButton(onClick = { viewModel.updateImagePath(null) }) {
                                Text("Remove")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Save Button
                Button(
                    onClick = { viewModel.saveExpense() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp),
                    enabled = uiState.formState.amount.isNotEmpty()
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (expenseId != null) "Update Expense" else "Save Expense",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Error message
                uiState.error?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Report,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
