package com.om.diucampusschedule

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    // This is a new comment

    override fun onCreate() {
        super.onCreate()
        
        // TODO: Setup WorkManager and sync when the worker system is properly configured
        // For now, keep the app simple to avoid crash
    }
}