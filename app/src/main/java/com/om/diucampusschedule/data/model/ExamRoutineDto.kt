package com.om.diucampusschedule.data.model

import com.google.firebase.firestore.PropertyName
import com.om.diucampusschedule.domain.model.ExamCourse
import com.om.diucampusschedule.domain.model.ExamDay
import com.om.diucampusschedule.domain.model.ExamRoutine
import com.om.diucampusschedule.domain.model.ExamSlot

data class ExamCourseDto(
    @PropertyName("code") val code: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("students") val students: Int = 0,
    @PropertyName("batch") val batch: String = "",
    @PropertyName("slot") val slot: String = ""
)

data class ExamDayDto(
    @PropertyName("day_number") val dayNumber: Int = 0,
    @PropertyName("date") val date: String = "",
    @PropertyName("weekday") val weekday: String = "",
    @PropertyName("courses") val courses: List<ExamCourseDto> = emptyList()
)

data class ExamRoutineDto(
    @PropertyName("id") val id: String = "",
    @PropertyName("university") val university: String = "",
    @PropertyName("department") val department: String = "",
    @PropertyName("exam_type") val examType: String = "",
    @PropertyName("semester") val semester: String = "",
    @PropertyName("start_date") val startDate: String = "",
    @PropertyName("end_date") val endDate: String = "",
    @PropertyName("slots") val slots: Map<String, String> = emptyMap(),
    @PropertyName("schedule") val schedule: List<ExamDayDto> = emptyList(),
    @PropertyName("version") val version: Long = 1,
    @PropertyName("createdAt") val createdAt: Long = 0L,
    @PropertyName("updatedAt") val updatedAt: Long = 0L
)

// Extension functions for mapping between DTOs and domain models
fun ExamCourseDto.toDomainModel(): ExamCourse {
    return ExamCourse(
        code = code,
        name = name,
        students = students,
        batch = batch,
        slot = slot
    )
}

fun ExamCourse.toDto(): ExamCourseDto {
    return ExamCourseDto(
        code = code,
        name = name,
        students = students,
        batch = batch,
        slot = slot
    )
}

fun ExamDayDto.toDomainModel(): ExamDay {
    return ExamDay(
        dayNumber = dayNumber,
        date = date,
        weekday = weekday,
        courses = courses.map { it.toDomainModel() }
    )
}

fun ExamDay.toDto(): ExamDayDto {
    return ExamDayDto(
        dayNumber = dayNumber,
        date = date,
        weekday = weekday,
        courses = courses.map { it.toDto() }
    )
}

fun ExamRoutineDto.toDomainModel(): ExamRoutine {
    return ExamRoutine(
        id = id,
        university = university,
        department = department,
        examType = examType,
        semester = semester,
        startDate = startDate,
        endDate = endDate,
        slots = slots,
        schedule = schedule.map { it.toDomainModel() },
        version = version,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ExamRoutine.toDto(): ExamRoutineDto {
    return ExamRoutineDto(
        id = id,
        university = university,
        department = department,
        examType = examType,
        semester = semester,
        startDate = startDate,
        endDate = endDate,
        slots = slots,
        schedule = schedule.map { it.toDto() },
        version = version,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}