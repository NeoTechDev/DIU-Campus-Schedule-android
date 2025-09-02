package com.om.diucampusschedule.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.om.diucampusschedule.data.remote.dto.NoteDto
import com.om.diucampusschedule.data.remote.dto.toNoteDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val notesCollection = firestore.collection("notes")

    suspend fun getNotesForUser(userId: String): Result<List<NoteDto>> {
        return try {
            val querySnapshot = notesCollection
                .whereEqualTo("userId", userId)
                .orderBy("lastEditedTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val notes = querySnapshot.documents.mapNotNull { it.toNoteDto() }
            Result.success(notes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNoteById(noteId: Int): Result<NoteDto?> {
        return try {
            val querySnapshot = notesCollection
                .whereEqualTo("id", noteId)
                .get()
                .await()

            val note = querySnapshot.documents.firstOrNull()?.toNoteDto()
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createNote(noteDto: NoteDto): Result<NoteDto> {
        return try {
            // Generate a document ID and use it as the note ID
            val docRef = notesCollection.document()
            val updatedNote = noteDto.copy(id = docRef.id.hashCode())
            
            docRef.set(updatedNote).await()
            Result.success(updatedNote)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNote(noteDto: NoteDto): Result<NoteDto> {
        return try {
            // Find the document by note ID
            val querySnapshot = notesCollection
                .whereEqualTo("id", noteDto.id)
                .get()
                .await()

            val document = querySnapshot.documents.firstOrNull()
                ?: throw Exception("Note not found")

            document.reference.set(noteDto).await()
            Result.success(noteDto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNote(noteId: Int): Result<Unit> {
        return try {
            val querySnapshot = notesCollection
                .whereEqualTo("id", noteId)
                .get()
                .await()

            val document = querySnapshot.documents.firstOrNull()
                ?: throw Exception("Note not found")

            document.reference.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNotes(noteIds: List<Int>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            
            for (noteId in noteIds) {
                val querySnapshot = notesCollection
                    .whereEqualTo("id", noteId)
                    .get()
                    .await()

                querySnapshot.documents.forEach { document ->
                    batch.delete(document.reference)
                }
            }
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
