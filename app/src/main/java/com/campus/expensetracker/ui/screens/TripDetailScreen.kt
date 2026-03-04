package com.campus.expensetracker.ui.screens

import android.content.Context
import android.content.Intent
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
import com.campus.expensetracker.data.entity.Expense
import com.campus.expensetracker.data.repository.ExpenseRepository
import com.campus.expensetracker.ui.components.ExpenseCard
import com.campus.expensetracker.utils.ShareSummaryGenerator
import com.campus.expensetracker.viewmodel.TripDetailViewModel
import com.campus.expensetracker.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    repository: ExpenseRepository
) {
    val viewModel: TripDetailViewModel = viewModel {
        ViewModelFactory(repository).create(TripDetailViewModel::class.java, tripId)
    }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var showDeleteFriendDialog by remember { mutableStateOf(false) }
    var friendToDelete by remember { mutableStateOf(com.campus.expensetracker.data.entity.Friend?>(null)) }
    var showDeleteExpenseDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.trip?.name ?: "Trip Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Summary")
                    }
                    IconButton(onClick = onNavigateToAddExpense) {
                        Icon(Icons.Default.Add, contentDescription = "Add Expense")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
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
                // Summary Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Total Expense",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "₹${String.format("%.2f", uiState.totalExpense)}",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${uiState.friends.size} friends",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Friends Section
                Text(
                    text = "Friends",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (uiState.friends.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "No friends added yet",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            TextButton(onClick = { showAddFriendDialog = true }) {
                                Text("Add Friend")
                            }
                        }
                    }
                } else {
                    uiState.friends.forEach { friend ->
                        val balance = uiState.friendBalances.find { it.friend.id == friend.id }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = friend.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    balance?.let {
                                        Text(
                                            text = "Paid: ₹${String.format("%.2f", it.paidAmount)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val balanceText = if (it.balance > 0) {
                                            "Should receive: ₹${String.format("%.2f", it.balance)}"
                                        } else if (it.balance < 0) {
                                            "Owes: ₹${String.format("%.2f", -it.balance)}"
                                        } else {
                                            "Settled up"
                                        }
                                        Text(
                                            text = balanceText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (it.balance > 0) 
                                                MaterialTheme.colorScheme.primary 
                                            else if (it.balance < 0) 
                                                MaterialTheme.colorScheme.error 
                                            else 
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        friendToDelete = friend
                                        showDeleteFriendDialog = true
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { showAddFriendDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Friend")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Expenses Section
                Text(
                    text = "Expenses",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (uiState.expenses.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "No expenses added yet",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            TextButton(onClick = onNavigateToAddExpense) {
                                Text("Add Expense")
                            }
                        }
                    }
                } else {
                    uiState.expenses.forEach { expense ->
                        ExpenseCard(
                            expense = expense,
                            category = null,
                            onClick = { },
                            onEditClick = { },
                            onDeleteClick = {
                                expenseToDelete = expense
                                showDeleteExpenseDialog = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Add Friend Dialog
    if (showAddFriendDialog) {
        AlertDialog(
            onDismissRequest = { showAddFriendDialog = false },
            title = { Text("Add Friend") },
            text = {
                OutlinedTextField(
                    value = uiState.newFriendName,
                    onValueChange = { viewModel.updateNewFriendName(it) },
                    label = { Text("Friend Name") },
                    placeholder = { Text("Enter name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addFriend()
                        showAddFriendDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFriendDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Friend Dialog
    if (showDeleteFriendDialog && friendToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteFriendDialog = false },
            title = { Text("Delete Friend") },
            text = { Text("Are you sure you want to delete this friend?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        friendToDelete?.let { friend ->
                            viewModel.deleteFriend(friend)
                        }
                        showDeleteFriendDialog = false
                        friendToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteFriendDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Expense Dialog
    if (showDeleteExpenseDialog && expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteExpenseDialog = false },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete this expense?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        expenseToDelete?.let { expense ->
                            viewModel.deleteExpense(expense)
                        }
                        showDeleteExpenseDialog = false
                        expenseToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteExpenseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Share Dialog
    if (showShareDialog) {
        val friendBalanceData = uiState.friendBalances.map { balance ->
            ShareSummaryGenerator.FriendBalanceData(
                friendName = balance.friend.name,
                paidAmount = balance.paidAmount,
                balance = balance.balance
            )
        }
        
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Summary") },
            text = {
                Column {
                    Text(
                        text = "Share as image or text?",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = viewModel.getShareableSummary(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            },
            confirmButton = {
                Row {
                    TextButton(
                        onClick = {
                            // Share as text
                            shareSummary(context, viewModel.getShareableSummary())
                            showShareDialog = false
                        }
                    ) {
                        Text("Text")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            // Generate and share image
                            val imageUri = ShareSummaryGenerator.generateSummaryImage(
                                context = context,
                                tripName = uiState.trip?.name ?: "Trip",
                                totalExpense = uiState.totalExpense,
                                friendBalances = friendBalanceData
                            )
                            imageUri?.let { uri ->
                                ShareSummaryGenerator.shareImage(context, uri)
                            }
                            showShareDialog = false
                        }
                    ) {
                        Text("Image")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareDialog = false }) {
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

private fun shareSummary(context: Context, summary: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, summary)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}
