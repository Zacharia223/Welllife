package com.example.welllife

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.welllife.data.WellLifeDatabase
import com.example.welllife.data.entity.SleepEntry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun SleepScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sleepDao = remember(context) {
        WellLifeDatabase.getDatabase(context).sleepDao()
    }
    val scope = rememberCoroutineScope()

    val historyFlow = remember(sleepDao) { sleepDao.getAllSleepEntries() }
    val sleepEntries by historyFlow.collectAsState(initial = emptyList())

    var durationText by rememberSaveable { mutableStateOf("") }
    var feedback by rememberSaveable { mutableStateOf("") }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deletingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var isBusy by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = durationText,
            onValueChange = {
                durationText = it
                feedback = ""
            },
            label = { Text("Sleep duration (minutes)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !isBusy,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                enabled = !isBusy,
                onClick = {
                    val minutes = durationText.trim().toIntOrNull()
                    val id = editingId
                    val entry = sleepEntries.firstOrNull { it.id == id }

                    if (minutes == null || minutes <= 0) {
                        feedback = "Enter a positive whole number of minutes."
                    } else if (id != null && entry == null) {
                        feedback = "This entry is no longer available. Cancel the edit to continue."
                    } else {
                        isBusy = true
                        feedback = ""

                        scope.launch {
                            try {
                                if (entry == null) {
                                    sleepDao.insertSleepEntry(
                                        SleepEntry(durationMinutes = minutes)
                                    )
                                    feedback = "Saved $minutes minutes of sleep."
                                } else {
                                    sleepDao.updateSleepEntry(
                                        entry.copy(durationMinutes = minutes)
                                    )
                                    feedback = "Sleep entry updated."
                                }

                                editingId = null
                                durationText = ""
                            } catch (e: CancellationException) {
                                throw e
                            } catch (_: Exception) {
                                feedback = "Couldn’t save the entry. Try again."
                            } finally {
                                isBusy = false
                            }
                        }
                    }
                }
            ) {
                Text(if (editingId == null) "Save" else "Save changes")
            }

            if (editingId != null) {
                TextButton(
                    enabled = !isBusy,
                    onClick = {
                        editingId = null
                        durationText = ""
                        feedback = ""
                    }
                ) {
                    Text("Cancel edit")
                }
            }
        }

        if (feedback.isNotEmpty()) {
            Text(feedback)
        }

        Text("Sleep history")

        if (sleepEntries.isEmpty()) {
            Text("No sleep entries yet.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sleepEntries, key = { it.id }) { entry ->
                    val hours = entry.durationMinutes / 60
                    val minutes = entry.durationMinutes % 60

                    Column {
                        Text("$hours h $minutes min")

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                enabled = !isBusy,
                                onClick = {
                                    editingId = entry.id
                                    durationText = entry.durationMinutes.toString()
                                    feedback = ""
                                }
                            ) {
                                Text("Edit")
                            }

                            TextButton(
                                enabled = !isBusy,
                                onClick = { deletingId = entry.id }
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    val entryToDelete = sleepEntries.firstOrNull { it.id == deletingId }

    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { deletingId = null },
            title = { Text("Delete sleep entry?") },
            text = {
                Text("Remove this ${entryToDelete.durationMinutes}-minute entry from your history?")
            },
            confirmButton = {
                TextButton(
                    enabled = !isBusy,
                    onClick = {
                        deletingId = null
                        isBusy = true
                        feedback = ""

                        scope.launch {
                            try {
                                sleepDao.deleteSleepEntry(entryToDelete)

                                if (editingId == entryToDelete.id) {
                                    editingId = null
                                    durationText = ""
                                }

                                feedback = "Sleep entry deleted."
                            } catch (e: CancellationException) {
                                throw e
                            } catch (_: Exception) {
                                feedback = "Couldn’t delete the entry. Try again."
                            } finally {
                                isBusy = false
                            }
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}