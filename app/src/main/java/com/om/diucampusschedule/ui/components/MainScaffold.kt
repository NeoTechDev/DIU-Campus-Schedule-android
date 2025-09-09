package com.om.diucampusschedule.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.om.diucampusschedule.ui.firebase.InAppMessageHandler
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
    
    // Handle back button to always go to Today screen from other main app screens
    if (showMainUI && isMainAppScreen(currentRoute) && currentRoute != Screen.Today.route) {
        BackHandler {
            navController.navigate(Screen.Today.route) {
                // Clear the entire back stack and make Today the only entry
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    
    if (showMainUI) {
        Scaffold(
            modifier = modifier,
            // topBar removed - individual screens will implement their own top app bars
            bottomBar = {
                // Only show bottom nav for main app screens
                if (isMainAppScreen(currentRoute)) {
                    DIUBottomNavigationBar(navController = navController)
                }
            },
            contentWindowInsets = WindowInsets(0) // Remove all default window insets
        ) { paddingValues ->
            Box {
                content(paddingValues)
                
                // Add InAppMessageHandler for main app screens
                if (isMainAppScreen(currentRoute)) {
                    InAppMessageHandler(
                        navController = navController,
                        targetScreen = currentRoute ?: ""
                    )
                }
            }
        }
    } else {
        // For auth screens and other full-screen content
        content(PaddingValues())
    }
}

/**
 * Determines if the current route should show the bottom navigation bar
 */
private fun isMainAppScreen(route: String?): Boolean {
    return when (route) {
        Screen.Today.route,
        Screen.Routine.route,
        Screen.EmptyRooms.route,
        Screen.Tasks.route,
        Screen.Notes.route -> true
        else -> false
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
