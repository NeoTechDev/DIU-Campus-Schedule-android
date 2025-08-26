package com.om.diucampusschedule.domain.usecase.routine

import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUserRoutineForDayUseCase @Inject constructor(
    private val routineRepository: RoutineRepository
) {
    operator fun invoke(user: User, day: String): Flow<List<RoutineItem>> {
        return routineRepository.observeRoutineForUserAndDay(user, day)
    }
}
