package com.om.diucampusschedule.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.om.diucampusschedule.ui.components.MainScaffold
import com.om.diucampusschedule.ui.screens.auth.EmailVerificationScreen
import com.om.diucampusschedule.ui.screens.auth.ForgotPasswordScreen
import com.om.diucampusschedule.ui.screens.auth.RegistrationFormScreen
import com.om.diucampusschedule.ui.screens.auth.SignInScreen
import com.om.diucampusschedule.ui.screens.auth.SignUpScreen
import com.om.diucampusschedule.ui.screens.routine.RoutineScreen
import com.om.diucampusschedule.ui.screens.welcome.WelcomeScreen
import com.om.diucampusschedule.ui.screens.debug.DebugScreen
import com.om.diucampusschedule.ui.viewmodel.AuthViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String? = null,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    
    // Determine the actual start destination based on auth state and parameter
    val actualStartDestination = remember(authState, startDestination) {
        when {
            startDestination != null -> startDestination
            authState.isAuthenticated && authState.user?.isProfileComplete == true -> Screen.Routine.route
            authState.isAuthenticated && authState.user?.isProfileComplete == false -> Screen.RegsitrationForm.route
            else -> Screen.Welcome.route
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
            composable(Screen.Routine.route) {
                RoutineScreen(navController = navController)
            }

            // TODO: Add other screens when they are implemented
            /*
            composable(Screen.Today.route) {
                TodayScreen(navController = navController)
            }
            
            composable(Screen.ExamRoutine.route) {
                PlaceholderScreen(title = "Exam Routine", description = "Exam schedules will be displayed here")
            }
            
            composable(Screen.Tasks.route) {
                PlaceholderScreen(title = "Tasks", description = "Your tasks and reminders will be displayed here")
            }
            
            composable(Screen.Notes.route) {
                PlaceholderScreen(title = "Notes", description = "Your notes will be displayed here")
            }
            
            composable(Screen.EmptyRooms.route) {
                PlaceholderScreen(title = "Empty Rooms", description = "Available rooms will be displayed here")
            }
            
            composable(Screen.FacultyInfo.route) {
                PlaceholderScreen(title = "Faculty Info", description = "Faculty information will be displayed here")
            }
            
            composable(Screen.Profile.route) {
                PlaceholderScreen(title = "Profile", description = "Your profile will be displayed here")
            }
            */

            composable(Screen.Debug.route) {
                DebugScreen(navController = navController)
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
