package com.om.diucampusschedule.receiver

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.om.diucampusschedule.MainActivity
import com.om.diucampusschedule.R

class ReminderBroadcastReceiver : BroadcastReceiver() {
    
    companion object {
        const val CHANNEL_ID = "task_reminder_channel"
        const val NOTIFICATION_ID_BASE = 1000
        
        // Intent extras
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_TITLE = "task_title"
        const val EXTRA_TASK_DESCRIPTION = "task_description"
        const val EXTRA_REMINDER_TYPE = "reminder_type"
        
        // Reminder types
        const val REMINDER_ON_TIME = "on_time"
        const val REMINDER_30_MIN_BEFORE = "30_min_before"
    }
    
    @SuppressLint("ScheduleExactAlarm")
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task Reminder"
        val taskDescription = intent.getStringExtra(EXTRA_TASK_DESCRIPTION) ?: ""
        val reminderType = intent.getStringExtra(EXTRA_REMINDER_TYPE) ?: REMINDER_ON_TIME
        
        if (taskId == -1L) return
        
        createNotificationChannel(context)
        showNotification(context, taskId, taskTitle, taskDescription, reminderType)
    }
    
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Task Reminders"
            val channelDescription = "Notifications for task reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                description = channelDescription
                enableVibration(true)
                setVibrationPattern(longArrayOf(0, 1000, 500, 1000))
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showNotification(
        context: Context,
        taskId: Long,
        taskTitle: String,
        taskDescription: String,
        reminderType: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create intent to open the app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "tasks")
            putExtra("task_id", taskId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationTitle = when (reminderType) {
            REMINDER_30_MIN_BEFORE -> "⏰ Task in 30 minutes"
            else -> "🔔 Task Time!"
        }
        
        val notificationText = if (taskDescription.isNotBlank()) {
            "$taskTitle - $taskDescription"
        } else {
            taskTitle
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // You'll need to add this icon
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .build()
        
        // Use unique notification ID based on task ID and reminder type
        val notificationId = NOTIFICATION_ID_BASE + taskId.toInt() + if (reminderType == REMINDER_30_MIN_BEFORE) 10000 else 0
        notificationManager.notify(notificationId, notification)
    }
}
