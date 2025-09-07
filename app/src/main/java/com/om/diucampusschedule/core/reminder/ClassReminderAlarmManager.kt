package com.om.diucampusschedule.core.reminder

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.receiver.ClassReminderReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Professional class reminder alarm manager using setExactAndAllowWhileIdle() 
 * for precise reminders that work during Doze mode.
 * 
 * Features:
 * - Uses setExactAndAllowWhileIdle() for Doze mode compatibility
 * - Professional error handling and logging
 * - Efficient alarm scheduling and cancellation
 * - Supports multiple reminder types
 */
@Singleton
class ClassReminderAlarmManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: AppLogger
) {
    
    companion object {
        const val REMINDER_MINUTES_BEFORE = 30L
        const val REQUEST_CODE_BASE = 10000
        private const val TAG = "ClassReminderAlarmManager"
    }
    
    private val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)
    
    /**
     * Schedule a class reminder alarm
     */
    @SuppressLint("ScheduleExactAlarm")
    fun scheduleClassReminder(
        routineItem: RoutineItem,
        targetDate: LocalDate = LocalDate.now()
    ): Boolean {
        return try {
            val reminderTime = calculateReminderTime(routineItem, targetDate)
            if (reminderTime == null) {
                logger.info(TAG, "Could not calculate reminder time for class: ${routineItem.courseCode}")
                return false
            }
            
            // Don't schedule alarms for past times
            val now = System.currentTimeMillis()
            if (reminderTime <= now) {
                logger.debug(TAG, "Not scheduling past reminder for: ${routineItem.courseCode}")
                return false
            }
            
            val intent = createReminderIntent(routineItem, targetDate)
            val pendingIntent = createPendingIntent(routineItem, intent, targetDate)
            
            // Use setExactAndAllowWhileIdle for best reliability during Doze mode
            alarmManager?.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
            
            logger.info(TAG, "Scheduled class reminder for ${routineItem.courseCode} at ${
                java.time.Instant.ofEpochMilli(reminderTime)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            }")
            
            true
        } catch (e: SecurityException) {
            logger.error(TAG, "Permission denied for scheduling exact alarms", e)
            false
        } catch (e: Exception) {
            logger.error(TAG, "Failed to schedule class reminder for ${routineItem.courseCode}", e)
            false
        }
    }
    
    /**
     * Cancel a scheduled class reminder
     */
    fun cancelClassReminder(
        routineItem: RoutineItem,
        targetDate: LocalDate = LocalDate.now()
    ): Boolean {
        return try {
            val intent = createReminderIntent(routineItem, targetDate)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                generateRequestCode(routineItem, targetDate),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            
            pendingIntent?.let {
                alarmManager?.cancel(it)
                it.cancel()
                logger.debug(TAG, "Cancelled class reminder for ${routineItem.courseCode}")
                return true
            }
            
            false
        } catch (e: Exception) {
            logger.error(TAG, "Failed to cancel class reminder for ${routineItem.courseCode}", e)
            false
        }
    }
    
    /**
     * Schedule reminders for multiple classes
     */
    fun scheduleMultipleClassReminders(
        routineItems: List<RoutineItem>,
        targetDate: LocalDate = LocalDate.now()
    ): Int {
        var successCount = 0
        routineItems.forEach { routineItem ->
            if (scheduleClassReminder(routineItem, targetDate)) {
                successCount++
            }
        }
        
        logger.info(TAG, "Scheduled $successCount out of ${routineItems.size} class reminders for $targetDate")
        return successCount
    }
    
    /**
     * Cancel all reminders for a specific date
     */
    fun cancelAllRemindersForDate(
        routineItems: List<RoutineItem>,
        targetDate: LocalDate
    ): Int {
        var cancelCount = 0
        routineItems.forEach { routineItem ->
            if (cancelClassReminder(routineItem, targetDate)) {
                cancelCount++
            }
        }
        
        logger.info(TAG, "Cancelled $cancelCount class reminders for $targetDate")
        return cancelCount
    }
    
    /**
     * Check if exact alarm permission is available (Android 12+)
     */
    fun hasExactAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager?.canScheduleExactAlarms() == true
        } else {
            true // Permission not required before Android 12
        }
    }
    
    /**
     * Calculate the reminder time (30 minutes before class)
     */
    private fun calculateReminderTime(
        routineItem: RoutineItem,
        targetDate: LocalDate
    ): Long? {
        return try {
            val startTime = routineItem.startTime ?: return null
            
            // Create the class start datetime
            val classStartDateTime = LocalDateTime.of(targetDate, startTime)
            
            // Calculate reminder time (30 minutes before)
            val reminderDateTime = classStartDateTime.minusMinutes(REMINDER_MINUTES_BEFORE)
            
            // Convert to millis
            reminderDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            logger.error(TAG, "Error calculating reminder time for ${routineItem.courseCode}", e)
            null
        }
    }
    
    /**
     * Create the reminder intent with all necessary data
     */
    private fun createReminderIntent(
        routineItem: RoutineItem,
        targetDate: LocalDate
    ): Intent {
        // Use department field as enhanced course name if available, otherwise use courseCode
        val courseName = if (routineItem.department.isNotBlank() && 
                             routineItem.department != routineItem.courseCode &&
                             !routineItem.department.contains("Engineering", ignoreCase = true)) {
            routineItem.department
        } else {
            routineItem.courseCode
        }
        
        return Intent(context, ClassReminderReceiver::class.java).apply {
            putExtra(ClassReminderReceiver.EXTRA_COURSE_CODE, routineItem.courseCode)
            putExtra(ClassReminderReceiver.EXTRA_COURSE_NAME, courseName)
            putExtra(ClassReminderReceiver.EXTRA_CLASS_TIME, routineItem.time)
            putExtra(ClassReminderReceiver.EXTRA_ROOM, routineItem.room)
            putExtra(ClassReminderReceiver.EXTRA_TEACHER_INITIAL, routineItem.teacherInitial)
            putExtra(ClassReminderReceiver.EXTRA_BATCH, routineItem.batch)
            putExtra(ClassReminderReceiver.EXTRA_SECTION, routineItem.section)
            putExtra(ClassReminderReceiver.EXTRA_CLASS_ID, routineItem.id)
            putExtra(ClassReminderReceiver.EXTRA_TARGET_DATE, targetDate.toString())
            putExtra(ClassReminderReceiver.EXTRA_REMINDER_MINUTES, REMINDER_MINUTES_BEFORE)
        }
    }
    
    /**
     * Create PendingIntent with unique request code
     */
    private fun createPendingIntent(
        routineItem: RoutineItem,
        intent: Intent,
        targetDate: LocalDate
    ): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            generateRequestCode(routineItem, targetDate),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * Generate unique request code for each class reminder
     */
    private fun generateRequestCode(
        routineItem: RoutineItem,
        targetDate: LocalDate
    ): Int {
        // Use a combination of date, course code, and time to create unique code
        val dateCode = targetDate.toEpochDay().toInt()
        val courseCodeHash = routineItem.courseCode.hashCode()
        val timeHash = routineItem.time.hashCode()
        
        return REQUEST_CODE_BASE + (dateCode + courseCodeHash + timeHash).let { 
            // Ensure it's positive and within reasonable range
            kotlin.math.abs(it) % 100000
        }
    }
}
