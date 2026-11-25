
package com.example.welllife.ui.hydration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.welllife.data.entity.WaterEntry
import java.util.Calendar

private const val DEFAULT_WATER_GOAL_ML = 2000

@Composable
fun HydrationScreen(
    viewModel: HydrationViewModel,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.waterEntries.collectAsState()
    val hydrationGoal by viewModel.hydrationGoal.collectAsState()

    var customAmount by remember { mutableStateOf("") }
    var editingEntry by remember { mutableStateOf<WaterEntry?>(null) }
    var editAmount by remember { mutableStateOf("") }

    // Convert the saved goal into millilitres
    val goalUnit = hydrationGoal?.unit?.trim()?.lowercase() ?: "ml"

    val savedTarget = hydrationGoal?.targetValue ?: DEFAULT_WATER_GOAL_ML.toDouble()

    val dailyWaterGoal = when (goalUnit) {
        "l", "liter", "liters", "litre", "litres" ->
            (savedTarget * 1000).toInt()
        else -> savedTarget.toInt()
    }.coerceAtLeast(1)

    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val startOfDay = calendar.timeInMillis

    calendar.add(Calendar.DAY_OF_MONTH, 1)
    val startOfTomorrow = calendar.timeInMillis

    val todayEntries = entries.filter {
        it.timestamp >= startOfDay &&
                it.timestamp < startOfTomorrow
    }

    val todayTotal = todayEntries.sumOf { it.amountMl }

    val progress = (todayTotal.toFloat() / dailyWaterGoal)
        .coerceIn(0f, 1f)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Text(
                text = "Hydration Tracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Stay hydrated and keep track of your daily water intake.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Today's Water Intake",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$todayTotal ml",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )

                    Text(
                        text = "Daily goal: $dailyWaterGoal ml",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .background(
                                Color.White,
                                RoundedCornerShape(50)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(12.dp)
                                .background(
                                    Color(0xFF2196F3),
                                    RoundedCornerShape(50)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${(progress * 100).toInt()}% of daily goal completed"
                    )
                }
            }
        }

        item {
            Text(
                text = "Quick Add Water",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(250, 500, 750).forEach { amount ->
                    Button(
                        onClick = {
                            viewModel.addWater(amount)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("${amount}ml")
                    }
                }
            }
        }

        item {
            Text(
                text = "Add Custom Amount",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = customAmount,
                onValueChange = {
                    if (it.all { character -> character.isDigit() }) {
                        customAmount = it
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Enter amount in ml") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val amount = customAmount.toIntOrNull()

                    if (amount != null && amount > 0) {
                        viewModel.addWater(amount)
                        customAmount = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = (customAmount.toIntOrNull() ?: 0) > 0
            ) {
                Text("Add Water")
            }
        }

        item {
            Text(
                text = "Today's Water Entries",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (todayEntries.isEmpty()) {
            item {
                Text(
                    text = "No water entries yet. Add your first drink!",
                    color = Color.Gray
                )
            }
        } else {
            items(
                items = todayEntries,
                key = { it.id }
            ) { entry ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${entry.amountMl} ml",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "Water recorded"
                            )
                        }

                        TextButton(
                            onClick = {
                                editingEntry = entry
                                editAmount = entry.amountMl.toString()
                            }
                        ) {
                            Text("Edit")
                        }

                        TextButton(
                            onClick = {
                                viewModel.deleteWater(entry)
                            }
                        ) {
                            Text(
                                text = "Delete",
                                color = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }

    editingEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = {
                editingEntry = null
            },
            title = {
                Text("Edit Water Entry")
            },
            text = {
                OutlinedTextField(
                    value = editAmount,
                    onValueChange = {
                        if (it.all { character -> character.isDigit() }) {
                            editAmount = it
                        }
                    },
                    label = { Text("Amount in ml") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newAmount = editAmount.toIntOrNull()

                        if (newAmount != null && newAmount > 0) {
                            viewModel.updateWater(
                                entry.copy(amountMl = newAmount)
                            )
                            editingEntry = null
                        }
                    },
                    enabled = (editAmount.toIntOrNull() ?: 0) > 0
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        editingEntry = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
