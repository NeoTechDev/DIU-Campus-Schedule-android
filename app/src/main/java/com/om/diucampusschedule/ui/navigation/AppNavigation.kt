package com.om.diucampusschedule.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.om.diucampusschedule.domain.model.AppState
import com.om.diucampusschedule.ui.components.MainScaffold
import com.om.diucampusschedule.ui.components.NavigationDrawer
import com.om.diucampusschedule.ui.screens.auth.EmailVerificationScreen
import com.om.diucampusschedule.ui.screens.auth.ForgotPasswordScreen
import com.om.diucampusschedule.ui.screens.auth.RegistrationFormScreen
import com.om.diucampusschedule.ui.screens.auth.SignInScreen
import com.om.diucampusschedule.ui.screens.auth.SignUpScreen
import com.om.diucampusschedule.ui.screens.community.CommunityScreen
import com.om.diucampusschedule.ui.screens.debug.DebugScreen
import com.om.diucampusschedule.ui.screens.emptyrooms.EmptyRoomsScreen
import com.om.diucampusschedule.ui.screens.facultyinfo.FacultyInfoScreen
import com.om.diucampusschedule.ui.screens.notes.NoteEditorScreen
import com.om.diucampusschedule.ui.screens.notes.NotesScreen
import com.om.diucampusschedule.ui.screens.profile.ProfileScreen
import com.om.diucampusschedule.ui.screens.routine.RoutineScreen
import com.om.diucampusschedule.ui.screens.tasks.TaskScreen
import com.om.diucampusschedule.ui.screens.today.TodayScreen
import com.om.diucampusschedule.ui.screens.welcome.WelcomeScreen
import com.om.diucampusschedule.ui.viewmodel.AppInitializationViewModel
import com.om.diucampusschedule.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

/**
 * Main navigation component that handles app-wide navigation based on app state
 * 
 * @param navController Navigation controller for handling navigation
 * @param appState Current application state
 * @param startDestination Optional start destination for deep linking
 * @param appInitializationViewModel ViewModel for app initialization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    appState: AppState,
    startDestination: String? = null,
    appInitializationViewModel: AppInitializationViewModel = hiltViewModel()
) {
    when (appState) {
        is AppState.Initializing -> {
            // Show loading screen while app initializes
            LoadingScreen()
        }
        
        is AppState.Unauthenticated -> {
            // Show authentication flow
            AuthNavigationHost(
                navController = navController,
                startDestination = startDestination ?: Screen.Welcome.route
            )
        }
        
        is AppState.AuthenticatedIncomplete -> {
            // Show profile completion flow
            AuthNavigationHost(
                navController = navController,
                startDestination = startDestination ?: Screen.RegsitrationForm.route
            )
        }
        
        is AppState.AuthenticatedComplete -> {
            // Show main app flow
            MainAppNavigationHost(
                navController = navController,
                startDestination = startDestination ?: Screen.Today.route
            )
        }
        
        is AppState.Error -> {
            // Show error screen with retry option
            ErrorScreen(
                message = appState.message,
                onRetry = {
                    // Retry app initialization
                    appInitializationViewModel.refreshAppState()
                }
            )
        }
    }
}

/**
 * Loading screen shown during app initialization
 */
@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Error screen with retry functionality
 */
@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

/**
 * Navigation host for authentication flow
 */
@Composable
private fun AuthNavigationHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Authentication Screens
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController)
        }

        composable(Screen.SignIn.route) {
            SignInScreen(navController = navController)
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }

        composable("${Screen.EmailVerification.route}/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            EmailVerificationScreen(
                navController = navController,
                userEmail = email
            )
        }

        composable(Screen.RegsitrationForm.route) {
            RegistrationFormScreen(navController = navController)
        }
    }
}

/**
 * Navigation host for main app flow with scaffold
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainAppNavigationHost(
    navController: NavHostController,
    startDestination: String
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        drawerContent = {
            NavigationDrawer(
                onNavigate = { route ->
                    navController.navigate(route)
                    scope.launch { drawerState.close() }
                },
                onCloseDrawer = {
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    ) {
        MainScaffold(navController = navController) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(paddingValues)
            ) {
                // Main App Screens
                composable(Screen.Today.route) {
                    TodayScreen(
                        navController = navController,
                        onOpenDrawer = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }
                
                composable(Screen.Routine.route) {
                    RoutineScreen()
                }
                
                composable(Screen.EmptyRooms.route) {
                    EmptyRoomsScreen()
                }
                
                composable(Screen.Tasks.route) {
                    TaskScreen(navController = navController)
                }
                
                composable(Screen.Notes.route) {
                    NotesScreen(navController = navController)
                }

                // Note Editor Screen - supports both create new note and edit existing note
                composable("note_editor?noteId={noteId}") { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull()
                    NoteEditorScreen(
                        navController = navController,
                        noteId = noteId
                    )
                }
                
                composable("note_editor") {
                    NoteEditorScreen(
                        navController = navController,
                        noteId = null
                    )
                }

                // Profile Screen
                composable(Screen.Profile.route) {
                    ProfileScreen(navController = navController)
                }
                
                // Placeholder for unimplemented screens
                composable(Screen.ExamRoutine.route) {
                    PlaceholderScreen(title = "Exam Routine", description = "Exam schedules will be displayed here")
                }
                
                composable(Screen.FacultyInfo.route) {
                    FacultyInfoScreen(onBack = navController)
                }

                composable(Screen.Community.route){
                    CommunityScreen(navController = navController)
                }

                composable(Screen.Debug.route) {
                    DebugScreen(navController = navController)
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
