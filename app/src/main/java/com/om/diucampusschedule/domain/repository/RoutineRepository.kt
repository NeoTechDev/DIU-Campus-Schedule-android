package com.om.diucampusschedule.domain.repository

import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.RoutineSchedule
import com.om.diucampusschedule.domain.model.User
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    
    // Get routine data for a specific user
    suspend fun getRoutineForUser(user: User): Result<List<RoutineItem>>
    
    // Get routine data for a specific user and day
    suspend fun getRoutineForUserAndDay(user: User, day: String): Result<List<RoutineItem>>
    
    // Observe routine changes for a specific user and day
    fun observeRoutineForUserAndDay(user: User, day: String): Flow<List<RoutineItem>>
    
    // Get all active days for a user
    suspend fun getActiveDaysForUser(user: User): Result<List<String>>
    
    // Get all time slots for a department
    suspend fun getAllTimeSlotsForDepartment(department: String): Result<List<String>>
    
    // Get the latest routine schedule for a department
    suspend fun getLatestScheduleForDepartment(department: String): Result<RoutineSchedule>
    
    // Observe routine schedule changes for a department
    fun observeLatestScheduleForDepartment(department: String): Flow<RoutineSchedule?>
    
    // Sync routine data from remote source
    suspend fun syncRoutineData(department: String): Result<Unit>
    
    // Check for routine updates
    suspend fun checkForUpdates(department: String): Result<Boolean>
    
    // Force refresh from remote
    suspend fun refreshFromRemote(department: String): Result<RoutineSchedule>
    
    // Clear local routine data
    suspend fun clearLocalData(department: String): Result<Unit>
    
    // Get sync status
    suspend fun getSyncStatus(department: String): Result<Boolean>
}
