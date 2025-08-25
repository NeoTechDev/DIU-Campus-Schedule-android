package com.om.diucampusschedule.data.model

import com.google.firebase.firestore.DocumentSnapshot
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.model.UserRole

data class UserDto(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val profilePictureUrl: String = "",
    val department: String = "Software Engineering",
    val role: String = "STUDENT", // Stored as string in Firestore
    val batch: String = "",
    val section: String = "",
    val labSection: String = "",
    val initial: String = "",
    val isProfileComplete: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// Extension functions for mapping between Domain and Data models
fun UserDto.toDomainModel(): User {
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
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun User.toDataModel(): UserDto {
    return UserDto(
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
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// Extension function for mapping from Firestore DocumentSnapshot
fun DocumentSnapshot.toUserDto(): UserDto? {
    return try {
        UserDto(
            id = id,
            email = getString("email") ?: "",
            name = getString("name") ?: "",
            profilePictureUrl = getString("profilePictureUrl") ?: "",
            department = getString("department") ?: "Software Engineering",
            role = getString("role") ?: "STUDENT",
            batch = getString("batch") ?: "",
            section = getString("section") ?: "",
            labSection = getString("labSection") ?: "",
            initial = getString("initial") ?: "",
            isProfileComplete = getBoolean("isProfileComplete") ?: false,
            createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
            updatedAt = getLong("updatedAt") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        null
    }
}
