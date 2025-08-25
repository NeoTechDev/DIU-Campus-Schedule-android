package com.om.diucampusschedule.domain.usecase.auth

import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.model.UserRegistrationForm
import com.om.diucampusschedule.domain.model.UserRole
import com.om.diucampusschedule.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        userId: String,
        form: UserRegistrationForm
    ): Result<User> {
        // Validate form data
        if (form.name.isBlank()) {
            return Result.failure(Exception("Name cannot be empty"))
        }
        
        if (form.role == UserRole.STUDENT) {
            if (form.batch.isBlank()) {
                return Result.failure(Exception("Batch is required for students"))
            }
            if (form.section.isBlank()) {
                return Result.failure(Exception("Section is required for students"))
            }
            if (form.labSection.isBlank()) {
                return Result.failure(Exception("Lab section is required for students"))
            }
        } else if (form.role == UserRole.TEACHER) {
            if (form.initial.isBlank()) {
                return Result.failure(Exception("Initial is required for teachers"))
            }
        }
        
        return authRepository.updateUserProfile(userId, form)
    }
}
