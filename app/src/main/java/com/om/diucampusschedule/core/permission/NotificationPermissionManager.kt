package com.om.diucampusschedule.core.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.data.preferences.NotificationPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Professional notification permission manager that handles:
 * - Runtime notification permission for Android 13+
 * - Exact alarm permissions for task reminders
 * - Permission state tracking and caching
 * - Rationale and settings redirection logic
 */
@Singleton
class NotificationPermissionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationPreferences: NotificationPreferences,
    private val logger: AppLogger
) {

    companion object {
        private const val TAG = "NotificationPermissionManager"
    }

    private val _permissionState = MutableStateFlow(NotificationPermissionState())
    val permissionState: StateFlow<NotificationPermissionState> = _permissionState.asStateFlow()

    init {
        updatePermissionState()
        logger.info(TAG, "NotificationPermissionManager initialized")
    }

    /**
     * Checks if notification permissions are granted
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Notifications work by default on older versions
        }
    }

    /**
     * Checks if exact alarm permission is granted (for task reminders)
     */
    fun hasExactAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Exact alarms work by default on older versions
        }
    }

    /**
     * Checks if all notification-related permissions are granted
     */
    fun hasAllNotificationPermissions(): Boolean {
        return hasNotificationPermission() && hasExactAlarmPermission()
    }

    /**
     * Checks if notification permission has been requested before
     */
    fun hasNotificationPermissionBeenRequested(): Boolean {
        return notificationPreferences.hasNotificationPermissionBeenRequested()
    }

    /**
     * Checks if permission rationale should be shown
     */
    fun shouldShowNotificationPermissionRationale(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // If permission was requested before but denied, show rationale
            notificationPreferences.hasNotificationPermissionBeenRequested() && !hasNotificationPermission()
        } else {
            false
        }
    }

    /**
     * Marks that notification permission has been requested
     */
    suspend fun markNotificationPermissionRequested() {
        notificationPreferences.markNotificationPermissionRequested()
        updatePermissionState()
        logger.info(TAG, "Notification permission request marked")
    }

    /**
     * Updates the current permission state
     */
    fun updatePermissionState() {
        val currentState = NotificationPermissionState(
            hasNotificationPermission = hasNotificationPermission(),
            hasExactAlarmPermission = hasExactAlarmPermission(),
            hasAllPermissions = hasAllNotificationPermissions(),
            shouldShowRationale = shouldShowNotificationPermissionRationale(),
            hasBeenRequested = notificationPreferences.hasNotificationPermissionBeenRequested()
        )
        
        _permissionState.value = currentState
        
        logger.debug(TAG, "Permission state updated: $currentState")
    }



    /**
     * Checks if the app needs to request any permissions
     */
    fun needsPermissionRequest(): Boolean {
        val state = _permissionState.value
        val classReminders = notificationPreferences.isClassRemindersEnabledSync()
        val taskReminders = notificationPreferences.isTaskRemindersEnabledSync()
        
        return (!state.hasNotificationPermission && (classReminders || taskReminders)) ||
               (!state.hasExactAlarmPermission && taskReminders)
    }

    /**
     * Gets user-friendly permission explanation text
     */
    fun getPermissionRationaleText(): String {
        val needsNotification = !hasNotificationPermission()
        val needsExactAlarm = !hasExactAlarmPermission()
        
        return when {
            needsNotification && needsExactAlarm -> 
                "This app needs notification and alarm permissions to send you class reminders and task notifications on time."
            needsNotification -> 
                "This app needs notification permission to send you important class and routine updates."
            needsExactAlarm -> 
                "This app needs alarm permission to send you precise task reminders."
            else -> ""
        }
    }
}

/**
 * Data class representing the current notification permission state
 */
data class NotificationPermissionState(
    val hasNotificationPermission: Boolean = false,
    val hasExactAlarmPermission: Boolean = false,
    val hasAllPermissions: Boolean = false,
    val shouldShowRationale: Boolean = false,
    val hasBeenRequested: Boolean = false
)

/**
 * Data class representing permission requirements based on user preferences
 */
data class PermissionRequirements(
    val needsNotificationPermission: Boolean,
    val needsExactAlarmPermission: Boolean,
    val classRemindersEnabled: Boolean,
    val taskRemindersEnabled: Boolean
) {
    val needsAnyPermission: Boolean
        get() = needsNotificationPermission || needsExactAlarmPermission
}
