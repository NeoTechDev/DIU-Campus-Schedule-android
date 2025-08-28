package com.om.diucampusschedule.core.cache

import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.RoutineSchedule
import com.om.diucampusschedule.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class RoutineCacheEntry(
    val routineItems: List<RoutineItem>,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String
) {
    fun isExpired(expiryTimeMs: Long = 5 * 60 * 1000): Boolean { // 5 minutes default
        return System.currentTimeMillis() - timestamp > expiryTimeMs
    }
    
    fun isForUser(user: User): Boolean {
        return userId == user.id
    }
}

data class FullScheduleCache(
    val schedule: RoutineSchedule,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String
) {
    fun isExpired(expiryTimeMs: Long = 30 * 60 * 1000): Boolean { // 30 minutes default
        return System.currentTimeMillis() - timestamp > expiryTimeMs
    }
    
    fun isForUser(user: User): Boolean {
        return userId == user.id
    }
}

@Singleton
class RoutineCacheService @Inject constructor() {
    
    // Cache for day-specific routine data
    private val dayRoutineCache = mutableMapOf<String, RoutineCacheEntry>()
    
    // Cache for complete routine schedule
    private var fullScheduleCache: FullScheduleCache? = null
    
    // Cache for active days
    private var activeDaysCache: Pair<List<String>, String>? = null // (days, userId)
    private var activeDaysTimestamp: Long = 0L
    
    // Cache state flow for reactive updates
    private val _cacheUpdateFlow = MutableStateFlow(System.currentTimeMillis())
    val cacheUpdateFlow: StateFlow<Long> = _cacheUpdateFlow.asStateFlow()
    
    /**
     * Get cached routine for a specific day
     */
    fun getCachedRoutineForDay(day: String, user: User): List<RoutineItem>? {
        val cacheKey = "${user.id}_$day"
        val cachedEntry = dayRoutineCache[cacheKey]
        
        return if (cachedEntry != null && !cachedEntry.isExpired() && cachedEntry.isForUser(user)) {
            cachedEntry.routineItems
        } else {
            null
        }
    }
    
    /**
     * Cache routine data for a specific day
     */
    fun cacheRoutineForDay(day: String, user: User, routineItems: List<RoutineItem>) {
        val cacheKey = "${user.id}_$day"
        dayRoutineCache[cacheKey] = RoutineCacheEntry(routineItems, userId = user.id)
        _cacheUpdateFlow.value = System.currentTimeMillis()
    }
    
    /**
     * Get cached full schedule
     */
    fun getCachedFullSchedule(user: User): RoutineSchedule? {
        val cached = fullScheduleCache
        return if (cached != null && !cached.isExpired() && cached.isForUser(user)) {
            cached.schedule
        } else {
            null
        }
    }
    
    /**
     * Cache full schedule
     */
    fun cacheFullSchedule(user: User, schedule: RoutineSchedule) {
        fullScheduleCache = FullScheduleCache(schedule, userId = user.id)
        _cacheUpdateFlow.value = System.currentTimeMillis()
    }
    
    /**
     * Get cached active days
     */
    fun getCachedActiveDays(user: User): List<String>? {
        val cached = activeDaysCache
        val age = System.currentTimeMillis() - activeDaysTimestamp
        
        return if (cached != null && cached.second == user.id && age < 30 * 60 * 1000) { // 30 minutes
            cached.first
        } else {
            null
        }
    }
    
    /**
     * Cache active days
     */
    fun cacheActiveDays(user: User, activeDays: List<String>) {
        activeDaysCache = activeDays to user.id
        activeDaysTimestamp = System.currentTimeMillis()
        _cacheUpdateFlow.value = System.currentTimeMillis()
    }
    
    /**
     * Try to get routine for day from full schedule cache
     */
    fun getRoutineFromFullSchedule(day: String, user: User): List<RoutineItem>? {
        val schedule = getCachedFullSchedule(user) ?: return null
        
        return try {
            val dayRoutine = schedule.getRoutineForDay(day, user)
            // Cache this day's data for faster future access
            cacheRoutineForDay(day, user, dayRoutine)
            dayRoutine
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Preload all days data from full schedule
     */
    fun preloadAllDaysFromSchedule(user: User): Boolean {
        val schedule = getCachedFullSchedule(user) ?: return false
        
        return try {
            val allDays = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
            
            allDays.forEach { day ->
                val dayRoutine = schedule.getRoutineForDay(day, user)
                cacheRoutineForDay(day, user, dayRoutine)
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Clear all cache for a specific user
     */
    fun clearCacheForUser(user: User) {
        // Remove day-specific cache for this user
        dayRoutineCache.entries.removeAll { it.value.userId == user.id }
        
        // Remove full schedule cache if it belongs to this user
        if (fullScheduleCache?.userId == user.id) {
            fullScheduleCache = null
        }
        
        // Remove active days cache if it belongs to this user
        if (activeDaysCache?.second == user.id) {
            activeDaysCache = null
            activeDaysTimestamp = 0L
        }
        
        _cacheUpdateFlow.value = System.currentTimeMillis()
    }
    
    /**
     * Clear all cache
     */
    fun clearAllCache() {
        dayRoutineCache.clear()
        fullScheduleCache = null
        activeDaysCache = null
        activeDaysTimestamp = 0L
        _cacheUpdateFlow.value = System.currentTimeMillis()
    }
    
    /**
     * Get cache statistics for debugging
     */
    fun getCacheStats(): Map<String, Any> {
        return mapOf(
            "dayRoutineCacheSize" to dayRoutineCache.size,
            "hasFullScheduleCache" to (fullScheduleCache != null),
            "hasActiveDaysCache" to (activeDaysCache != null),
            "fullScheduleCacheAge" to (fullScheduleCache?.let { System.currentTimeMillis() - it.timestamp } ?: 0),
            "activeDaysCacheAge" to (System.currentTimeMillis() - activeDaysTimestamp)
        )
    }
}
