package com.om.diucampusschedule.ui.screens.today.components

import com.om.diucampusschedule.ui.theme.customFontFamily
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Deep color palette for break time card
val BreakBackgroundColor = Color(0xFFC73659).copy(alpha = 0.8f) // Deep teal background with further reduced opacity
val BreakTextColor = Color(0xFFFFFFFF).copy(alpha = 0.8f)        // White text

@Composable
fun BreakTimeCard(
    breakText: String,
    subText: String,
    startTime: LocalTime,
    endTime: LocalTime,
    modifier: Modifier = Modifier,
    formatter12HourUS: DateTimeFormatter? = null,
    isToday: Boolean = false
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    // Check if break is currently ongoing
    val isOngoing = isToday && isBreakTimeOngoing(startTime, endTime)

    // Create pulsating animation for the Now tag
    val infiniteTransition = rememberInfiniteTransition(label = "nowTag")
    val tagAnimation = infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tagPulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(vertical = 4.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left time slot section
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(90.dp)
                    .background(
                        color = BreakBackgroundColor,
                        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 4.dp, horizontal = 7.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                            initialOffsetY = { -20 },
                            animationSpec = tween(300)
                        )
                    ) {
                        Text(
                            text = startTime.format(formatter12HourUS ?: DateTimeFormatter.ofPattern("hh:mm a")),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f),
                                fontFamily = customFontFamily()
                            ),
                            textAlign = TextAlign.Start,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Vertical gradient divider line
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(300, delayMillis = 100))
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(28.dp)
                                .background(
                                    Color.White.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(2.dp)
                                )
                                .align(Alignment.Start)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                            initialOffsetY = { 20 },
                            animationSpec = tween(300)
                        )
                    ) {
                        Text(
                            text = endTime.format(formatter12HourUS ?: DateTimeFormatter.ofPattern("hh:mm a")),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f),
                                fontFamily = customFontFamily()
                            ),
                            textAlign = TextAlign.Start,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // White divider between sections
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .height(90.dp)
                    .background(MaterialTheme.colorScheme.background)
            )

            // Right content section
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
                    .background(
                        BreakBackgroundColor,
                        RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left content (icon and text)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Icon
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(animationSpec = tween(300))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Coffee,
                                contentDescription = "Break Icon",
                                tint = BreakTextColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Content Column
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Break Title
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                                    initialOffsetY = { -20 },
                                    animationSpec = tween(300)
                                )
                            ) {
                                Text(
                                    text = breakText.uppercase(),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        letterSpacing = 0.5.sp,
                                        fontFamily = customFontFamily()
                                    ),
                                    color = BreakTextColor
                                )
                            }

                            // Sub-text
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(animationSpec = tween(300, delayMillis = 100))
                            ) {
                                Text(
                                    text = subText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        fontFamily = customFontFamily()
                                    ),
                                    color = BreakTextColor.copy(alpha = 0.7f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Right side - Duration display
                    val duration = Duration.between(startTime, endTime)
                    val hours = duration.toHours()
                    val minutes = duration.toMinutes() % 60
                    val durationText = when {
                        hours > 0 && minutes > 0 -> "${hours}h ${minutes}min"
                        hours > 0 -> "${hours}h"
                        else -> "${minutes}min"
                    }

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(300, delayMillis = 200))
                    ) {
                        Text(
                            text = durationText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                fontFamily = customFontFamily()
                            ),
                            color = BreakTextColor,
                            modifier = Modifier
                                .background(
                                    color = BreakTextColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Now tag in upper right corner for ongoing breaks
        if (isOngoing) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 6.dp)
                    .graphicsLayer {
                        alpha = tagAnimation.value
                        scaleX = tagAnimation.value
                        scaleY = tagAnimation.value
                    }
                    .background(
                        Color(0xFF4CAF50), // Green color for break time
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Now",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = customFontFamily()
                    )
                )
            }
        }
    }
}

// Function to check if break time is currently ongoing
private fun isBreakTimeOngoing(startTime: LocalTime, endTime: LocalTime): Boolean {
    val currentTime = LocalTime.now()
    return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime)
}