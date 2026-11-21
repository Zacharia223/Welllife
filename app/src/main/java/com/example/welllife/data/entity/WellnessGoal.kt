package com.example.welllife.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wellness_goals",
    indices = [Index(value = ["goalType"], unique = true)]
)
data class WellnessGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalType: String,
    val targetValue: Double,
    val unit: String
)
