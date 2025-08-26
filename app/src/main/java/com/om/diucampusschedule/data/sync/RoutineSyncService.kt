package com.om.diucampusschedule.data.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.om.diucampusschedule.MainActivity
import com.om.diucampusschedule.R
import com.om.diucampusschedule.domain.repository.AuthRepository
import com.om.diucampusschedule.domain.repository.RoutineRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class RoutineSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val routineRepository: RoutineRepository,
    private val authRepository: AuthRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "routine_sync_work"
        const val CHANNEL_ID = "routine_updates"
        const val NOTIFICATION_ID = 1001
        
        fun enqueuePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicWorkRequest = PeriodicWorkRequestBuilder<RoutineSyncWorker>(
                repeatInterval = 6, // Check every 6 hours
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicWorkRequest
                )
        }
        
        fun enqueueSyncNow(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeWorkRequest = OneTimeWorkRequestBuilder<RoutineSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueue(oneTimeWorkRequest)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            // Get current user
            val currentUser = authRepository.observeCurrentUser().first()
            if (currentUser == null) {
                return Result.success()
            }

            // Check for routine updates
            val hasUpdates = routineRepository.checkForUpdates(currentUser.department)
            
            hasUpdates.fold(
                onSuccess = { updates ->
                    if (updates) {
                        // Sync the new routine data
                        routineRepository.syncRoutineData(currentUser.department).fold(
                            onSuccess = {
                                // Show notification about routine update
                                showRoutineUpdateNotification()
                                Result.success()
                            },
                            onFailure = { error ->
                                Result.failure()
                            }
                        )
                    } else {
                        Result.success()
                    }
                },
                onFailure = { error ->
                    Result.retry()
                }
            )
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun showRoutineUpdateNotification() {
        createNotificationChannel()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "routine")
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // You'll need to add this icon
            .setContentTitle("Routine Updated")
            .setContentText("Your class schedule has been updated. Tap to view changes.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Routine Updates"
            val descriptionText = "Notifications about schedule changes"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @dagger.assisted.AssistedFactory
    interface Factory {
        fun create(context: Context, workerParams: WorkerParameters): RoutineSyncWorker
    }
}

class RoutineSyncChildWorkerFactory @AssistedInject constructor(
    private val routineSyncWorkerFactory: RoutineSyncWorker.Factory
) : ChildWorkerFactory {
    
    override fun create(appContext: Context, workerParams: WorkerParameters): ListenableWorker {
        return routineSyncWorkerFactory.create(appContext, workerParams)
    }
}

interface ChildWorkerFactory {
    fun create(appContext: Context, workerParams: WorkerParameters): ListenableWorker
}

class WorkerFactory(
    private val routineSyncChildWorkerFactory: RoutineSyncChildWorkerFactory
) : androidx.work.WorkerFactory() {
    
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            RoutineSyncWorker::class.java.name -> {
                routineSyncChildWorkerFactory.create(appContext, workerParameters)
            }
            else -> null
        }
    }
}
