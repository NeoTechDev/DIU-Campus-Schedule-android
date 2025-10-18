package com.om.diucampusschedule.di

import com.om.diucampusschedule.data.repository.ExamRoutineRepositoryImpl
import com.om.diucampusschedule.domain.repository.ExamRoutineRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExamRoutineModule {
    
    @Binds
    @Singleton
    abstract fun bindExamRoutineRepository(
        examRoutineRepositoryImpl: ExamRoutineRepositoryImpl
    ): ExamRoutineRepository
}