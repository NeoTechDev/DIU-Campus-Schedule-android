package com.om.diucampusschedule.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.diucampusschedule.core.cache.RoutineCacheService
import com.om.diucampusschedule.core.error.AppError
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.core.network.NetworkMonitor
import com.om.diucampusschedule.domain.model.DayOfWeek
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.RoutineSchedule
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.usecase.auth.GetCurrentUserUseCase
import com.om.diucampusschedule.domain.usecase.routine.GetActiveDaysUseCase
import com.om.diucampusschedule.domain.usecase.routine.GetAllDaysUseCase
import com.om.diucampusschedule.domain.usecase.routine.GetAllTimeSlotsUseCase
import com.om.diucampusschedule.domain.usecase.routine.GetFullRoutineScheduleUseCase
import com.om.diucampusschedule.domain.usecase.routine.GetUserRoutineForDayUseCase
import com.om.diucampusschedule.domain.usecase.routine.GetUserRoutineUseCase
import com.om.diucampusschedule.domain.usecase.routine.ObserveUserRoutineForDayUseCase
import com.om.diucampusschedule.domain.usecase.routine.RefreshRoutineUseCase
import com.om.diucampusschedule.domain.usecase.routine.SyncRoutineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class RoutineUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val hasPendingSync: Boolean = false,
    val routineItems: List<RoutineItem> = emptyList(), // For selected day (backward compatibility)
    val allRoutineItems: List<RoutineItem> = emptyList(), // All routine items for user's full week
    val fullDatabaseRoutineItems: List<RoutineItem> = emptyList(), // Full database routine items for filtering
    val filteredRoutineItems: List<RoutineItem> = emptyList(), // Filtered routine items
    val allTimeSlots: List<String> = emptyList(), // All time slots sorted chronologically
    val activeDays: List<String> = emptyList(),
    val allDays: List<String> = emptyList(), // All days including off days
    val selectedDay: String = DayOfWeek.getCurrentDay().displayName,
    val currentUser: User? = null,
    val error: AppError? = null,
    val lastSyncTime: Long = 0L,
    val isCacheLoaded: Boolean = false, // Track if initial cache is loaded
    val currentFilter: RoutineFilter? = null, // Current applied filter
    val isFiltered: Boolean = false // Whether a filter is currently applied
)



@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserRoutineUseCase: GetUserRoutineUseCase,
    private val getUserRoutineForDayUseCase: GetUserRoutineForDayUseCase,
    private val observeUserRoutineForDayUseCase: ObserveUserRoutineForDayUseCase,
    private val getActiveDaysUseCase: GetActiveDaysUseCase,
    private val getAllDaysUseCase: GetAllDaysUseCase,
    private val getAllTimeSlotsUseCase: GetAllTimeSlotsUseCase,
    private val getFullRoutineScheduleUseCase: GetFullRoutineScheduleUseCase,
    private val syncRoutineUseCase: SyncRoutineUseCase,
    private val refreshRoutineUseCase: RefreshRoutineUseCase,
    private val networkMonitor: NetworkMonitor,
    private val logger: AppLogger,
    private val cacheService: RoutineCacheService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineUiState())
    val uiState: StateFlow<RoutineUiState> = _uiState.asStateFlow()

    private var currentUser: User? = null

    companion object {
        private const val TAG = "RoutineViewModel"
    }

    init {
        logger.debug(TAG, "RoutineViewModel initialized")
        observeUserChanges()
        observeNetworkChanges()
    }

    private fun observeUserChanges() {
        logger.debug(TAG, "Starting to observe user changes")
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        getCurrentUserUseCase.observeCurrentUser()
            .onEach { user ->
                logger.debug(TAG, "User changed: ${user?.id}")
                android.util.Log.d("RoutineViewModel", "User profile changed: ${user?.name}, Batch: ${user?.batch}, Section: ${user?.section}, Initial: ${user?.initial}")
                
                if (user != null) {
                    val previousUser = currentUser
                    currentUser = user
                    
                    _uiState.value = _uiState.value.copy(
                        currentUser = user,
                        isLoading = false
                    )
                    
                    // Check if this is a profile update (user existed before) or initial load
                    if (previousUser != null && hasUserProfileChanged(previousUser, user)) {
                        logger.info(TAG, "User profile updated, refreshing routine data")
                        android.util.Log.d("RoutineViewModel", "Profile changed - refreshing routine data")
                        refreshRoutineData()
                    } else if (previousUser == null) {
                        logger.info(TAG, "Initial user load: ${user.id}, department: ${user.department}")
                        loadInitialData()
                    }
                } else {
                    currentUser = null
                    val error = AppError.AuthenticationError("Please sign in to view your routine")
                    logger.warning(TAG, "User not authenticated")
                    _uiState.value = _uiState.value.copy(
                        error = error,
                        isLoading = false,
                        currentUser = null
                    )
                }
            }
            .catch { throwable ->
                val error = AppError.fromThrowable(throwable)
                logger.error(TAG, "Error observing user changes", throwable)
                _uiState.value = _uiState.value.copy(
                    error = error,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }
    
    private fun hasUserProfileChanged(oldUser: User, newUser: User): Boolean {
        return oldUser.batch != newUser.batch ||
               oldUser.section != newUser.section ||
               oldUser.labSection != newUser.labSection ||
               oldUser.initial != newUser.initial ||
               oldUser.department != newUser.department ||
               oldUser.role != newUser.role
    }

    private fun loadInitialData() {
        val user = currentUser ?: return
        
        viewModelScope.launch {
            logger.debug(TAG, "Loading initial routine data for user: ${user.id}")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Load all days (including off days) and active days
                val allDays = getAllDaysUseCase()
                
                // Load time slots for the department
                loadTimeSlots(user.department)
                
                // Check cache first
                val cachedActiveDays = cacheService.getCachedActiveDays(user)
                if (cachedActiveDays != null) {
                    logger.info(TAG, "Using cached active days: $cachedActiveDays")
                    _uiState.value = _uiState.value.copy(
                        activeDays = cachedActiveDays,
                        allDays = allDays
                    )
                    
                    // Set selected day and continue with preloading
                    val currentDay = DayOfWeek.getCurrentDay().displayName
                    val selectedDay = currentDay // Always select current day, even if it's an off day
                    
                    logger.debug(TAG, "Selected initial day: $selectedDay")
                    _uiState.value = _uiState.value.copy(selectedDay = selectedDay)
                    
                    // Preload all days data for faster switching
                    preloadAllDaysData()
                    
                    // Load full database routine for filtering
                    loadFullDatabaseRoutine()
                    
                    // Start observing routine for selected day (will be empty for off days)
                    observeRoutineForDay(selectedDay)
                } else {
                    // Load from use case and cache
                    getActiveDaysUseCase(user).fold(
                        onSuccess = { activeDays ->
                            logger.info(TAG, "Loaded ${activeDays.size} active days: $activeDays")
                            logger.info(TAG, "All days including off days: $allDays")
                            
                            // Cache the active days
                            cacheService.cacheActiveDays(user, activeDays)
                            
                            _uiState.value = _uiState.value.copy(
                                activeDays = activeDays,
                                allDays = allDays
                            )
                            
                            // Set selected day to current day (including off days)
                            val currentDay = DayOfWeek.getCurrentDay().displayName
                            val selectedDay = currentDay // Always select current day, even if it's an off day
                            
                            logger.debug(TAG, "Selected initial day: $selectedDay")
                            _uiState.value = _uiState.value.copy(selectedDay = selectedDay)
                            
                            // Preload all days data for faster switching
                            preloadAllDaysData()
                            
                            // Load full database routine for filtering
                            loadFullDatabaseRoutine()
                            
                            // Start observing routine for selected day (will be empty for off days)
                            observeRoutineForDay(selectedDay)
                        },
                        onFailure = { throwable ->
                            val error = AppError.fromThrowable(throwable)
                            logger.error(TAG, "Failed to load routine data", throwable)
                            _uiState.value = _uiState.value.copy(
                                error = error,
                                isLoading = false,
                                isOffline = !networkMonitor.isCurrentlyOnline()
                            )
                        }
                    )
                }
                
                // Sync in background
                syncRoutineDataSilently()
                
            } catch (e: Exception) {
                val error = AppError.fromThrowable(e)
                logger.error(TAG, "Error loading initial data", e)
                _uiState.value = _uiState.value.copy(
                    error = error,
                    isLoading = false,
                    isOffline = !networkMonitor.isCurrentlyOnline()
                )
            }
        }
    }

    private fun observeRoutineForDay(day: String) {
        val user = currentUser ?: return
        
        logger.debug(TAG, "Starting to observe routine for day: $day")
        android.util.Log.d("RoutineViewModel", "User details - Name: ${user.name}, Department: ${user.department}, Batch: ${user.batch}, Section: ${user.section}, Role: ${user.role}")
        
        observeUserRoutineForDayUseCase(user, day)
            .catch { throwable ->
                val error = AppError.fromThrowable(throwable)
                logger.error(TAG, "Error observing routine for day: $day", throwable)
                android.util.Log.e("RoutineViewModel", "Error observing routine for day: $day", throwable)
                _uiState.value = _uiState.value.copy(
                    error = error,
                    isLoading = false,
                    isOffline = !networkMonitor.isCurrentlyOnline()
                )
            }
            .onEach { routineItems ->
                logger.debug(TAG, "Received ${routineItems.size} routine items for $day")
                android.util.Log.d("RoutineViewModel", "Received ${routineItems.size} routine items for $day")
                
                // Cache the data using singleton cache service
                cacheService.cacheRoutineForDay(day, user, routineItems)
                
                _uiState.value = _uiState.value.copy(
                    routineItems = routineItems,
                    isLoading = false,
                    isRefreshing = false,
                    isOffline = false,
                    hasPendingSync = false,
                    lastSyncTime = System.currentTimeMillis()
                )
            }
            .launchIn(viewModelScope)
    }
    

    
    private fun preloadAllDaysData() {
        val user = currentUser ?: return
        
        viewModelScope.launch {
            try {
                logger.debug(TAG, "Preloading all days data")
                
                // First check if we already have cached data
                if (cacheService.preloadAllDaysFromSchedule(user)) {
                    logger.debug(TAG, "Preloaded from existing full schedule cache")
                    
                    // Get all cached routine items for the full week view
                    val allDays = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
                    val allCachedItems = mutableListOf<RoutineItem>()
                    allDays.forEach { day ->
                        val dayItems = cacheService.getCachedRoutineForDay(day, user) ?: emptyList()
                        allCachedItems.addAll(dayItems)
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isCacheLoaded = true,
                        allRoutineItems = allCachedItems
                    )
                    return@launch
                }
                
                // If no full schedule cache, load from use case
                getUserRoutineUseCase(user).fold(
                    onSuccess = { allRoutineItems ->
                        logger.debug(TAG, "Successfully preloaded ${allRoutineItems.size} routine items")
                        
                        // Group by day and cache using singleton service
                        val groupedByDay = allRoutineItems.groupBy { it.day }
                        groupedByDay.forEach { (day, items) ->
                            cacheService.cacheRoutineForDay(day, user, items)
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isCacheLoaded = true,
                            allRoutineItems = allRoutineItems // Store all routine items for full week view
                        )
                        logger.debug(TAG, "Cached data for ${groupedByDay.keys.size} days")
                    },
                    onFailure = { error ->
                        logger.error(TAG, "Failed to preload routine data", error)
                    }
                )
            } catch (e: Exception) {
                logger.error(TAG, "Error preloading routine data", e)
            }
        }
    }

    private fun observeNetworkChanges() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                logger.debug(TAG, "Network status changed: $isOnline")
                _uiState.value = _uiState.value.copy(isOffline = !isOnline)
                
                // Auto-sync when coming back online
                if (isOnline && _uiState.value.hasPendingSync) {
                    logger.info(TAG, "Network restored - triggering auto-sync")
                    refreshRoutine()
                }
            }
        }
    }

    fun selectDay(day: String) {
        if (day != _uiState.value.selectedDay) {
            val user = currentUser ?: return
            logger.debug(TAG, "Day selected: $day")
            
            // Check if we have cached data for this day
            val cachedData = cacheService.getCachedRoutineForDay(day, user)
            
            if (cachedData != null) {
                logger.debug(TAG, "Using cached data for day: $day (${cachedData.size} items)")
                _uiState.value = _uiState.value.copy(
                    selectedDay = day,
                    routineItems = cachedData,
                    isLoading = false,
                    error = null
                )
            } else {
                logger.debug(TAG, "No valid cache for day: $day, trying full schedule cache")
                
                // Try to get data from full schedule cache first
                val fromFullSchedule = cacheService.getRoutineFromFullSchedule(day, user)
                if (fromFullSchedule != null) {
                    logger.debug(TAG, "Loaded day $day from full schedule cache (${fromFullSchedule.size} items)")
                    _uiState.value = _uiState.value.copy(
                        selectedDay = day,
                        routineItems = fromFullSchedule,
                        isLoading = false,
                        error = null
                    )
                } else {
                    logger.debug(TAG, "No cache available, loading from data source")
                    _uiState.value = _uiState.value.copy(
                        selectedDay = day,
                        isLoading = true,
                        error = null
                    )
                    // Fall back to observing data
                    observeRoutineForDay(day)
                }
            }
            
            logger.logUserAction("day_selected", mapOf("day" to day))
        }
    }

    fun refreshRoutine() {
        val user = currentUser ?: return
        
        viewModelScope.launch {
            logger.info(TAG, "Manual refresh triggered")
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            
            // Clear cache to ensure fresh data
            clearCache()
            
            try {
                refreshRoutineUseCase(user.department).fold(
                    onSuccess = {
                        logger.info(TAG, "Routine refresh completed successfully")
                        // Reload active days and current routine
                        loadActiveDays()
                        
                        // Load time slots
                        loadTimeSlots(user.department)
                        
                        // Load full database routine for filtering
                        loadFullDatabaseRoutine()
                        
                        // Preload fresh data
                        preloadAllDaysData()
                        
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            isOffline = false,
                            hasPendingSync = false,
                            lastSyncTime = System.currentTimeMillis()
                        )
                        
                        logger.logUserAction("routine_refreshed", mapOf(
                            "department" to user.department,
                            "success" to "true"
                        ))
                    },
                    onFailure = { throwable ->
                        val error = AppError.fromThrowable(throwable)
                        logger.error(TAG, "Routine refresh failed", throwable)
                        _uiState.value = _uiState.value.copy(
                            error = error,
                            isRefreshing = false,
                            isOffline = !networkMonitor.isCurrentlyOnline(),
                            hasPendingSync = !networkMonitor.isCurrentlyOnline()
                        )
                        
                        logger.logUserAction("routine_refreshed", mapOf(
                            "department" to user.department,
                            "success" to "false",
                            "error" to error.message
                        ))
                    }
                )
            } catch (e: Exception) {
                val error = AppError.fromThrowable(e)
                logger.error(TAG, "Error during routine refresh", e)
                _uiState.value = _uiState.value.copy(
                    error = error,
                    isRefreshing = false,
                    isOffline = !networkMonitor.isCurrentlyOnline(),
                    hasPendingSync = !networkMonitor.isCurrentlyOnline()
                )
            }
        }
    }

    private fun syncRoutineDataSilently() {
        val user = currentUser ?: return
        
        viewModelScope.launch {
            try {
                logger.debug(TAG, "Starting background sync for department: ${user.department}")
                syncRoutineUseCase(user.department).fold(
                    onSuccess = {
                        logger.info(TAG, "Background sync completed successfully")
                        _uiState.value = _uiState.value.copy(
                            isOffline = false,
                            hasPendingSync = false,
                            lastSyncTime = System.currentTimeMillis()
                        )
                    },
                    onFailure = { throwable ->
                        logger.warning(TAG, "Background sync failed", throwable)
                        _uiState.value = _uiState.value.copy(
                            isOffline = !networkMonitor.isCurrentlyOnline(),
                            hasPendingSync = !networkMonitor.isCurrentlyOnline()
                        )
                    }
                )
            } catch (e: Exception) {
                logger.warning(TAG, "Error during background sync", e)
                _uiState.value = _uiState.value.copy(
                    isOffline = !networkMonitor.isCurrentlyOnline(),
                    hasPendingSync = !networkMonitor.isCurrentlyOnline()
                )
            }
        }
    }
    
    private fun refreshRoutineData() {
        val user = currentUser ?: return
        
        viewModelScope.launch {
            logger.debug(TAG, "Refreshing routine data due to profile changes")
            android.util.Log.d("RoutineViewModel", "Refreshing routine data for updated user profile")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Load all days (including off days) and active days with the new profile
                val allDays = getAllDaysUseCase()
                
                // Load time slots for the department
                loadTimeSlots(user.department)
                
                getActiveDaysUseCase(user).fold(
                    onSuccess = { activeDays ->
                        logger.info(TAG, "Loaded ${activeDays.size} active days for updated profile: $activeDays")
                        android.util.Log.d("RoutineViewModel", "New active days after profile update: $activeDays")
                        
                        _uiState.value = _uiState.value.copy(
                            activeDays = activeDays,
                            allDays = allDays
                        )
                        
                        // Set selected day to current day (including off days)
                        val currentDay = DayOfWeek.getCurrentDay().displayName
                        val selectedDay = currentDay // Always select current day, even if it's an off day
                        
                        logger.debug(TAG, "Selected day after profile update: $selectedDay")
                        _uiState.value = _uiState.value.copy(selectedDay = selectedDay)
                        
                        // Clear cache when profile changes
                        clearCache()
                        
                        // Preload all days data for faster switching
                        preloadAllDaysData()
                        
                        // Start observing routine for selected day with new profile
                        observeRoutineForDay(selectedDay)
                    },
                    onFailure = { throwable ->
                        val error = AppError.fromThrowable(throwable)
                        logger.error(TAG, "Failed to load routine data after profile update", throwable)
                        android.util.Log.e("RoutineViewModel", "Failed to refresh routine after profile update", throwable)
                        _uiState.value = _uiState.value.copy(
                            error = error,
                            isLoading = false,
                            isOffline = !networkMonitor.isCurrentlyOnline()
                        )
                    }
                )
            } catch (e: Exception) {
                val error = AppError.fromThrowable(e)
                logger.error(TAG, "Error refreshing routine data after profile update", e)
                _uiState.value = _uiState.value.copy(
                    error = error,
                    isLoading = false,
                    isOffline = !networkMonitor.isCurrentlyOnline()
                )
            }
        }
    }

    private fun loadActiveDays() {
        val user = currentUser ?: return
        
        viewModelScope.launch {
            try {
                logger.debug(TAG, "Reloading active days")
                getActiveDaysUseCase(user).fold(
                    onSuccess = { days ->
                        logger.debug(TAG, "Reloaded ${days.size} active days")
                        _uiState.value = _uiState.value.copy(activeDays = days)
                    },
                    onFailure = { throwable ->
                        val error = AppError.fromThrowable(throwable)
                        logger.error(TAG, "Failed to reload active days", throwable)
                        _uiState.value = _uiState.value.copy(error = error)
                    }
                )
            } catch (e: Exception) {
                val error = AppError.fromThrowable(e)
                logger.error(TAG, "Error reloading active days", e)
                _uiState.value = _uiState.value.copy(error = error)
            }
        }
    }
    
    private fun loadTimeSlots(department: String) {
        viewModelScope.launch {
            try {
                logger.debug(TAG, "Loading time slots for department: $department")
                getAllTimeSlotsUseCase(department).fold(
                    onSuccess = { timeSlots ->
                        logger.debug(TAG, "Loaded ${timeSlots.size} time slots")
                        _uiState.value = _uiState.value.copy(allTimeSlots = timeSlots)
                    },
                    onFailure = { throwable ->
                        logger.error(TAG, "Failed to load time slots", throwable)
                        // Don't set error state for time slots as it's not critical
                    }
                )
            } catch (e: Exception) {
                logger.error(TAG, "Error loading time slots", e)
                // Don't set error state for time slots as it's not critical
            }
        }
    }
    
    private fun loadFullDatabaseRoutine() {
        val user = currentUser ?: return
        
        viewModelScope.launch {
            try {
                logger.debug(TAG, "Loading full database routine for department: ${user.department}")
                getFullRoutineScheduleUseCase(user.department).fold(
                    onSuccess = { fullRoutineItems ->
                        logger.info(TAG, "Loaded ${fullRoutineItems.size} full database routine items")
                        _uiState.value = _uiState.value.copy(fullDatabaseRoutineItems = fullRoutineItems)
                    },
                    onFailure = { throwable ->
                        logger.error(TAG, "Failed to load full database routine", throwable)
                        // Don't set error state as it's not critical for filtering
                    }
                )
            } catch (e: Exception) {
                logger.error(TAG, "Error loading full database routine", e)
                // Don't set error state as it's not critical for filtering
            }
        }
    }

    fun clearError() {
        logger.debug(TAG, "Clearing error state")
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun retryLastAction() {
        logger.debug(TAG, "Retrying last action")
        _uiState.value = _uiState.value.copy(error = null, isLoading = true)
        loadInitialData()
    }

    // Helper functions for UI
    fun getTodayRoutine(): List<RoutineItem> {
        val today = DayOfWeek.getCurrentDay().displayName
        return if (_uiState.value.selectedDay == today) {
            _uiState.value.routineItems
        } else {
            emptyList()
        }
    }

    fun hasClassesOnDay(day: String): Boolean {
        return _uiState.value.activeDays.contains(day)
    }

    fun getNextClass(): RoutineItem? {
        val today = DayOfWeek.getCurrentDay().displayName
        val todayRoutine = if (_uiState.value.selectedDay == today) {
            _uiState.value.routineItems
        } else {
            return null
        }
        
        val currentTime = LocalTime.now()
        return todayRoutine
            .sortedBy { it.startTime }
            .find { routineItem ->
                val startTime = routineItem.startTime
                startTime != null && startTime.isAfter(currentTime)
            }
    }

    fun getCurrentClass(): RoutineItem? {
        val today = DayOfWeek.getCurrentDay().displayName
        val todayRoutine = if (_uiState.value.selectedDay == today) {
            _uiState.value.routineItems
        } else {
            return null
        }
        
        val currentTime = LocalTime.now()
        return todayRoutine.find { routineItem ->
            val startTime = routineItem.startTime
            val endTime = routineItem.endTime
            startTime != null && endTime != null && 
            currentTime.isAfter(startTime) && currentTime.isBefore(endTime)
        }
    }

    fun getClassStatus(routineItem: RoutineItem): ClassStatus {
        val currentTime = LocalTime.now()
        val startTime = routineItem.startTime
        val endTime = routineItem.endTime
        
        return when {
            startTime == null || endTime == null -> ClassStatus.UNKNOWN
            currentTime.isBefore(startTime) -> ClassStatus.UPCOMING
            currentTime.isAfter(startTime) && currentTime.isBefore(endTime) -> ClassStatus.ONGOING
            currentTime.isAfter(endTime) -> ClassStatus.COMPLETED
            else -> ClassStatus.UNKNOWN
        }
    }

    private fun clearCache() {
        logger.debug(TAG, "Clearing routine cache")
        val user = currentUser
        if (user != null) {
            cacheService.clearCacheForUser(user)
        } else {
            cacheService.clearAllCache()
        }
        _uiState.value = _uiState.value.copy(isCacheLoaded = false)
    }

    // Filtering methods
    fun applyFilter(filter: RoutineFilter) {
        logger.debug(TAG, "Applying filter: ${filter.type} - ${filter.getDisplayText()}")
        
        val allItems = _uiState.value.fullDatabaseRoutineItems
        
        // If full database routine is not loaded yet, load it first
        if (allItems.isEmpty()) {
            logger.debug(TAG, "Full database routine not loaded yet, loading now...")
            loadFullDatabaseRoutine()
            return
        }
        val filteredItems = when (filter.type) {
            FilterType.STUDENT -> {
                allItems.filter { item ->
                    // For student filter, match the provided criteria
                    val batchMatches = if (filter.batch.isNullOrBlank()) {
                        true // If no batch specified, don't filter by batch
                    } else {
                        item.batch.equals(filter.batch, ignoreCase = true)
                    }
                    
                    val sectionMatches = if (filter.section.isNullOrBlank()) {
                        true // If no section specified, don't filter by section
                    } else {
                        item.section.equals(filter.section, ignoreCase = true)
                    }
                    

                    batchMatches && sectionMatches
                }
            }
            FilterType.TEACHER -> {
                allItems.filter { item ->
                    // For teacher filter, initial must match exactly
                    if (filter.teacherInitial.isNullOrBlank()) {
                        false // Must provide a teacher initial
                    } else {
                        item.teacherInitial.equals(filter.teacherInitial, ignoreCase = true)
                    }
                }
            }
            FilterType.ROOM -> {
                allItems.filter { item ->
                    // For room filter, room must match exactly
                    if (filter.room.isNullOrBlank()) {
                        false // Must provide a room
                    } else {
                        item.room.equals(filter.room, ignoreCase = true)
                    }
                }
            }
        }
        
        _uiState.value = _uiState.value.copy(
            filteredRoutineItems = filteredItems,
            currentFilter = filter,
            isFiltered = true
        )
        
        // Debug logging for filtering
        logger.debug(TAG, "Filter applied: ${filteredItems.size} items from ${allItems.size} total items match filter")
        logger.debug(TAG, "Filter details - Type: ${filter.type}, Batch: '${filter.batch}', Section: '${filter.section}', Initial: '${filter.teacherInitial}', Room: '${filter.room}'")
        
        if (allItems.size <= 10) {
            allItems.forEachIndexed { index, item ->
                logger.debug(TAG, "Item $index: batch='${item.batch}', section='${item.section}', initial='${item.teacherInitial}', room='${item.room}', course='${item.courseCode}'")
            }
        }
        
        logger.info(TAG, "Filter applied: Found ${filteredItems.size} matches out of ${allItems.size} total items for ${filter.type} filter")
    }

    fun clearFilter() {
        logger.debug(TAG, "Clearing filter")
        _uiState.value = _uiState.value.copy(
            filteredRoutineItems = emptyList(),
            currentFilter = null,
            isFiltered = false
        )
    }

    fun getDisplayRoutineItems(): List<RoutineItem> {
        return if (_uiState.value.isFiltered) {
            _uiState.value.filteredRoutineItems
        } else {
            _uiState.value.allRoutineItems
        }
    }

    fun getDefaultFilterText(): String {
        val user = currentUser ?: return "All"
        return when (user.role.name) {
            "STUDENT" -> {
                val batch = user.batch?.takeIf { it.isNotBlank() } ?: ""
                val section = user.section?.takeIf { it.isNotBlank() } ?: ""
                if (batch.isNotEmpty() && section.isNotEmpty()) {
                    "$batch-$section"
                } else "All"
            }
            "TEACHER" -> {
                user.initial?.takeIf { it.isNotBlank() } ?: "All"
            }
            else -> "All"
        }
    }

    override fun onCleared() {
        super.onCleared()
        logger.debug(TAG, "RoutineViewModel cleared")
        clearCache()
    }
}

enum class ClassStatus {
    UPCOMING, ONGOING, COMPLETED, UNKNOWN
}

data class RoutineFilter(
    val type: FilterType,
    val batch: String? = null,
    val section: String? = null,
    val teacherInitial: String? = null,
    val room: String? = null
) {
    fun getDisplayText(): String {
        return when (type) {
            FilterType.STUDENT -> "${batch}-${section}"
            FilterType.TEACHER -> teacherInitial ?: ""
            FilterType.ROOM -> room ?: ""
        }
    }
}

enum class FilterType {
    STUDENT, TEACHER, ROOM
}