package com.om.diucampusschedule.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.om.diucampusschedule.MainActivity
import com.om.diucampusschedule.R
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.domain.model.RoutineItem
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: AppLogger
) {

    companion object {
        // Notification Channels
        const val CHANNEL_ROUTINE_UPDATES = "routine_updates"
        const val CHANNEL_CLASS_REMINDERS = "class_reminders"
        const val CHANNEL_GENERAL = "general"
        
        // Notification IDs
        const val NOTIFICATION_ID_ROUTINE_UPDATE = 1001
        const val NOTIFICATION_ID_CLASS_REMINDER = 2000 // Base ID, will add class ID
        const val NOTIFICATION_ID_GENERAL = 3000
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ROUTINE_UPDATES,
                    "Routine Updates",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications about schedule changes and updates"
                    enableVibration(true)
                    setShowBadge(true)
                },
                
                NotificationChannel(
                    CHANNEL_CLASS_REMINDERS,
                    "Class Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders for upcoming classes"
                    enableVibration(true)
                    setShowBadge(true)
                },
                
                NotificationChannel(
                    CHANNEL_GENERAL,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "General app notifications"
                    setShowBadge(false)
                }
            )

            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { channel ->
                systemNotificationManager.createNotificationChannel(channel)
            }
            
            logger.info("NotificationManager", "Created ${channels.size} notification channels")
        }
    }

    fun showRoutineUpdateNotification(
        title: String = "Routine Updated",
        message: String = "Your class schedule has been updated",
        department: String? = null
    ) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to", "routine")
                department?.let { putExtra("department", it) }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID_ROUTINE_UPDATE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ROUTINE_UPDATES)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .build()

            notificationManager.notify(NOTIFICATION_ID_ROUTINE_UPDATE, notification)
            
            logger.logUserAction("notification_shown", mapOf(
                "type" to "routine_update",
                "department" to (department ?: "unknown")
            ))
            
        } catch (e: Exception) {
            logger.error("NotificationManager", "Failed to show routine update notification", e)
        }
    }

    fun showClassReminderNotification(
        routineItem: RoutineItem,
        minutesUntilClass: Int
    ) {
        try {
            val title = "Class Reminder"
            val message = "${routineItem.courseCode} starts in $minutesUntilClass minutes\nRoom ${routineItem.room} • ${routineItem.teacherInitial}"

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to", "routine")
                putExtra("selected_day", routineItem.day)
            }

            val notificationId = NOTIFICATION_ID_CLASS_REMINDER + routineItem.hashCode()
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_CLASS_REMINDERS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .build()

            notificationManager.notify(notificationId, notification)
            
            logger.logUserAction("notification_shown", mapOf(
                "type" to "class_reminder",
                "course" to routineItem.courseCode,
                "minutes_until" to minutesUntilClass.toString()
            ))
            
        } catch (e: Exception) {
            logger.error("NotificationManager", "Failed to show class reminder notification", e)
        }
    }

    fun showGeneralNotification(
        title: String,
        message: String,
        actionRoute: String? = null
    ) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                actionRoute?.let { putExtra("navigate_to", it) }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID_GENERAL,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_GENERAL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .build()

            notificationManager.notify(NOTIFICATION_ID_GENERAL, notification)
            
            logger.logUserAction("notification_shown", mapOf(
                "type" to "general",
                "action_route" to (actionRoute ?: "none")
            ))
            
        } catch (e: Exception) {
            logger.error("NotificationManager", "Failed to show general notification", e)
        }
    }

    fun cancelNotification(notificationId: Int) {
        try {
            notificationManager.cancel(notificationId)
            logger.debug("NotificationManager", "Cancelled notification: $notificationId")
        } catch (e: Exception) {
            logger.error("NotificationManager", "Failed to cancel notification: $notificationId", e)
        }
    }

    fun cancelAllNotifications() {
        try {
            notificationManager.cancelAll()
            logger.debug("NotificationManager", "Cancelled all notifications")
        } catch (e: Exception) {
            logger.error("NotificationManager", "Failed to cancel all notifications", e)
        }
    }

    fun areNotificationsEnabled(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }

    fun isChannelEnabled(channelId: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = systemNotificationManager.getNotificationChannel(channelId)
            return channel?.importance != NotificationManager.IMPORTANCE_NONE
        }
        return areNotificationsEnabled()
    }
}
