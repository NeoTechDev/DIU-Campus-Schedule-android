package com.om.diucampusschedule.core.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.om.diucampusschedule.core.logging.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Professional notification permission handler that manages permission requests
 * and provides callbacks for permission results.
 * 
 * This class should be used from Activities to handle the actual permission
 * request dialogs and user interactions.
 */
@Singleton
class NotificationPermissionHandler @Inject constructor(
    private val permissionManager: NotificationPermissionManager,
    private val logger: AppLogger
) {

    companion object {
        private const val TAG = "NotificationPermissionHandler"
    }

    // Coroutine scope for handling suspend operations
    private val handlerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _permissionResults = MutableSharedFlow<NotificationPermissionResult>()
    val permissionResults: SharedFlow<NotificationPermissionResult> = _permissionResults.asSharedFlow()

    private var notificationPermissionLauncher: ActivityResultLauncher<String>? = null
    private var exactAlarmPermissionLauncher: ActivityResultLauncher<Intent>? = null

    /**
     * Initialize permission launchers with the given activity.
     * Call this in onCreate() of your activity.
     */
    fun initialize(activity: ComponentActivity) {
        // Initialize notification permission launcher for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher = activity.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                handleNotificationPermissionResult(isGranted)
            }
        }

        // Initialize exact alarm permission launcher for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            exactAlarmPermissionLauncher = activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { _ ->
                // Check if exact alarm permission is now granted
                val isGranted = permissionManager.hasExactAlarmPermission()
                handleExactAlarmPermissionResult(isGranted)
            }
        }

        logger.info(TAG, "Permission launchers initialized for activity: ${activity.javaClass.simpleName}")
    }

    /**
     * Request notification permission automatically if needed (first time user)
     * This is a convenience method for initial app permission setup
     */
    fun requestNotificationPermissionIfNeeded() {
        // Only request if permission hasn't been requested before and not currently granted
        if (!permissionManager.hasNotificationPermissionBeenRequested() && 
            !permissionManager.hasNotificationPermission()) {
            logger.info(TAG, "First time user - requesting notification permission")
            // Launch permission request without blocking
            requestNotificationPermission()
        } else {
            logger.info(TAG, "Notification permission already requested or granted")
        }
    }

    /**
     * Request notification permission
     */
    fun requestNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (permissionManager.hasNotificationPermission()) {
                logger.info(TAG, "Notification permission already granted")
                emitPermissionResult(NotificationPermissionResult.NotificationGranted)
                true
            } else {
                logger.info(TAG, "Requesting notification permission")
                notificationPermissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
                // Mark permission as requested in background
                handlerScope.launch {
                    permissionManager.markNotificationPermissionRequested()
                }
                false // Permission request initiated
            }
        } else {
            logger.info(TAG, "Notification permission not required for this Android version")
            emitPermissionResult(NotificationPermissionResult.NotificationGranted)
            true
        }
    }

    /**
     * Request exact alarm permission for task reminders
     */
    fun requestExactAlarmPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (permissionManager.hasExactAlarmPermission()) {
                logger.info(TAG, "Exact alarm permission already granted")
                emitPermissionResult(NotificationPermissionResult.ExactAlarmGranted)
                true
            } else {
                logger.info(TAG, "Requesting exact alarm permission")
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                exactAlarmPermissionLauncher?.launch(intent)
                false // Permission request initiated
            }
        } else {
            logger.info(TAG, "Exact alarm permission not required for this Android version")
            emitPermissionResult(NotificationPermissionResult.ExactAlarmGranted)
            true
        }
    }

    /**
     * Request all necessary permissions based on current settings
     */
    suspend fun requestAllNecessaryPermissions(context: Context) {
        val needsNotification = !permissionManager.hasNotificationPermission()
        val needsExactAlarm = !permissionManager.hasExactAlarmPermission()

        when {
            needsNotification -> {
                logger.info(TAG, "Requesting notification permission first")
                requestNotificationPermission()
            }
            needsExactAlarm -> {
                logger.info(TAG, "Requesting exact alarm permission")
                requestExactAlarmPermission(context)
            }
            else -> {
                logger.info(TAG, "All permissions already granted")
                emitPermissionResult(NotificationPermissionResult.AllPermissionsGranted)
            }
        }
    }

    /**
     * Open app settings for manual permission management
     */
    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            logger.info(TAG, "Opened app settings for manual permission management")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to open app settings", e)
        }
    }

    /**
     * Open notification settings for the app
     */
    fun openNotificationSettings(context: Context) {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            logger.info(TAG, "Opened notification settings")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to open notification settings", e)
        }
    }

    private fun handleNotificationPermissionResult(isGranted: Boolean) {
        permissionManager.updatePermissionState()
        
        if (isGranted) {
            logger.info(TAG, "Notification permission granted")
            emitPermissionResult(NotificationPermissionResult.NotificationGranted)
            
            // Check if we need to request exact alarm permission next
            if (!permissionManager.hasExactAlarmPermission() && 
                permissionManager.needsPermissionRequest()) {
                // Will be handled by the calling code
            }
        } else {
            logger.warning(TAG, "Notification permission denied")
            emitPermissionResult(NotificationPermissionResult.NotificationDenied)
        }
    }

    private fun handleExactAlarmPermissionResult(isGranted: Boolean) {
        permissionManager.updatePermissionState()
        
        if (isGranted) {
            logger.info(TAG, "Exact alarm permission granted")
            emitPermissionResult(NotificationPermissionResult.ExactAlarmGranted)
        } else {
            logger.warning(TAG, "Exact alarm permission denied")
            emitPermissionResult(NotificationPermissionResult.ExactAlarmDenied)
        }
    }

    private fun emitPermissionResult(result: NotificationPermissionResult) {
        _permissionResults.tryEmit(result)
    }

    /**
     * Check current permission status and emit appropriate result
     */
    fun checkAndEmitCurrentStatus() {
        permissionManager.updatePermissionState()
        val state = permissionManager.permissionState.value
        
        when {
            state.hasAllPermissions -> emitPermissionResult(NotificationPermissionResult.AllPermissionsGranted)
            state.hasNotificationPermission && !state.hasExactAlarmPermission -> 
                emitPermissionResult(NotificationPermissionResult.ExactAlarmDenied)
            !state.hasNotificationPermission && state.hasExactAlarmPermission -> 
                emitPermissionResult(NotificationPermissionResult.NotificationDenied)
            else -> emitPermissionResult(NotificationPermissionResult.AllPermissionsDenied)
        }
    }
}

/**
 * Sealed class representing different permission results
 */
sealed class NotificationPermissionResult {
    object NotificationGranted : NotificationPermissionResult()
    object NotificationDenied : NotificationPermissionResult()
    object ExactAlarmGranted : NotificationPermissionResult()
    object ExactAlarmDenied : NotificationPermissionResult()
    object AllPermissionsGranted : NotificationPermissionResult()
    object AllPermissionsDenied : NotificationPermissionResult()
}
