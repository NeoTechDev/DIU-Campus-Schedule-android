package com.om.diucampusschedule.domain.usecase.exam

import com.om.diucampusschedule.domain.model.ExamRoutine
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.repository.ExamRoutineRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveExamRoutineForUserUseCase @Inject constructor(
    private val examRoutineRepository: ExamRoutineRepository
) {
    operator fun invoke(user: User): Flow<ExamRoutine?> {
        return examRoutineRepository.observeExamRoutineForDepartment(user.department)
    }
}