package com.om.diucampusschedule.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import com.om.diucampusschedule.widget.glance.ClassScheduleWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Broadcast receiver for widget updates
 * Handles various system events that should trigger widget refresh
 */
@AndroidEntryPoint
class WidgetUpdateReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var widgetManager: WidgetManager
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_TIME_TICK -> {
                // Update every minute to keep time accurate
                updateWidgets(context)
            }
            Intent.ACTION_TIME_CHANGED -> {
                // Update when system time changes
                updateWidgets(context)
            }
            Intent.ACTION_TIMEZONE_CHANGED -> {
                // Update when timezone changes
                updateWidgets(context)
            }
            Intent.ACTION_DATE_CHANGED -> {
                // Update when date changes (midnight)
                updateWidgets(context)
            }
            "com.om.diucampusschedule.WIDGET_UPDATE" -> {
                // Custom action for manual widget updates
                updateWidgets(context)
            }
        }
    }
    
    private fun updateWidgets(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val glanceManager = GlanceAppWidgetManager(context)
                ClassScheduleWidget().updateAll(context)
                
                android.util.Log.d("WidgetUpdateReceiver", "Widgets updated successfully")
            } catch (e: Exception) {
                android.util.Log.e("WidgetUpdateReceiver", "Error updating widgets", e)
            }
        }
    }
}

/**
 * Widget manager for programmatic widget updates
 */
class WidgetManager @Inject constructor() {
    
    fun updateWidgets(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ClassScheduleWidget().updateAll(context)
                android.util.Log.d("WidgetManager", "Manual widget update completed")
            } catch (e: Exception) {
                android.util.Log.e("WidgetManager", "Error in manual widget update", e)
            }
        }
    }
    
    fun forceRefreshWidgets(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Send broadcast to trigger refresh
                val intent = Intent("com.om.diucampusschedule.WIDGET_UPDATE")
                context.sendBroadcast(intent)
                
                android.util.Log.d("WidgetManager", "Widget refresh broadcast sent")
            } catch (e: Exception) {
                android.util.Log.e("WidgetManager", "Error sending widget refresh broadcast", e)
            }
        }
    }
}
