package com.om.diucampusschedule.domain.model

import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class ExamType(val displayName: String) {
    BATCH("Your Batch"),
    SELF_STUDY("Self Study"),
    RETAKE("Retake")
}

data class ExamCourse(
    val code: String,
    val name: String,
    val students: Int,
    val batch: String,
    val slot: String
)

data class ExamDay(
    val dayNumber: Int,
    val date: String,
    val weekday: String,
    val courses: List<ExamCourse>
)

data class ExamSlot(
    val id: String,
    val timeRange: String
) {
    val startTime: LocalTime?
        get() = try {
            val timeString = timeRange.split(" - ")[0].trim()
            parseTimeWithFallbacks(timeString)
        } catch (e: Exception) {
            null
        }
    
    val endTime: LocalTime?
        get() = try {
            val timeString = timeRange.split(" - ")[1].trim()
            parseTimeWithFallbacks(timeString)
        } catch (e: Exception) {
            null
        }
    
    private fun parseTimeWithFallbacks(timeString: String): LocalTime? {
        val formats = listOf(
            DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH),  // 9:00 am
            DateTimeFormatter.ofPattern("HH:mm"),                             // 09:00
            DateTimeFormatter.ofPattern("H:mm")                               // 9:00
        )
        
        for (format in formats) {
            try {
                return LocalTime.parse(timeString, format)
            } catch (e: Exception) {
                // Continue to next format
            }
        }
        
        return null
    }
}

data class ExamRoutine(
    val id: String = "",
    val university: String,
    val department: String,
    val examType: String,
    val semester: String,
    val startDate: String,
    val endDate: String,
    val message: String,
    val slots: Map<String, String>, // Slot ID to time range mapping
    val schedule: List<ExamDay>,
    val version: Long = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    
    // Get exam courses for a specific user (filtered by batch only, excluding Self Study)
    fun getExamCoursesForUser(user: User): List<ExamCourse> {
        android.util.Log.d("ExamRoutine", "Filtering exam courses for user: ${user.name}")
        android.util.Log.d("ExamRoutine", "User details - Batch: '${user.batch}', Department: '${user.department}'")
        
        val userBatch = user.batch?.trim()
        if (userBatch.isNullOrEmpty()) {
            android.util.Log.d("ExamRoutine", "User has no batch information, returning empty list")
            return emptyList()
        }
        
        val filtered = schedule.flatMap { day ->
            day.courses.filter { course ->
                val courseBatch = course.batch.trim()
                val matches = courseBatch == userBatch || courseBatch.isEmpty()
                android.util.Log.d("ExamRoutine", "Course ${course.code} (batch: '$courseBatch') matches user batch '$userBatch': $matches")
                matches
            }
        }
        
        android.util.Log.d("ExamRoutine", "Filtered result: ${filtered.size} exam courses match user")
        return filtered
    }
    
    // Get Self Study exam courses (available for everyone)
    fun getSelfStudyExamCourses(): List<ExamCourse> {
        android.util.Log.d("ExamRoutine", "Getting Self Study exam courses")
        
        val selfStudyCourses = schedule.flatMap { day ->
            day.courses.filter { course ->
                val courseBatch = course.batch.trim()
                val isSelfStudy = courseBatch.equals("Self Study", ignoreCase = true)
                android.util.Log.d("ExamRoutine", "Course ${course.code} (batch: '$courseBatch') is Self Study: $isSelfStudy")
                isSelfStudy
            }
        }
        
        android.util.Log.d("ExamRoutine", "Found ${selfStudyCourses.size} Self Study exam courses")
        return selfStudyCourses
    }
    
    // Get Retake exam courses (available for everyone)
    fun getRetakeExamCourses(): List<ExamCourse> {
        android.util.Log.d("ExamRoutine", "Getting Retake exam courses")
        
        val retakeCourses = schedule.flatMap { day ->
            day.courses.filter { course ->
                val courseBatch = course.batch.trim()
                val isRetake = courseBatch.equals("Retake", ignoreCase = true)
                android.util.Log.d("ExamRoutine", "Course ${course.code} (batch: '$courseBatch') is Retake: $isRetake")
                isRetake
            }
        }
        
        android.util.Log.d("ExamRoutine", "Found ${retakeCourses.size} Retake exam courses")
        return retakeCourses
    }
    
    // Get combined exam courses for user (batch + Self Study + Retake based on selection)
    fun getCombinedExamCoursesForUser(user: User, includeSelfStudy: Boolean = true, includeRetake: Boolean = true): List<ExamCourse> {
        val userCourses = getExamCoursesForUser(user)
        val combinedCourses = mutableListOf<ExamCourse>().apply {
            addAll(userCourses)
            if (includeSelfStudy) addAll(getSelfStudyExamCourses())
            if (includeRetake) addAll(getRetakeExamCourses())
        }
        return combinedCourses
    }
    
    // Get exam courses for a specific day and user
    fun getExamCoursesForDay(date: String, user: User): List<ExamCourse> {
        val userExamCourses = getExamCoursesForUser(user)
        val dayExamCourses = schedule.find { it.date == date }?.courses?.filter { course ->
            userExamCourses.any { it.code == course.code }
        } ?: emptyList()
        
        android.util.Log.d("ExamRoutine", "Day '$date' has ${dayExamCourses.size} exam courses for user ${user.name}")
        return dayExamCourses.sortedBy { getSlotStartTime(it.slot) }
    }
    
    // Get all dates that have exams for the user
    fun getActiveDatesForUser(user: User): List<String> {
        val userExamCourses = getExamCoursesForUser(user)
        val activeDates = schedule
            .filter { day ->
                day.courses.any { course ->
                    userExamCourses.any { it.code == course.code }
                }
            }
            .map { it.date }
            .distinct()
            .sorted()
        
        android.util.Log.d("ExamRoutine", "User ${user.name} has exams on ${activeDates.size} days: $activeDates")
        return activeDates
    }
    
    // Get slot time range by slot ID
    fun getSlotTimeRange(slotId: String): String? {
        return slots[slotId]
    }
    
    // Get slot start time for sorting
    private fun getSlotStartTime(slotId: String): LocalTime? {
        val timeRange = slots[slotId] ?: return null
        return try {
            val timeString = timeRange.split(" - ")[0].trim()
            LocalTime.parse(timeString, DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH))
        } catch (e: Exception) {
            null
        }
    }
    
    // Check if this exam routine matches a user
    fun matchesUser(user: User): Boolean {
        return user.department.equals(department, ignoreCase = true)
    }
}