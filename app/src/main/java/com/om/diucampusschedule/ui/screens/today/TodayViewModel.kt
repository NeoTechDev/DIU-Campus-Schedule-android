package com.om.diucampusschedule.ui.screens.today

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.diucampusschedule.core.error.AppError
import com.om.diucampusschedule.core.reminder.ClassReminderScheduler
import com.om.diucampusschedule.core.service.CourseNameService
import com.om.diucampusschedule.data.repository.TaskRepository
import com.om.diucampusschedule.data.repository.RoutineRepository
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.Task
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.usecase.auth.GetCurrentUserUseCase
import com.om.diucampusschedule.domain.usecase.routine.GetUserRoutineForDayUseCase
import com.om.diucampusschedule.ui.screens.today.components.CourseUtils
import com.om.diucampusschedule.widget.WidgetManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
    val courseNames: Map<String, String> = emptyMap() // Cache for course names
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
    private val courseNameService: CourseNameService,
    private val taskRepository: TaskRepository,
    private val routineRepository: RoutineRepository,
    private val classReminderScheduler: ClassReminderScheduler,
    private val widgetManager: WidgetManager,
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
    
    private var currentUser: User? = null
    
    init {
        observeUser()
        observeDateChanges()
        
        // Initialize class reminder scheduler
        classReminderScheduler.initialize()
    }
    
    private fun observeUser() {
        getCurrentUserUseCase.observeCurrentUser()
            .onEach { user ->
                currentUser = user
                if (user != null) {
                    _uiState.value = _uiState.value.copy(currentUser = user)
                    // Load data for current selected date
                    loadDataForDate(_selectedDate.value)
                } else {
                    _uiState.value = _uiState.value.copy(
                        currentUser = null,
                        routineItems = emptyList(),
                        tasks = emptyList(),
                        isLoading = false
                    )
                    dayDataCache.clear() // Clear cache when user changes
                }
            }
            .catch { throwable ->
                val error = AppError.fromThrowable(throwable)
                _uiState.value = _uiState.value.copy(error = error, isLoading = false)
            }
            .launchIn(viewModelScope)
    }
    
    private fun observeDateChanges() {
        _selectedDate
            .onEach { date ->
                _uiState.value = _uiState.value.copy(selectedDate = date)
                loadDataForDate(date)
            }
            .launchIn(viewModelScope)
    }
    
    private fun loadDataForDate(date: LocalDate) {
        val user = currentUser ?: return
        val cacheKey = getCacheKey(user, date)
        
        // Check cache first
        val cachedData = dayDataCache[cacheKey]
        if (cachedData != null && !isCacheExpired(cachedData)) {
            // Use cached data - but still need to ensure course names are loaded
            viewModelScope.launch {
                val updatedCourseNames = loadCourseNamesSync(cachedData.routineItems)
                _uiState.value = _uiState.value.copy(
                    routineItems = cachedData.routineItems,
                    tasks = cachedData.tasks,
                    courseNames = updatedCourseNames,
                    isLoading = false,
                    error = null
                )
                // Also update the CourseUtils cache for ClassRoutineCard compatibility
                CourseUtils.setCourseNames(updatedCourseNames)
            }
            android.util.Log.d("TodayViewModel", "Using cached data for $date")
            return
        }
        
        // Load fresh data - coordinate both routine and tasks loading
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                // Load both routine and tasks concurrently and wait for both to complete
                val routineDeferred = async { loadRoutineForDateAsync(user, date) }
                val tasksDeferred = async { loadTasksForDateAsync(date) }
                
                val routineItems = routineDeferred.await()
                val tasks = tasksDeferred.await()
                
                // Load course names for the routine items and get the updated course names map
                val updatedCourseNames = loadCourseNamesSync(routineItems)
                
                // Update UI state with ALL data at once (routine, tasks, course names)
                _uiState.value = _uiState.value.copy(
                    routineItems = routineItems,
                    tasks = tasks,
                    courseNames = updatedCourseNames,
                    isLoading = false,
                    error = null
                )
                
                // Also update the CourseUtils cache for ClassRoutineCard compatibility
                CourseUtils.setCourseNames(updatedCourseNames)
                
                // Cache the complete data
                val cacheData = CachedDayData(routineItems, tasks)
                dayDataCache[cacheKey] = cacheData
                android.util.Log.d("TodayViewModel", "Loaded and cached complete data for $date with ${updatedCourseNames.size} course names")
                
                // Update widgets when data changes for today
                if (date == LocalDate.now()) {
                    widgetManager.updateWidgets(context)
                }
                
            } catch (e: Exception) {
                android.util.Log.e("TodayViewModel", "Error loading data for $date", e)
                val error = AppError.fromThrowable(e)
                _uiState.value = _uiState.value.copy(
                    error = error,
                    isLoading = false
                )
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
        
        android.util.Log.d("TodayViewModel", "Loading routine for user: ${user.name}")
        android.util.Log.d("TodayViewModel", "User details - Batch: '${user.batch}', Section: '${user.section}', LabSection: '${user.labSection}'")
        android.util.Log.d("TodayViewModel", "Filtering for day: $dayName")
        
        return getUserRoutineForDayUseCase(user, dayName).fold(
            onSuccess = { routineItems ->
                android.util.Log.d("TodayViewModel", "Received ${routineItems.size} routine items for $dayName")
                
                val sortedRoutineItems = routineItems.sortedBy { it.startTime }
                sortedRoutineItems
            },
            onFailure = { throwable ->
                android.util.Log.e("TodayViewModel", "Error loading routine", throwable)
                throw throwable
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
            } catch (e: Exception) {
                android.util.Log.e("TodayViewModel", "Error deleting task: ${e.message}")
            }
        }
    }
    
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
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
        val user = currentUser ?: return
        val date = _selectedDate.value
        
        // Clear current date from cache
        val cacheKey = getCacheKey(user, date)
        dayDataCache.remove(cacheKey)
        
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
        android.util.Log.d("TodayViewModel", "Refreshed data for $date")
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
     * Clean up resources when ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        classReminderScheduler.cleanup()
    }
}
