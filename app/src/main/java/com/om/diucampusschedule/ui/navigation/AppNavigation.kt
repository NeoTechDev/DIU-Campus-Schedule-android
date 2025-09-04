package com.om.diucampusschedule.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
import com.om.diucampusschedule.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String? = null,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    
    // Determine the actual start destination based on auth state and parameter
    // Only compute this once on initial load to prevent navigation loops
    val actualStartDestination = remember(startDestination) {
        startDestination ?: Screen.Welcome.route
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Handle auth state changes for navigation
    LaunchedEffect(authState.isAuthenticated, authState.user?.isProfileComplete) {
        if (!authState.isLoading) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            
            when {
                authState.isAuthenticated && authState.user?.isProfileComplete == true -> {
                    // User is authenticated and profile is complete
                    if (currentRoute in listOf(Screen.Welcome.route, Screen.SignIn.route, Screen.SignUp.route, Screen.RegsitrationForm.route)) {
                        navController.navigate(Screen.Today.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                authState.isAuthenticated && authState.user?.isProfileComplete == false -> {
                    // User is authenticated but profile is incomplete
                    if (currentRoute in listOf(Screen.Welcome.route, Screen.SignIn.route, Screen.SignUp.route)) {
                        navController.navigate(Screen.RegsitrationForm.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                !authState.isAuthenticated -> {
                    // User is not authenticated
                    if (currentRoute !in listOf(Screen.Welcome.route, Screen.SignIn.route, Screen.SignUp.route, Screen.ForgotPassword.route, Screen.EmailVerification.route)) {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
        }
    }
    
    // Show loading state while determining auth status
    if (authState.isLoading && startDestination == null) {
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
                Text(text = "Loading...")
            }
        }
        return
    }
    
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
                startDestination = actualStartDestination,
                modifier = Modifier.padding(paddingValues)
            ) {
                // Authentication Screens (no scaffold)
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

                // Main App Screens (with scaffold)
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
