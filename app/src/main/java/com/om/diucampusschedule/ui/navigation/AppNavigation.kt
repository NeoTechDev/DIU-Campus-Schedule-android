package com.om.diucampusschedule.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.om.diucampusschedule.ui.screens.auth.EmailVerificationScreen
import com.om.diucampusschedule.ui.screens.auth.ForgotPasswordScreen
import com.om.diucampusschedule.ui.screens.auth.RegistrationFormScreen
import com.om.diucampusschedule.ui.screens.auth.SignInScreen
import com.om.diucampusschedule.ui.screens.auth.SignUpScreen
import com.om.diucampusschedule.ui.screens.routine.RoutineScreen
import com.om.diucampusschedule.ui.screens.today.TodayScreen
import com.om.diucampusschedule.ui.screens.welcome.WelcomeScreen
import com.om.diucampusschedule.ui.screens.debug.DebugScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Welcome.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
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

        composable(Screen.Today.route) {
            TodayScreen(navController = navController)
        }

        composable(Screen.Routine.route) {
            RoutineScreen(navController = navController)
        }

        composable(Screen.Debug.route) {
            DebugScreen(navController = navController)
        }

        // TODO: Add other main app screen destinations as they are implemented
        // composable(Screen.ExamRoutine.route) { ExamRoutineScreen(navController) }
        // composable(Screen.Tasks.route) { TasksScreen(navController) }
        // composable(Screen.Notes.route) { NotesScreen(navController) }
        // composable(Screen.EmptyRooms.route) { EmptyRoomsScreen(navController) }
        // composable(Screen.FacultyInfo.route) { FacultyInfoScreen(navController) }
        // composable(Screen.Profile.route) { ProfileScreen(navController) }
    }
}
