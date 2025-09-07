package com.om.diucampusschedule.domain.usecase.notification

import com.om.diucampusschedule.core.reminder.ClassReminderScheduler
import com.om.diucampusschedule.data.preferences.NotificationPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for managing notification preferences and class reminder scheduling
 */
class ManageNotificationPreferencesUseCase @Inject constructor(
    private val notificationPreferences: NotificationPreferences,
    private val classReminderScheduler: ClassReminderScheduler
) {
    
    /**
     * Get the current state of class reminders
     */
    fun isClassRemindersEnabled(): Flow<Boolean> {
        return notificationPreferences.isClassRemindersEnabled
    }
    
    /**
     * Toggle class reminders on/off
     */
    suspend fun toggleClassReminders(enabled: Boolean) {
        notificationPreferences.setClassRemindersEnabled(enabled)
        
        if (enabled) {
            // Enable reminders - schedule for the next week
            classReminderScheduler.scheduleWeeklyReminders()
        } else {
            // Disable reminders - cancel all scheduled alarms
            classReminderScheduler.cancelAllFutureReminders()
        }
    }
    
    /**
     * Check if notification permission has been requested before
     */
    fun hasNotificationPermissionBeenRequested(): Flow<Boolean> {
        return notificationPreferences.hasNotificationPermissionBeenRequested
    }
    
    /**
     * Mark notification permission as requested
     */
    suspend fun markNotificationPermissionRequested() {
        notificationPreferences.markNotificationPermissionRequested()
    }
}
