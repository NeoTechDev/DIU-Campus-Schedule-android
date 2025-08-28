package com.om.diucampusschedule.domain.usecase.routine

import com.om.diucampusschedule.domain.model.DayOfWeek
import javax.inject.Inject

class GetAllDaysUseCase @Inject constructor() {
    
    /**
     * Get all days of the week including off days for comprehensive display
     */
    operator fun invoke(): List<String> {
        // Get all working days from enum
        val workingDays = DayOfWeek.getWorkingDays().map { it.displayName }
        
        // Add Friday as off day
        val allDays = workingDays + listOf("Friday")
        
        return allDays.sortedBy { day ->
            when (day.lowercase()) {
                "saturday" -> 1
                "sunday" -> 2
                "monday" -> 3
                "tuesday" -> 4
                "wednesday" -> 5
                "thursday" -> 6
                "friday" -> 7
                else -> 8
            }
        }
    }
}
