package com.om.diucampusschedule.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import com.om.diucampusschedule.domain.model.Notification
import com.om.diucampusschedule.domain.model.NotificationType
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Universal Notification DTO - Stored once for all users
 * Optimized for Firebase free tier storage efficiency
 */
data class UniversalNotificationDto(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "GENERAL",
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val actionRoute: String? = null,
    val department: String? = null,
    val imageUrl: String? = null,
    val isFromAdmin: Boolean = false,
    // Targeting - who should see this notification
    val targetAudience: String = "ALL", // "ALL", "DEPARTMENT:{dept}", "USER:{userId}"
    val priority: String = "NORMAL", // HIGH, NORMAL, LOW
    val expiryTimestamp: Timestamp? = null,
    val createdBy: String? = null, // Admin/system that created this
    @ServerTimestamp
    val createdAt: Timestamp? = null
)

/**
 * User-specific notification state - tracks read/hidden status per user
 */
data class UserNotificationStateDto(
    @DocumentId
    val notificationId: String = "",
    val isRead: Boolean = false,
    val isHidden: Boolean = false, // Soft delete
    val readTimestamp: Timestamp? = null,
    val hiddenTimestamp: Timestamp? = null,
    @ServerTimestamp
    val lastModified: Timestamp? = null
)

/**
 * Combined notification with user state for UI consumption
 */
data class NotificationWithStateDto(
    val notification: UniversalNotificationDto,
    val userState: UserNotificationStateDto?
) {
    fun toDomainModel(): Notification {
        return Notification(
            id = notification.id,
            title = notification.title,
            message = notification.message,
            type = try {
                NotificationType.valueOf(notification.type)
            } catch (e: Exception) {
                NotificationType.GENERAL
            },
            timestamp = notification.timestamp?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime() 
                ?: LocalDateTime.now(),
            isRead = userState?.isRead ?: false,
            actionRoute = notification.actionRoute,
            department = notification.department,
            imageUrl = notification.imageUrl,
            isFromAdmin = notification.isFromAdmin
        )
    }
}

// Extension functions
fun Notification.toUniversalDto(
    targetAudience: String = "ALL",
    createdBy: String? = null
): UniversalNotificationDto {
    return UniversalNotificationDto(
        id = id,
        title = title,
        message = message,
        type = type.name,
        timestamp = Timestamp.now(),
        actionRoute = actionRoute,
        department = department,
        imageUrl = imageUrl,
        isFromAdmin = isFromAdmin,
        targetAudience = targetAudience,
        priority = when (type) {
            NotificationType.MAINTENANCE -> "HIGH"
            NotificationType.ADMIN_MESSAGE -> "HIGH"
            NotificationType.ROUTINE_UPDATE -> "NORMAL"
            else -> "NORMAL"
        },
        createdBy = createdBy
    )
}

/**
 * Optimized Firestore paths for universal notification system
 */
object UniversalNotificationPaths {
    // Universal notifications collection
    const val NOTIFICATIONS_COLLECTION = "notifications"
    
    // User-specific states collection
    const val USER_STATES_COLLECTION = "userNotificationStates"
    const val STATES_SUBCOLLECTION = "states"
    
    fun getNotificationPath(notificationId: String): String {
        return "$NOTIFICATIONS_COLLECTION/$notificationId"
    }
    
    fun getUserStatesPath(userId: String): String {
        return "$USER_STATES_COLLECTION/$userId/$STATES_SUBCOLLECTION"
    }
    
    fun getUserNotificationStatePath(userId: String, notificationId: String): String {
        return "${getUserStatesPath(userId)}/$notificationId"
    }
}