package com.om.diucampusschedule.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_preferences")

@Singleton
class NotificationPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private val CLASS_REMINDERS_ENABLED = booleanPreferencesKey("class_reminders_enabled")
        private val NOTIFICATION_PERMISSION_REQUESTED = booleanPreferencesKey("notification_permission_requested")
    }
    
    /**
     * Flow that emits whether class reminders are enabled
     */
    val isClassRemindersEnabled: Flow<Boolean> = context.notificationDataStore.data
        .map { preferences ->
            preferences[CLASS_REMINDERS_ENABLED] ?: true // Default enabled
        }
    
    /**
     * Flow that emits whether notification permission has been requested before
     */
    val hasNotificationPermissionBeenRequested: Flow<Boolean> = context.notificationDataStore.data
        .map { preferences ->
            preferences[NOTIFICATION_PERMISSION_REQUESTED] ?: false
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
     * Mark that notification permission has been requested
     */
    suspend fun markNotificationPermissionRequested() {
        context.notificationDataStore.edit { preferences ->
            preferences[NOTIFICATION_PERMISSION_REQUESTED] = true
        }
    }
}
