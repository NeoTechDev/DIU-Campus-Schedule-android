package com.om.diucampusschedule.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.data.repository.NotificationRepository
import com.om.diucampusschedule.domain.model.NotificationType
import com.om.diucampusschedule.domain.usecase.auth.GetCurrentUserUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NotificationSaveWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationRepository: NotificationRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logger: AppLogger
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "NotificationSaveWorker"
        const val KEY_TITLE = "title"
        const val KEY_MESSAGE = "message"
        const val KEY_TYPE = "type"
        const val KEY_ACTION_ROUTE = "action_route"
        const val KEY_DEPARTMENT = "department"
        const val KEY_IMAGE_URL = "image_url"
        const val KEY_IS_FROM_ADMIN = "is_from_admin"
    }

    override suspend fun doWork(): Result {
        return try {
            val title = inputData.getString(KEY_TITLE) ?: "Notification"
            val message = inputData.getString(KEY_MESSAGE) ?: ""
            val typeString = inputData.getString(KEY_TYPE) ?: "GENERAL"
            val actionRoute = inputData.getString(KEY_ACTION_ROUTE)
            val department = inputData.getString(KEY_DEPARTMENT)
            val imageUrl = inputData.getString(KEY_IMAGE_URL)
            val isFromAdmin = inputData.getBoolean(KEY_IS_FROM_ADMIN, false)

            val type = try {
                NotificationType.valueOf(typeString)
            } catch (e: IllegalArgumentException) {
                NotificationType.GENERAL
            }

            val currentUser = getCurrentUserUseCase()
            if (currentUser.isSuccess && currentUser.getOrNull() != null) {
                val user = currentUser.getOrThrow()!!

                val result = notificationRepository.insertNotificationFromFCM(
                    title = title,
                    message = message,
                    type = type,
                    userId = user.id,
                    actionRoute = actionRoute,
                    department = department,
                    imageUrl = imageUrl,
                    isFromAdmin = isFromAdmin
                )

                if (result.isSuccess) {
                    logger.info(TAG, "Background notification saved successfully for user: ${user.id}")
                    Result.success()
                } else {
                    logger.error(TAG, "Failed to save background notification", result.exceptionOrNull())
                    Result.failure()
                }
            } else {
                logger.error(TAG, "User not authenticated - cannot save background notification")
                Result.failure()
            }
        } catch (e: Exception) {
            logger.error(TAG, "Failed to save background notification", e)
            Result.failure()
        }
    }
}