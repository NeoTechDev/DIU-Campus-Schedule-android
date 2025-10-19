package com.om.diucampusschedule.core.reminder

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.usecase.auth.GetCurrentUserUseCase
import com.om.diucampusschedule.domain.usecase.exam.GetExamModeInfoUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Professional scheduler that automatically manages class reminders.
 * Integrates seamlessly with the app lifecycle, user authentication, and exam mode.
 * 
 * Features:
 * - Automatic scheduling when user logs in
 * - Lifecycle-aware operations
 * - Handles routine data changes
 * - Handles exam mode changes
 * - Professional error handling and logging
 */
@Singleton
class ClassReminderScheduler @Inject constructor(
    private val reminderService: ClassReminderService,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getExamModeInfoUseCase: GetExamModeInfoUseCase,
    private val logger: AppLogger
) : DefaultLifecycleObserver {
    
    companion object {
        private const val TAG = "ClassReminderScheduler"
    }
    
    private val schedulerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentUser: User? = null
    private var isInitialized = false
    private var currentExamMode: Boolean = false
    
    /**
     * Initialize the scheduler
     */
    fun initialize() {
        if (isInitialized) {
            logger.debug(TAG, "Scheduler already initialized")
            return
        }
        
        try {
            // Register lifecycle observer
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
            
            // Observe user changes
            observeUserChanges()
            
            // Observe exam mode changes
            observeExamModeChanges()
            
            isInitialized = true
            logger.info(TAG, "Class reminder scheduler initialized successfully")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to initialize class reminder scheduler", e)
        }
    }
    
    /**
     * Schedule reminders immediately for today
     */
    fun scheduleTodayReminders() {
        schedulerScope.launch {
            try {
                logger.info(TAG, "Manually scheduling today's reminders")
                reminderService.scheduleTodayReminders()
            } catch (e: Exception) {
                logger.error(TAG, "Failed to manually schedule today's reminders", e)
            }
        }
    }
    
    /**
     * Schedule reminders for the upcoming week
     */
    fun scheduleWeeklyReminders() {
        schedulerScope.launch {
            try {
                logger.info(TAG, "Manually scheduling weekly reminders")
                reminderService.scheduleWeeklyReminders()
            } catch (e: Exception) {
                logger.error(TAG, "Failed to manually schedule weekly reminders", e)
            }
        }
    }
    
    /**
     * Refresh reminders when routine data changes
     */
    fun refreshReminders() {
        schedulerScope.launch {
            try {
                logger.info(TAG, "Refreshing reminders due to data changes")
                reminderService.refreshReminders()
            } catch (e: Exception) {
                logger.error(TAG, "Failed to refresh reminders", e)
            }
        }
    }
    
    /**
     * Cancel all scheduled reminders
     */
    fun cancelAllReminders() {
        schedulerScope.launch {
            try {
                logger.info(TAG, "Cancelling all scheduled reminders")
                reminderService.cancelAllFutureReminders()
            } catch (e: Exception) {
                logger.error(TAG, "Failed to cancel all reminders", e)
            }
        }
    }
    
    /**
     * Check if the required permissions are available
     */
    fun checkPermissions(): Boolean {
        val hasPermissions = reminderService.hasRequiredPermissions()
        if (!hasPermissions) {
            logger.info(TAG, "Exact alarm permission not available")
        }
        return hasPermissions
    }
    
    // Lifecycle callbacks
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        logger.debug(TAG, "App started - checking reminder status")
        
        // Schedule today's reminders when app starts
        schedulerScope.launch {
            if (currentUser != null && checkPermissions()) {
                reminderService.scheduleTodayReminders()
            }
        }
    }
    
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        logger.debug(TAG, "App stopped")
    }
    
    /**
     * Observe user authentication changes
     */
    private fun observeUserChanges() {
        getCurrentUserUseCase.observeCurrentUser()
            .onEach { user ->
                handleUserChange(user)
            }
            .launchIn(schedulerScope)
    }
    
    /**
     * Observe exam mode changes
     */
    private fun observeExamModeChanges() {
        schedulerScope.launch {
            try {
                // Poll exam mode status periodically (since we don't have a flow for exam mode)
                while (isInitialized) {
                    try {
                        val examModeInfo = getExamModeInfoUseCase().getOrNull()
                        val isExamMode = examModeInfo?.isExamMode ?: false
                        
                        if (isExamMode != currentExamMode) {
                            logger.info(TAG, "Exam mode changed from $currentExamMode to $isExamMode")
                            currentExamMode = isExamMode
                            reminderService.handleExamModeChange(isExamMode)
                        }
                    } catch (e: Exception) {
                        logger.error(TAG, "Error checking exam mode status", e)
                    }
                    
                    // Check every 30 seconds
                    kotlinx.coroutines.delay(30_000)
                }
            } catch (e: Exception) {
                logger.error(TAG, "Error in exam mode observation", e)
            }
        }
    }
    
    /**
     * Handle user login/logout
     */
    private suspend fun handleUserChange(user: User?) {
        try {
            val previousUser = currentUser
            currentUser = user
            
            when {
                user == null -> {
                    // User logged out - cancel all reminders
                    logger.info(TAG, "User logged out - cancelling all reminders")
                    reminderService.cancelAllFutureReminders()
                }
                
                previousUser == null && user != null -> {
                    // User logged in - schedule reminders
                    logger.info(TAG, "User logged in - scheduling reminders for ${user.name}")
                    if (checkPermissions()) {
                        reminderService.scheduleWeeklyReminders()
                    } else {
                        logger.info(TAG, "Cannot schedule reminders - missing permissions")
                    }
                }
                
                previousUser != null && user != null && previousUser.id != user.id -> {
                    // User switched - refresh reminders
                    logger.info(TAG, "User switched from ${previousUser.name} to ${user.name}")
                    if (checkPermissions()) {
                        reminderService.refreshReminders()
                    }
                }
                
                previousUser != null && user != null -> {
                    // Same user - check if profile changes affect class filtering
                    val hasClassFilteringChanges = hasClassFilteringChanges(previousUser, user)
                    if (hasClassFilteringChanges) {
                        logger.info(TAG, "User profile changes affect class filtering - refreshing reminders for ${user.name}")
                        if (checkPermissions()) {
                            reminderService.refreshReminders()
                        }
                    } else {
                        logger.debug(TAG, "User data updated for ${user.name} - no class filtering changes")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(TAG, "Error handling user change", e)
        }
    }
    
    /**
     * Check if user profile changes affect class filtering
     */
    private fun hasClassFilteringChanges(previousUser: User, currentUser: User): Boolean {
        return when (currentUser.role.name) {
            "STUDENT" -> {
                // For students, check batch, section, and labSection
                previousUser.batch != currentUser.batch ||
                previousUser.section != currentUser.section ||
                previousUser.labSection != currentUser.labSection
            }
            "TEACHER" -> {
                // For teachers, check initial
                previousUser.initial != currentUser.initial
            }
            else -> false
        }
    }
    
    /**
     * Cancel all future reminders
     */
    fun cancelAllFutureReminders() {
        schedulerScope.launch {
            try {
                logger.info(TAG, "Cancelling all future reminders")
                reminderService.cancelAllFutureReminders()
            } catch (e: Exception) {
                logger.error(TAG, "Failed to cancel future reminders", e)
            }
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
            isInitialized = false
            logger.info(TAG, "Class reminder scheduler cleaned up")
        } catch (e: Exception) {
            logger.error(TAG, "Error during cleanup", e)
        }
    }
}
