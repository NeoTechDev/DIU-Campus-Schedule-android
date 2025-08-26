package com.om.diucampusschedule.domain.usecase.routine

import com.om.diucampusschedule.domain.model.RoutineSchedule
import com.om.diucampusschedule.domain.repository.RoutineRepository
import javax.inject.Inject

class RefreshRoutineUseCase @Inject constructor(
    private val routineRepository: RoutineRepository
) {
    suspend operator fun invoke(department: String): Result<RoutineSchedule> {
        return routineRepository.refreshFromRemote(department)
    }
}
