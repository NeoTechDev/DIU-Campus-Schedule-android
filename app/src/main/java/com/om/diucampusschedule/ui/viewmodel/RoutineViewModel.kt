package com.om.diucampusschedule.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.diucampusschedule.core.error.AppError
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.core.network.NetworkMonitor
import com.om.diucampusschedule.domain.model.DayOfWeek
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.usecase.auth.GetCurrentUserUseCase
import com.om.diucampusschedule.domain.usecase.routine.GetActiveDaysUseCase
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
import kotlinx.coroutines.flow.combine
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
    val routineItems: List<RoutineItem> = emptyList(),
    val activeDays: List<String> = emptyList(),
    val selectedDay: String = DayOfWeek.getCurrentDay().displayName,
    val currentUser: User? = null,
    val error: AppError? = null,
    val lastSyncTime: Long = 0L
)

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserRoutineUseCase: GetUserRoutineUseCase,
    private val getUserRoutineForDayUseCase: GetUserRoutineForDayUseCase,
    private val observeUserRoutineForDayUseCase: ObserveUserRoutineForDayUseCase,
    private val getActiveDaysUseCase: GetActiveDaysUseCase,
    private val syncRoutineUseCase: SyncRoutineUseCase,
    private val refreshRoutineUseCase: RefreshRoutineUseCase,
    private val networkMonitor: NetworkMonitor,
    private val logger: AppLogger
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
                // Load active days first
                getActiveDaysUseCase(user).fold(
                    onSuccess = { days ->
                        logger.info(TAG, "Loaded ${days.size} active days: $days")
                        _uiState.value = _uiState.value.copy(activeDays = days)
                        
                        // Set selected day to current day if it's active, otherwise first active day
                        val currentDay = DayOfWeek.getCurrentDay().displayName
                        val selectedDay = if (days.contains(currentDay)) {
                            currentDay
                        } else {
                            days.firstOrNull() ?: currentDay
                        }
                        
                        logger.debug(TAG, "Selected initial day: $selectedDay")
                        _uiState.value = _uiState.value.copy(selectedDay = selectedDay)
                        
                        // Start observing routine for selected day
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
                    isOffline = !networkMonitor.isCurrentlyOnline()
                )
            }
            .onEach { routineItems ->
                logger.debug(TAG, "Received ${routineItems.size} routine items for $day")
                android.util.Log.d("RoutineViewModel", "Received ${routineItems.size} routine items for $day")
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
            logger.debug(TAG, "Day selected: $day")
            _uiState.value = _uiState.value.copy(
                selectedDay = day,
                isLoading = true,
                error = null
            )
            observeRoutineForDay(day)
            
            logger.logUserAction("day_selected", mapOf("day" to day))
        }
    }

    fun refreshRoutine() {
        val user = currentUser ?: return
        
        viewModelScope.launch {
            logger.info(TAG, "Manual refresh triggered")
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            
            try {
                refreshRoutineUseCase(user.department).fold(
                    onSuccess = {
                        logger.info(TAG, "Routine refresh completed successfully")
                        // Reload active days and current routine
                        loadActiveDays()
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
                // Load active days first with the new profile
                getActiveDaysUseCase(user).fold(
                    onSuccess = { days ->
                        logger.info(TAG, "Loaded ${days.size} active days for updated profile: $days")
                        android.util.Log.d("RoutineViewModel", "New active days after profile update: $days")
                        _uiState.value = _uiState.value.copy(activeDays = days)
                        
                        // Set selected day to current day if it's active, otherwise first active day
                        val currentDay = DayOfWeek.getCurrentDay().displayName
                        val selectedDay = if (days.contains(currentDay)) {
                            currentDay
                        } else {
                            days.firstOrNull() ?: currentDay
                        }
                        
                        logger.debug(TAG, "Selected day after profile update: $selectedDay")
                        _uiState.value = _uiState.value.copy(selectedDay = selectedDay)
                        
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

    override fun onCleared() {
        super.onCleared()
        logger.debug(TAG, "RoutineViewModel cleared")
    }
}

enum class ClassStatus {
    UPCOMING, ONGOING, COMPLETED, UNKNOWN
}