package com.example.welllife.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.welllife.data.entity.WellnessGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface WellnessGoalDao {
    @Query("SELECT * FROM wellness_goals")
    fun getAllGoals(): Flow<List<WellnessGoal>>

    @Query("SELECT * FROM wellness_goals WHERE UPPER(goalType) = UPPER(:goalType) LIMIT 1")
    fun getGoalByType(goalType: String): Flow<WellnessGoal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: WellnessGoal): Long

    @Update
    suspend fun updateGoal(goal: WellnessGoal)

    @Delete
    suspend fun deleteGoal(goal: WellnessGoal)
}
