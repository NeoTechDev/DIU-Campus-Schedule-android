package com.om.diucampusschedule.data.local.dao

import androidx.room.*
import com.om.diucampusschedule.data.local.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Note operations
 */
@Dao
interface NoteDao {
    
    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY lastEditedTime DESC")
    fun getAllNotesForUser(userId: String): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY lastEditedTime DESC")
    suspend fun getAllNotesForUserSync(userId: String): List<NoteEntity>
    
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): NoteEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)
    
    @Update
    suspend fun updateNote(note: NoteEntity)
    
    @Delete
    suspend fun deleteNote(note: NoteEntity)
    
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Int)
    
    @Query("DELETE FROM notes WHERE userId = :userId")
    suspend fun deleteAllNotesForUser(userId: String)
    
    @Query("DELETE FROM notes WHERE id IN (:noteIds)")
    suspend fun deleteNotesByIds(noteIds: List<Int>)
}
