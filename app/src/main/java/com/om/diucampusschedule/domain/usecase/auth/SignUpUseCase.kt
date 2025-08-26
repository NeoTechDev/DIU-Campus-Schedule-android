package com.om.diucampusschedule.domain.usecase.auth

import com.om.diucampusschedule.core.validation.DataValidator
import com.om.diucampusschedule.domain.model.SignUpRequest
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String, 
        password: String, 
        confirmPassword: String
    ): Result<User> {
        // Comprehensive email validation
        val emailValidation = DataValidator.validateEmail(email)
        if (!emailValidation.isValid) {
            return Result.failure(Exception("Email validation failed: ${emailValidation.getErrorMessage()}"))
        }
        
        // Comprehensive password validation
        val passwordValidation = DataValidator.validatePassword(password)
        if (!passwordValidation.isValid) {
            return Result.failure(Exception("Password validation failed: ${passwordValidation.getErrorMessage()}"))
        }
        
        // Confirm password validation
        if (confirmPassword.isBlank()) {
            return Result.failure(Exception("Please confirm your password"))
        }
        
        if (password != confirmPassword) {
            return Result.failure(Exception("Passwords do not match"))
        }
        
        // Additional security checks
        if (email.trim().lowercase() == password.lowercase()) {
            return Result.failure(Exception("Password cannot be the same as email"))
        }
        
        val commonPasswords = setOf(
            "password", "123456", "password123", "admin", "qwerty", 
            "letmein", "welcome", "monkey", "1234567890"
        )
        if (password.lowercase() in commonPasswords) {
            return Result.failure(Exception("Password is too common. Please choose a stronger password"))
        }
        
        return try {
            authRepository.signUp(
                SignUpRequest(
                    email = email.trim().lowercase(),
                    password = password,
                    confirmPassword = confirmPassword
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Sign up failed: ${e.message}"))
        }
    }
}
