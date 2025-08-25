package com.om.diucampusschedule.domain.usecase.auth

import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.repository.AuthRepository
import javax.inject.Inject

class GoogleSignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<User> {
        if (idToken.isBlank()) {
            return Result.failure(Exception("Google sign-in failed. Please try again."))
        }
        
        return authRepository.signInWithGoogle(idToken)
    }
}
