package com.om.diucampusschedule.domain.usecase.auth

import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User?> {
        return authRepository.getCurrentUser()
    }
    
    fun observeAuthState(): Flow<User?> {
        return authRepository.observeAuthState()
    }
}
