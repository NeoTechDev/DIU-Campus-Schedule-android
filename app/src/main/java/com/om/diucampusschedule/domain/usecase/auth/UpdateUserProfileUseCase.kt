package com.om.diucampusschedule.domain.usecase.auth

import com.om.diucampusschedule.core.validation.DataValidator
import com.om.diucampusschedule.core.validation.DynamicDataValidator
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.model.UserRegistrationForm
import com.om.diucampusschedule.domain.model.UserRole
import com.om.diucampusschedule.domain.repository.AuthRepository
import com.om.diucampusschedule.domain.usecase.validation.GetValidationDataUseCase
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val getValidationDataUseCase: GetValidationDataUseCase
) {
    suspend operator fun invoke(
        userId: String,
        form: UserRegistrationForm
    ): Result<User> {
        // Validate user ID
        if (userId.isBlank()) {
            return Result.failure(Exception("User ID cannot be empty"))
        }
        
        // Get current user to validate email (we need it for comprehensive validation)
        val currentUserResult = authRepository.getCurrentUser()
        val currentUser = currentUserResult.getOrNull()
        val userEmail = currentUser?.email ?: ""
        
        // Get dynamic validation data from routine
        val validationDataResult = getValidationDataUseCase(form.department)
        val validationData = validationDataResult.getOrNull() ?: run {
            android.util.Log.w("UpdateProfile", "Could not get validation data, using static validation")
            return Result.failure(Exception("Could not load validation data. Please try again."))
        }
        
        // Comprehensive server-side validation using DynamicDataValidator
        val validationResult = DynamicDataValidator.validateUserProfile(
            name = form.name,
            email = userEmail,
            department = form.department,
            role = form.role,
            batch = if (form.role == UserRole.STUDENT) form.batch else null,
            section = if (form.role == UserRole.STUDENT) form.section else null,
            labSection = if (form.role == UserRole.STUDENT) form.labSection else null,
            initial = if (form.role == UserRole.TEACHER) form.initial else null,
            validationData = validationData
        )
        
        if (!validationResult.isValid) {
            return Result.failure(Exception("Validation failed: ${validationResult.getErrorMessage()}"))
        }
        
        // Additional profile picture URL validation if provided
        if (form.profilePictureUrl.isNotBlank()) {
            val urlValidation = DataValidator.validateUrl(form.profilePictureUrl)
            if (!urlValidation.isValid) {
                return Result.failure(Exception("Invalid profile picture URL: ${urlValidation.getErrorMessage()}"))
            }
        }
        
        // Sanitize and normalize data before saving
        val sanitizedForm = form.copy(
            name = form.name.trim(),
            department = form.department.trim(),
            batch = form.batch.trim().uppercase(),
            section = form.section.trim().uppercase(),
            labSection = form.labSection.trim().uppercase().takeIf { it.isNotBlank() } ?: "",
            initial = form.initial.trim().uppercase(),
            profilePictureUrl = form.profilePictureUrl.trim()
        )
        
        return try {
            authRepository.updateUserProfile(userId, sanitizedForm)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update profile: ${e.message}"))
        }
    }
}
