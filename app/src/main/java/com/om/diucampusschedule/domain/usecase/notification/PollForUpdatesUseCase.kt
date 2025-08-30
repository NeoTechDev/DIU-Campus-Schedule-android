package com.om.diucampusschedule.domain.usecase.notification

import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.core.notification.NotificationManager
import com.om.diucampusschedule.domain.usecase.routine.GetMaintenanceInfoUseCase
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PollForUpdatesUseCase @Inject constructor(
    private val getMaintenanceInfoUseCase: GetMaintenanceInfoUseCase,
    private val notificationManager: NotificationManager,
    private val logger: AppLogger
) {
    companion object {
        private const val TAG = "PollForUpdates"
        private const val POLL_INTERVAL_MS = 30_000L // 30 seconds
    }
    
    private var lastKnownVersion: Long = 0L
    private var lastUpdateType: String? = null
    
    suspend fun startPolling() {
        logger.info(TAG, "Starting version polling for push notification simulation")
        
        while (true) {
            try {
                getMaintenanceInfoUseCase().fold(
                    onSuccess = { maintenanceInfo ->
                        // Check if there's a version change (in a real scenario, we'd get this from metadata)
                        val currentTime = System.currentTimeMillis()
                        
                        // Simulate checking if update type changed (indicating new notification needed)
                        if (maintenanceInfo.updateType != lastUpdateType && maintenanceInfo.updateType != null) {
                            logger.info(TAG, "Detected update type change: ${lastUpdateType} -> ${maintenanceInfo.updateType}")
                            
                            sendLocalNotification(maintenanceInfo.updateType, maintenanceInfo.maintenanceMessage)
                            lastUpdateType = maintenanceInfo.updateType
                        }
                    },
                    onFailure = { error ->
                        logger.warning(TAG, "Failed to check for updates during polling", error)
                    }
                )
                
                delay(POLL_INTERVAL_MS)
            } catch (e: Exception) {
                logger.error(TAG, "Error during polling", e)
                delay(POLL_INTERVAL_MS)
            }
        }
    }
    
    private fun sendLocalNotification(updateType: String, message: String?) {
        val (title, body) = when (updateType) {
            "routine_deleted" -> "Schedule Update" to (message ?: "Your schedule is being updated. New schedule will be available soon.")
            "all_routines_deleted" -> "System Maintenance" to (message ?: "System maintenance in progress. New routines will be uploaded soon.")
            "maintenance_enabled" -> "System Maintenance" to (message ?: "System is under maintenance. Please check back later.")
            "semester_break" -> "Semester Break" to (message ?: "Semester break is in progress. New semester routine will be available soon.")
            "routine_uploaded" -> "New Schedule Available" to "Your class schedule has been updated with new information."
            "manual_trigger" -> "Schedule Refresh" to "Please refresh your app to see the latest schedule updates."
            "maintenance_disabled" -> "Service Restored" to "The app is back to normal. Your schedules are now available."
            else -> "Schedule Update" to (message ?: "Your schedule may have been updated.")
        }
        
        logger.info(TAG, "Sending local notification: $title - $body")
        
        notificationManager.showGeneralNotification(
            title = title,
            message = body,
            actionRoute = "routine"
        )
    }
}
