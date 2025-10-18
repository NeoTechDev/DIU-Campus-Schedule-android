package com.om.diucampusschedule.domain.usecase.exam

import com.om.diucampusschedule.data.remote.ExamRoutineRemoteDataSource
import com.om.diucampusschedule.domain.repository.ExamRoutineRepository
import javax.inject.Inject

class GetExamModeInfoUseCase @Inject constructor(
    private val examRoutineRepository: ExamRoutineRepository
) {
    suspend operator fun invoke(): Result<ExamRoutineRemoteDataSource.ExamModeInfo> {
        return examRoutineRepository.getExamModeInfo()
    }
}