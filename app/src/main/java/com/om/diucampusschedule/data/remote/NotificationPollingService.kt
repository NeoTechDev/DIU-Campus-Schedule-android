package com.om.diucampusschedule.data.remote

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.core.notification.NotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationPollingService : Service() {

    @Inject
    lateinit var firestore: FirebaseFirestore
    
    @Inject
    lateinit var notificationManager: NotificationManager
    
    @Inject
    lateinit var logger: AppLogger

    private var metadataListener: ListenerRegistration? = null
    private var lastKnownVersion: Long = 0L

    companion object {
        private const val TAG = "NotificationPolling"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        logger.info(TAG, "NotificationPollingService created")
        startListeningForMetadataChanges()
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.info(TAG, "NotificationPollingService destroyed")
        metadataListener?.remove()
    }

    private fun startListeningForMetadataChanges() {
        logger.info(TAG, "Starting to listen for metadata changes")
        
        metadataListener = firestore.collection("metadata")
            .document("routine_version")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logger.error(TAG, "Error listening to metadata changes", error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data
                    val version = data?.get("version") as? Long ?: 0L
                    val updateType = data?.get("updateType") as? String
                    val maintenanceMessage = data?.get("maintenanceMessage") as? String
                    
                    logger.debug(TAG, "Metadata changed - version: $version, updateType: $updateType")
                    
                    // Only send notification if version actually changed (not initial load)
                    if (lastKnownVersion != 0L && version > lastKnownVersion && updateType != null) {
                        logger.info(TAG, "Version changed from $lastKnownVersion to $version - sending notification")
                        sendLocalNotification(updateType, maintenanceMessage)
                    }
                    
                    lastKnownVersion = version
                }
            }
    }

    private fun sendLocalNotification(updateType: String, message: String?) {
        val (title, body) = when (updateType) {
            "routine_deleted" -> "Schedule Update" to (message ?: "Your schedule is being updated. New schedule will be available soon.")
            "all_routines_deleted" -> "System Maintenance" to (message ?: "System maintenance in progress. New routines will be uploaded soon.")
            "maintenance_enabled" -> "System Maintenance" to (message ?: "System is under maintenance. Please check back later.")
            "semester_break" -> "Semester Break" to (message ?: "Semester break is in progress. New semester routine will be available soon.")
            "routine_uploaded" -> "New Schedule Available" to "Your class schedule has been updated with new information."
            "manual_trigger" -> "Schedule Refresh" to "Please refresh your app to see the latest schedule updates."
            "maintenance_disabled" -> "Service Restored" to "The app is back to normal. Your schedules are now available."
            else -> "Schedule Update" to (message ?: "Your schedule may have been updated.")
        }

        logger.info(TAG, "Sending notification: $title - $body")

        notificationManager.showGeneralNotification(
            title = title,
            message = body,
            actionRoute = "routine"
        )
    }
}
