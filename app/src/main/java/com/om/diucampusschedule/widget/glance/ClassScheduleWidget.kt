package com.om.diucampusschedule.widget.glance

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.om.diucampusschedule.MainActivity
import com.om.diucampusschedule.R
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.ui.theme.md_theme_light_error
import com.om.diucampusschedule.ui.theme.md_theme_light_primary
import com.om.diucampusschedule.widget.data.WidgetDataRepository
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Define a sealed class for widget items (Routine or Break)
sealed class WidgetScheduleItem {
    data class RoutineWidgetItem(val routine: RoutineItem) : WidgetScheduleItem()
    data class BreakWidgetItem(val startTime: LocalTime, val endTime: LocalTime, val role: String) : WidgetScheduleItem()
}

/**
 * Professional Daily Class Schedule Widget using Jetpack Glance
 * Displays today's classes in a clean, accessible format with both light and dark theme support
 */
class ClassScheduleWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Get the widget data repository through Hilt
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val widgetDataRepository = entryPoint.widgetDataRepository()

        provideContent {
            GlanceTheme(
                colors = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    GlanceTheme.colors  // Use system dynamic colors when available
                } else {
                    widgetColorScheme   // Use our custom color scheme on older devices
                }
            ) {
                ClassScheduleWidgetContent(widgetDataRepository)
            }
        }
    }
}

@Composable
private fun ClassScheduleWidgetContent(
    widgetDataRepository: WidgetDataRepository
) {
    val context = LocalContext.current
    val isDarkMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val isLoading by widgetDataRepository.isLoading.collectAsState()
    val currentUser by widgetDataRepository.getCurrentUser().collectAsState(initial = null)
    
    // Check if refresh is needed and get data
    val todayClasses = remember { mutableStateOf(emptyList<RoutineItem>()) }
    val courseNames = remember { mutableStateOf(mapOf<String, String>()) }
    val isDataLoaded = remember { mutableStateOf(false) }
    
    // Initialize GlanceAppWidgetManager
    val glanceManager = remember { GlanceAppWidgetManager(context) }
    
    // Refresh data if needed
    LaunchedEffect(Unit) {
        try {
            // Check if data refresh is needed
            if (widgetDataRepository.needsRefresh()) {
                // Refresh data which will update isLoading state
                val refreshedClasses = widgetDataRepository.refreshData()
                todayClasses.value = refreshedClasses
                
                // Load course names for the refreshed classes
                if (refreshedClasses.isNotEmpty()) {
                    val finalMap = mutableMapOf<String, String>()
                    refreshedClasses.forEach { routineItem ->
                        try {
                            val courseName = widgetDataRepository.getCourseName(routineItem.courseCode)
                            finalMap[routineItem.courseCode] = courseName ?: routineItem.courseCode
                        } catch (e: Exception) {
                            finalMap[routineItem.courseCode] = routineItem.courseCode
                        }
                    }
                    courseNames.value = finalMap
                }
            } else {
                // Use existing data from the flow
                val existingClasses = widgetDataRepository.getTodayClasses().first()
                todayClasses.value = existingClasses
                
                // Load course names for existing classes if not already loaded
                if (existingClasses.isNotEmpty() && courseNames.value.isEmpty()) {
                    val finalMap = mutableMapOf<String, String>()
                    existingClasses.forEach { routineItem ->
                        try {
                            val courseName = widgetDataRepository.getCourseName(routineItem.courseCode)
                            finalMap[routineItem.courseCode] = courseName ?: routineItem.courseCode
                        } catch (e: Exception) {
                            finalMap[routineItem.courseCode] = routineItem.courseCode
                        }
                    }
                    courseNames.value = finalMap
                }
            }
            isDataLoaded.value = true
        } catch (e: Exception) {
            android.util.Log.e("ClassScheduleWidget", "Error loading widget data", e)
            isDataLoaded.value = true
        }
    }
    
    // Get user role from current user
    val role = currentUser?.role?.name ?: ""

    // Show loading state only when actually loading
    if (isLoading && !isDataLoaded.value) {
        LoadingContent()
        return
    }

    // Prepare schedule items with routines and breaks
    val scheduleItems = remember(todayClasses.value, role) {
        prepareScheduleItems(todayClasses.value, role)
    }

    val classCount = remember(scheduleItems) {
        scheduleItems.count { it is WidgetScheduleItem.RoutineWidgetItem }
    }
    val breakCount = remember(scheduleItems) {
        scheduleItems.count { it is WidgetScheduleItem.BreakWidgetItem }
    }

    val todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM"))

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_background))
            .cornerRadius(12.dp)
            .padding(bottom = 12.dp)
            .clickable {
                context.startActivity(
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
            }
    ) {
        // Header Section
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 12.dp, end = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = GlanceModifier.defaultWeight()
            ) {
                Text(
                    text = "Today's Classes",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = androidx.glance.unit.ColorProvider(Color.White)
                    ),
                    maxLines = 1
                )
                Text(
                    text = todayDate.uppercase(),
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = androidx.glance.unit.ColorProvider(Color.White.copy(alpha = 0.9f))
                    )
                )
                // Class and break counts
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier.padding(top = 4.dp)
                ){
                    if(scheduleItems.isNotEmpty()){
                        Text(
                            text = "$classCount ${if (classCount == 1) "Class" else "Classes"} • $breakCount ${if (breakCount < 2) "Break" else "Breaks"}",
                            style = TextStyle(
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp,
                                color = ColorProvider(Color.White.copy(alpha = 0.7f))
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
            
            // ENDS time badge
            Column(
                horizontalAlignment = Alignment.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier.defaultWeight()
            ){
                val lastClassEndTime = getLastScheduleEndTime(scheduleItems)
                if (lastClassEndTime.isNotEmpty()) {
                    Box(
                        modifier = GlanceModifier
                            .background(
                                if(isDarkMode)  ImageProvider(R.drawable.widget_endtime_background_dark)
                                else ImageProvider(R.drawable.widget_endtime_background)
                            )
                            .cornerRadius(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = GlanceModifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            androidx.glance.Image(
                                provider = ImageProvider(R.drawable.ic_clock_outline),
                                contentDescription = null,
                                modifier = GlanceModifier.size(12.dp),
                                colorFilter = ColorFilter.tint(ColorProvider(day = md_theme_light_primary, night = Color.White))
                            )
                            Spacer(modifier = GlanceModifier.width(4.dp))
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ENDS",
                                    style = TextStyle(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 8.sp,
                                        color = ColorProvider(day = md_theme_light_primary.copy(alpha = 0.8f), night = Color.White.copy(alpha = 0.8f))
                                    )
                                )
                                Text(
                                    text = lastClassEndTime,
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = ColorProvider(day = md_theme_light_primary, night = Color.White)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Content Section - simplified logic
        when {
            currentUser == null -> {
                NoUserState()
            }
            scheduleItems.isNotEmpty() -> {
                LazyColumn(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(
                            if(isDarkMode) ImageProvider(R.drawable.widget_courselist_section_background_dark)
                            else ImageProvider(R.drawable.widget_courselist_section_background)
                        )
                        .cornerRadius(12.dp)
                ) {
                    items(scheduleItems) { item ->
                        when (item) {
                            is WidgetScheduleItem.RoutineWidgetItem -> RoutineWidgetItem(
                                routine = item.routine,
                                courseName = courseNames.value[item.routine.courseCode] ?: item.routine.courseCode
                            )
                            is WidgetScheduleItem.BreakWidgetItem -> BreakWidgetItem(
                                startTime = item.startTime,
                                endTime = item.endTime,
                                role = item.role
                            )
                        }
                    }
                }
            }
            else -> {
                NoClassesState()
            }
        }
    }
}

// Function to prepare schedule items (Routines and Breaks)
private fun prepareScheduleItems(
    filteredRoutines: List<RoutineItem>,
    role: String
): List<WidgetScheduleItem> {
    val scheduleItems = mutableListOf<WidgetScheduleItem>()
    val breakRanges = mutableListOf<Pair<LocalTime, LocalTime>>()

    // Sort routines by start time
    val sortedRoutines = filteredRoutines.sortedBy { it.startTime }
    val mergedRoutines = mutableListOf<RoutineItem>()

    // Merge consecutive routines of the same course
    var currentRoutine: RoutineItem? = null

    for (routine in sortedRoutines) {
        if (currentRoutine == null) {
            currentRoutine = routine
        } else {
            // Check if current routine is the same course and consecutive
            val currentEndTime = currentRoutine.endTime
            val routineStartTime = routine.startTime
            val routineEndTime = routine.endTime
            
            if (currentRoutine.courseCode == routine.courseCode &&
                currentEndTime != null && routineStartTime != null && routineEndTime != null &&
                currentEndTime == routineStartTime &&
                currentRoutine.room == routine.room &&
                currentRoutine.batch == routine.batch &&
                currentRoutine.section == routine.section &&
                currentRoutine.teacherInitial == routine.teacherInitial) {
                
                // Merge the routines by updating the time string
                val startTimeStr = currentRoutine.time.split(" - ")[0].trim()
                val endTimeStr = routine.time.split(" - ")[1].trim()
                val mergedTime = "$startTimeStr - $endTimeStr"
                
                currentRoutine = currentRoutine.copy(time = mergedTime)
            } else {
                // Add the current routine and start a new one
                mergedRoutines.add(currentRoutine)
                currentRoutine = routine
            }
        }
    }

    // Add the last routine if exists
    currentRoutine?.let { mergedRoutines.add(it) }

    // Add all merged routines
    mergedRoutines.forEach { routine ->
        scheduleItems.add(WidgetScheduleItem.RoutineWidgetItem(routine))
    }

    // Find break times between classes
    if (mergedRoutines.size > 1) {
        mergedRoutines.zipWithNext { current, next ->
            val currentEndTime = current.endTime
            val nextStartTime = next.startTime
            if (currentEndTime != null && nextStartTime != null &&
                currentEndTime.isBefore(nextStartTime)) {
                breakRanges.add(currentEndTime to nextStartTime)
            }
        }
    }

    // Add break items
    breakRanges.forEach { range ->
        scheduleItems.add(WidgetScheduleItem.BreakWidgetItem(range.first, range.second, role))
    }

    // Sort all items by start time
    return scheduleItems.sortedBy {
        when (it) {
            is WidgetScheduleItem.RoutineWidgetItem -> it.routine.startTime
            is WidgetScheduleItem.BreakWidgetItem -> it.startTime
        }
    }
}

@Composable
private fun RoutineWidgetItem(routine: RoutineItem, courseName: String) {
    val courseColor = getCourseColor(routine.courseCode)
    val timeText = "${formatTime(routine.startTime?.toString() ?: "")} - ${formatTime(routine.endTime?.toString() ?: "")}"
    val roomBatchSectionText = "${routine.room} | ${routine.batch}-${routine.section}"

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .width(4.dp)
                .fillMaxHeight()
                .cornerRadius(2.dp)
                .background(androidx.glance.unit.ColorProvider(courseColor))
        ) {}
        Spacer(modifier = GlanceModifier.width(8.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = courseName,
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = ColorProvider(day = Color.Black, night = Color.White)
                ),
                maxLines = 3
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = timeText,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(day = Color.Gray, night = Color.White.copy(alpha = 0.7f))
                )
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = roomBatchSectionText,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(day = Color.Gray, night = Color.White.copy(alpha = 0.7f))
                )
            )
        }
    }
}

@Composable
private fun BreakWidgetItem(
    startTime: LocalTime,
    endTime: LocalTime,
    role: String
) {
    val breakText = if (role == "Teacher") "Counselling Time" else "Break Time"
    val breakTime = "${startTime.formatTime()} - ${endTime.formatTime()}"

    val duration = Duration.between(startTime, endTime)
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    val durationText = when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}min"
        hours > 0 -> "${hours}h"
        else -> "${minutes}min"
    }

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 12.dp)
            .background(
                ColorProvider(
                    day = md_theme_light_error.copy(alpha = 0.15f),
                    night = Color(0xFF1E293B)
                )
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .width(4.dp)
                .fillMaxHeight()
                .cornerRadius(2.dp)
                .background(ColorProvider(md_theme_light_error))
        ) {}
        Spacer(modifier = GlanceModifier.width(8.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = breakText,
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = ColorProvider(day = Color.Black, night = Color.White)
                )
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = breakTime.uppercase(),
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(day = Color.Gray, night = Color.White.copy(alpha = 0.7f))
                )
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = "Duration: $durationText",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(day = Color.Gray, night = Color.White.copy(alpha = 0.7f))
                )
            )
        }
    }
}

private fun getLastScheduleEndTime(scheduleItems: List<WidgetScheduleItem>): String {
    var lastEndTime = LocalTime.MIN
    for (item in scheduleItems) {
        when (item) {
            is WidgetScheduleItem.RoutineWidgetItem -> {
                item.routine.endTime?.let { endTime ->
                    if (endTime.isAfter(lastEndTime)) {
                        lastEndTime = endTime
                    }
                }
            }
            is WidgetScheduleItem.BreakWidgetItem -> {
                if (item.endTime.isAfter(lastEndTime)) {
                    lastEndTime = item.endTime
                }
            }
        }
    }

    return if (lastEndTime != LocalTime.MIN) {
        lastEndTime.formatTime()
    } else {
        ""
    }
}

@Composable
private fun NoUserState() {
    val context = LocalContext.current
    val isDarkMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                if(isDarkMode) ImageProvider(R.drawable.widget_courselist_section_background_dark)
                else ImageProvider(R.drawable.widget_courselist_section_background)
            )
            .cornerRadius(12.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.glance.Image(
            provider = ImageProvider(R.drawable.sign_in),
            contentDescription = null,
            modifier = GlanceModifier.size(45.dp)
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = "Please login to see classes",
            style = TextStyle(
                fontSize = 14.sp,
                color = ColorProvider(day = Color.Gray, night = Color.White)
            )
        )
    }
}

@Composable
private fun NoClassesState() {
    val context = LocalContext.current
    val isDarkMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                if(isDarkMode) ImageProvider(R.drawable.widget_courselist_section_background_dark)
                else ImageProvider(R.drawable.widget_courselist_section_background)
            )
            .cornerRadius(12.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.glance.Image(
            provider = ImageProvider(R.drawable.star),
            contentDescription = null,
            modifier = GlanceModifier.size(45.dp)
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = "No classes today!",
            style = TextStyle(
                fontSize = 14.sp,
                color = ColorProvider(day = Color.Gray, night = Color.White)
            )
        )
    }
}

// Extension functions for time formatting
private fun LocalTime.formatTime(): String {
    return DateTimeFormatter.ofPattern("h:mm a", Locale.US).format(this)
}

private fun formatTime(time: String): String {
    return try {
        if (time.isBlank()) return ""
        val localTime = LocalTime.parse(time)
        localTime.format(DateTimeFormatter.ofPattern("h:mm a"))
    } catch (e: Exception) {
        time
    }
}

private fun getCourseColor(courseCode: String): Color {
    // Define a set of attractive colors for courses (excluding red - reserved for breaks)
    val colors = listOf(
        Color(0xFF3B82F6), // Blue
        Color(0xFF10B981), // Green
        Color(0xFFF59E0B), // Orange
        Color(0xFF8B5CF6), // Purple
        Color(0xFF06B6D4), // Cyan
        Color(0xFFEC4899), // Pink
        Color(0xFF84CC16), // Lime
        Color(0xFF6366F1), // Indigo
        Color(0xFFF97316), // Orange-Red
        Color(0xFF14B8A6), // Teal
        Color(0xFFA855F7), // Violet
        Color(0xFFEAB308), // Yellow
        Color(0xFFF472B6), // Rose
        Color(0xFF22D3EE), // Sky
        Color(0xFF34D399), // Emerald
        Color(0xFFFBBF24), // Amber
        Color(0xFFE879F9), // Fuchsia
        Color(0xFF60A5FA), // Blue-400
        Color(0xFF4ADE80), // Green-400
        Color(0xFF818CF8)  // Indigo-400
    )
    
    // Use course code hash to get consistent random color
    val hash = courseCode.hashCode()
    val colorIndex = kotlin.math.abs(hash) % colors.size
    return colors[colorIndex]
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_background))
            .cornerRadius(12.dp)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Loading Classes...",
            style = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = androidx.glance.unit.ColorProvider(Color.White)
            )
        )
    }
}

// Color schemes for light and dark themes
private val lightColors = androidx.compose.material3.lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF1A56DB),
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFE1EFFE),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF0D2137),
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color(0xFF1A1A1A),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF666666)
)

private val darkColors = androidx.compose.material3.darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF6B9FFF),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF0D2137),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF2D3748),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFE1EFFE),
    surface = androidx.compose.ui.graphics.Color(0xFF1A1A1A),
    onSurface = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF2D3748),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFB0B0B0)
)

private val widgetColorScheme = ColorProviders(
    light = lightColors,
    dark = darkColors
)
