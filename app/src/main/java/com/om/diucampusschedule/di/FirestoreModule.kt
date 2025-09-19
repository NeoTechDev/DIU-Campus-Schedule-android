package com.om.diucampusschedule.di

import com.google.firebase.firestore.FirebaseFirestore
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.data.repository.UniversalNotificationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {

   /* @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }*/

    @Provides
    @Singleton
    fun provideUniversalNotificationRepository(
        firestore: FirebaseFirestore,
        logger: AppLogger
    ): UniversalNotificationRepository {
        return UniversalNotificationRepository(firestore, logger)
    }
}