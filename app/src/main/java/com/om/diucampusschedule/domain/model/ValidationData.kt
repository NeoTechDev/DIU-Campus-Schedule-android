package com.om.diucampusschedule.domain.model

/**
 * Data class to hold dynamic validation information extracted from routine data
 */
data class ValidationData(
    val validBatches: Set<String> = emptySet(),
    val validSectionsForBatch: Map<String, Set<String>> = emptyMap(),
    val validLabSections: Set<String> = emptySet(),
    val validTeacherInitials: Set<String> = emptySet(),
    val validDepartments: Set<String> = emptySet()
) {
    /**
     * Get valid sections for a specific batch
     */
    fun getSectionsForBatch(batch: String): Set<String> {
        return validSectionsForBatch[batch.trim()] ?: emptySet()
    }
    
    /**
     * Check if a batch is valid
     */
    fun isBatchValid(batch: String): Boolean {
        return validBatches.contains(batch.trim())
    }
    
    /**
     * Check if a section is valid for a specific batch
     */
    fun isSectionValidForBatch(batch: String, section: String): Boolean {
        val validSections = getSectionsForBatch(batch.trim())
        return validSections.contains(section.trim().uppercase())
    }
    
    /**
     * Check if a teacher initial is valid
     */
    fun isTeacherInitialValid(initial: String): Boolean {
        return validTeacherInitials.contains(initial.trim().uppercase())
    }
    
    /**
     * Check if a lab section is valid
     */
    fun isLabSectionValid(labSection: String): Boolean {
        return validLabSections.contains(labSection.trim().uppercase())
    }
    
    /**
     * Check if a department is valid
     */
    fun isDepartmentValid(department: String): Boolean {
        return validDepartments.contains(department.trim())
    }
}
