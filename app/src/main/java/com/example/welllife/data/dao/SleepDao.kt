package com.example.welllife.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.welllife.data.entity.SleepEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_entries ORDER BY timestamp DESC")
    fun getAllSleepEntries(): Flow<List<SleepEntry>>

    @Query("SELECT * FROM sleep_entries WHERE id = :id")
    fun getSleepEntryById(id: Long): Flow<SleepEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepEntry(entry: SleepEntry): Long

    @Update
    suspend fun updateSleepEntry(entry: SleepEntry)

    @Delete
    suspend fun deleteSleepEntry(entry: SleepEntry)
}
