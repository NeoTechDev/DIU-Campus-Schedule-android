package com.om.diucampusschedule.domain.repository


import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.om.diucampusschedule.data.preferences.InAppMessagePreferences
import com.om.diucampusschedule.domain.model.InAppMessage
import com.om.diucampusschedule.domain.model.MessageButton
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppMessageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val inAppMessagePreferences: InAppMessagePreferences
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val COLLECTION_NAME = "inAppMessages"

    suspend fun getActiveMessages(): List<InAppMessage> {
        return try {
//            Log.d("InAppMessageRepo", "Fetching messages from Firestore...")
            val snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("isActive", true)
                .get()
                .await()

//            Log.d("InAppMessageRepo", "Received ${snapshot.documents.size} documents")
            snapshot.documents.mapNotNull { document ->
                try {
                    val buttonsData = document.get("buttons") as? List<Map<String, Any>> ?: emptyList()
                    val buttons = buttonsData.map { buttonMap ->
                        MessageButton(
                            text = buttonMap["text"] as? String ?: "",
                            action = buttonMap["action"] as? String ?: ""
                        )
                    }

                    val message = InAppMessage(
                        id = document.getString("id") ?: "",
                        title = document.getString("title") ?: "",
                        message = document.getString("message") ?: "",
                        type = document.getString("type") ?: "dialog",
                        isActive = document.getBoolean("isActive") ?: false,
                        targetScreen = document.getString("targetScreen") ?: "",
                        buttons = buttons,
                        createdAt = document.getLong("createdAt") ?: 0L,
                        expiresAt = document.getLong("expiresAt") ?: 0L,
                        showToNewUsers = document.getBoolean("showToNewUsers") ?: true
                    )
//                    Log.d("InAppMessageRepo", "Parsed message: ${message.id} - ${message.title}")
                    message
                } catch (e: Exception) {
                    Log.e("InAppMessageRepo", "Error parsing document: ${document.id}", e)
                    null
                }
            }.also { messages ->
//                Log.d("InAppMessageRepo", "Returning ${messages.size} valid messages")
                null
            }.sortedBy { it.id } // Sort client-side by ID
        } catch (e: Exception) {
            Log.e("InAppMessageRepo", "Error fetching messages", e)
            emptyList()
        }
    }

    suspend fun getMessagesForScreen(targetScreen: String): List<InAppMessage> {
        return try {
            val allMessages = getActiveMessages()
            allMessages.filter { it.targetScreen == targetScreen || it.targetScreen.isEmpty() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Get messages filtered for a specific user based on their install time
    suspend fun getMessagesForUser(targetScreen: String = ""): List<InAppMessage> {
        return try {
            val allMessages = getActiveMessages()
            val userInstallTime = inAppMessagePreferences.getUserInstallTime()
            val currentTime = System.currentTimeMillis()

            allMessages.filter { message ->
                // Basic active and screen filtering
                val screenMatches = targetScreen.isEmpty() ||
                        message.targetScreen == targetScreen ||
                        message.targetScreen.isEmpty()

                if (!screenMatches) return@filter false

                // Check if message has expired
                if (message.expiresAt > 0 && currentTime > message.expiresAt) {
//                    Log.d("InAppMessageRepo", "Message ${message.id} expired")
                    return@filter false
                }

                // Check if message should be shown to new users
                if (!message.showToNewUsers && message.createdAt > 0 && message.createdAt < userInstallTime) {
//                    Log.d("InAppMessageRepo", "Message ${message.id} not shown to new users")
                    return@filter false
                }

                true
            }
        } catch (e: Exception) {
            Log.e("InAppMessageRepo", "Error filtering messages for user", e)
            emptyList()
        }
    }

    /**
     * Check if a message has been dismissed by the user
     */
    suspend fun isMessageDismissed(messageId: String): Boolean {
        return inAppMessagePreferences.isMessageDismissed(messageId)
    }

    /**
     * Mark a message as dismissed
     */
    suspend fun dismissMessage(messageId: String) {
        inAppMessagePreferences.dismissMessage(messageId)
    }

    /**
     * Get all dismissed message IDs
     */
    suspend fun getDismissedMessageIds(): Set<String> {
        return inAppMessagePreferences.getDismissedMessageIds()
    }

    /**
     * Reset user install time (for testing)
     */
    suspend fun resetUserInstallTime() {
        inAppMessagePreferences.resetUserInstallTime()
    }

    /**
     * Reset all dismissed messages (for testing)
     */
    suspend fun resetDismissedMessages() {
        inAppMessagePreferences.resetDismissedMessages()
    }
}