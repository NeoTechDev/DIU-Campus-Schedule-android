package com.om.diucampusschedule.domain.usecase.routine

import com.om.diucampusschedule.data.remote.RoutineRemoteDataSource
import javax.inject.Inject

class GetMaintenanceInfoUseCase @Inject constructor(
    private val routineRemoteDataSource: RoutineRemoteDataSource
) {
    suspend operator fun invoke(): Result<RoutineRemoteDataSource.MaintenanceInfo> {
        return routineRemoteDataSource.getMaintenanceInfo()
    }
}
