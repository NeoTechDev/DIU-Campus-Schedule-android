package com.om.diucampusschedule

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.om.diucampusschedule.ui.navigation.AppNavigation
import com.om.diucampusschedule.ui.theme.DIUCampusScheduleTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Handle navigation from notifications
        val navigateTo = intent.getStringExtra("navigate_to")
        val startDestination = when (navigateTo) {
            "routine" -> "routine"
            else -> "welcome"
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
        intent.getStringExtra("navigate_to")?.let { destination ->
            // You can add navigation logic here if needed
        }
    }
}