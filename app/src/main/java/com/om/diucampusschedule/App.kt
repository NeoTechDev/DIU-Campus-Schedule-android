package com.om.diucampusschedule

import android.app.Application
import android.content.Intent
import com.om.diucampusschedule.data.remote.NotificationPollingService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    // This is a new comment

    override fun onCreate() {
        super.onCreate()
        
        // Start real-time notification service for admin updates
        startService(Intent(this, NotificationPollingService::class.java))
        
        // TODO: Setup WorkManager and sync when the worker system is properly configured
        // For now, keep the app simple to avoid crash
    }
}