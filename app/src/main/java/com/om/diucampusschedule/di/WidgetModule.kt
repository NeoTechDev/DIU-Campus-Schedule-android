package com.om.diucampusschedule.di

import com.om.diucampusschedule.widget.WidgetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for widget-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object WidgetModule {
    
    @Provides
    @Singleton
    fun provideWidgetManager(): WidgetManager {
        return WidgetManager()
    }
}
