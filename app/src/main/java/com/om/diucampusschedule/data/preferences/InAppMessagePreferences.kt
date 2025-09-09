package com.om.diucampusschedule.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.inAppMessageDataStore: DataStore<Preferences> by preferencesDataStore(name = "in_app_message_preferences")

@Singleton
class InAppMessagePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private val USER_INSTALL_TIME = longPreferencesKey("user_install_time")
        private val DISMISSED_MESSAGE_IDS = stringSetPreferencesKey("dismissed_message_ids")
    }
    
    /**
     * Flow that emits the user's install time (when they first used the app)
     */
    val userInstallTime: Flow<Long> = context.inAppMessageDataStore.data
        .map { preferences ->
            preferences[USER_INSTALL_TIME] ?: 0L
        }
    
    /**
     * Flow that emits the set of dismissed message IDs
     */
    val dismissedMessageIds: Flow<Set<String>> = context.inAppMessageDataStore.data
        .map { preferences ->
            preferences[DISMISSED_MESSAGE_IDS] ?: emptySet()
        }
    
    /**
     * Get the user install time, creating it if it doesn't exist
     */
    suspend fun getUserInstallTime(): Long {
        val currentTime = userInstallTime.first()
        
        if (currentTime == 0L) {
            val installTime = System.currentTimeMillis()
            setUserInstallTime(installTime)
            return installTime
        }
        
        return currentTime
    }
    
    /**
     * Set the user install time
     */
    suspend fun setUserInstallTime(timestamp: Long) {
        context.inAppMessageDataStore.edit { preferences ->
            preferences[USER_INSTALL_TIME] = timestamp
        }
    }
    
    /**
     * Add a message ID to the dismissed set
     */
    suspend fun dismissMessage(messageId: String) {
        context.inAppMessageDataStore.edit { preferences ->
            val currentDismissed = preferences[DISMISSED_MESSAGE_IDS] ?: emptySet()
            preferences[DISMISSED_MESSAGE_IDS] = currentDismissed + messageId
        }
    }
    
    /**
     * Check if a message has been dismissed
     */
    suspend fun isMessageDismissed(messageId: String): Boolean {
        return dismissedMessageIds.first().contains(messageId)
    }
    
    /**
     * Get all dismissed message IDs
     */
    suspend fun getDismissedMessageIds(): Set<String> {
        return dismissedMessageIds.first()
    }
    
    /**
     * Reset user install time (for testing)
     */
    suspend fun resetUserInstallTime() {
        context.inAppMessageDataStore.edit { preferences ->
            preferences.remove(USER_INSTALL_TIME)
        }
    }
    
    /**
     * Reset all dismissed messages (for testing)
     */
    suspend fun resetDismissedMessages() {
        context.inAppMessageDataStore.edit { preferences ->
            preferences.remove(DISMISSED_MESSAGE_IDS)
        }
    }
    
    /**
     * Reset all preferences (for testing)
     */
    suspend fun resetAllPreferences() {
        context.inAppMessageDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
