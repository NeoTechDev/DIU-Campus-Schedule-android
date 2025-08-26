package com.om.diucampusschedule.core.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics,
    private val logger: AppLogger
) {

    companion object {
        // Screen Names
        const val SCREEN_SIGN_IN = "sign_in"
        const val SCREEN_SIGN_UP = "sign_up"
        const val SCREEN_REGISTRATION = "registration"
        const val SCREEN_ROUTINE = "routine"
        const val SCREEN_DEBUG = "debug"
        
        // Event Names
        const val EVENT_USER_SIGN_IN = "user_sign_in"
        const val EVENT_USER_SIGN_UP = "user_sign_up"
        const val EVENT_USER_SIGN_OUT = "user_sign_out"
        const val EVENT_ROUTINE_VIEW = "routine_view"
        const val EVENT_ROUTINE_REFRESH = "routine_refresh"
        const val EVENT_DAY_SELECT = "day_select"
        const val EVENT_CLASS_VIEW = "class_view"
        const val EVENT_NOTIFICATION_RECEIVED = "notification_received"
        const val EVENT_SYNC_COMPLETED = "sync_completed"
        const val EVENT_ERROR_OCCURRED = "error_occurred"
        const val EVENT_OFFLINE_ACCESS = "offline_access"
        
        // Parameter Names
        const val PARAM_METHOD = "method"
        const val PARAM_SUCCESS = "success"
        const val PARAM_ERROR_TYPE = "error_type"
        const val PARAM_SCREEN_NAME = "screen_name"
        const val PARAM_USER_ROLE = "user_role"
        const val PARAM_DEPARTMENT = "department"
        const val PARAM_DAY = "day"
        const val PARAM_COURSE_CODE = "course_code"
        const val PARAM_SYNC_TYPE = "sync_type"
        const val PARAM_ITEM_COUNT = "item_count"
        const val PARAM_DURATION = "duration"
        const val PARAM_NOTIFICATION_TYPE = "notification_type"
        
        private const val TAG = "AnalyticsManager"
    }

    fun setUserId(userId: String) {
        try {
            firebaseAnalytics.setUserId(userId)
            crashlytics.setUserId(userId)
            logger.debug(TAG, "User ID set for analytics: $userId")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to set user ID for analytics", e)
        }
    }

    fun setUserProperties(user: User) {
        try {
            firebaseAnalytics.setUserProperty(PARAM_USER_ROLE, user.role.name)
            firebaseAnalytics.setUserProperty(PARAM_DEPARTMENT, user.department)
            
            crashlytics.setCustomKey(PARAM_USER_ROLE, user.role.name)
            crashlytics.setCustomKey(PARAM_DEPARTMENT, user.department)
            crashlytics.setCustomKey("user_batch", user.batch ?: "unknown")
            crashlytics.setCustomKey("user_section", user.section ?: "unknown")
            crashlytics.setCustomKey("user_lab_section", user.labSection ?: "unknown")
            crashlytics.setCustomKey("user_initial", user.initial ?: "unknown")
            
            logger.debug(TAG, "User properties set: role=${user.role.name}, department=${user.department}")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to set user properties", e)
        }
    }

    fun logScreenView(screenName: String, screenClass: String? = null) {
        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                screenClass?.let { putString(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
            }
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
            
            logger.debug(TAG, "Screen view logged: $screenName")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to log screen view", e)
        }
    }

    fun logUserSignIn(method: String, success: Boolean, role: String? = null) {
        try {
            val bundle = Bundle().apply {
                putString(PARAM_METHOD, method)
                putBoolean(PARAM_SUCCESS, success)
                role?.let { putString(PARAM_USER_ROLE, it) }
            }
            firebaseAnalytics.logEvent(EVENT_USER_SIGN_IN, bundle)
            
            logger.info(TAG, "Sign in logged: method=$method, success=$success")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to log user sign in", e)
        }
    }

    fun logUserSignUp(method: String, success: Boolean, role: String? = null, department: String? = null) {
        try {
            val bundle = Bundle().apply {
                putString(PARAM_METHOD, method)
                putBoolean(PARAM_SUCCESS, success)
                role?.let { putString(PARAM_USER_ROLE, it) }
                department?.let { putString(PARAM_DEPARTMENT, it) }
            }
            firebaseAnalytics.logEvent(EVENT_USER_SIGN_UP, bundle)
            
            logger.info(TAG, "Sign up logged: method=$method, success=$success")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to log user sign up", e)
        }
    }

    fun logUserSignOut() {
        try {
            firebaseAnalytics.logEvent(EVENT_USER_SIGN_OUT, Bundle())
            logger.info(TAG, "Sign out logged")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to log user sign out", e)
        }
    }

    fun logRoutineView(day: String, itemCount: Int, department: String) {
        try {
            val bundle = Bundle().apply {
                putString(PARAM_DAY, day)
                putInt(PARAM_ITEM_COUNT, itemCount)
                putString(PARAM_DEPARTMENT, department)
            }
            firebaseAnalytics.logEvent(EVENT_ROUTINE_VIEW, bundle)
            
            logger.debug(TAG, "Routine view logged: day=$day, items=$itemCount")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to log routine view", e)
        }
    }

    fun logRoutineRefresh(success: Boolean, duration: Long, itemCount: Int? = null, department: String? = null) {
        try {
            val bundle = Bundle().apply {
                putBoolean(PARAM_SUCCESS, success)
                putLong(PARAM_DURATION, duration)
                itemCount?.let { putInt(PARAM_ITEM_COUNT, it) }
                department?.let { putString(PARAM_DEPARTMENT, it) }
            }
            firebaseAnalytics.logEvent(EVENT_ROUTINE_REFRESH, bundle)
            
            logger.info(TAG, "Routine refresh logged: success=$success, duration=${duration}ms")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to log routine refresh", e)
        }
    }

    fun logDaySelect(day: String, department: String) {
        try {
            val bundle = Bundle().apply {
                putString(PARAM_DAY, day)
                putString(PARAM_DEPARTMENT, department)
            }
            firebaseAnalytics.logEvent(EVENT_DAY_SELECT, bundle)
            
            logger.debug(TAG, "Day select logged: $day")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to log day select", e)
        }
    }

    fun logClassView(courseCode: String, day: String, department: String) {
        try {
            val bundle = Bundle().apply {
                putString(PARAM_COURSE_CODE, courseCode)
                putString(PARAM_DAY, day)
                putString(PARAM_DEPARTMENT, department)
            }
            firebaseAnalytics.logEvent(EVENT_CLASS_VIEW, bundle)
            
            logger.debug(TAG, "Class view logged: $courseCode on $day")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to log class view", e)
        }
    }

    fun logNotificationReceived(type: String, department: String? = null) {
        try {
            val bundle = Bundle().apply {
                putString(PARAM_NOTIFICATION_TYPE, type)
                department?.let { putString(PARAM_DEPARTMENT, it) }
            }
            firebaseAnalytics.logEvent(EVENT_NOTIFICATION_RECEIVED, bundle)
            
            logger.info(TAG, "Notification received logged: type=$type")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to log notification received", e)
        }
    }

    fun logSyncCompleted(type: String, success: Boolean, duration: Long, itemCount: Int? = null) {
        try {
            val bundle = Bundle().apply {
                putString(PARAM_SYNC_TYPE, type)
                putBoolean(PARAM_SUCCESS, success)
                putLong(PARAM_DURATION, duration)
                itemCount?.let { putInt(PARAM_ITEM_COUNT, it) }
            }
            firebaseAnalytics.logEvent(EVENT_SYNC_COMPLETED, bundle)
            
            logger.info(TAG, "Sync completed logged: type=$type, success=$success, duration=${duration}ms")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to log sync completed", e)
        }
    }

    fun logError(errorType: String, errorMessage: String, screenName: String? = null) {
        try {
            val bundle = Bundle().apply {
                putString(PARAM_ERROR_TYPE, errorType)
                putString("error_message", errorMessage)
                screenName?.let { putString(PARAM_SCREEN_NAME, it) }
            }
            firebaseAnalytics.logEvent(EVENT_ERROR_OCCURRED, bundle)
            
            // Also record in Crashlytics
            crashlytics.recordException(Exception("$errorType: $errorMessage"))
            
            logger.warning(TAG, "Error logged: type=$errorType, message=$errorMessage")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to log error", e)
        }
    }

    fun logOfflineAccess(screenName: String, action: String) {
        try {
            val bundle = Bundle().apply {
                putString(PARAM_SCREEN_NAME, screenName)
                putString("action", action)
            }
            firebaseAnalytics.logEvent(EVENT_OFFLINE_ACCESS, bundle)
            
            logger.info(TAG, "Offline access logged: screen=$screenName, action=$action")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to log offline access", e)
        }
    }

    fun logCustomEvent(eventName: String, parameters: Map<String, Any>) {
        try {
            val bundle = Bundle().apply {
                parameters.forEach { (key, value) ->
                    when (value) {
                        is String -> putString(key, value)
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                        is Double -> putDouble(key, value)
                        is Boolean -> putBoolean(key, value)
                        else -> putString(key, value.toString())
                    }
                }
            }
            firebaseAnalytics.logEvent(eventName, bundle)
            
            logger.debug(TAG, "Custom event logged: $eventName with ${parameters.size} parameters")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to log custom event", e)
        }
    }

    fun setCustomProperty(key: String, value: String) {
        try {
            firebaseAnalytics.setUserProperty(key, value)
            crashlytics.setCustomKey(key, value)
            
            logger.debug(TAG, "Custom property set: $key=$value")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to set custom property", e)
        }
    }

    fun recordException(throwable: Throwable, additionalData: Map<String, String> = emptyMap()) {
        try {
            // Set additional context
            additionalData.forEach { (key, value) ->
                crashlytics.setCustomKey(key, value)
            }
            
            crashlytics.recordException(throwable)
            
            logger.error(TAG, "Exception recorded in Crashlytics", throwable)
        } catch (e: Exception) {
            logger.error(TAG, "Failed to record exception in Crashlytics", e)
        }
    }

    fun setAnalyticsEnabled(enabled: Boolean) {
        try {
            firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
            logger.info(TAG, "Analytics collection enabled: $enabled")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to set analytics enabled state", e)
        }
    }

    fun clearUserData() {
        try {
            firebaseAnalytics.resetAnalyticsData()
            logger.info(TAG, "Analytics user data cleared")
        } catch (e: Exception) {
            logger.error(TAG, "Failed to clear analytics user data", e)
        }
    }
}
