package com.om.diucampusschedule.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.om.diucampusschedule.core.network.NetworkConnectivityManager
import com.om.diucampusschedule.data.repository.AuthRepositoryImpl
import com.om.diucampusschedule.domain.model.AuthState
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.model.UserRegistrationForm
import com.om.diucampusschedule.domain.usecase.auth.CheckEmailVerificationUseCase
import com.om.diucampusschedule.domain.usecase.auth.GetCurrentUserUseCase
import com.om.diucampusschedule.domain.usecase.auth.GoogleSignInUseCase
import com.om.diucampusschedule.domain.usecase.auth.ResetPasswordUseCase
import com.om.diucampusschedule.domain.usecase.auth.SendEmailVerificationUseCase
import com.om.diucampusschedule.domain.usecase.auth.SignInUseCase
import com.om.diucampusschedule.domain.usecase.auth.SignOutUseCase
import com.om.diucampusschedule.domain.usecase.auth.SignUpUseCase
import com.om.diucampusschedule.domain.usecase.auth.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val sendEmailVerificationUseCase: SendEmailVerificationUseCase,
    private val checkEmailVerificationUseCase: CheckEmailVerificationUseCase,
    private val networkConnectivityManager: NetworkConnectivityManager,
    // Inject the repository implementation to demonstrate accessing additional methods
    private val authRepositoryImpl: AuthRepositoryImpl
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        getCurrentUserUseCase.observeCurrentUser() // Changed from observeAuthState
            .onEach { user ->
                _authState.value = _authState.value.copy(
                    isAuthenticated = user != null,
                    user = user,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            // Check internet connectivity first
            if (!networkConnectivityManager.isConnected()) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = networkConnectivityManager.getNetworkErrorMessage()
                )
                return@launch
            }

            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            val result = signInUseCase(email, password)
            
            result.fold(
                onSuccess = { user ->
                    _authState.value = _authState.value.copy(
                        isAuthenticated = true,
                        user = user,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _authState.value = _authState.value.copy(
                        isAuthenticated = false,
                        user = null,
                        isLoading = false,
                        error = if (!networkConnectivityManager.isConnected()) {
                            networkConnectivityManager.getNetworkErrorMessage()
                        } else {
                            exception.message
                        }
                    )
                }
            )
        }
    }

    fun signUp(email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            // Check internet connectivity first
            if (!networkConnectivityManager.isConnected()) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = networkConnectivityManager.getNetworkErrorMessage()
                )
                return@launch
            }

            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            val result = signUpUseCase(email, password, confirmPassword)
            
            result.fold(
                onSuccess = { user ->
                    _authState.value = _authState.value.copy(
                        isAuthenticated = true,
                        user = user,
                        isLoading = false,
                        error = null,
                        isEmailVerificationSent = true // Email verification is sent during signup
                    )
                },
                onFailure = { exception ->
                    _authState.value = _authState.value.copy(
                        isAuthenticated = false,
                        user = null,
                        isLoading = false,
                        error = if (!networkConnectivityManager.isConnected()) {
                            networkConnectivityManager.getNetworkErrorMessage()
                        } else {
                            exception.message
                        }
                    )
                }
            )
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            // Check internet connectivity first
            if (!networkConnectivityManager.isConnected()) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = networkConnectivityManager.getNetworkErrorMessage()
                )
                return@launch
            }

            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            val result = googleSignInUseCase(idToken)
            
            result.fold(
                onSuccess = { user ->
                    _authState.value = _authState.value.copy(
                        isAuthenticated = true,
                        user = user,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _authState.value = _authState.value.copy(
                        isAuthenticated = false,
                        user = null,
                        isLoading = false,
                        error = if (!networkConnectivityManager.isConnected()) {
                            networkConnectivityManager.getNetworkErrorMessage()
                        } else {
                            exception.message
                        }
                    )
                }
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            val result = signOutUseCase()
            
            result.fold(
                onSuccess = {
                    _authState.value = _authState.value.copy(
                        isAuthenticated = false,
                        user = null,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

    fun signOutWithGoogle(googleSignInClient: GoogleSignInClient? = null) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            try {
                // Sign out from Firebase first
                val result = signOutUseCase()
                
                result.fold(
                    onSuccess = {
                        // Sign out from Google Sign-In client if provided
                        googleSignInClient?.signOut()?.addOnCompleteListener {
                            _authState.value = _authState.value.copy(
                                isAuthenticated = false,
                                user = null,
                                isLoading = false,
                                error = null
                            )
                        } ?: run {
                            // If no Google client provided, just update state
                            _authState.value = _authState.value.copy(
                                isAuthenticated = false,
                                user = null,
                                isLoading = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Sign out failed"
                )
            }
        }
    }

    fun updateUserProfile(form: UserRegistrationForm) {
        viewModelScope.launch {
            val currentUser = _authState.value.user
            if (currentUser == null) {
                _authState.value = _authState.value.copy(
                    error = "No user logged in"
                )
                return@launch
            }

            // Check internet connectivity first
            if (!networkConnectivityManager.isConnected()) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = networkConnectivityManager.getNetworkErrorMessage()
                )
                return@launch
            }

            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            val result = updateUserProfileUseCase(currentUser.id, form)
            
            result.fold(
                onSuccess = { user ->
                    _authState.value = _authState.value.copy(
                        user = user,
                        isLoading = false,
                        error = null,
                        successMessage = "Profile updated successfully!"
                    )
                },
                onFailure = { exception ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = if (!networkConnectivityManager.isConnected()) {
                            networkConnectivityManager.getNetworkErrorMessage()
                        } else {
                            exception.message
                        }
                    )
                }
            )
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            val result = resetPasswordUseCase(email)
            
            result.fold(
                onSuccess = {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = null,
                        isPasswordResetSent = true,
                        successMessage = "Password reset email sent! Check your inbox."
                    )
                },
                onFailure = { exception ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = exception.message,
                        isPasswordResetSent = false
                    )
                }
            )
        }
    }

    fun sendEmailVerification() {
        viewModelScope.launch {
            // Check internet connectivity first
            if (!networkConnectivityManager.isConnected()) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = networkConnectivityManager.getNetworkErrorMessage()
                )
                return@launch
            }

            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            val result = sendEmailVerificationUseCase()
            
            result.fold(
                onSuccess = {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = null,
                        isEmailVerificationSent = true,
                        successMessage = "Verification email sent! Check your inbox and click the link to verify your email."
                    )
                },
                onFailure = { exception ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = if (!networkConnectivityManager.isConnected()) {
                            networkConnectivityManager.getNetworkErrorMessage()
                        } else {
                            "Failed to send verification email: ${exception.message ?: "Unknown error"}"
                        },
                        isEmailVerificationSent = false
                    )
                }
            )
        }
    }

    fun checkEmailVerification() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            val result = checkEmailVerificationUseCase()
            
            result.fold(
                onSuccess = { isVerified ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = null,
                        successMessage = if (isVerified) "Email verified successfully!" else null
                    )
                    
                    // If verified, refresh user data
                    if (isVerified) {
                        // Trigger auth state refresh
                        observeAuthState()
                    }
                },
                onFailure = { exception ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
    
    fun clearSuccessMessage() {
        _authState.value = _authState.value.copy(successMessage = null)
    }
    
    fun setError(message: String) {
        _authState.value = _authState.value.copy(error = message, isLoading = false)
    }
    
    fun clearFlags() {
        _authState.value = _authState.value.copy(
            isEmailVerificationSent = false,
            isPasswordResetSent = false,
            successMessage = null,
            error = null
        )
    }

    /**
     * Demonstrates using the previously unused methods:
     * - observeUserWithOfflineFallback() from AuthRepositoryImpl
     * - Various data source methods that use toDataModel(), observeUser(), deleteUser()
     */
    fun testUnusedMethods() {
        viewModelScope.launch {
            val currentUser = _authState.value.user
            if (currentUser != null) {
                try {
                    // Demonstrate observeUserWithOfflineFallback (uses observeUser() internally)
                    authRepositoryImpl.observeUserWithOfflineFallback(currentUser.id)
                        .onEach { user ->
                            // This demonstrates the offline-first approach combining local and remote data
                            // The method internally uses:
                            // - observeUser() from AuthLocalDataSource  
                            // - toDataModel() and toDomainModel() extension functions
                            // - All the previously unused mapping methods
                            
                            // Update state to show this is working
                            if (user != null) {
                                _authState.value = _authState.value.copy(
                                    user = user,
                                    error = "✅ Successfully tested unused methods: observeUser(), toDataModel(), etc."
                                )
                            }
                        }
                        .launchIn(this)
                        
                } catch (e: Exception) {
                    _authState.value = _authState.value.copy(
                        error = "❌ Error testing unused methods: ${e.message}"
                    )
                }
            } else {
                _authState.value = _authState.value.copy(
                    error = "ℹ️ No user logged in to test unused methods"
                )
            }
        }
    }
}
