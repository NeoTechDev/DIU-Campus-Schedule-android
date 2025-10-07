package com.om.diucampusschedule.domain.usecase.notification

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.messaging.FirebaseMessaging
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.domain.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateFCMTokenUseCase @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging,
    private val logger: AppLogger
) {
    companion object {
        private const val TAG = "UpdateFCMToken"
        private const val FCM_TOKENS_COLLECTION = "fcm_tokens"
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 2000L
    }

    suspend operator fun invoke(user: User): Result<String> {
        for (attempt in 1..MAX_RETRIES) {
            try {
                logger.debug(TAG, "Getting FCM token for user: ${user.id}, attempt: $attempt")
                
                // Get current FCM token
                val token = firebaseMessaging.token.await()
                
                logger.debug(TAG, "FCM token retrieved: ${token.take(20)}...")
                
                // Store token in Firestore
                val tokenData = mapOf(
                    "token" to token,
                    "userId" to user.id,
                    "department" to user.department,
                    "batch" to user.batch,
                    "section" to user.section,
                    "role" to user.role.name,
                    "enabled" to true,
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "deviceInfo" to mapOf(
                        "platform" to "android",
                        "sdkVersion" to android.os.Build.VERSION.SDK_INT,
                        "deviceModel" to android.os.Build.MODEL,
                        "deviceManufacturer" to android.os.Build.MANUFACTURER
                    )
                )
                
                // Use user ID as document ID to ensure one token per user
                firestore.collection(FCM_TOKENS_COLLECTION)
                    .document(user.id)
                    .set(tokenData)
                    .await()
                
                logger.info(TAG, "FCM token successfully stored for user: ${user.id}")
                return Result.success(token)
                
            } catch (e: Exception) {
                val isRetryable = e is IOException && e.message?.contains("SERVICE_NOT_AVAILABLE") == true
                if (isRetryable && attempt < MAX_RETRIES) {
                    logger.warning(TAG, "Failed to get FCM token (SERVICE_NOT_AVAILABLE), retrying in ${RETRY_DELAY_MS}ms...")
                    delay(RETRY_DELAY_MS)
                } else {
                    logger.error(TAG, "Failed to update FCM token for user: ${user.id} after $attempt attempts", e)
                    return Result.failure(e)
                }
            }
        }
        
        return Result.failure(IllegalStateException("Failed to update FCM token after $MAX_RETRIES attempts"))
    }
    
    suspend fun removeToken(userId: String): Result<Unit> {
        return try {
            logger.debug(TAG, "Removing FCM token for user: $userId")
            
            firestore.collection(FCM_TOKENS_COLLECTION)
                .document(userId)
                .delete()
                .await()
            
            logger.info(TAG, "FCM token removed for user: $userId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            logger.error(TAG, "Failed to remove FCM token for user: $userId", e)
            Result.failure(e)
        }
    }
}
