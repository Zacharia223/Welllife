package com.example.welllife.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.welllife.data.entity.ActivityEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity_entries ORDER BY timestamp DESC")
    fun getAllActivityEntries(): Flow<List<ActivityEntry>>

    @Query("SELECT * FROM activity_entries WHERE id = :id")
    fun getActivityEntryById(id: Long): Flow<ActivityEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityEntry(entry: ActivityEntry): Long

    @Update
    suspend fun updateActivityEntry(entry: ActivityEntry)

    @Delete
    suspend fun deleteActivityEntry(entry: ActivityEntry)
}
