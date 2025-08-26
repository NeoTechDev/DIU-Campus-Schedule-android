package com.om.diucampusschedule.core.error

import androidx.annotation.StringRes
import com.om.diucampusschedule.R

/**
 * Sealed class representing different types of application errors
 */
sealed class AppError(
    override val message: String,
    @StringRes val messageRes: Int,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    // Network related errors
    data class NetworkError(
        override val message: String = "Network connection error",
        override val cause: Throwable? = null
    ) : AppError(message, R.string.error_network, cause)

    data class TimeoutError(
        override val message: String = "Request timeout",
        override val cause: Throwable? = null
    ) : AppError(message, R.string.error_timeout, cause)

    // Authentication errors
    data class AuthenticationError(
        override val message: String = "Authentication failed",
        override val cause: Throwable? = null
    ) : AppError(message, R.string.error_authentication, cause)

    data class UnauthorizedError(
        override val message: String = "Unauthorized access",
        override val cause: Throwable? = null
    ) : AppError(message, R.string.error_unauthorized, cause)

    // Data errors
    data class DataNotFoundError(
        override val message: String = "Data not found",
        override val cause: Throwable? = null
    ) : AppError(message, R.string.error_data_not_found, cause)

    data class DataParsingError(
        override val message: String = "Failed to parse data",
        override val cause: Throwable? = null
    ) : AppError(message, R.string.error_data_parsing, cause)

    data class DatabaseError(
        override val message: String = "Database operation failed",
        override val cause: Throwable? = null
    ) : AppError(message, R.string.error_database, cause)

    // Sync errors
    data class SyncError(
        override val message: String = "Synchronization failed",
        override val cause: Throwable? = null
    ) : AppError(message, R.string.error_sync, cause)

    data class OfflineError(
        override val message: String = "Operation requires internet connection",
        override val cause: Throwable? = null
    ) : AppError(message, R.string.error_offline, cause)

    // Validation errors
    data class ValidationError(
        override val message: String = "Validation failed",
        override val cause: Throwable? = null
    ) : AppError(message, R.string.error_validation, cause)

    // Generic errors
    data class UnknownError(
        override val message: String = "An unknown error occurred",
        override val cause: Throwable? = null
    ) : AppError(message, R.string.error_unknown, cause)

    data class ServerError(
        override val message: String = "Server error",
        override val cause: Throwable? = null
    ) : AppError(message, R.string.error_server, cause)

    companion object {
        /**
         * Convert a generic throwable to an AppError
         */
        fun fromThrowable(throwable: Throwable): AppError {
            return when (throwable) {
                is AppError -> throwable
                is java.net.UnknownHostException -> NetworkError(cause = throwable)
                is java.net.SocketTimeoutException -> TimeoutError(cause = throwable)
                is java.net.ConnectException -> NetworkError(cause = throwable)
                is com.google.firebase.auth.FirebaseAuthException -> AuthenticationError(
                    message = throwable.message ?: "Authentication error",
                    cause = throwable
                )
                is com.google.firebase.firestore.FirebaseFirestoreException -> {
                    when (throwable.code) {
                        com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED -> 
                            UnauthorizedError(cause = throwable)
                        com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND -> 
                            DataNotFoundError(cause = throwable)
                        com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE -> 
                            NetworkError(cause = throwable)
                        else -> ServerError(
                            message = throwable.message ?: "Server error",
                            cause = throwable
                        )
                    }
                }
                is android.database.sqlite.SQLiteException -> DatabaseError(cause = throwable)
                else -> UnknownError(
                    message = throwable.message ?: "Unknown error",
                    cause = throwable
                )
            }
        }
    }
}
