package com.om.diucampusschedule.ui.screens.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.om.diucampusschedule.util.DeveloperRoutineUpload
import com.om.diucampusschedule.util.InAppMessageSetupUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isLoading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        TopAppBar(
            title = {
                Text(
                    text = "Debug Panel",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Warning Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Development Only",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This screen is for development purposes only. Use it to upload routine data to Firebase during initial setup.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        // In-App Messages Section
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "📱 In-App Messages",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Manage in-app messages for testing the messaging system.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Create Sample Messages Button
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            showResult = false
                            
                            try {
                                InAppMessageSetupUtil.createSampleMessages()
                                resultMessage = "✅ Sample in-app messages created successfully!"
                            } catch (e: Exception) {
                                resultMessage = "❌ Error creating messages: ${e.message}"
                            }
                            
                            isLoading = false
                            showResult = true
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Creating...")
                    } else {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Sample Messages")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Delete Sample Messages Button
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            showResult = false
                            
                            try {
                                InAppMessageSetupUtil.deleteSampleMessages()
                                resultMessage = "✅ Sample in-app messages deleted successfully!"
                            } catch (e: Exception) {
                                resultMessage = "❌ Error deleting messages: ${e.message}"
                            }
                            
                            isLoading = false
                            showResult = true
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Deleting...")
                    } else {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Sample Messages")
                    }
                }
            }
        }
        
        // Quick Setup Button (Recommended)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Rocket,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🚀 Quick Setup (Recommended)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Upload routine data and set version in one step. This is the easiest way to get started!",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            showResult = false
                            
                            val result = DeveloperRoutineUpload.quickSetup(context)
                            
                            result.fold(
                                onSuccess = { message ->
                                    resultMessage = message
                                },
                                onFailure = { error ->
                                    resultMessage = "❌ Error: ${error.message}"
                                }
                            )
                            
                            isLoading = false
                            showResult = true
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Setting up...")
                    } else {
                        Icon(Icons.Default.Rocket, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🚀 Quick Setup")
                    }
                }
            }
        }
        
        // Upload Routine Button
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Upload Routine Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Upload the routine data from assets/routines.json to Firebase Firestore. This will create or update the routine for Software Engineering department.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            showResult = false
                            
                            val result = DeveloperRoutineUpload.uploadRoutineDataToFirebase(context)
                            
                            result.fold(
                                onSuccess = { message ->
                                    resultMessage = message
                                },
                                onFailure = { error ->
                                    resultMessage = "❌ Error: ${error.message}"
                                }
                            )
                            
                            isLoading = false
                            showResult = true
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Uploading...")
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Only")
                    }
                }
            }
        }
        
        // Update Version Button
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Update Routine Version",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Increment the routine version to trigger sync notifications for all users. Use this when you make changes to the routine.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            showResult = false
                            
                            val result = DeveloperRoutineUpload.updateRoutineVersion("Software Engineering")
                            
                            result.fold(
                                onSuccess = { message ->
                                    resultMessage = "✅ Success: $message"
                                },
                                onFailure = { error ->
                                    resultMessage = "❌ Error: ${error.message}"
                                }
                            )
                            
                            isLoading = false
                            showResult = true
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Updating...")
                    } else {
                        Icon(Icons.Default.Update, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Update Version")
                    }
                }
            }
        }
        
        // List Routines Button
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "List All Routines",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "View all routine documents currently stored in Firebase Firestore.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            showResult = false
                            
                            val result = DeveloperRoutineUpload.listAllRoutines()
                            
                            result.fold(
                                onSuccess = { routines ->
                                    resultMessage = if (routines.isEmpty()) {
                                        "📋 No routines found in Firebase"
                                    } else {
                                        "📋 Found ${routines.size} routine(s):\n\n" + routines.joinToString("\n")
                                    }
                                },
                                onFailure = { error ->
                                    resultMessage = "❌ Error: ${error.message}"
                                }
                            )
                            
                            isLoading = false
                            showResult = true
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.List, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("List Routines")
                }
            }
        }
        
        // Delete All Routines (Danger Zone)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🗑️ Danger Zone",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Delete all routine data from Firebase. Use this to clean up during development.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            showResult = false
                            
                            val result = DeveloperRoutineUpload.deleteAllRoutines()
                            
                            result.fold(
                                onSuccess = { message ->
                                    resultMessage = "✅ Success: $message"
                                },
                                onFailure = { error ->
                                    resultMessage = "❌ Error: ${error.message}"
                                }
                            )
                            
                            isLoading = false
                            showResult = true
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete All Routines")
                }
            }
        }
        
        // Result Display
        if (showResult) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (resultMessage.startsWith("✅")) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Result",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = resultMessage,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = { showResult = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }
        
        // Instructions Card
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📝 Instructions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val instructions = listOf(
                    "1. First, tap 'Upload Routine Data' to upload your routines.json to Firebase",
                    "2. When you make changes to the routine, tap 'Update Version' to notify all users",
                    "3. Use 'List Routines' to see what's currently in your Firebase database",
                    "4. Make sure your Firebase project has Firestore enabled",
                    "5. Ensure your app has proper Firebase configuration"
                )
                
                instructions.forEach { instruction ->
                    Text(
                        text = instruction,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}
