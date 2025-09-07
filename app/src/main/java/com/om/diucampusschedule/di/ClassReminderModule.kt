package com.om.diucampusschedule.di

import com.om.diucampusschedule.core.reminder.ClassReminderAlarmManager
import com.om.diucampusschedule.core.reminder.ClassReminderScheduler
import com.om.diucampusschedule.core.reminder.ClassReminderService
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for class reminder dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object ClassReminderModule {
    // All dependencies are provided via @Inject constructors
    // This module exists to ensure proper Hilt component installation
}
