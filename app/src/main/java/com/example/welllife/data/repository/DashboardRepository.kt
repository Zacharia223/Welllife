package com.example.welllife.data.repository

import com.example.welllife.data.dao.ActivityDao
import com.example.welllife.data.dao.SleepDao
import com.example.welllife.data.dao.WaterDao
import com.example.welllife.data.dao.WellnessGoalDao
import com.example.welllife.data.entity.WellnessGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.ZoneId

data class GoalProgress(
    val goal: WellnessGoal,
    val currentValue: Double
) {
    val progressText: String
        get() {
            val currentFormatted = if (currentValue % 1 == 0.0) currentValue.toInt().toString() else currentValue.toString()
            val targetFormatted = if (goal.targetValue % 1 == 0.0) goal.targetValue.toInt().toString() else goal.targetValue.toString()
            return "${goal.goalType}: $currentFormatted / $targetFormatted ${goal.unit}"
        }
}

data class DashboardData(
    val todayWaterMl: Int = 0,
    val todayActivityMinutes: Int = 0,
    val latestSleepDurationMinutes: Int? = null,
    val goals: List<WellnessGoal> = emptyList()
) {
    val waterSummary: String
        get() = "$todayWaterMl mL today"

    val activitySummary: String
        get() = "$todayActivityMinutes min today"

    val sleepSummary: String
        get() = latestSleepDurationMinutes?.let { minutes ->
            val hours = minutes / 60
            val mins = minutes % 60
            if (hours > 0 && mins > 0) "${hours}h ${mins}m"
            else if (hours > 0) "$hours hours"
            else "$mins min"
        } ?: "No sleep recorded"

    val goalProgresses: List<GoalProgress>
        get() = goals.map { goal ->
            val current = when (goal.goalType.uppercase()) {
                "WATER", "HYDRATION" -> todayWaterMl.toDouble()
                "ACTIVITY", "PHYSICAL ACTIVITY" -> todayActivityMinutes.toDouble()
                "SLEEP" -> (latestSleepDurationMinutes ?: 0).toDouble()
                else -> 0.0
            }
            GoalProgress(goal = goal, currentValue = current)
        }

    val goalsSummary: String
        get() = if (goals.isEmpty()) {
            "No active goals set"
        } else {
            goalProgresses.joinToString(" • ") { it.progressText }
        }
}

interface DashboardRepository {
    fun getDashboardData(): Flow<DashboardData>
}

class DashboardRepositoryImpl(
    private val waterDao: WaterDao,
    private val sleepDao: SleepDao,
    private val activityDao: ActivityDao,
    private val wellnessGoalDao: WellnessGoalDao
) : DashboardRepository {

    override fun getDashboardData(): Flow<DashboardData> {
        return combine(
            waterDao.getAllWaterEntries(),
            sleepDao.getAllSleepEntries(),
            activityDao.getAllActivityEntries(),
            wellnessGoalDao.getAllGoals()
        ) { waterEntries, sleepEntries, activityEntries, goals ->
            val now = LocalDate.now()
            val startOfToday = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfToday = now.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val todayWater = waterEntries
                .filter { it.timestamp in startOfToday until endOfToday }
                .sumOf { it.amountMl }

            val todayActivity = activityEntries
                .filter { it.timestamp in startOfToday until endOfToday }
                .sumOf { it.durationMinutes }

            val latestSleep = sleepEntries.maxByOrNull { it.timestamp }?.durationMinutes

            DashboardData(
                todayWaterMl = todayWater,
                todayActivityMinutes = todayActivity,
                latestSleepDurationMinutes = latestSleep,
                goals = goals
            )
        }
    }
}
