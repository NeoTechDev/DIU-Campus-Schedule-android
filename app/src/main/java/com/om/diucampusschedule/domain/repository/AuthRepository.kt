package com.om.diucampusschedule.domain.repository

import com.om.diucampusschedule.domain.model.SignInRequest
import com.om.diucampusschedule.domain.model.SignUpRequest
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.model.UserRegistrationForm
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signIn(request: SignInRequest): Result<User>
    suspend fun signUp(request: SignUpRequest): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): Result<User?>
    fun observeAuthState(): Flow<User?>
    suspend fun updateUserProfile(userId: String, form: UserRegistrationForm): Result<User>
    suspend fun isUserProfileComplete(userId: String): Result<Boolean>
    suspend fun resetPassword(email: String): Result<Unit>
}
