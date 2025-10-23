package com.om.diucampusschedule

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.om.diucampusschedule.core.reminder.ClassReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider, LifecycleEventObserver {

    @Inject
    lateinit var classReminderScheduler: ClassReminderScheduler

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    companion object {
        /**
         * Tracks if the app is in the foreground.
         * This is essential for preventing duplicate notifications.
         */
        var isAppInForeground = false
            private set
    }

    /**
     * Provides the WorkManager configuration with Hilt's factory.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Register this class to observe the app's lifecycle
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // Initialize class reminder scheduler
        try {
            classReminderScheduler.initialize()
        } catch (e: Exception) {
            Log.e("App", "Failed to initialize class reminder scheduler", e)
        }
    }

    /**
     * This callback updates our 'isAppInForeground' flag.
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> {
                isAppInForeground = true
                Log.d("AppLifecycle", "App is in FOREGROUND")
            }
            Lifecycle.Event.ON_STOP -> {
                isAppInForeground = false
                Log.d("AppLifecycle", "App is in BACKGROUND")
            }
            else -> { /* We don't need other events */ }
        }
    }
}