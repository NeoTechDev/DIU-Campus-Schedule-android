package com.om.diucampusschedule.data.remote.dto

import com.google.firebase.firestore.DocumentSnapshot
import com.om.diucampusschedule.domain.model.Note

/**
 * Data Transfer Object for Note - used for Firebase/API communication
 */
data class NoteDto(
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val lastEditedTime: String = "",
    val userId: String = "",
    val richTextHtml: String = "",
    val color: String = "#FFFFFF"
)

// Extension functions for mapping between DTOs and domain models
fun NoteDto.toDomainModel(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        lastEditedTime = lastEditedTime,
        userId = userId,
        richTextHtml = richTextHtml,
        color = color
    )
}

fun Note.toDto(): NoteDto {
    return NoteDto(
        id = id,
        title = title,
        content = content,
        lastEditedTime = lastEditedTime,
        userId = userId,
        richTextHtml = richTextHtml,
        color = color
    )
}

// Extension function for mapping from Firestore DocumentSnapshot
fun DocumentSnapshot.toNoteDto(): NoteDto? {
    return try {
        NoteDto(
            id = getLong("id")?.toInt() ?: 0,
            title = getString("title") ?: "",
            content = getString("content") ?: "",
            lastEditedTime = getString("lastEditedTime") ?: "",
            userId = getString("userId") ?: "",
            richTextHtml = getString("richTextHtml") ?: "",
            color = getString("color") ?: "#FFFFFF"
        )
    } catch (e: Exception) {
        null
    }
}
