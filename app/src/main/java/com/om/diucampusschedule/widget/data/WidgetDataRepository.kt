package com.om.diucampusschedule.widget.data

import com.om.diucampusschedule.core.service.CourseNameService
import com.om.diucampusschedule.data.repository.RoutineRepository
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.usecase.auth.GetCurrentUserUseCase
import com.om.diucampusschedule.domain.usecase.routine.GetUserRoutineForDayUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for widget data management
 * Provides clean interface for widget to access user routine data
 */
@Singleton
class WidgetDataRepository @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserRoutineForDayUseCase: GetUserRoutineForDayUseCase,
    private val courseNameService: CourseNameService,
    private val routineRepository: RoutineRepository
) {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _lastUpdated = MutableStateFlow(0L)
    val lastUpdated: StateFlow<Long> = _lastUpdated.asStateFlow()
    
    /**
     * Get current user
     */
    fun getCurrentUser(): Flow<User?> {
        return getCurrentUserUseCase.observeCurrentUser()
            .catch { emit(null) }
            .distinctUntilChanged()
    }
    
    /**
     * Get today's classes for the current user
     */
    fun getTodayClasses(): Flow<List<RoutineItem>> {
        return getCurrentUser().flatMapLatest { user ->
            if (user != null) {
                getTodayClassesForUser(user)
            } else {
                flowOf(emptyList())
            }
        }.distinctUntilChanged()
    }
    
    /**
     * Get today's classes for a specific user
     */
    private fun getTodayClassesForUser(user: User): Flow<List<RoutineItem>> = flow {
        _isLoading.value = true
        
        try {
            val today = LocalDate.now()
            val dayName = today.format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH))
            
            // Get today's routine
            val result = getUserRoutineForDayUseCase(user, dayName)
            
            result.fold(
                onSuccess = { routineItems ->
                    // Sort by start time and emit
                    val sortedItems = routineItems.sortedBy { it.startTime }
                    emit(sortedItems)
                    _lastUpdated.value = System.currentTimeMillis()
                },
                onFailure = { throwable ->
                    android.util.Log.e("WidgetDataRepository", "Error loading routine", throwable)
                    emit(emptyList())
                }
            )
        } catch (e: Exception) {
            android.util.Log.e("WidgetDataRepository", "Error in getTodayClassesForUser", e)
            emit(emptyList())
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Get course name for a course code
     */
    suspend fun getCourseName(courseCode: String): String? {
        return try {
            courseNameService.getCourseName(courseCode)
        } catch (e: Exception) {
            android.util.Log.e("WidgetDataRepository", "Error getting course name for $courseCode", e)
            null
        }
    }

    /**
     * Force refresh widget data
     */
    suspend fun refreshData(): List<RoutineItem> {
        return try {
            val user = getCurrentUser().first() ?: return emptyList()
            
            _isLoading.value = true
            
            // Clear any cached data
            routineRepository.invalidateCache()
            
            // Get fresh data
            val today = LocalDate.now()
            val dayName = today.format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH))
            
            val result = getUserRoutineForDayUseCase(user, dayName)
            result.fold(
                onSuccess = { routineItems ->
                    val sortedItems = routineItems.sortedBy { it.startTime }
                    _lastUpdated.value = System.currentTimeMillis()
                    sortedItems
                },
                onFailure = { throwable ->
                    android.util.Log.e("WidgetDataRepository", "Error refreshing data", throwable)
                    emptyList()
                }
            )
        } catch (e: Exception) {
            android.util.Log.e("WidgetDataRepository", "Error in refreshData", e)
            emptyList()
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Check if data needs refresh (older than 30 minutes)
     */
    fun needsRefresh(): Boolean {
        val now = System.currentTimeMillis()
        val lastUpdate = _lastUpdated.value
        val thirtyMinutes = 30 * 60 * 1000L // 30 minutes in milliseconds
        
        return (now - lastUpdate) > thirtyMinutes
    }

}

