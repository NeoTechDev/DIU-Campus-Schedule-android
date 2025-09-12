package com.om.diucampusschedule.domain.model

/**
 * Represents the overall application state during initialization
 */
sealed interface AppState {
    /**
     * App is initializing (checking auth, loading user data, etc.)
     */
    data object Initializing : AppState
    
    /**
     * User is not authenticated - should show onboarding/welcome flow
     */
    data object Unauthenticated : AppState
    
    /**
     * User is authenticated but email not verified - should show email verification flow
     */
    data class AuthenticatedEmailUnverified(val user: User) : AppState
    
    /**
     * User is authenticated and email verified but profile setup is incomplete
     */
    data class AuthenticatedIncomplete(val user: User) : AppState
    
    /**
     * User is fully authenticated and ready to use the app
     */
    data class AuthenticatedComplete(val user: User) : AppState
    
    /**
     * An error occurred during initialization
     */
    data class Error(val message: String) : AppState
}
