package com.om.diucampusschedule.data.sync

import android.Manifest
import androidx.annotation.RequiresPermission
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.core.notification.NotificationManager
import com.om.diucampusschedule.domain.usecase.routine.SyncRoutineUseCase
import com.om.diucampusschedule.domain.usecase.auth.GetCurrentUserUseCase
import com.om.diucampusschedule.data.repository.UniversalNotificationRepository
import com.om.diucampusschedule.domain.model.NotificationType
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
    lateinit var universalNotificationRepository: UniversalNotificationRepository

    @Inject
    lateinit var logger: AppLogger

    companion object {
        private const val TAG = "FCM"
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        logger.info(TAG, "FCM message received from: ${remoteMessage.from}")
        logger.debug(TAG, "FCM data: ${remoteMessage.data}")
        logger.debug(TAG, "FCM notification: ${remoteMessage.notification?.title} - ${remoteMessage.notification?.body}")

        try {
            // FACEBOOK-STYLE APPROACH: Save notification to Firestore immediately
            // This ensures instant delivery and real-time sync across all devices
            saveFCMNotificationToFirestore(remoteMessage)
            
            // Then handle UI updates based on app state
            handleUIBasedOnAppState(remoteMessage)
            
        } catch (e: Exception) {
            logger.error(TAG, "Error processing FCM message", e)
        }
    }
    
    /**
     * FACEBOOK-STYLE: Save FCM notification to Firestore for real-time sync
     * This replaces the old Room DB + WorkManager approach
     */
    private fun saveFCMNotificationToFirestore(remoteMessage: RemoteMessage) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    
                    // Extract notification data
                    val title = remoteMessage.notification?.title 
                        ?: remoteMessage.data["title"] 
                        ?: "DIU Campus Schedule"
                        
                    val message = remoteMessage.notification?.body 
                        ?: remoteMessage.data["message"] 
                        ?: remoteMessage.data["body"] 
                        ?: "You have a new notification"
                        
                    val messageType = remoteMessage.data["type"] ?: "general"
                    val actionRoute = remoteMessage.data["action_route"]
                    val department = remoteMessage.data["department"]
                    val imageUrl = remoteMessage.data["image_url"]
                    
                    // Determine notification type and admin status
                    val notificationType = when (messageType) {
                        "routine_update" -> NotificationType.ROUTINE_UPDATE
                        "maintenance" -> NotificationType.MAINTENANCE
                        "admin_message" -> NotificationType.ADMIN_MESSAGE
                        "class_reminder" -> NotificationType.GENERAL
                        else -> NotificationType.GENERAL
                    }
                    val isFromAdmin = (messageType == "admin_message" || messageType == "maintenance" || messageType == "routine_update")
                    
                    // Determine target audience based on message data
                    val targetAudience = when {
                        department != null -> "DEPARTMENT:$department"
                        messageType == "maintenance" || messageType == "admin_message" -> "ALL"
                        remoteMessage.data["target_user"] != null -> "USER:${remoteMessage.data["target_user"]}"
                        else -> "ALL"
                    }
                    
                    logger.info(TAG, "Saving FCM notification to Firestore - Type: $messageType, Title: $title, Target: $targetAudience")
                    
                    // Save to universal Firestore (storage efficient)
                    val result = universalNotificationRepository.insertNotificationFromFCM(
                        title = title,
                        message = message,
                        type = notificationType,
                        targetAudience = targetAudience,
                        actionRoute = actionRoute,
                        department = department,
                        imageUrl = imageUrl,
                        isFromAdmin = isFromAdmin,
                        createdBy = "FCM_SYSTEM"
                    )
                    
                    if (result.isSuccess) {
                        logger.info(TAG, "FCM notification saved to universal Firestore successfully with target: $targetAudience")
                    } else {
                        logger.error(TAG, "Failed to save FCM notification to universal Firestore", result.exceptionOrNull())
                    }
                } else {
                    logger.warning(TAG, "User not authenticated - FCM notification not saved")
                }
            } catch (e: Exception) {
                logger.error(TAG, "Error saving FCM notification to Firestore", e)
            }
        }
    }
    
    /**
     * PROFESSIONAL: Handle UI updates based on app lifecycle state
     * Similar to how WhatsApp, Telegram handle foreground/background notifications
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun handleUIBasedOnAppState(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title 
            ?: remoteMessage.data["title"] 
            ?: "DIU Campus Schedule"
            
        val message = remoteMessage.notification?.body 
            ?: remoteMessage.data["message"] 
            ?: remoteMessage.data["body"] 
            ?: "You have a new notification"
            
        val actionRoute = remoteMessage.data["action_route"]
        val messageType = remoteMessage.data["type"] ?: "general"
        
        // PROFESSIONAL: Check app lifecycle state
        if (isAppInForeground()) {
            logger.info(TAG, "📱 App in foreground - notification will update UI directly via real-time flows")
            // Don't show system notification when app is in foreground
            // The database update + event broadcasting will automatically trigger UI update
        } else {
            logger.info(TAG, "📴 App in background - showing system notification")
            // Show system notification when app is in background/killed
            when (messageType) {
                "routine_update" -> {
                    val department = remoteMessage.data["department"] ?: "Unknown"
                    notificationManager.showRoutineUpdateNotification(title, message, department)
                }
                else -> {
                    notificationManager.showGeneralNotification(title, message, actionRoute)
                }
            }
        }
        
        // Handle specific business logic (routine sync, etc.)
        handleSpecificNotificationLogic(remoteMessage)
    }
    
    /**
     * PROFESSIONAL: Detect if app is currently in foreground
     * Uses ProcessLifecycleOwner (Google's recommended approach)
     */
    private fun isAppInForeground(): Boolean {
        return try {
            val processLifecycleOwner = androidx.lifecycle.ProcessLifecycleOwner.get()
            val isInForeground = processLifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)
            logger.debug(TAG, "App foreground state: $isInForeground")
            isInForeground
        } catch (e: Exception) {
            logger.warning(TAG, "Could not determine app foreground state", e)
            false // Default to background behavior for safety
        }
    }
    
    /**
     * PROFESSIONAL: Handle specific notification logic (routine sync, etc.)
     */
    private fun handleSpecificNotificationLogic(remoteMessage: RemoteMessage) {
        val messageType = remoteMessage.data["type"] ?: "general"
        
        when (messageType) {
            "routine_update" -> {
                val department = remoteMessage.data["department"] ?: "Unknown"
                handleRoutineSync(department)
            }
            // Add other specific handlers as needed
        }
    }
    
    /**
     * PROFESSIONAL: Handle routine synchronization in background
     */
    private fun handleRoutineSync(department: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    
                    // Only sync if the update is for the user's department
                    if (user.department == department || department == "All") {
                        logger.info(TAG, "Starting routine sync for department: $department")
                        syncRoutineUseCase(department)
                        logger.info(TAG, "Routine sync completed for department: $department")
                    } else {
                        logger.debug(TAG, "Skipping sync - user department (${user.department}) doesn't match update department ($department)")
                    }
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to sync routine for department: $department", e)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        logger.info(TAG, "FCM token refreshed")
        
        // Store token locally and send to server
        storeTokenLocally(token)
        sendTokenToServer(token)
        
        // Subscribe to topics
        subscribeToTopics()
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
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    logger.info(TAG, "Sending FCM token to Firestore for user: ${user.id}")
                    
                    // Store FCM token in Firestore for push notifications
                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val tokenData = mapOf(
                        "token" to token,
                        "userId" to user.id,
                        "department" to user.department,
                        "batch" to user.batch,
                        "section" to user.section,
                        "enabled" to true,
                        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                        "deviceInfo" to mapOf(
                            "platform" to "android",
                            "appVersion" to getAppVersion()
                        )
                    )
                    
                    // Use user ID as document ID to ensure one token per user
                    firestore.collection("fcm_tokens")
                        .document(user.id)
                        .set(tokenData)
                        .addOnSuccessListener {
                            logger.info(TAG, "FCM token successfully stored in Firestore")
                        }
                        .addOnFailureListener { exception ->
                            logger.error(TAG, "Failed to store FCM token in Firestore", exception)
                        }
                } else {
                    logger.debug(TAG, "User not authenticated - will send token later")
                }
                
            } catch (e: Exception) {
                logger.error(TAG, "Failed to send FCM token to server", e)
            }
        }
    }
    
    /**
     * Subscribe to FCM topics for receiving notifications
     */
    fun subscribeToTopics() {
        val topics = listOf("general", "admin", "maintenance", "all")
        
        topics.forEach { topic ->
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        logger.info(TAG, "Successfully subscribed to topic: $topic")
                    } else {
                        logger.error(TAG, "Failed to subscribe to topic: $topic", task.exception)
                    }
                }
        }
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
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