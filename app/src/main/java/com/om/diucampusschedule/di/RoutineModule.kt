package com.om.diucampusschedule.di

import com.om.diucampusschedule.data.repository.RoutineRepositoryImpl
import com.om.diucampusschedule.domain.repository.RoutineRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RoutineModule {
    
    @Binds
    @Singleton
    abstract fun bindRoutineRepository(
        routineRepositoryImpl: RoutineRepositoryImpl
    ): RoutineRepository
}
