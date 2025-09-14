package com.om.diucampusschedule.data.repository

import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.data.local.dao.NotificationDao
import com.om.diucampusschedule.data.local.entities.NotificationEntity
import com.om.diucampusschedule.domain.model.Notification
import com.om.diucampusschedule.domain.model.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao,
    private val logger: AppLogger
) {
    companion object {
        private const val TAG = "NotificationRepository"
    }

    fun getAllNotifications(userId: String): Flow<List<Notification>> {
        return notificationDao.getAllNotifications(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun getAdminNotifications(userId: String): Flow<List<Notification>> {
        return notificationDao.getAdminNotifications(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun getNotificationsByType(userId: String, type: NotificationType): Flow<List<Notification>> {
        return notificationDao.getNotificationsByType(userId, type.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun getUnreadCount(userId: String): Flow<Int> {
        return notificationDao.getUnreadCount(userId)
    }

    fun getUnreadAdminCount(userId: String): Flow<Int> {
        return notificationDao.getUnreadAdminCount(userId)
    }

    suspend fun insertNotification(notification: Notification, userId: String): Result<Unit> {
        return try {
            val entity = notification.toEntity(userId)
            notificationDao.insertNotification(entity)
            logger.debug(TAG, "Notification inserted: ${notification.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(TAG, "Failed to insert notification: ${notification.id}", e)
            Result.failure(e)
        }
    }

    suspend fun insertNotificationFromFCM(
        title: String,
        message: String,
        type: NotificationType,
        userId: String,
        actionRoute: String? = null,
        department: String? = null,
        imageUrl: String? = null,
        isFromAdmin: Boolean = false
    ): Result<String> {
        return try {
            val notificationId = UUID.randomUUID().toString()
            val entity = NotificationEntity(
                id = notificationId,
                title = title,
                message = message,
                type = type,
                timestamp = LocalDateTime.now(),
                isRead = false,
                actionRoute = actionRoute,
                department = department,
                imageUrl = imageUrl,
                isFromAdmin = isFromAdmin,
                userId = userId
            )
            notificationDao.insertNotification(entity)
            logger.info(TAG, "FCM notification saved: $notificationId")
            Result.success(notificationId)
        } catch (e: Exception) {
            logger.error(TAG, "Failed to save FCM notification", e)
            Result.failure(e)
        }
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            notificationDao.markAsRead(notificationId)
            logger.debug(TAG, "Notification marked as read: $notificationId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(TAG, "Failed to mark notification as read: $notificationId", e)
            Result.failure(e)
        }
    }

    suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            notificationDao.markAllAsRead(userId)
            logger.debug(TAG, "All notifications marked as read for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(TAG, "Failed to mark all notifications as read for user: $userId", e)
            Result.failure(e)
        }
    }

    suspend fun markAllAdminAsRead(userId: String): Result<Unit> {
        return try {
            notificationDao.markAllAdminAsRead(userId)
            logger.debug(TAG, "All admin notifications marked as read for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(TAG, "Failed to mark all admin notifications as read for user: $userId", e)
            Result.failure(e)
        }
    }

    suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            notificationDao.deleteNotificationById(notificationId)
            logger.debug(TAG, "Notification deleted: $notificationId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(TAG, "Failed to delete notification: $notificationId", e)
            Result.failure(e)
        }
    }

    suspend fun deleteAllUserNotifications(userId: String): Result<Unit> {
        return try {
            notificationDao.deleteAllUserNotifications(userId)
            logger.debug(TAG, "All notifications deleted for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(TAG, "Failed to delete all notifications for user: $userId", e)
            Result.failure(e)
        }
    }

    suspend fun cleanupOldNotifications(daysToKeep: Int = 30): Result<Unit> {
        return try {
            val cutoffTime = LocalDateTime.now().minusDays(daysToKeep.toLong())
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            notificationDao.deleteOldNotifications(cutoffTime.format(formatter))
            logger.debug(TAG, "Old notifications cleaned up (older than $daysToKeep days)")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(TAG, "Failed to cleanup old notifications", e)
            Result.failure(e)
        }
    }

    private fun NotificationEntity.toDomainModel(): Notification {
        return Notification(
            id = id,
            title = title,
            message = message,
            type = type,
            timestamp = timestamp,
            isRead = isRead,
            actionRoute = actionRoute,
            department = department,
            imageUrl = imageUrl,
            isFromAdmin = isFromAdmin
        )
    }

    private fun Notification.toEntity(userId: String): NotificationEntity {
        return NotificationEntity(
            id = id,
            title = title,
            message = message,
            type = type,
            timestamp = timestamp,
            isRead = isRead,
            actionRoute = actionRoute,
            department = department,
            imageUrl = imageUrl,
            isFromAdmin = isFromAdmin,
            userId = userId
        )
    }
}