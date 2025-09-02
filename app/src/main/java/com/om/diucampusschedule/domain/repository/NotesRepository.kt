package com.om.diucampusschedule.domain.repository

import com.om.diucampusschedule.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun observeNotesForUser(userId: String): Flow<List<Note>>
    suspend fun getNotesForUser(userId: String): Result<List<Note>>
    suspend fun getNoteById(noteId: Int): Result<Note?>
    suspend fun createNote(note: Note): Result<Note>
    suspend fun updateNote(note: Note): Result<Note>
    suspend fun deleteNote(noteId: Int): Result<Unit>
    suspend fun deleteNotes(noteIds: List<Int>): Result<Unit>
    suspend fun syncNotesWithRemote(userId: String): Result<Unit>
}
