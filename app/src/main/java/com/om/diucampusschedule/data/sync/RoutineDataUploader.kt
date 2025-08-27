package com.om.diucampusschedule.data.sync

import android.content.Context
import com.om.diucampusschedule.data.remote.RoutineRemoteDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineDataUploader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val routineRemoteDataSource: RoutineRemoteDataSource
) {

}
