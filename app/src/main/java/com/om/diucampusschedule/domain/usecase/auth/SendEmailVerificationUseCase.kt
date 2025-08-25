package com.om.diucampusschedule.domain.usecase.auth

import com.om.diucampusschedule.domain.repository.AuthRepository
import javax.inject.Inject

class SendEmailVerificationUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            authRepository.sendEmailVerification()
        } catch (e: Exception) {
            Result.failure(Exception("Failed to send verification email: ${e.message}"))
        }
    }
}
