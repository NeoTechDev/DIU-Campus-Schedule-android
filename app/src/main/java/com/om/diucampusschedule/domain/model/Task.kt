package com.om.diucampusschedule.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ReminderOption {
    NONE,
    ON_TIME,
    THIRTY_MINUTES_BEFORE,
    BOTH
}

@Serializable
data class Task(
    val id: Long = 0,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val isCompleted: Boolean = false,
    val reminderOption: ReminderOption = ReminderOption.NONE,
    val reminderTimeOnTime: Long? = null,
    val reminderTime30MinBefore: Long? = null,
    val groupId: Long = 0 // Default to ungrouped (group ID 0)
)

@Serializable
data class TaskGroup(
    val id: Long = 0,
    val name: String,
    val color: Long = 0xFF6200EE // Default color in ARGB format
)