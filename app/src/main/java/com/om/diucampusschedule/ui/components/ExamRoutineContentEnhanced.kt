package com.om.diucampusschedule.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.diucampusschedule.domain.model.ExamCourse
import com.om.diucampusschedule.domain.model.ExamRoutine
import com.om.diucampusschedule.domain.model.User

/**
 * Enhanced exam routine content matching the beautiful design from screenshot
 * Shows exam type, dates, and individual exam courses with clean cards
 */
@Composable
fun ExamRoutineContentEnhanced(
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
            item(key = "date_header_$date") {
                ExamDateHeader(date = date)
            }
            
            items(
                items = examsForDate,
                key = { course -> "${course.code}_${course.batch}_${course.slot}_$date" }
            ) { examCourse ->
                android.util.Log.d("ExamRoutineContentEnhanced", "Rendering course card: ${examCourse.code} - ${examCourse.name}")
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
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Course name and slot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = examCourse.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                // Slot badge with colors like in screenshot
                SlotBadge(
                    slot = examCourse.slot,
                    color = getSlotColor(examCourse.slot)
                )
            }
            
            // Date and time row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Date with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "${examDay?.weekday ?: "TBD"}, ${examDay?.date ?: "TBD"}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Time with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = timeRange,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top row with exam type and batch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (examRoutine.examType.isBlank()) "Final Exam" else examRoutine.examType,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Batch badge (like in screenshot)
                user?.batch?.let { batch ->
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "Batch $batch",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Semester and department
            Text(
                text = examRoutine.semester,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = examRoutine.department,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Start date with blue color (like in screenshot)
            Text(
                text = if (examRoutine.startDate.isBlank()) "Exam dates will be announced" else "Starts from ${examRoutine.startDate}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
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
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Red icon
            Text(
                text = "📋",
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            Column {
                Text(
                    text = "Important Notice",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "This schedule may contain conflicts or mistakes. Always verify exam timings and dates from the official university website before attending.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

/**
 * Date header for grouping exams by date
 */
@Composable
private fun ExamDateHeader(
    date: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                text = date,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
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
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        color = color
    ) {
        Text(
            text = "Slot $slot",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
        "B" -> Color(0xFFFF9800) // Orange like in screenshot  
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