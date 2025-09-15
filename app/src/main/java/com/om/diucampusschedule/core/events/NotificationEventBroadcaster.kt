package com.om.diucampusschedule.core.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Professional notification event broadcasting system
 * Used by Google, Facebook, and other major apps for real-time UI updates
 */
@Singleton
class NotificationEventBroadcaster @Inject constructor() {
    
    private val _notificationEvents = MutableSharedFlow<NotificationEvent>(
        replay = 0, // Don't replay events to new subscribers
        extraBufferCapacity = 64 // Buffer for high-frequency events
    )
    
    val notificationEvents: SharedFlow<NotificationEvent> = _notificationEvents.asSharedFlow()
    
    /**
     * Broadcast that a new notification was received
     * Triggers real-time UI updates across the app
     */
    suspend fun broadcastNotificationReceived(
        notificationId: String? = null,
        title: String? = null,
        message: String? = null,
        type: String? = null,
        userId: String? = null
    ) {
        _notificationEvents.emit(
            NotificationEvent.NotificationReceived(
                notificationId = notificationId,
                title = title,
                message = message,
                type = type,
                userId = userId,
                timestamp = System.currentTimeMillis()
            )
        )
    }
    
    /**
     * Broadcast that a notification was read
     */
    suspend fun broadcastNotificationRead(notificationId: String, userId: String? = null) {
        _notificationEvents.emit(
            NotificationEvent.NotificationRead(
                notificationId = notificationId,
                userId = userId,
                timestamp = System.currentTimeMillis()
            )
        )
    }
    
    /**
     * Broadcast that a notification was deleted
     */
    suspend fun broadcastNotificationDeleted(notificationId: String, userId: String? = null) {
        _notificationEvents.emit(
            NotificationEvent.NotificationDeleted(
                notificationId = notificationId,
                userId = userId,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}

/**
 * Sealed class representing different notification events
 * Professional pattern used by major apps for type-safe event handling
 */
sealed class NotificationEvent {
    abstract val timestamp: Long
    
    data class NotificationReceived(
        val notificationId: String?,
        val title: String?,
        val message: String?,
        val type: String?,
        val userId: String?,
        override val timestamp: Long
    ) : NotificationEvent()
    
    data class NotificationRead(
        val notificationId: String,
        val userId: String?,
        override val timestamp: Long
    ) : NotificationEvent()
    
    data class NotificationDeleted(
        val notificationId: String,
        val userId: String?,
        override val timestamp: Long
    ) : NotificationEvent()
}
