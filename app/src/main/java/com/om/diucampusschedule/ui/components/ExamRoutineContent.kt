package com.om.diucampusschedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
 * Enhanced exam routine content with Google Material 3 design
 * Clean, compact, and highly rounded corners throughout
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
            val userExamCourses by remember(examRoutine, user) {
                derivedStateOf {
                    if (user != null) {
                        android.util.Log.d("ExamRoutineContent", "User details: name=${user.name}, batch='${user.batch}', department='${user.department}'")

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

            ExamRoutineContent(
                examRoutine = examRoutine,
                userExamCourses = userExamCourses,
                user = user,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ExamRoutineContent(
    examRoutine: ExamRoutine,
    userExamCourses: List<ExamCourse>,
    user: User?,
    modifier: Modifier = Modifier
) {
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

            android.util.Log.d("ExamRoutineContentEnhanced", "Grouped into ${grouped.size} date groups")
            grouped.forEach { (date, courses) ->
                android.util.Log.d("ExamRoutineContentEnhanced", "Date $date has ${courses.size} courses")
            }

            grouped
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
            examsByDate.forEach { (date, examsForDate) ->
                item {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 4.dp)
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
            item {
                EmptyExamCard(
                    currentExamType = ExamType.BATCH,
                    userBatch = user?.batch
                )
            }
        }

        item {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        // Expandable optional exam sections
        val selfStudyCount = examRoutine.getSelfStudyExamCourses().size
        val retakeCount = examRoutine.getRetakeExamCourses().size

        item {
            ExpandableOptionalSection(
                title = "Self Study exams",
                subtitle = if (selfStudyCount > 0) "$selfStudyCount exam${if (selfStudyCount > 1) "s" else ""}" else "Tap to check",
                examCourses = examRoutine.getSelfStudyExamCourses(),
                examRoutine = examRoutine,
                color = MaterialTheme.colorScheme.primary,
                icon = ImageVector.vectorResource(id = R.drawable.book_open_reader)
            )
        }

        item {
            ExpandableOptionalSection(
                title = "Retake exams",
                subtitle = if (retakeCount > 0) "$retakeCount exam${if (retakeCount > 1) "s" else ""}" else "Tap to check",
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
    val examDay = remember(examCourse, examRoutine) {
        examRoutine.schedule.find { day ->
            day.courses.any { it.code == examCourse.code && it.batch == examCourse.batch }
        }
    }

    val timeRange = examRoutine.getSlotTimeRange(examCourse.slot) ?: "Time TBD"

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Course name and slot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = examCourse.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(8.dp))

                SlotBadge(
                    slot = examCourse.slot,
                    color = getSlotColor(examCourse.slot)
                )
            }

            // Date and time row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Time with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = timeRange.uppercase(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Date with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = examDay?.weekday ?: "TBD",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ExamRoutineHeaderCard(
    examRoutine: ExamRoutine,
    user: User?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Semester ${examRoutine.semester}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                // Batch badge
                user?.batch?.let { batch ->
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Batch $batch",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            // Exam dates info
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (examRoutine.startDate.isBlank())
                        "Dates to be announced"
                    else
                        "Starts ${examRoutine.startDate}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                // User's first exam date
                val userFirstExamDate = remember(examRoutine, user) {
                    user?.let { u ->
                        examRoutine.schedule
                            .firstOrNull { day -> day.courses.any { it.batch == u.batch } }
                            ?.date
                    }
                }

                if (!userFirstExamDate.isNullOrBlank()) {
                    Text(
                        text = "Your first exam: $userFirstExamDate",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AccentGreen
                    )
                }
            }

            // Message box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = examRoutine.message,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ImportantNoticeCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Announcement,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Important Notice",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(4.dp))

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

@Composable
private fun SlotBadge(
    slot: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(100.dp)
    ) {
        Text(
            text = "Slot $slot",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )
    }
}

@Composable
private fun getSlotColor(slot: String): Color {
    return when (slot.uppercase()) {
        "A" -> Color(0xFF1E88E5)
        "B" -> Color(0xFFD81B60)
        "C" -> Color(0xFF43A047)
        else -> MaterialTheme.colorScheme.primary
    }
}

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
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Exams Finished",
                modifier = Modifier.size(64.dp),
                tint = AccentGreen
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "All done for now 😎",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "No upcoming exams scheduled",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyExamCard(
    currentExamType: ExamType,
    userBatch: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = when (currentExamType) {
                    ExamType.BATCH -> Icons.Default.Groups
                    ExamType.SELF_STUDY -> Icons.Default.School
                    ExamType.RETAKE -> Icons.Default.Refresh
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )

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
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = when (currentExamType) {
                    ExamType.BATCH -> if (userBatch != null) {
                        "No schedules available for batch $userBatch"
                    } else {
                        "No schedules currently available"
                    }
                    ExamType.SELF_STUDY -> "Only some students have these exams"
                    ExamType.RETAKE -> "Great! You don't have any retakes"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

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

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .clickable { isExpanded = !isExpanded },
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Expanded content
        if (isExpanded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (examCourses.isNotEmpty()) {
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

                    examsByDate.forEach { (date, examsForDate) ->
                        Text(
                            text = date,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(
                                top = if (examsByDate.first().first == date) 0.dp else 12.dp,
                                bottom = 4.dp,
                                start = 4.dp
                            )
                        )

                        examsForDate.forEach { examCourse ->
                            ExamCourseCard(
                                examCourse = examCourse,
                                examRoutine = examRoutine
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = "No exams found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                        )
                    }
                }
            }
        }
    }
}