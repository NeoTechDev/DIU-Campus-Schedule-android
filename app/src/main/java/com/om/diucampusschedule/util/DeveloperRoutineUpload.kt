package com.om.diucampusschedule.util

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.om.diucampusschedule.data.model.RoutineItemDto
import com.om.diucampusschedule.data.model.RoutineScheduleDto
import kotlinx.coroutines.tasks.await
import java.io.InputStreamReader

/**
 * Developer utility for uploading routine data to Firebase
 * This bypasses authentication and directly uploads data for development purposes
 */
object DeveloperRoutineUpload {
    
    private const val TAG = "DeveloperUpload"
    private const val ROUTINES_COLLECTION = "routines"
    
    /**
     * Upload routine data directly to Firebase (Developer only)
     * This function bypasses all authentication checks
     */
    suspend fun uploadRoutineDataToFirebase(
        context: Context,
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ): Result<String> {
        return try {
            Log.d(TAG, "🔧 Developer: Starting routine data upload to Firebase...")
            
            // Read JSON from assets
            val inputStream = context.assets.open("routines.json")
            val reader = InputStreamReader(inputStream)
            val gson = Gson()
            
            // Parse JSON
            val jsonObject = gson.fromJson(reader, JsonObject::class.java)
            
            val semester = jsonObject.get("semester").asString
            val department = jsonObject.get("department").asString
            val effectiveFrom = jsonObject.get("effectiveFrom").asString
            
            Log.d(TAG, "📋 Parsed routine: $semester, $department, $effectiveFrom")
            
            // Parse schedule array
            val scheduleJsonArray = jsonObject.getAsJsonArray("schedule")
            val routineItemsType = object : TypeToken<List<RoutineItemDto>>() {}.type
            val scheduleItems: List<RoutineItemDto> = gson.fromJson(scheduleJsonArray, routineItemsType)
            
            Log.d(TAG, "📚 Parsed ${scheduleItems.size} routine items")
            
            // Create routine schedule DTO with current timestamp as version
            val currentTime = System.currentTimeMillis()
            val routineScheduleDto = RoutineScheduleDto(
                id = "", // Firestore will generate this
                semester = semester,
                department = department,
                effectiveFrom = effectiveFrom,
                schedule = scheduleItems,
                version = currentTime, // Use timestamp as version for uniqueness
                createdAt = currentTime,
                updatedAt = currentTime
            )
            
            // Check if routine already exists for this department and semester
            val existingQuery = firestore.collection(ROUTINES_COLLECTION)
                .whereEqualTo("department", department)
                .whereEqualTo("semester", semester)
                .get()
                .await()
            
            val documentRef = if (existingQuery.documents.isNotEmpty()) {
                // Update existing routine
                val existingDoc = existingQuery.documents.first()
                Log.d(TAG, "🔄 Updating existing routine with ID: ${existingDoc.id}")
                firestore.collection(ROUTINES_COLLECTION).document(existingDoc.id)
            } else {
                // Create new routine
                Log.d(TAG, "✨ Creating new routine document")
                firestore.collection(ROUTINES_COLLECTION).document()
            }
            
            // Upload to Firestore (this works without authentication in development)
            documentRef.set(routineScheduleDto).await()
            
            val message = "✅ Routine uploaded successfully!\n" +
                    "📄 Document ID: ${documentRef.id}\n" +
                    "🏫 Department: $department\n" +
                    "📅 Semester: $semester\n" +
                    "📚 Items: ${scheduleItems.size}\n" +
                    "🔢 Version: $currentTime"
            
            Log.d(TAG, message)
            
            reader.close()
            inputStream.close()
            
            Result.success(message)
            
        } catch (e: Exception) {
            val errorMessage = "❌ Failed to upload routine: ${e.message}"
            Log.e(TAG, errorMessage, e)
            Result.failure(e)
        }
    }
    
    /**
     * Update routine version to trigger user notifications
     */
    suspend fun updateRoutineVersion(
        department: String,
        semester: String = "Summer 2025",
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ): Result<String> {
        return try {
            Log.d(TAG, "🔄 Developer: Updating routine version for $department - $semester")
            
            // Get existing routine
            val querySnapshot = firestore.collection(ROUTINES_COLLECTION)
                .whereEqualTo("department", department)
                .whereEqualTo("semester", semester)
                .limit(1)
                .get()
                .await()
            
            if (querySnapshot.documents.isNotEmpty()) {
                val document = querySnapshot.documents.first()
                val newVersion = System.currentTimeMillis()
                
                val updates = mapOf(
                    "version" to newVersion,
                    "updatedAt" to newVersion
                )
                
                document.reference.update(updates).await()
                
                val message = "✅ Version updated successfully!\n" +
                        "🏫 Department: $department\n" +
                        "🔢 New Version: $newVersion\n" +
                        "📱 Users will receive notifications"
                
                Log.d(TAG, message)
                Result.success(message)
            } else {
                val errorMessage = "❌ No routine found for: $department - $semester"
                Log.e(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            val errorMessage = "❌ Failed to update version: ${e.message}"
            Log.e(TAG, errorMessage, e)
            Result.failure(e)
        }
    }
    
    /**
     * List all routines in Firebase (for verification)
     */
    suspend fun listAllRoutines(
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ): Result<List<String>> {
        return try {
            Log.d(TAG, "📋 Developer: Listing all routines...")
            
            val querySnapshot = firestore.collection(ROUTINES_COLLECTION)
                .get()
                .await()
            
            val routines = querySnapshot.documents.mapIndexed { index, document ->
                val data = document.data
                val department = data?.get("department") ?: "Unknown"
                val semester = data?.get("semester") ?: "Unknown"
                val version = data?.get("version") ?: "Unknown"
                val itemCount = (data?.get("schedule") as? List<*>)?.size ?: 0
                
                "${index + 1}. 📄 ID: ${document.id}\n" +
                "   🏫 $department - $semester\n" +
                "   🔢 Version: $version\n" +
                "   📚 Items: $itemCount"
            }
            
            val summary = if (routines.isEmpty()) {
                "📭 No routines found in Firebase"
            } else {
                "📋 Found ${routines.size} routine(s) in Firebase:\n\n${routines.joinToString("\n\n")}"
            }
            
            Log.d(TAG, summary)
            Result.success(listOf(summary))
            
        } catch (e: Exception) {
            val errorMessage = "❌ Failed to list routines: ${e.message}"
            Log.e(TAG, errorMessage, e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete all routines (for cleanup during development)
     */
    suspend fun deleteAllRoutines(
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ): Result<String> {
        return try {
            Log.d(TAG, "🗑️ Developer: Deleting all routines...")
            
            val querySnapshot = firestore.collection(ROUTINES_COLLECTION)
                .get()
                .await()
            
            var deletedCount = 0
            querySnapshot.documents.forEach { document ->
                document.reference.delete().await()
                deletedCount++
                Log.d(TAG, "🗑️ Deleted: ${document.id}")
            }
            
            val message = "✅ Deleted $deletedCount routine(s) from Firebase"
            Log.d(TAG, message)
            Result.success(message)
            
        } catch (e: Exception) {
            val errorMessage = "❌ Failed to delete routines: ${e.message}"
            Log.e(TAG, errorMessage, e)
            Result.failure(e)
        }
    }
    
    /**
     * Quick setup: Upload routine and set version for immediate use
     */
    suspend fun quickSetup(
        context: Context,
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ): Result<String> {
        return try {
            Log.d(TAG, "🚀 Developer: Quick setup starting...")
            
            // First upload the routine
            val uploadResult = uploadRoutineDataToFirebase(context, firestore)
            
            uploadResult.fold(
                onSuccess = { uploadMessage ->
                    Log.d(TAG, "✅ Upload successful, now updating version...")
                    
                    // Then update version to trigger notifications
                    updateRoutineVersion("Software Engineering", "Summer 2025", firestore).fold(
                        onSuccess = { versionMessage ->
                            val combinedMessage = "🎉 Quick Setup Complete!\n\n" +
                                    "📤 Upload: $uploadMessage\n\n" +
                                    "🔄 Version: $versionMessage"
                            Result.success(combinedMessage)
                        },
                        onFailure = { versionError ->
                            Result.success("✅ Upload successful but version update failed: ${versionError.message}")
                        }
                    )
                },
                onFailure = { uploadError ->
                    Result.failure(uploadError)
                }
            )
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
