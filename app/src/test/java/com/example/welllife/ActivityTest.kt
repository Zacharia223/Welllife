package com.example.welllife

import com.example.welllife.data.dao.ActivityDao
import com.example.welllife.data.entity.ActivityEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class TestableActivityDao : ActivityDao {
    private val entries = MutableStateFlow<List<ActivityEntry>>(emptyList())
    override fun getAllActivityEntries() = entries
    override fun getActivityEntryById(id: Long) = MutableStateFlow(entries.value.find { it.id == id })
    override suspend fun insertActivityEntry(entry: ActivityEntry): Long {
        val newId = (entries.value.maxOfOrNull { it.id } ?: 0) + 1
        val newEntry = entry.copy(id = newId)
        entries.value = entries.value + newEntry
        return newId
    }
    override suspend fun updateActivityEntry(entry: ActivityEntry) {
        entries.value = entries.value.map { if (it.id == entry.id) entry else it }
    }
    override suspend fun deleteActivityEntry(entry: ActivityEntry) {
        entries.value = entries.value.filter { it.id != entry.id }
    }
}

class ActivityTest {

    @Test
    fun testActivityDaoOperations() = runBlocking {
        val dao = TestableActivityDao()

        // Initial empty
        val initialEntries = dao.getAllActivityEntries().first()
        assertEquals(0, initialEntries.size)

        // Insert entry
        val entry = ActivityEntry(activityType = "Running", durationMinutes = 30, caloriesBurned = 250)
        dao.insertActivityEntry(entry)

        val entriesAfterInsert = dao.getAllActivityEntries().first()
        assertEquals(1, entriesAfterInsert.size)
        assertEquals("Running", entriesAfterInsert[0].activityType)
        assertEquals(30, entriesAfterInsert[0].durationMinutes)
        assertEquals(250, entriesAfterInsert[0].caloriesBurned)

        // Update entry
        val updatedEntry = entriesAfterInsert[0].copy(activityType = "Cycling", durationMinutes = 45)
        dao.updateActivityEntry(updatedEntry)

        val entriesAfterUpdate = dao.getAllActivityEntries().first()
        assertEquals("Cycling", entriesAfterUpdate[0].activityType)
        assertEquals(45, entriesAfterUpdate[0].durationMinutes)

        // Delete entry
        dao.deleteActivityEntry(entriesAfterUpdate[0])
        val entriesAfterDelete = dao.getAllActivityEntries().first()
        assertEquals(0, entriesAfterDelete.size)
    }
}
