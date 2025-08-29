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
            // Get current local version
            val localSchedule = localDataSource.getLatestScheduleForDepartment(department)
            val currentVersion = localSchedule?.version ?: 0L
            
            // Check if there are updates
            remoteDataSource.checkForUpdates(department, currentVersion).fold(
                onSuccess = { hasUpdates ->
                    if (hasUpdates || localSchedule == null) {
                        // Fetch latest from remote
                        remoteDataSource.getLatestRoutineForDepartment(department).fold(
                            onSuccess = { remoteSchedule ->
                                // Save to local storage
                                localDataSource.saveSchedule(remoteSchedule)
                                Result.success(Unit)
                            },
                            onFailure = { error ->
                                Result.failure(error)
                            }
                        )
                    } else {
                        // No updates needed
                        Result.success(Unit)
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkForUpdates(department: String): Result<Boolean> {
        return try {
            val localSchedule = localDataSource.getLatestScheduleForDepartment(department)
            val currentVersion = localSchedule?.version ?: 0L
            
            remoteDataSource.checkForUpdates(department, currentVersion)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshFromRemote(department: String): Result<RoutineSchedule> {
        return try {
            remoteDataSource.getLatestRoutineForDepartment(department).fold(
                onSuccess = { remoteSchedule ->
                    // Save to local storage
                    localDataSource.saveSchedule(remoteSchedule)
                    Result.success(remoteSchedule)
                },
                onFailure = { error ->
                    // If remote fails, try to return local data
                    val localSchedule = localDataSource.getLatestScheduleForDepartment(department)
                    if (localSchedule != null) {
                        Result.success(localSchedule)
                    } else {
                        Result.failure(error)
                    }
                }
            )
        } catch (e: Exception) {
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
}
