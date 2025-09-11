package com.om.diucampusschedule.di

import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.core.permission.NotificationPermissionHandler
import com.om.diucampusschedule.core.permission.NotificationPermissionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing notification permission related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object NotificationPermissionModule {

    @Provides
    @Singleton
    fun provideNotificationPermissionHandler(
        permissionManager: NotificationPermissionManager,
        logger: AppLogger
    ): NotificationPermissionHandler {
        return NotificationPermissionHandler(permissionManager, logger)
    }
}
