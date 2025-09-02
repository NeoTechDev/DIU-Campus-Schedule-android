package com.om.diucampusschedule.di

import com.om.diucampusschedule.data.repository.AuthRepositoryImpl
import com.om.diucampusschedule.data.repository.NotesRepositoryImpl
import com.om.diucampusschedule.domain.repository.AuthRepository
import com.om.diucampusschedule.domain.repository.NotesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindNotesRepository(
        notesRepositoryImpl: NotesRepositoryImpl
    ): NotesRepository
}
