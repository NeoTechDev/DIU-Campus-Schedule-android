package com.om.diucampusschedule.domain.usecase.auth

import com.om.diucampusschedule.core.validation.DataValidator
import com.om.diucampusschedule.domain.model.SignInRequest
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.repository.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        // Email validation
        val emailValidation = DataValidator.validateEmail(email)
        if (!emailValidation.isValid) {
            return Result.failure(Exception("Email validation failed: ${emailValidation.getErrorMessage()}"))
        }
        
        // Basic password validation (not as strict as signup since it's existing password)
        if (password.isBlank()) {
            return Result.failure(Exception("Password cannot be empty"))
        }
        
        if (password.length > 128) {
            return Result.failure(Exception("Password is too long"))
        }
        
        return try {
            authRepository.signIn(SignInRequest(email.trim().lowercase(), password))
        } catch (e: Exception) {
            Result.failure(Exception("Sign in failed: ${e.message}"))
        }
    }
}
