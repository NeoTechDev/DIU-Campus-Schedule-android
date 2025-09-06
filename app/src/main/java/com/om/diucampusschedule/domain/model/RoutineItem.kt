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
    
    /**
     * Check if this routine item is on an off day
     */
    val isOnOffDay: Boolean
        get() = DayOfWeek.isOffDay(day)
    
    // Check if this routine item matches user's profile
    fun matchesUser(user: User): Boolean {
        val matches = when (user.role) {
            UserRole.STUDENT -> {
                // Students filter by: department + batch + section (including lab sections)
                val deptMatch = department.equals(user.department, ignoreCase = true)
                val batchMatch = batch.trim().equals(user.batch.trim(), ignoreCase = true)
                
                // Enhanced section matching logic with proper lab section filtering
                val userSection = user.section.trim().uppercase()
                val userLabSection = user.labSection.trim().uppercase()
                val itemSection = section.trim().uppercase()
                
                val sectionMatch = when {
                    // Exact section match (e.g., user: "J", item: "J")
                    itemSection == userSection -> true
                    
                    // Lab section specific matching
                    userLabSection.isNotEmpty() -> {
                        // If user has a specific lab section (e.g., "J1"), only show:
                        // 1. Classes for the main section "J" 
                        // 2. Classes specifically for their lab section "J1" (not J2, J3, etc.)
                        itemSection == userSection || itemSection == userLabSection
                    }
                    
                    // User has main section but no specific lab section
                    // Show all classes for their section including lab sections
                    userSection.length == 1 && itemSection.startsWith(userSection) && 
                    itemSection.length > 1 && itemSection.substring(1).all { it.isDigit() } -> true
                    
                    else -> false
                }
                
                android.util.Log.d("RoutineItem", "Student filtering for ${user.name}:")
                android.util.Log.d("RoutineItem", "  Course: $courseCode, Day: $day, Time: $time, Batch: $batch, Section: $section")
                android.util.Log.d("RoutineItem", "  Item dept: '$department' vs User dept: '${user.department}' -> $deptMatch")
                android.util.Log.d("RoutineItem", "  Item batch: '$batch' vs User batch: '${user.batch}' -> $batchMatch")
                android.util.Log.d("RoutineItem", "  Item section: '$section' vs User section: '${user.section}' -> $sectionMatch")
                android.util.Log.d("RoutineItem", "  Section match logic: userSection='$userSection', userLabSection='$userLabSection', itemSection='$itemSection'")
                android.util.Log.d("RoutineItem", "  FINAL MATCH: ${deptMatch && batchMatch && sectionMatch}")
                
                deptMatch && batchMatch && sectionMatch
            }
            UserRole.TEACHER -> {
                // Teachers filter by: department + initial
                val deptMatch = department.equals(user.department, ignoreCase = true)
                val initialMatch = teacherInitial.trim().equals(user.initial.trim(), ignoreCase = true)
                
                android.util.Log.d("RoutineItem", "Teacher filtering for ${user.name}:")
                android.util.Log.d("RoutineItem", "  Course: $courseCode, Day: $day, Time: $time, Teacher: $teacherInitial")
                android.util.Log.d("RoutineItem", "  Item dept: '$department' vs User dept: '${user.department}' -> $deptMatch")
                android.util.Log.d("RoutineItem", "  Item initial: '$teacherInitial' vs User initial: '${user.initial}' -> $initialMatch")
                android.util.Log.d("RoutineItem", "  FINAL MATCH: ${deptMatch && initialMatch}")
                
                deptMatch && initialMatch
            }
        }
        return matches
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
        android.util.Log.d("RoutineSchedule", "Filtering ${schedule.size} routine items for user: ${user.name}")
        android.util.Log.d("RoutineSchedule", "User details - Role: ${user.role}, Dept: '${user.department}', Batch: '${user.batch}', Section: '${user.section}', LabSection: '${user.labSection}', Initial: '${user.initial}'")
        
        val filtered = schedule.filter { it.matchesUser(user) }
        android.util.Log.d("RoutineSchedule", "Filtered result: ${filtered.size} items match user")
        
        return filtered
    }
    
    // Get routine items for a specific day
    fun getRoutineForDay(day: String, user: User): List<RoutineItem> {
        val userRoutine = getRoutineForUser(user)
        val dayRoutine = userRoutine.filter { it.day.equals(day, ignoreCase = true) }
        android.util.Log.d("RoutineSchedule", "Day '$day' has ${dayRoutine.size} classes for user ${user.name}")
        return dayRoutine.sortedBy { it.startTime }
    }
    
    // Get all days that have classes for the user (excluding off days like Friday)
    fun getActiveDaysForUser(user: User): List<String> {
        val userRoutine = getRoutineForUser(user)
        val activeDays = userRoutine
            .map { it.day }
            .distinct()
            .filter { day -> !DayOfWeek.isOffDay(day) } // Exclude off days like Friday
            .sortedBy { day ->
                when (day.lowercase()) {
                    "saturday" -> 1
                    "sunday" -> 2
                    "monday" -> 3
                    "tuesday" -> 4
                    "wednesday" -> 5
                    "thursday" -> 6
                    else -> 8
                }
            }
        android.util.Log.d("RoutineSchedule", "Active days for user ${user.name}: $activeDays (Friday excluded as off day)")
        return activeDays
    }
    
    /**
     * Get all possible days including off days for comprehensive display
     */
    fun getAllDaysForUser(user: User): List<String> {
        // Get all working days from enum
        val workingDays = DayOfWeek.getWorkingDays().map { it.displayName }
        
        // Add Friday as off day
        val allDays = workingDays + listOf("Friday")
        
        return allDays.sortedBy { day ->
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

// Enum for days of the week (excluding Friday - off day)
enum class DayOfWeek(val displayName: String, val shortName: String, val isOffDay: Boolean = false) {
    SATURDAY("Saturday", "Sat"),
    SUNDAY("Sunday", "Sun"),
    MONDAY("Monday", "Mon"),
    TUESDAY("Tuesday", "Tue"),
    WEDNESDAY("Wednesday", "Wed"),
    THURSDAY("Thursday", "Thu"),
    FRIDAY("Friday", "Fri", isOffDay = true); // Friday is an off day
    
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
        
        /**
         * Get all working days (excluding off days)
         */
        fun getWorkingDays(): List<DayOfWeek> {
            return values().filter { !it.isOffDay }
        }
        
        /**
         * Check if a given day is an off day
         */
        fun isOffDay(day: String): Boolean {
            return fromString(day)?.isOffDay == true
        }
    }
}
