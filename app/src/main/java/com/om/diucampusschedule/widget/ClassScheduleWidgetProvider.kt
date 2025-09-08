package com.om.diucampusschedule.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.om.diucampusschedule.widget.glance.ClassScheduleWidget

/**
 * Implementation of App Widget functionality for Daily Class Schedule.
 * Provides users with a convenient home screen widget to view their daily classes.
 */
class ClassScheduleWidgetProvider : GlanceAppWidgetReceiver() {
    
    override val glanceAppWidget: GlanceAppWidget = ClassScheduleWidget()
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Called when the first widget instance is added to the home screen
        // You can perform one-time setup here
    }
    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Called when the last widget instance is removed from the home screen
        // You can perform cleanup here
    }
}
