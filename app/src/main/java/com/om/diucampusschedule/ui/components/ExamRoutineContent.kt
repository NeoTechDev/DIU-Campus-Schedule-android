package com.om.diucampusschedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.diucampusschedule.R
import com.om.diucampusschedule.domain.model.ExamCourse
import com.om.diucampusschedule.domain.model.ExamRoutine
import com.om.diucampusschedule.domain.model.ExamType
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
                modifier = modifier
            )
        }
        else -> {
            // Get batch exam courses for the user
            val userExamCourses by remember(examRoutine, user) {
                derivedStateOf {
                    if (user != null) {
                        android.util.Log.d("ExamRoutineContent", "User details: name=${user.name}, batch='${user.batch}', department='${user.department}'")
                        
                        // Debug: Show all available courses in the exam routine
                        val allCourses = examRoutine.schedule.flatMap { it.courses }
                        android.util.Log.d("ExamRoutineContent", "Total courses in exam routine: ${allCourses.size}")
                        allCourses.forEachIndexed { index, course ->
                            android.util.Log.d("ExamRoutineContent", "Course $index: ${course.code} - ${course.name} (batch: '${course.batch}')")
                        }
                        
                        val filtered = examRoutine.getExamCoursesForUser(user)
                        android.util.Log.d("ExamRoutineContent", "Filtered courses for user: ${filtered.size}")

                        filtered
                    } else {
                        android.util.Log.d("ExamRoutineContent", "No user provided, showing empty list")
                        emptyList()
                    }
                }
            }

            // Show the main content with batch exams + optional sections
            ExamRoutineContent(
                examRoutine = examRoutine,
                userExamCourses = userExamCourses,
                user = user,
                modifier = modifier
            )
        }
    }
}

/**
 * Main exam routine content with batch exams + optional sections
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header Card
        item {
            ExamRoutineHeaderCard(
                examRoutine = examRoutine,
                user = user
            )
        }

        // Important Notice
        item {
            ImportantNoticeCard()
        }

        // Show exam courses or empty state card
        if (examsByDate.isNotEmpty()) {
            // Render grouped exams with date headers
            examsByDate.forEach { (date, examsForDate) ->
                item {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp, bottom = 0.dp)
                    )
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
        } else {
            // Show empty state for batch exams
            item {
                EmptyExamCard(
                    currentExamType = ExamType.BATCH,
                    userBatch = user?.batch
                )
            }
        }

        item {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 24.dp)
            )
        }

        // Expandable optional exam sections
        val selfStudyCount = examRoutine.getSelfStudyExamCourses().size
        val retakeCount = examRoutine.getRetakeExamCourses().size

        // Self Study expandable section
        item {
            ExpandableOptionalSection(
                title = "If you have Self Study exams",
                subtitle = if (selfStudyCount > 0) "$selfStudyCount exam${if (selfStudyCount > 1) "s" else ""} found" else "Tap to check if you have any",
                examCourses = examRoutine.getSelfStudyExamCourses(),
                examRoutine = examRoutine,
                color = MaterialTheme.colorScheme.primary,
                icon = ImageVector.vectorResource(id = R.drawable.book_open_reader)
            )
        }

        // Retake expandable section
        item {
            ExpandableOptionalSection(
                title = "If you have Retake exams",
                subtitle = if (retakeCount > 0) "$retakeCount exam${if (retakeCount > 1) "s" else ""} found" else "Tap to check if you have any",
                examCourses = examRoutine.getRetakeExamCourses(),
                examRoutine = examRoutine,
                color = MaterialTheme.colorScheme.secondary,
                icon = Icons.Default.Refresh
            )
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if(isSystemInDarkTheme()) Color.Transparent else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Course name and slot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = examCourse.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Slot badge
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = timeRange.uppercase(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                // Date with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = examDay?.weekday ?: "TBD",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Header card with improved spacing and typography
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
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
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
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            lineHeight = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Semester
                    Text(
                        text = "Semester: ${examRoutine.semester}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Exam Start date
                    Text(
                        text = if (examRoutine.startDate.isBlank())
                            "Exam dates will be announced"
                        else
                            "Starts from: ${examRoutine.startDate}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

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
                            userFirstExamDate.isNullOrBlank() && examRoutine.startDate.isBlank() ->
                                "Exam dates will be announced"
                            userFirstExamDate.isNullOrBlank() ->
                                "Starts from ${examRoutine.startDate}"
                            else ->
                                "Your first exam: $userFirstExamDate"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AccentGreen,
                    )
                }

                // Batch badge
                user?.batch?.let { batch ->
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "Batch $batch",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AccentGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = examRoutine.message,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    ),
                    color = if (isSystemInDarkTheme())
                        Color.White.copy(alpha = 0.95f)
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * Important notice card with improved visual hierarchy
 */
@Composable
private fun ImportantNoticeCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if(isSystemInDarkTheme()) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Announcement,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Important Notice",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "This schedule may contain conflicts or mistakes. Always verify exam timings and dates from the official university website before attending.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * Slot badge with improved styling
 */
@Composable
private fun SlotBadge(
    slot: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(20.dp)),
        color = color,
        shadowElevation = 2.dp
    ) {
        Text(
            text = "Slot $slot",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )
    }
}

/**
 * Get slot color
 */
@Composable
private fun getSlotColor(slot: String): Color {
    return when (slot.uppercase()) {
        "A" -> Color(0xFF2196F3)
        "B" -> Color(0xFFE91E63)
        "C" -> Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.primary
    }
}

/**
 * Empty state when no exam routines are available
 */
@Composable
private fun EmptyExamRoutineState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // New icon: check circle to indicate completion
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Exams Finished",
                modifier = Modifier.size(72.dp),
                tint = AccentGreen.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Exams? Done and dusted for now 😎",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Optional motivational subtext
            Text(
                text = "Take a break or start preparing for the next one!",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}


/**
 * Empty exam card
 */
@Composable
private fun EmptyExamCard(
    currentExamType: ExamType,
    userBatch: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = when (currentExamType) {
                    ExamType.BATCH -> Icons.Default.Groups
                    ExamType.SELF_STUDY -> Icons.Default.School
                    ExamType.RETAKE -> Icons.Default.Refresh
                },
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = when (currentExamType) {
                    ExamType.BATCH -> if (userBatch != null) {
                        "No Batch Exams"
                    } else {
                        "No Exams Available"
                    }
                    ExamType.SELF_STUDY -> "No Self Study Exams"
                    ExamType.RETAKE -> "No Retake Exams"
                },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when (currentExamType) {
                    ExamType.BATCH -> if (userBatch != null) {
                        "No exam schedules are currently available for batch $userBatch. Check back soon!"
                    } else {
                        "No exam schedules are currently available. Check back soon!"
                    }
                    ExamType.SELF_STUDY -> "Don't have Self Study exams? That's okay! Only some students have them."
                    ExamType.RETAKE -> "Don't have Retake exams? That's great! Only students who need to retake courses will have these exams."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}

/**
 * Expandable section with improved interaction design
 */
@Composable
private fun ExpandableOptionalSection(
    title: String,
    subtitle: String,
    examCourses: List<ExamCourse>,
    examRoutine: ExamRoutine,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { isExpanded = !isExpanded },
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Expanded content
        if (isExpanded) {
            Column(
                modifier = Modifier.padding(top = 12.dp)
            ) {
                if (examCourses.isNotEmpty()) {
                    // Group exams by date
                    val examsByDate = examCourses
                        .groupBy { examCourse ->
                            val examDay = examRoutine.schedule.find { day ->
                                day.courses.any { it.code == examCourse.code && it.batch == examCourse.batch }
                            }
                            examDay?.date ?: "TBD"
                        }
                        .toList()
                        .sortedWith { (dateA, _), (dateB, _) ->
                            when {
                                dateA == "TBD" && dateB == "TBD" -> 0
                                dateA == "TBD" -> 1
                                dateB == "TBD" -> -1
                                else -> {
                                    try {
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

                    // Show grouped exams with date headers
                    examsByDate.forEach { (date, examsForDate) ->
                        // Date header
                        Text(
                            text = date,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(
                                top = if (examsByDate.first().first == date) 0.dp else 20.dp,
                                bottom = 8.dp
                            )
                        )

                        // Exam cards for this date
                        examsForDate.forEach { examCourse ->
                            ExamCourseCard(
                                examCourse = examCourse,
                                examRoutine = examRoutine,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }
                } else {
                    // Empty message
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "No exams found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                        )
                    }
                }
            }
        }
    }
}