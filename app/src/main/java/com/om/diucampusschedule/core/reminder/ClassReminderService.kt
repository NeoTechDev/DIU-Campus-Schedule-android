package com.om.diucampusschedule.core.reminder

import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.core.service.CourseNameService
import com.om.diucampusschedule.data.preferences.NotificationPreferences
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.repository.RoutineRepository
import com.om.diucampusschedule.domain.usecase.auth.GetCurrentUserUseCase
import com.om.diucampusschedule.domain.usecase.exam.GetExamModeInfoUseCase
import com.om.diucampusschedule.domain.usecase.routine.GetMaintenanceInfoUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Professional service for managing class reminder scheduling.
 * Integrates with user's routine data and handles scheduling logic.
 * Now also checks exam mode status to disable class notifications during exams.
 * Now also checks maintenance mode status to disable class notifications during maintenance.
 */
@Singleton
class ClassReminderService @Inject constructor(
    private val alarmManager: ClassReminderAlarmManager,
    private val routineRepository: RoutineRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getExamModeInfoUseCase: GetExamModeInfoUseCase,
    private val getMaintenanceModeInfoUseCase: GetMaintenanceInfoUseCase,
    private val courseNameService: CourseNameService,
    private val notificationPreferences: NotificationPreferences,
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
                // Check if notifications are enabled
                val notificationsEnabled = notificationPreferences.isClassRemindersEnabled.first()
                if (!notificationsEnabled) {
                    logger.info(TAG, "Class reminders are disabled, skipping scheduling")
                    return@launch
                }
                
                // Check if exam mode is active
                val examModeInfo = getExamModeInfoUseCase().getOrNull()
                if (examModeInfo?.isExamMode == true) {
                    logger.info(TAG, "Exam mode is active, skipping class reminder scheduling")
                    return@launch
                }

                // Checks if maintenance mode is active
                val maintenanceModeInfo = getMaintenanceModeInfoUseCase().getOrNull()
                if (maintenanceModeInfo?.isMaintenanceMode == true || maintenanceModeInfo?.isSemesterBreak == true) {
                    logger.info(TAG, "Maintenance mode or semester break is active, skipping class reminder scheduling")
                    return@launch
                }
                
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
                // Check if notifications are enabled
                val notificationsEnabled = notificationPreferences.isClassRemindersEnabled.first()
                if (!notificationsEnabled) {
                    logger.info(TAG, "Class reminders are disabled, skipping scheduling for $targetDate")
                    return@launch
                }
                
                // Check if exam mode is active
                val examModeInfo = getExamModeInfoUseCase().getOrNull()
                if (examModeInfo?.isExamMode == true) {
                    logger.info(TAG, "Exam mode is active, skipping class reminder scheduling for $targetDate")
                    return@launch
                }

                // Checks if maintenance mode is active
                val maintenanceModeInfo = getMaintenanceModeInfoUseCase().getOrNull()
                if (maintenanceModeInfo?.isMaintenanceMode == true || maintenanceModeInfo?.isSemesterBreak == true) {
                    logger.info(TAG, "Maintenance mode or semester break is active, skipping class reminder scheduling for $targetDate")
                    return@launch
                }
                
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
                // Check if notifications are enabled
                val notificationsEnabled = notificationPreferences.isClassRemindersEnabled.first()
                if (!notificationsEnabled) {
                    logger.info(TAG, "Class reminders are disabled, skipping weekly scheduling")
                    return@launch
                }
                
                // Check if exam mode is active
                val examModeInfo = getExamModeInfoUseCase().getOrNull()
                if (examModeInfo?.isExamMode == true) {
                    logger.info(TAG, "Exam mode is active, skipping weekly class reminder scheduling")
                    return@launch
                }

                // Checks if maintenance mode is active
                val maintenanceModeInfo = getMaintenanceModeInfoUseCase().getOrNull()
                if (maintenanceModeInfo?.isMaintenanceMode == true || maintenanceModeInfo?.isSemesterBreak == true) {
                    logger.info(TAG, "Maintenance mode or semester break is active, skipping weekly class reminder scheduling")
                    return@launch
                }
                
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
                
                // Check if exam mode is active
                val examModeInfo = getExamModeInfoUseCase().getOrNull()
                if (examModeInfo?.isExamMode == true) {
                    logger.info(TAG, "Exam mode is active, cancelling all class reminders instead of refreshing")
                    cancelAllFutureReminders()
                    return@launch
                }

                // Checks if maintenance mode is active
                val maintenanceModeInfo = getMaintenanceModeInfoUseCase().getOrNull()
                if (maintenanceModeInfo?.isMaintenanceMode == true || maintenanceModeInfo?.isSemesterBreak == true) {
                    logger.info(TAG, "Maintenance mode or semester break is active, cancelling all class reminders instead of refreshing")
                    return@launch
                }
                
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
     * Handle exam mode changes
     * When exam mode is activated, cancel all class reminders
     * When exam mode is deactivated, reschedule class reminders
     */
    fun handleExamModeChange(isExamMode: Boolean) {
        serviceScope.launch {
            try {
                if (isExamMode) {
                    logger.info(TAG, "Exam mode activated - cancelling all class reminders")
                    cancelAllFutureReminders()
                } else {
                    logger.info(TAG, "Exam mode deactivated - rescheduling class reminders")
                    // Check if notifications are enabled before rescheduling
                    val notificationsEnabled = notificationPreferences.isClassRemindersEnabled.first()
                    if (notificationsEnabled) {
                        scheduleWeeklyReminders()
                    } else {
                        logger.info(TAG, "Class reminders are disabled, not rescheduling")
                    }
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to handle exam mode change", e)
            }
        }
    }

    /**
     * Handle maintenance mode and semester break changes
     * When maintenance mode or semester break is activated, cancel all class reminders
     * When maintenance mode or semester break is deactivated, reschedule class reminders
     */
    fun handleMaintenanceModeChange(isMaintenanceMode: Boolean, isSemesterBreak: Boolean) {
        serviceScope.launch {
            try {
                if (isMaintenanceMode || isSemesterBreak) {
                    logger.info(TAG, "Maintenance mode or semester break activated - cancelling all class reminders")
                    cancelAllFutureReminders()
                } else {
                    logger.info(TAG, "Maintenance mode or semester break deactivated - rescheduling class reminders")
                    // Check if notifications are enabled before rescheduling
                    val notificationsEnabled = notificationPreferences.isClassRemindersEnabled.first()
                    if (notificationsEnabled) {
                        scheduleWeeklyReminders()
                    } else {
                        logger.info(TAG, "Class reminders are disabled, not rescheduling")
                    }
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to handle maintenance mode change", e)
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
