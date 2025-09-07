package com.om.diucampusschedule.core.reminder

import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.core.service.CourseNameService
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.repository.RoutineRepository
import com.om.diucampusschedule.domain.usecase.auth.GetCurrentUserUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Professional service for managing class reminder scheduling.
 * Integrates with user's routine data and handles scheduling logic.
 */
@Singleton
class ClassReminderService @Inject constructor(
    private val alarmManager: ClassReminderAlarmManager,
    private val routineRepository: RoutineRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val courseNameService: CourseNameService,
    private val logger: AppLogger
) {
    
    companion object {
        private const val TAG = "ClassReminderService"
        private const val MAX_DAYS_AHEAD = 7 // Schedule reminders up to 7 days ahead
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Schedule reminders for today's classes
     */
    fun scheduleTodayReminders() {
        serviceScope.launch {
            try {
                val userResult = getCurrentUserUseCase.invoke()
                val user = userResult.getOrNull()
                if (user == null) {
                    logger.info(TAG, "No current user found, cannot schedule reminders")
                    return@launch
                }
                
                scheduleRemindersForDate(LocalDate.now(), user)
            } catch (e: Exception) {
                logger.error(TAG, "Failed to schedule today's reminders", e)
            }
        }
    }
    
    /**
     * Schedule reminders for a specific date
     */
    fun scheduleRemindersForDate(targetDate: LocalDate) {
        serviceScope.launch {
            try {
                val userResult = getCurrentUserUseCase.invoke()
                val user = userResult.getOrNull()
                if (user == null) {
                    logger.info(TAG, "No current user found, cannot schedule reminders for $targetDate")
                    return@launch
                }
                
                scheduleRemindersForDate(targetDate, user)
            } catch (e: Exception) {
                logger.error(TAG, "Failed to schedule reminders for $targetDate", e)
            }
        }
    }
    
    /**
     * Schedule reminders for the next week
     */
    fun scheduleWeeklyReminders() {
        serviceScope.launch {
            try {
                val userResult = getCurrentUserUseCase.invoke()
                val user = userResult.getOrNull()
                if (user == null) {
                    logger.info(TAG, "No current user found, cannot schedule weekly reminders")
                    return@launch
                }
                
                val today = LocalDate.now()
                var scheduledCount = 0
                
                // Schedule for today and next 6 days
                for (i in 0 until MAX_DAYS_AHEAD) {
                    val targetDate = today.plusDays(i.toLong())
                    val dayCount = scheduleRemindersForDate(targetDate, user)
                    scheduledCount += dayCount
                }
                
                logger.info(TAG, "Scheduled $scheduledCount total reminders for the next $MAX_DAYS_AHEAD days")
            } catch (e: Exception) {
                logger.error(TAG, "Failed to schedule weekly reminders", e)
            }
        }
    }
    
    /**
     * Cancel all scheduled reminders for a specific date
     */
    fun cancelRemindersForDate(targetDate: LocalDate) {
        serviceScope.launch {
            try {
                val userResult = getCurrentUserUseCase.invoke()
                val user = userResult.getOrNull()
                if (user == null) {
                    logger.info(TAG, "No current user found, cannot cancel reminders for $targetDate")
                    return@launch
                }
                
                // Get routine items for the date
                val routineItems = getRoutineItemsForDate(targetDate, user)
                if (routineItems.isNotEmpty()) {
                    val cancelCount = alarmManager.cancelAllRemindersForDate(routineItems, targetDate)
                    logger.info(TAG, "Cancelled $cancelCount reminders for $targetDate")
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to cancel reminders for $targetDate", e)
            }
        }
    }
    
    /**
     * Refresh reminders when routine data changes
     */
    fun refreshReminders() {
        serviceScope.launch {
            try {
                logger.info(TAG, "Refreshing class reminders due to routine data changes")
                
                // Cancel existing reminders and reschedule
                cancelAllFutureReminders()
                
                // Wait a bit to ensure cancellations are processed
                kotlinx.coroutines.delay(100)
                
                // Reschedule for the week
                scheduleWeeklyReminders()
            } catch (e: Exception) {
                logger.error(TAG, "Failed to refresh reminders", e)
            }
        }
    }
    
    /**
     * Cancel all future reminders
     */
    fun cancelAllFutureReminders() {
        serviceScope.launch {
            try {
                val userResult = getCurrentUserUseCase.invoke()
                val user = userResult.getOrNull()
                if (user == null) {
                    logger.info(TAG, "No current user found, cannot cancel reminders")
                    return@launch
                }
                
                val today = LocalDate.now()
                var cancelledCount = 0
                
                // Cancel for today and next days
                for (i in 0 until MAX_DAYS_AHEAD) {
                    val targetDate = today.plusDays(i.toLong())
                    val routineItems = getRoutineItemsForDate(targetDate, user)
                    if (routineItems.isNotEmpty()) {
                        val dayCount = alarmManager.cancelAllRemindersForDate(routineItems, targetDate)
                        cancelledCount += dayCount
                    }
                }
                
                logger.info(TAG, "Cancelled $cancelledCount future reminders")
            } catch (e: Exception) {
                logger.error(TAG, "Failed to cancel future reminders", e)
            }
        }
    }
    
    /**
     * Check if exact alarm permission is available
     */
    fun hasRequiredPermissions(): Boolean {
        return alarmManager.hasExactAlarmPermission()
    }
    
    /**
     * Internal method to schedule reminders for a specific date and user
     */
    private suspend fun scheduleRemindersForDate(targetDate: LocalDate, user: User): Int {
        try {
            val dayString = targetDate.format(DateTimeFormatter.ofPattern("EEEE"))
            logger.debug(TAG, "Scheduling reminders for $dayString ($targetDate)")
            
            // Get routine items for the specific date
            val routineItems = getRoutineItemsForDate(targetDate, user)
            
            if (routineItems.isEmpty()) {
                logger.debug(TAG, "No classes found for $dayString")
                return 0
            }
            
            // Enhance routine items with course names
            val enhancedRoutineItems = enhanceWithCourseNames(routineItems)
            
            // Schedule reminders
            val scheduledCount = alarmManager.scheduleMultipleClassReminders(enhancedRoutineItems, targetDate)
            
            logger.info(TAG, "Scheduled $scheduledCount out of ${enhancedRoutineItems.size} reminders for $dayString")
            return scheduledCount
            
        } catch (e: Exception) {
            logger.error(TAG, "Failed to schedule reminders for $targetDate", e)
            return 0
        }
    }
    
    /**
     * Get routine items for a specific date
     */
    private suspend fun getRoutineItemsForDate(targetDate: LocalDate, user: User): List<RoutineItem> {
        return try {
            val dayString = targetDate.format(DateTimeFormatter.ofPattern("EEEE"))
            val result = routineRepository.getRoutineForUserAndDay(user, dayString)
            
            result.getOrElse { 
                logger.info(TAG, "Failed to get routine for $dayString: ${it.message}")
                emptyList()
            }
        } catch (e: Exception) {
            logger.error(TAG, "Error getting routine items for $targetDate", e)
            emptyList()
        }
    }
    
    /**
     * Enhance routine items with actual course names for better notifications
     */
    private suspend fun enhanceWithCourseNames(routineItems: List<RoutineItem>): List<RoutineItem> {
        return routineItems.map { item ->
            try {
                val courseName = courseNameService.getCourseName(item.courseCode)
                if (!courseName.isNullOrBlank() && courseName != item.courseCode) {
                    // Store the course name in a way that can be used by the alarm manager
                    // We'll create a copy with course name in the department field temporarily
                    item.copy(department = courseName)
                } else {
                    item
                }
            } catch (e: Exception) {
                logger.info(TAG, "Could not get course name for ${item.courseCode}: ${e.message}")
                item
            }
        }
    }
}
