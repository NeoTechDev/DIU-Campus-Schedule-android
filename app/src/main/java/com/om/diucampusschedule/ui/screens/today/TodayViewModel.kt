package com.om.diucampusschedule.ui.screens.today

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.diucampusschedule.core.error.AppError
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.core.reminder.ClassReminderScheduler
import com.om.diucampusschedule.core.service.CourseNameService
import com.om.diucampusschedule.data.repository.NoticeRepository
import com.om.diucampusschedule.data.repository.RoutineRepository
import com.om.diucampusschedule.data.repository.TaskRepository
import com.om.diucampusschedule.data.repository.NotificationRepository
import com.om.diucampusschedule.domain.model.Notice
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.Task
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.usecase.auth.GetCurrentUserUseCase
import com.om.diucampusschedule.domain.usecase.routine.GetMaintenanceInfoUseCase
import com.om.diucampusschedule.domain.usecase.routine.GetUserRoutineForDayUseCase
import com.om.diucampusschedule.ui.screens.today.components.CourseUtils
import com.om.diucampusschedule.widget.WidgetManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class TodayUiState(
    val isLoading: Boolean = false,
    val routineItems: List<RoutineItem> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val currentUser: User? = null,
    val error: AppError? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val courseNames: Map<String, String> = emptyMap(), // Cache for course names
    // Maintenance related fields for class routines
    val isMaintenanceMode: Boolean = false, // Whether the system is in maintenance mode
    val maintenanceMessage: String? = null, // Message to show during maintenance
    val isSemesterBreak: Boolean = false, // Whether it's semester break
    val updateType: String? = null, // Type of maintenance update (maintenance_enabled, semester_break, etc.)
    val hasLoadedOnce: Boolean = false // Track if data has been loaded at least once to prevent blinking
)

data class CachedDayData(
    val routineItems: List<RoutineItem>,
    val tasks: List<Task>,
    val timestamp: Long = System.currentTimeMillis()
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserRoutineForDayUseCase: GetUserRoutineForDayUseCase,
    private val getMaintenanceInfoUseCase: GetMaintenanceInfoUseCase,
    private val courseNameService: CourseNameService,
    private val taskRepository: TaskRepository,
    private val routineRepository: RoutineRepository,
    private val notificationRepository: NotificationRepository,
    private val classReminderScheduler: ClassReminderScheduler,
    private val widgetManager: WidgetManager,
    private val logger: AppLogger,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()
    
    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()
    
    // Expose routine loading state for calendar
    val isRoutineLoading = routineRepository.isLoading
    
    // Professional caching solution
    private val dayDataCache = mutableMapOf<String, CachedDayData>()
    private val cacheExpirationTime = 5 * 60 * 1000L // 5 minutes
    
    // Rate limiting for refresh operations
    private var lastRefreshTime = 0L
    private val refreshCooldownMs = 2000L // 2 seconds minimum between refreshes
    
    private var currentUser: User? = null
    
    // Notification count state
    private val _unreadNotificationCount = MutableStateFlow(0)
    val unreadNotificationCount: StateFlow<Int> = _unreadNotificationCount

    companion object {
        private const val TAG = "TodayViewModel"
    }
    
    init {
        observeUser()
        observeDateChanges()
        observeTaskChanges() // Add task observation
        observeNotificationCount() // Add notification count observation
        
        // Initialize class reminder scheduler
        classReminderScheduler.initialize()
        
        // Check maintenance mode immediately on initialization
        viewModelScope.launch {
            logger.debug(TAG, "Initial maintenance mode check on ViewModel init")
            checkMaintenanceMode()
        }
    }
    
    private fun observeUser() {
        getCurrentUserUseCase.observeCurrentUser()
            .onEach { user ->
                try {
                    val previousUser = currentUser
                    currentUser = user
                    
                    if (user != null) {
                        // Check if this is a user profile change that affects class filtering
                        val shouldClearCache = previousUser == null || 
                                             previousUser.id != user.id ||
                                             hasClassFilteringChanges(previousUser, user)
                        
                        if (shouldClearCache) {
                            dayDataCache.clear() // Clear cache when user changes or profile affects filtering
                            logger.debug(TAG, "Cleared cache due to user profile changes affecting class filtering")
                        }
                        
                        _uiState.value = _uiState.value.copy(currentUser = user)
                        // Load data for current selected date
                        loadDataForDate(_selectedDate.value)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            currentUser = null,
                            routineItems = emptyList(),
                            tasks = emptyList(),
                            isLoading = false,
                            hasLoadedOnce = true
                        )
                        dayDataCache.clear() // Clear cache when user changes
                    }
                } catch (e: Exception) {
                    logger.error(TAG, "Error in observeUser", e)
                    // Prevent crash by setting safe state
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        hasLoadedOnce = true
                    )
                }
            }
            .catch { throwable ->
                logger.error(TAG, "Critical error in user observation", throwable)
                // Set safe error state to prevent app crash
                _uiState.value = _uiState.value.copy(
                    error = null, // Don't show error UI to prevent blinking
                    isLoading = false,
                    hasLoadedOnce = true
                )
            }
            .launchIn(viewModelScope)
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
    
    private fun observeDateChanges() {
        _selectedDate
            .onEach { date ->
                _uiState.value = _uiState.value.copy(selectedDate = date)
                // Load routine data only (tasks are now handled by observeTaskChanges)
                loadDataForDate(date) // Use loadDataForDate which checks cache first
            }
            .launchIn(viewModelScope)
    }
    
    private fun observeTaskChanges() {
        // Combine selected date with task repository to get tasks for current date
        combine(
            _selectedDate,
            taskRepository.getAllTasks() // Observe all tasks changes
        ) { date, _ ->
            // When either date changes or tasks change, reload tasks for the date
            val dateString = date.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
            
            try {
                // Get tasks for the specific date
                val allTasksForDate = taskRepository.getTasksByDate(dateString).first()
                val pendingTasks = allTasksForDate.filter { !it.isCompleted }
                
                logger.debug(TAG, "Tasks updated for $dateString: ${pendingTasks.size} pending tasks")
                pendingTasks
            } catch (e: Exception) {
                logger.error(TAG, "Error loading tasks for date $dateString", e)
                emptyList()
            }
        }
            .distinctUntilChanged() // Only emit when tasks actually change
            .onEach { tasks ->
                // Update UI state with new tasks - but don't change loading state
                val currentState = _uiState.value
                _uiState.value = currentState.copy(tasks = tasks)
                
                // Update cache as well
                val user = currentUser
                val date = _selectedDate.value
                if (user != null) {
                    updateCache(user, date, tasks = tasks)
                }
            }
            .catch { throwable ->
                logger.error(TAG, "Error in task observation", throwable)
            }
            .launchIn(viewModelScope)
    }

    private fun loadTasksAndUpdateUISilently(date: LocalDate) {
        viewModelScope.launch {
            try {
                val tasks = loadTasksForDateAsync(date)
                // Update UI state without changing loading state
                _uiState.value = _uiState.value.copy(tasks = tasks)
                
                // Update cache with new tasks
                val user = currentUser
                if (user != null) {
                    updateCache(user, date, tasks = tasks)
                }
            } catch (e: Exception) {
                logger.error(TAG, "Error loading tasks for silent UI update", e)
            }
        }
    }
    
    private fun loadDataForDate(date: LocalDate) {
        val user = currentUser ?: return
        val cacheKey = getCacheKey(user, date)
        
        // Check cache first for both routine and task data
        val cachedData = dayDataCache[cacheKey]
        if (cachedData != null && !isCacheExpired(cachedData)) {
            // Use cached data immediately without loading state
            viewModelScope.launch {
                val updatedCourseNames = loadCourseNamesSync(cachedData.routineItems)
                
                // Get current tasks for this date from repository
                val dateString = date.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
                val currentTasks = try {
                    taskRepository.getTasksByDate(dateString).first().filter { !it.isCompleted }
                } catch (e: Exception) {
                    logger.error(TAG, "Error loading tasks for cached data", e)
                    emptyList()
                }
                
                _uiState.value = _uiState.value.copy(
                    routineItems = cachedData.routineItems,
                    tasks = currentTasks,
                    courseNames = updatedCourseNames,
                    isLoading = false,
                    error = null,
                    hasLoadedOnce = true
                )
                // Also update the CourseUtils cache for ClassRoutineCard compatibility
                CourseUtils.setCourseNames(updatedCourseNames)
                
                // Update cache with current tasks
                dayDataCache[cacheKey] = cachedData.copy(tasks = currentTasks)
            }
            logger.debug(TAG, "Using cached data for $date - no loading state shown")
            return
        }
        
        // Load fresh routine data only - tasks are handled by observeTaskChanges
        loadRoutineDataForDate(date)
    }
    
    private fun loadRoutineDataForDate(date: LocalDate) {
        val user = currentUser ?: return
        
        // Only show loading state if we haven't loaded once yet, to prevent blinking
        val currentState = _uiState.value
        if (!currentState.hasLoadedOnce) {
            _uiState.value = currentState.copy(isLoading = true, error = null)
        }
        
        viewModelScope.launch {
            try {
                // Check maintenance mode first
                checkMaintenanceMode()
                
                // Load only routine data
                val routineItems = loadRoutineForDateAsync(user, date)
                
                // Load course names for the routine items
                val updatedCourseNames = loadCourseNamesSync(routineItems)
                
                // Update UI state with routine data only (tasks handled separately)
                _uiState.value = _uiState.value.copy(
                    routineItems = routineItems,
                    courseNames = updatedCourseNames,
                    isLoading = false,
                    error = null,
                    hasLoadedOnce = true
                )
                
                // Also update the CourseUtils cache for ClassRoutineCard compatibility
                CourseUtils.setCourseNames(updatedCourseNames)
                
                // Cache the routine data (tasks will be cached separately)
                val cacheData = CachedDayData(routineItems, emptyList()) // Empty tasks list for routine-only cache
                dayDataCache[getCacheKey(user, date)] = cacheData
                logger.debug(TAG, "Loaded and cached routine data for $date with ${updatedCourseNames.size} course names")
                
                // Update widgets when data changes for today
                if (date == LocalDate.now()) {
                    widgetManager.updateWidgets(context)
                }
                
            } catch (e: Exception) {
                logger.error(TAG, "Error loading routine data for $date", e)
                
                // Set a stable error state to prevent blinking
                // Don't retry automatically to avoid infinite loops
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null, // Don't show error for routine loading failures
                    routineItems = emptyList(), // Show empty state instead of error
                    hasLoadedOnce = true
                )
                
                // Still cache empty result to prevent repeated failures
                val emptyCacheData = CachedDayData(emptyList(), emptyList())
                dayDataCache[getCacheKey(user, date)] = emptyCacheData
            }
        }
    }
    
    private fun getCacheKey(user: User, date: LocalDate): String {
        return "${user.id}_${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
    }
    
    private fun isCacheExpired(cachedData: CachedDayData): Boolean {
        return System.currentTimeMillis() - cachedData.timestamp > cacheExpirationTime
    }

    // New async versions for coordinated loading
    private suspend fun loadRoutineForDateAsync(user: User, date: LocalDate): List<RoutineItem> {
        val dayName = date.format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH))
        
        logger.debug(TAG, "Loading routine for user: ${user.name}")
        logger.debug(TAG, "User details - Batch: '${user.batch}', Section: '${user.section}', LabSection: '${user.labSection}'")
        logger.debug(TAG, "Filtering for day: $dayName")
        
        return getUserRoutineForDayUseCase(user, dayName).fold(
            onSuccess = { routineItems ->
                logger.debug(TAG, "Received ${routineItems.size} routine items for $dayName")
                
                // Log time parsing status for debugging Xiaomi issues
                routineItems.forEachIndexed { index, item ->
                    val startTime = item.startTime
                    val endTime = item.endTime
                    logger.debug(TAG, "Item $index: ${item.courseCode} - Time: '${item.time}' | StartTime: $startTime | EndTime: $endTime")
                    if (startTime == null || endTime == null) {
                        logger.warning(TAG, "Time parsing failed for item $index: ${item.courseCode} with time '${item.time}'")
                    }
                }
                
                val sortedRoutineItems = routineItems.sortedBy { it.startTime }
                sortedRoutineItems
            },
            onFailure = { throwable ->
                logger.error(TAG, "Error loading routine for $dayName", throwable)
                
                // Don't throw the error - return empty list instead
                // This prevents crashes and infinite retry loops
                emptyList()
            }
        )
    }
    
    private suspend fun loadTasksForDateAsync(date: LocalDate): List<Task> {
        val dateString = date.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
        
        // Get the first emission from the Flow
        val allTasks = taskRepository.getTasksByDate(dateString).first()
        
        // Filter to only show pending (incomplete) tasks
        val pendingTasks = allTasks.filter { !it.isCompleted }
        android.util.Log.d("TodayViewModel", "Loaded ${pendingTasks.size} pending tasks for $dateString (${allTasks.size} total)")
        
        return pendingTasks
    }
    
    private fun updateCache(user: User, date: LocalDate, routineItems: List<RoutineItem>? = null, tasks: List<Task>? = null) {
        val cacheKey = getCacheKey(user, date)
        val existingCache = dayDataCache[cacheKey]
        
        val updatedCache = if (existingCache != null) {
            // Update existing cache with new data
            existingCache.copy(
                routineItems = routineItems ?: existingCache.routineItems,
                tasks = tasks ?: existingCache.tasks,
                timestamp = System.currentTimeMillis()
            )
        } else {
            // Create new cache entry
            CachedDayData(
                routineItems = routineItems ?: emptyList(),
                tasks = tasks ?: emptyList()
            )
        }
        
        dayDataCache[cacheKey] = updatedCache
        android.util.Log.d("TodayViewModel", "Updated cache for $date")
    }

    private suspend fun loadCourseNamesSync(routineItems: List<RoutineItem>): Map<String, String> {
        val currentNames = _uiState.value.courseNames.toMutableMap()
        
        routineItems.forEach { item ->
            if (!currentNames.containsKey(item.courseCode)) {
                val courseName = courseNameService.getCourseName(item.courseCode)
                if (courseName != null) {
                    currentNames[item.courseCode] = courseName
                    android.util.Log.d("TodayViewModel", "Loaded course name: ${item.courseCode} -> $courseName")
                } else {
                    // If no course name found, keep the course code as fallback
                    currentNames[item.courseCode] = item.courseCode
                    android.util.Log.d("TodayViewModel", "No course name found for: ${item.courseCode}, using code as fallback")
                }
            }
        }
        
        return currentNames
    }
    
    fun getCourseName(courseCode: String): String {
        return _uiState.value.courseNames[courseCode] ?: courseCode
    }
    
    // Task management methods
    fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.updateTask(task)
                android.util.Log.d("TodayViewModel", "Task updated: ${task.title}")
                
                // Immediately refresh tasks for current date
                refreshTasksForCurrentDate()
            } catch (e: Exception) {
                android.util.Log.e("TodayViewModel", "Error updating task: ${e.message}")
            }
        }
    }
    
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(task)
                android.util.Log.d("TodayViewModel", "Task deleted: ${task.title}")
                
                // Immediately refresh tasks for current date
                refreshTasksForCurrentDate()
            } catch (e: Exception) {
                android.util.Log.e("TodayViewModel", "Error deleting task: ${e.message}")
            }
        }
    }
    
    private fun refreshTasksForCurrentDate() {
        val currentDate = _selectedDate.value
        val user = currentUser
        
        if (user != null) {
            // Clear cache and reload tasks immediately without showing loading
            val cacheKey = getCacheKey(user, currentDate)
            dayDataCache.remove(cacheKey)
            loadTasksAndUpdateUISilently(currentDate)
        }
    }
    
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        
        // Preload nearby dates for smoother navigation
        preloadNearbyDates(date)
    }
    
    private fun preloadNearbyDates(currentDate: LocalDate) {
        val user = currentUser ?: return
        
        viewModelScope.launch {
            // Preload previous and next day's data in background
            val datesToPreload = listOf(
                currentDate.minusDays(1),
                currentDate.plusDays(1)
            )
            
            datesToPreload.forEach { date ->
                val cacheKey = getCacheKey(user, date)
                // Only preload if not already cached
                if (!dayDataCache.containsKey(cacheKey)) {
                    try {
                        logger.debug(TAG, "Preloading data for $date")
                        preloadDataForDate(user, date)
                    } catch (e: Exception) {
                        // Ignore preload failures
                        logger.debug(TAG, "Failed to preload data for $date: ${e.message}")
                    }
                }
            }
        }
    }
    
    fun resetToToday() {
        _selectedDate.value = LocalDate.now()
        
        // Schedule today's reminders when user opens today screen
        classReminderScheduler.scheduleTodayReminders()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun retryLastAction() {
        val currentState = _uiState.value
        if (currentState.currentUser != null) {
            // Clear cache for current date to force refresh
            val cacheKey = getCacheKey(currentState.currentUser, currentState.selectedDate)
            dayDataCache.remove(cacheKey)
            
            viewModelScope.launch {
                loadDataForDate(currentState.selectedDate)
                
                // Refresh reminders when retrying
                classReminderScheduler.refreshReminders()
            }
        }
    }

    // Professional method to get all routine items for all days of the week for calendar
    suspend fun getAllWeekRoutineItems(): List<RoutineItem> {
        val user = currentUser ?: return emptyList()
        
        // Try to get cached data first for immediate response
        val cachedData = routineRepository.getCachedWeekRoutineItems(user)
        if (cachedData != null) {
            return cachedData
        }
        
        // If no cache, fetch from repository (which handles its own caching)
        return try {
            routineRepository.getWeekRoutineItems(user).first()
        } catch (e: Exception) {
            android.util.Log.e("TodayViewModel", "Error loading week routine items", e)
            emptyList()
        }
    }
    
    // Method to refresh routine data
    suspend fun refreshWeekRoutineItems(): List<RoutineItem> {
        val user = currentUser ?: return emptyList()
        return try {
            routineRepository.refreshWeekRoutineItems(user)
        } catch (e: Exception) {
            android.util.Log.e("TodayViewModel", "Error refreshing week routine items", e)
            emptyList()
        }
    }
    
    // Method to get all tasks for a specific month for calendar
    suspend fun getAllTasksForMonth(yearMonth: YearMonth): List<Task> {
        return try {
            // Get all tasks and filter for the specific month
            val allTasks = taskRepository.getAllTasks().first()
            val dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.US)
            
            allTasks.filter { task ->
                try {
                    val taskDate = LocalDate.parse(task.date, dateFormatter)
                    YearMonth.from(taskDate) == yearMonth
                } catch (e: Exception) {
                    false
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TodayViewModel", "Error loading tasks for month $yearMonth", e)
            emptyList()
        }
    }
    
    // Method to clear cache when needed (e.g., when user logs out or data becomes stale)
    fun clearCache() {
        dayDataCache.clear()
        routineRepository.invalidateCache()
        android.util.Log.d("TodayViewModel", "Cache cleared")
    }
    
    // Method to refresh current data (clears cache and reloads)
    fun refreshCurrentData() {
        val currentTime = System.currentTimeMillis()
        
        // Rate limiting: prevent excessive refreshes
        if (currentTime - lastRefreshTime < refreshCooldownMs) {
            logger.debug(TAG, "Refresh rate limited - too many requests")
            return
        }
        
        lastRefreshTime = currentTime
        
        val user = currentUser ?: return
        val date = _selectedDate.value
        
        // Clear current date from cache
        val cacheKey = getCacheKey(user, date)
        dayDataCache.remove(cacheKey)
        
        // Reset hasLoadedOnce to allow loading state on manual refresh
        _uiState.value = _uiState.value.copy(hasLoadedOnce = false)
        
        // Reload data
        viewModelScope.launch {
            loadDataForDate(date)
            
            // Refresh reminders when data is refreshed
            classReminderScheduler.refreshReminders()
            
            // Update widgets on refresh
            if (date == LocalDate.now()) {
                widgetManager.updateWidgets(context)
            }
        }
        logger.debug(TAG, "Refreshed data for $date")
    }
    
    // Method to preload data for nearby dates (optional performance optimization)
    fun preloadNearbyDates() {
        val currentDate = _selectedDate.value
        val user = currentUser ?: return
        
        viewModelScope.launch {
            // Preload previous and next day data in background
            listOf(currentDate.minusDays(1), currentDate.plusDays(1)).forEach { date ->
                val cacheKey = getCacheKey(user, date)
                if (!dayDataCache.containsKey(cacheKey)) {
                    android.util.Log.d("TodayViewModel", "Preloading data for $date")
                    // Load without updating UI state
                    preloadDataForDate(user, date)
                }
            }
        }
    }
    
    private suspend fun preloadDataForDate(user: User, date: LocalDate) {
        val dayName = date.format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH))
        
        try {
            getUserRoutineForDayUseCase(user, dayName).fold(
                onSuccess = { routineItems ->
                    val sortedRoutineItems = routineItems.sortedBy { it.startTime }
                    updateCache(user, date, routineItems = sortedRoutineItems)
                },
                onFailure = { /* Ignore preload failures */ }
            )
        } catch (e: Exception) {
            // Ignore preload failures
        }
    }
    
    /**
     * Check maintenance mode status from Firebase and update UI state accordingly
     * This is specifically for class routine content, not tasks
     */
    private suspend fun checkMaintenanceMode() {
        try {
            logger.debug(TAG, "Checking maintenance mode status from Firebase for class routines")
            
            getMaintenanceInfoUseCase().fold(
                onSuccess = { maintenanceInfo ->
                    logger.info(TAG, "Maintenance info for Today screen: isMaintenanceMode=${maintenanceInfo.isMaintenanceMode}, message=${maintenanceInfo.maintenanceMessage}, isSemesterBreak=${maintenanceInfo.isSemesterBreak}")
                    
                    // Update maintenance state for class routine content only
                    _uiState.value = _uiState.value.copy(
                        isMaintenanceMode = maintenanceInfo.isMaintenanceMode,
                        maintenanceMessage = maintenanceInfo.maintenanceMessage,
                        isSemesterBreak = maintenanceInfo.isSemesterBreak,
                        updateType = maintenanceInfo.updateType
                    )
                    
                    logger.info(TAG, "Updated Today screen maintenance state - isMaintenanceMode: ${maintenanceInfo.isMaintenanceMode}, isSemesterBreak: ${maintenanceInfo.isSemesterBreak}")
                },
                onFailure = { error ->
                    logger.warning(TAG, "Failed to fetch maintenance info from Firebase for Today screen", error)
                    // Don't update maintenance state on failure to avoid false positives
                }
            )
        } catch (e: Exception) {
            logger.error(TAG, "Error checking maintenance mode for Today screen", e)
        }
    }
    
    private fun observeNotificationCount() {
        viewModelScope.launch {
            try {
                val currentUser = getCurrentUserUseCase()
                if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                    val user = currentUser.getOrThrow()!!
                    
                    notificationRepository.getUnreadCount(user.id)
                        .catch { exception ->
                            logger.error(TAG, "Failed to observe unread notification count", exception)
                        }
                        .collect { count ->
                            _unreadNotificationCount.value = count
                        }
                } else {
                    logger.warning(TAG, "User not authenticated - cannot observe notification count")
                }
            } catch (e: Exception) {
                logger.error(TAG, "Failed to setup notification count observation", e)
            }
        }
    }
    
    /**
     * Clean up resources when ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        classReminderScheduler.cleanup()
    }
}
