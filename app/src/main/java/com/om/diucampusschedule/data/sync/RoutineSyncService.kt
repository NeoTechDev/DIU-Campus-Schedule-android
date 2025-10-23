package com.om.diucampusschedule.data.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.om.diucampusschedule.MainActivity
import com.om.diucampusschedule.R
import com.om.diucampusschedule.domain.repository.AuthRepository
import com.om.diucampusschedule.domain.repository.RoutineRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker // <-- USE @HiltWorker ANNOTATION
class RoutineSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context, // Make it a property
    @Assisted workerParams: WorkerParameters,
    private val routineRepository: RoutineRepository,
    private val authRepository: AuthRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "routine_sync_work"
        const val CHANNEL_ID = "routine_updates"
        const val NOTIFICATION_ID = 1001

        // --- KEYS FOR INPUT DATA ---
        const val KEY_DEPARTMENT = "department"
        const val KEY_IS_SILENT_SYNC = "is_silent_sync" // <-- The "don't notify" flag

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
    }

    override suspend fun doWork(): ListenableWorker.Result {
        return try {
            // --- UPDATED LOGIC ---

            // 1. Check if this is a silent sync (from FCM)
            val isSilentSync = inputData.getBoolean(KEY_IS_SILENT_SYNC, false)

            // 2. Get department
            // Either from FCM's input data OR from the logged-in user (for periodic)
            val departmentToSync = inputData.getString(KEY_DEPARTMENT)
                ?: authRepository.observeCurrentUser().first()?.department

            if (departmentToSync == null) {
                return ListenableWorker.Result.success() // No user, no department. We're done.
            }
            // ---

            // 3. Check for updates
            // We can skip this check if FCM (isSilentSync) told us to sync
            val hasUpdatesResult: kotlin.Result<Boolean> = if (isSilentSync) {
                kotlin.Result.success(true) // FCM already knows there's an update, just sync
            } else {
                routineRepository.checkForUpdates(departmentToSync)
            }

            val hasUpdates = hasUpdatesResult.getOrNull()
                ?: return ListenableWorker.Result.retry() // failed to check -> retry

            if (hasUpdates) {
                // 4. Sync the new routine data
                val syncResult: kotlin.Result<Unit> = routineRepository.syncRoutineData(departmentToSync)

                return if (syncResult.isSuccess) {
                    // --- MODIFIED NOTIFICATION LOGIC ---
                    if (!isSilentSync) {
                        // Only show notification if NOT silent
                        // (i.e., this is a periodic sync)
                        showRoutineUpdateNotification()
                    }
                    ListenableWorker.Result.success()
                } else {
                    ListenableWorker.Result.failure()
                }
            } else {
                ListenableWorker.Result.success() // No updates found
            }
        } catch (e: Exception) {
            ListenableWorker.Result.failure()
        }
    }

    private fun showRoutineUpdateNotification() {
        createNotificationChannel()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "routine")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_notification_logo) // Make sure this drawable exists!
            .setContentTitle("Routine Updated")
            .setContentText("Your class routine has been updated. Tap to view changes.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // --- DELETE THE 'Factory' INTERFACE and ALL OTHER FACTORY CLASSES ---
}