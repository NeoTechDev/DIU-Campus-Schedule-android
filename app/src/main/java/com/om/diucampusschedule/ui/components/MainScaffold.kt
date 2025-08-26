package com.om.diucampusschedule.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.om.diucampusschedule.ui.navigation.Screen
import com.om.diucampusschedule.ui.theme.DIUCampusScheduleTheme

@Composable
fun MainScaffold(
    navController: NavController,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Determine if we should show the main UI (top bar + bottom nav)
    val showMainUI = when (currentRoute) {
        Screen.Welcome.route,
        Screen.SignIn.route,
        Screen.SignUp.route,
        Screen.ForgotPassword.route,
        Screen.EmailVerification.route,
        Screen.RegsitrationForm.route,
        Screen.Debug.route -> false
        else -> true
    }
    
    if (showMainUI) {
        Scaffold(
            modifier = modifier,
            topBar = {
                MainTopAppBar(
                    currentRoute = currentRoute,
                    navController = navController
                )
            },
            bottomBar = {
                // Only show bottom nav for main app screens
                if (isMainAppScreen(currentRoute)) {
                    DIUBottomNavigationBar(navController = navController)
                }
            }
        ) { paddingValues ->
            content(paddingValues)
        }
    } else {
        // For auth screens and other full-screen content
        content(PaddingValues())
    }
}

/**
 * Determines if the current route should show the bottom navigation bar
 * For now, only show bottom nav on Routine screen since it's the only implemented screen
 */
private fun isMainAppScreen(route: String?): Boolean {
    return when (route) {
        Screen.Routine.route -> true
        // TODO: Add other screens when implemented
        /*
        Screen.Today.route,
        Screen.ExamRoutine.route,
        Screen.Tasks.route,
        Screen.Profile.route -> true
        */
        else -> false
    }
}

/**
 * Alternative scaffold with custom bottom navigation design
 */
@Composable
fun CustomMainScaffold(
    navController: NavController,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val showMainUI = when (currentRoute) {
        Screen.Welcome.route,
        Screen.SignIn.route,
        Screen.SignUp.route,
        Screen.ForgotPassword.route,
        Screen.EmailVerification.route,
        Screen.RegsitrationForm.route,
        Screen.Debug.route -> false
        else -> true
    }
    
    if (showMainUI) {
        Scaffold(
            modifier = modifier,
            topBar = {
                MainTopAppBar(
                    currentRoute = currentRoute,
                    navController = navController
                )
            },
            bottomBar = {
                if (isMainAppScreen(currentRoute)) {
                    CustomBottomNavigationBar(navController = navController)
                }
            }
        ) { paddingValues ->
            content(paddingValues)
        }
    } else {
        content(PaddingValues())
    }
}

@Preview(showBackground = true)
@Composable
fun MainScaffoldPreview() {
    DIUCampusScheduleTheme {
        MainScaffold(
            navController = rememberNavController()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Text("Main Content Area")
            }
        }
    }
}
