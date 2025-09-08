package com.om.diucampusschedule.widget.glance

import com.om.diucampusschedule.widget.data.WidgetDataRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt Entry Point for accessing dependencies in widgets
 * Since widgets don't support direct dependency injection, we use this entry point
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun widgetDataRepository(): WidgetDataRepository
}
