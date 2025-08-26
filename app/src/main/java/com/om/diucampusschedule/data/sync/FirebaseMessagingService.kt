package com.om.diucampusschedule.data.sync

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.core.notification.NotificationManager
import com.om.diucampusschedule.domain.usecase.routine.SyncRoutineUseCase
import com.om.diucampusschedule.domain.usecase.auth.GetCurrentUserUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var syncRoutineUseCase: SyncRoutineUseCase

    @Inject
    lateinit var getCurrentUserUseCase: GetCurrentUserUseCase

    @Inject
    lateinit var logger: AppLogger

    companion object {
        private const val TAG = "FCM"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        logger.info(TAG, "FCM message received from: ${remoteMessage.from}")

        try {
            // Check if message contains data payload
            if (remoteMessage.data.isNotEmpty()) {
                logger.debug(TAG, "Message data payload: ${remoteMessage.data}")
                handleDataMessage(remoteMessage.data)
            }

            // Check if message contains notification payload
            remoteMessage.notification?.let {
                logger.debug(TAG, "Message notification body: ${it.body}")
                handleNotificationMessage(it, remoteMessage.data)
            }
        } catch (e: Exception) {
            logger.error(TAG, "Error processing FCM message", e)
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val messageType = data["type"]
        
        logger.debug(TAG, "Handling data message of type: $messageType")
        
        when (messageType) {
            "routine_update" -> {
                handleRoutineUpdateMessage(data)
            }
            
            "class_reminder" -> {
                handleClassReminderMessage(data)
            }
            
            "general" -> {
                handleGeneralMessage(data)
            }
            
            else -> {
                logger.warning(TAG, "Unknown message type: $messageType")
                handleGeneralMessage(data)
            }
        }
    }

    private fun handleRoutineUpdateMessage(data: Map<String, String>) {
        val department = data["department"] ?: "Unknown"
        val title = data["title"] ?: "Routine Updated"
        val message = data["message"] ?: data["body"] ?: "Your class schedule has been updated"
        
        logger.info(TAG, "Processing routine update for department: $department")
        
        // Trigger background sync
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if user is authenticated before syncing
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    
                    // Only sync if the update is for the user's department
                    if (user.department == department || department == "All") {
                        syncRoutineUseCase(department)
                        logger.info(TAG, "Routine sync completed for department: $department")
                        
                        // Show notification after successful sync
                        notificationManager.showRoutineUpdateNotification(
                            title = title,
                            message = message,
                            department = department
                        )
                    } else {
                        logger.debug(TAG, "Skipping sync - user department (${user.department}) doesn't match update department ($department)")
                    }
                } else {
                    logger.warning(TAG, "User not authenticated - skipping routine sync")
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to sync routine for department: $department", e)
                
                // Still show notification even if sync fails
                notificationManager.showRoutineUpdateNotification(
                    title = title,
                    message = "Schedule update received. Please open the app to refresh.",
                    department = department
                )
            }
        }
    }

    private fun handleClassReminderMessage(data: Map<String, String>) {
        val title = data["title"] ?: "Class Reminder"
        val message = data["message"] ?: data["body"] ?: "You have an upcoming class"
        
        logger.info(TAG, "Processing class reminder")
        
        notificationManager.showGeneralNotification(
            title = title,
            message = message,
            actionRoute = "routine"
        )
    }

    private fun handleGeneralMessage(data: Map<String, String>) {
        val title = data["title"] ?: "DIU Campus Schedule"
        val message = data["message"] ?: data["body"] ?: "You have a new notification"
        val actionRoute = data["action_route"]
        
        logger.info(TAG, "Processing general message")
        
        notificationManager.showGeneralNotification(
            title = title,
            message = message,
            actionRoute = actionRoute
        )
    }

    private fun handleNotificationMessage(notification: RemoteMessage.Notification, data: Map<String, String>) {
        // If no specific data payload handling, show general notification
        val actionRoute = data["action_route"]
        
        logger.info(TAG, "Processing notification message")
        
        notificationManager.showGeneralNotification(
            title = notification.title ?: "DIU Campus Schedule",
            message = notification.body ?: "You have a new notification",
            actionRoute = actionRoute
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        logger.info(TAG, "FCM token refreshed")
        
        // Store token locally and send to server
        storeTokenLocally(token)
        sendTokenToServer(token)
    }

    private fun storeTokenLocally(token: String) {
        try {
            // Store in SharedPreferences for later use
            val prefs = getSharedPreferences("fcm_prefs", MODE_PRIVATE)
            prefs.edit().putString("fcm_token", token).apply()
            
            logger.debug(TAG, "FCM token stored locally")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to store FCM token locally", e)
        }
    }

    private fun sendTokenToServer(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // TODO: Implement token sending to your server
                // This would typically involve calling your backend API
                // to associate the token with the current user
                
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    logger.info(TAG, "Should send FCM token to server for user: ${user.id}")
                    
                    // Example API call:
                    // apiService.updateUserFCMToken(user.id, token)
                } else {
                    logger.debug(TAG, "User not authenticated - will send token later")
                }
                
            } catch (e: Exception) {
                logger.error(TAG, "Failed to send FCM token to server", e)
            }
        }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
        logger.warning(TAG, "FCM messages were deleted - may need to refresh data")
        
        // Trigger a general sync to ensure we have the latest data
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    syncRoutineUseCase(user.department)
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to sync after deleted messages", e)
            }
        }
    }
}