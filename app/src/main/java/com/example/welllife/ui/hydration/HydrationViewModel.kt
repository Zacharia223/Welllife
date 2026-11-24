
package com.example.welllife.ui.hydration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.welllife.data.dao.WaterDao
import com.example.welllife.data.dao.WellnessGoalDao
import com.example.welllife.data.entity.WaterEntry
import com.example.welllife.data.entity.WellnessGoal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class HydrationViewModel(
    private val waterDao: WaterDao,
    private val wellnessGoalDao: WellnessGoalDao
) : ViewModel() {

    // Observe all saved water entries from the database
    val waterEntries: StateFlow<List<WaterEntry>> =
        waterDao.getAllWaterEntries()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Observe the saved hydration goal
    val hydrationGoal: StateFlow<WellnessGoal?> =
        wellnessGoalDao.getGoalByType("WATER")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    // Add a new water entry
    fun addWater(amountMl: Int) {
        if (amountMl <= 0) return

        viewModelScope.launch {
            waterDao.insertWaterEntry(
                WaterEntry(amountMl = amountMl)
            )
        }
    }

    // Update an existing water entry
    fun updateWater(entry: WaterEntry) {
        if (entry.amountMl <= 0) return

        viewModelScope.launch {
            waterDao.updateWaterEntry(entry)
        }
    }

    // Delete a water entry
    fun deleteWater(entry: WaterEntry) {
        viewModelScope.launch {
            waterDao.deleteWaterEntry(entry)
        }
    }

    // Calculate the total water consumed today
    fun getTodayTotal(): Int {

        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startOfDay = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, 1)

        val startOfTomorrow = calendar.timeInMillis

        return waterEntries.value
            .filter { entry ->
                entry.timestamp >= startOfDay &&
                        entry.timestamp < startOfTomorrow
            }
            .sumOf { it.amountMl }
    }
}
