package com.om.diucampusschedule.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.om.diucampusschedule.data.model.ExamRoutineDto
import com.om.diucampusschedule.data.model.ExamDayDto
import com.om.diucampusschedule.data.model.ExamCourseDto
import com.om.diucampusschedule.data.model.toDomainModel
import com.om.diucampusschedule.data.model.toDto
import com.om.diucampusschedule.domain.model.ExamRoutine
import com.om.diucampusschedule.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamRoutineRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "ExamRoutineDataSource"
        private const val EXAM_ROUTINES_COLLECTION = "exam_routines"
    }

    suspend fun getExamRoutineForDepartment(department: String): Result<ExamRoutine?> {
        return try {
            Log.d(TAG, "Fetching exam routine for department: $department")
            
            // Get all exam routine documents (following class routine pattern)
            val querySnapshot = firestore.collection(EXAM_ROUTINES_COLLECTION)
                .get()
                .await()

            Log.d(TAG, "Found ${querySnapshot.documents.size} exam routine documents")
            
            if (querySnapshot.documents.isNotEmpty()) {
                // Try each document until we find one that matches our department
                for (document in querySnapshot.documents) {
                    Log.d(TAG, "Checking exam routine document ID: ${document.id}")
                    
                    val documentData = document.data
                    if (documentData == null) {
                        Log.d(TAG, "Document ${document.id} has no data")
                        continue
                    }
                    
                    Log.d(TAG, "Document keys: ${documentData.keys}")
                    
                    // Check multiple possible data structures (following class routine pattern)
                    var routineDepartment: String? = null
                    
                    // Format 1: Direct fields in document
                    routineDepartment = documentData["department"] as? String
                    
                    // Format 2: Nested in 'data' field (if uploaded via admin dashboard)
                    if (routineDepartment == null && documentData.containsKey("data")) {
                        val nestedData = documentData["data"] as? Map<String, Any>
                        routineDepartment = nestedData?.get("department") as? String
                    }
                    
                    Log.d(TAG, "Document ${document.id}: dept=$routineDepartment, looking for: $department")
                    
                    // Check if this routine matches our department (flexible matching like class routines)
                    val departmentMatches = routineDepartment != null && (
                        routineDepartment.equals(department, ignoreCase = true) || 
                        routineDepartment.contains("Software", ignoreCase = true) || 
                        department.contains("Software", ignoreCase = true) ||
                        routineDepartment.contains("Engineering", ignoreCase = true) ||
                        department.contains("Engineering", ignoreCase = true)
                    )
                    
                    Log.d(TAG, "Department match result: $departmentMatches")
                    
                    if (departmentMatches) {
                        // Handle both direct and nested data structures using manual extraction
                        val sourceData = if (documentData.containsKey("data")) {
                            // Data is nested under "data" field
                            documentData["data"] as? Map<String, Any>
                        } else {
                            // Data is at root level
                            documentData
                        }
                        
                        val examRoutineDto = sourceData?.let { data ->
                            Log.d(TAG, "Creating DTO from data. exam_type: ${data["exam_type"]}, start_date: ${data["start_date"]}")
                            ExamRoutineDto(
                                id = document.id,
                                university = data["university"] as? String ?: "",
                                department = data["department"] as? String ?: "",
                                examType = data["exam_type"] as? String ?: "",
                                semester = data["semester"] as? String ?: "",
                                startDate = data["start_date"] as? String ?: "",
                                endDate = data["end_date"] as? String ?: "",
                                slots = data["slots"] as? Map<String, String> ?: emptyMap(),
                                schedule = (data["schedule"] as? List<*>)?.mapNotNull { scheduleItem ->
                                    (scheduleItem as? Map<String, Any>)?.let { dayData ->
                                        ExamDayDto(
                                            dayNumber = (dayData["day_number"] as? Number)?.toInt() ?: 0,
                                            date = dayData["date"] as? String ?: "",
                                            weekday = dayData["weekday"] as? String ?: "",
                                            courses = (dayData["courses"] as? List<*>)?.mapNotNull { courseItem ->
                                                (courseItem as? Map<String, Any>)?.let { courseData ->
                                                    ExamCourseDto(
                                                        code = courseData["code"] as? String ?: "",
                                                        name = courseData["name"] as? String ?: "",
                                                        students = (courseData["students"] as? Number)?.toInt() ?: 0,
                                                        batch = courseData["batch"] as? String ?: "",
                                                        slot = courseData["slot"] as? String ?: ""
                                                    )
                                                }
                                            } ?: emptyList()
                                        )
                                    }
                                } ?: emptyList(),
                                version = (data["version"] as? Number)?.toLong() ?: 1L,
                                createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
                                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: 0L
                            )
                        }
                        
                        if (examRoutineDto != null) {
                            Log.d(TAG, "Created examRoutineDto - examType: ${examRoutineDto.examType}, startDate: ${examRoutineDto.startDate}")
                            val examRoutine = examRoutineDto.toDomainModel()
                            Log.d(TAG, "Converted to domain model - examType: ${examRoutine.examType}, startDate: ${examRoutine.startDate}")
                            Log.d(TAG, "Found matching exam routine: ${examRoutine.examType} for ${examRoutine.department}")
                            return Result.success(examRoutine)
                        }
                    }
                }
            }
            
            Log.d(TAG, "No matching exam routine found for department: $department")
            Result.success(null)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching exam routine for department: $department", e)
            Result.failure(e)
        }
    }

    fun observeExamRoutineForDepartment(department: String): Flow<ExamRoutine?> = callbackFlow {
        Log.d(TAG, "Starting to observe exam routine for department: $department")

        val listenerRegistration = try {
            // Listen to all exam routine documents (following class routine pattern)
            firestore.collection(EXAM_ROUTINES_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing exam routine", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        // Find matching document using same logic as getExamRoutineForDepartment
                        for (document in snapshot.documents) {
                            val documentData = document.data
                            if (documentData == null) continue
                            
                            var routineDepartment: String? = documentData["department"] as? String
                            
                            if (routineDepartment == null && documentData.containsKey("data")) {
                                val nestedData = documentData["data"] as? Map<String, Any>
                                routineDepartment = nestedData?.get("department") as? String
                            }
                            
                            val departmentMatches = routineDepartment != null && (
                                routineDepartment.equals(department, ignoreCase = true) || 
                                routineDepartment.contains("Software", ignoreCase = true) || 
                                department.contains("Software", ignoreCase = true) ||
                                routineDepartment.contains("Engineering", ignoreCase = true) ||
                                department.contains("Engineering", ignoreCase = true)
                            )
                            
                            if (departmentMatches) {
                                // Handle both direct and nested data structures using manual extraction
                                val sourceData = if (documentData.containsKey("data")) {
                                    // Data is nested under "data" field
                                    documentData["data"] as? Map<String, Any>
                                } else {
                                    // Data is at root level
                                    documentData
                                }
                                
                                val examRoutineDto = sourceData?.let { data ->
                                    ExamRoutineDto(
                                        id = document.id,
                                        university = data["university"] as? String ?: "",
                                        department = data["department"] as? String ?: "",
                                        examType = data["exam_type"] as? String ?: "",
                                        semester = data["semester"] as? String ?: "",
                                        startDate = data["start_date"] as? String ?: "",
                                        endDate = data["end_date"] as? String ?: "",
                                        slots = data["slots"] as? Map<String, String> ?: emptyMap(),
                                        schedule = (data["schedule"] as? List<*>)?.mapNotNull { scheduleItem ->
                                            (scheduleItem as? Map<String, Any>)?.let { dayData ->
                                                ExamDayDto(
                                                    dayNumber = (dayData["day_number"] as? Number)?.toInt() ?: 0,
                                                    date = dayData["date"] as? String ?: "",
                                                    weekday = dayData["weekday"] as? String ?: "",
                                                    courses = (dayData["courses"] as? List<*>)?.mapNotNull { courseItem ->
                                                        (courseItem as? Map<String, Any>)?.let { courseData ->
                                                            ExamCourseDto(
                                                                code = courseData["code"] as? String ?: "",
                                                                name = courseData["name"] as? String ?: "",
                                                                students = (courseData["students"] as? Number)?.toInt() ?: 0,
                                                                batch = courseData["batch"] as? String ?: "",
                                                                slot = courseData["slot"] as? String ?: ""
                                                            )
                                                        }
                                                    } ?: emptyList()
                                                )
                                            }
                                        } ?: emptyList(),
                                        version = (data["version"] as? Number)?.toLong() ?: 1L,
                                        createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
                                        updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: 0L
                                    )
                                }
                                
                                if (examRoutineDto != null) {
                                    val examRoutine = examRoutineDto.toDomainModel()
                                    Log.d(TAG, "Observed exam routine update: ${examRoutine.examType}")
                                    trySend(examRoutine)
                                    return@addSnapshotListener
                                }
                            }
                        }
                        // No matching document found
                        Log.d(TAG, "No matching exam routine found in observation")
                        trySend(null)
                    } else {
                        Log.d(TAG, "No exam routine found for department: $department")
                        trySend(null)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up exam routine listener", e)
            close(e)
            return@callbackFlow
        }

        awaitClose {
            Log.d(TAG, "Closing exam routine listener for department: $department")
            listenerRegistration?.remove()
        }
    }

    suspend fun uploadExamRoutine(examRoutine: ExamRoutine): Result<String> {
        return try {
            Log.d(TAG, "Uploading exam routine: ${examRoutine.examType}")
            
            val examRoutineDto = examRoutine.toDto()
            val documentRef = if (examRoutine.id.isNotEmpty()) {
                firestore.collection(EXAM_ROUTINES_COLLECTION).document(examRoutine.id)
            } else {
                firestore.collection(EXAM_ROUTINES_COLLECTION).document()
            }

            documentRef.set(examRoutineDto).await()
            Log.d(TAG, "Exam routine uploaded successfully with ID: ${documentRef.id}")
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading exam routine", e)
            Result.failure(e)
        }
    }

    suspend fun deleteExamRoutine(examRoutineId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting exam routine with ID: $examRoutineId")
            
            firestore.collection(EXAM_ROUTINES_COLLECTION)
                .document(examRoutineId)
                .delete()
                .await()
                
            Log.d(TAG, "Exam routine deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting exam routine", e)
            Result.failure(e)
        }
    }

    data class ExamModeInfo(
        val isExamMode: Boolean = false,
        val examMessage: String? = null
    )

    suspend fun getExamModeInfo(): Result<ExamModeInfo> {
        return try {
            Log.d(TAG, "Fetching exam mode info from Firestore metadata/routine_version")
            val metadataDoc = firestore.collection("metadata").document("routine_version").get().await()
            
            if (metadataDoc.exists()) {
                val data = metadataDoc.data
                Log.d(TAG, "Metadata document exists with data: $data")
                
                val examMode = data?.get("examMode") as? Boolean ?: false
                val examMessage = data?.get("examMessage") as? String
                
                Log.d(TAG, "Exam mode info - examMode: $examMode, examMessage: $examMessage")
                
                val examModeInfo = ExamModeInfo(
                    isExamMode = examMode,
                    examMessage = examMessage
                )
                
                Result.success(examModeInfo)
            } else {
                Log.d(TAG, "Metadata document does not exist, returning default exam mode info")
                Result.success(ExamModeInfo())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching exam mode info", e)
            Result.failure(e)
        }
    }
}