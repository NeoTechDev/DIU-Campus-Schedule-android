package com.om.diucampusschedule.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.om.diucampusschedule.data.remote.dto.NoteDto
import com.om.diucampusschedule.domain.model.Note

/**
 * Room entity for Note
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val lastEditedTime: String,
    val userId: String,
    val richTextHtml: String = "",
    val color: String = "#FFFFFF"
)

// Extension functions for mapping between entities, DTOs, and domain models
fun NoteEntity.toDomainModel(): Note {
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

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        lastEditedTime = lastEditedTime,
        userId = userId,
        richTextHtml = richTextHtml,
        color = color
    )
}

fun NoteDto.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        lastEditedTime = lastEditedTime,
        userId = userId,
        richTextHtml = richTextHtml,
        color = color
    )
}

fun NoteEntity.toDto(): NoteDto {
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
