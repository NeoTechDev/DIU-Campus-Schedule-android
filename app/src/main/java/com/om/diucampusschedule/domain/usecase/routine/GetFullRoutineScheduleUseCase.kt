package com.om.diucampusschedule.domain.usecase.routine

import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.repository.RoutineRepository
import javax.inject.Inject

class GetFullRoutineScheduleUseCase @Inject constructor(
    private val routineRepository: RoutineRepository
) {
    suspend operator fun invoke(department: String): Result<List<RoutineItem>> {
        return routineRepository.getLatestScheduleForDepartment(department)
            .map { schedule -> schedule.schedule }
    }
}
