package com.example.welllife.ui.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.welllife.data.entity.ActivityEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityScreen(
    viewModel: ActivityViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var activityType by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf("") }
    var caloriesBurned by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var editingEntry by remember { mutableStateOf<ActivityEntry?>(null) }
    var editActivityType by remember { mutableStateOf("") }
    var editDurationMinutes by remember { mutableStateOf("") }
    var editCaloriesBurned by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Today's Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Today's Activity Total",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${uiState.todayTotalMinutes} minutes",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Add Activity Form Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Log New Activity",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = activityType,
                    onValueChange = { activityType = it },
                    label = { Text("Activity Type (e.g., Running, Walking)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = durationMinutes,
                        onValueChange = { durationMinutes = it },
                        label = { Text("Duration (mins)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = caloriesBurned,
                        onValueChange = { caloriesBurned = it },
                        label = { Text("Calories (opt)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        viewModel.addActivity(activityType, durationMinutes, caloriesBurned) { success, error ->
                            if (success) {
                                activityType = ""
                                durationMinutes = ""
                                caloriesBurned = ""
                                errorMessage = null
                            } else {
                                errorMessage = error
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Activity")
                }
            }
        }

        Text(
            text = "Activity History",
            style = MaterialTheme.typography.titleMedium
        )

        if (uiState.entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No activities logged yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.entries, key = { it.id }) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = entry.activityType,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${entry.durationMinutes} mins${if (entry.caloriesBurned > 0) " • ${entry.caloriesBurned} kcal" else ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = dateFormat.format(Date(entry.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                TextButton(onClick = {
                                    editingEntry = entry
                                    editActivityType = entry.activityType
                                    editDurationMinutes = entry.durationMinutes.toString()
                                    editCaloriesBurned = if (entry.caloriesBurned > 0) entry.caloriesBurned.toString() else ""
                                }) {
                                    Text("Edit")
                                }
                                TextButton(onClick = {
                                    viewModel.deleteActivity(entry)
                                }) {
                                    Text("Delete", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Dialog
    if (editingEntry != null) {
        AlertDialog(
            onDismissRequest = { editingEntry = null },
            title = { Text("Edit Activity") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editActivityType,
                        onValueChange = { editActivityType = it },
                        label = { Text("Activity Type") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editDurationMinutes,
                        onValueChange = { editDurationMinutes = it },
                        label = { Text("Duration (mins)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editCaloriesBurned,
                        onValueChange = { editCaloriesBurned = it },
                        label = { Text("Calories (opt)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    editingEntry?.let { entry ->
                        viewModel.updateActivity(entry, editActivityType, editDurationMinutes, editCaloriesBurned) { success, _ ->
                            if (success) {
                                editingEntry = null
                            }
                        }
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingEntry = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
