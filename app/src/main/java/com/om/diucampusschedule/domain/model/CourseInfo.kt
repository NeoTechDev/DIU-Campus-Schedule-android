package com.om.diucampusschedule.domain.model

/**
 * Data class representing course information loaded from courseNames.json
 */
data class CourseInfo(
    val courseCode: String,
    val courseName: String,
    val credit: Int,
    val batch: Int
)