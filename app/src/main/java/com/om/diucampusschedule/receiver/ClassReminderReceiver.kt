package com.om.diucampusschedule.receiver

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
import com.om.diucampusschedule.data.preferences.NotificationPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Professional broadcast receiver for class reminder notifications.
 * Creates rich notifications matching the design shown in the image.
 */
@AndroidEntryPoint
class ClassReminderReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var notificationPreferences: NotificationPreferences
    
    companion object {
        // Channel for class reminders
        const val CHANNEL_ID = "class_reminder_channel"
        private const val CHANNEL_NAME = "Class Reminders"
        private const val CHANNEL_DESCRIPTION = "Notifications for upcoming classes"
        
        // Notification ID base
        const val NOTIFICATION_ID_BASE = 2000
        
        // Intent extras
        const val EXTRA_COURSE_CODE = "course_code"
        const val EXTRA_COURSE_NAME = "course_name"
        const val EXTRA_CLASS_TIME = "class_time"
        const val EXTRA_ROOM = "room"
        const val EXTRA_TEACHER_INITIAL = "teacher_initial"
        const val EXTRA_BATCH = "batch"
        const val EXTRA_SECTION = "section"
        const val EXTRA_CLASS_ID = "class_id"
        const val EXTRA_TARGET_DATE = "target_date"
        const val EXTRA_REMINDER_MINUTES = "reminder_minutes"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        // Check if notifications are enabled using the injected NotificationPreferences
        val notificationsEnabled = runBlocking {
            notificationPreferences.isClassRemindersEnabled.first()
        }
        
        if (!notificationsEnabled) {
            // Notifications are disabled, don't show
            return
        }
        
        // Extract class information
        val courseCode = intent.getStringExtra(EXTRA_COURSE_CODE) ?: return
        val courseName = intent.getStringExtra(EXTRA_COURSE_NAME) ?: courseCode
        val classTime = intent.getStringExtra(EXTRA_CLASS_TIME) ?: ""
        val room = intent.getStringExtra(EXTRA_ROOM) ?: ""
        val teacherInitial = intent.getStringExtra(EXTRA_TEACHER_INITIAL) ?: ""
        val batch = intent.getStringExtra(EXTRA_BATCH) ?: ""
        val section = intent.getStringExtra(EXTRA_SECTION) ?: ""
        val classId = intent.getStringExtra(EXTRA_CLASS_ID) ?: ""
        val reminderMinutes = intent.getLongExtra(EXTRA_REMINDER_MINUTES, 30)
        
        // Create notification channel
        createNotificationChannel(context)
        
        // Show notification
        showClassReminderNotification(
            context = context,
            courseCode = courseCode,
            courseName = courseName,
            classTime = classTime,
            room = room,
            teacherInitial = teacherInitial,
            batch = batch,
            section = section,
            classId = classId,
            reminderMinutes = reminderMinutes
        )
    }
    
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setVibrationPattern(longArrayOf(0, 500, 200, 500))
                setShowBadge(true)
                enableLights(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showClassReminderNotification(
        context: Context,
        courseCode: String,
        courseName: String,
        classTime: String,
        room: String,
        teacherInitial: String,
        batch: String,
        section: String,
        classId: String,
        reminderMinutes: Long
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create intent to open the app and navigate to today screen
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "today")
            putExtra("class_id", classId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            classId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Extract time components for the notification
        val timeComponents = extractTimeComponents(classTime)
        
        // Build notification exactly like the image
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_notification_logo) // Calendar icon like in image
            .setContentTitle("Upcoming Class: $courseName") // Main title: "Class Reminder: Object Oriented Design"
            .setContentText("Time: $timeComponents | Room: $room") // Subtitle showing time
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
        
        // Create rich notification content matching the image design exactly
        val expandedContent = buildExpandedNotificationContent(
            courseName = courseName,
            timeComponents = timeComponents,
            room = room,
            teacherInitial = teacherInitial,
            batch = batch,
            section = section
        )
        
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(expandedContent)
            .setBigContentTitle("Upcoming Class: $courseName") // "Upcoming Class: Object Oriented Design"
        
        notificationBuilder.setStyle(bigTextStyle)
        
        // Generate unique notification ID
        val notificationId = NOTIFICATION_ID_BASE + classId.hashCode()
        
        // Show notification
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
    
    /**
     * Extract time components from time string (e.g., "1:00 PM" from "1:00 PM - 2:30 PM")
     */
    private fun extractTimeComponents(timeString: String): String {
        return try {
            // Extract start time from "HH:MM AM/PM - HH:MM AM/PM" format
            val startTime = timeString.split(" - ")[0].trim()
            startTime
        } catch (e: Exception) {
            timeString // Return original if parsing fails
        }
    }
    
    /**
     * Build expanded notification content matching the image design exactly
     */
    private fun buildExpandedNotificationContent(
        courseName: String,
        timeComponents: String,
        room: String,
        teacherInitial: String,
        batch: String,
        section: String
    ): String {
        return buildString {
            // First line: Time with clock emoji (exactly like image)
            appendLine("🕐 Time: $timeComponents")
            
            // Second line: Room with building emoji (exactly like image)
            appendLine("🏢 Room: $room")
            
            // Third line: Teacher with person emoji (exactly like image)
            appendLine("👨‍🏫 Teacher: $teacherInitial")
            
            // Fourth line: Batch and Section with books emoji (exactly like image)
            append("📚 Batch: $batch | Section: $section")
        }
    }
}
