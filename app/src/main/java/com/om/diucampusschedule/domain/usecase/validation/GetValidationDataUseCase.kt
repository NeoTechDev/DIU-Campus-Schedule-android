package com.om.diucampusschedule.domain.usecase.validation

import com.om.diucampusschedule.domain.model.ValidationData
import com.om.diucampusschedule.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetValidationDataUseCase @Inject constructor(
    private val routineRepository: RoutineRepository
) {
    
    /**
     * Get validation data for a specific department
     */
    suspend operator fun invoke(department: String): Result<ValidationData> {
        return try {
            val routineResult = routineRepository.getLatestScheduleForDepartment(department)
            routineResult.fold(
                onSuccess = { routineSchedule ->
                    val validationData = extractValidationData(routineSchedule.schedule)
                    Result.success(validationData)
                },
                onFailure = { error ->
                    // If no routine data available, return empty validation data
                    android.util.Log.w("GetValidationData", "No routine data for validation: ${error.message}")
                    Result.success(ValidationData())
                }
            )
        } catch (e: Exception) {
            android.util.Log.e("GetValidationData", "Error getting validation data", e)
            Result.success(ValidationData()) // Return empty data rather than failing
        }
    }
    
    /**
     * Observe validation data changes for a department
     */
    fun observeValidationData(department: String): Flow<ValidationData> {
        return routineRepository.observeLatestScheduleForDepartment(department)
            .map { routineSchedule ->
                if (routineSchedule != null) {
                    extractValidationData(routineSchedule.schedule)
                } else {
                    ValidationData()
                }
            }
    }
    
    /**
     * Extract validation data from routine items
     */
    private fun extractValidationData(routineItems: List<com.om.diucampusschedule.domain.model.RoutineItem>): ValidationData {
        val batches = mutableSetOf<String>()
        val sectionsForBatch = mutableMapOf<String, MutableSet<String>>()
        val labSections = mutableSetOf<String>()
        val teacherInitials = mutableSetOf<String>()
        val departments = mutableSetOf<String>()
        
        routineItems.forEach { item ->
            // Extract batches
            val batch = item.batch.trim()
            if (batch.isNotBlank()) {
                batches.add(batch)
            }
            
            // Extract sections for each batch
            val section = item.section.trim().uppercase()
            if (batch.isNotBlank() && section.isNotBlank()) {
                sectionsForBatch.getOrPut(batch) { mutableSetOf() }.add(section)
            }
            
            // Extract lab sections
            val labSection = item.labSection?.trim()?.uppercase()
            if (!labSection.isNullOrBlank()) {
                labSections.add(labSection)
            }
            
            // Extract teacher initials
            val initial = item.teacherInitial.trim().uppercase()
            if (initial.isNotBlank()) {
                teacherInitials.add(initial)
            }
            
            // Extract departments
            val department = item.department.trim()
            if (department.isNotBlank()) {
                departments.add(department)
            }
        }
        
        // Log extracted data for debugging
        android.util.Log.d("ValidationData", "Extracted batches: $batches")
        android.util.Log.d("ValidationData", "Extracted sections per batch: $sectionsForBatch")
        android.util.Log.d("ValidationData", "Extracted lab sections: $labSections")
        android.util.Log.d("ValidationData", "Extracted teacher initials: $teacherInitials")
        android.util.Log.d("ValidationData", "Extracted departments: $departments")
        
        return ValidationData(
            validBatches = batches,
            validSectionsForBatch = sectionsForBatch.mapValues { it.value.toSet() },
            validLabSections = labSections,
            validTeacherInitials = teacherInitials,
            validDepartments = departments
        )
    }
}
