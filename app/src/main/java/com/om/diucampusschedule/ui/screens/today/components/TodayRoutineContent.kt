package com.om.diucampusschedule.ui.screens.today.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.ui.viewmodel.ClassStatus
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Data class to represent schedule items (either class or break)
sealed class ScheduleItem {
    data class Class(val routineItem: RoutineItem) : ScheduleItem()
    data class Break(val duration: String, val startTime: String, val endTime: String) : ScheduleItem()
}

@Composable
fun TodayRoutineContent(
    routineItems: List<RoutineItem>,
    currentUser: User?,
    isLoading: Boolean,
    getCourseName: (String) -> String = { it }, // Function to get course name from course code
    onClassClick: (RoutineItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        LoadingContent()
    } else if (routineItems.isEmpty()) {
        NoClassesToday(currentUser)
    } else {
        val scheduleItems = createScheduleWithBreaks(routineItems)
        
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Show filter info if user has lab section
            if (currentUser?.labSection?.isNotEmpty() == true) {
                item {
                    FilterInfoCard(currentUser)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(
                items = scheduleItems,
                key = { item ->
                    when (item) {
                        is ScheduleItem.Class -> item.routineItem.id.ifEmpty { 
                            "${item.routineItem.courseCode}_${item.routineItem.time}_${item.routineItem.room}" 
                        }
                        is ScheduleItem.Break -> "break_${item.startTime}_${item.endTime}"
                    }
                }
            ) { scheduleItem ->
                when (scheduleItem) {
                    is ScheduleItem.Class -> {
                        ClassCard(
                            routineItem = scheduleItem.routineItem,
                            courseName = getCourseName(scheduleItem.routineItem.courseCode),
                            status = getClassStatus(scheduleItem.routineItem),
                            onClick = { onClassClick(scheduleItem.routineItem) }
                        )
                    }
                    is ScheduleItem.Break -> {
                        BreakTimeCard(
                            duration = scheduleItem.duration,
                            startTime = scheduleItem.startTime,
                            endTime = scheduleItem.endTime
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun FilterInfoCard(user: User) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "Showing classes for:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                val filterText = if (user.labSection.isNotEmpty()) {
                    "Section ${user.section} + Lab Section ${user.labSection} only"
                } else {
                    "Section ${user.section} (all lab sections)"
                }
                
                Text(
                    text = filterText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading your schedule...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun NoClassesToday(currentUser: User?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EventBusy,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "No Classes Today",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val message = if (currentUser?.labSection?.isNotEmpty() == true) {
                    "No classes scheduled for your section ${currentUser.section} and lab section ${currentUser.labSection} today."
                } else if (currentUser?.section?.isNotEmpty() == true) {
                    "No classes scheduled for your section ${currentUser.section} today."
                } else {
                    "No classes scheduled for today. Enjoy your free time!"
                }
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

private fun getClassStatus(routineItem: RoutineItem): ClassStatus {
    val currentTime = LocalTime.now()
    val startTime = routineItem.startTime
    val endTime = routineItem.endTime
    
    return when {
        startTime == null || endTime == null -> ClassStatus.UNKNOWN
        currentTime.isBefore(startTime) -> ClassStatus.UPCOMING
        currentTime.isAfter(startTime) && currentTime.isBefore(endTime) -> ClassStatus.ONGOING
        currentTime.isAfter(endTime) -> ClassStatus.COMPLETED
        else -> ClassStatus.UNKNOWN
    }
}

@Composable
private fun BreakTimeCard(
    duration: String,
    startTime: String,
    endTime: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Coffee,
                    contentDescription = "Break time",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Break Time",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "$startTime - $endTime",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = duration,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

private fun createScheduleWithBreaks(routineItems: List<RoutineItem>): List<ScheduleItem> {
    val scheduleItems = mutableListOf<ScheduleItem>()
    val sortedItems = routineItems
        .filter { it.startTime != null && it.endTime != null }
        .sortedBy { it.startTime }
    
    if (sortedItems.isEmpty()) return emptyList()
    
    // Add first class
    scheduleItems.add(ScheduleItem.Class(sortedItems.first()))
    
    // Add breaks between classes
    for (i in 1 until sortedItems.size) {
        val previousClass = sortedItems[i - 1]
        val currentClass = sortedItems[i]
        
        val previousEndTime = previousClass.endTime
        val currentStartTime = currentClass.startTime
        
        if (previousEndTime != null && currentStartTime != null) {
            val breakDurationMinutes = ChronoUnit.MINUTES.between(previousEndTime, currentStartTime)
            
            // Only add break if it's more than 5 minutes (to avoid very short gaps)
            if (breakDurationMinutes > 5) {
                val breakDuration = formatBreakDuration(breakDurationMinutes)
                val startTimeFormatted = previousEndTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
                val endTimeFormatted = currentStartTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
                
                scheduleItems.add(
                    ScheduleItem.Break(
                        duration = breakDuration,
                        startTime = startTimeFormatted,
                        endTime = endTimeFormatted
                    )
                )
            }
        }
        
        // Add current class
        scheduleItems.add(ScheduleItem.Class(currentClass))
    }
    
    return scheduleItems
}

private fun formatBreakDuration(minutes: Long): String {
    return when {
        minutes < 60 -> "${minutes}m"
        minutes % 60 == 0L -> "${minutes / 60}h"
        else -> "${minutes / 60}h ${minutes % 60}m"
    }
}
