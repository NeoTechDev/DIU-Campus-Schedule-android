package com.om.diucampusschedule.core.validation

import android.util.Patterns
import com.om.diucampusschedule.domain.model.UserRole

/**
 * Comprehensive data validation utility for ensuring data integrity
 * across the application. Provides server-side validation beyond UI validation.
 */
object DataValidator {

    /**
     * Validation result with error details
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList()
    ) {
        fun getErrorMessage(): String = errors.joinToString("; ")
    }

    /**
     * Email validation
     */
    fun validateEmail(email: String?): ValidationResult {
        val errors = mutableListOf<String>()
        
        when {
            email.isNullOrBlank() -> errors.add("Email cannot be empty")
            email.length > 254 -> errors.add("Email is too long (max 254 characters)")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> errors.add("Invalid email format")
            email.contains("..") -> errors.add("Email cannot contain consecutive dots")
            email.startsWith(".") || email.endsWith(".") -> errors.add("Email cannot start or end with a dot")
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Password validation
     */
    fun validatePassword(password: String?): ValidationResult {
        val errors = mutableListOf<String>()
        
        when {
            password.isNullOrBlank() -> errors.add("Password cannot be empty")
            password.length < 6 -> errors.add("Password must be at least 6 characters long")
            password.length > 128 -> errors.add("Password is too long (max 128 characters)")
            !password.any { it.isDigit() } -> errors.add("Password must contain at least one number")
            !password.any { it.isLetter() } -> errors.add("Password must contain at least one letter")
            password.contains(" ") -> errors.add("Password cannot contain spaces")
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Name validation (for user profiles)
     */
    fun validateName(name: String?): ValidationResult {
        val errors = mutableListOf<String>()
        
        when {
            name.isNullOrBlank() -> errors.add("Name cannot be empty")
            name.trim().length < 2 -> errors.add("Name must be at least 2 characters")
            name.trim().length > 50 -> errors.add("Name must be less than 50 characters")
            !name.trim().matches(Regex("^[a-zA-Z\\s.'-]+$")) -> {
                errors.add("Name can only contain letters, spaces, dots, apostrophes, and hyphens")
            }
            name.trim().split("\\s+".toRegex()).size > 10 -> errors.add("Name has too many words")
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Batch validation (for students)
     */
    fun validateBatch(batch: String?): ValidationResult {
        val errors = mutableListOf<String>()
        
        when {
            batch.isNullOrBlank() -> errors.add("Batch cannot be empty")
            !batch.trim().matches(Regex("^\\d+$")) -> errors.add("Batch must contain only numbers")
            else -> {
                val batchNumber = batch.trim().toIntOrNull()
                when {
                    batchNumber == null -> errors.add("Invalid batch number")
                    batchNumber < 1 -> errors.add("Batch must be a positive number")
                    batchNumber > 200 -> errors.add("Batch number is too high (max 200)")
                }
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Section validation (for students)
     */
    fun validateSection(section: String?): ValidationResult {
        val errors = mutableListOf<String>()
        
        when {
            section.isNullOrBlank() -> errors.add("Section cannot be empty")
            section.trim().length != 1 -> errors.add("Section must be a single character")
            !section.trim().matches(Regex("^[A-Za-z]$")) -> errors.add("Section must be a letter (A-Z)")
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Lab section validation (for students) - Optional field
     */
    fun validateLabSection(labSection: String?): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (!labSection.isNullOrBlank()) {
            when {
                labSection.trim().length > 10 -> errors.add("Lab section is too long (max 10 characters)")
                !labSection.trim().matches(Regex("^[A-Za-z]\\d*$")) -> {
                    errors.add("Lab section must start with a letter followed by numbers (e.g., A1, B2)")
                }
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Teacher initial validation
     */
    fun validateTeacherInitial(initial: String?): ValidationResult {
        val errors = mutableListOf<String>()
        
        when {
            initial.isNullOrBlank() -> errors.add("Teacher initial cannot be empty")
            initial.trim().length < 2 -> errors.add("Initial must be at least 2 characters")
            initial.trim().length > 6 -> errors.add("Initial must be less than 6 characters")
            !initial.trim().matches(Regex("^[A-Za-z]+$")) -> errors.add("Initial can only contain letters")
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Department validation
     */
    fun validateDepartment(department: String?): ValidationResult {
        val errors = mutableListOf<String>()
        val validDepartments = setOf(
            "Software Engineering",
            "Computer Science",
            "Electrical Engineering", 
            "Mechanical Engineering",
            "Civil Engineering",
            "Business Administration",
            "English",
            "Mathematics"
        )
        
        when {
            department.isNullOrBlank() -> errors.add("Department cannot be empty")
            department.trim().length > 100 -> errors.add("Department name is too long")
            department.trim() !in validDepartments -> {
                errors.add("Invalid department. Allowed: ${validDepartments.joinToString(", ")}")
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Comprehensive user profile validation
     */
    fun validateUserProfile(
        name: String?,
        email: String?,
        department: String?,
        role: UserRole?,
        batch: String? = null,
        section: String? = null,
        labSection: String? = null,
        initial: String? = null
    ): ValidationResult {
        val allErrors = mutableListOf<String>()
        
        // Basic field validation
        allErrors.addAll(validateName(name).errors)
        allErrors.addAll(validateEmail(email).errors)
        allErrors.addAll(validateDepartment(department).errors)
        
        // Role validation
        if (role == null) {
            allErrors.add("Role must be specified")
        }
        
        // Role-specific validation
        when (role) {
            UserRole.STUDENT -> {
                allErrors.addAll(validateBatch(batch).errors)
                allErrors.addAll(validateSection(section).errors)
                allErrors.addAll(validateLabSection(labSection).errors)
                
                // Ensure teacher-specific fields are not provided
                if (!initial.isNullOrBlank()) {
                    allErrors.add("Students should not have teacher initial")
                }
            }
            UserRole.TEACHER -> {
                allErrors.addAll(validateTeacherInitial(initial).errors)
                
                // Ensure student-specific fields are not provided
                if (!batch.isNullOrBlank()) {
                    allErrors.add("Teachers should not have student batch")
                }
                if (!section.isNullOrBlank()) {
                    allErrors.add("Teachers should not have student section")
                }
                if (!labSection.isNullOrBlank()) {
                    allErrors.add("Teachers should not have lab section")
                }
            }
            null -> {
                // Role validation error already added above
            }
        }
        
        return ValidationResult(allErrors.isEmpty(), allErrors)
    }

    /**
     * Course code validation (for routine items)
     */
    fun validateCourseCode(courseCode: String?): ValidationResult {
        val errors = mutableListOf<String>()
        
        when {
            courseCode.isNullOrBlank() -> errors.add("Course code cannot be empty")
            courseCode.trim().length < 3 -> errors.add("Course code must be at least 3 characters")
            courseCode.trim().length > 10 -> errors.add("Course code must be less than 10 characters")
            !courseCode.trim().matches(Regex("^[A-Za-z]{2,4}\\d{3,4}$")) -> {
                errors.add("Course code must be in format: 2-4 letters followed by 3-4 numbers (e.g., SE214, CSE101)")
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Room validation (for routine items)
     */
    fun validateRoom(room: String?): ValidationResult {
        val errors = mutableListOf<String>()
        
        when {
            room.isNullOrBlank() -> errors.add("Room cannot be empty")
            room.trim().length > 20 -> errors.add("Room name is too long (max 20 characters)")
            !room.trim().matches(Regex("^[A-Za-z0-9\\-_\\s]+$")) -> {
                errors.add("Room can only contain letters, numbers, hyphens, underscores, and spaces")
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Time format validation (for routine items)
     */
    fun validateTimeFormat(time: String?): ValidationResult {
        val errors = mutableListOf<String>()
        
        when {
            time.isNullOrBlank() -> errors.add("Time cannot be empty")
            !time.trim().matches(Regex("^\\d{1,2}:\\d{2}\\s?(AM|PM)\\s?-\\s?\\d{1,2}:\\d{2}\\s?(AM|PM)$")) -> {
                errors.add("Time must be in format: HH:MM AM/PM - HH:MM AM/PM (e.g., 08:30 AM - 10:00 AM)")
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Day validation (for routine items)
     */
    fun validateDay(day: String?): ValidationResult {
        val errors = mutableListOf<String>()
        val validDays = setOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        
        when {
            day.isNullOrBlank() -> errors.add("Day cannot be empty")
            day.trim() !in validDays -> {
                errors.add("Invalid day. Must be one of: ${validDays.joinToString(", ")}")
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * URL validation (for profile pictures)
     * Accepts both HTTP/HTTPS URLs and local content URIs
     */
    fun validateUrl(url: String?): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (!url.isNullOrBlank()) {
            when {
                url.length > 2048 -> errors.add("URL is too long (max 2048 characters)")
                // Accept HTTP/HTTPS URLs and content:// URIs for local images
                !url.matches(Regex("^(https?://.*|content://.*|file://.*)")) -> 
                    errors.add("URL must be a valid web URL (http/https) or local content URI")
                url.contains(" ") -> errors.add("URL cannot contain spaces")
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Generic string length validation
     */
    fun validateStringLength(
        value: String?,
        fieldName: String,
        minLength: Int = 0,
        maxLength: Int = Int.MAX_VALUE,
        required: Boolean = true
    ): ValidationResult {
        val errors = mutableListOf<String>()
        
        when {
            value.isNullOrBlank() && required -> errors.add("$fieldName cannot be empty")
            value != null && value.trim().length < minLength -> {
                errors.add("$fieldName must be at least $minLength characters")
            }
            value != null && value.trim().length > maxLength -> {
                errors.add("$fieldName must be less than $maxLength characters")
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
}
