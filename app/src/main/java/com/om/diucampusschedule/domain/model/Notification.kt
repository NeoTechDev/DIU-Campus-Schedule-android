package com.om.diucampusschedule.domain.model

import java.time.LocalDateTime

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: LocalDateTime,
    val isRead: Boolean = false,
    val actionRoute: String? = null,
    val department: String? = null,
    val imageUrl: String? = null,
    val isFromAdmin: Boolean = false
)

enum class NotificationType {
    ROUTINE_UPDATE,
    GENERAL,
    MAINTENANCE,
    ADMIN_MESSAGE
}