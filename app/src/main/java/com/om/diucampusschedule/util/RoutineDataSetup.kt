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
 * Utility class to upload routine data from assets/routines.json to Firebase Firestore
 * This should be used only once during initial setup or when updating the routine data
 */
object RoutineDataSetup {
    
    private const val TAG = "RoutineDataSetup"
    private const val ROUTINES_COLLECTION = "routines"
    
    /**
     * Upload routine data from assets/routines.json to Firebase Firestore
     * Call this function from your app's debug menu or during development
     */
    suspend fun uploadRoutineToFirebase(
        context: Context,
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ): Result<String> {
        return try {
            Log.d(TAG, "Starting routine data upload to Firebase...")
            
            // Read JSON from assets
            val inputStream = context.assets.open("routines.json")
            val reader = InputStreamReader(inputStream)
            val gson = Gson()
            
            // Parse JSON
            val jsonObject = gson.fromJson(reader, JsonObject::class.java)
            
            val semester = jsonObject.get("semester").asString
            val department = jsonObject.get("department").asString
            val effectiveFrom = jsonObject.get("effectiveFrom").asString
            
            Log.d(TAG, "Parsed routine info: $semester, $department, $effectiveFrom")
            
            // Parse schedule array
            val scheduleJsonArray = jsonObject.getAsJsonArray("schedule")
            val routineItemsType = object : TypeToken<List<RoutineItemDto>>() {}.type
            val scheduleItems: List<RoutineItemDto> = gson.fromJson(scheduleJsonArray, routineItemsType)
            
            Log.d(TAG, "Parsed ${scheduleItems.size} routine items")
            
            // Create routine schedule DTO
            val routineScheduleDto = RoutineScheduleDto(
                id = "", // Firestore will generate this
                semester = semester,
                department = department,
                effectiveFrom = effectiveFrom,
                schedule = scheduleItems,
                version = 1,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            // Check if routine already exists for this department
            val existingQuery = firestore.collection(ROUTINES_COLLECTION)
                .whereEqualTo("department", department)
                .whereEqualTo("semester", semester)
                .get()
                .await()
            
            val documentRef = if (existingQuery.documents.isNotEmpty()) {
                // Update existing routine
                val existingDoc = existingQuery.documents.first()
                Log.d(TAG, "Updating existing routine with ID: ${existingDoc.id}")
                firestore.collection(ROUTINES_COLLECTION).document(existingDoc.id)
            } else {
                // Create new routine
                Log.d(TAG, "Creating new routine document")
                firestore.collection(ROUTINES_COLLECTION).document()
            }
            
            // Upload to Firestore
            documentRef.set(routineScheduleDto).await()
            
            val message = "Routine uploaded successfully with ID: ${documentRef.id}"
            Log.d(TAG, message)
            
            reader.close()
            inputStream.close()
            
            Result.success(message)
            
        } catch (e: Exception) {
            val errorMessage = "Failed to upload routine: ${e.message}"
            Log.e(TAG, errorMessage, e)
            Result.failure(e)
        }
    }
    
    /**
     * Update the version of existing routine to trigger sync notifications
     */
    suspend fun updateRoutineVersion(
        department: String,
        newVersion: Long = System.currentTimeMillis(),
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ): Result<String> {
        return try {
            Log.d(TAG, "Updating routine version for department: $department")
            
            // Get existing routine
            val querySnapshot = firestore.collection(ROUTINES_COLLECTION)
                .whereEqualTo("department", department)
                .limit(1)
                .get()
                .await()
            
            if (querySnapshot.documents.isNotEmpty()) {
                val document = querySnapshot.documents.first()
                val updates = mapOf(
                    "version" to newVersion,
                    "updatedAt" to System.currentTimeMillis()
                )
                
                document.reference.update(updates).await()
                
                val message = "Routine version updated to $newVersion for department: $department"
                Log.d(TAG, message)
                Result.success(message)
            } else {
                val errorMessage = "No routine found for department: $department"
                Log.e(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            val errorMessage = "Failed to update routine version: ${e.message}"
            Log.e(TAG, errorMessage, e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete all routines for a specific department
     */
    suspend fun deleteRoutineForDepartment(
        department: String,
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ): Result<String> {
        return try {
            Log.d(TAG, "Deleting routines for department: $department")
            
            val querySnapshot = firestore.collection(ROUTINES_COLLECTION)
                .whereEqualTo("department", department)
                .get()
                .await()
            
            var deletedCount = 0
            querySnapshot.documents.forEach { document ->
                document.reference.delete().await()
                deletedCount++
            }
            
            val message = "Deleted $deletedCount routine(s) for department: $department"
            Log.d(TAG, message)
            Result.success(message)
            
        } catch (e: Exception) {
            val errorMessage = "Failed to delete routines: ${e.message}"
            Log.e(TAG, errorMessage, e)
            Result.failure(e)
        }
    }
    
    /**
     * List all routines in Firestore (for debugging)
     */
    suspend fun listAllRoutines(
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ): Result<List<String>> {
        return try {
            Log.d(TAG, "Listing all routines...")
            
            val querySnapshot = firestore.collection(ROUTINES_COLLECTION)
                .get()
                .await()
            
            val routines = querySnapshot.documents.map { document ->
                val data = document.data
                "${document.id}: ${data?.get("department")} - ${data?.get("semester")} (v${data?.get("version")})"
            }
            
            Log.d(TAG, "Found ${routines.size} routines")
            routines.forEach { routine ->
                Log.d(TAG, routine)
            }
            
            Result.success(routines)
            
        } catch (e: Exception) {
            val errorMessage = "Failed to list routines: ${e.message}"
            Log.e(TAG, errorMessage, e)
            Result.failure(e)
        }
    }
}
