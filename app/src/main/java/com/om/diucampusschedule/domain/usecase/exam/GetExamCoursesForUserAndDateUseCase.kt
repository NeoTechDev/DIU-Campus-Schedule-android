package com.om.diucampusschedule.domain.usecase.exam

import com.om.diucampusschedule.domain.model.ExamCourse
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.repository.ExamRoutineRepository
import javax.inject.Inject

class GetExamCoursesForUserAndDateUseCase @Inject constructor(
    private val examRoutineRepository: ExamRoutineRepository
) {
    suspend operator fun invoke(user: User, date: String): Result<List<ExamCourse>> {
        return examRoutineRepository.getExamCoursesForUserAndDate(user, date)
    }
}