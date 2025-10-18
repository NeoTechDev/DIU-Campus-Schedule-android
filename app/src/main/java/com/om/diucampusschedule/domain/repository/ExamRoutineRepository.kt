package com.om.diucampusschedule.domain.repository

import com.om.diucampusschedule.data.remote.ExamRoutineRemoteDataSource
import com.om.diucampusschedule.domain.model.ExamCourse
import com.om.diucampusschedule.domain.model.ExamRoutine
import com.om.diucampusschedule.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ExamRoutineRepository {
    
    // Get exam routine for a specific department
    suspend fun getExamRoutineForDepartment(department: String): Result<ExamRoutine?>
    
    // Observe exam routine changes for a department
    fun observeExamRoutineForDepartment(department: String): Flow<ExamRoutine?>
    
    // Get exam courses for a user and specific date
    suspend fun getExamCoursesForUserAndDate(user: User, date: String): Result<List<ExamCourse>>
    
    // Get all exam dates for a user
    suspend fun getExamDatesForUser(user: User): Result<List<String>>
    
    // Get exam mode information
    suspend fun getExamModeInfo(): Result<ExamRoutineRemoteDataSource.ExamModeInfo>
    
    // Upload exam routine
    suspend fun uploadExamRoutine(examRoutine: ExamRoutine): Result<String>
    
    // Delete exam routine
    suspend fun deleteExamRoutine(examRoutineId: String): Result<Unit>
}