package com.om.diucampusschedule.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.diucampusschedule.domain.model.InAppMessage
import com.om.diucampusschedule.domain.repository.InAppMessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InAppMessageViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<InAppMessage>>(emptyList())
    val messages: StateFlow<List<InAppMessage>> get() = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _dismissedMessages = MutableStateFlow<Set<String>>(emptySet())
    val dismissedMessages: StateFlow<Set<String>> get() = _dismissedMessages

    companion object {
        private const val DISMISSED_MESSAGES_PREF = "dismissed_in_app_messages"
        private const val DISMISSED_MESSAGES_KEY = "dismissed_message_ids"
    }

    fun loadDismissedMessages(context: Context) {
        val sharedPref = context.getSharedPreferences(DISMISSED_MESSAGES_PREF, Context.MODE_PRIVATE)
        val dismissedIds = sharedPref.getStringSet(DISMISSED_MESSAGES_KEY, emptySet()) ?: emptySet()
        _dismissedMessages.value = dismissedIds
//        Log.d("InAppMessageVM", "Loaded ${dismissedIds.size} dismissed message IDs")
    }

    private fun saveDismissedMessages(context: Context) {
        val sharedPref = context.getSharedPreferences(DISMISSED_MESSAGES_PREF, Context.MODE_PRIVATE)
        sharedPref.edit()
            .putStringSet(DISMISSED_MESSAGES_KEY, _dismissedMessages.value)
            .apply()
//        Log.d("InAppMessageVM", "Saved ${_dismissedMessages.value.size} dismissed message IDs")
    }

    fun loadMessages(context: Context) {
        viewModelScope.launch {
//            Log.d("InAppMessageVM", "Loading messages with user filtering...")

            // Load dismissed messages first
            loadDismissedMessages(context)

            _isLoading.value = true
            try {
                val messages = InAppMessageRepository.getMessagesForUser(context)
//                Log.d("InAppMessageVM", "Loaded ${messages.size} user-filtered messages")
                _messages.value = messages
            } catch (e: Exception) {
//                Log.e("InAppMessageVM", "Error loading messages", e)
                _messages.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getMessagesForScreen(targetScreen: String): List<InAppMessage> {
        return _messages.value.filter { message ->
            !_dismissedMessages.value.contains(message.id) &&
                    (message.targetScreen == targetScreen || message.targetScreen.isEmpty())
        }
    }

    // Load messages with user-specific filtering
    suspend fun loadMessagesForUser(context: Context) {
        _isLoading.value = true
        try {
            val messages = InAppMessageRepository.getMessagesForUser(context)
//            Log.d("InAppMessageVM", "Loaded ${messages.size} user-filtered messages")
            _messages.value = messages
        } catch (e: Exception) {
            Log.e("InAppMessageVM", "Error loading user messages", e)
            _messages.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    fun dismissMessage(context: Context, messageId: String) {
        _dismissedMessages.value = _dismissedMessages.value + messageId
        saveDismissedMessages(context)
//        Log.d("InAppMessageVM", "Dismissed message: $messageId")
    }

    fun resetDismissedMessages(context: Context) {
        _dismissedMessages.value = emptySet()
        saveDismissedMessages(context)
//        Log.d("InAppMessageVM", "Reset all dismissed messages")
    }

    private var lastRefreshTime = 0L
    private val minRefreshInterval = 5 * 60 * 1000L // 5 minutes minimum between refreshes

    // Smart manual refresh method - prevents too frequent updates
    fun refreshMessagesIfNeeded(context: Context, forceRefresh: Boolean = false) {
        val currentTime = System.currentTimeMillis()

        // Skip if refreshed recently (unless forced)
        if (!forceRefresh && (currentTime - lastRefreshTime) < minRefreshInterval) {
//            Log.d("InAppMessageVM", "Refresh skipped - too recent (${(currentTime - lastRefreshTime) / 1000}s ago)")
            return
        }

        viewModelScope.launch {
            try {
                if (isInternetAvailable(context)) {
//                    Log.d("InAppMessageVM", "Smart refresh: Checking for new messages...")
                    val messages = InAppMessageRepository.getMessagesForUser(context)

                    // Only update if messages actually changed
                    if (messages != _messages.value) {
                        _messages.value = messages
//                        Log.d("InAppMessageVM", "Messages updated: ${messages.size} total (user-filtered)")
                    } else {
//                        Log.d("InAppMessageVM", "No message changes detected")
                    }

                    lastRefreshTime = currentTime
                } else {
//                    Log.d("InAppMessageVM", "Refresh skipped - no internet connection")
                }
            } catch (e: Exception) {
                Log.e("InAppMessageVM", "Smart refresh failed: ${e.message}", e)
            }
        }
    }

    // Force refresh method for pull-to-refresh or user-triggered refresh
    fun forceRefreshMessages(context: Context) {
        refreshMessagesIfNeeded(context, forceRefresh = true)
    }

    private suspend fun isInternetAvailable(context: Context): Boolean {
        return try {
            com.om.diucampusschedule.screens.components.isInternetAvailable(context)
        } catch (e: Exception) {
            false
        }
    }
}