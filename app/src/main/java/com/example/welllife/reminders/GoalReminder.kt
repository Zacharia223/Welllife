package com.example.welllife.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.welllife.R
import java.util.concurrent.TimeUnit

private const val CHANNEL_ID = "wellness_goal_reminders"
private const val WORK_PREFIX = "wellness_goal_reminder_"

object GoalReminderPreferences {
    private const val PREFS = "goal_reminder_preferences"

    fun isEnabled(context: Context, goalType: String): Boolean = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getBoolean(goalType.key(), false)

    fun setEnabled(context: Context, goalType: String, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(goalType.key(), enabled).apply()
    }

    private fun String.key() = "enabled_${trim().lowercase()}"
}

object GoalReminderScheduler {
    fun schedule(context: Context, goalType: String, targetValue: Double, unit: String) {
        val request = PeriodicWorkRequestBuilder<GoalReminderWorker>(24, TimeUnit.HOURS)
            .setInputData(androidx.work.workDataOf("goalType" to goalType, "targetValue" to targetValue, "unit" to unit))
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_PREFIX + goalType.trim().lowercase(),
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel(context: Context, goalType: String) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_PREFIX + goalType.trim().lowercase())
    }
}

class GoalReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val goalType = inputData.getString("goalType") ?: return Result.failure()
        val target = inputData.getDouble("targetValue", 0.0)
        val unit = inputData.getString("unit") ?: ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            applicationContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return Result.success()

        val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Wellness goal reminders", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("WellLife goal reminder")
            .setContentText("Remember your $goalType goal: $target $unit")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(goalType.hashCode(), notification)
        return Result.success()
    }
}
