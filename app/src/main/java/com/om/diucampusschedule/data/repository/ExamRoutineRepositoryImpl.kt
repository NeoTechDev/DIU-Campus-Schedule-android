package com.om.diucampusschedule.data.repository

import com.om.diucampusschedule.data.remote.ExamRoutineRemoteDataSource
import com.om.diucampusschedule.domain.model.ExamCourse
import com.om.diucampusschedule.domain.model.ExamRoutine
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.repository.ExamRoutineRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamRoutineRepositoryImpl @Inject constructor(
    private val remoteDataSource: ExamRoutineRemoteDataSource
) : ExamRoutineRepository {

    override suspend fun getExamRoutineForDepartment(department: String): Result<ExamRoutine?> {
        return remoteDataSource.getExamRoutineForDepartment(department)
    }

    override fun observeExamRoutineForDepartment(department: String): Flow<ExamRoutine?> {
        return remoteDataSource.observeExamRoutineForDepartment(department)
    }

    override suspend fun getExamCoursesForUserAndDate(user: User, date: String): Result<List<ExamCourse>> {
        return try {
            val examRoutineResult = getExamRoutineForDepartment(user.department)
            examRoutineResult.fold(
                onSuccess = { examRoutine ->
                    if (examRoutine != null) {
                        val examCourses = examRoutine.getExamCoursesForDay(date, user)
                        Result.success(examCourses)
                    } else {
                        Result.success(emptyList())
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

    override suspend fun getExamDatesForUser(user: User): Result<List<String>> {
        return try {
            val examRoutineResult = getExamRoutineForDepartment(user.department)
            examRoutineResult.fold(
                onSuccess = { examRoutine ->
                    if (examRoutine != null) {
                        val examDates = examRoutine.getActiveDatesForUser(user)
                        Result.success(examDates)
                    } else {
                        Result.success(emptyList())
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

    override suspend fun getExamModeInfo(): Result<ExamRoutineRemoteDataSource.ExamModeInfo> {
        return remoteDataSource.getExamModeInfo()
    }

    override suspend fun uploadExamRoutine(examRoutine: ExamRoutine): Result<String> {
        return remoteDataSource.uploadExamRoutine(examRoutine)
    }

    override suspend fun deleteExamRoutine(examRoutineId: String): Result<Unit> {
        return remoteDataSource.deleteExamRoutine(examRoutineId)
    }
}