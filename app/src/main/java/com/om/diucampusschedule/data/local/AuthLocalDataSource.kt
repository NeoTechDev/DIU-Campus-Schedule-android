package com.om.diucampusschedule.data.local

import com.om.diucampusschedule.data.local.entities.UserEntity
import com.om.diucampusschedule.data.local.entities.toDomainModel
import com.om.diucampusschedule.data.local.entities.toDto
import com.om.diucampusschedule.data.local.entities.toEntity
import com.om.diucampusschedule.data.model.UserDto
import com.om.diucampusschedule.data.model.toDataModel
import com.om.diucampusschedule.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthLocalDataSource @Inject constructor(
    private val database: AppDatabase
) {
    private val userDao = database.userDao()

    suspend fun saveUser(userDto: UserDto) {
        userDao.insertUser(userDto.toEntity())
    }

    suspend fun saveUserFromDomain(user: User) {
        // Using toDataModel() extension function
        val userDto = user.toDataModel()
        userDao.insertUser(userDto.toEntity())
    }

    suspend fun getUser(userId: String): UserEntity? {
        return userDao.getUserById(userId)
    }

    suspend fun getUserAsDto(userId: String): UserDto? {
        // Using toDto() extension function  
        return userDao.getUserById(userId)?.toDto()
    }

    suspend fun getUserAsDomain(userId: String): User? {
        // Using toDomainModel() extension function
        return userDao.getUserById(userId)?.toDomainModel()
    }

    fun observeUser(userId: String): Flow<UserEntity?> {
        return userDao.observeUserById(userId)
    }

    fun observeUserAsDto(userId: String): Flow<UserDto?> {
        // Using observeUser() and toDto() extension function
        return userDao.observeUserById(userId).map { entity ->
            entity?.toDto()
        }
    }

    fun observeUserAsDomain(userId: String): Flow<User?> {
        // Using observeUser() and toDomainModel() extension function
        return userDao.observeUserById(userId).map { entity ->
            entity?.toDomainModel()
        }
    }

    suspend fun updateUser(userDto: UserDto) {
        userDao.updateUser(userDto.toEntity())
    }

    suspend fun updateUserFromDomain(user: User) {
        // Using toDataModel() extension function
        val userDto = user.toDataModel()
        userDao.updateUser(userDto.toEntity())
    }

    suspend fun deleteUser(userId: String) {
        userDao.deleteUser(userId)
    }

    suspend fun clearAllUsers() {
        userDao.clearAllUsers()
    }
}
