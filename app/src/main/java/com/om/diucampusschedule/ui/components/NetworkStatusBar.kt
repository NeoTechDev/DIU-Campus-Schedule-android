package com.om.diucampusschedule.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.om.diucampusschedule.core.network.rememberConnectivityState
import kotlinx.coroutines.delay

@Composable
fun NetworkStatusBar(
    modifier: Modifier = Modifier,
    showWhenConnected: Boolean = false
) {
    val isConnected = rememberConnectivityState()
    var showConnectedMessage by remember { mutableStateOf(false) }

    // Show "Connected" message briefly when connection is restored
    LaunchedEffect(isConnected) {
        if (isConnected && showWhenConnected) {
            showConnectedMessage = true
            delay(2000) // Show for 2 seconds
            showConnectedMessage = false
        }
    }

    AnimatedVisibility(
        visible = !isConnected || showConnectedMessage,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isConnected) {
                    Color(0xFF4CAF50) // Green for connected
                } else {
                    Color(0xFFF44336) // Red for disconnected
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isConnected) {
                        "Internet Connected"
                    } else {
                        "No Internet Connection"
                    },
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun NetworkStatusMessage(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isConnected) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "No Internet Connection",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Please check your network settings and try again.",
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectionAwareContent(
    content: @Composable (isConnected: Boolean) -> Unit
) {
    val isConnected = rememberConnectivityState()
    
    Column {
        NetworkStatusBar(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            showWhenConnected = true
        )
        content(isConnected)
    }
}
