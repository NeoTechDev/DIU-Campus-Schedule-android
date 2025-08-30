package com.om.diucampusschedule.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.om.diucampusschedule.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadOptionsBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onPdfClick: () -> Unit,
    onImageClick: () -> Unit,
    isPdfGenerating: Boolean = false,
    isImageGenerating: Boolean = false
) {
    if (isVisible) {
        val bottomSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = bottomSheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            Color.Gray.copy(alpha = 0.3f),
                            RoundedCornerShape(2.dp)
                        )
                )
            },

            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Share Routine",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose your preferred format",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    IconButton(
                        onClick = onDismiss
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_close),
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Options Column
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // PDF Option
                    DownloadOptionCard(
                        icon = ImageVector.vectorResource(id = R.drawable.file24),
                        title = "Share as PDF",
                        subtitle = "Portable Document Format • Perfect for printing and sharing",
                        color = MaterialTheme.colorScheme.primary,
                        isLoading = isPdfGenerating,
                        enabled = !isPdfGenerating && !isImageGenerating,
                        onClick = {
                            onPdfClick()
                            onDismiss()
                        }
                    )

                    // Image Option
                    DownloadOptionCard(
                        icon = ImageVector.vectorResource(id = R.drawable.picture_24),
                        title = "Share as Image",
                        subtitle = "High-quality JPEG • Super easy to share on Messenger or WhatsApp",
                        color = Color(0xFF4CAF50),
                        isLoading = isImageGenerating,
                        enabled = !isPdfGenerating && !isImageGenerating,
                        onClick = {
                            onImageClick()
                            onDismiss()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DownloadOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled && !isLoading) 1f else 0.95f,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "card_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = if (enabled) {
                        listOf(
                            color.copy(alpha = 0.05f),
                            color.copy(alpha = 0.08f)
                        )
                    } else {
                        listOf(
                            Color.Gray.copy(alpha = 0.03f),
                            Color.Gray.copy(alpha = 0.05f)
                        )
                    }
                )
            )
            .border(
                width = 2.dp,
                color = if (enabled) color.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(enabled = enabled && !isLoading) { onClick() }
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icon Container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = if (enabled) {
                                listOf(
                                    color.copy(alpha = 0.15f),
                                    color.copy(alpha = 0.25f)
                                )
                            } else {
                                listOf(
                                    Color.Gray.copy(alpha = 0.1f),
                                    Color.Gray.copy(alpha = 0.2f)
                                )
                            }
                        ),
                        shape = CircleShape
                    )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = color,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (enabled) color else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Text Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isLoading) "Generating..." else title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                        text = if (isLoading) "Please wait while we process your request..." else subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Normal
                        ),
                        color = if (enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else Color.Gray
                    )
            }

            // Indicator Arrow
            if (enabled && !isLoading) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
