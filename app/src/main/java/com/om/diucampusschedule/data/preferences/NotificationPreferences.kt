package com.om.diucampusschedule.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_preferences")

@Singleton
class NotificationPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private val CLASS_REMINDERS_ENABLED = booleanPreferencesKey("class_reminders_enabled")
        private val TASK_REMINDERS_ENABLED = booleanPreferencesKey("task_reminders_enabled")
        private val NOTIFICATION_PERMISSION_REQUESTED = booleanPreferencesKey("notification_permission_requested")
        private val WELCOME_DIALOG_SHOWN = booleanPreferencesKey("welcome_dialog_shown")
    }
    
    /**
     * Flow that emits whether class reminders are enabled
     */
    val isClassRemindersEnabled: Flow<Boolean> = context.notificationDataStore.data
        .map { preferences ->
            preferences[CLASS_REMINDERS_ENABLED] ?: true // Default enabled
        }
    
    /**
     * Flow that emits whether task reminders are enabled
     */
    val isTaskRemindersEnabled: Flow<Boolean> = context.notificationDataStore.data
        .map { preferences ->
            preferences[TASK_REMINDERS_ENABLED] ?: true // Default enabled
        }
    
    /**
     * Flow that emits whether notification permission has been requested before
     */
    val hasNotificationPermissionBeenRequested: Flow<Boolean> = context.notificationDataStore.data
        .map { preferences ->
            preferences[NOTIFICATION_PERMISSION_REQUESTED] ?: false
        }
    
    /**
     * Flow that emits whether welcome dialog has been shown before
     */
    val hasWelcomeDialogBeenShown: Flow<Boolean> = context.notificationDataStore.data
        .map { preferences ->
            preferences[WELCOME_DIALOG_SHOWN] ?: false
        }
    
    /**
     * Enable or disable class reminders
     */
    suspend fun setClassRemindersEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { preferences ->
            preferences[CLASS_REMINDERS_ENABLED] = enabled
        }
    }
    
    /**
     * Enable or disable task reminders
     */
    suspend fun setTaskRemindersEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { preferences ->
            preferences[TASK_REMINDERS_ENABLED] = enabled
        }
    }
    
    /**
     * Mark that notification permission has been requested
     */
    suspend fun markNotificationPermissionRequested() {
        context.notificationDataStore.edit { preferences ->
            preferences[NOTIFICATION_PERMISSION_REQUESTED] = true
        }
    }
    
    /**
     * Mark that welcome dialog has been shown
     */
    suspend fun markWelcomeDialogShown() {
        context.notificationDataStore.edit { preferences ->
            preferences[WELCOME_DIALOG_SHOWN] = true
        }
    }
    
    // Synchronous methods for use in cases where suspend functions can't be used
    
    /**
     * Get class reminders enabled state synchronously
     */
    fun isClassRemindersEnabledSync(): Boolean {
        return runBlocking {
            isClassRemindersEnabled.first()
        }
    }
    
    /**
     * Get task reminders enabled state synchronously
     */
    fun isTaskRemindersEnabledSync(): Boolean {
        return runBlocking {
            isTaskRemindersEnabled.first()
        }
    }
    
    /**
     * Get notification permission requested state synchronously
     */
    fun hasNotificationPermissionBeenRequested(): Boolean {
        return runBlocking {
            hasNotificationPermissionBeenRequested.first()
        }
    }
}
