package com.om.diucampusschedule.domain.usecase.auth

import com.om.diucampusschedule.domain.repository.AuthRepository
import javax.inject.Inject

class CheckEmailVerificationUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Boolean> {
        return try {
            // First reload user to get latest verification status
            authRepository.reloadUser()
            // Then check if email is verified
            authRepository.isEmailVerified()
        } catch (e: Exception) {
            Result.failure(Exception("Failed to check email verification: ${e.message}"))
        }
    }
}
