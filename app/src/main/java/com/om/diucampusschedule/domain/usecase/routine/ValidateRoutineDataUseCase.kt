package com.om.diucampusschedule.domain.usecase.routine

import com.om.diucampusschedule.core.validation.DataValidator
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.RoutineSchedule
import javax.inject.Inject

class ValidateRoutineDataUseCase @Inject constructor() {
    
    /**
     * Validates a single routine item
     */
    fun validateRoutineItem(routineItem: RoutineItem): DataValidator.ValidationResult {
        val allErrors = mutableListOf<String>()
        
        // Validate individual fields
        allErrors.addAll(DataValidator.validateCourseCode(routineItem.courseCode).errors)
        allErrors.addAll(DataValidator.validateRoom(routineItem.room).errors)
        allErrors.addAll(DataValidator.validateTimeFormat(routineItem.time).errors)
        allErrors.addAll(DataValidator.validateDay(routineItem.day).errors)
        allErrors.addAll(DataValidator.validateTeacherInitial(routineItem.teacherInitial).errors)
        allErrors.addAll(DataValidator.validateBatch(routineItem.batch).errors)
        allErrors.addAll(DataValidator.validateSection(routineItem.section).errors)
        
        // Lab section is optional
        if (routineItem.labSection?.isNotBlank() == true) {
            allErrors.addAll(DataValidator.validateLabSection(routineItem.labSection).errors)
        }
        
        // Validate department and other required fields
        allErrors.addAll(DataValidator.validateDepartment(routineItem.department).errors)
        
        // Validate semester format
        if (routineItem.semester.isBlank()) {
            allErrors.add("Semester cannot be empty")
        } else if (routineItem.semester.length > 50) {
            allErrors.add("Semester name is too long")
        }
        
        // Validate effective from date format (basic check)
        if (routineItem.effectiveFrom.isBlank()) {
            allErrors.add("Effective from date cannot be empty")
        } else if (!routineItem.effectiveFrom.matches(Regex("^\\d{2}-\\d{2}-\\d{4}$"))) {
            allErrors.add("Effective from date must be in DD-MM-YYYY format")
        }
        
        return DataValidator.ValidationResult(allErrors.isEmpty(), allErrors)
    }
    
    /**
     * Validates an entire routine schedule
     */
    fun validateRoutineSchedule(routineSchedule: RoutineSchedule): DataValidator.ValidationResult {
        val allErrors = mutableListOf<String>()
        
        // Validate schedule metadata
        allErrors.addAll(DataValidator.validateDepartment(routineSchedule.department).errors)
        
        if (routineSchedule.semester.isBlank()) {
            allErrors.add("Schedule semester cannot be empty")
        }
        
        if (routineSchedule.effectiveFrom.isBlank()) {
            allErrors.add("Schedule effective from date cannot be empty")
        }
        
        if (routineSchedule.schedule.isEmpty()) {
            allErrors.add("Schedule cannot be empty")
        }
        
        // Validate each routine item
        routineSchedule.schedule.forEachIndexed { index, routineItem ->
            val itemValidation = validateRoutineItem(routineItem)
            if (!itemValidation.isValid) {
                allErrors.add("Item ${index + 1}: ${itemValidation.getErrorMessage()}")
            }
        }
        
        // Check for duplicate items (same day, time, room)
        val duplicateGroups = routineSchedule.schedule
            .groupBy { "${it.day}-${it.time}-${it.room}" }
            .filter { it.value.size > 1 }
        
        if (duplicateGroups.isNotEmpty()) {
            duplicateGroups.forEach { (key, items) ->
                allErrors.add("Duplicate schedule items found: $key (${items.size} items)")
            }
        }
        
        // Check for time conflicts (same room, same time, same day)
        val timeConflicts = routineSchedule.schedule
            .groupBy { "${it.day}-${it.room}" }
            .filter { it.value.size > 1 }
            .mapValues { entry ->
                entry.value.groupBy { it.time }.filter { it.value.size > 1 }
            }
            .filter { it.value.isNotEmpty() }
        
        if (timeConflicts.isNotEmpty()) {
            timeConflicts.forEach { (dayRoom, conflicts) ->
                conflicts.forEach { (time, items) ->
                    allErrors.add("Time conflict in room ${dayRoom.split("-")[1]} on ${dayRoom.split("-")[0]} at $time (${items.size} classes)")
                }
            }
        }
        
        return DataValidator.ValidationResult(allErrors.isEmpty(), allErrors)
    }
    
    /**
     * Validates routine data consistency
     */
    fun validateRoutineConsistency(routineSchedule: RoutineSchedule): DataValidator.ValidationResult {
        val allErrors = mutableListOf<String>()
        
        // Check if all items have consistent department
        val departments = routineSchedule.schedule.map { it.department }.distinct()
        if (departments.size > 1) {
            allErrors.add("Inconsistent departments in routine items: ${departments.joinToString(", ")}")
        }
        
        if (departments.isNotEmpty() && departments.first() != routineSchedule.department) {
            allErrors.add("Schedule department (${routineSchedule.department}) doesn't match item departments (${departments.first()})")
        }
        
        // Check if all items have consistent semester
        val semesters = routineSchedule.schedule.map { it.semester }.distinct()
        if (semesters.size > 1) {
            allErrors.add("Inconsistent semesters in routine items: ${semesters.joinToString(", ")}")
        }
        
        if (semesters.isNotEmpty() && semesters.first() != routineSchedule.semester) {
            allErrors.add("Schedule semester (${routineSchedule.semester}) doesn't match item semesters (${semesters.first()})")
        }
        
        // Check if all items have consistent effective from date
        val effectiveDates = routineSchedule.schedule.map { it.effectiveFrom }.distinct()
        if (effectiveDates.size > 1) {
            allErrors.add("Inconsistent effective dates in routine items: ${effectiveDates.joinToString(", ")}")
        }
        
        if (effectiveDates.isNotEmpty() && effectiveDates.first() != routineSchedule.effectiveFrom) {
            allErrors.add("Schedule effective date (${routineSchedule.effectiveFrom}) doesn't match item dates (${effectiveDates.first()})")
        }
        
        return DataValidator.ValidationResult(allErrors.isEmpty(), allErrors)
    }
}
