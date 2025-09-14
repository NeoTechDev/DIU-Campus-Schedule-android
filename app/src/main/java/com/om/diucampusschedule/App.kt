package com.om.diucampusschedule

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.messaging.FirebaseMessaging
import com.om.diucampusschedule.core.reminder.ClassReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {
    
    @Inject
    lateinit var classReminderScheduler: ClassReminderScheduler
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        
        // Initialize class reminder scheduler
        try {
            classReminderScheduler.initialize()
        } catch (e: Exception) {
            // Log error but don't crash the app
            android.util.Log.e("App", "Failed to initialize class reminder scheduler", e)
        }
        
        // Subscribe to FCM topics
        try {
            subscribeToFCMTopics()
        } catch (e: Exception) {
            android.util.Log.e("App", "Failed to subscribe to FCM topics", e)
        }
    }
    
    private fun subscribeToFCMTopics() {
        val topics = listOf("general", "admin", "maintenance", "all")
        
        topics.forEach { topic ->
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        android.util.Log.d("App", "Successfully subscribed to topic: $topic")
                    } else {
                        android.util.Log.e("App", "Failed to subscribe to topic: $topic", task.exception)
                    }
                }
        }
    }
}