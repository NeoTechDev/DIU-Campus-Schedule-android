package com.om.diucampusschedule.domain.usecase.auth

import com.om.diucampusschedule.domain.model.SignInRequest
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.repository.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank()) {
            return Result.failure(Exception("Email cannot be empty"))
        }
        
        if (password.isBlank()) {
            return Result.failure(Exception("Password cannot be empty"))
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Please enter a valid email address"))
        }
        
        return authRepository.signIn(SignInRequest(email.trim(), password))
    }
}
