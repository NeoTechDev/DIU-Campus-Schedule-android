package com.om.diucampusschedule.core.reminder.debug

import android.content.Context
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.core.reminder.ClassReminderScheduler
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Debug utilities for testing class reminders in development
 */
@Singleton
class ClassReminderDebugHelper @Inject constructor(
    private val scheduler: ClassReminderScheduler,
    private val logger: AppLogger
) {
    
    companion object {
        private const val TAG = "ClassReminderDebug"
    }
    
    /**
     * Force schedule reminders for today (for testing)
     */
    fun forceScheduleTodayReminders() {
        logger.info(TAG, "Debug: Force scheduling today's reminders")
        scheduler.scheduleTodayReminders()
    }
    
    /**
     * Force schedule reminders for the week (for testing)
     */
    fun forceScheduleWeeklyReminders() {
        logger.info(TAG, "Debug: Force scheduling weekly reminders")
        scheduler.scheduleWeeklyReminders()
    }
    
    /**
     * Force refresh all reminders (for testing)
     */
    fun forceRefreshReminders() {
        logger.info(TAG, "Debug: Force refreshing all reminders")
        scheduler.refreshReminders()
    }
    
    /**
     * Check permission status (for testing)
     */
    fun checkPermissionStatus(): Boolean {
        val hasPermissions = scheduler.checkPermissions()
        logger.info(TAG, "Debug: Exact alarm permission status: $hasPermissions")
        return hasPermissions
    }
    
    /**
     * Cancel all reminders (for testing)
     */
    fun forceCancelAllReminders() {
        logger.info(TAG, "Debug: Force cancelling all reminders")
        scheduler.cancelAllReminders()
    }
}
