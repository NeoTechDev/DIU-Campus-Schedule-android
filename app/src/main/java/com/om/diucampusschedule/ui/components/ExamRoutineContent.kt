package com.om.diucampusschedule.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.diucampusschedule.domain.model.ExamCourse
import com.om.diucampusschedule.domain.model.ExamRoutine
import com.om.diucampusschedule.domain.model.User

// Custom colors for exam routine
private val ExamPrimaryColor = Color(0xFFE91E63) // Pink for exam
private val ExamAccentColor = Color(0xFFAD1457)
private val ExamBackgroundColor = Color(0xFFFCE4EC)
private val ExamCardColor = Color(0xFFF8BBD9)

@Composable
fun ExamRoutineContent(
    examRoutine: ExamRoutine?,
    examCourses: List<ExamCourse>,
    examDates: List<String>,
    selectedExamDate: String,
    currentUser: User?,
    onDateSelected: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (examRoutine != null) {
            // Exam routine header
            ExamRoutineHeader(
                examRoutine = examRoutine,
                selectedDate = selectedExamDate,
                examDates = examDates,
                onDateSelected = onDateSelected
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Exam courses for selected date
            ExamCoursesView(examCourses = examCourses, examRoutine = examRoutine)
        } else {
            // No exam routine found
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "No Exam",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Exam Routine Found",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Exam routine for your department is not available yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ExamRoutineHeader(
    examRoutine: ExamRoutine,
    selectedDate: String,
    examDates: List<String>,
    onDateSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = ExamBackgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Exam type and semester
            Text(
                text = examRoutine.examType,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = ExamPrimaryColor
            )
            Text(
                text = "${examRoutine.semester} • ${examRoutine.department}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Exam period
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Exam Period",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${examRoutine.startDate} - ${examRoutine.endDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (examDates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                // Date selector
                Text(
                    text = "Select Exam Date:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    examDates.forEach { date ->
                        ExamDateChip(
                            date = date,
                            isSelected = date == selectedDate,
                            onClick = { onDateSelected(date) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamDateChip(
    date: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) ExamPrimaryColor else Color.Transparent
    val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    val borderColor = if (isSelected) ExamPrimaryColor else MaterialTheme.colorScheme.outline
    
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(20.dp)),
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ExamCoursesView(
    examCourses: List<ExamCourse>,
    examRoutine: ExamRoutine
) {
    if (examCourses.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "No Exams",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Exams Today",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "You have no exams scheduled for this date.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            examCourses.forEach { examCourse ->
                ExamCourseCard(
                    examCourse = examCourse,
                    examRoutine = examRoutine
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ExamCourseCard(
    examCourse: ExamCourse,
    examRoutine: ExamRoutine
) {
    var cardVisible by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 200, easing = EaseInOut),
        label = "examCardScaleAnimation"
    )

    LaunchedEffect(Unit) {
        cardVisible = true
    }

    AnimatedVisibility(
        visible = cardVisible,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight / 4 },
            animationSpec = tween(durationMillis = 300, easing = EaseOutBack)
        ) + fadeIn(animationSpec = tween(durationMillis = 300, easing = EaseInOut)),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> -fullHeight / 4 },
            animationSpec = tween(durationMillis = 300, easing = EaseInOut)
        ) + fadeOut(animationSpec = tween(durationMillis = 250, easing = EaseInOut))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale),
            colors = CardDefaults.cardColors(
                containerColor = ExamCardColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Time slot
                val timeRange = examRoutine.getSlotTimeRange(examCourse.slot)
                if (timeRange != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            color = ExamPrimaryColor,
                            shape = CircleShape
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = timeRange,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = ExamAccentColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Slot ${examCourse.slot}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Course code and name
                Text(
                    text = examCourse.code,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ExamPrimaryColor
                )
                
                Text(
                    text = examCourse.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Additional info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (examCourse.batch.isNotEmpty()) {
                        Text(
                            text = "Batch ${examCourse.batch}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = "${examCourse.students} students",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}