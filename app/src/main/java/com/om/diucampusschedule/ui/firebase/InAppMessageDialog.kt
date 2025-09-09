package com.om.diucampusschedule.ui.firebase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.om.diucampusschedule.domain.model.InAppMessage
import com.om.diucampusschedule.domain.model.MessageButton
import com.om.diucampusschedule.ui.theme.customFontFamily

enum class DialogType {
    INFO, WARNING, SUCCESS, ERROR, CONFIRMATION, NEUTRAL
}

fun getDialogTypeIcon(type: DialogType): ImageVector {
    return when (type) {
        DialogType.INFO -> Icons.Default.Info
        DialogType.WARNING -> Icons.Default.Warning
        DialogType.SUCCESS -> Icons.Default.CheckCircle
        DialogType.ERROR -> Icons.Default.Warning
        DialogType.CONFIRMATION -> Icons.Default.Notifications
        DialogType.NEUTRAL -> Icons.Default.Notifications
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InAppMessageDialog(
    message: InAppMessage,
    dialogType: DialogType = DialogType.NEUTRAL,
    onButtonClick: (MessageButton) -> Unit,
    onDismiss: () -> Unit,
    dismissible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val maxWidth = (configuration.screenWidthDp * 0.9f).dp

    Dialog(
        onDismissRequest = { /* Prevent outside tap dismiss */ },
        properties = DialogProperties(
            dismissOnBackPress = dismissible,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = modifier
                .widthIn(max = maxWidth)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header with close button if dismissible
                if (dismissible) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Icon centered
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = when (dialogType) {
                            DialogType.INFO -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            DialogType.WARNING -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                            DialogType.SUCCESS -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            DialogType.ERROR -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            DialogType.CONFIRMATION -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            DialogType.NEUTRAL -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        }
                    ) {
                        Icon(
                            imageVector = getDialogTypeIcon(dialogType),
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                            tint = when (dialogType) {
                                DialogType.INFO -> MaterialTheme.colorScheme.primary
                                DialogType.WARNING -> MaterialTheme.colorScheme.tertiary
                                DialogType.SUCCESS -> MaterialTheme.colorScheme.secondary
                                DialogType.ERROR -> MaterialTheme.colorScheme.error
                                DialogType.CONFIRMATION -> MaterialTheme.colorScheme.primary
                                DialogType.NEUTRAL -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            }
                        )
                    }
                }

                // Title centered
                if (message.title.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = message.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = customFontFamily()
                    )
                }

                // Message content
                Spacer(modifier = Modifier.height(if (message.title.isNotEmpty()) 20.dp else 16.dp))

                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    fontFamily = customFontFamily()
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Action buttons
                when {
                    message.buttons.isEmpty() -> {
                        // Default OK button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = onDismiss,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = "OK",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontFamily = customFontFamily()
                                )
                            }
                        }
                    }

                    message.buttons.size == 1 -> {
                        // Single primary action
                        Button(
                            onClick = { onButtonClick(message.buttons[0]) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = message.buttons[0].text,
                                style = MaterialTheme.typography.labelLarge,
                                fontFamily = customFontFamily(),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    message.buttons.size == 2 -> {
                        // Two buttons - secondary and primary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onButtonClick(message.buttons[0]) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            ) {
                                Text(
                                    text = message.buttons[0].text,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontFamily = customFontFamily(),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            Button(
                                onClick = { onButtonClick(message.buttons[1]) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    text = message.buttons[1].text,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontFamily = customFontFamily(),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }

                    else -> {
                        // Multiple buttons - flexible layout with hierarchy
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Primary action (last button)
                            Button(
                                onClick = { onButtonClick(message.buttons.last()) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    text = message.buttons.last().text,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontFamily = customFontFamily(),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            // Secondary actions in flexible row
                            if (message.buttons.size > 1) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    message.buttons.dropLast(1).forEachIndexed { index, button ->
                                        if (index == 0 && message.buttons.size == 3) {
                                            // Second most important action
                                            FilledTonalButton(
                                                onClick = { onButtonClick(button) },
                                                shape = RoundedCornerShape(20.dp)
                                            ) {
                                                Text(
                                                    text = button.text,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontFamily = customFontFamily()
                                                )
                                            }
                                        } else {
                                            // Tertiary actions
                                            OutlinedButton(
                                                onClick = { onButtonClick(button) },
                                                shape = RoundedCornerShape(20.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                                )
                                            ) {
                                                Text(
                                                    text = button.text,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontFamily = customFontFamily()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
fun InAppMessageDialogPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Info dialog with two buttons
        InAppMessageDialog(
            message = InAppMessage(
                title = "Update Available",
                message = "A new version of the app is available with improved performance and bug fixes. Would you like to update now?",
                buttons = listOf(
                    MessageButton(text = "Later", action = "later"),
                    MessageButton(text = "Update Now", action = "update")
                )
            ),
            dialogType = DialogType.INFO,
            onButtonClick = {},
            onDismiss = {}
        )

        // Success dialog with single button
        InAppMessageDialog(
            message = InAppMessage(
                title = "Sync Complete",
                message = "Your schedule has been successfully synchronized with the campus system. All your classes are now up to date.",
                buttons = listOf(
                    MessageButton(text = "Continue", action = "continue")
                )
            ),
            dialogType = DialogType.SUCCESS,
            onButtonClick = {},
            onDismiss = {}
        )

        // Warning dialog with multiple buttons
        InAppMessageDialog(
            message = InAppMessage(
                title = "Storage Warning",
                message = "Your device is running low on storage space. This may affect app performance and data synchronization.",
                buttons = listOf(
                    MessageButton(text = "Ignore", action = "ignore"),
                    MessageButton(text = "Settings", action = "settings"),
                    MessageButton(text = "Clean Up", action = "cleanup")
                )
            ),
            dialogType = DialogType.WARNING,
            dismissible = false,
            onButtonClick = {},
            onDismiss = {}
        )

        // Confirmation dialog without title
        InAppMessageDialog(
            message = InAppMessage(
                title = "",
                message = "Are you sure you want to delete this schedule? This action cannot be undone.",
                buttons = listOf(
                    MessageButton(text = "Cancel", action = "cancel"),
                    MessageButton(text = "Delete", action = "delete")
                )
            ),
            dialogType = DialogType.ERROR,
            onButtonClick = {},
            onDismiss = {}
        )
    }
}