package com.example.welllife.ui.goals

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.welllife.data.entity.WellnessGoal
import com.example.welllife.data.repository.DashboardRepository
import com.example.welllife.reminders.GoalReminderPreferences
import com.example.welllife.reminders.GoalReminderScheduler
import kotlinx.coroutines.launch

@Composable
fun GoalsScreen(goals: List<WellnessGoal>, repository: DashboardRepository, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var editingGoal by remember { mutableStateOf<WellnessGoal?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var pendingReminderGoal by remember { mutableStateOf<WellnessGoal?>(null) }
    var editorError by remember { mutableStateOf<String?>(null) }
    var reminderVersion by remember { mutableIntStateOf(0) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        pendingReminderGoal?.let { goal ->
            if (granted) {
                GoalReminderPreferences.setEnabled(context, goal.goalType, true)
                GoalReminderScheduler.schedule(context, goal.goalType, goal.targetValue, goal.unit)
                reminderVersion++
            }
            pendingReminderGoal = null
        }
    }

    Column(modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Set targets for water, sleep, or activity. Reminders are optional.")
        Button(onClick = { editingGoal = null; editorError = null; showEditor = true }) { Text("Add goal") }
        if (goals.isEmpty()) Text("No goals yet.")
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(goals, key = { it.id }) { goal ->
                val enabled = reminderVersion.let {
                    GoalReminderPreferences.isEnabled(context, goal.goalType)
                }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${goal.goalType}: ${goal.targetValue} ${goal.unit}")
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            OutlinedButton(onClick = { editingGoal = goal; editorError = null; showEditor = true }) { Text("Edit") }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Reminders")
                                Switch(checked = enabled, onCheckedChange = { checked ->
                                    if (!checked) {
                                        GoalReminderPreferences.setEnabled(context, goal.goalType, false)
                                        GoalReminderScheduler.cancel(context, goal.goalType)
                                        reminderVersion++
                                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        pendingReminderGoal = goal
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        GoalReminderPreferences.setEnabled(context, goal.goalType, true)
                                        GoalReminderScheduler.schedule(context, goal.goalType, goal.targetValue, goal.unit)
                                        reminderVersion++
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        GoalEditorDialog(
            initialGoal = editingGoal,
            errorMessage = editorError,
            onDismiss = { showEditor = false; editorError = null },
            onSave = { goal ->
                val normalizedType = when (goal.goalType.trim().uppercase()) {
                    "WATER", "HYDRATION" -> "WATER"
                    "ACTIVITY", "PHYSICAL ACTIVITY" -> "ACTIVITY"
                    "SLEEP" -> "SLEEP"
                    else -> goal.goalType.trim()
                }
                val normalizedGoal = goal.copy(goalType = normalizedType)
                val duplicateType = goals.any {
                    it.id != normalizedGoal.id && it.goalType.equals(normalizedGoal.goalType, ignoreCase = true)
                }
                if (duplicateType) {
                    editorError = "A goal for ${normalizedGoal.goalType} already exists."
                } else {
                    scope.launch {
                        val previousGoal = editingGoal
                        val reminderWasEnabled = previousGoal?.let {
                            GoalReminderPreferences.isEnabled(context, it.goalType)
                        } == true
                        if (previousGoal != null && previousGoal.goalType != normalizedGoal.goalType) {
                            GoalReminderPreferences.setEnabled(context, previousGoal.goalType, false)
                            GoalReminderScheduler.cancel(context, previousGoal.goalType)
                        }
                        if (normalizedGoal.id == 0L) repository.saveGoal(normalizedGoal) else repository.updateGoal(normalizedGoal)
                        if (reminderWasEnabled) {
                            GoalReminderPreferences.setEnabled(context, normalizedGoal.goalType, true)
                        }
                        if (reminderWasEnabled || GoalReminderPreferences.isEnabled(context, normalizedGoal.goalType)) {
                            GoalReminderScheduler.schedule(context, normalizedGoal.goalType, normalizedGoal.targetValue, normalizedGoal.unit)
                        }
                        showEditor = false
                    }
                }
            },
            onDelete = { goal ->
                scope.launch {
                    repository.deleteGoal(goal)
                    GoalReminderPreferences.setEnabled(context, goal.goalType, false)
                    GoalReminderScheduler.cancel(context, goal.goalType)
                    showEditor = false
                }
            }
        )
    }
}

@Composable
private fun GoalEditorDialog(
    initialGoal: WellnessGoal?,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (WellnessGoal) -> Unit,
    onDelete: (WellnessGoal) -> Unit
) {
    var type by remember(initialGoal) { mutableStateOf(initialGoal?.goalType ?: "Water") }
    var target by remember(initialGoal) { mutableStateOf(initialGoal?.targetValue?.toString() ?: "") }
    var unit by remember(initialGoal) { mutableStateOf(initialGoal?.unit ?: "mL") }
    val targetValue = target.toDoubleOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialGoal == null) "Add wellness goal" else "Edit wellness goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(type, { type = it }, label = { Text("Goal type") }, singleLine = true)
                OutlinedTextField(target, { target = it }, label = { Text("Target") }, singleLine = true)
                OutlinedTextField(unit, { unit = it }, label = { Text("Unit") }, singleLine = true)
                errorMessage?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(enabled = type.isNotBlank() && targetValue != null && targetValue >= 0 && unit.isNotBlank(), onClick = {
                onSave(WellnessGoal(initialGoal?.id ?: 0L, type.trim(), targetValue!!, unit.trim()))
            }) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (initialGoal != null) TextButton(onClick = { onDelete(initialGoal) }) { Text("Delete") }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
