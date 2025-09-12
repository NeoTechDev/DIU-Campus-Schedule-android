package com.om.diucampusschedule.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.om.diucampusschedule.data.model.UserDto
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val name: String,
    val profilePictureUrl: String,
    val department: String,
    val role: String, // Stored as string in Room
    val batch: String,
    val section: String,
    val labSection: String,
    val initial: String,
    val isProfileComplete: Boolean,
    val isEmailVerified: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

// Extension functions for mapping
fun UserEntity.toDomainModel(): User {
    return User(
        id = id,
        email = email,
        name = name,
        profilePictureUrl = profilePictureUrl,
        department = department,
        role = UserRole.valueOf(role),
        batch = batch,
        section = section,
        labSection = labSection,
        initial = initial,
        isProfileComplete = isProfileComplete,
        isEmailVerified = isEmailVerified,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        email = email,
        name = name,
        profilePictureUrl = profilePictureUrl,
        department = department,
        role = role.name,
        batch = batch,
        section = section,
        labSection = labSection,
        initial = initial,
        isProfileComplete = isProfileComplete,
        isEmailVerified = isEmailVerified,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun UserDto.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        email = email,
        name = name,
        profilePictureUrl = profilePictureUrl,
        department = department,
        role = role,
        batch = batch,
        section = section,
        labSection = labSection,
        initial = initial,
        isProfileComplete = isProfileComplete,
        isEmailVerified = isEmailVerified,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun UserEntity.toDto(): UserDto {
    return UserDto(
        id = id,
        email = email,
        name = name,
        profilePictureUrl = profilePictureUrl,
        department = department,
        role = role,
        batch = batch,
        section = section,
        labSection = labSection,
        initial = initial,
        isProfileComplete = isProfileComplete,
        isEmailVerified = isEmailVerified,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
