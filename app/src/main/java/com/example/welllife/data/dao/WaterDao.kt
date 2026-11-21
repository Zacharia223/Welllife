package com.example.welllife.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.welllife.data.entity.WaterEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_entries ORDER BY timestamp DESC")
    fun getAllWaterEntries(): Flow<List<WaterEntry>>

    @Query("SELECT * FROM water_entries WHERE id = :id")
    fun getWaterEntryById(id: Long): Flow<WaterEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterEntry(entry: WaterEntry): Long

    @Update
    suspend fun updateWaterEntry(entry: WaterEntry)

    @Delete
    suspend fun deleteWaterEntry(entry: WaterEntry)
}
