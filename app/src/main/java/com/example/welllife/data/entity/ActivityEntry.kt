package com.example.welllife.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_entries")
data class ActivityEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val activityType: String,
    val durationMinutes: Int,
    val caloriesBurned: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
