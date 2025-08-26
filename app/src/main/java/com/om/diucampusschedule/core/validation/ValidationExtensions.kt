package com.om.diucampusschedule.core.validation

/**
 * Extension functions for validation handling throughout the app
 */

/**
 * Validates and returns a Result based on validation outcome
 */
inline fun <T> DataValidator.ValidationResult.toResult(onSuccess: () -> T): Result<T> {
    return if (isValid) {
        try {
            Result.success(onSuccess())
        } catch (e: Exception) {
            Result.failure(e)
        }
    } else {
        Result.failure(Exception(getErrorMessage()))
    }
}

/**
 * Validates and executes a block if validation passes, otherwise returns failure
 */
inline fun <T> DataValidator.ValidationResult.ifValid(block: () -> Result<T>): Result<T> {
    return if (isValid) {
        block()
    } else {
        Result.failure(Exception(getErrorMessage()))
    }
}

/**
 * Combines multiple validation results
 */
fun combineValidations(vararg validations: DataValidator.ValidationResult): DataValidator.ValidationResult {
    val allErrors = validations.flatMap { it.errors }
    return DataValidator.ValidationResult(allErrors.isEmpty(), allErrors)
}

/**
 * Validates a list of items and returns combined result
 */
inline fun <T> List<T>.validateAll(validator: (T) -> DataValidator.ValidationResult): DataValidator.ValidationResult {
    val allErrors = mutableListOf<String>()
    
    this.forEachIndexed { index, item ->
        val validation = validator(item)
        if (!validation.isValid) {
            allErrors.addAll(validation.errors.map { "Item ${index + 1}: $it" })
        }
    }
    
    return DataValidator.ValidationResult(allErrors.isEmpty(), allErrors)
}

/**
 * Creates a validation result for a single error
 */
fun validationError(message: String): DataValidator.ValidationResult {
    return DataValidator.ValidationResult(false, listOf(message))
}

/**
 * Creates a successful validation result
 */
fun validationSuccess(): DataValidator.ValidationResult {
    return DataValidator.ValidationResult(true, emptyList())
}
