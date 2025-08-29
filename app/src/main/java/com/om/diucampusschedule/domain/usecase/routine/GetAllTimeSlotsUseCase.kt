package com.om.diucampusschedule.domain.usecase.routine

import com.om.diucampusschedule.domain.repository.RoutineRepository
import javax.inject.Inject

class GetAllTimeSlotsUseCase @Inject constructor(
    private val routineRepository: RoutineRepository
) {
    suspend operator fun invoke(department: String): Result<List<String>> {
        return routineRepository.getAllTimeSlotsForDepartment(department)
    }
}