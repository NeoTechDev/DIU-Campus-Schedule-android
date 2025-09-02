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
            android.util.Log.d("NotesRemoteDataSource", "Fetching notes for user: $userId")
            
            val querySnapshot = notesCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            android.util.Log.d("NotesRemoteDataSource", "Found ${querySnapshot.documents.size} documents")
            
            val notes = querySnapshot.documents.mapNotNull { document ->
                android.util.Log.d("NotesRemoteDataSource", "Processing document: ${document.id}")
                document.toNoteDto()?.also { note ->
                    android.util.Log.d("NotesRemoteDataSource", "Successfully mapped note: ${note.id} - ${note.title}")
                }
            }.sortedByDescending { it.lastEditedTime } // Sort in memory instead
            
            android.util.Log.d("NotesRemoteDataSource", "Returning ${notes.size} notes")
            Result.success(notes)
        } catch (e: Exception) {
            android.util.Log.e("NotesRemoteDataSource", "Error fetching notes for user $userId", e)
            Result.failure(e)
        }
    }

    suspend fun getNoteById(noteId: Int): Result<NoteDto?> {
        return try {
            android.util.Log.d("NotesRemoteDataSource", "Getting note by ID: $noteId")
            
            val querySnapshot = notesCollection
                .whereEqualTo("id", noteId)
                .limit(1)
                .get()
                .await()

            val note = querySnapshot.documents.firstOrNull()?.toNoteDto()
            android.util.Log.d("NotesRemoteDataSource", "Found note: ${note?.title ?: "null"}")
            Result.success(note)
        } catch (e: Exception) {
            android.util.Log.e("NotesRemoteDataSource", "Error getting note by ID: $noteId", e)
            Result.failure(e)
        }
    }

    suspend fun createNote(noteDto: NoteDto): Result<NoteDto> {
        return try {
            android.util.Log.d("NotesRemoteDataSource", "Creating note: ${noteDto.title} for user: ${noteDto.userId}")
            
            // Let Firestore auto-generate a document ID for better distribution
            val docRef = notesCollection.document()
            
            // Use a deterministic approach: hash the Firestore document ID to get an integer
            val noteId = docRef.id.hashCode()
            val updatedNote = noteDto.copy(id = noteId)
            
            // Store the note with the Firestore-generated document ID
            docRef.set(updatedNote).await()
            
            android.util.Log.d("NotesRemoteDataSource", "Successfully created note with ID: $noteId, Firestore Doc: ${docRef.id}")
            Result.success(updatedNote)
        } catch (e: Exception) {
            android.util.Log.e("NotesRemoteDataSource", "Error creating note", e)
            Result.failure(e)
        }
    }

    suspend fun updateNote(noteDto: NoteDto): Result<NoteDto> {
        return try {
            android.util.Log.d("NotesRemoteDataSource", "Updating note: ${noteDto.id}")
            
            // Find the document by searching for the note ID in the stored documents
            val querySnapshot = notesCollection
                .whereEqualTo("id", noteDto.id)
                .whereEqualTo("userId", noteDto.userId) // Add user filter for security
                .limit(1) // We only expect one document
                .get()
                .await()

            val document = querySnapshot.documents.firstOrNull()
            if (document != null) {
                // Update the found document
                document.reference.set(noteDto).await()
                android.util.Log.d("NotesRemoteDataSource", "Successfully updated note: ${noteDto.id} in document: ${document.id}")
            } else {
                // Document not found - this shouldn't happen in normal flow
                // Create a new document as fallback
                android.util.Log.w("NotesRemoteDataSource", "Note ${noteDto.id} not found in Firestore, creating new document")
                val newDocRef = notesCollection.document()
                newDocRef.set(noteDto).await()
                android.util.Log.d("NotesRemoteDataSource", "Created new document: ${newDocRef.id} for note: ${noteDto.id}")
            }
            
            Result.success(noteDto)
        } catch (e: Exception) {
            android.util.Log.e("NotesRemoteDataSource", "Error updating note: ${noteDto.id}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteNote(noteId: Int): Result<Unit> {
        return try {
            android.util.Log.d("NotesRemoteDataSource", "Deleting note: $noteId")
            
            // Find the document by searching for the note ID
            val querySnapshot = notesCollection
                .whereEqualTo("id", noteId)
                .limit(1) // We only expect one document
                .get()
                .await()

            val document = querySnapshot.documents.firstOrNull()
            if (document != null) {
                // Delete the found document
                document.reference.delete().await()
                android.util.Log.d("NotesRemoteDataSource", "Successfully deleted note: $noteId from document: ${document.id}")
            } else {
                android.util.Log.w("NotesRemoteDataSource", "Note $noteId not found in Firestore")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("NotesRemoteDataSource", "Error deleting note: $noteId", e)
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
