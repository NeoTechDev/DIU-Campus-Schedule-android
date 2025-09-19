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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.diucampusschedule.R
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.ui.theme.customFontFamily
import com.om.diucampusschedule.ui.utils.TimeFormatterUtils
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Data class to adapt RoutineItem to ClassRoutine for legacy UI compatibility
data class ClassRoutine(
    val day: String,
    val time: String,
    val room: String,
    val courseCode: String,
    val teacherInitial: String?,
    val batch: String?,
    val section: String?,
    val startTime: LocalTime?,
    val endTime: LocalTime?
)

// Extension function to convert RoutineItem to ClassRoutine
fun RoutineItem.toClassRoutine(): ClassRoutine {
    return ClassRoutine(
        day = this.day,
        time = this.time,
        room = this.room,
        courseCode = this.courseCode,
        teacherInitial = this.teacherInitial,
        batch = this.batch,
        section = this.section,
        startTime = this.startTime,
        endTime = this.endTime
    )
}

// Mock course name utility for backward compatibility
object CourseUtils {
    private val courseNameCache = mutableMapOf<String, String>()
    
    fun getCourseName(courseCode: String): String? {
        return courseNameCache[courseCode]
    }
    
    fun setCourseNames(courseNames: Map<String, String>) {
        courseNameCache.clear()
        courseNameCache.putAll(courseNames)
    }
}

// Define the missing color constant
val AppSecondaryColorDark = Color(0xFF3730A3) // Purple color similar to secondary

@Composable
fun RoutineCard(
    routine: ClassRoutine,
    courseName: String? = null, // Accept course name from external source
    selectedDate: LocalDate = LocalDate.now(),
    formatter12HourUS: DateTimeFormatter? = null,
    isToday: Boolean = false,
    onTeacherClick: ((String) -> Unit)? = null // Add callback for teacher initial click
) {
    val displayedCourseName = courseName ?: CourseUtils.getCourseName(routine.courseCode) ?: routine.courseCode

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
    val colorIndex = (colorSeed % pastelColors.size)
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
                    color = AppSecondaryColorDark,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 4.dp, horizontal = 6.dp),
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
                                text = routine.startTime?.format(formatter12HourUS ?: TimeFormatterUtils.createRobustTimeFormatter()) ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 14.sp
                                ),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDDD6FE),
                                fontFamily = customFontFamily(),
                                textAlign = TextAlign.Start,
                                lineHeight = 14.sp,
                                letterSpacing = (-0.5).sp,
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
                                                Color(0xFF0EA5E9)
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
                                text = routine.endTime?.format(formatter12HourUS ?: TimeFormatterUtils.createRobustTimeFormatter()) ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDDD6FE),
                                    fontFamily = customFontFamily()
                                ),
                                textAlign = TextAlign.Start,
                                lineHeight = 14.sp,
                                letterSpacing = (-0.5).sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // White divider between sections
                Box(
                    modifier = Modifier
                        .width(8.dp)
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
                                .padding(horizontal = 8.dp, vertical = 12.dp),
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
                                        text = displayedCourseName,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            lineHeight = 16.sp,
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
                                        ClickableInfoChip(
                                            icon = R.drawable.lesson_class_24,
                                            text = routine.teacherInitial ?: "TBA",
                                            backgroundColor = Color.White.copy(alpha = 0.6f),
                                            iconColor = primaryColor,
                                            onClick = routine.teacherInitial?.let { initial ->
                                                { onTeacherClick?.invoke(initial) }
                                            }
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
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    color = Color(0xFF34495E),
                                                    fontSize = if (roomNo.length > 5) 15.sp else 19.sp,
                                                    letterSpacing = (-0.5).sp,
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
            .background(backgroundColor, RoundedCornerShape(10.dp))
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

@Composable
private fun ClickableInfoChip(
    icon: Int,
    text: String,
    backgroundColor: Color,
    iconColor: Color,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .let { modifier ->
                if (onClick != null) {
                    modifier.clickable { onClick() }
                } else {
                    modifier
                }
            }
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
                    fontFamily = customFontFamily(),
                    textDecoration = if (onClick != null) TextDecoration.Underline else null
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
        quadraticTo(
            size.width * 0.9f, size.height * 0.3f,
            size.width * 0.8f, size.height * 0.4f
        )
        quadraticTo(
            size.width * 0.7f, size.height * 0.5f,
            size.width * 0.8f, size.height * 0.6f
        )
        quadraticTo(
            size.width * 0.9f, size.height * 0.7f,
            size.width * 0.8f, size.height * 0.8f
        )
    }
    drawPath(path, primaryColor, style = Fill)
}

// Preview routine card
@Composable
@Preview(showBackground = true)
fun RoutineCardPreview() {
    val sampleRoutineItem = RoutineItem(
        day = "Monday",
        time = "10:00 AM - 11:30 AM",
        room = "A-101",
        courseCode = "CSE420",
        teacherInitial = "ABC",
        batch = "55",
        section = "A",
        department = "CSE"
    )

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RoutineCard(
            routine = sampleRoutineItem.toClassRoutine(),
            courseName = "Software Engineering",
            selectedDate = LocalDate.now(),
            formatter12HourUS = TimeFormatterUtils.createRobustTimeFormatter(),
            onTeacherClick = { /* Preview - no action */ }
        )

        RoutineCard(
            routine = sampleRoutineItem.copy(
                courseCode = "MAT101",
                room = "B-205",
                teacherInitial = "XYZ"
            ).toClassRoutine(),
            courseName = "Mathematics",
            selectedDate = LocalDate.now(),
            formatter12HourUS = TimeFormatterUtils.createRobustTimeFormatter(),
            onTeacherClick = { /* Preview - no action */ }
        )
    }
}


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
    isToday: Boolean = false,
    prayerBackgroundImage: Int? = null // Add parameter for prayer background image
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    // Check if break is currently ongoing
    val isOngoing = isToday && isBreakTimeOngoing(startTime, endTime)

    val isPrayerTime = startTime == LocalTime.parse("13:00") && endTime == LocalTime.parse("14:30")

    // Prayer-specific colors
    val prayerTextColor = Color.White
    val prayerTextShadow = Shadow(
        color = Color.Black.copy(alpha = 0.6f),
        offset = Offset(1f, 1f),
        blurRadius = 2f
    )

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
        // Background image for prayer time
        if (isPrayerTime && prayerBackgroundImage != null) {
            Image(
                painter = painterResource(id = prayerBackgroundImage),
                contentDescription = "Prayer background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp)),
                alpha = 1f
            )
        }

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
                        color = if(isPrayerTime) {
                            if (prayerBackgroundImage != null) Color.Transparent
                            else Color(0xFF102C57)
                        } else BreakBackgroundColor,
                        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 4.dp, horizontal = 6.dp),
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
                            text = startTime.format(formatter12HourUS ?: TimeFormatterUtils.createRobustTimeFormatter()),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if(isPrayerTime) prayerTextColor else Color.White.copy(alpha = 0.95f),
                                fontFamily = customFontFamily(),
                                shadow = if(isPrayerTime) prayerTextShadow else null
                            ),
                            textAlign = TextAlign.Start,
                            lineHeight = 14.sp,
                            letterSpacing = (-0.5).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Vertical divider
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(300, delayMillis = 100))
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(28.dp)
                                .background(
                                    if(isPrayerTime) prayerTextColor else Color.White.copy(alpha = 0.9f),
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
                            text = endTime.format(formatter12HourUS ?: TimeFormatterUtils.createRobustTimeFormatter()),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if(isPrayerTime) prayerTextColor else Color.White.copy(alpha = 0.95f),
                                fontFamily = customFontFamily(),
                                shadow = if(isPrayerTime) prayerTextShadow else null
                            ),
                            lineHeight = 14.sp,
                            letterSpacing = (-0.5).sp,
                            textAlign = TextAlign.Start,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(90.dp)
                    .background(MaterialTheme.colorScheme.background)
            )

            // Right content section
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
                    .background(
                        color = if(isPrayerTime) {
                            if (prayerBackgroundImage != null) Color.Transparent
                            else Color(0xFF102C57)
                        } else BreakBackgroundColor,
                        shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left content
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Icon
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(animationSpec = tween(300))
                        ) {
                            Icon(
                                imageVector = if(isPrayerTime) ImageVector.vectorResource(id = R.drawable.mosque_24) else Icons.Outlined.Coffee,
                                contentDescription = "Break Icon",
                                tint = if (isPrayerTime && prayerBackgroundImage != null) prayerTextColor else BreakTextColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Title + Subtext
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Title
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                                    initialOffsetY = { -20 },
                                    animationSpec = tween(300)
                                )
                            ) {
                                Text(
                                    text = if(isPrayerTime) "PRAYER TIME" else breakText.uppercase(),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        letterSpacing = 0.5.sp,
                                        fontFamily = customFontFamily(),
                                        shadow = if(isPrayerTime) prayerTextShadow else null
                                    ),
                                    color = if (isPrayerTime && prayerBackgroundImage != null) prayerTextColor else BreakTextColor
                                )
                            }

                            // Sub-text (only for non-prayer breaks)
                            if(!isPrayerTime){
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
                                        lineHeight = 14.sp,
                                        color = BreakTextColor.copy(alpha = 0.7f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Duration
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
                                fontSize = 10.sp,
                                fontFamily = customFontFamily(),
                                shadow = if(isPrayerTime) prayerTextShadow else null
                            ),
                            lineHeight = 10.sp,
                            textAlign = TextAlign.Center,
                            color = if (isPrayerTime && prayerBackgroundImage != null) Color.White else BreakTextColor,
                            modifier = Modifier
                                .background(
                                    color = if(isPrayerTime) Color.Black.copy(alpha = 0.2f) else BreakTextColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Now tag
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
                        Color(0xFF4CAF50),
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
                        fontFamily = customFontFamily(),
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = Offset(1f, 1f),
                            blurRadius = 2f
                        )
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