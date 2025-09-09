package com.om.diucampusschedule.domain.model


import kotlinx.serialization.Serializable

@Serializable
data class MessageButton(
    val text: String = "",
    val action: String = ""
)

@Serializable
data class InAppMessage(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "dialog", // "dialog" or "banner"
    val isActive: Boolean = false,
    val targetScreen: String = "",
    val buttons: List<MessageButton> = emptyList(),
    val createdAt: Long = 0L, // Timestamp when message was created
    val expiresAt: Long = 0L, // Optional expiry timestamp (0 = never expires)
    val showToNewUsers: Boolean = true // Whether to show to users who install after this message
)