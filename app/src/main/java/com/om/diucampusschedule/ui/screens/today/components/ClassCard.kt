package com.om.diucampusschedule.ui.screens.today.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.ui.viewmodel.ClassStatus
import java.time.LocalTime

@Composable
fun ClassCard(
    routineItem: RoutineItem,
    courseName: String = routineItem.courseCode, // Default to course code if name not provided
    status: ClassStatus = ClassStatus.UPCOMING,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "card_scale"
    )
    
    val cardColor by animateColorAsState(
        targetValue = when (status) {
            ClassStatus.ONGOING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ClassStatus.COMPLETED -> MaterialTheme.colorScheme.outline.copy(alpha = 0.05f)
            ClassStatus.UPCOMING -> MaterialTheme.colorScheme.surface
            ClassStatus.UNKNOWN -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(300),
        label = "card_color"
    )
    
    val borderColor by animateColorAsState(
        targetValue = when (status) {
            ClassStatus.ONGOING -> MaterialTheme.colorScheme.primary
            ClassStatus.COMPLETED -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ClassStatus.UPCOMING -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            ClassStatus.UNKNOWN -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        },
        animationSpec = tween(300),
        label = "border_color"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = when (status) {
                ClassStatus.ONGOING -> 6.dp
                ClassStatus.UPCOMING -> 2.dp
                ClassStatus.COMPLETED -> 1.dp
                ClassStatus.UNKNOWN -> 2.dp
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (status == ClassStatus.ONGOING) 2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with course name, code and status indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = courseName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = when (status) {
                                ClassStatus.ONGOING -> MaterialTheme.colorScheme.primary
                                ClassStatus.COMPLETED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Text(
                            text = routineItem.courseCode,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = when (status) {
                                ClassStatus.ONGOING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                ClassStatus.COMPLETED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    StatusIndicator(status = status)
                }
                
                // Time information
                InfoChip(
                    icon = Icons.Default.AccessTime,
                    text = routineItem.time,
                    iconTint = when (status) {
                        ClassStatus.ONGOING -> MaterialTheme.colorScheme.primary
                        ClassStatus.COMPLETED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                // Room and teacher info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoChip(
                        icon = Icons.Default.LocationOn,
                        text = routineItem.room,
                        modifier = Modifier.weight(1f),
                        iconTint = when (status) {
                            ClassStatus.ONGOING -> MaterialTheme.colorScheme.primary
                            ClassStatus.COMPLETED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    InfoChip(
                        icon = Icons.Default.Person,
                        text = routineItem.teacherInitial,
                        modifier = Modifier.weight(1f),
                        iconTint = when (status) {
                            ClassStatus.ONGOING -> MaterialTheme.colorScheme.primary
                            ClassStatus.COMPLETED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                // Duration and section info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Duration: ${routineItem.duration}",
                        style = MaterialTheme.typography.bodySmall,
                        color = when (status) {
                            ClassStatus.COMPLETED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    Text(
                        text = "${routineItem.batch}-${routineItem.section}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = when (status) {
                            ClassStatus.ONGOING -> MaterialTheme.colorScheme.primary
                            ClassStatus.COMPLETED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                // Time until class (only for upcoming classes)
                if (status == ClassStatus.UPCOMING) {
                    val timeUntil = getTimeUntil(routineItem.startTime)
                    if (timeUntil.isNotEmpty()) {
                        Text(
                            text = "Starts in $timeUntil",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusIndicator(
    status: ClassStatus,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (status) {
        ClassStatus.ONGOING -> MaterialTheme.colorScheme.primary to "ONGOING"
        ClassStatus.COMPLETED -> MaterialTheme.colorScheme.outline to "COMPLETED"
        ClassStatus.UPCOMING -> MaterialTheme.colorScheme.tertiary to "UPCOMING"
        ClassStatus.UNKNOWN -> MaterialTheme.colorScheme.outline to ""
    }
    
    if (text.isNotEmpty()) {
        Box(
            modifier = modifier
                .background(
                    color.copy(alpha = 0.15f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = color
            )
        }
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = iconTint
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun getTimeUntil(startTime: LocalTime?): String {
    if (startTime == null) return ""
    
    val now = LocalTime.now()
    if (now.isAfter(startTime)) return ""
    
    val duration = java.time.Duration.between(now, startTime)
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "Starting soon"
    }
}
