package com.campus.expensetracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AmountInput(
    amount: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "₹",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
        OutlinedTextField(
            value = amount,
            onValueChange = { value ->
                // Only allow digits and one decimal point
                if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*\$"))) {
                    onAmountChange(value)
                }
            },
            label = { Text("Amount") },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textStyle = MaterialTheme.typography.displayMedium
        )
    }
}

@Composable
fun NoteInput(
    note: String,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        label = { Text("Note (optional)") },
        placeholder = { Text("Add a note...") },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    selectedCategoryId: Long?,
    categories: List<CategoryWithIcon>,
    onCategorySelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Category",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategoryId == null,
                    onClick = { onCategorySelected(null) },
                    label = { Text("All") },
                    leadingIcon = if (selectedCategoryId == null) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null
                )
            }

            items(categories) { category ->
                FilterChip(
                    selected = selectedCategoryId == category.id,
                    onClick = { onCategorySelected(category.id) },
                    label = { Text(category.name) },
                    leadingIcon = if (selectedCategoryId == category.id) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        { Text(category.icon) }
                    }
                )
            }
        }
    }
}

data class CategoryWithIcon(
    val id: Long,
    val name: String,
    val icon: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripSelector(
    selectedTripId: Long?,
    trips: List<TripWithIcon>,
    onTripSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (trips.isEmpty()) return
    
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Trip (optional)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = selectedTripId?.let { id -> trips.find { it.id == id }?.name } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Trip") },
                placeholder = { Text("No trip selected") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
        }
        
        trips.forEach { trip ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedTripId == trip.id,
                    onClick = { onTripSelected(trip.id) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(trip.name)
            }
        }
    }
}

data class TripWithIcon(
    val id: Long,
    val name: String
)

@Composable
fun FriendSelector(
    selectedFriendId: Long?,
    friends: List<FriendWithIcon>,
    onFriendSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (friends.isEmpty()) return
    
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Paid by friend (optional)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        friends.forEach { friend ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedFriendId == friend.id,
                    onClick = { onFriendSelected(friend.id) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = friend.name,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

data class FriendWithIcon(
    val id: Long,
    val name: String
)
