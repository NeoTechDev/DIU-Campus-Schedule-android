package com.om.diucampusschedule.ui.screens.notices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.data.repository.NoticeRepository
import com.om.diucampusschedule.data.repository.UniversalNotificationRepository
import com.om.diucampusschedule.domain.model.Notice
import com.om.diucampusschedule.domain.model.Notification
import com.om.diucampusschedule.domain.usecase.auth.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticesViewModel @Inject constructor(
    private val universalNotificationRepository: UniversalNotificationRepository, // New universal system
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

    // Notices state
    private val _notices = MutableStateFlow<List<Notice>>(emptyList())
    val notices: StateFlow<List<Notice>> = _notices
    private val _isNoticesLoading = MutableStateFlow(false)
    val isNoticesLoading: StateFlow<Boolean> = _isNoticesLoading

    init {
        // FACEBOOK-STYLE APPROACH: Start real-time observation immediately
        startObservingNotifications()
        startObservingUnreadCount()
        cleanupOldNotifications() // Automatically cleanup old notifications on startup
    }

    /**
     * FACEBOOK-STYLE: Continuous real-time observation of notifications
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
                    
                    // FACEBOOK-STYLE: Continuous Flow observation (never terminates)
                    universalNotificationRepository.getAllNotifications(user.id)
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
     * FACEBOOK-STYLE: Real-time unread count observation
     */
    private fun startObservingUnreadCount() {
        viewModelScope.launch {
            try {
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    
                    universalNotificationRepository.getUnreadCount(user.id)
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
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    
                    val result = universalNotificationRepository.markAsRead(user.id, notificationId)
                    if (result.isSuccess) {
                        logger.debug(TAG, "Notification marked as read: $notificationId")
                        // The flow will automatically update the UI
                    } else {
                        logger.error(TAG, "Failed to mark notification as read: $notificationId", result.exceptionOrNull())
                    }
                } else {
                    logger.error(TAG, "User not authenticated - cannot mark notification as read")
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to mark notification as read: $notificationId", e)
            }
        }
    }

    fun cleanupOldNotifications() {
        viewModelScope.launch {
            try {
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    
                    val result = universalNotificationRepository.cleanupOldNotifications(120) // Keep 4 months
                    if (result.isSuccess) {
                        logger.debug(TAG, "Old notifications cleaned up")
                    } else {
                        logger.error(TAG, "Failed to cleanup old notifications", result.exceptionOrNull())
                    }
                } else {
                    logger.error(TAG, "User not authenticated - cannot cleanup notifications")
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to cleanup old notifications", e)
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    
                    val result = universalNotificationRepository.hideNotification(user.id, notificationId)
                    if (result.isSuccess) {
                        logger.debug(TAG, "Notification hidden: $notificationId")
                    } else {
                        logger.error(TAG, "Failed to hide notification: $notificationId", result.exceptionOrNull())
                    }
                } else {
                    logger.error(TAG, "User not authenticated - cannot hide notification")
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to hide notification: $notificationId", e)
            }
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            try {
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    
                    val result = universalNotificationRepository.hideAllNotifications(user.id)
                    if (result.isSuccess) {
                        logger.debug(TAG, "All notifications hidden for user: ${user.id}")
                        // Real-time updates will automatically refresh the UI
                    } else {
                        logger.error(TAG, "Failed to hide all notifications", result.exceptionOrNull())
                    }
                } else {
                    logger.error(TAG, "User not authenticated - cannot hide notifications")
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to hide all notifications", e)
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    
                    val result = universalNotificationRepository.markAllAsRead(user.id)
                    if (result.isSuccess) {
                        logger.debug(TAG, "All notifications marked as read")
                        // Real-time updates will automatically refresh the UI
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
                    universalNotificationRepository.insertNotificationFromFCM(
                        title = "Test Admin Message",
                        message = "This is a test admin notification to verify the system works properly",
                        type = com.om.diucampusschedule.domain.model.NotificationType.ADMIN_MESSAGE,
                        targetAudience = "ALL",
                        department = "CSE",
                        isFromAdmin = true,
                        createdBy = user.id
                    )
                    
                    // Add test routine update notification
                    universalNotificationRepository.insertNotificationFromFCM(
                        title = "Schedule Updated",
                        message = "Your class schedule has been updated for tomorrow",
                        type = com.om.diucampusschedule.domain.model.NotificationType.ROUTINE_UPDATE,
                        targetAudience = "DEPARTMENT:CSE",
                        actionRoute = "routine",
                        isFromAdmin = false,
                        createdBy = user.id
                    )
                    
                    // Add test general notification
                    universalNotificationRepository.insertNotificationFromFCM(
                        title = "General Notice",
                        message = "This is a general notification for testing purposes",
                        type = com.om.diucampusschedule.domain.model.NotificationType.GENERAL,
                        targetAudience = "ALL",
                        isFromAdmin = false,
                        createdBy = user.id
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