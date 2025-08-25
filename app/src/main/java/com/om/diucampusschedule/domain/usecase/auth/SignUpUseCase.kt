package com.om.diucampusschedule.domain.usecase.auth

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
        if (email.isBlank()) {
            return Result.failure(Exception("Email cannot be empty"))
        }
        
        if (password.isBlank()) {
            return Result.failure(Exception("Password cannot be empty"))
        }
        
        if (confirmPassword.isBlank()) {
            return Result.failure(Exception("Please confirm your password"))
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Please enter a valid email address"))
        }
        
        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters"))
        }
        
        if (password != confirmPassword) {
            return Result.failure(Exception("Passwords do not match"))
        }
        
        return authRepository.signUp(
            SignUpRequest(
                email = email.trim(),
                password = password,
                confirmPassword = confirmPassword
            )
        )
    }
}
