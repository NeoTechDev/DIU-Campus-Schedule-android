package com.om.diucampusschedule.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.diucampusschedule.domain.model.Note
import com.om.diucampusschedule.domain.repository.AuthRepository
import com.om.diucampusschedule.domain.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedNoteIds: Set<Int> = emptySet(),
    val isSelectionMode: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncTime: String? = null,
    val syncMessage: String? = null
)

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    // Get current user ID from auth repository
    private val currentUserId = authRepository.observeCurrentUser()
        .map { it?.id }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        observeNotes()
    }

    private fun observeNotes() {
        currentUserId.filterNotNull().flatMapLatest { userId ->
            // Auto-sync when user changes (login)
            syncNotesInBackground()
            notesRepository.observeNotesForUser(userId)
        }.onEach { notes ->
            _uiState.value = _uiState.value.copy(
                notes = notes,
                isLoading = false
            )
        }.launchIn(viewModelScope)
    }

    fun createNote(title: String, content: String, richTextHtml: String = "", color: String = "#FFFFFF") {
        val userId = currentUserId.value ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val timestamp = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                .format(Date())
            
            val note = Note(
                title = title,
                content = content,
                lastEditedTime = timestamp,
                userId = userId,
                richTextHtml = richTextHtml,
                color = color
            )

            notesRepository.createNote(note)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    // Auto-sync after creating note
                    syncNotesInBackground()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to create note"
                    )
                }
        }
    }

    fun updateNote(noteId: Int, title: String, content: String, richTextHtml: String = "", color: String = "#FFFFFF") {
        val userId = currentUserId.value ?: return
        
        viewModelScope.launch {
            android.util.Log.d("NoteViewModel", "Updating note: $noteId with title: '$title'")
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val timestamp = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                .format(Date())
            
            val note = Note(
                id = noteId,
                title = title,
                content = content,
                lastEditedTime = timestamp,
                userId = userId,
                richTextHtml = richTextHtml,
                color = color
            )

            android.util.Log.d("NoteViewModel", "Created note object: $note")

            notesRepository.updateNote(note)
                .onSuccess { updatedNote ->
                    android.util.Log.d("NoteViewModel", "Successfully updated note: ${updatedNote.id}")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    // Auto-sync after updating note
                    syncNotesInBackground()
                }
                .onFailure { error ->
                    android.util.Log.e("NoteViewModel", "Failed to update note: $noteId", error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to update note"
                    )
                }
        }
    }

    fun deleteNote(noteId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            notesRepository.deleteNote(noteId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    // Auto-sync after deleting note
                    syncNotesInBackground()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to delete note"
                    )
                }
        }
    }

    fun deleteSelectedNotes() {
        val selectedIds = _uiState.value.selectedNoteIds.toList()
        if (selectedIds.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            notesRepository.deleteNotes(selectedIds)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedNoteIds = emptySet(),
                        isSelectionMode = false
                    )
                    // Auto-sync after deleting notes
                    syncNotesInBackground()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to delete notes"
                    )
                }
        }
    }

    fun toggleNoteSelection(noteId: Int) {
        val currentSelection = _uiState.value.selectedNoteIds
        val newSelection = if (currentSelection.contains(noteId)) {
            currentSelection - noteId
        } else {
            currentSelection + noteId
        }
        
        _uiState.value = _uiState.value.copy(
            selectedNoteIds = newSelection,
            isSelectionMode = newSelection.isNotEmpty()
        )
    }

    fun selectAllNotes() {
        val allNoteIds = _uiState.value.notes.map { it.id }.toSet()
        _uiState.value = _uiState.value.copy(
            selectedNoteIds = allNoteIds,
            isSelectionMode = allNoteIds.isNotEmpty()
        )
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(
            selectedNoteIds = emptySet(),
            isSelectionMode = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // Background sync that doesn't show loading state to avoid UI interruption
    private fun syncNotesInBackground() {
        val userId = currentUserId.value ?: return
        
        viewModelScope.launch {
            // Perform sync without updating loading state but show sync indicator
            _uiState.value = _uiState.value.copy(isSyncing = true)
            
            notesRepository.syncNotesWithRemote(userId)
                .onSuccess {
                    val timestamp = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date())
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        lastSyncTime = timestamp
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isSyncing = false)
                }
        }
    }
    
    fun syncNotes() {
        val userId = currentUserId.value ?: return
        
        viewModelScope.launch {
            android.util.Log.d("NoteViewModel", "Starting sync for user: $userId")
            
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                isSyncing = true, 
                errorMessage = null,
                syncMessage = "Syncing notes..."
            )
            
            val startTime = System.currentTimeMillis()
            
            notesRepository.syncNotesWithRemote(userId)
                .onSuccess {
                    val duration = System.currentTimeMillis() - startTime
                    val timestamp = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date())
                    
                    android.util.Log.d("NoteViewModel", "Sync completed successfully in ${duration}ms")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSyncing = false,
                        lastSyncTime = timestamp,
                        syncMessage = "Sync completed successfully"
                    )
                    
                    // Clear sync message after 3 seconds
                    kotlinx.coroutines.delay(3000)
                    _uiState.value = _uiState.value.copy(syncMessage = null)
                }
                .onFailure { error ->
                    android.util.Log.e("NoteViewModel", "Sync failed", error)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSyncing = false,
                        errorMessage = error.message ?: "Failed to sync notes",
                        syncMessage = null
                    )
                }
        }
    }

    // Legacy methods for compatibility with existing UI (can be removed later)
    @Deprecated("Use repository-based methods instead")
    fun loadNotes(sharedPreferences: android.content.SharedPreferences): List<Note> {
        return _uiState.value.notes
    }

    @Deprecated("Use repository-based methods instead")
    fun saveNotes(sharedPreferences: android.content.SharedPreferences, notes: List<Note>) {
        // No-op - handled by repository
    }

    @Deprecated("Use deleteSelectedNotes() instead")
    fun deleteNotes(noteIds: Set<Int>, sharedPreferences: android.content.SharedPreferences) {
        val currentSelection = _uiState.value.selectedNoteIds
        _uiState.value = _uiState.value.copy(selectedNoteIds = noteIds)
        deleteSelectedNotes()
    }
}