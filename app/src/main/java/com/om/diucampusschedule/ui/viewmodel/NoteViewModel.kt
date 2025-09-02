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
    val isSelectionMode: Boolean = false
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

            notesRepository.updateNote(note)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { error ->
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

    fun syncNotes() {
        val userId = currentUserId.value ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            notesRepository.syncNotesWithRemote(userId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to sync notes"
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