package com.om.diucampusschedule.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.diucampusschedule.domain.model.AppState
import com.om.diucampusschedule.domain.usecase.auth.GetCurrentUserUseCase
import com.om.diucampusschedule.domain.usecase.notification.UpdateFCMTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing app initialization state
 * Separates app initialization logic from auth-specific logic
 */
@HiltViewModel
class AppInitializationViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateFCMTokenUseCase: UpdateFCMTokenUseCase
) : ViewModel() {

    private val _appState = MutableStateFlow<AppState>(AppState.Initializing)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch {
            try {
                // Observe current user and map to app state
                getCurrentUserUseCase.observeCurrentUser()
                    .onEach { user ->
                        _appState.value = when {
                            user == null -> AppState.Unauthenticated
                            !user.isProfileComplete -> AppState.AuthenticatedIncomplete(user)
                            else -> {
                                // Register FCM token for authenticated users
                                registerFCMToken(user)
                                AppState.AuthenticatedComplete(user)
                            }
                        }
                    }
                    .launchIn(this)
            } catch (e: Exception) {
                _appState.value = AppState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private fun registerFCMToken(user: com.om.diucampusschedule.domain.model.User) {
        viewModelScope.launch {
            try {
                updateFCMTokenUseCase(user).fold(
                    onSuccess = { token ->
                        android.util.Log.d("AppInit", "FCM token registered on app start: ${token.take(20)}...")
                    },
                    onFailure = { error ->
                        android.util.Log.w("AppInit", "Failed to register FCM token on app start", error)
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("AppInit", "Error during FCM token registration", e)
            }
        }
    }

    /**
     * Force refresh of app state - useful for retry scenarios
     */
    fun refreshAppState() {
        _appState.value = AppState.Initializing
        initializeApp()
    }
}
