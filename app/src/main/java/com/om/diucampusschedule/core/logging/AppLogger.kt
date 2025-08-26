package com.om.diucampusschedule.core.logging

import android.util.Log
import com.google.firebase.crashlytics.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLogger @Inject constructor(
    private val crashlytics: FirebaseCrashlytics
) {
    
    companion object {
        private const val DEFAULT_TAG = "DIUCampusSchedule"
    }

    fun debug(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message, throwable)
        }
    }

    fun info(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        Log.i(tag, message, throwable)
        if (!BuildConfig.DEBUG) {
            crashlytics.log("INFO/$tag: $message")
        }
    }

    fun warning(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        Log.w(tag, message, throwable)
        crashlytics.log("WARN/$tag: $message")
        throwable?.let { crashlytics.recordException(it) }
    }

    fun error(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        crashlytics.log("ERROR/$tag: $message")
        throwable?.let { crashlytics.recordException(it) }
    }

    fun logUserAction(action: String, parameters: Map<String, String> = emptyMap()) {
        val message = "UserAction: $action ${parameters.entries.joinToString(", ") { "${it.key}=${it.value}" }}"
        debug("UserAction", message)
        
        // Log custom keys to Crashlytics for better debugging
        parameters.forEach { (key, value) ->
            crashlytics.setCustomKey(key, value)
        }
        crashlytics.log(message)
    }

    fun logNetworkRequest(method: String, url: String, statusCode: Int? = null, duration: Long? = null) {
        val message = buildString {
            append("NetworkRequest: $method $url")
            statusCode?.let { append(" - Status: $it") }
            duration?.let { append(" - Duration: ${it}ms") }
        }
        debug("Network", message)
        crashlytics.log(message)
    }

    fun logDatabaseOperation(operation: String, table: String, success: Boolean, duration: Long? = null) {
        val message = buildString {
            append("DatabaseOp: $operation on $table")
            append(" - Success: $success")
            duration?.let { append(" - Duration: ${it}ms") }
        }
        debug("Database", message)
        crashlytics.log(message)
    }

    fun logSyncOperation(operation: String, result: String, itemCount: Int? = null) {
        val message = buildString {
            append("SyncOp: $operation - $result")
            itemCount?.let { append(" - Items: $it") }
        }
        info("Sync", message)
    }

    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    fun setUserProperty(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }
}
