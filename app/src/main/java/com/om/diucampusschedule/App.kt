package com.om.diucampusschedule

import android.app.Application
import android.content.Intent
import com.om.diucampusschedule.core.reminder.ClassReminderScheduler
import com.om.diucampusschedule.data.remote.NotificationPollingService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    
    @Inject
    lateinit var classReminderScheduler: ClassReminderScheduler

    override fun onCreate() {
        super.onCreate()
        
        // Start real-time notification service for admin updates
        startService(Intent(this, NotificationPollingService::class.java))
        
        // Initialize class reminder scheduler
        try {
            classReminderScheduler.initialize()
        } catch (e: Exception) {
            // Log error but don't crash the app
            android.util.Log.e("App", "Failed to initialize class reminder scheduler", e)
        }
    }
}