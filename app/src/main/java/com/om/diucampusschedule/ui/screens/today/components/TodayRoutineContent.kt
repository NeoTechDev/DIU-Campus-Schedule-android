package com.om.diucampusschedule.ui.screens.today.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.om.diucampusschedule.R
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.Task
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.model.UserRole
import com.om.diucampusschedule.domain.model.ExamRoutine
import com.om.diucampusschedule.ui.screens.tasks.TaskCard
import com.om.diucampusschedule.ui.utils.TimeFormatterUtils
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
    hasLoadedOnce: Boolean = false, // Add this parameter to prevent blinking
    getCourseName: (String) -> String = { it }, // Function to get course name from course code
    onClassClick: (RoutineItem) -> Unit = {},
    onUpdateTask: (Task) -> Unit = {},
    onDeleteTask: (Task) -> Unit = {},
    onEditTask: (Task) -> Unit = {},
    onShareTask: (Task) -> Unit = {},
    onTeacherClick: (String) -> Unit = {}, // Add teacher click callback
    isToday: Boolean,
    modifier: Modifier = Modifier,
    noContentImage: Painter,
    noScheduleMessages: String? = null,
    noScheduleSubMessage: String? = null,
    // Maintenance mode parameters for class routines only
    isMaintenanceMode: Boolean = false,
    maintenanceMessage: String? = null,
    isSemesterBreak: Boolean = false,
    updateType: String? = null,
    selectedDate: LocalDate,
    // Exam mode parameters
    isExamMode: Boolean = false,
    examRoutine: ExamRoutine? = null,
    onNavigateToRoutine: () -> Unit = {},
    // Add scroll state change listener
    onScrollStateChanged: (Int, Int) -> Unit = { _, _ -> }
) {
    // Only show loading if we haven't loaded once and are currently loading
    val shouldShowLoading = isLoading && !hasLoadedOnce
    
    if (shouldShowLoading) {
        LoadingContent()
    } else if (routineItems.isEmpty() && tasks.isEmpty() && !isExamMode) {
        // Wrap empty content in LazyColumn to enable pull-to-refresh
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Check if we need to show maintenance message for class routines specifically
                if (isMaintenanceMode) {
                    EmptyClassRoutineContent(
                        isMaintenanceMode = isMaintenanceMode,
                        maintenanceMessage = maintenanceMessage,
                        isSemesterBreak = isSemesterBreak,
                        updateType = updateType
                    )
                } else {
                    NoContentToday(
                        noContentImage = noContentImage,
                        message = noScheduleMessages,
                        subMessage = noScheduleSubMessage,
                        selectedDate = selectedDate,
                        user = currentUser
                    )
                }
            }
        }
    } else {
        val scheduleItems = if (routineItems.isNotEmpty()) {
            // Add debug logging for Xiaomi compatibility issues
            android.util.Log.d("TodayRoutineContent", "Processing ${routineItems.size} routine items")
            routineItems.forEachIndexed { index, item ->
                android.util.Log.d("TodayRoutineContent", "Item $index: ${item.courseCode} - startTime: ${item.startTime}, endTime: ${item.endTime}")
            }
            val result = createScheduleWithBreaks(routineItems)
            android.util.Log.d("TodayRoutineContent", "Created ${result.size} schedule items after processing")
            result
        } else emptyList()
        val classRoutines = routineItems.map { it.toClassRoutine() }
        
        // Create scroll state for tracking
        val listState = rememberLazyListState()

        // Monitor scroll state changes
        LaunchedEffect(listState) {
            snapshotFlow {
                listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
            }.collect { (index, offset) ->
                onScrollStateChanged(index, offset)
            }
        }

        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Add ClassRoutineSectionHeader when routine items are not empty OR in maintenance mode OR in exam mode
            if (routineItems.isNotEmpty() || isMaintenanceMode || isExamMode) {
                item {
                    if (isExamMode) {
                        // Show exam mode content when exam mode is active
                        ExamModeContent(
                            examRoutine = examRoutine,
                            onNavigateToRoutine = onNavigateToRoutine,
                            currentUser = currentUser
                        )
                    } else if (isMaintenanceMode) {
                        // Show maintenance message for class routine section only
                        EmptyClassRoutineContent(
                            isMaintenanceMode = isMaintenanceMode,
                            maintenanceMessage = maintenanceMessage,
                            isSemesterBreak = isSemesterBreak,
                            updateType = updateType
                        )
                    } else {
                        ClassRoutineSectionHeader(
                            count = routineItems.size,
                            title = "Your Classes",
                            countColor = Color(0xFF6200EE),
                            filteredRoutines = classRoutines,
                            formatter12HourUS = TimeFormatterUtils.createRobustTimeFormatter(),
                            selectedDate = if (isToday) LocalDate.now() else null
                        )
                        
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                }
                
                // Only show routine items if not in maintenance mode and not in exam mode
                if (!isMaintenanceMode && !isExamMode) {
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
                                    formatter12HourUS = TimeFormatterUtils.createRobustTimeFormatter(),
                                    isToday = isToday,
                                    onTeacherClick = onTeacherClick
                                )
                            }
                            is ScheduleItem.Break -> {
                                // Parse the break time strings back to LocalTime for the BreakTimeCard
                                val robustFormatter = TimeFormatterUtils.createRobustTimeFormatter()
                                val startTime = try {
                                    LocalTime.parse(scheduleItem.startTime, robustFormatter)
                                } catch (e: Exception) {
                                    try {
                                        LocalTime.parse(scheduleItem.startTime, DateTimeFormatter.ofPattern("h:mm a"))
                                    } catch (e2: Exception) {
                                        try {
                                            LocalTime.parse(scheduleItem.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                                        } catch (e3: Exception) {
                                            LocalTime.now() // fallback
                                        }
                                    }
                                }
                                val endTime = try {
                                    LocalTime.parse(scheduleItem.endTime, robustFormatter)
                                } catch (e: Exception) {
                                    try {
                                        LocalTime.parse(scheduleItem.endTime, DateTimeFormatter.ofPattern("h:mm a"))
                                    } catch (e2: Exception) {
                                        try {
                                            LocalTime.parse(scheduleItem.endTime, DateTimeFormatter.ofPattern("HH:mm"))
                                        } catch (e3: Exception) {
                                            LocalTime.now().plusHours(1) // fallback
                                        }
                                    }
                                }
                                
                                BreakTimeCard(
                                    breakText = if(currentUser?.role == UserRole.STUDENT) "Break Time" else "Counselling Hour",
                                    subText = if(currentUser?.role == UserRole.STUDENT) "Time to recharge and\nget ready!" else "Time for student\nconsultations and guidance",
                                    startTime = startTime,
                                    endTime = endTime,
                                    formatter12HourUS = robustFormatter,
                                    isToday = isToday,
                                    prayerBackgroundImage = R.drawable.prayer_time_bg
                                )
                            }
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
                Spacer(modifier = Modifier.height(180.dp))
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
            // Lottie Animation
            val composition by rememberLottieComposition(
                spec = LottieCompositionSpec.RawRes(
                    resId = R.raw.loading
                )
            )
            val progress by animateLottieCompositionAsState(
                composition,
                isPlaying = true,
                iterations = LottieConstants.IterateForever
            )

            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading your schedule...",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun NoContentToday(
    noContentImage: Painter,
    message: String? = null,
    subMessage: String? = null,
    selectedDate: LocalDate,
    user: User?
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

           if(user?.role == UserRole.STUDENT){
               // Get random meme for students - changes every time app opens
               val memeData = remember { getRandomMeme(selectedDate) }

               Card(
                   modifier = Modifier.size(250.dp),
                   shape = RoundedCornerShape(20.dp),
                   colors = CardDefaults.cardColors(
                       containerColor = MaterialTheme.colorScheme.surface
                   ),
               ){
                   Image(
                       painter = painterResource(id = memeData.imageRes),
                       contentDescription = memeData.description,
                       modifier = Modifier.size(250.dp)
                   )
               }
           } else @Composable {
               Image(
                   painter = noContentImage,
                   contentDescription = "No Schedule",
                   modifier = Modifier.size(180.dp)
               )
           }

            Spacer(modifier = Modifier.height(24.dp))

            if (message != null) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (subMessage != null) {
                Text(
                    text = subMessage,
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
    
    // Separate items with valid times from those without
    val itemsWithValidTimes = routineItems.filter { it.startTime != null && it.endTime != null }
    val itemsWithoutValidTimes = routineItems.filter { it.startTime == null || it.endTime == null }
    
    // If no items have valid times, just show all items as classes without breaks
    if (itemsWithValidTimes.isEmpty()) {
        return routineItems.map { ScheduleItem.Class(it) }
    }
    
    val sortedItems = itemsWithValidTimes.sortedBy { it.startTime }
    
    if (sortedItems.isEmpty()) return routineItems.map { ScheduleItem.Class(it) }
    
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
                val startTimeFormatted = TimeFormatterUtils.formatTime(previousEndTime)
                val endTimeFormatted = TimeFormatterUtils.formatTime(currentStartTime)
                
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
    
    // Add items without valid times at the end
    itemsWithoutValidTimes.forEach { item ->
        scheduleItems.add(ScheduleItem.Class(item))
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
            val startTimeFormatted = TimeFormatterUtils.formatTime(currentRoutine.startTime)
            val endTimeFormatted = TimeFormatterUtils.formatTime(routine.endTime)
            if (startTimeFormatted.isNotEmpty() && endTimeFormatted.isNotEmpty()) {
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

/**
 * Empty content specifically for class routine maintenance messages
 * This shows only for class routines, not tasks
 */
@Composable
private fun EmptyClassRoutineContent(
    isMaintenanceMode: Boolean = false,
    maintenanceMessage: String? = null,
    isSemesterBreak: Boolean = false,
    updateType: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Different icons and messages based on the state - use updateType for accurate detection
            val (icon, iconColor, title, message) = when {
                updateType == "semester_break" -> {
                    Quadruple(
                        Icons.Default.EventBusy,
                        MaterialTheme.colorScheme.primary,
                        "Semester Break",
                        maintenanceMessage ?: "Semester break is in progress. New semester routine will be available soon."
                    )
                }
                updateType == "maintenance_enabled" || (isMaintenanceMode && !isSemesterBreak) -> {
                    Quadruple(
                        Icons.Default.Refresh,
                        MaterialTheme.colorScheme.tertiary,
                        "System Maintenance",
                        maintenanceMessage ?: "System is under maintenance. New routine will be available soon."
                    )
                }
                isMaintenanceMode && isSemesterBreak -> {
                    Quadruple(
                        Icons.Default.EventBusy,
                        MaterialTheme.colorScheme.primary,
                        "Semester Break",
                        maintenanceMessage ?: "Semester break is in progress. New semester routine will be available soon."
                    )
                }
                else -> {
                    Quadruple(
                        Icons.Default.EventBusy,
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        "No Classes Scheduled",
                        "Your class routine will appear here once it's available."
                    )
                }
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = iconColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                color = if (isMaintenanceMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ExamModeContent(
    examRoutine: ExamRoutine?,
    onNavigateToRoutine: () -> Unit,
    currentUser: User?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if(currentUser?.role == UserRole.STUDENT) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Exam icon
                Image(
                    painter = painterResource(id = R.drawable.exam),
                    contentDescription = null,
                    modifier = Modifier.size(180.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))


                // Exam information
                if (examRoutine != null) {
                    Text(
                        text = "${examRoutine.examType} of ${examRoutine.semester} are currently in progress from ${examRoutine.startDate} to ${examRoutine.endDate}.",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Description text
                Text(
                    text = "Please check your exam routine",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
        else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Image(
                    painter = painterResource(id = R.drawable.exam),
                    contentDescription = null,
                    modifier = Modifier.size(200.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${examRoutine?.examType} of ${examRoutine?.semester} are currently in progress from ${examRoutine?.startDate} to ${examRoutine?.endDate}. Please wait until examination period concludes.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// Helper data class for multiple return values
private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
