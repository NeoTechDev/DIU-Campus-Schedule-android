package com.om.diucampusschedule.util

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.om.diucampusschedule.data.model.ExamCourseDto
import com.om.diucampusschedule.data.model.ExamDayDto
import com.om.diucampusschedule.data.model.ExamRoutineDto
import kotlinx.coroutines.tasks.await
import java.io.InputStreamReader

/**
 * Developer utility for uploading exam routine data to Firebase
 * This bypasses authentication and directly uploads data for development purposes
 */
object DeveloperExamRoutineUpload {
    
    private const val TAG = "DeveloperExamUpload"
    private const val EXAM_ROUTINES_COLLECTION = "examRoutines"
    
    /**
     * Upload exam routine data directly to Firebase (Developer only)
     * This function bypasses all authentication checks
     */
    suspend fun uploadExamRoutineDataToFirebase(
        context: Context,
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ): Result<String> {
        return try {
            Log.d(TAG, "🎯 Developer: Starting exam routine upload from examRoutine.json")
            
            // Read the examRoutine.json file from assets
            val inputStream = context.assets.open("examRoutine.json")
            val reader = InputStreamReader(inputStream)
            val gson = Gson()
            
            // Parse the JSON structure
            val jsonObject = gson.fromJson(reader, com.google.gson.JsonObject::class.java)
            
            Log.d(TAG, "📖 JSON parsed successfully: ${jsonObject.keySet()}")
            
            // Extract basic info
            val university = jsonObject.get("university")?.asString ?: ""
            val department = jsonObject.get("department")?.asString ?: ""
            val examType = jsonObject.get("exam_type")?.asString ?: ""
            val semester = jsonObject.get("semester")?.asString ?: ""
            val startDate = jsonObject.get("start_date")?.asString ?: ""
            val endDate = jsonObject.get("end_date")?.asString ?: ""
            
            // Extract slots
            val slotsJsonObject = jsonObject.getAsJsonObject("slots")
            val slots = mutableMapOf<String, String>()
            slotsJsonObject?.entrySet()?.forEach { entry ->
                slots[entry.key] = entry.value.asString
            }
            
            // Extract schedule
            val scheduleJsonArray = jsonObject.getAsJsonArray("schedule")
            val schedule = mutableListOf<ExamDayDto>()
            
            scheduleJsonArray?.forEach { scheduleElement ->
                val dayObject = scheduleElement.asJsonObject
                val dayNumber = dayObject.get("day_number")?.asInt ?: 0
                val date = dayObject.get("date")?.asString ?: ""
                val weekday = dayObject.get("weekday")?.asString ?: ""
                
                val coursesJsonArray = dayObject.getAsJsonArray("courses")
                val courses = mutableListOf<ExamCourseDto>()
                
                coursesJsonArray?.forEach { courseElement ->
                    val courseObject = courseElement.asJsonObject
                    val course = ExamCourseDto(
                        code = courseObject.get("code")?.asString ?: "",
                        name = courseObject.get("name")?.asString ?: "",
                        students = courseObject.get("students")?.asInt ?: 0,
                        batch = courseObject.get("batch")?.asString ?: "",
                        slot = courseObject.get("slot")?.asString ?: ""
                    )
                    courses.add(course)
                }
                
                val examDay = ExamDayDto(
                    dayNumber = dayNumber,
                    date = date,
                    weekday = weekday,
                    courses = courses
                )
                schedule.add(examDay)
            }
            
            Log.d(TAG, "📚 Parsed exam routine: $examType for $department")
            Log.d(TAG, "📅 Schedule: ${schedule.size} days, ${schedule.sumOf { it.courses.size }} total exam courses")
            
            val currentTime = System.currentTimeMillis()
            
            // Create ExamRoutineDto
            val examRoutineDto = ExamRoutineDto(
                university = university,
                department = department,
                examType = examType,
                semester = semester,
                startDate = startDate,
                endDate = endDate,
                slots = slots,
                schedule = schedule,
                version = currentTime,
                createdAt = currentTime,
                updatedAt = currentTime
            )
            
            // Check if exam routine already exists for this department and exam type
            val existingQuery = firestore.collection(EXAM_ROUTINES_COLLECTION)
                .whereEqualTo("department", department)
                .whereEqualTo("exam_type", examType)
                .limit(1)
                .get()
                .await()
            
            val documentRef = if (existingQuery.documents.isNotEmpty()) {
                Log.d(TAG, "📝 Updating existing exam routine document")
                existingQuery.documents.first().reference
            } else {
                Log.d(TAG, "✨ Creating new exam routine document")
                firestore.collection(EXAM_ROUTINES_COLLECTION).document()
            }
            
            // Upload to Firestore (this works without authentication in development)
            documentRef.set(examRoutineDto).await()
            
            val message = "✅ Exam routine uploaded successfully!\n" +
                    "📄 Document ID: ${documentRef.id}\n" +
                    "🏫 Department: $department\n" +
                    "📝 Exam Type: $examType\n" +
                    "📅 Semester: $semester\n" +
                    "📚 Exam Courses: ${schedule.sumOf { it.courses.size }}\n" +
                    "🔢 Version: $currentTime"
            
            Log.d(TAG, message)
            
            reader.close()
            inputStream.close()
            
            Result.success(message)
            
        } catch (e: Exception) {
            val errorMessage = "❌ Failed to upload exam routine: ${e.message}"
            Log.e(TAG, errorMessage, e)
            Result.failure(e)
        }
    }
    
    /**
     * Quick setup: Upload exam routine for immediate use
     */
    suspend fun quickExamSetup(
        context: Context,
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ): Result<String> {
        return try {
            Log.d(TAG, "🚀 Developer: Quick exam setup starting...")
            
            // Upload the exam routine
            val uploadResult = uploadExamRoutineDataToFirebase(context, firestore)
            
            uploadResult.fold(
                onSuccess = { uploadMessage ->
                    Log.d(TAG, "✅ Exam upload successful")
                    
                    val combinedMessage = "🎉 Quick Exam Setup Complete!\n\n" +
                            "📤 Upload: $uploadMessage"
                    
                    Log.d(TAG, combinedMessage)
                    Result.success(combinedMessage)
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Exam upload failed", error)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            val errorMessage = "❌ Quick exam setup failed: ${e.message}"
            Log.e(TAG, errorMessage, e)
            Result.failure(e)
        }
    }
    
    /**
     * List all exam routines in Firestore
     */
    suspend fun listAllExamRoutines(
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ): Result<List<String>> {
        return try {
            Log.d(TAG, "📋 Listing all exam routines...")
            
            val querySnapshot = firestore.collection(EXAM_ROUTINES_COLLECTION)
                .get()
                .await()
            
            val examRoutines = querySnapshot.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) {
                    val department = data["department"] as? String ?: "Unknown"
                    val examType = data["exam_type"] as? String ?: "Unknown"
                    val semester = data["semester"] as? String ?: "Unknown"
                    val scheduleSize = (data["schedule"] as? List<*>)?.size ?: 0
                    "📄 ${doc.id}: $examType - $department ($semester) - $scheduleSize days"
                } else null
            }
            
            Log.d(TAG, "Found ${examRoutines.size} exam routines")
            Result.success(examRoutines)
            
        } catch (e: Exception) {
            val errorMessage = "❌ Failed to list exam routines: ${e.message}"
            Log.e(TAG, errorMessage, e)
            Result.failure(e)
        }
    }
}