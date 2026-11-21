package com.example.welllife.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.welllife.data.dao.ActivityDao
import com.example.welllife.data.dao.SleepDao
import com.example.welllife.data.dao.WaterDao
import com.example.welllife.data.dao.WellnessGoalDao
import com.example.welllife.data.entity.ActivityEntry
import com.example.welllife.data.entity.SleepEntry
import com.example.welllife.data.entity.WaterEntry
import com.example.welllife.data.entity.WellnessGoal

@Database(
    entities = [
        WaterEntry::class,
        SleepEntry::class,
        ActivityEntry::class,
        WellnessGoal::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WellLifeDatabase : RoomDatabase() {

    abstract fun waterDao(): WaterDao
    abstract fun sleepDao(): SleepDao
    abstract fun activityDao(): ActivityDao
    abstract fun wellnessGoalDao(): WellnessGoalDao

    companion object {
        @Volatile
        private var INSTANCE: WellLifeDatabase? = null

        fun getDatabase(context: Context): WellLifeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WellLifeDatabase::class.java,
                    "welllife_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
