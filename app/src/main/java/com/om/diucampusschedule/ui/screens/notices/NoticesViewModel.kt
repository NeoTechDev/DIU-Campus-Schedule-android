package com.om.diucampusschedule.ui.screens.notices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.diucampusschedule.core.events.NotificationEventBroadcaster
import com.om.diucampusschedule.core.events.NotificationEvent
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.data.repository.NoticeRepository
import com.om.diucampusschedule.data.repository.NotificationRepository
import com.om.diucampusschedule.domain.model.Notice
import com.om.diucampusschedule.domain.model.Notification
import com.om.diucampusschedule.domain.usecase.auth.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticesViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val notificationEventBroadcaster: NotificationEventBroadcaster,
    private val logger: AppLogger
) : ViewModel() {

    companion object {
        private const val TAG = "NoticesViewModel"
    }

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadNotificationCount = MutableStateFlow(0)
    val unreadNotificationCount: StateFlow<Int> = _unreadNotificationCount.asStateFlow()

    private val _isNotificationsLoading = MutableStateFlow(false)
    val isNotificationsLoading: StateFlow<Boolean> = _isNotificationsLoading.asStateFlow()

    // Notices state
    private val _notices = MutableStateFlow<List<Notice>>(emptyList())
    val notices: StateFlow<List<Notice>> = _notices
    private val _isNoticesLoading = MutableStateFlow(false)
    val isNoticesLoading: StateFlow<Boolean> = _isNoticesLoading

    init {
        // PROFESSIONAL APPROACH: Start real-time observation immediately
        startObservingNotifications()
        startObservingUnreadCount()
        startObservingNotificationEvents() // PROFESSIONAL: Listen to real-time events
        cleanupOldNotifications() // Automatically cleanup old notifications on startup
    }

    /**
     * PROFESSIONAL: Listen to real-time notification events
     * This provides instant feedback when notifications are received/read/deleted
     * Similar to how WhatsApp, Telegram handle real-time updates
     */
    private fun startObservingNotificationEvents() {
        viewModelScope.launch {
            try {
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    
                    logger.debug(TAG, "Starting real-time notification event observation for user: ${user.id}")
                    
                    notificationEventBroadcaster.notificationEvents
                        .collect { event ->
                            when (event) {
                                is NotificationEvent.NotificationReceived -> {
                                    if (event.userId == user.id) {
                                        logger.info(TAG, "🔔 Real-time notification event: ${event.title}")
                                        // The database flow will automatically update the UI
                                        // This provides immediate visual feedback
                                    }
                                }
                                is NotificationEvent.NotificationRead -> {
                                    if (event.userId == user.id) {
                                        logger.debug(TAG, "📖 Notification marked as read: ${event.notificationId}")
                                    }
                                }
                                is NotificationEvent.NotificationDeleted -> {
                                    if (event.userId == user.id) {
                                        logger.debug(TAG, "🗑️ Notification deleted: ${event.notificationId}")
                                    }
                                }
                            }
                        }
                } else {
                    logger.warning(TAG, "User not authenticated - cannot observe notification events")
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to observe notification events", e)
            }
        }
    }

    /**
     * PROFESSIONAL: Continuous real-time observation of notifications
     * This ensures instant UI updates when notifications arrive via FCM
     */
    private fun startObservingNotifications() {
        viewModelScope.launch {
            try {
                logger.debug(TAG, "Starting real-time notification observation...")
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    logger.debug(TAG, "Observing notifications for user: ${user.id}")
                    
                    _isNotificationsLoading.value = true
                    
                    // PROFESSIONAL: Continuous Flow observation (never terminates)
                    notificationRepository.getAllNotifications(user.id)
                        .catch { exception ->
                            logger.error(TAG, "Failed to observe notifications", exception)
                            _isNotificationsLoading.value = false
                        }
                        .collect { notifications ->
                            logger.info(TAG, "Real-time update: ${notifications.size} notifications for user: ${user.id}")
                            notifications.forEach { notification ->
                                logger.debug(TAG, "Notification: ${notification.title} - ${notification.type} - isRead: ${notification.isRead} - timestamp: ${notification.timestamp}")
                            }
                            _notifications.value = notifications
                            _isNotificationsLoading.value = false
                        }
                } else {
                    logger.warning(TAG, "User not authenticated - cannot observe notifications. Error: ${currentUser.exceptionOrNull()?.message}")
                    _isNotificationsLoading.value = false
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to start real-time observation", e)
                _isNotificationsLoading.value = false
            }
        }
    }

    /**
     * PROFESSIONAL: Real-time unread count observation
     */
    private fun startObservingUnreadCount() {
        viewModelScope.launch {
            try {
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    
                    notificationRepository.getUnreadCount(user.id)
                        .catch { exception ->
                            logger.error(TAG, "Failed to observe unread count", exception)
                        }
                        .collect { count ->
                            logger.debug(TAG, "Real-time unread count update: $count")
                            _unreadNotificationCount.value = count
                        }
                } else {
                    logger.warning(TAG, "User not authenticated - cannot observe unread count")
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to observe unread count", e)
            }
        }
    }

    fun loadNotifications() {
        // PROFESSIONAL: This now serves as a manual refresh trigger
        // Real-time updates come through startObservingNotifications()
        logger.debug(TAG, "Manual notification refresh triggered...")
    }

    fun loadUnreadCount() {
        // PROFESSIONAL: This now serves as a manual refresh trigger  
        // Real-time updates come through startObservingUnreadCount()
        logger.debug(TAG, "Manual unread count refresh triggered...")
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                val result = notificationRepository.markAsRead(notificationId)
                if (result.isSuccess) {
                    logger.debug(TAG, "Notification marked as read: $notificationId")
                    // The flow will automatically update the UI
                } else {
                    logger.error(TAG, "Failed to mark notification as read: $notificationId", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to mark notification as read: $notificationId", e)
            }
        }
    }

    fun cleanupOldNotifications() {
        viewModelScope.launch {
            try {
                val result = notificationRepository.cleanupOldNotifications(120) // Keep 4 months
                if (result.isSuccess) {
                    logger.debug(TAG, "Old notifications cleaned up")
                } else {
                    logger.error(TAG, "Failed to cleanup old notifications", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to cleanup old notifications", e)
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                val result = notificationRepository.deleteNotification(notificationId)
                if (result.isSuccess) {
                    logger.debug(TAG, "Notification deleted: $notificationId")
                } else {
                    logger.error(TAG, "Failed to delete notification: $notificationId", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to delete notification: $notificationId", e)
            }
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            try {
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    
                    val result = notificationRepository.deleteAllUserNotifications(user.id)
                    if (result.isSuccess) {
                        logger.debug(TAG, "All notifications deleted for user: ${user.id}")
                        loadNotifications() // Refresh the notifications list
                        loadUnreadCount() // Refresh the unread count
                    } else {
                        logger.error(TAG, "Failed to delete all notifications", result.exceptionOrNull())
                    }
                } else {
                    logger.error(TAG, "User not authenticated - cannot delete notifications")
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to delete all notifications", e)
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    
                    val result = notificationRepository.markAllAsRead(user.id)
                    if (result.isSuccess) {
                        logger.debug(TAG, "All notifications marked as read")
                        loadUnreadCount() // Refresh the unread count
                    } else {
                        logger.error(TAG, "Failed to mark all notifications as read", result.exceptionOrNull())
                    }
                } else {
                    logger.error(TAG, "User not authenticated - cannot mark notifications as read")
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to mark all notifications as read", e)
            }
        }
    }

    // Notices management
    @Inject
    lateinit var noticeRepository: NoticeRepository

    fun fetchNotices() {
        viewModelScope.launch {
            _isNoticesLoading.value = true
            try {
                val result = noticeRepository.fetchNotices()
                _notices.value = result
            } catch (e: Exception) {
                _notices.value = emptyList()
            } finally {
                _isNoticesLoading.value = false
            }
        }
    }

    // Test method to add sample notifications - remove in production
    fun addTestNotifications() {
        viewModelScope.launch {
            try {
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    
                    // Add test admin notification
                    notificationRepository.insertNotificationFromFCM(
                        title = "Test Admin Message",
                        message = "This is a test admin notification to verify the system works properly",
                        type = com.om.diucampusschedule.domain.model.NotificationType.ADMIN_MESSAGE,
                        userId = user.id,
                        department = "CSE",
                        isFromAdmin = true
                    )
                    
                    // Add test routine update notification
                    notificationRepository.insertNotificationFromFCM(
                        title = "Schedule Updated",
                        message = "Your class schedule has been updated for tomorrow",
                        type = com.om.diucampusschedule.domain.model.NotificationType.ROUTINE_UPDATE,
                        userId = user.id,
                        actionRoute = "routine",
                        isFromAdmin = false
                    )
                    
                    // Add test general notification
                    notificationRepository.insertNotificationFromFCM(
                        title = "General Notice",
                        message = "This is a general notification for testing purposes",
                        type = com.om.diucampusschedule.domain.model.NotificationType.GENERAL,
                        userId = user.id,
                        isFromAdmin = false
                    )
                    
                    logger.info(TAG, "Test notifications added successfully")
                } else {
                    logger.error(TAG, "User not authenticated - cannot add test notifications")
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to add test notifications", e)
            }
        }
    }
}