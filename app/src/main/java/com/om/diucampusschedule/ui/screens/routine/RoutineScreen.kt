package com.om.diucampusschedule.ui.screens.routine

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.UserRole
import com.om.diucampusschedule.ui.theme.DIUCampusScheduleTheme
import com.om.diucampusschedule.ui.theme.RobotoFontFamily
import com.om.diucampusschedule.ui.viewmodel.RoutineViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Custom colors for the table design
private val AppPrimaryColorLight = Color(0xFF1A56DB)
private val AppPrimaryColor = Color(0xFF1E3A8A)
private val AppBackgroundColorLight = Color(0xFFF8FAFC)
private val SoftBlue = Color(0xFF93C5FD)

// Custom font family function
@Composable
private fun customFontFamily() = RobotoFontFamily

// Time formatter for 12-hour format
private val formatter12HourUS = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(
    navController: NavController,
    viewModel: RoutineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Debug logging
    LaunchedEffect(uiState) {
        android.util.Log.d("RoutineScreen", "UI State updated:")
        android.util.Log.d("RoutineScreen", "  - isLoading: ${uiState.isLoading}")
        android.util.Log.d("RoutineScreen", "  - hasError: ${uiState.error != null}")
        android.util.Log.d("RoutineScreen", "  - error: ${uiState.error?.message}")
        android.util.Log.d("RoutineScreen", "  - activeDays: ${uiState.activeDays}")
        android.util.Log.d("RoutineScreen", "  - selectedDay: ${uiState.selectedDay}")
        android.util.Log.d("RoutineScreen", "  - routineItems: ${uiState.routineItems.size}")
        android.util.Log.d("RoutineScreen", "  - currentUser: ${uiState.currentUser?.name}")
    }

    // Animation states
    val refreshRotation by animateFloatAsState(
        targetValue = if (uiState.isRefreshing) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refreshRotation"
    )

    DIUCampusScheduleTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Class Routine",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (uiState.currentUser != null) {
                            Text(
                                text = "${uiState.currentUser!!.department} • ${uiState.currentUser!!.role.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                actions = {
                    // Offline indicator
                    if (uiState.isOffline) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Offline Mode",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    // Refresh button
                    IconButton(
                        onClick = { viewModel.refreshRoutine() },
                        enabled = !uiState.isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.graphicsLayer(rotationZ = refreshRotation),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )

            when {
                uiState.isLoading && uiState.routineItems.isEmpty() -> {
                    android.util.Log.d("RoutineScreen", "Showing LoadingContent")
                    LoadingContent()
                }

                uiState.error != null && uiState.routineItems.isEmpty() -> {
                    android.util.Log.d("RoutineScreen", "Showing ErrorContent: ${uiState.error!!.message}")
                    ErrorContent(
                        error = uiState.error!!.message ?: "An unknown error occurred",
                        isOffline = uiState.isOffline,
                        onRetry = { viewModel.retryLastAction() },
                        onDismiss = { viewModel.clearError() }
                    )
                }

                uiState.activeDays.isEmpty() -> {
                    android.util.Log.d("RoutineScreen", "Showing EmptyContent (activeDays is empty)")
                    EmptyContent()
                }

                else -> {
                    android.util.Log.d("RoutineScreen", "Showing RoutineContent with ${uiState.routineItems.size} items")
                    RoutineContent(
            allDays = uiState.allDays,
                        activeDays = uiState.activeDays,
                        selectedDay = uiState.selectedDay,
            routineItems = uiState.allRoutineItems, // Use all routine items for full week view
                        isRefreshing = uiState.isRefreshing,
                        currentUser = uiState.currentUser,
                        onDaySelected = { day -> viewModel.selectDay(day) },
                        onRefresh = { viewModel.refreshRoutine() }
                    )
                }
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
                text = "Loading your routine...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    isOffline: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isOffline) Icons.Default.CloudOff else Icons.Default.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isOffline) "Offline Mode" else "Error",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Dismiss")
                    }

                    Button(
                        onClick = onRetry,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EventBusy,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Classes Scheduled",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your routine will appear here once it's available.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun RoutineContent(
    allDays: List<String>,
    activeDays: List<String>,
    selectedDay: String,
    routineItems: List<RoutineItem>,
    isRefreshing: Boolean,
    currentUser: com.om.diucampusschedule.domain.model.User?,
    onDaySelected: (String) -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Refresh indicator
        if (isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Table-style routine display - shows full weekly routine
        TableRoutineView(
            currentUser = currentUser,
            routineItems = routineItems
        )
    }
}

@Composable
private fun TableRoutineView(
    currentUser: com.om.diucampusschedule.domain.model.User?,
    routineItems: List<RoutineItem>
) {
    val days = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday")
    
    // Generate time slots from routine items
    val startTimes = remember(routineItems) {
        routineItems.mapNotNull { routine ->
            try {
                val timeRange = routine.time.split(" - ")[0].trim()
                timeRange
            } catch (e: Exception) {
                null
            }
        }.distinct().sorted()
    }
    
    val scrollStateHorizontal = rememberScrollState()
    val scrollStateVertical = rememberScrollState()

    AnimatedVisibility(
        visible = true,
        enter = scaleIn(
            animationSpec = tween(
                durationMillis = 500,
                delayMillis = 300,
                easing = EaseInOutCubic
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 500,
                delayMillis = 300,
                easing = EaseInOutCubic
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(shape = RoundedCornerShape(12.dp))
                .background(color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else AppBackgroundColorLight)
                .border(
                    width = 2.dp,
                    color = AppPrimaryColorLight,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Column(modifier = Modifier.verticalScroll(scrollStateVertical)) {
                // Header row
                    AnimatedVisibility(
                        visible = true,
                    enter = scaleIn(
                        animationSpec = tween(
                            durationMillis = 400,
                            delayMillis = 400,
                            easing = EaseInOutCubic
                        )
                    ) + fadeIn(
                        animationSpec = tween(
                            durationMillis = 400,
                            delayMillis = 400,
                            easing = EaseInOutCubic
                        )
                    )
                ) {
                    Row(modifier = Modifier.horizontalScroll(scrollStateHorizontal)) {
                        Box(
                            modifier = Modifier
                                .width(110.dp)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Day/Time",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                fontFamily = customFontFamily(),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        startTimes.forEach { time ->
                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    time,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    fontFamily = customFontFamily(),
                                    color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

                HorizontalDivider(thickness = 1.dp, color = Color.Gray)

                // Days rows
                days.forEachIndexed { index, day ->
                    val routinesForDay = routineItems.filter { it.day.equals(day, ignoreCase = true) }
                    val firstClass = routinesForDay.minByOrNull { it.startTime ?: LocalTime.MIN }
                    val lastClass = routinesForDay.maxByOrNull { it.endTime ?: LocalTime.MAX }

                    if (routinesForDay.isNotEmpty()) {
                        Row(
                            modifier = Modifier.horizontalScroll(scrollStateHorizontal),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.take(3).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    fontFamily = customFontFamily(),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            startTimes.forEach { time ->
                                val timeSlots = routinesForDay.filter { it.time.startsWith(time) }
                                Column(
                                    modifier = Modifier
                                        .width(110.dp)
                                        .padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (timeSlots.isNotEmpty()) {
                                        timeSlots.forEach { routine ->
                                            RoutineCell(
                                                routine = routine
                                            )
                                        }
                                    } else {
                                        // Handle empty time slots
                                        var startTime: LocalTime? = null
                                        try {
                                            startTime = LocalTime.parse(time, formatter12HourUS)
                                        } catch (e: Exception) {
                                            startTime = null
                                        }
                                        val firstClassTime = firstClass?.startTime ?: LocalTime.MIN
                                        val lastClassTime = lastClass?.endTime ?: LocalTime.MAX

                                        if (startTime != null && startTime.isAfter(firstClassTime) && startTime.isBefore(lastClassTime)) {
                                            Box(
        modifier = Modifier
                                                    .width(110.dp)
                                                    .height(60.dp)
                                                    .background(
                                                        color = Color.Transparent,
                                                        shape = RoundedCornerShape(20.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (currentUser?.role == UserRole.TEACHER) "Counselling" else "Break",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = if (!isSystemInDarkTheme()) Color.DarkGray else MaterialTheme.colorScheme.onSurface,
                                                    fontFamily = customFontFamily(),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        } else {
                                            Box(
                modifier = Modifier
                                                    .height(110.dp)
                                                    .padding(4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {}
                                        }
                                    }
                                }
                            }
                        }
                        Divider(color = Color.Gray, thickness = 1.dp)
                    } else {
                        // Display "OFF DAY" for days with no routines
                        Row(
                            modifier = Modifier.horizontalScroll(scrollStateHorizontal),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                ) {
                    Text(
                                    text = day.take(3).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    fontFamily = customFontFamily(),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Create one wide cell for "OFF DAY" text
                            Box(
                                modifier = Modifier
                                    .width((110 * startTimes.size).dp)
                                    .height(60.dp)
                                    .padding(4.dp)
                                    .background(
                                        color = Color.LightGray.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                        Text(
                                    text = "OFF DAY",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = if (!isSystemInDarkTheme()) Color.Gray else MaterialTheme.colorScheme.onSurface,
                                    fontFamily = customFontFamily(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Divider(color = Color.Gray, thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineCell(
    routine: RoutineItem
) {
    var cellVisible by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 200, easing = EaseInOut),
        label = "cellScaleAnimation"
    )

    LaunchedEffect(Unit) {
        cellVisible = true
    }

    AnimatedVisibility(
        visible = cellVisible,
        enter = slideInVertically( // Slide in from bottom
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 400, easing = EaseOutBack) // EaseOutBack for overshoot effect
        ) + fadeIn(animationSpec = tween(durationMillis = 300, easing = EaseInOut)),
        exit = slideOutVertically( // Slide out to bottom
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 300, easing = EaseInOut)
        ) + fadeOut(animationSpec = tween(durationMillis = 250, easing = EaseInOut))
    ) {
            Column(
            modifier = Modifier
                .background(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(4.dp)
                .scale(scale),
                horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
                    Box(
                        modifier = Modifier
                    .width(120.dp)
                    .height(60.dp)
                    .background(
                        color = AppPrimaryColorLight,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {

                        }
                    )
            ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    // Top part for course code and teacher initial - Slide in from left
                    AnimatedVisibility(
                        visible = cellVisible,
                        enter = slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(durationMillis = 350, delayMillis = 50, easing = EaseInOut)
                        ) + fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = 50, easing = EaseInOut))
                ) {
                    Text(
                            text = "${routine.courseCode} - ${routine.teacherInitial ?: ""}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontFamily = customFontFamily()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .height(44.dp)
                            .background(
                                color = AppPrimaryColor,
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Bottom part for room and section - Slide in from right
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AnimatedVisibility(
                                visible = cellVisible,
                                enter = slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(durationMillis = 350, delayMillis = 100, easing = EaseInOut)
                                ) + fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = 100, easing = EaseInOut))
                        ) {
                            Text(
                                    text = routine.room ?: "",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    fontFamily = customFontFamily()
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                                AnimatedVisibility(
                                    visible = cellVisible,
                                    enter = slideInHorizontally(
                                        initialOffsetX = { -it },
                                        animationSpec = tween(durationMillis = 350, delayMillis = 150, easing = EaseInOut)
                                    ) + fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = 150, easing = EaseInOut))
                                ) {
                    Text(
                                        text = routine.batch ?: "",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = SoftBlue,
                                        textAlign = TextAlign.Center,
                                        fontFamily = customFontFamily()
                                    )
                                }
                                AnimatedVisibility(
                                    visible = cellVisible,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 250, delayMillis = 200, easing = EaseInOut)) // Just fade for '-'
                                ) {
                    Text(
                                        text = "-",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = SoftBlue,
                                        textAlign = TextAlign.Center,
                                        fontFamily = customFontFamily()
                                    )
                                }
                                AnimatedVisibility(
                                    visible = cellVisible,
                                    enter = slideInHorizontally(
                                        initialOffsetX = { it },
                                        animationSpec = tween(durationMillis = 350, delayMillis = 250, easing = EaseInOut)
                                    ) + fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = 250, easing = EaseInOut))
                                ) {
                    Text(
                                        text = routine.section ?: "",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = SoftBlue,
                                        textAlign = TextAlign.Center,
                                        fontFamily = customFontFamily()
                    )
                }
            }
        }
    }
}
            }
        }
    }
}

private fun slideInHorizontally(
    initialOffsetX: (fullWidth: Int) -> Int,
    animationSpec: FiniteAnimationSpec<IntOffset> = tween(durationMillis = 300) // Default to tween
): EnterTransition {
    return slideIn(
        animationSpec = animationSpec,
        initialOffset = { IntOffset(x = initialOffsetX(it.width), y = 0) }
    )
}

@Preview(showBackground = true)
@Composable
fun RoutineScreenPreview() {
    DIUCampusScheduleTheme {
        RoutineScreen(navController = rememberNavController())
    }
}