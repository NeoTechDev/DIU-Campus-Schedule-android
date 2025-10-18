package com.om.diucampusschedule.domain.usecase.exam

import com.om.diucampusschedule.domain.model.ExamRoutine
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.repository.ExamRoutineRepository
import javax.inject.Inject

class GetExamRoutineForUserUseCase @Inject constructor(
    private val examRoutineRepository: ExamRoutineRepository
) {
    suspend operator fun invoke(user: User): Result<ExamRoutine?> {
        return examRoutineRepository.getExamRoutineForDepartment(user.department)
    }
}