package com.om.diucampusschedule.ui.screens.notices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.data.repository.NotificationRepository
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

    init {
        loadNotifications()
        loadUnreadCount()
        cleanupOldNotifications() // Automatically cleanup old notifications on startup
    }

    fun loadNotifications() {
        viewModelScope.launch {
            try {
                logger.debug(TAG, "Loading notifications...")
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    logger.debug(TAG, "Loading notifications for user: ${user.id}")
                    
                    _isNotificationsLoading.value = true
                    
                    notificationRepository.getAllNotifications(user.id)
                        .catch { exception ->
                            logger.error(TAG, "Failed to load notifications", exception)
                            _isNotificationsLoading.value = false
                        }
                        .onEach { notifications ->
                            logger.info(TAG, "Loaded ${notifications.size} notifications for user: ${user.id}")
                            notifications.forEach { notification ->
                                logger.debug(TAG, "Notification: ${notification.title} - ${notification.type} - isRead: ${notification.isRead}")
                            }
                            _notifications.value = notifications
                            _isNotificationsLoading.value = false
                        }
                        .collect {}
                } else {
                    logger.warning(TAG, "User not authenticated - cannot load notifications. Error: ${currentUser.exceptionOrNull()?.message}")
                    _isNotificationsLoading.value = false
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to load notifications", e)
                _isNotificationsLoading.value = false
            }
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            try {
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    
                    notificationRepository.getUnreadCount(user.id)
                        .catch { exception ->
                            logger.error(TAG, "Failed to load unread count", exception)
                        }
                        .collect { count ->
                            _unreadNotificationCount.value = count
                        }
                } else {
                    logger.warning(TAG, "User not authenticated - cannot load unread count")
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to load unread count", e)
            }
        }
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