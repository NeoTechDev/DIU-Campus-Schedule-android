package com.om.diucampusschedule

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.om.diucampusschedule.domain.model.AppState
import com.om.diucampusschedule.core.permission.NotificationPermissionHandler
import com.om.diucampusschedule.ui.navigation.AppNavigation
import com.om.diucampusschedule.ui.navigation.Screen
import com.om.diucampusschedule.ui.theme.DIUCampusScheduleTheme
import com.om.diucampusschedule.ui.viewmodel.AppInitializationViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val appInitializationViewModel: AppInitializationViewModel by viewModels()
    
    @Inject
    lateinit var notificationPermissionHandler: NotificationPermissionHandler
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate()
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize notification permission handler
        notificationPermissionHandler.initialize(this)
        
        // Keep splash screen visible until app state is determined
        splashScreen.setKeepOnScreenCondition {
            appInitializationViewModel.appState.value is AppState.Initializing
        }
        
        // Handle navigation from notifications or deep links
        val navigateTo = intent.getStringExtra("navigate_to")
        val startDestination = when (navigateTo) {
            "today" -> Screen.Today.route
            "routine" -> Screen.Routine.route
            "empty_rooms" -> Screen.EmptyRooms.route
            "tasks" -> Screen.Tasks.route
            "notes" -> Screen.Notes.route
            "profile" -> Screen.Profile.route
            "faculty_info" -> Screen.FacultyInfo.route
            "community" -> Screen.Community.route
            else -> null // Let AppInitializationViewModel determine the appropriate start destination
        }
        
        setContent {
            DIUCampusScheduleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    val appState by appInitializationViewModel.appState.collectAsStateWithLifecycle()
                    
                    // Handle notification permission request after app initialization
                    LaunchedEffect(appState) {
                        if (appState is AppState.AuthenticatedComplete) {
                            // Request notification permissions if not already granted or requested
                            notificationPermissionHandler.requestNotificationPermissionIfNeeded()
                        }
                    }
                    
                    AppNavigation(
                        navController = navController,
                        appState = appState,
                        startDestination = startDestination,
                        appInitializationViewModel = appInitializationViewModel
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