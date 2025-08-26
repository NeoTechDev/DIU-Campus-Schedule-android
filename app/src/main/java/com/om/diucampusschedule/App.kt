package com.om.diucampusschedule

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.om.diucampusschedule.data.sync.RoutineSyncWorker
import com.om.diucampusschedule.data.sync.WorkerFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App(override val workManagerConfiguration: Configuration) : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: WorkerFactory

    override fun onCreate() {
        super.onCreate()
        
        // Initialize WorkManager with custom factory
        WorkManager.initialize(this, workManagerConfiguration)
        
        // Schedule periodic routine sync
        RoutineSyncWorker.enqueuePeriodicSync(this)
    }

    fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}