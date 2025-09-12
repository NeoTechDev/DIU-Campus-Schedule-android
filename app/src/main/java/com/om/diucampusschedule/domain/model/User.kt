package com.om.diucampusschedule.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val profilePictureUrl: String = "",
    val department: String = "Software Engineering",
    val role: UserRole = UserRole.STUDENT,
    val batch: String = "", // for students
    val section: String = "", // for students
    val labSection: String = "", // for students
    val initial: String = "", // for teachers
    val isProfileComplete: Boolean = false,
    val isEmailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    STUDENT,
    TEACHER
}

data class AuthState(
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEmailVerificationSent: Boolean = false,
    val isPasswordResetSent: Boolean = false,
    val successMessage: String? = null
)

data class SignInRequest(
    val email: String,
    val password: String
)

data class SignUpRequest(
    val email: String,
    val password: String,
    val confirmPassword: String
)

data class UserRegistrationForm(
    val name: String = "",
    val profilePictureUrl: String = "",
    val department: String = "Software Engineering",
    val role: UserRole = UserRole.STUDENT,
    val batch: String = "",
    val section: String = "",
    val labSection: String = "",
    val initial: String = ""
)
