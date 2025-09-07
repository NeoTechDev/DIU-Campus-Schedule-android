package com.om.diucampusschedule.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.diucampusschedule.data.preferences.NotificationPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WelcomeUiState(
    val showWelcomeDialog: Boolean = false,
    val isLoading: Boolean = false
)

/**
 * ViewModel for managing welcome dialog state
 * Determines whether to show welcome dialog for first-time users
 */
@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val notificationPreferences: NotificationPreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()
    
    init {
        checkWelcomeDialogStatus()
    }
    
    /**
     * Check if welcome dialog should be shown
     */
    private fun checkWelcomeDialogStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val hasBeenShown: Boolean = notificationPreferences.hasWelcomeDialogBeenShown.first()
                _uiState.value = _uiState.value.copy(
                    showWelcomeDialog = !hasBeenShown,
                    isLoading = false
                )
            } catch (e: Exception) {
                // In case of error, don't show dialog to avoid issues
                _uiState.value = _uiState.value.copy(
                    showWelcomeDialog = false,
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * Mark welcome dialog as shown and hide it
     */
    fun dismissWelcomeDialog() {
        viewModelScope.launch {
            try {
                notificationPreferences.markWelcomeDialogShown()
                _uiState.value = _uiState.value.copy(showWelcomeDialog = false)
            } catch (e: Exception) {
                // In case of error, still hide dialog to avoid user being stuck
                _uiState.value = _uiState.value.copy(showWelcomeDialog = false)
            }
        }
    }
    
    /**
     * Force show welcome dialog (for testing purposes)
     */
    fun forceShowWelcomeDialog() {
        _uiState.value = _uiState.value.copy(showWelcomeDialog = true)
    }
}
