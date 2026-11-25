package com.example.welllife.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.welllife.data.dao.ActivityDao
import com.example.welllife.data.entity.ActivityEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class ActivityUiState(
    val entries: List<ActivityEntry> = emptyList(),
    val todayTotalMinutes: Int = 0,
    val errorMessage: String? = null
)

class ActivityViewModel(
    private val activityDao: ActivityDao
) : ViewModel() {

    val uiState: StateFlow<ActivityUiState> = activityDao.getAllActivityEntries()
        .map { entries ->
            val calendar = Calendar.getInstance()
            val todayYear = calendar.get(Calendar.YEAR)
            val todayDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

            val todayTotal = entries.filter { entry ->
                calendar.timeInMillis = entry.timestamp
                calendar.get(Calendar.YEAR) == todayYear &&
                        calendar.get(Calendar.DAY_OF_YEAR) == todayDayOfYear
            }.sumOf { it.durationMinutes }

            ActivityUiState(
                entries = entries,
                todayTotalMinutes = todayTotal
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ActivityUiState()
        )

    fun addActivity(
        activityType: String,
        durationMinutesStr: String,
        caloriesStr: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val trimmedType = activityType.trim()
        if (trimmedType.isEmpty()) {
            onResult(false, "Activity type cannot be empty")
            return
        }

        val duration = durationMinutesStr.toIntOrNull()
        if (duration == null || duration <= 0) {
            onResult(false, "Please enter a valid positive duration in minutes")
            return
        }

        val calories = caloriesStr.toIntOrNull() ?: 0

        viewModelScope.launch {
            try {
                val entry = ActivityEntry(
                    activityType = trimmedType,
                    durationMinutes = duration,
                    caloriesBurned = calories,
                    timestamp = System.currentTimeMillis()
                )
                activityDao.insertActivityEntry(entry)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to save activity")
            }
        }
    }

    fun updateActivity(
        entry: ActivityEntry,
        activityType: String,
        durationMinutesStr: String,
        caloriesStr: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val trimmedType = activityType.trim()
        if (trimmedType.isEmpty()) {
            onResult(false, "Activity type cannot be empty")
            return
        }

        val duration = durationMinutesStr.toIntOrNull()
        if (duration == null || duration <= 0) {
            onResult(false, "Please enter a valid positive duration in minutes")
            return
        }

        val calories = caloriesStr.toIntOrNull() ?: 0

        viewModelScope.launch {
            try {
                val updated = entry.copy(
                    activityType = trimmedType,
                    durationMinutes = duration,
                    caloriesBurned = calories
                )
                activityDao.updateActivityEntry(updated)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to update activity")
            }
        }
    }

    fun deleteActivity(entry: ActivityEntry) {
        viewModelScope.launch {
            activityDao.deleteActivityEntry(entry)
        }
    }
}

class ActivityViewModelFactory(
    private val activityDao: ActivityDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivityViewModel::class.java)) {
            return ActivityViewModel(activityDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
