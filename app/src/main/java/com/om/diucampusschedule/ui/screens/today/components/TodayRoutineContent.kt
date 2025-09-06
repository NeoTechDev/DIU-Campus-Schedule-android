package com.om.diucampusschedule.ui.screens.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.Task
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.model.UserRole
import com.om.diucampusschedule.ui.screens.tasks.TaskCard
import com.om.diucampusschedule.ui.viewmodel.ClassStatus
import java.time.LocalDate
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
    tasks: List<Task>,
    currentUser: User?,
    isLoading: Boolean,
    getCourseName: (String) -> String = { it }, // Function to get course name from course code
    onClassClick: (RoutineItem) -> Unit = {},
    onUpdateTask: (Task) -> Unit = {},
    onDeleteTask: (Task) -> Unit = {},
    onEditTask: (Task) -> Unit = {},
    onShareTask: (Task) -> Unit = {},
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        LoadingContent()
    } else if (routineItems.isEmpty() && tasks.isEmpty()) {
        NoContentToday(currentUser)
    } else {
        val scheduleItems = if (routineItems.isNotEmpty()) createScheduleWithBreaks(routineItems) else emptyList()
        val classRoutines = routineItems.map { it.toClassRoutine() }
        
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Add ClassRoutineSectionHeader when routine items are not empty
            if (routineItems.isNotEmpty()) {
                item {
                    ClassRoutineSectionHeader(
                        count = routineItems.size,
                        title = "Your Classes",
                        countColor = Color(0xFF6200EE),
                        filteredRoutines = classRoutines,
                        formatter12HourUS = DateTimeFormatter.ofPattern("hh:mm a"),
                        selectedDate = if (isToday) LocalDate.now() else null
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(1.dp))
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
                            RoutineCard(
                                routine = scheduleItem.routineItem.toClassRoutine(),
                                courseName = getCourseName(scheduleItem.routineItem.courseCode),
                                selectedDate = LocalDate.now(),
                                formatter12HourUS = DateTimeFormatter.ofPattern("hh:mm a"),
                                isToday = isToday
                            )
                        }
                        is ScheduleItem.Break -> {
                            // Parse the break time strings back to LocalTime for the BreakTimeCard
                            val formatter = DateTimeFormatter.ofPattern("hh:mm a")
                            val startTime = try {
                                LocalTime.parse(scheduleItem.startTime, formatter)
                            } catch (e: Exception) {
                                LocalTime.parse(scheduleItem.startTime, DateTimeFormatter.ofPattern("h:mm a"))
                            }
                            val endTime = try {
                                LocalTime.parse(scheduleItem.endTime, formatter)
                            } catch (e: Exception) {
                                LocalTime.parse(scheduleItem.endTime, DateTimeFormatter.ofPattern("h:mm a"))
                            }
                            
                            BreakTimeCard(
                                breakText = if(currentUser?.role == UserRole.STUDENT) "Break Time" else "Counselling Hour",
                                subText = if(currentUser?.role == UserRole.STUDENT) "Time to recharge and\nget ready!" else "Time for student\nconsultations and guidance",
                                startTime = startTime,
                                endTime = endTime,
                                formatter12HourUS = DateTimeFormatter.ofPattern("hh:mm a"),
                                isToday = isToday
                            )
                        }
                    }
                }
            }
            
            // Add Tasks section if tasks are available
            if (tasks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(if (routineItems.isNotEmpty()) 16.dp else 10.dp))
                }
                
                item {
                    TaskSectionHeader(
                        count = tasks.size,
                        title = "Pending Tasks",
                        countColor = Color(0xFFFF6F00),
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(3.dp))
                }
                
                items(
                    items = tasks,
                    key = { task -> task.id }
                ) { task ->
                    Box(
                        modifier = Modifier
                            .padding(vertical = 3.dp)
                            .graphicsLayer {
                                shadowElevation = 2f
                                shape = RoundedCornerShape(20.dp)
                            }
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(20.dp)
                            )
                    ){
                        TaskCard(
                            task = task,
                            onUpdateTask = onUpdateTask,
                            onDeleteTask = onDeleteTask,
                            onEditTask = onEditTask,
                            onShareTask = onShareTask,
                            enableContextMenu = false, // Disable context menu for today screen
                            isInSelectionMode = false,
                            isSelected = false,
                            onSelectionChange = { _, _ -> },
                            bgColor = MaterialTheme.colorScheme.surface,
                            cardShape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
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
private fun NoContentToday(currentUser: User?) {
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
                    text = "Nothing Scheduled Today",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val message = "No classes or tasks scheduled for today. Enjoy your free time!"
                
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

private fun createScheduleWithBreaks(routineItems: List<RoutineItem>): List<ScheduleItem> {
    val scheduleItems = mutableListOf<ScheduleItem>()
    val sortedItems = routineItems
        .filter { it.startTime != null && it.endTime != null }
        .sortedBy { it.startTime }
    
    if (sortedItems.isEmpty()) return emptyList()
    
    // Merge consecutive identical classes
    val mergedItems = mergeConsecutiveClasses(sortedItems)
    
    // Add first class
    scheduleItems.add(ScheduleItem.Class(mergedItems.first()))
    
    // Add breaks between classes
    for (i in 1 until mergedItems.size) {
        val previousClass = mergedItems[i - 1]
        val currentClass = mergedItems[i]
        
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

private fun mergeConsecutiveClasses(routineItems: List<RoutineItem>): List<RoutineItem> {
    if (routineItems.isEmpty()) return emptyList()
    
    val mergedList = mutableListOf<RoutineItem>()
    var currentRoutine = routineItems.first()
    
    for (i in 1 until routineItems.size) {
        val routine = routineItems[i]
        
        // Check if current routine is the same course and consecutive
        if (currentRoutine.courseCode == routine.courseCode &&
            currentRoutine.endTime == routine.startTime &&
            currentRoutine.room == routine.room &&
            currentRoutine.batch == routine.batch &&
            currentRoutine.section == routine.section &&
            currentRoutine.teacherInitial == routine.teacherInitial) {
            // Merge the routines by updating the time string
            val startTimeFormatted = currentRoutine.startTime?.format(DateTimeFormatter.ofPattern("hh:mm a"))
            val endTimeFormatted = routine.endTime?.format(DateTimeFormatter.ofPattern("hh:mm a"))
            if (startTimeFormatted != null && endTimeFormatted != null) {
                val mergedTime = "$startTimeFormatted - $endTimeFormatted"
                currentRoutine = currentRoutine.copy(time = mergedTime)
            }
        } else {
            // Add the current routine to the merged list and start a new one
            mergedList.add(currentRoutine)
            currentRoutine = routine
        }
    }
    
    // Add the last routine
    mergedList.add(currentRoutine)
    
    return mergedList
}
