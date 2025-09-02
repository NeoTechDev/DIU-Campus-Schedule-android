package com.om.diucampusschedule.data.local

import com.om.diucampusschedule.data.local.entities.NoteEntity
import com.om.diucampusschedule.data.local.entities.toDomainModel
import com.om.diucampusschedule.data.local.entities.toEntity
import com.om.diucampusschedule.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesLocalDataSource @Inject constructor(
    private val database: AppDatabase
) {
    private val noteDao = database.noteDao()

    fun observeNotesForUser(userId: String): Flow<List<Note>> {
        return noteDao.getAllNotesForUser(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getNotesForUser(userId: String): List<Note> {
        return noteDao.getAllNotesForUserSync(userId).map { it.toDomainModel() }
    }

    suspend fun getNoteById(noteId: Int): Note? {
        return noteDao.getNoteById(noteId)?.toDomainModel()
    }

    suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(note.toEntity())
    }

    suspend fun insertNotes(notes: List<Note>) {
        noteDao.insertNotes(notes.map { it.toEntity() })
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note.toEntity())
    }

    suspend fun deleteNote(noteId: Int) {
        noteDao.deleteNoteById(noteId)
    }

    suspend fun deleteNotes(noteIds: List<Int>) {
        noteDao.deleteNotesByIds(noteIds)
    }

    suspend fun deleteAllNotesForUser(userId: String) {
        noteDao.deleteAllNotesForUser(userId)
    }
}
