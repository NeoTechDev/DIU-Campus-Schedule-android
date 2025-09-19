package com.om.diucampusschedule.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.data.remote.dto.NotificationWithStateDto
import com.om.diucampusschedule.data.remote.dto.UniversalNotificationDto
import com.om.diucampusschedule.data.remote.dto.UniversalNotificationPaths
import com.om.diucampusschedule.data.remote.dto.UserNotificationStateDto
import com.om.diucampusschedule.data.remote.dto.toUniversalDto
import com.om.diucampusschedule.domain.model.Notification
import com.om.diucampusschedule.domain.model.NotificationType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Universal Notification Repository - Optimized for Firebase Free Tier
 * 
 * Features:
 * - Stores notifications universally (single copy for all users)
 * - User-specific read/hidden states
 * - Soft delete functionality
 * - Real-time updates across all devices
 * - Efficient storage usage for Firebase free tier
 */
@Singleton
class UniversalNotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val logger: AppLogger
) {
    companion object {
        private const val TAG = "UniversalNotificationRepo"
        private const val NOTIFICATION_LIMIT = 100L
    }

    /**
     * Real-time listener for user notifications with their states
     * Uses recommended approach: separate flows combined with combineLatest
     */
    fun getAllNotifications(userId: String): Flow<List<Notification>> {
        logger.debug(TAG, "Starting real-time notification listener for user: $userId")
        
        val notificationsFlow = getUniversalNotificationsFlow()
        val userStatesFlow = getUserStatesFlow(userId)
        
        return combine(notificationsFlow, userStatesFlow) { notifications, userStates ->
            logger.debug(TAG, "Combining ${notifications.size} notifications with ${userStates.size} user states")
            
            val result = notifications
                .filter { notification ->
                    // Filter based on target audience
                    when {
                        notification.targetAudience == "ALL" -> true
                        notification.targetAudience.startsWith("DEPARTMENT:") -> {
                            val targetDept = notification.targetAudience.substringAfter("DEPARTMENT:")
                            notification.department == targetDept
                        }
                        notification.targetAudience.startsWith("USER:") -> {
                            val targetUserId = notification.targetAudience.substringAfter("USER:")
                            targetUserId == userId
                        }
                        else -> true
                    }
                }
                .filter { notification ->
                    // Filter out hidden notifications (soft delete)
                    val state = userStates[notification.id]
                    state?.isHidden != true
                }
                .map { notification ->
                    NotificationWithStateDto(
                        notification = notification,
                        userState = userStates[notification.id]
                    ).toDomainModel()
                }

            logger.debug(TAG, "Final combined result: ${result.size} notifications")
            result
        }
    }

    /**
     * Separate flow for universal notifications
     */
    private fun getUniversalNotificationsFlow(): Flow<List<UniversalNotificationDto>> = callbackFlow {
        logger.debug(TAG, "Starting universal notifications flow")
        
        val listener = firestore.collection(UniversalNotificationPaths.NOTIFICATIONS_COLLECTION)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(NOTIFICATION_LIMIT)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    logger.error(TAG, "Error in universal notifications listener", error)
                    // Don't close the flow on error - let it try to reconnect
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val notifications = snapshot.documents.mapNotNull { document ->
                        try {
                            document.toObject(UniversalNotificationDto::class.java)
                        } catch (e: Exception) {
                            logger.error(TAG, "Failed to parse notification: ${document.id}", e)
                            null
                        }
                    }
                    logger.debug(TAG, "Universal notifications updated: ${notifications.size}")
                    trySend(notifications)
                }
            }
        
        awaitClose {
            logger.debug(TAG, "Closing universal notifications listener")
            listener.remove()
        }
    }

    /**
     * Separate flow for user states
     */
    private fun getUserStatesFlow(userId: String): Flow<Map<String, UserNotificationStateDto>> = callbackFlow {
        logger.debug(TAG, "Starting user states flow for user: $userId")
        
        val listener = firestore.collection(UniversalNotificationPaths.getUserStatesPath(userId))
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    logger.error(TAG, "Error in user states listener", error)
                    // Don't close the flow on error - let it try to reconnect
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val userStates = snapshot.documents.mapNotNull { document ->
                        try {
                            val state = document.toObject(UserNotificationStateDto::class.java)
                            val finalState = state?.copy(notificationId = document.id)
                            finalState
                        } catch (e: Exception) {
                            logger.error(TAG, "Failed to parse user state: ${document.id}", e)
                            null
                        }
                    }.associateBy { it.notificationId }

                    logger.debug(TAG, "User states updated: ${userStates.size}")
                    trySend(userStates)
                }
            }
        
        awaitClose {
            logger.debug(TAG, "Closing user states listener for user: $userId")
            listener.remove()
        }
    }

    /**
     * Real-time unread count listener using recommended combine approach
     */
    fun getUnreadCount(userId: String): Flow<Int> {
        logger.debug(TAG, "Starting unread count listener for user: $userId")
        
        val notificationsFlow = getUniversalNotificationsFlow()
        val userStatesFlow = getUserStatesFlow(userId)
        
        return combine(notificationsFlow, userStatesFlow) { notifications, userStates ->
            val unreadCount = notifications
                .filter { notification ->
                    // Filter based on target audience
                    when {
                        notification.targetAudience == "ALL" -> true
                        notification.targetAudience.startsWith("DEPARTMENT:") -> {
                            val targetDept = notification.targetAudience.substringAfter("DEPARTMENT:")
                            notification.department == targetDept
                        }
                        notification.targetAudience.startsWith("USER:") -> {
                            val targetUserId = notification.targetAudience.substringAfter("USER:")
                            targetUserId == userId
                        }
                        else -> true
                    }
                }
                .filter { notification ->
                    val state = userStates[notification.id]
                    // Count as unread if not hidden and not read
                    state?.isHidden != true && state?.isRead != true
                }
                .size
            
            logger.debug(TAG, "Unread count calculated: $unreadCount (from ${notifications.size} notifications)")
            unreadCount
        }
    }

    /**
     * Save notification from FCM - stores universally
     */
    suspend fun insertNotificationFromFCM(
        title: String,
        message: String,
        type: NotificationType,
        targetAudience: String = "ALL",
        actionRoute: String? = null,
        department: String? = null,
        imageUrl: String? = null,
        isFromAdmin: Boolean = false,
        createdBy: String? = null
    ): Result<String> {
        return try {
            val notificationId = UUID.randomUUID().toString()
            
            val notification = Notification(
                id = notificationId,
                title = title,
                message = message,
                type = type,
                timestamp = java.time.LocalDateTime.now(),
                isRead = false,
                actionRoute = actionRoute,
                department = department,
                imageUrl = imageUrl,
                isFromAdmin = isFromAdmin
            )
            
            val universalDto = notification.toUniversalDto(targetAudience, createdBy)
            
            firestore.document(UniversalNotificationPaths.getNotificationPath(notificationId))
                .set(universalDto)
                .await()
            
            logger.info(TAG, "Universal notification saved: $notificationId with target: $targetAudience")
            Result.success(notificationId)
            
        } catch (e: Exception) {
            logger.error(TAG, "Failed to save universal notification", e)
            Result.failure(e)
        }
    }

    /**
     * Mark notification as read - creates/updates user state
     */
    suspend fun markAsRead(userId: String, notificationId: String): Result<Unit> {
        return try {
            val stateDoc = firestore.document(
                UniversalNotificationPaths.getUserNotificationStatePath(userId, notificationId)
            )
            
            val updates = mapOf(
                "notificationId" to notificationId,
                "isRead" to true,
                "readTimestamp" to com.google.firebase.Timestamp.now(),
                "lastModified" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            
            stateDoc.set(updates, com.google.firebase.firestore.SetOptions.merge()).await()
            
            logger.debug(TAG, "Notification marked as read: $notificationId for user: $userId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            logger.error(TAG, "Failed to mark notification as read: $notificationId", e)
            Result.failure(e)
        }
    }

    /**
     * Mark all notifications as read - bulk user state operation
     */
    suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            // Get all universal notifications that apply to this user
            val notifications = firestore.collection(UniversalNotificationPaths.NOTIFICATIONS_COLLECTION)
                .get()
                .await()
            
            val batch = firestore.batch()
            val timestamp = com.google.firebase.Timestamp.now()
            
            notifications.documents.forEach { document ->
                val notification = document.toObject(UniversalNotificationDto::class.java)
                if (notification != null) {
                    // Check if notification applies to this user
                    val appliesTo = when {
                        notification.targetAudience == "ALL" -> true
                        notification.targetAudience.startsWith("DEPARTMENT:") -> {
                            val targetDept = notification.targetAudience.substringAfter("DEPARTMENT:")
                            notification.department == targetDept
                        }
                        notification.targetAudience.startsWith("USER:") -> {
                            val targetUserId = notification.targetAudience.substringAfter("USER:")
                            targetUserId == userId
                        }
                        else -> true
                    }
                    
                    if (appliesTo) {
                        val statePath = UniversalNotificationPaths.getUserNotificationStatePath(userId, notification.id)
                        val stateDoc = firestore.document(statePath)
                        
                        val updates = mapOf(
                            "notificationId" to notification.id,
                            "isRead" to true,
                            "readTimestamp" to timestamp,
                            "lastModified" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                        )
                        
                        batch.set(stateDoc, updates, com.google.firebase.firestore.SetOptions.merge())
                    }
                }
            }

            batch.commit().await()
            
            logger.debug(TAG, "All notifications marked as read for user: $userId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            logger.error(TAG, "Failed to mark all notifications as read for user: $userId", e)
            Result.failure(e)
        }
    }

    /**
     * Hide notification - soft delete using user state
     */
    suspend fun hideNotification(userId: String, notificationId: String): Result<Unit> {
        return try {
            val stateDoc = firestore.document(
                UniversalNotificationPaths.getUserNotificationStatePath(userId, notificationId)
            )
            
            val updates = mapOf(
                "isHidden" to true,
                "hiddenTimestamp" to com.google.firebase.Timestamp.now(),
                "lastModified" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            
            stateDoc.set(updates, com.google.firebase.firestore.SetOptions.merge()).await()
            
            logger.debug(TAG, "Notification hidden: $notificationId for user: $userId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            logger.error(TAG, "Failed to hide notification: $notificationId", e)
            Result.failure(e)
        }
    }

    /**
     * Hide all notifications for user - soft delete all
     */
    suspend fun hideAllNotifications(userId: String): Result<Unit> {
        return try {
            // Get all universal notifications that apply to this user
            val notifications = firestore.collection(UniversalNotificationPaths.NOTIFICATIONS_COLLECTION)
                .get()
                .await()
            
            val batch = firestore.batch()
            val timestamp = com.google.firebase.Timestamp.now()
            
            notifications.documents.forEach { document ->
                val notification = document.toObject(UniversalNotificationDto::class.java)
                if (notification != null) {
                    val stateDoc = firestore.document(
                        UniversalNotificationPaths.getUserNotificationStatePath(userId, notification.id)
                    )
                    
                    val updates = mapOf(
                        "isHidden" to true,
                        "hiddenTimestamp" to timestamp,
                        "lastModified" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                    
                    batch.set(stateDoc, updates, com.google.firebase.firestore.SetOptions.merge())
                }
            }
            
            batch.commit().await()
            
            logger.debug(TAG, "All notifications hidden for user: $userId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            logger.error(TAG, "Failed to hide all notifications for user: $userId", e)
            Result.failure(e)
        }
    }

    /**
     * Cleanup old notifications - admin function to clean universal notifications
     */
    suspend fun cleanupOldNotifications(daysOld: Int = 120): Result<Unit> {
        return try {
            val cutoffTime = com.google.firebase.Timestamp(
                java.util.Date(System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L))
            )
            
            val oldNotifications = firestore.collection(UniversalNotificationPaths.NOTIFICATIONS_COLLECTION)
                .whereLessThan("timestamp", cutoffTime)
                .get()
                .await()
            
            val batch = firestore.batch()
            oldNotifications.documents.forEach { document ->
                batch.delete(document.reference)
            }
            
            batch.commit().await()
            
            logger.debug(TAG, "Cleaned up ${oldNotifications.size()} old universal notifications")
            Result.success(Unit)
            
        } catch (e: Exception) {
            logger.error(TAG, "Failed to cleanup old notifications", e)
            Result.failure(e)
        }
    }
}