package com.om.diucampusschedule

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.om.diucampusschedule.ui.navigation.AppNavigation
import com.om.diucampusschedule.ui.navigation.Screen
import com.om.diucampusschedule.ui.theme.DIUCampusScheduleTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        
        // Handle navigation from notifications or deep links
        val navigateTo = intent.getStringExtra("navigate_to")
        val startDestination = when (navigateTo) {
            "routine" -> Screen.Routine.route
            // TODO: Add other navigation destinations when screens are implemented
            /*
            "today" -> Screen.Today.route
            "tasks" -> Screen.Tasks.route
            "exams" -> Screen.ExamRoutine.route
            "profile" -> Screen.Profile.route
            "notes" -> Screen.Notes.route
            "rooms" -> Screen.EmptyRooms.route
            "faculty" -> Screen.FacultyInfo.route
            */
            else -> null // Let AuthViewModel determine the appropriate start destination
        }
        
        setContent {
            DIUCampusScheduleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        
        // Handle navigation from notifications when app is already running
        intent.getStringExtra("navigate_to")?.let { _ ->
            // TODO: Implement navigation to specific destination when app is already running
            // This can be handled by exposing the NavController through a shared state
            // or using a navigation event system
        }
    }
}