package com.om.diucampusschedule.domain.usecase.routine

import com.om.diucampusschedule.domain.repository.RoutineRepository
import javax.inject.Inject

class SyncRoutineUseCase @Inject constructor(
    private val routineRepository: RoutineRepository
) {
    suspend operator fun invoke(department: String): Result<Unit> {
        return routineRepository.syncRoutineData(department)
    }
}
