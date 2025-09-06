package com.om.diucampusschedule.ui.screens.today.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.diucampusschedule.R
import com.om.diucampusschedule.ui.theme.customFontFamily
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun RoutineCard(
    routine: ClassRoutine,
    selectedDate: LocalDate = LocalDate.now(),
    formatter12HourUS: DateTimeFormatter? = null,
    isToday: Boolean = false
) {
    val courseName = CourseUtils.getCourseName(routine.courseCode) ?: routine.courseCode

    // Create a better hashing for consistent but unique colors per course code
    val colorSeed = routine.courseCode.sumOf { it.code } // Sum of char codes for more variety

    // Predefined pastel colors (more visually distinct)
    val pastelColors = listOf(
        Color(0xFFB3E5FC), // Light Blue
        Color(0xFFFFCCBC), // Light Orange
        Color(0xFFDCEDC8), // Light Green
        Color(0xFFF8BBD0), // Light Pink
        Color(0xFFE1BEE7), // Light Purple
        Color(0xFFFFECB3), // Light Amber
        Color(0xFFCFD8DC), // Light Blue Grey
        Color(0xFFD7CCC8), // Light Brown
        Color(0xFFB2DFDB), // Light Teal
        Color(0xFFC5CAE9), // Light Indigo
        Color(0xFFFFF9C4), // Light Yellow
        Color(0xFFFFCDD2)  // Light Red
    )

    // Matching vibrant accent colors for the pastel colors above
    val accentColors = listOf(
        Color(0xFF2196F3), // Blue
        Color(0xFFFF5722), // Orange
        Color(0xFF8BC34A), // Green
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFFFFC107), // Amber
        Color(0xFF607D8B), // Blue Grey
        Color(0xFF795548), // Brown
        Color(0xFF009688), // Teal
        Color(0xFF3F51B5), // Indigo
        Color(0xFFFFEB3B), // Yellow
        Color(0xFFF44336)  // Red
    )

    // Select color based on course code
    val colorIndex = (colorSeed % pastelColors.size).toInt()
    val pastelColor = pastelColors[colorIndex]
    val primaryColor = accentColors[colorIndex]

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    // Check if class is currently running
    val isRunning = isToday && isClassRunning(routine.startTime, routine.endTime)

    // Create pulsating animation for the LIVE tag
    val infiniteTransition = rememberInfiniteTransition(label = "nowTag")
    val tagAnimation = infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tagPulse"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left time slot section
                Surface(
                    modifier = Modifier
                        .width(70.dp)
                        .height(90.dp),
                    shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                    color = Color(0xFF3730A3),
                    shadowElevation = 4.dp
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
                                text = routine.startTime?.format(formatter12HourUS ?: DateTimeFormatter.ofPattern("hh:mm a")) ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 14.sp
                                ),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDDD6FE),
                                fontFamily = customFontFamily(),
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
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.tertiary
                                            )
                                        ),
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
                                text = routine.endTime?.format(formatter12HourUS ?: DateTimeFormatter.ofPattern("hh:mm a")) ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDDD6FE),
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
                            pastelColor,
                            RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
                        )
                ) {
                    // Decorative background
                    Canvas(
                        modifier = Modifier.matchParentSize()
                    ) {
                        drawDecorativeElements(
                            primaryColor = primaryColor.copy(alpha = 0.1f),
                            secondaryColor = primaryColor.copy(alpha = 0.05f)
                        )
                    }

                    // Main content
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Main card content
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Course details column
                            Column(
                                modifier = Modifier.fillMaxWidth(0.75f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Course name
                                AnimatedVisibility(
                                    visible = visible,
                                    enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                                        initialOffsetY = { -20 },
                                        animationSpec = tween(300)
                                    )
                                ) {
                                    Text(
                                        text = courseName,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            lineHeight = 17.sp,
                                            color = Color(0xFF0D2137),
                                            fontFamily = customFontFamily()
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Teacher info
                                    AnimatedVisibility(
                                        visible = visible,
                                        enter = fadeIn(animationSpec = tween(300, delayMillis = 100))
                                    ) {
                                        InfoChip(
                                            icon = R.drawable.lesson_class_24,
                                            text = routine.teacherInitial ?: "TBA",
                                            backgroundColor = Color.White.copy(alpha = 0.6f),
                                            iconColor = primaryColor
                                        )
                                    }

                                    // Batch and Section info pill
                                    routine.batch?.let { batch ->
                                        routine.section?.let { section ->
                                            AnimatedVisibility(
                                                visible = visible,
                                                enter = fadeIn(animationSpec = tween(300, delayMillis = 200))
                                            ) {
                                                InfoChip(
                                                    icon = R.drawable.users_class_24,
                                                    text = "$batch-$section",
                                                    backgroundColor = Color.White.copy(alpha = 0.6f),
                                                    iconColor = primaryColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Right info column - Room display
                            Box(
                                modifier = Modifier.padding(end = 0.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // Room info
                                    AnimatedVisibility(
                                        visible = visible,
                                        enter = fadeIn(animationSpec = tween(300, delayMillis = 300))
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            val roomNo = routine.room ?: "TBA"

                                            Icon(
                                                painter = painterResource(id = R.drawable.location_filled),
                                                contentDescription = "Room",
                                                tint = primaryColor,
                                                modifier = Modifier.size(20.dp)
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = roomNo,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = Color(0xFF34495E),
                                                    fontSize = if (roomNo.length > 5) 15.sp else 19.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = customFontFamily()
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } // End of Row

            // Now tag in upper right corner
            if (isRunning) {
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
                            primaryColor,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Now",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = customFontFamily()
                        )
                    )
                }
            }
        } // End of Box
    }
}

@Composable
private fun InfoChip(
    icon: Int,
    text: String,
    backgroundColor: Color,
    iconColor: Color
) {
    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color(0xFF34495E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = customFontFamily()
                )
            )
        }
    }
}

// Function to check if a class is currently running
private fun isClassRunning(startTime: LocalTime?, endTime: LocalTime?): Boolean {
    if (startTime == null || endTime == null) return false
    val currentTime = LocalTime.now()
    return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime)
}

// DrawScope extension function to draw decorative elements on the card
private fun DrawScope.drawDecorativeElements(
    primaryColor: Color,
    secondaryColor: Color
) {
    // Draw scattered dots
    for (i in 0..8) {
        val x = size.width * (0.2f + 0.1f * (i % 3))
        val y = size.height * (0.2f + 0.1f * (i / 3))
        drawCircle(
            color = if (i % 2 == 0) primaryColor else secondaryColor,
            radius = 2.dp.toPx(),
            center = Offset(x, y)
        )
    }

    // Draw a decorative path
    val path = Path().apply {
        moveTo(size.width * 0.8f, size.height * 0.2f)
        quadraticBezierTo(
            size.width * 0.9f, size.height * 0.3f,
            size.width * 0.8f, size.height * 0.4f
        )
        quadraticBezierTo(
            size.width * 0.7f, size.height * 0.5f,
            size.width * 0.8f, size.height * 0.6f
        )
        quadraticBezierTo(
            size.width * 0.9f, size.height * 0.7f,
            size.width * 0.8f, size.height * 0.8f
        )
    }
    drawPath(path, primaryColor, style = Fill)
}