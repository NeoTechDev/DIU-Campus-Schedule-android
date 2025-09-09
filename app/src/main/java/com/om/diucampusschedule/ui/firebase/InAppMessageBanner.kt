package com.om.diucampusschedule.ui.firebase


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.om.diucampusschedule.domain.model.InAppMessage
import com.om.diucampusschedule.domain.model.MessageButton
import com.om.diucampusschedule.ui.theme.customFontFamily

enum class MessageType {
    INFO, WARNING, SUCCESS, ERROR, NEUTRAL
}

fun getMessageTypeIcon(type: MessageType): ImageVector {
    return when (type) {
        MessageType.INFO -> Icons.Default.Info
        MessageType.WARNING -> Icons.Default.Warning
        MessageType.SUCCESS -> Icons.Default.Notifications
        MessageType.ERROR -> Icons.Default.Warning
        MessageType.NEUTRAL -> Icons.Default.Notifications
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InAppMessageBanner(
    message: InAppMessage,
    isVisible: Boolean,
    messageType: MessageType = MessageType.NEUTRAL,
    onButtonClick: (MessageButton) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300)
        ) + expandVertically(
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(250)
        ) + shrinkVertically(
            animationSpec = tween(250)
        ) + fadeOut(animationSpec = tween(250))
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header section with icon, title, and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Message type indicator
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = when (messageType) {
                            MessageType.INFO -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            MessageType.WARNING -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                            MessageType.SUCCESS -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            MessageType.ERROR -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            MessageType.NEUTRAL -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        }
                    ) {
                        Icon(
                            imageVector = getMessageTypeIcon(messageType),
                            contentDescription = null,
                            modifier = Modifier.padding(6.dp),
                            tint = when (messageType) {
                                MessageType.INFO -> MaterialTheme.colorScheme.primary
                                MessageType.WARNING -> MaterialTheme.colorScheme.tertiary
                                MessageType.SUCCESS -> MaterialTheme.colorScheme.secondary
                                MessageType.ERROR -> MaterialTheme.colorScheme.error
                                MessageType.NEUTRAL -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title and message content
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (message.title.isNotEmpty()) {
                            Text(
                                text = message.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = customFontFamily()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Text(
                            text = message.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                            fontFamily = customFontFamily()
                        )
                    }

                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Action buttons
                if (message.buttons.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))

                    when {
                        message.buttons.size == 1 -> {
                            // Single primary action
                            Button(
                                onClick = { onButtonClick(message.buttons[0]) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth()
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
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text(
                                        text = message.buttons[0].text,
                                        style = MaterialTheme.typography.labelMedium,
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
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = customFontFamily(),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        else -> {
                            // Multiple buttons - flexible layout
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                message.buttons.forEachIndexed { index, button ->
                                    when (index) {
                                        0 -> {
                                            // Primary action
                                            Button(
                                                onClick = { onButtonClick(button) },
                                                shape = RoundedCornerShape(20.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            ) {
                                                Text(
                                                    text = button.text,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontFamily = customFontFamily()
                                                )
                                            }
                                        }
                                        1 -> {
                                            // Secondary action
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
                                        }
                                        else -> {
                                            // Tertiary actions
                                            OutlinedButton(
                                                onClick = { onButtonClick(button) },
                                                shape = RoundedCornerShape(20.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            ) {
                                                Text(
                                                    text = button.text,
                                                    style = MaterialTheme.typography.labelSmall,
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
fun InAppMessageBannerPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        // Info message with single button
        InAppMessageBanner(
            message = InAppMessage(
                title = "New Feature Available",
                message = "Check out our latest update with improved performance and new customization options.",
                buttons = listOf(
                    MessageButton(
                        text = "Learn More",
                        action = "learn_more"
                    )
                )
            ),
            isVisible = true,
            messageType = MessageType.INFO,
            onButtonClick = {},
            onDismiss = {}
        )

        // Warning message with two buttons
        InAppMessageBanner(
            message = InAppMessage(
                title = "Storage Almost Full",
                message = "Your device storage is running low. Consider removing unused files to improve app performance.",
                buttons = listOf(
                    MessageButton(
                        text = "Later",
                        action = "later"
                    ),
                    MessageButton(
                        text = "Clean Up",
                        action = "cleanup"
                    )
                )
            ),
            isVisible = true,
            messageType = MessageType.WARNING,
            onButtonClick = {},
            onDismiss = {}
        )

        // Success message without title
        InAppMessageBanner(
            message = InAppMessage(
                title = "",
                message = "Your schedule has been successfully synced with the campus system. All classes are up to date.",
                buttons = listOf()
            ),
            isVisible = true,
            messageType = MessageType.SUCCESS,
            onButtonClick = {},
            onDismiss = {}
        )

        // Multiple buttons example
        InAppMessageBanner(
            message = InAppMessage(
                title = "Customize Your Experience",
                message = "Choose your preferred settings to get the most out of the app.",
                buttons = listOf(
                    MessageButton(text = "Get Started", action = "start"),
                    MessageButton(text = "Import Settings", action = "import"),
                    MessageButton(text = "Skip for Now", action = "skip"),
                    MessageButton(text = "Help", action = "help")
                )
            ),
            isVisible = true,
            messageType = MessageType.NEUTRAL,
            onButtonClick = {},
            onDismiss = {}
        )
    }
}