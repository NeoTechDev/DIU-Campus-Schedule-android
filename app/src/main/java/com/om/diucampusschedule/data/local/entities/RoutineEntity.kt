package com.om.diucampusschedule.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.om.diucampusschedule.domain.model.RoutineItem

@Entity(tableName = "routine_items")
data class RoutineEntity(
    @PrimaryKey
    val id: String,
    val day: String,
    val time: String,
    val room: String,
    val courseCode: String,
    val teacherInitial: String,
    val batch: String,
    val section: String,
    val labSection: String?,
    val semester: String,
    val department: String,
    val effectiveFrom: String,
    val scheduleId: String, // Reference to the schedule this item belongs to
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "routine_schedules")
data class RoutineScheduleEntity(
    @PrimaryKey
    val id: String,
    val semester: String,
    val department: String,
    val effectiveFrom: String,
    val version: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean = false, // Track sync status
    val lastSyncTime: Long = 0L
)

// Extension functions for mapping between entities and domain models
fun RoutineEntity.toDomainModel(): RoutineItem {
    return RoutineItem(
        id = id,
        day = day,
        time = time,
        room = room,
        courseCode = courseCode,
        teacherInitial = teacherInitial,
        batch = batch,
        section = section,
        labSection = labSection,
        semester = semester,
        department = department,
        effectiveFrom = effectiveFrom,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun RoutineItem.toEntity(scheduleId: String): RoutineEntity {
    return RoutineEntity(
        id = id.ifEmpty { "${scheduleId}_${day}_${time}_${room}_${courseCode}" },
        day = day,
        time = time,
        room = room,
        courseCode = courseCode,
        teacherInitial = teacherInitial,
        batch = batch,
        section = section,
        labSection = labSection,
        semester = semester,
        department = department,
        effectiveFrom = effectiveFrom,
        scheduleId = scheduleId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
