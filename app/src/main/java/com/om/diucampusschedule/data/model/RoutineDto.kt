package com.om.diucampusschedule.data.model

import com.google.firebase.firestore.PropertyName
import com.om.diucampusschedule.data.local.entities.RoutineEntity
import com.om.diucampusschedule.data.local.entities.RoutineScheduleEntity
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.RoutineSchedule

data class RoutineItemDto(
    @PropertyName("day") val day: String = "",
    @PropertyName("time") val time: String = "",
    @PropertyName("room") val room: String = "",
    @PropertyName("courseCode") val courseCode: String = "",
    @PropertyName("teacherInitial") val teacherInitial: String = "",
    @PropertyName("batch") val batch: String = "",
    @PropertyName("section") val section: String = "",
    @PropertyName("labSection") val labSection: String? = null
)

data class RoutineScheduleDto(
    @PropertyName("id") val id: String = "",
    @PropertyName("semester") val semester: String = "",
    @PropertyName("department") val department: String = "",
    @PropertyName("effectiveFrom") val effectiveFrom: String = "",
    @PropertyName("schedule") val schedule: List<RoutineItemDto> = emptyList(),
    @PropertyName("version") val version: Long = 1,
    @PropertyName("createdAt") val createdAt: Long = 0L,
    @PropertyName("updatedAt") val updatedAt: Long = 0L
)

// Extension functions for mapping between DTOs and domain models
fun RoutineItemDto.toDomainModel(): RoutineItem {
    return RoutineItem(
        id = "",
        day = day,
        time = time,
        room = room,
        courseCode = courseCode,
        teacherInitial = teacherInitial,
        batch = batch,
        section = section,
        labSection = labSection,
        semester = "",
        department = "",
        effectiveFrom = ""
    )
}

fun RoutineItem.toDto(): RoutineItemDto {
    return RoutineItemDto(
        day = day,
        time = time,
        room = room,
        courseCode = courseCode,
        teacherInitial = teacherInitial,
        batch = batch,
        section = section,
        labSection = labSection
    )
}

fun RoutineScheduleDto.toDomainModel(): RoutineSchedule {
    return RoutineSchedule(
        id = id,
        semester = semester,
        department = department,
        effectiveFrom = effectiveFrom,
        schedule = schedule.map { item ->
            item.toDomainModel().copy(
                id = "${id}_${item.day}_${item.time}_${item.room}_${item.courseCode}",
                semester = semester,
                department = department,
                effectiveFrom = effectiveFrom
            )
        },
        version = version,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun RoutineSchedule.toDto(): RoutineScheduleDto {
    return RoutineScheduleDto(
        id = id,
        semester = semester,
        department = department,
        effectiveFrom = effectiveFrom,
        schedule = schedule.map { it.toDto() },
        version = version,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun RoutineScheduleDto.toEntity(): RoutineScheduleEntity {
    return RoutineScheduleEntity(
        id = id,
        semester = semester,
        department = department,
        effectiveFrom = effectiveFrom,
        version = version,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = true,
        lastSyncTime = System.currentTimeMillis()
    )
}

fun RoutineScheduleEntity.toDto(): RoutineScheduleDto {
    return RoutineScheduleDto(
        id = id,
        semester = semester,
        department = department,
        effectiveFrom = effectiveFrom,
        schedule = emptyList(), // Will be populated separately
        version = version,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun RoutineItemDto.toEntity(scheduleId: String, semester: String, department: String, effectiveFrom: String): RoutineEntity {
    return RoutineEntity(
        id = "${scheduleId}_${day}_${time}_${room}_${courseCode}",
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
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}

fun RoutineEntity.toDto(): RoutineItemDto {
    return RoutineItemDto(
        day = day,
        time = time,
        room = room,
        courseCode = courseCode,
        teacherInitial = teacherInitial,
        batch = batch,
        section = section,
        labSection = labSection
    )
}
