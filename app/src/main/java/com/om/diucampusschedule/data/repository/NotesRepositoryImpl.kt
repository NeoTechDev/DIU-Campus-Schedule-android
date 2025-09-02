package com.om.diucampusschedule.data.repository

import com.om.diucampusschedule.data.local.NotesLocalDataSource
import com.om.diucampusschedule.data.remote.NotesRemoteDataSource
import com.om.diucampusschedule.data.remote.dto.toDomainModel
import com.om.diucampusschedule.data.remote.dto.toDto
import com.om.diucampusschedule.domain.model.Note
import com.om.diucampusschedule.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesRepositoryImpl @Inject constructor(
    private val localDataSource: NotesLocalDataSource,
    private val remoteDataSource: NotesRemoteDataSource
) : NotesRepository {

    override fun observeNotesForUser(userId: String): Flow<List<Note>> {
        return localDataSource.observeNotesForUser(userId)
    }

    override suspend fun getNotesForUser(userId: String): Result<List<Note>> {
        return try {
            // Try remote first for latest data
            val remoteResult = remoteDataSource.getNotesForUser(userId)
            if (remoteResult.isSuccess) {
                val remoteDtos = remoteResult.getOrThrow()
                val remoteNotes = remoteDtos.map { it.toDomainModel() }
                
                // Update local cache
                localDataSource.deleteAllNotesForUser(userId)
                if (remoteNotes.isNotEmpty()) {
                    localDataSource.insertNotes(remoteNotes)
                }
                
                Result.success(remoteNotes)
            } else {
                // Fallback to local data
                val localNotes = localDataSource.getNotesForUser(userId)
                Result.success(localNotes)
            }
        } catch (e: Exception) {
            // Fallback to local data on any error
            try {
                val localNotes = localDataSource.getNotesForUser(userId)
                Result.success(localNotes)
            } catch (localException: Exception) {
                Result.failure(localException)
            }
        }
    }

    override suspend fun getNoteById(noteId: Int): Result<Note?> {
        return try {
            // Try local first for better performance
            val localNote = localDataSource.getNoteById(noteId)
            if (localNote != null) {
                Result.success(localNote)
            } else {
                // Try remote if not found locally
                val remoteResult = remoteDataSource.getNoteById(noteId)
                if (remoteResult.isSuccess) {
                    val remoteDto = remoteResult.getOrThrow()
                    val remoteNote = remoteDto?.toDomainModel()
                    
                    // Cache locally if found
                    if (remoteNote != null) {
                        localDataSource.insertNote(remoteNote)
                    }
                    
                    Result.success(remoteNote)
                } else {
                    Result.success(null)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createNote(note: Note): Result<Note> {
        return try {
            // Save locally first
            val localId = localDataSource.insertNote(note)
            val localNote = note.copy(id = localId.toInt())
            
            // Try to save remotely
            val remoteResult = remoteDataSource.createNote(localNote.toDto())
            if (remoteResult.isSuccess) {
                val remoteDto = remoteResult.getOrThrow()
                val remoteNote = remoteDto.toDomainModel()
                
                // Update local with remote ID if different
                if (remoteNote.id != localNote.id) {
                    localDataSource.updateNote(remoteNote)
                }
                
                Result.success(remoteNote)
            } else {
                // Return local note even if remote failed
                Result.success(localNote)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNote(note: Note): Result<Note> {
        return try {
            // Update locally first
            localDataSource.updateNote(note)
            
            // Try to update remotely
            val remoteResult = remoteDataSource.updateNote(note.toDto())
            if (remoteResult.isSuccess) {
                val remoteDto = remoteResult.getOrThrow()
                val remoteNote = remoteDto.toDomainModel()
                
                // Update local with remote data
                localDataSource.updateNote(remoteNote)
                
                Result.success(remoteNote)
            } else {
                // Return local note even if remote failed
                Result.success(note)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(noteId: Int): Result<Unit> {
        return try {
            // Delete locally first
            localDataSource.deleteNote(noteId)
            
            // Try to delete remotely
            val remoteResult = remoteDataSource.deleteNote(noteId)
            if (remoteResult.isSuccess) {
                Result.success(Unit)
            } else {
                // Consider it successful even if remote failed since local is deleted
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNotes(noteIds: List<Int>): Result<Unit> {
        return try {
            // Delete locally first
            localDataSource.deleteNotes(noteIds)
            
            // Try to delete remotely
            val remoteResult = remoteDataSource.deleteNotes(noteIds)
            if (remoteResult.isSuccess) {
                Result.success(Unit)
            } else {
                // Consider it successful even if remote failed since local is deleted
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncNotesWithRemote(userId: String): Result<Unit> {
        return try {
            val remoteResult = remoteDataSource.getNotesForUser(userId)
            if (remoteResult.isSuccess) {
                val remoteDtos = remoteResult.getOrThrow()
                val remoteNotes = remoteDtos.map { it.toDomainModel() }
                
                // Replace local data with remote data
                localDataSource.deleteAllNotesForUser(userId)
                if (remoteNotes.isNotEmpty()) {
                    localDataSource.insertNotes(remoteNotes)
                }
                
                Result.success(Unit)
            } else {
                Result.failure(remoteResult.exceptionOrNull() ?: Exception("Sync failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
