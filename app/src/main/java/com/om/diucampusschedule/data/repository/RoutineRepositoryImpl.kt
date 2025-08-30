package com.om.diucampusschedule.data.repository

import com.om.diucampusschedule.data.local.RoutineLocalDataSource
import com.om.diucampusschedule.data.remote.RoutineRemoteDataSource
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.RoutineSchedule
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepositoryImpl @Inject constructor(
    private val remoteDataSource: RoutineRemoteDataSource,
    private val localDataSource: RoutineLocalDataSource
) : RoutineRepository {

    override suspend fun getRoutineForUser(user: User): Result<List<RoutineItem>> {
        return try {
            // First try to get from local storage
            val localRoutine = localDataSource.getRoutineForUser(user)
            
            if (localRoutine.isNotEmpty()) {
                // Check if we need to sync in the background
                syncRoutineDataInBackground(user.department)
                Result.success(localRoutine)
            } else {
                // No local data, fetch from remote
                syncRoutineData(user.department).fold(
                    onSuccess = {
                        val updatedRoutine = localDataSource.getRoutineForUser(user)
                        Result.success(updatedRoutine)
                    },
                    onFailure = { error ->
                        Result.failure(error)
                    }
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRoutineForUserAndDay(user: User, day: String): Result<List<RoutineItem>> {
        return try {
            android.util.Log.d("RoutineRepository", "Getting routine for user: ${user.name}, day: $day")
            
            // TEMPORARY: Force remote fetch to bypass local issues
            android.util.Log.d("RoutineRepository", "FORCING remote fetch for day $day debugging")
            refreshFromRemote(user.department).fold(
                onSuccess = { schedule ->
                    android.util.Log.d("RoutineRepository", "Remote fetch successful, schedule has ${schedule.schedule.size} total items")
                    android.util.Log.d("RoutineRepository", "Filtering for day: $day")
                    val dayRoutine = schedule.getRoutineForDay(day, user)
                    android.util.Log.d("RoutineRepository", "Routine items for $day: ${dayRoutine.size}")
                    Result.success(dayRoutine)
                },
                onFailure = { error ->
                    android.util.Log.e("RoutineRepository", "Remote fetch failed for day $day", error)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            android.util.Log.e("RoutineRepository", "Error getting routine for day $day", e)
            Result.failure(e)
        }
    }

    override fun observeRoutineForUserAndDay(user: User, day: String): Flow<List<RoutineItem>> {
        android.util.Log.d("RoutineRepository", "Starting to observe routine for user: ${user.name}, day: $day")
        
        // Use local data source's observe method for real-time updates
        return localDataSource.observeRoutineForUserAndDay(user, day)
            .catch { throwable ->
                android.util.Log.e("RoutineRepository", "Error observing local routine for day $day", throwable)
                emit(emptyList<RoutineItem>())
            }
    }

    override suspend fun getActiveDaysForUser(user: User): Result<List<String>> {
        return try {
            android.util.Log.d("RoutineRepository", "Getting active days for user: ${user.name}, department: ${user.department}")
            
            // First try to get from local storage
            val localSchedule = localDataSource.getLatestScheduleForDepartment(user.department)
            
            if (localSchedule != null) {
                android.util.Log.d("RoutineRepository", "Using cached schedule with ${localSchedule.schedule.size} total items")
                val activeDays = localSchedule.getActiveDaysForUser(user)
                android.util.Log.d("RoutineRepository", "Active days from cache: $activeDays")
                
                // Sync in background to ensure we have latest data
                syncRoutineDataInBackground(user.department)
                
                Result.success(activeDays)
            } else {
                // No local data, fetch from remote
                android.util.Log.d("RoutineRepository", "No cached data, fetching from remote")
                refreshFromRemote(user.department).fold(
                    onSuccess = { schedule ->
                        val activeDays = schedule.getActiveDaysForUser(user)
                        android.util.Log.d("RoutineRepository", "Active days from remote: $activeDays")
                        Result.success(activeDays)
                    },
                    onFailure = { error ->
                        android.util.Log.e("RoutineRepository", "Remote fetch failed", error)
                        Result.failure(error)
                    }
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("RoutineRepository", "Error getting active days", e)
            Result.failure(e)
        }
    }

    override suspend fun getAllTimeSlotsForDepartment(department: String): Result<List<String>> {
        return try {
            android.util.Log.d("RoutineRepository", "Getting all time slots for department: $department")
            
            // First try to get from local storage
            val localSchedule = localDataSource.getLatestScheduleForDepartment(department)
            
            if (localSchedule != null) {
                val timeSlots = localDataSource.getAllTimeSlotsForDepartment(department)
                android.util.Log.d("RoutineRepository", "Found ${timeSlots.size} time slots from cache")
                
                // Sync in background to ensure we have latest data
                syncRoutineDataInBackground(department)
                
                Result.success(timeSlots)
            } else {
                // No local data, fetch from remote
                android.util.Log.d("RoutineRepository", "No cached data, fetching from remote")
                refreshFromRemote(department).fold(
                    onSuccess = { _ ->
                        val timeSlots = localDataSource.getAllTimeSlotsForDepartment(department)
                        android.util.Log.d("RoutineRepository", "Found ${timeSlots.size} time slots from remote")
                        Result.success(timeSlots)
                    },
                    onFailure = { error ->
                        android.util.Log.e("RoutineRepository", "Remote fetch failed for time slots", error)
                        Result.failure(error)
                    }
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("RoutineRepository", "Error getting time slots", e)
            Result.failure(e)
        }
    }

    override suspend fun getLatestScheduleForDepartment(department: String): Result<RoutineSchedule> {
        return try {
            // First try to get from local storage
            val localSchedule = localDataSource.getLatestScheduleForDepartment(department)
            
            if (localSchedule != null) {
                // Check if we need to sync in the background
                syncRoutineDataInBackground(department)
                Result.success(localSchedule)
            } else {
                // No local data, fetch from remote
                refreshFromRemote(department)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeLatestScheduleForDepartment(department: String): Flow<RoutineSchedule?> {
        return flow {
            // Start by emitting local data
            val localSchedule = localDataSource.getLatestScheduleForDepartment(department)
            emit(localSchedule)
            
            // Then sync in background if needed
            syncRoutineDataInBackground(department)
            
            // Observe local changes
            localDataSource.observeLatestScheduleForDepartment(department)
                .collect { updatedSchedule ->
                    emit(updatedSchedule)
                }
        }.catch { e ->
            // Fall back to local data only
            localDataSource.observeLatestScheduleForDepartment(department)
                .collect { localSchedule ->
                    emit(localSchedule)
                }
        }
    }

    override suspend fun syncRoutineData(department: String): Result<Unit> {
        return try {
            android.util.Log.d("RoutineRepository", "Starting sync for department: $department")
            
            // Get current local version - use the highest version between schedule and metadata
            val localSchedule = localDataSource.getLatestScheduleForDepartment(department)
            val localScheduleVersion = localSchedule?.version ?: 0L
            
            // Get the last known metadata version from local storage
            val currentVersion = maxOf(localScheduleVersion, getLastKnownMetadataVersion())
            
            android.util.Log.d("RoutineRepository", "Current local version: $currentVersion (schedule: $localScheduleVersion)")
            
            // Check if there are updates (this now includes metadata version checking)
            remoteDataSource.checkForUpdates(department, currentVersion).fold(
                onSuccess = { hasUpdates ->
                    android.util.Log.d("RoutineRepository", "Has updates: $hasUpdates")
                    
                    if (hasUpdates || localSchedule == null) {
                        // Fetch latest from remote
                        remoteDataSource.getLatestRoutineForDepartment(department).fold(
                            onSuccess = { remoteSchedule ->
                                android.util.Log.d("RoutineRepository", "Successfully fetched remote schedule with ${remoteSchedule.schedule.size} items")
                                
                                // IMPORTANT: Clear existing data first to handle deletions properly
                                localDataSource.clearRoutinesForDepartment(department)
                                android.util.Log.d("RoutineRepository", "Cleared existing local data before saving new data")
                                
                                // Update the schedule version with current metadata version if it's higher
                                val metadataVersionResult = remoteDataSource.getCurrentMetadataVersion()
                                val finalVersion = if (metadataVersionResult.isSuccess) {
                                    maxOf(remoteSchedule.version, metadataVersionResult.getOrNull() ?: remoteSchedule.version)
                                } else {
                                    remoteSchedule.version
                                }
                                
                                val updatedSchedule = remoteSchedule.copy(version = finalVersion)
                                
                                // Save fresh data to local storage
                                localDataSource.saveSchedule(updatedSchedule)
                                
                                // Store the metadata version for future comparisons
                                storeLastKnownMetadataVersion(finalVersion)
                                
                                android.util.Log.d("RoutineRepository", "Sync completed successfully with version: $finalVersion")
                                Result.success(Unit)
                            },
                            onFailure = { error ->
                                android.util.Log.e("RoutineRepository", "Failed to fetch remote schedule", error)
                                
                                // Handle case where all routines might have been deleted
                                if (error.message?.contains("No documents found", ignoreCase = true) == true) {
                                    android.util.Log.w("RoutineRepository", "No remote data - clearing local data for deletions")
                                    localDataSource.clearRoutinesForDepartment(department)
                                    
                                    // Update metadata version even for deletions
                                    val metadataVersionResult = remoteDataSource.getCurrentMetadataVersion()
                                    if (metadataVersionResult.isSuccess) {
                                        storeLastKnownMetadataVersion(metadataVersionResult.getOrNull() ?: System.currentTimeMillis())
                                    }
                                    
                                    Result.success(Unit)
                                } else {
                                    Result.failure(error)
                                }
                            }
                        )
                    } else {
                        // No updates needed
                        android.util.Log.d("RoutineRepository", "No updates needed")
                        Result.success(Unit)
                    }
                },
                onFailure = { error ->
                    android.util.Log.e("RoutineRepository", "Failed to check for updates", error)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            android.util.Log.e("RoutineRepository", "Sync error", e)
            Result.failure(e)
        }
    }

    override suspend fun checkForUpdates(department: String): Result<Boolean> {
        return try {
            val localSchedule = localDataSource.getLatestScheduleForDepartment(department)
            val localScheduleVersion = localSchedule?.version ?: 0L
            val currentVersion = maxOf(localScheduleVersion, getLastKnownMetadataVersion())
            
            android.util.Log.d("RoutineRepository", "Checking updates with version: $currentVersion")
            remoteDataSource.checkForUpdates(department, currentVersion)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshFromRemote(department: String): Result<RoutineSchedule> {
        return try {
            android.util.Log.d("RoutineRepository", "Refreshing from remote for department: $department")
            
            remoteDataSource.getLatestRoutineForDepartment(department).fold(
                onSuccess = { remoteSchedule ->
                    android.util.Log.d("RoutineRepository", "Remote schedule fetched with ${remoteSchedule.schedule.size} items")
                    
                    // IMPORTANT: Clear existing data first to handle deletions
                    localDataSource.clearRoutinesForDepartment(department)
                    android.util.Log.d("RoutineRepository", "Cleared existing local data for department: $department")
                    
                    // Save fresh data to local storage
                    localDataSource.saveSchedule(remoteSchedule)
                    android.util.Log.d("RoutineRepository", "Saved fresh schedule to local storage")
                    
                    Result.success(remoteSchedule)
                },
                onFailure = { error ->
                    android.util.Log.e("RoutineRepository", "Remote fetch failed", error)
                    
                    // Check if this might be a "no data" scenario (all routines deleted)
                    if (error.message?.contains("No documents found", ignoreCase = true) == true) {
                        android.util.Log.w("RoutineRepository", "No remote data found - clearing local data")
                        localDataSource.clearRoutinesForDepartment(department)
                        
                        // Return empty schedule instead of error
                        val emptySchedule = RoutineSchedule(
                            id = "",
                            semester = "Unknown",
                            department = department,
                            effectiveFrom = "",
                            schedule = emptyList(),
                            version = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        Result.success(emptySchedule)
                    } else {
                        // If remote fails for other reasons, try to return local data
                        val localSchedule = localDataSource.getLatestScheduleForDepartment(department)
                        if (localSchedule != null) {
                            android.util.Log.d("RoutineRepository", "Using local fallback data")
                            Result.success(localSchedule)
                        } else {
                            Result.failure(error)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            android.util.Log.e("RoutineRepository", "Exception in refreshFromRemote", e)
            Result.failure(e)
        }
    }

    override suspend fun clearLocalData(department: String): Result<Unit> {
        return try {
            localDataSource.clearRoutinesForDepartment(department)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSyncStatus(department: String): Result<Boolean> {
        return try {
            val localSchedule = localDataSource.getLatestScheduleForDepartment(department)
            Result.success(localSchedule != null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncRoutineDataInBackground(department: String) {
        try {
            // Run sync in background without blocking the UI
            syncRoutineData(department)
        } catch (e: Exception) {
            // Ignore errors in background sync
        }
    }

    private fun getLastKnownMetadataVersion(): Long {
        // This could be stored in SharedPreferences or local database
        // For now, return 0 - in a real implementation, you'd retrieve this from persistent storage
        return 0L
    }

    private fun storeLastKnownMetadataVersion(version: Long) {
        // This should store the version in SharedPreferences or local database
        // For now, this is a placeholder - in a real implementation, you'd persist this
    }
}
