package com.om.diucampusschedule.domain.model

import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class RoutineItem(
    val id: String = "",
    val day: String,
    val time: String,
    val room: String,
    val courseCode: String,
    val teacherInitial: String,
    val batch: String,
    val section: String,
    val labSection: String? = null,
    val semester: String = "",
    val department: String = "",
    val effectiveFrom: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Helper properties for better UI display
    val startTime: LocalTime?
        get() = try {
            val timeRange = time.split(" - ")[0].trim()
            LocalTime.parse(timeRange, DateTimeFormatter.ofPattern("hh:mm a"))
        } catch (e: Exception) {
            null
        }
    
    val endTime: LocalTime?
        get() = try {
            val timeRange = time.split(" - ")[1].trim()
            LocalTime.parse(timeRange, DateTimeFormatter.ofPattern("hh:mm a"))
        } catch (e: Exception) {
            null
        }
    
    val duration: String
        get() = try {
            val times = time.split(" - ")
            if (times.size == 2) {
                val start = LocalTime.parse(times[0].trim(), DateTimeFormatter.ofPattern("hh:mm a"))
                val end = LocalTime.parse(times[1].trim(), DateTimeFormatter.ofPattern("hh:mm a"))
                val minutes = java.time.Duration.between(start, end).toMinutes()
                when {
                    minutes < 60 -> "${minutes}m"
                    minutes % 60 == 0L -> "${minutes / 60}h"
                    else -> "${minutes / 60}h ${minutes % 60}m"
                }
            } else time
        } catch (e: Exception) {
            time
        }
    
    val dayOfWeek: Int
        get() = when (day.lowercase()) {
            "saturday" -> 1
            "sunday" -> 2
            "monday" -> 3
            "tuesday" -> 4
            "wednesday" -> 5
            "thursday" -> 6
            "friday" -> 7
            else -> 0
        }
    
    // Check if this routine item matches user's profile
    fun matchesUser(user: User): Boolean {
        return when (user.role) {
            UserRole.STUDENT -> {
                department.equals(user.department, ignoreCase = true) &&
                batch == user.batch &&
                (section == user.section || labSection == user.labSection)
            }
            UserRole.TEACHER -> {
                department.equals(user.department, ignoreCase = true) &&
                teacherInitial.equals(user.initial, ignoreCase = true)
            }
        }
    }
}

data class RoutineSchedule(
    val id: String = "",
    val semester: String,
    val department: String,
    val effectiveFrom: String,
    val schedule: List<RoutineItem>,
    val version: Long = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Get routine items for a specific user
    fun getRoutineForUser(user: User): List<RoutineItem> {
        return schedule.filter { it.matchesUser(user) }
    }
    
    // Get routine items for a specific day
    fun getRoutineForDay(day: String, user: User): List<RoutineItem> {
        return getRoutineForUser(user)
            .filter { it.day.equals(day, ignoreCase = true) }
            .sortedBy { it.startTime }
    }
    
    // Get all days that have classes for the user
    fun getActiveDaysForUser(user: User): List<String> {
        return getRoutineForUser(user)
            .map { it.day }
            .distinct()
            .sortedBy { day ->
                when (day.lowercase()) {
                    "saturday" -> 1
                    "sunday" -> 2
                    "monday" -> 3
                    "tuesday" -> 4
                    "wednesday" -> 5
                    "thursday" -> 6
                    "friday" -> 7
                    else -> 8
                }
            }
    }
}

// Enum for days of the week
enum class DayOfWeek(val displayName: String, val shortName: String) {
    SATURDAY("Saturday", "Sat"),
    SUNDAY("Sunday", "Sun"),
    MONDAY("Monday", "Mon"),
    TUESDAY("Tuesday", "Tue"),
    WEDNESDAY("Wednesday", "Wed"),
    THURSDAY("Thursday", "Thu"),
    FRIDAY("Friday", "Fri");
    
    companion object {
        fun fromString(day: String): DayOfWeek? {
            return values().find { it.displayName.equals(day, ignoreCase = true) }
        }
        
        fun getCurrentDay(): DayOfWeek {
            return when (java.time.LocalDate.now().dayOfWeek) {
                java.time.DayOfWeek.SATURDAY -> SATURDAY
                java.time.DayOfWeek.SUNDAY -> SUNDAY
                java.time.DayOfWeek.MONDAY -> MONDAY
                java.time.DayOfWeek.TUESDAY -> TUESDAY
                java.time.DayOfWeek.WEDNESDAY -> WEDNESDAY
                java.time.DayOfWeek.THURSDAY -> THURSDAY
                java.time.DayOfWeek.FRIDAY -> FRIDAY
            }
        }
    }
}
