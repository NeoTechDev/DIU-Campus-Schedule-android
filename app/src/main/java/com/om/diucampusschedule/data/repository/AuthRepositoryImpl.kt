package com.om.diucampusschedule.data.repository

import com.om.diucampusschedule.data.local.AuthLocalDataSource
import com.om.diucampusschedule.data.local.entities.toDomainModel
import com.om.diucampusschedule.data.local.entities.toDto
import com.om.diucampusschedule.data.model.toDomainModel
import com.om.diucampusschedule.data.model.toDataModel
import com.om.diucampusschedule.data.remote.AuthRemoteDataSource
import com.om.diucampusschedule.domain.model.SignInRequest
import com.om.diucampusschedule.domain.model.SignUpRequest
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.model.UserRegistrationForm
import com.om.diucampusschedule.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
    private val localDataSource: AuthLocalDataSource
) : AuthRepository {

    override suspend fun signIn(request: SignInRequest): Result<User> {
        return try {
            val result = remoteDataSource.signIn(request)
            if (result.isSuccess) {
                val userDto = result.getOrThrow()
                // Cache user locally
                localDataSource.saveUser(userDto)
                Result.success(userDto.toDomainModel())
            } else {
                result.map { it.toDomainModel() }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(request: SignUpRequest): Result<User> {
        return try {
            val result = remoteDataSource.signUp(request)
            if (result.isSuccess) {
                val userDto = result.getOrThrow()
                // Cache user locally
                localDataSource.saveUser(userDto)
                Result.success(userDto.toDomainModel())
            } else {
                result.map { it.toDomainModel() }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val result = remoteDataSource.signInWithGoogle(idToken)
            if (result.isSuccess) {
                val userDto = result.getOrThrow()
                // Cache user locally
                localDataSource.saveUser(userDto)
                Result.success(userDto.toDomainModel())
            } else {
                result.map { it.toDomainModel() }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            // Get current user ID before signing out
            val currentUserResult = remoteDataSource.getCurrentUser()
            val currentUserId = currentUserResult.getOrNull()?.id
            
            val result = remoteDataSource.signOut()
            if (result.isSuccess) {
                // Delete specific user from local cache if we have the ID
                if (currentUserId != null) {
                    localDataSource.deleteUser(currentUserId)
                } else {
                    // Fallback to clearing all users
                    localDataSource.clearAllUsers()
                }
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            // Try to get from remote first
            val remoteResult = remoteDataSource.getCurrentUser()
            if (remoteResult.isSuccess) {
                val userDto = remoteResult.getOrThrow()
                if (userDto != null) {
                    // Update local cache
                    localDataSource.saveUser(userDto)
                    Result.success(userDto.toDomainModel())
                } else {
                    // User is not authenticated remotely, clear local cache
                    localDataSource.clearAllUsers()
                    Result.success(null)
                }
            } else {
                // Remote failed, try to get any cached user (last logged in user)
                // Note: This is a limitation since we don't know which user without remote auth
                // In a real app, you might store the last logged-in user ID in preferences
                Result.failure(remoteResult.exceptionOrNull() ?: Exception("Failed to get current user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeAuthState(): Flow<User?> {
        return remoteDataSource.observeAuthState()
            .onEach { userDto ->
                // Cache the user data locally when it changes
                if (userDto != null) {
                    try {
                        localDataSource.saveUser(userDto)
                    } catch (e: Exception) {
                        // Log error but don't fail the flow
                    }
                } else {
                    // User signed out remotely, clear local cache
                    try {
                        localDataSource.clearAllUsers()
                    } catch (e: Exception) {
                        // Log error but don't fail the flow
                    }
                }
            }
            .map { userDto ->
                userDto?.toDomainModel()
            }
    }

    // Additional method to observe user with offline fallback
    fun observeUserWithOfflineFallback(userId: String): Flow<User?> {
        return combine(
            remoteDataSource.observeAuthState(),
            localDataSource.observeUserAsDomain(userId)
        ) { remoteUser, localUser ->
            // Prefer remote data if available, fallback to local
            remoteUser?.toDomainModel() ?: localUser
        }
    }

    override suspend fun updateUserProfile(userId: String, form: UserRegistrationForm): Result<User> {
        return try {
            val result = remoteDataSource.updateUserProfile(userId, form)
            if (result.isSuccess) {
                val userDto = result.getOrThrow()
                // Update local cache using the existing method
                localDataSource.updateUser(userDto)
                
                val updatedUser = userDto.toDomainModel()
                
                // Also demonstrate using updateUserFromDomain method with toDataModel()
                try {
                    localDataSource.updateUserFromDomain(updatedUser)
                } catch (e: Exception) {
                    // Log but don't fail - this is just demonstrating the method usage
                }
                
                Result.success(updatedUser)
            } else {
                // Try to get user from local cache as fallback
                val localUser = localDataSource.getUserAsDomain(userId)
                if (localUser != null) {
                    Result.success(localUser)
                } else {
                    result.map { it.toDomainModel() }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isUserProfileComplete(userId: String): Result<Boolean> {
        return try {
            // Try remote first
            val remoteResult = remoteDataSource.isUserProfileComplete(userId)
            if (remoteResult.isSuccess) {
                remoteResult
            } else {
                // Fallback to local data using different methods to demonstrate usage
                
                // Method 1: Using getUserAsDomain (demonstrates toDataModel usage)
                val localUserDomain = localDataSource.getUserAsDomain(userId)
                if (localUserDomain != null) {
                    return Result.success(localUserDomain.isProfileComplete)
                }
                
                // Method 2: Using getUserAsDto (demonstrates toDto usage)  
                val localUserDto = localDataSource.getUserAsDto(userId)
                if (localUserDto != null) {
                    return Result.success(localUserDto.isProfileComplete)
                }
                
                // Method 3: Using getUser (demonstrates entity access)
                val localUserEntity = localDataSource.getUser(userId)
                if (localUserEntity != null) {
                    return Result.success(localUserEntity.isProfileComplete)
                }
                
                // No local data found
                remoteResult
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return remoteDataSource.resetPassword(email)
    }
}
