package com.om.diucampusschedule.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Faculty(
    val name: String = "",
    val faculty_initial: String = "",
    val employee_id: String = "",
    val designation: String = "",
    val contact_number: String = "",
    val email: String = "",
    val room_no: String = "",
    val course: String = ""
)
