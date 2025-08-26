package com.om.diucampusschedule.data.sync

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.om.diucampusschedule.data.model.RoutineItemDto
import com.om.diucampusschedule.data.model.RoutineScheduleDto
import com.om.diucampusschedule.data.model.toDomainModel
import com.om.diucampusschedule.data.remote.RoutineRemoteDataSource
import com.om.diucampusschedule.domain.model.RoutineSchedule
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineDataUploader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val routineRemoteDataSource: RoutineRemoteDataSource
) {

    suspend fun uploadRoutineFromAssets(): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Read JSON from assets
                val inputStream = context.assets.open("routines.json")
                val reader = InputStreamReader(inputStream)
                val gson = Gson()
                
                // Parse JSON
                val jsonObject = gson.fromJson(reader, JsonObject::class.java)
                
                val semester = jsonObject.get("semester").asString
                val department = jsonObject.get("department").asString
                val effectiveFrom = jsonObject.get("effectiveFrom").asString
                
                // Parse schedule array
                val scheduleJsonArray = jsonObject.getAsJsonArray("schedule")
                val routineItemsType = object : TypeToken<List<RoutineItemDto>>() {}.type
                val scheduleItems: List<RoutineItemDto> = gson.fromJson(scheduleJsonArray, routineItemsType)
                
                // Create routine schedule DTO
                val routineScheduleDto = RoutineScheduleDto(
                    id = "", // Firebase will generate this
                    semester = semester,
                    department = department,
                    effectiveFrom = effectiveFrom,
                    schedule = scheduleItems,
                    version = 1,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                // Convert to domain model and upload
                val routineSchedule = routineScheduleDto.toDomainModel()
                
                routineRemoteDataSource.uploadRoutine(routineSchedule).fold(
                    onSuccess = { documentId ->
                        Result.success("Routine uploaded successfully with ID: $documentId")
                    },
                    onFailure = { error ->
                        Result.failure(error)
                    }
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun updateRoutineVersion(department: String, newVersion: Long): Result<String> {
        return try {
            // Get existing routine
            routineRemoteDataSource.getLatestRoutineForDepartment(department).fold(
                onSuccess = { existingRoutine ->
                    // Update version and timestamp
                    val updatedRoutine = existingRoutine.copy(
                        version = newVersion,
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    // Upload updated routine
                    routineRemoteDataSource.uploadRoutine(updatedRoutine).fold(
                        onSuccess = { documentId ->
                            Result.success("Routine version updated to $newVersion")
                        },
                        onFailure = { error ->
                            Result.failure(error)
                        }
                    )
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
