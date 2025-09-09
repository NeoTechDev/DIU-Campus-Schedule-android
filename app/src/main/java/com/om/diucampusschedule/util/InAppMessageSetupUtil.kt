package com.om.diucampusschedule.util

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.om.diucampusschedule.domain.model.InAppMessage
import com.om.diucampusschedule.domain.model.MessageButton

/**
 * Development utility for creating sample in-app messages.
 * This should only be used during development and testing.
 */
object InAppMessageSetupUtil {

    private val firestore = FirebaseFirestore.getInstance()
    private const val COLLECTION_NAME = "inAppMessages"

    /**
     * Creates sample in-app messages for testing
     * Call this function only during development
     */
    fun createSampleMessages() {
        val sampleMessages = listOf(
            // Welcome message for new users
            InAppMessage(
                id = "welcome_message_2024",
                title = "Welcome to DIU Campus Schedule!",
                message = "Get ready to manage your academic life efficiently. Check out the new features we've added just for you.",
                type = "dialog",
                isActive = true,
                targetScreen = "today",
                buttons = listOf(
                    MessageButton(text = "Get Started", action = "dismiss"),
                    MessageButton(text = "Learn More", action = "navigate:profile")
                ),
                createdAt = System.currentTimeMillis(),
                expiresAt = 0L, // Never expires
                showToNewUsers = true
            ),

            // App update notification
            InAppMessage(
                id = "app_update_v5",
                title = "New Features Available",
                message = "We've improved performance and added new customization options for your schedule.",
                type = "banner",
                isActive = true,
                targetScreen = "", // Show on all screens
                buttons = listOf(
                    MessageButton(text = "What's New", action = "navigate:profile")
                ),
                createdAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // 30 days
                showToNewUsers = false
            ),

            // Routine sync reminder
            InAppMessage(
                id = "sync_reminder_2024",
                title = "Sync Your Schedule",
                message = "Keep your class routine up to date by syncing with the campus system regularly.",
                type = "banner",
                isActive = true,
                targetScreen = "routine",
                buttons = listOf(
                    MessageButton(text = "Sync Now", action = "dismiss"),
                    MessageButton(text = "Later", action = "dismiss")
                ),
                createdAt = System.currentTimeMillis(),
                expiresAt = 0L,
                showToNewUsers = true
            ),

            // Exam preparation tip
            InAppMessage(
                id = "exam_prep_tip_2024",
                title = "Exam Season Tip",
                message = "Use the Tasks feature to break down your study schedule into manageable chunks. Good luck!",
                type = "dialog",
                isActive = true,
                targetScreen = "tasks",
                buttons = listOf(
                    MessageButton(text = "Thanks!", action = "dismiss")
                ),
                createdAt = System.currentTimeMillis(),
                expiresAt = 0L,
                showToNewUsers = true
            ),

            // Community feature promotion
            InAppMessage(
                id = "community_feature_2024",
                title = "Connect with Fellow Students",
                message = "Join the community section to share notes, discuss assignments, and help each other succeed.",
                type = "banner",
                isActive = false, // Disabled for now
                targetScreen = "today",
                buttons = listOf(
                    MessageButton(text = "Check it Out", action = "navigate:community"),
                    MessageButton(text = "Maybe Later", action = "dismiss")
                ),
                createdAt = System.currentTimeMillis(),
                expiresAt = 0L,
                showToNewUsers = true
            )
        )

        // Upload sample messages to Firestore
        sampleMessages.forEach { message ->
            firestore.collection(COLLECTION_NAME)
                .document(message.id)
                .set(message)
                .addOnSuccessListener {
                    Log.d("InAppMessageSetup", "Sample message created: ${message.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("InAppMessageSetup", "Error creating sample message: ${message.id}", e)
                }
        }
    }

    /**
     * Delete all sample messages
     */
    fun deleteSampleMessages() {
        val sampleIds = listOf(
            "welcome_message_2024",
            "app_update_v5", 
            "sync_reminder_2024",
            "exam_prep_tip_2024",
            "community_feature_2024"
        )

        sampleIds.forEach { id ->
            firestore.collection(COLLECTION_NAME)
                .document(id)
                .delete()
                .addOnSuccessListener {
                    Log.d("InAppMessageSetup", "Sample message deleted: $id")
                }
                .addOnFailureListener { e ->
                    Log.e("InAppMessageSetup", "Error deleting sample message: $id", e)
                }
        }
    }
}
