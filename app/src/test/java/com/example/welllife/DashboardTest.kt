package com.example.welllife

import com.example.welllife.data.dao.ActivityDao
import com.example.welllife.data.dao.SleepDao
import com.example.welllife.data.dao.WaterDao
import com.example.welllife.data.dao.WellnessGoalDao
import com.example.welllife.data.entity.ActivityEntry
import com.example.welllife.data.entity.SleepEntry
import com.example.welllife.data.entity.WaterEntry
import com.example.welllife.data.entity.WellnessGoal
import com.example.welllife.data.repository.DashboardRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeWaterDao : WaterDao {
    private val entries = MutableStateFlow<List<WaterEntry>>(emptyList())

    override fun getAllWaterEntries() = entries

    override fun getWaterEntryById(id: Long) = MutableStateFlow(entries.value.find { it.id == id })

    override suspend fun insertWaterEntry(entry: WaterEntry): Long {
        val newId = (entries.value.maxOfOrNull { it.id } ?: 0) + 1
        val newEntry = entry.copy(id = newId)
        entries.value = entries.value + newEntry
        return newId
    }

    override suspend fun updateWaterEntry(entry: WaterEntry) {
        entries.value = entries.value.map { if (it.id == entry.id) entry else it }
    }

    override suspend fun deleteWaterEntry(entry: WaterEntry) {
        entries.value = entries.value.filter { it.id != entry.id }
    }
}

class FakeSleepDao : SleepDao {
    private val entries = MutableStateFlow<List<SleepEntry>>(emptyList())
    override fun getAllSleepEntries() = entries
    override fun getSleepEntryById(id: Long) = MutableStateFlow(entries.value.find { it.id == id })
    override suspend fun insertSleepEntry(entry: SleepEntry): Long {
        val newId = (entries.value.maxOfOrNull { it.id } ?: 0) + 1
        entries.value = entries.value + entry.copy(id = newId)
        return newId
    }
    override suspend fun updateSleepEntry(entry: SleepEntry) {}
    override suspend fun deleteSleepEntry(entry: SleepEntry) {}
}

class FakeActivityDao : ActivityDao {
    private val entries = MutableStateFlow<List<ActivityEntry>>(emptyList())
    override fun getAllActivityEntries() = entries
    override fun getActivityEntryById(id: Long) = MutableStateFlow(entries.value.find { it.id == id })
    override suspend fun insertActivityEntry(entry: ActivityEntry): Long {
        val newId = (entries.value.maxOfOrNull { it.id } ?: 0) + 1
        entries.value = entries.value + entry.copy(id = newId)
        return newId
    }
    override suspend fun updateActivityEntry(entry: ActivityEntry) {}
    override suspend fun deleteActivityEntry(entry: ActivityEntry) {}
}

class FakeWellnessGoalDao : WellnessGoalDao {
    private val goals = MutableStateFlow<List<WellnessGoal>>(emptyList())
    override fun getAllGoals() = goals
    override fun getGoalByType(goalType: String) = MutableStateFlow(goals.value.find { it.goalType == goalType })
    override suspend fun insertGoal(goal: WellnessGoal): Long {
        val newId = (goals.value.maxOfOrNull { it.id } ?: 0) + 1
        goals.value = goals.value + goal.copy(id = newId)
        return newId
    }
    override suspend fun updateGoal(goal: WellnessGoal) {}
    override suspend fun deleteGoal(goal: WellnessGoal) {}
}

class DashboardTest {

    @Test
    fun testDashboardFlowWithTemporaryWaterEntry() = runBlocking {
        val waterDao = FakeWaterDao()
        val sleepDao = FakeSleepDao()
        val activityDao = FakeActivityDao()
        val goalDao = FakeWellnessGoalDao()

        val repository = DashboardRepositoryImpl(waterDao, sleepDao, activityDao, goalDao)

        // 1. Initial State: Empty Dashboard
        val initialData = repository.getDashboardData().first()
        assertEquals(0, initialData.todayWaterMl)
        assertEquals("0 mL today", initialData.waterSummary)
        assertEquals("No sleep recorded", initialData.sleepSummary)
        assertEquals("0 min today", initialData.activitySummary)

        // 2. Add temporary 250 mL water entry dated today
        val entry = WaterEntry(amountMl = 250, timestamp = System.currentTimeMillis())
        val entryId = waterDao.insertWaterEntry(entry)

        val updatedData = repository.getDashboardData().first()
        assertEquals(250, updatedData.todayWaterMl)
        assertEquals("250 mL today", updatedData.waterSummary)

        // 3. Remove the temporary entry
        waterDao.deleteWaterEntry(entry.copy(id = entryId))

        val resetData = repository.getDashboardData().first()
        assertEquals(0, resetData.todayWaterMl)
        assertEquals("0 mL today", resetData.waterSummary)
    }
}
