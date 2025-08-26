package com.om.diucampusschedule.ui.screens.routine

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.om.diucampusschedule.domain.model.DayOfWeek
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.UserRole
import com.om.diucampusschedule.ui.theme.DIUCampusScheduleTheme
import com.om.diucampusschedule.ui.viewmodel.RoutineViewModel
import java.time.LocalTime

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
                        activeDays = uiState.activeDays,
                        selectedDay = uiState.selectedDay,
                        routineItems = uiState.routineItems,
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
        // Day Selector
        DaySelector(
            activeDays = activeDays,
            selectedDay = selectedDay,
            onDaySelected = onDaySelected
        )

        // Refresh indicator
        if (isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Routine List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (routineItems.isEmpty()) {
                item {
                    NoDayClassesContent(selectedDay)
                }
            } else {
                items(
                    items = routineItems,
                    key = { it.id }
                ) { routineItem ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300))
                    ) {
                        RoutineItemCard(
                            routineItem = routineItem,
                            currentUser = currentUser
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DaySelector(
    activeDays: List<String>,
    selectedDay: String,
    onDaySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(activeDays) { day ->
            val isSelected = day == selectedDay
            val isToday = day == DayOfWeek.getCurrentDay().displayName

            val dayOfWeek = DayOfWeek.fromString(day)
            val shortName = dayOfWeek?.shortName ?: day.take(3)

            Card(
                modifier = Modifier
                    .clickable { onDaySelected(day) }
                    .animateContentSize(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 8.dp else 2.dp
                ),
                border = if (isToday && !isSelected) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else null
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = shortName,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isToday -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )

                    if (isToday) {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutineItemCard(
    routineItem: RoutineItem,
    currentUser: com.om.diucampusschedule.domain.model.User?
) {
    val currentTime = remember { LocalTime.now() }
    val isCurrentClass = remember(routineItem, currentTime) {
        val startTime = routineItem.startTime
        val endTime = routineItem.endTime
        val today = DayOfWeek.getCurrentDay().displayName

        routineItem.day == today && startTime != null && endTime != null &&
                currentTime.isAfter(startTime) && currentTime.isBefore(endTime)
    }

    val isUpcoming = remember(routineItem, currentTime) {
        val startTime = routineItem.startTime
        val today = DayOfWeek.getCurrentDay().displayName

        routineItem.day == today && startTime != null &&
                startTime.isAfter(currentTime) &&
                java.time.Duration.between(currentTime, startTime).toMinutes() <= 30
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCurrentClass -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                isUpcoming -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentClass || isUpcoming) 8.dp else 4.dp
        ),
        border = when {
            isCurrentClass -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            isUpcoming -> BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
            else -> null
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = routineItem.time.split(" - ")[0].trim(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = when {
                        isCurrentClass -> MaterialTheme.colorScheme.primary
                        isUpcoming -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                Text(
                    text = routineItem.duration,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                if (isCurrentClass) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Course details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = routineItem.courseCode,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (isCurrentClass) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "NOW",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = routineItem.teacherInitial,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Room ${routineItem.room}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                if (currentUser?.role == UserRole.TEACHER) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Batch ${routineItem.batch} • Section ${routineItem.section}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NoDayClassesContent(selectedDay: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EventBusy,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Classes on $selectedDay",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enjoy your free day!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RoutineScreenPreview() {
    DIUCampusScheduleTheme {
        RoutineScreen(navController = rememberNavController())
    }
}