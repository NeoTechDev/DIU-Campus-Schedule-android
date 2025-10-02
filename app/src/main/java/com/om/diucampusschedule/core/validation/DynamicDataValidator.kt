package com.om.diucampusschedule.core.validation

import com.om.diucampusschedule.domain.model.UserRole
import com.om.diucampusschedule.domain.model.ValidationData

/**
 * Dynamic data validator that uses actual routine data to validate user input
 */
object DynamicDataValidator {
    
    /**
     * Validate batch using actual routine data
     */
    fun validateBatch(batch: String?, validationData: ValidationData): DataValidator.ValidationResult {
        val errors = mutableListOf<String>()
        
        when {
            batch.isNullOrBlank() -> errors.add("Batch cannot be empty")
            !batch.trim().matches(Regex("^\\d+$")) -> errors.add("Batch must contain only numbers")
            !validationData.isBatchValid(batch.trim()) -> {
                val sortedBatches = validationData.validBatches.map { it.toInt() }.sorted()
                val availableBatches = if (sortedBatches.size > 1 &&
                    sortedBatches.last() - sortedBatches.first() + 1 == sortedBatches.size
                ) {
                    // Continuous range
                    "${sortedBatches.first()} to ${sortedBatches.last()}"
                } else {
                    // Non-continuous, fall back to comma-separated
                    sortedBatches.joinToString(", ")
                }
                errors.add("Invalid batch. Available batches: $availableBatches")
            }
        }
        
        return DataValidator.ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Validate section using actual routine data for the specific batch
     */
    fun validateSection(section: String?, batch: String?, validationData: ValidationData): DataValidator.ValidationResult {
        val errors = mutableListOf<String>()
        val sec = section?.trim()?.uppercase()
        val bat = batch?.trim()

        when {
            sec.isNullOrEmpty() -> errors.add("Section cannot be empty")
            sec.length != 1 -> errors.add("Section must be a single letter")
            sec[0] !in 'A'..'Z' -> errors.add("Section must be a letter (A-Z)")
            bat.isNullOrEmpty() -> errors.add("Please select a valid batch first")
            !validationData.isBatchValid(bat) -> errors.add("Please select a valid batch first")
            !validationData.isSectionValidForBatch(bat, sec) -> {
                val availableSections = formatSectionsAsLetterRange(
                    validationData.getSectionsForBatch(bat)
                )
                if (availableSections.isNotEmpty()) {
                    errors.add("Invalid section for batch $bat. Available sections: $availableSections")
                } else {
                    errors.add("No sections available for batch $bat")
                }
            }
        }

        return DataValidator.ValidationResult(errors.isEmpty(), errors)
    }



    /**
     * Validate lab section using actual routine data
     */
    fun validateLabSection(labSection: String?, validationData: ValidationData): DataValidator.ValidationResult {
        val errors = mutableListOf<String>()
        
        if (!labSection.isNullOrBlank()) {
            when {
                labSection.trim().length > 10 -> errors.add("Lab section is too long (max 10 characters)")
                !labSection.trim().matches(Regex("^[A-Za-z]\\d*$")) -> {
                    errors.add("Lab section must start with a letter followed by numbers (e.g., A1, B2)")
                }
                validationData.validLabSections.isNotEmpty() && !validationData.isLabSectionValid(labSection.trim()) -> {
                    val availableLabSections = validationData.validLabSections.sorted().joinToString(", ")
                    errors.add("Invalid lab section. Available lab sections: $availableLabSections")
                }
            }
        }
        
        return DataValidator.ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Validate teacher initial using actual routine data
     */
    fun validateTeacherInitial(initial: String?, validationData: ValidationData): DataValidator.ValidationResult {
        val errors = mutableListOf<String>()
        val trimmedInitial = initial?.trim()

        when {
            trimmedInitial.isNullOrEmpty() -> errors.add("Teacher initial cannot be empty")
            trimmedInitial.length < 2 -> errors.add("Initial must be at least 2 characters")
            trimmedInitial.length > 6 -> errors.add("Initial must be at most 6 characters")
            !trimmedInitial.matches(Regex("^[A-Za-z]+$")) -> errors.add("Initial can only contain letters")
            validationData.validTeacherInitials.isNotEmpty() &&
                    !validationData.isTeacherInitialValid(trimmedInitial) -> {
                errors.add("Invalid teacher initial. Please enter correct initial")
            }
        }

        return DataValidator.ValidationResult(errors.isEmpty(), errors)
    }


    /**
     * Validate department using actual routine data
     */
    fun validateDepartment(department: String?, validationData: ValidationData): DataValidator.ValidationResult {
        val errors = mutableListOf<String>()
        
        when {
            department.isNullOrBlank() -> errors.add("Department cannot be empty")
            department.trim().length > 100 -> errors.add("Department name is too long")
            validationData.validDepartments.isNotEmpty() && !validationData.isDepartmentValid(department.trim()) -> {
                val availableDepartments = validationData.validDepartments.sorted().joinToString(", ")
                errors.add("Invalid department. Available departments: $availableDepartments")
            }
        }
        
        return DataValidator.ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Validate room number using actual routine data
     */
    fun validateRoom(room: String?, validationData: ValidationData): DataValidator.ValidationResult {
        val errors = mutableListOf<String>()
        val trimmed = room?.trim()?.uppercase()

        when {
            trimmed.isNullOrEmpty() -> errors.add("Room number cannot be empty")
            validationData.validRooms.isNotEmpty() && !validationData.isRoomValid(trimmed) -> {
                errors.add("Invalid room number. Please enter a correct room")
            }
        }

        return DataValidator.ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Comprehensive user profile validation using dynamic data
     */
    fun validateUserProfile(
        name: String?,
        email: String?,
        department: String?,
        role: UserRole?,
        batch: String? = null,
        section: String? = null,
        labSection: String? = null,
        initial: String? = null,
        validationData: ValidationData
    ): DataValidator.ValidationResult {
        val allErrors = mutableListOf<String>()
        
        // Basic field validation using static validator
        allErrors.addAll(DataValidator.validateName(name).errors)
        allErrors.addAll(DataValidator.validateEmail(email).errors)
        
        // Dynamic department validation
        allErrors.addAll(validateDepartment(department, validationData).errors)
        
        // Role validation
        if (role == null) {
            allErrors.add("Role must be specified")
        }
        
        // Role-specific dynamic validation
        when (role) {
            UserRole.STUDENT -> {
                // Dynamic batch validation
                allErrors.addAll(validateBatch(batch, validationData).errors)
                
                // Dynamic section validation (depends on batch)
                allErrors.addAll(validateSection(section, batch, validationData).errors)
                
                // Dynamic lab section validation
                allErrors.addAll(validateLabSection(labSection, validationData).errors)
                
                // Ensure teacher-specific fields are not provided
                if (!initial.isNullOrBlank()) {
                    allErrors.add("Students should not have teacher initial")
                }
            }
            UserRole.TEACHER -> {
                // Dynamic teacher initial validation
                allErrors.addAll(validateTeacherInitial(initial, validationData).errors)
                
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
        
        return DataValidator.ValidationResult(allErrors.isEmpty(), allErrors)
    }
    
    /**
     * Get helpful suggestions based on validation data
     */
    fun getValidationSuggestions(validationData: ValidationData): Map<String, List<String>> {
        return mapOf(
            "batches" to validationData.validBatches.sorted(),
            "departments" to validationData.validDepartments.sorted(),
            "teacherInitials" to validationData.validTeacherInitials.sorted(),
            "labSections" to validationData.validLabSections.sorted(),
            "rooms" to validationData.validRooms.sorted()
        )
    }

    private fun formatSectionsAsLetterRange(raw: Collection<String>): String {
        if (raw.isEmpty()) return ""

        // Take only first character, make uppercase, deduplicate, sort
        val letters = raw.mapNotNull {
            it.trim().takeIf { s -> s.isNotEmpty() }?.uppercase()?.firstOrNull()
        }.distinct().sorted()

        if (letters.isEmpty()) return ""
        if (letters.size == 1) return letters.first().toString()

        val first = letters.first()
        val last = letters.last()
        val isContinuous = last.code - first.code + 1 == letters.size &&
                letters.withIndex().all { (i, c) -> c.code == first.code + i }

        return if (isContinuous) {
            "$first to $last"
        } else {
            // Not continuous: just list the letters
            letters.joinToString(", ")
        }
    }

}
