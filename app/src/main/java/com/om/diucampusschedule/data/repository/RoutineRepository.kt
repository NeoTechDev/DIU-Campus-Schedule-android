package com.om.diucampusschedule.data.repository

import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.usecase.routine.GetUserRoutineForDayUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

data class WeekRoutineCache(
    val data: List<RoutineItem>,
    val timestamp: Long,
    val userId: String
)

@Singleton
class RoutineRepository @Inject constructor(
    private val getUserRoutineForDayUseCase: GetUserRoutineForDayUseCase
) {
    
    private var weekRoutineCache: WeekRoutineCache? = null
    private val cacheExpirationTime = 10 * 60 * 1000L // 10 minutes
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * Get all routine items for the week with proper caching
     * Returns a Flow for reactive updates
     */
    fun getWeekRoutineItems(user: User): Flow<List<RoutineItem>> = flow {
        val currentTime = System.currentTimeMillis()
        val cache = weekRoutineCache
        
        // Check if cache is valid
        if (cache != null && 
            cache.userId == user.id.toString() && 
            currentTime - cache.timestamp < cacheExpirationTime) {
            emit(cache.data)
            return@flow
        }
        
        // Cache miss or expired - fetch fresh data
        _isLoading.value = true
        
        try {
            val routineItems = fetchWeekRoutineItems(user)
            
            // Update cache
            weekRoutineCache = WeekRoutineCache(
                data = routineItems,
                timestamp = currentTime,
                userId = user.id.toString()
            )
            
            emit(routineItems)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Force refresh routine data
     */
    suspend fun refreshWeekRoutineItems(user: User): List<RoutineItem> {
        invalidateCache()
        _isLoading.value = true
        
        return try {
            val routineItems = fetchWeekRoutineItems(user)
            
            weekRoutineCache = WeekRoutineCache(
                data = routineItems,
                timestamp = System.currentTimeMillis(),
                userId = user.id.toString()
            )
            
            routineItems
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Get cached data immediately if available
     */
    fun getCachedWeekRoutineItems(user: User): List<RoutineItem>? {
        val cache = weekRoutineCache
        val currentTime = System.currentTimeMillis()
        
        return if (cache != null && 
                   cache.userId == user.id.toString() && 
                   currentTime - cache.timestamp < cacheExpirationTime) {
            cache.data
        } else {
            null
        }
    }
    
    /**
     * Invalidate cache (e.g., when user logs out or data changes)
     */
    fun invalidateCache() {
        weekRoutineCache = null
    }
    
    /**
     * Private method to fetch routine items for all days concurrently
     */
    private suspend fun fetchWeekRoutineItems(user: User): List<RoutineItem> = coroutineScope {
        val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        
        val deferredResults = daysOfWeek.map { dayName ->
            async {
                try {
                    getUserRoutineForDayUseCase(user, dayName).fold(
                        onSuccess = { routineItems -> routineItems },
                        onFailure = { 
                            android.util.Log.w("RoutineRepository", "Failed to load routine for $dayName")
                            emptyList()
                        }
                    )
                } catch (e: Exception) {
                    android.util.Log.w("RoutineRepository", "Error loading routine for $dayName", e)
                    emptyList()
                }
            }
        }
        
        val allRoutineItems = mutableListOf<RoutineItem>()
        deferredResults.forEach { deferred ->
            allRoutineItems.addAll(deferred.await())
        }
        
        allRoutineItems.distinctBy { "${it.day}-${it.time}-${it.courseCode}-${it.room}" }
    }
}
