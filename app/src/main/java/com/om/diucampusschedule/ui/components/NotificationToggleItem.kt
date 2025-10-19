package com.om.diucampusschedule.ui.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.om.diucampusschedule.R
import com.om.diucampusschedule.ui.viewmodel.NotificationSettingsViewModel

/**
 * Material 3 notification toggle component for the navigation drawer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationToggleItem(
    onItemClick: () -> Unit = {},
    notificationViewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isRemindersEnabled by notificationViewModel.isClassRemindersEnabled.collectAsStateWithLifecycle()
    val hasPermissionBeenRequested by notificationViewModel.hasNotificationPermissionBeenRequested.collectAsStateWithLifecycle()
    val hasNotificationPermission by notificationViewModel.hasNotificationPermission.collectAsStateWithLifecycle()
    val isExamMode by notificationViewModel.isExamMode.collectAsStateWithLifecycle()
    val isMaintenanceMode by notificationViewModel.isMaintenanceMode.collectAsStateWithLifecycle()
    val isSemesterBreak by notificationViewModel.isSemesterBreak.collectAsStateWithLifecycle()

    
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationViewModel.markNotificationPermissionRequested()
        
        if (isGranted) {
            // Enable reminders if permission granted
            notificationViewModel.toggleClassReminders(true)
        } else {
            // Show dialog to go to settings
            showPermissionDialog = true
        }
    }
    
    // Animated colors and scale
    val iconColor by animateColorAsState(
        targetValue = if (isExamMode || isMaintenanceMode || isSemesterBreak) {
            MaterialTheme.colorScheme.tertiary // Different color for exam mode
        } else if (isRemindersEnabled && hasNotificationPermission) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(300),
        label = "icon_color"
    )
    
    val iconScale by animateFloatAsState(
        targetValue = if (isRemindersEnabled && hasNotificationPermission) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "icon_scale"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onItemClick()
                if (hasNotificationPermission || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    // Permission available, toggle the setting
                    notificationViewModel.toggleClassReminders(!isRemindersEnabled)
                } else {
                    // Request permission first
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (!hasPermissionBeenRequested) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            // Show dialog to go to settings
                            showPermissionDialog = true
                        }
                    }
                }
            }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (isExamMode || isMaintenanceMode || isSemesterBreak) {
                    Icons.Default.NotificationsOff // Always off during exam mode
                } else if (isRemindersEnabled && hasNotificationPermission) {
                    Icons.Default.Notifications
                } else {
                    Icons.Default.NotificationsOff
                },
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .size(24.dp)
                    .scale(iconScale)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Class Reminders",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = when {
                        isExamMode -> "Disabled during exam period"
                        isMaintenanceMode -> "Disabled during maintenance period"
                        isSemesterBreak -> "Disabled during semester break"
                        hasNotificationPermission || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> {
                            if (isRemindersEnabled) "Get notified 30 minutes before class" 
                            else "No class notifications"
                        }
                        else -> "Tap to enable notifications"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isExamMode || isMaintenanceMode || isSemesterBreak) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Material 3 Switch
        if (hasNotificationPermission || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Switch(
                checked = isRemindersEnabled && !isExamMode && !isMaintenanceMode && !isSemesterBreak,
                onCheckedChange = { enabled ->
                    if (!isExamMode && !isMaintenanceMode) {
                        notificationViewModel.toggleClassReminders(enabled)
                    }
                },
                enabled = !isExamMode && !isMaintenanceMode && !isSemesterBreak,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = if (isExamMode || isMaintenanceMode || isSemesterBreak) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                    checkedTrackColor = if (isExamMode || isMaintenanceMode || isSemesterBreak) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledCheckedThumbColor = MaterialTheme.colorScheme.outline,
                    disabledCheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledUncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        } else {
            // Show enable button for permission
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.single_reminder),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
    
    // Permission dialog
    if (showPermissionDialog) {
        BasicAlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            modifier = Modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp)),
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Enable Notifications",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "To receive class reminders, please enable notifications in your device settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showPermissionDialog = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            showPermissionDialog = false
                            // Open app settings
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Settings")
                    }
                }
            }
        }
    }
}
