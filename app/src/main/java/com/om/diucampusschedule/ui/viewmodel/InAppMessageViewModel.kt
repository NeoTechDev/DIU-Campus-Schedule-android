package com.om.diucampusschedule.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.diucampusschedule.domain.model.InAppMessage
import com.om.diucampusschedule.domain.repository.InAppMessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InAppMessageViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val inAppMessageRepository: InAppMessageRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<InAppMessage>>(emptyList())
    val messages: StateFlow<List<InAppMessage>> get() = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _dismissedMessages = MutableStateFlow<Set<String>>(emptySet())
    val dismissedMessages: StateFlow<Set<String>> get() = _dismissedMessages

    fun loadDismissedMessages() {
        viewModelScope.launch {
            try {
                val dismissedIds = inAppMessageRepository.getDismissedMessageIds()
                _dismissedMessages.value = dismissedIds
//                Log.d("InAppMessageVM", "Loaded ${dismissedIds.size} dismissed message IDs")
            } catch (e: Exception) {
                Log.e("InAppMessageVM", "Error loading dismissed messages", e)
            }
        }
    }

    fun loadMessages() {
        viewModelScope.launch {
//            Log.d("InAppMessageVM", "Loading messages with user filtering...")

            // Load dismissed messages first
            loadDismissedMessages()

            _isLoading.value = true
            try {
                val messages = inAppMessageRepository.getMessagesForUser()
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
    suspend fun loadMessagesForUser() {
        _isLoading.value = true
        try {
            val messages = inAppMessageRepository.getMessagesForUser()
//            Log.d("InAppMessageVM", "Loaded ${messages.size} user-filtered messages")
            _messages.value = messages
        } catch (e: Exception) {
            Log.e("InAppMessageVM", "Error loading user messages", e)
            _messages.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    fun dismissMessage(messageId: String) {
        viewModelScope.launch {
            try {
                inAppMessageRepository.dismissMessage(messageId)
                _dismissedMessages.value = _dismissedMessages.value + messageId
//                Log.d("InAppMessageVM", "Dismissed message: $messageId")
            } catch (e: Exception) {
                Log.e("InAppMessageVM", "Error dismissing message", e)
            }
        }
    }

    fun resetDismissedMessages() {
        viewModelScope.launch {
            try {
                inAppMessageRepository.resetDismissedMessages()
                _dismissedMessages.value = emptySet()
//                Log.d("InAppMessageVM", "Reset all dismissed messages")
            } catch (e: Exception) {
                Log.e("InAppMessageVM", "Error resetting dismissed messages", e)
            }
        }
    }

    private var lastRefreshTime = 0L
    private val minRefreshInterval = 5 * 60 * 1000L // 5 minutes minimum between refreshes

    // Smart manual refresh method - prevents too frequent updates
    fun refreshMessagesIfNeeded(forceRefresh: Boolean = false) {
        val currentTime = System.currentTimeMillis()

        // Skip if refreshed recently (unless forced)
        if (!forceRefresh && (currentTime - lastRefreshTime) < minRefreshInterval) {
//            Log.d("InAppMessageVM", "Refresh skipped - too recent (${(currentTime - lastRefreshTime) / 1000}s ago)")
            return
        }

        viewModelScope.launch {
            try {
                if (isInternetAvailable()) {
//                    Log.d("InAppMessageVM", "Smart refresh: Checking for new messages...")
                    val messages = inAppMessageRepository.getMessagesForUser()

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
    fun forceRefreshMessages() {
        refreshMessagesIfNeeded(forceRefresh = true)
    }

    private suspend fun isInternetAvailable(): Boolean {
        return try {
            com.om.diucampusschedule.ui.utils.isInternetAvailable(context)
        } catch (e: Exception) {
            false
        }
    }
}