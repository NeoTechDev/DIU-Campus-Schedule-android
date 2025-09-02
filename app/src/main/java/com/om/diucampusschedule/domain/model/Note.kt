package com.om.diucampusschedule.domain.model

/**
 * Domain model for Note
 */
data class Note(
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val lastEditedTime: String = "",
    val userId: String = "",
    val richTextHtml: String = "",
    val color: String = "#FFFFFF"
)
