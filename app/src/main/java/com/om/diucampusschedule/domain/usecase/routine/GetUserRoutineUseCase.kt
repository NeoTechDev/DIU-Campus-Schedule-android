package com.om.diucampusschedule.domain.usecase.routine

import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.repository.RoutineRepository
import javax.inject.Inject

class GetUserRoutineUseCase @Inject constructor(
    private val routineRepository: RoutineRepository
) {
    suspend operator fun invoke(user: User): Result<List<RoutineItem>> {
        return routineRepository.getRoutineForUser(user)
    }
}
