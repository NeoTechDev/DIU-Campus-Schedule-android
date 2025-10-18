package com.om.diucampusschedule.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.diucampusschedule.domain.model.ExamCourse
import com.om.diucampusschedule.domain.model.ExamRoutine
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.ui.theme.AccentGreen

/**
 * Enhanced exam routine content matching the beautiful design from screenshot
 * Shows exam type, dates, and individual exam courses with clean cards
 */
@Composable
fun ExamRoutineContent(
    examRoutine: ExamRoutine?,
    user: User?,
    modifier: Modifier = Modifier
) {
    when {
        examRoutine == null -> {
            EmptyExamRoutineState(
                modifier = modifier,
                userBatch = user?.batch
            )
        }
        else -> {
            val userExamCourses by remember(examRoutine, user) {
                derivedStateOf {
                    if (user != null) {
                        val filtered = examRoutine.getExamCoursesForUser(user)
                        android.util.Log.d("ExamRoutineContentEnhanced", "Filtered courses for user: ${filtered.size}")
                        
                        // If no courses match the user's batch, show all courses for debugging
                        if (filtered.isEmpty()) {
                            android.util.Log.d("ExamRoutineContentEnhanced", "No courses match user batch, showing all courses for debugging")
                            examRoutine.schedule.flatMap { it.courses }
                        } else {
                            filtered
                        }
                    } else {
                        android.util.Log.d("ExamRoutineContentEnhanced", "No user provided, showing all courses")
                        examRoutine.schedule.flatMap { it.courses }
                    }
                }
            }

            if (userExamCourses.isEmpty()) {
                EmptyExamRoutineState(
                    modifier = modifier,
                    userBatch = user?.batch
                )
            } else {
                ExamRoutineContent(
                    examRoutine = examRoutine,
                    userExamCourses = userExamCourses,
                    user = user,
                    modifier = modifier
                )
            }
        }
    }
}

/**
 * Main exam routine content with header and exam cards
 */
@Composable
private fun ExamRoutineContent(
    examRoutine: ExamRoutine,
    userExamCourses: List<ExamCourse>,
    user: User?,
    modifier: Modifier = Modifier
) {
    // Group exams by date using professional Compose patterns
    val examsByDate by remember(userExamCourses, examRoutine) {
        derivedStateOf {
            android.util.Log.d("ExamRoutineContentEnhanced", "Grouping ${userExamCourses.size} exam courses by date")
            
            val grouped = userExamCourses
                .groupBy { examCourse ->
                    val examDay = examRoutine.schedule.find { day ->
                        day.courses.any { it.code == examCourse.code && it.batch == examCourse.batch }
                    }
                    val date = examDay?.date ?: "TBD"
                    android.util.Log.d("ExamRoutineContentEnhanced", "Course ${examCourse.code} assigned to date: $date")
                    date
                }
                .toList()
                .sortedWith { (dateA, _), (dateB, _) ->
                    // Sort by date, handling "TBD" and different date formats
                    when {
                        dateA == "TBD" && dateB == "TBD" -> 0
                        dateA == "TBD" -> 1
                        dateB == "TBD" -> -1
                        else -> {
                            try {
                                // Parse DD/MM/YYYY format
                                val parseDate = { dateStr: String ->
                                    val parts = dateStr.split("/")
                                    if (parts.size == 3) {
                                        val day = parts[0].toInt()
                                        val month = parts[1].toInt()
                                        val year = parts[2].toInt()
                                        year * 10000 + month * 100 + day
                                    } else {
                                        0
                                    }
                                }
                                parseDate(dateA).compareTo(parseDate(dateB))
                            } catch (e: Exception) {
                                dateA.compareTo(dateB)
                            }
                        }
                    }
                }
            
            android.util.Log.d("ExamRoutineContentEnhanced", "Grouped into ${grouped.size} date groups")
            grouped.forEach { (date, courses) ->
                android.util.Log.d("ExamRoutineContentEnhanced", "Date $date has ${courses.size} courses")
            }
            
            grouped
        }
    }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card (like in screenshot)
        item {
            ExamRoutineHeaderCard(
                examRoutine = examRoutine,
                user = user
            )
        }
        
        // Important Notice (like in screenshot)
        item {
            ImportantNoticeCard()
        }

        // Render grouped exams with date headers
        examsByDate.forEach { (date, examsForDate) ->
            items(
                items = examsForDate,
                key = { course -> "${course.code}_${course.batch}_${course.slot}_$date" }
            ) { examCourse ->
                android.util.Log.d("ExamRoutineContentEnhanced", "Rendering course card: ${examCourse.code} - ${examCourse.name}")
                Text(
                    text = date,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExamCourseCard(
                    examCourse = examCourse,
                    examRoutine = examRoutine
                )
            }
        }
        
        // Add debugging info if no courses
        if (examsByDate.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Debug: No exam courses found",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Total courses in routine: ${examRoutine.schedule.sumOf { it.courses.size }}",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "User batch: ${user?.batch}",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}



@Composable
private fun ExamCourseCard(
    examCourse: ExamCourse,
    examRoutine: ExamRoutine,
    modifier: Modifier = Modifier
) {
    // Get exam day information for this course
    val examDay = remember(examCourse, examRoutine) {
        examRoutine.schedule.find { day ->
            day.courses.any { it.code == examCourse.code && it.batch == examCourse.batch }
        }
    }
    
    // Get time from slot
    val timeRange = examRoutine.getSlotTimeRange(examCourse.slot) ?: "Time TBD"
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if(isSystemInDarkTheme()) MaterialTheme.colorScheme.outline.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Course name and slot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = examCourse.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(12.dp))
                
                // Slot badge with colors like in screenshot
                SlotBadge(
                    slot = examCourse.slot,
                    color = getSlotColor(examCourse.slot)
                )
            }
            
            // Date and time row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Time with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = timeRange.uppercase(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                // Date with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    Text(
                        text = examDay?.weekday ?: "TBD",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Header card matching the screenshot design
 */
@Composable
private fun ExamRoutineHeaderCard(
    examRoutine: ExamRoutine,
    user: User?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top row with exam type and batch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = examRoutine.examType.ifBlank { "Unknown Exam Type" },
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Semester and department
                    Text(
                        text = "Semester: ${examRoutine.semester}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Exam Start date
                    Text(
                        text = if (examRoutine.startDate.isBlank()) "Exam dates will be announced" else "Starts from: ${examRoutine.startDate}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentGreen,
                    )

                    // Users exam start date
                    Spacer(modifier = Modifier.height(4.dp))

                    // User's first exam date
                    val userFirstExamDate = remember(examRoutine, user) {
                        user?.let { u ->
                            examRoutine.schedule
                                .firstOrNull { day -> day.courses.any { it.batch == u.batch } }
                                ?.date
                        }
                    }

                    Text(
                        text = when {
                            userFirstExamDate.isNullOrBlank() && examRoutine.startDate.isBlank() -> "Exam dates will be announced"
                            userFirstExamDate.isNullOrBlank() -> "Starts from ${examRoutine.startDate}"
                            else -> "Your exam start date: $userFirstExamDate"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                // Batch badge (like in screenshot)
                user?.batch?.let { batch ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "Batch $batch",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Important notice card (like in screenshot)
 */
@Composable
private fun ImportantNoticeCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Announcement,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Important Notice",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "This schedule may contain conflicts or mistakes. Always verify exam timings and dates from the official university website before attending.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * Slot badge with dynamic colors like in screenshot
 */
@Composable
private fun SlotBadge(
    slot: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        color = color
    ) {
        Text(
            text = "Slot $slot",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

/**
 * Get slot color like in screenshot (A=Blue, B=Orange, C=Green)
 */
@Composable
private fun getSlotColor(slot: String): Color {
    return when (slot.uppercase()) {
        "A" -> Color(0xFF2196F3) // Blue like in screenshot
        "B" -> Color(0xFFE91E63) // Orange like in screenshot
        "C" -> Color(0xFF4CAF50) // Green like in screenshot
        else -> MaterialTheme.colorScheme.primary
    }
}

/**
 * Empty state when no exam routines are available
 */
@Composable
private fun EmptyExamRoutineState(
    userBatch: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (userBatch != null) {
                    "No exam routine available for batch $userBatch"
                } else {
                    "No exam routine available"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}