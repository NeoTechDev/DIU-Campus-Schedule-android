package com.om.diucampusschedule.data.sync

import android.Manifest
import android.content.SharedPreferences
import androidx.annotation.RequiresPermission
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.om.diucampusschedule.App
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.core.notification.NotificationManager
import com.om.diucampusschedule.data.repository.UniversalNotificationRepository
import com.om.diucampusschedule.domain.model.NotificationType
import com.om.diucampusschedule.domain.usecase.auth.GetCurrentUserUseCase
import com.om.diucampusschedule.domain.usecase.routine.SyncRoutineUseCase
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

    @Inject
    lateinit var workManager: WorkManager

    private val fcmPrefs: SharedPreferences by lazy {
        getSharedPreferences("fcm_prefs", MODE_PRIVATE)
    }

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
            saveFCMNotificationToFirestore(remoteMessage)

            // Then handle UI updates based on app state
            handleUIBasedOnAppState(remoteMessage)

        } catch (e: Exception) {
            logger.error(TAG, "Error processing FCM message", e)
        }
    }

    /**
     * FACEBOOK-STYLE: Save FCM notification to Firestore for real-time sync
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
     * --- THIS IS THE CORRECTED LOGIC ---
     * PROFESSIONAL: Handle UI updates based on app lifecycle state
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
        if (App.isAppInForeground) { // <-- USE THE FLAG FROM App.kt
            logger.info(TAG, "📱 App in foreground - Showing custom notification")

            // The system won't show the notification, so we must.
            when (messageType) {
                "routine_update" -> {
                    val department = remoteMessage.data["department"] ?: "Unknown"
                    notificationManager.showRoutineUpdateNotification(title, message, department)
                }
                else -> {
                    notificationManager.showGeneralNotification(title, message, actionRoute)
                }
            }
        } else {
            logger.info(TAG, "📴 App in background - System already handled notification. DOING NOTHING.")
            // DO NOT show a notification here.
            // The system has *already* displayed the one from the "notification" payload.
            // This fixes the duplicate bug.
        }

        // Always handle the business logic regardless of app state.
        handleSpecificNotificationLogic(remoteMessage)
    }

    /**
     * PROFESSIONAL: Handle specific notification logic (routine sync, etc.)
     */
    private fun handleSpecificNotificationLogic(remoteMessage: RemoteMessage) {
        val messageType = remoteMessage.data["type"] ?: "general"

        when (messageType) {
            "routine_update" -> {
                val department = remoteMessage.data["department"] ?: "Unknown"
                handleRoutineSync(department) // This now uses WorkManager
            }
            // Add other specific handlers as needed
        }
    }

    /**
     * --- THIS IS THE UPDATED FUNCTION ---
     * PROFESSIONAL: Handle routine synchronization by enqueuing a guaranteed Worker
     */
    private fun handleRoutineSync(department: String) {
        logger.info(TAG, "Enqueuing routine sync worker for: $department")

        // 1. Create input data to pass to the worker
        val inputData = Data.Builder()
            .putString(RoutineSyncWorker.KEY_DEPARTMENT, department)
            .putBoolean(RoutineSyncWorker.KEY_IS_SILENT_SYNC, true) // Tell worker NOT to notify
            .build()

        // 2. Define constraints (Don't sync if there's no network)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 3. Build the WorkRequest
        val syncWorkRequest = OneTimeWorkRequest.Builder(RoutineSyncWorker::class.java)
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()

        // 4. Enqueue the work
        // Use REPLACE to avoid stacking multiple syncs if messages arrive quickly
        workManager.enqueueUniqueWork(
            "fcm_routine_sync", // A unique name for this FCM-triggered job
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }

    /**
     * --- THIS IS THE UPDATED onNewToken ---
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        logger.info(TAG, "FCM token refreshed: $token")

        // 1. Store token locally
        storeTokenLocally(token)

        // 2. Send token to your server
        sendTokenToServer(token)

        // 3. Subscribe to topics (MOVED FROM App.kt)
        subscribeToTopics()
    }

    private fun storeTokenLocally(token: String) {
        try {
            fcmPrefs.edit().putString("fcm_token", token).apply()
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

                    val firestore = FirebaseFirestore.getInstance()
                    val tokenData = mapOf(
                        "token" to token,
                        "userId" to user.id,
                        "department" to user.department,
                        "batch" to user.batch,
                        "section" to user.section,
                        "enabled" to true,
                        "updatedAt" to FieldValue.serverTimestamp(),
                        "deviceInfo" to mapOf(
                            "platform" to "android",
                            "appVersion" to getAppVersion()
                        )
                    )

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
     * --- THIS FUNCTION IS MOVED HERE FROM App.kt ---
     * Subscribe to FCM topics for receiving notifications
     */
    private fun subscribeToTopics() {
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

    /**
     * Logic to re-sync if messages were dropped by FCM
     */
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