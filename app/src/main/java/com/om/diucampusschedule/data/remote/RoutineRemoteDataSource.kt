package com.om.diucampusschedule.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import com.om.diucampusschedule.data.model.RoutineScheduleDto
import com.om.diucampusschedule.data.model.toDomainModel
import com.om.diucampusschedule.data.model.toDto
import com.om.diucampusschedule.domain.model.RoutineSchedule
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val ROUTINES_COLLECTION = "routines"
    }

    suspend fun getLatestRoutineForDepartment(department: String): Result<RoutineSchedule> {
        return try {
            android.util.Log.d("RoutineDataSource", "=== STARTING FIREBASE FETCH ===")
            android.util.Log.d("RoutineDataSource", "Fetching routines for department: '$department'")
            android.util.Log.d("RoutineDataSource", "Firebase instance: ${firestore.app.name}")
            
            // Test Firebase connection first
            android.util.Log.d("RoutineDataSource", "Testing Firebase connection...")
            
            // Get all routine documents without ordering to avoid index requirement
            val querySnapshot = firestore.collection(ROUTINES_COLLECTION)
                .get()
                .await()

            android.util.Log.d("RoutineDataSource", "✅ Firebase connected successfully!")
            android.util.Log.d("RoutineDataSource", "Found ${querySnapshot.documents.size} documents in '$ROUTINES_COLLECTION' collection")
            
            if (querySnapshot.documents.isEmpty()) {
                android.util.Log.e("RoutineDataSource", "❌ No documents found in Firebase collection '$ROUTINES_COLLECTION'")
                android.util.Log.e("RoutineDataSource", "Please check if routine data has been uploaded via admin dashboard")
            }

            if (querySnapshot.documents.isNotEmpty()) {
                // Try each document until we find one that matches our department
                for (document in querySnapshot.documents) {
                    android.util.Log.d("RoutineDataSource", "Checking document ID: ${document.id}")
                    
                    val documentData = document.data
                    if (documentData == null) {
                        android.util.Log.d("RoutineDataSource", "Document ${document.id} has no data")
                        continue
                    }
                    
                    android.util.Log.d("RoutineDataSource", "Document keys: ${documentData.keys}")
                    
                    // Check multiple possible data structures
                    var routineDepartment: String? = null
                    var routineSchedule: List<Map<String, Any>>? = null
                    var semester: String? = null
                    var effectiveFrom: String? = null
                    
                    // Format 1: Direct fields in document
                    routineDepartment = documentData["department"] as? String
                    routineSchedule = documentData["schedule"] as? List<Map<String, Any>>
                    semester = documentData["semester"] as? String
                    effectiveFrom = documentData["effectiveFrom"] as? String
                    
                    // Format 2: Nested in 'data' field (from admin dashboard)
                    if (routineDepartment == null && documentData.containsKey("data")) {
                        val nestedData = documentData["data"] as? Map<String, Any>
                        routineDepartment = nestedData?.get("department") as? String
                        routineSchedule = nestedData?.get("schedule") as? List<Map<String, Any>>
                        semester = nestedData?.get("semester") as? String
                        effectiveFrom = nestedData?.get("effectiveFrom") as? String
                    }
                    
                    android.util.Log.d("RoutineDataSource", "Document ${document.id}: dept=$routineDepartment, scheduleSize=${routineSchedule?.size}")
                    
                    android.util.Log.d("RoutineDataSource", "Found document with department: '$routineDepartment', looking for: '$department'")
                    
                    // Check if this routine matches our department (be more lenient with matching)
                    val departmentMatches = routineDepartment != null && routineSchedule != null && (
                        routineDepartment.equals(department, ignoreCase = true) || 
                        routineDepartment.contains("Software", ignoreCase = true) || 
                        department.contains("Software", ignoreCase = true) ||
                        routineDepartment.contains("Engineering", ignoreCase = true) ||
                        department.contains("Engineering", ignoreCase = true)
                    )
                    
                    android.util.Log.d("RoutineDataSource", "Department match result: $departmentMatches")
                    
                    if (departmentMatches) {
                        android.util.Log.d("RoutineDataSource", "Found matching routine for $department with ${routineSchedule?.size} items")
                        
                        // Convert to our domain model
                        val routineItems = routineSchedule?.mapNotNull { scheduleItem ->
                            try {
                                android.util.Log.d("RoutineDataSource", "Converting schedule item: ${scheduleItem.keys}")
                                com.om.diucampusschedule.domain.model.RoutineItem(
                                    id = "${document.id}_${scheduleItem.hashCode()}",
                                    day = scheduleItem["day"] as? String ?: "",
                                    time = scheduleItem["time"] as? String ?: "",
                                    room = scheduleItem["room"] as? String ?: "",
                                    courseCode = scheduleItem["courseCode"] as? String ?: "",
                                    teacherInitial = scheduleItem["teacherInitial"] as? String ?: "",
                                    batch = scheduleItem["batch"] as? String ?: "",
                                    section = scheduleItem["section"] as? String ?: "",
                                    labSection = scheduleItem["labSection"] as? String,
                                    semester = semester ?: "",
                                    department = routineDepartment ?: "Software Engineering",
                                    effectiveFrom = effectiveFrom ?: ""
                                )
                            } catch (e: Exception) {
                                android.util.Log.e("RoutineDataSource", "Error converting schedule item", e)
                                null
                            }
                        }
                        
                        android.util.Log.d("RoutineDataSource", "Successfully converted ${routineItems?.size} routine items")
                        
                        val routine = com.om.diucampusschedule.domain.model.RoutineSchedule(
                            id = document.id,
                            semester = semester ?: "Unknown",
                            department = routineDepartment ?: "Software Engineering",
                            effectiveFrom = effectiveFrom ?: "",
                            schedule = routineItems ?: emptyList(),
                            version = documentData["version"] as? Long ?: System.currentTimeMillis(),
                            updatedAt = (documentData["uploadedAt"] as? Timestamp)?.seconds?.times(1000) ?: System.currentTimeMillis()
                        )
                        
                        return Result.success(routine)
                    }
                }
                
                // If no exact match found, try to return the first available routine as fallback
                android.util.Log.w("RoutineDataSource", "No exact match for department: $department, trying fallback...")
                
                for (document in querySnapshot.documents) {
                    val documentData = document.data
                    if (documentData != null) {
                        var routineSchedule: List<Map<String, Any>>? = null
                        var semester: String? = null
                        var effectiveFrom: String? = null
                        var routineDepartment: String? = null
                        
                        // Try both data formats
                        routineSchedule = documentData["schedule"] as? List<Map<String, Any>>
                        semester = documentData["semester"] as? String
                        effectiveFrom = documentData["effectiveFrom"] as? String
                        routineDepartment = documentData["department"] as? String
                        
                        if (routineSchedule == null && documentData.containsKey("data")) {
                            val nestedData = documentData["data"] as? Map<String, Any>
                            routineSchedule = nestedData?.get("schedule") as? List<Map<String, Any>>
                            semester = nestedData?.get("semester") as? String
                            effectiveFrom = nestedData?.get("effectiveFrom") as? String
                            routineDepartment = nestedData?.get("department") as? String
                        }
                        
                        if (routineSchedule != null && routineSchedule.isNotEmpty()) {
                            android.util.Log.w("RoutineDataSource", "Using fallback routine from department: $routineDepartment with ${routineSchedule.size} items")
                            
                            val routineItems = routineSchedule.mapNotNull { scheduleItem ->
                                try {
                                    com.om.diucampusschedule.domain.model.RoutineItem(
                                        id = "${document.id}_${scheduleItem.hashCode()}",
                                        day = scheduleItem["day"] as? String ?: "",
                                        time = scheduleItem["time"] as? String ?: "",
                                        room = scheduleItem["room"] as? String ?: "",
                                        courseCode = scheduleItem["courseCode"] as? String ?: "",
                                        teacherInitial = scheduleItem["teacherInitial"] as? String ?: "",
                                        batch = scheduleItem["batch"] as? String ?: "",
                                        section = scheduleItem["section"] as? String ?: "",
                                        labSection = scheduleItem["labSection"] as? String,
                                        semester = semester ?: "",
                                        department = routineDepartment ?: "Software Engineering",
                                        effectiveFrom = effectiveFrom ?: ""
                                    )
                                } catch (e: Exception) {
                                    android.util.Log.e("RoutineDataSource", "Error converting fallback schedule item", e)
                                    null
                                }
                            }
                            
                            val routine = com.om.diucampusschedule.domain.model.RoutineSchedule(
                                id = document.id,
                                semester = semester ?: "Unknown",
                                department = routineDepartment ?: "Software Engineering",
                                effectiveFrom = effectiveFrom ?: "",
                                schedule = routineItems,
                                version = documentData["version"] as? Long ?: System.currentTimeMillis(),
                                updatedAt = (documentData["uploadedAt"] as? Timestamp)?.seconds?.times(1000) ?: System.currentTimeMillis()
                            )
                            
                            return Result.success(routine)
                        }
                    }
                }
                
                android.util.Log.e("RoutineDataSource", "No routine found for department: $department (tried fallback)")
                Result.failure(Exception("No routine found for department: $department"))
            } else {
                android.util.Log.e("RoutineDataSource", "No documents found in routines collection")
                Result.failure(Exception("No routine documents found"))
            }
        } catch (e: Exception) {
            android.util.Log.e("RoutineDataSource", "Error fetching routine", e)
            Result.failure(e)
        }
    }

    suspend fun getRoutineById(routineId: String): Result<RoutineSchedule> {
        return try {
            val document = firestore.collection(ROUTINES_COLLECTION)
                .document(routineId)
                .get()
                .await()

            if (document.exists()) {
                val routineDto = document.toObject(RoutineScheduleDto::class.java)
                if (routineDto != null) {
                    val routine = routineDto.copy(id = document.id).toDomainModel()
                    Result.success(routine)
                } else {
                    Result.failure(Exception("Failed to parse routine data"))
                }
            } else {
                Result.failure(Exception("Routine not found with ID: $routineId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkForUpdates(department: String, currentVersion: Long): Result<Boolean> {
        return try {
            val querySnapshot = firestore.collection(ROUTINES_COLLECTION)
                .whereEqualTo("department", department)
                .whereGreaterThan("version", currentVersion)
                .limit(1)
                .get()
                .await()

            Result.success(querySnapshot.documents.isNotEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeRoutineUpdates(department: String): Flow<RoutineSchedule?> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null

        try {
            listenerRegistration = firestore.collection(ROUTINES_COLLECTION)
                .whereEqualTo("department", department)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val document = snapshot.documents.first()
                        val routineDto = document.toObject(RoutineScheduleDto::class.java)
                        if (routineDto != null) {
                            val routine = routineDto.copy(id = document.id).toDomainModel()
                            trySend(routine)
                        } else {
                            trySend(null)
                        }
                    } else {
                        trySend(null)
                    }
                }
        } catch (e: Exception) {
            close(e)
        }

        awaitClose {
            listenerRegistration?.remove()
        }
    }

    suspend fun uploadRoutine(routine: RoutineSchedule): Result<String> {
        return try {
            val routineDto = routine.toDto()
            val documentRef = if (routine.id.isNotEmpty()) {
                firestore.collection(ROUTINES_COLLECTION).document(routine.id)
            } else {
                firestore.collection(ROUTINES_COLLECTION).document()
            }

            documentRef.set(routineDto).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRoutine(routineId: String): Result<Unit> {
        return try {
            firestore.collection(ROUTINES_COLLECTION)
                .document(routineId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllRoutinesForDepartment(department: String): Result<List<RoutineSchedule>> {
        return try {
            val querySnapshot = firestore.collection(ROUTINES_COLLECTION)
                .get()
                .await()

            val routines = querySnapshot.documents.mapNotNull { document ->
                val documentData = document.data
                
                // Check if this is the new format from admin dashboard
                if (documentData?.containsKey("data") == true) {
                    val nestedData = documentData["data"] as? Map<String, Any>
                    val routineDepartment = nestedData?.get("department") as? String
                    val routineSchedule = nestedData?.get("schedule") as? List<Map<String, Any>>
                    val semester = nestedData?.get("semester") as? String
                    val effectiveFrom = nestedData?.get("effectiveFrom") as? String
                    
                    // Check if this routine is for the requested department
                    if (routineDepartment == department && routineSchedule != null) {
                        try {
                            // Convert the admin dashboard format to our domain model
                            val routineItems = routineSchedule.mapNotNull { scheduleItem ->
                                try {
                                    com.om.diucampusschedule.domain.model.RoutineItem(
                                        id = "${document.id}_${scheduleItem.hashCode()}",
                                        day = scheduleItem["day"] as? String ?: "",
                                        time = scheduleItem["time"] as? String ?: "",
                                        room = scheduleItem["room"] as? String ?: "",
                                        courseCode = scheduleItem["courseCode"] as? String ?: "",
                                        teacherInitial = scheduleItem["teacherInitial"] as? String ?: "",
                                        batch = scheduleItem["batch"] as? String ?: "",
                                        section = scheduleItem["section"] as? String ?: "",
                                        labSection = scheduleItem["labSection"] as? String,
                                        semester = semester ?: "",
                                        department = routineDepartment,
                                        effectiveFrom = effectiveFrom ?: ""
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            
                            com.om.diucampusschedule.domain.model.RoutineSchedule(
                                id = document.id,
                                semester = semester ?: "Unknown",
                                department = routineDepartment,
                                effectiveFrom = effectiveFrom ?: "",
                                schedule = routineItems,
                                version = documentData["version"] as? Long ?: System.currentTimeMillis(),
                                updatedAt = (documentData["uploadedAt"] as? Timestamp)?.seconds?.times(1000) ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } else {
                        null
                    }
                } else {
                    // Fallback to old format
                    val routineDto = document.toObject(RoutineScheduleDto::class.java)
                    if (routineDto?.department == department) {
                        routineDto.copy(id = document.id).toDomainModel()
                    } else {
                        null
                    }
                }
            }

            Result.success(routines)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
