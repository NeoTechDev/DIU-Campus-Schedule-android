package com.om.diucampusschedule.ui.screens.debug

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Simple screen to access debug functions
 * Navigate to this screen by going to: /debug in your navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugAccessScreen(navController: NavController) {
    var showConfirmation by remember { mutableStateOf(false) }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = {
                Text("Access Debug Panel?")
            },
            text = {
                Text("This will take you to the debug panel where you can upload routine data to Firebase. This should only be used during development.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmation = false
                        navController.navigate("debug")
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.DeveloperMode,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Debug Access",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Access debug tools for uploading routine data to Firebase",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { showConfirmation = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Debug Panel")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}
