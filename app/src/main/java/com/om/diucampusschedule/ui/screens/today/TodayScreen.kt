package com.om.diucampusschedule.ui.screens.today


import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.om.diucampusschedule.R
import com.om.diucampusschedule.domain.model.Task
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.ui.components.AddTaskBottomSheet
import com.om.diucampusschedule.ui.navigation.Screen
import com.om.diucampusschedule.ui.screens.notices.NoticesViewModel
import com.om.diucampusschedule.ui.screens.today.components.CalendarViewComponent
import com.om.diucampusschedule.ui.screens.today.components.FindCourseBottomSheetContent
import com.om.diucampusschedule.ui.screens.today.components.MiniCalendar
import com.om.diucampusschedule.ui.screens.today.components.TodayActionButton
import com.om.diucampusschedule.ui.screens.today.components.TodayRoutineContent
import com.om.diucampusschedule.ui.screens.today.components.calculateDailyEventCounts
import com.om.diucampusschedule.ui.utils.ScreenConfig
import com.om.diucampusschedule.ui.utils.TopAppBarIconSize.topbarIconSize
import com.om.diucampusschedule.ui.viewmodel.AuthViewModel
import com.om.diucampusschedule.ui.viewmodel.ModernTaskViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    navController: NavController,
    onOpenDrawer: () -> Unit = {}, // Add drawer parameter
    authViewModel: AuthViewModel = hiltViewModel(),
    todayViewModel: TodayViewModel = hiltViewModel(),
    taskViewModel: ModernTaskViewModel = hiltViewModel(),
    noticesViewModel: NoticesViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val selectedDate by todayViewModel.selectedDate.collectAsStateWithLifecycle()
    val todayState by todayViewModel.uiState.collectAsStateWithLifecycle()
    val taskGroups by taskViewModel.taskGroups.collectAsState(initial = emptyList())
    val unreadNotificationCount by todayViewModel.unreadNotificationCount.collectAsState()

    val focusManager = LocalFocusManager.current
    
    // Pull-to-refresh state (Material 3) - using a simple isRefreshing boolean
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    
    // Sync refresh state with ViewModel loading state
    LaunchedEffect(todayState.isLoading) {
        if (!todayState.isLoading) {
            isRefreshing = false
        }
    }
    
    // State for action button
    var isActionButtonExpanded by remember { mutableStateOf(false) }
    
    // State for Find Course bottom sheet
    var showFindCourseBottomSheet by remember { mutableStateOf(false) }
    val findCourseSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // State for Full Calendar bottom sheet
    var showFullCalendarBottomSheet by remember { mutableStateOf(false) }
    val fullCalendarSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentCalendarMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    
    // State for Add/Edit Task bottom sheet
    var showTaskBottomSheet by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    
    // No more ModalSheet for notices; navigation will be used
    
    // Swipe gesture states
    var horizontalOffset by remember { mutableStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = horizontalOffset,
        animationSpec = spring(dampingRatio = 0.95f, stiffness = 120f), // Smoother spring
        label = "horizontalOffset"
    )
    
    // Handle back button press to close action button
    BackHandler(enabled = isActionButtonExpanded) {
        isActionButtonExpanded = false
    }
    
    // Reset to today's date when screen is first composed or revisited
    LaunchedEffect(Unit) {
        todayViewModel.resetToToday()
        // Preload nearby dates for smoother navigation
        todayViewModel.preloadNearbyDates()
    }
    
    // Preload nearby dates when the selected date changes
    LaunchedEffect(selectedDate) {
        todayViewModel.preloadNearbyDates()
    }
    
    // Clear cache when the screen is disposed to free memory
    DisposableEffect(Unit) {
        onDispose {
            todayViewModel.clearCache()
        }
    }

    // Main content area with pull-to-refresh and swipe gesture support
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            todayViewModel.refreshCurrentData()
            // Reset refresh state after a delay (in real implementation, this would be set when data loading completes)
            // You should set isRefreshing = false when todayState.isLoading changes to false
        },
        state = pullToRefreshState,
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .run { ScreenConfig.run { withTopAppBar() } }
        ) {
            // Custom Top App Bar with Header and Calendar content
            CustomTopAppBar(
                user = authState.user,
                selectedDate = selectedDate,
                todayViewModel = todayViewModel,
                unreadNotificationCount = unreadNotificationCount,
                onProfileClick = {
//                    onOpenDrawer() // Open drawer instead of navigating to profile
                    navController.navigate(Screen.Profile.route)
                },
                onNotificationClick = {
                    noticesViewModel.fetchNotices()
                    navController.navigate(Screen.Notices.route)
                },
                onMenuClick = {
                    onOpenDrawer()
                },
                onCalendarClick = {
                    currentCalendarMonth = YearMonth.from(selectedDate)
                    showFullCalendarBottomSheet = true
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isActionButtonExpanded, selectedDate) {
                        if (isActionButtonExpanded) {
                            detectTapGestures(
                                onTap = {
                                    isActionButtonExpanded = false
                                    focusManager.clearFocus()
                                }
                            )
                        } else {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    val swipeThreshold =
                                        80f // Lower threshold for more responsive swipe
                                    if (horizontalOffset > swipeThreshold) {
                                        todayViewModel.selectDate(selectedDate.minusDays(1))
                                    } else if (horizontalOffset < -swipeThreshold) {
                                        todayViewModel.selectDate(selectedDate.plusDays(1))
                                    }
                                    // Animate offset back to zero for smooth snap
                                    horizontalOffset = 0f
                                },
                                onDragCancel = {
                                    horizontalOffset = 0f
                                }
                            ) { change, dragAmount ->
                                change.consume()
                                horizontalOffset =
                                    (horizontalOffset + dragAmount * 0.7f).coerceIn(-350f, 350f)
                            }
                        }
                    }
            ) {
                // Animated content container with offset and fade for swipe feedback
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(animatedOffset.toInt(), 0) }
                        .graphicsLayer {
                            alpha =
                                1f - (kotlin.math.abs(animatedOffset) / 700f).coerceIn(0f, 0.25f)
                        } // Subtle fade while swiping
                ) {
                    AnimatedContent(
                        targetState = selectedDate,
                        transitionSpec = {
                            val direction = if (targetState.isAfter(initialState)) {
                                AnimatedContentTransitionScope.SlideDirection.Left
                            } else {
                                AnimatedContentTransitionScope.SlideDirection.Right
                            }
                            ContentTransform(
                                targetContentEnter = slideIntoContainer(
                                    towards = direction,
                                    animationSpec = spring(dampingRatio = 0.95f, stiffness = 120f)
                                ) + fadeIn(animationSpec = tween(350)),
                                initialContentExit = slideOutOfContainer(
                                    towards = direction,
                                    animationSpec = spring(dampingRatio = 0.95f, stiffness = 120f)
                                ) + fadeOut(animationSpec = tween(250))
                            )
                        },
                        label = "dateContentAnimation"
                    ) { animatedDate ->
                        val animatedTodayState = if (animatedDate == selectedDate) todayState else {
                            todayState.copy(
                                selectedDate = animatedDate,
                                routineItems = emptyList(),
                                tasks = emptyList(),
                                isLoading = false,
                                hasLoadedOnce = true // Set to true for animation states to prevent loading
                            )
                        }
                        TodayRoutineContent(
                            routineItems = animatedTodayState.routineItems,
                            tasks = animatedTodayState.tasks,
                            currentUser = animatedTodayState.currentUser,
                            isLoading = animatedTodayState.isLoading,
                            hasLoadedOnce = animatedTodayState.hasLoadedOnce,
                            getCourseName = todayViewModel::getCourseName,
                            onClassClick = { _ -> },
                            onUpdateTask = todayViewModel::updateTask,
                            onDeleteTask = todayViewModel::deleteTask,
                            onEditTask = { task ->
                                taskToEdit = task
                                showTaskBottomSheet = true
                            },
                            onTeacherClick = { teacherInitial ->
                                navController.navigate(Screen.FacultyInfo.createRoute(teacherInitial))
                            },
                            isToday = animatedDate == LocalDate.now(),
                            modifier = Modifier.fillMaxSize(),
                            noContentImage = if (animatedDate.dayOfWeek == DayOfWeek.FRIDAY) painterResource(
                                id = R.drawable.muslim
                            ) else painterResource(id = R.drawable.sleep),
                            noScheduleMessages = if (animatedDate.dayOfWeek == DayOfWeek.FRIDAY) "It's Friday!" else if (animatedDate == LocalDate.now()) "Nothing scheduled today!" else "Nothing scheduled that day",
                            noScheduleSubMessage = if (animatedDate.dayOfWeek == DayOfWeek.FRIDAY) "Offer your Jumma prayer, may Allah grant barakah in your life." else if (animatedDate == LocalDate.now()) "No classes or tasks scheduled for today. Enjoy your free time!" else "No classes or tasks will be scheduled for that day. All yours to chill and enjoy!",
                            // Maintenance mode parameters for class routines only
                            isMaintenanceMode = animatedTodayState.isMaintenanceMode,
                            maintenanceMessage = animatedTodayState.maintenanceMessage,
                            isSemesterBreak = animatedTodayState.isSemesterBreak,
                            updateType = animatedTodayState.updateType,
                            selectedDate = animatedDate
                        )
                    }
                }

                // Action Button positioned at bottom right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    TodayActionButton(
                        isExpanded = isActionButtonExpanded,
                        onToggleExpand = { isActionButtonExpanded = !isActionButtonExpanded },
                        onFindCourseClick = {
                            isActionButtonExpanded = false
                            showFindCourseBottomSheet = true
                        },
                        onAddTaskClick = {
                            isActionButtonExpanded = false
                            taskToEdit = null // Clear any existing task to edit
                            showTaskBottomSheet = true
                        },
                        onFacultyInfoClick = {
                            isActionButtonExpanded = false
                            navController.navigate(Screen.FacultyInfo.route)
                        }
                    )
                }
            }

            // Error handling
            todayState.error?.let { error ->
                LaunchedEffect(error) {
                    // Clear cache on error to ensure fresh data on retry
                    todayViewModel.clearCache()
                    // Show error in snackbar or handle as needed
                    // For now, just retry automatically
                    todayViewModel.retryLastAction()
                }
            }
        }
    }
    
    // Find Course Bottom Sheet
    if (showFindCourseBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFindCourseBottomSheet = false },
            sheetState = findCourseSheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            Color.Gray.copy(alpha = 0.3f),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        ) {
            FindCourseBottomSheetContent(
                onDismiss = { showFindCourseBottomSheet = false }
            )
        }
    }
    
    // Add/Edit Task Bottom Sheet
    if (showTaskBottomSheet) {
        AddTaskBottomSheet(
            onAddTask = { task ->
                if (taskToEdit == null) {
                    taskViewModel.addTask(task)
                } else {
                    taskViewModel.updateTask(task.copy(id = taskToEdit!!.id))
                    taskToEdit = null
                }
                showTaskBottomSheet = false
            },
            onDismiss = {
                showTaskBottomSheet = false
                taskToEdit = null
            },
            existingTask = taskToEdit,
            taskGroups = taskGroups,
            selectedGroupId = if (taskToEdit != null) taskToEdit!!.groupId else 0L, // Default to "All Tasks" for new tasks
            onAddTaskGroup = { groupName ->
                taskViewModel.addTaskGroup(groupName)
            }
        )
    }
    
    // Full Calendar Bottom Sheet
    if (showFullCalendarBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFullCalendarBottomSheet = false },
            sheetState = fullCalendarSheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            Color.Gray.copy(alpha = 0.3f),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        ) {
            CalendarViewComponent(
                onDismiss = { showFullCalendarBottomSheet = false },
                onDateSelected = { selectedDate ->
                    todayViewModel.selectDate(selectedDate)
                    showFullCalendarBottomSheet = false
                },
                currentMonth = currentCalendarMonth,
                onMonthChanged = { newMonth ->
                    currentCalendarMonth = newMonth
                },
                dailyEventCounts = calculateDailyEventCountsForCalendar(
                    todayViewModel = todayViewModel,
                    yearMonth = currentCalendarMonth
                )
            )
        }
    }
    
    // Notices ModalSheet removed; handled by navigation to NoticesScreen
}

/**
 * Calculate daily event counts for calendar using ViewModel methods
 */
@Composable
private fun calculateDailyEventCountsForCalendar(
    todayViewModel: TodayViewModel,
    yearMonth: YearMonth
): Map<LocalDate, Pair<Int, Int>> {
    var dailyEventCounts by remember(yearMonth) { mutableStateOf<Map<LocalDate, Pair<Int, Int>>>(emptyMap()) }
    var isLoading by remember(yearMonth) { mutableStateOf(false) }
    
    LaunchedEffect(yearMonth) {
        isLoading = true
        try {
            // Use coroutineScope for concurrent loading
            coroutineScope {
                val routineDeferred = async { todayViewModel.getAllWeekRoutineItems() }
                val tasksDeferred = async { todayViewModel.getAllTasksForMonth(yearMonth) }
                
                val allRoutineItems = routineDeferred.await()
                val allTasks = tasksDeferred.await()
                
                dailyEventCounts = calculateDailyEventCounts(
                    routineItems = allRoutineItems,
                    tasks = allTasks,
                    yearMonth = yearMonth
                )
            }
        } finally {
            isLoading = false
        }
    }
    
    return dailyEventCounts
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTopAppBar(
    user: User?,
    selectedDate: LocalDate,
    todayViewModel: TodayViewModel,
    unreadNotificationCount: Int,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onMenuClick: () -> Unit,
    onCalendarClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                clip = false
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        // Original TopAppBar
        TopAppBar(
            title = {
                // Left side: User profile section only
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onProfileClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clean profile picture
                    ProfilePicture(
                        user = user,
                        size = 36.dp
                    )
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    // Clean user info
                    UserInfoSection(
                        user = user,
                        modifier = Modifier.weight(1f)
                    )
                }
            },
            actions = {
                // "Back to Today" button - only show when not on current date
                if (selectedDate != LocalDate.now()) {
                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                            .clickable {
                                todayViewModel.selectDate(LocalDate.now())
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.undo_24px),
                                contentDescription = "Back to Today",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                
                // Right side: Notification + Menu icons
                IconButton(
                    onClick = onNotificationClick
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotificationCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ) {
                                    Text(
                                        text = if (unreadNotificationCount > 99) "99+" else unreadNotificationCount.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(topbarIconSize)
                        )
                    }
                }

                IconButton(
                    onClick = onMenuClick
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.apps),
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            windowInsets = ScreenConfig.getTopAppBarWindowInsets(handleStatusBar = true)
        )
        
        // Header with "Today's Schedule" and Date
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 4.dp)
                .clickable (
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        onCalendarClick()
                    }
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: Today's Schedule title
            Text(
                text = if(selectedDate == LocalDate.now()) "Today's Schedule" else "Schedule On",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 14.sp
            )
            
            // Right side: Date with dropdown arrow
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Date with dropdown arrow on the same row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Open Calendar",
                        tint = if (isPressed) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())),
                        color = if (isPressed) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 14.sp
                    )
                }
                // Day below the date - no padding
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())).uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 11.sp
                )
            }
        }
        
        // Mini Calendar Section
        MiniCalendar(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                todayViewModel.selectDate(date)
            },
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun ProfilePicture(
    user: User?,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profileUrl = user?.profilePictureUrl
    var isPressed by remember { mutableStateOf(false) }
    
    // Animation for press effect
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "profile_scale"
    )
    
    // Dynamic background color animation
    val backgroundColor by animateColorAsState(
        targetValue = if (user?.role?.name == "STUDENT") {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.tertiary
        },
        animationSpec = tween(300),
        label = "background_color"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.9f),
                        backgroundColor.copy(alpha = 0.7f)
                    )
                )
            )
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!profileUrl.isNullOrEmpty()) {
            // Use SubcomposeAsyncImage for better error handling
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(profileUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                loading = {
                    // Show initials while loading
                    ProfileInitials(
                        user = user,
                        size = size
                    )
                },
                error = {
                    // Show initials if error occurs
                    ProfileInitials(
                        user = user,
                        size = size
                    )
                }
            )
        } else {
            // Show initials when no profile URL
            ProfileInitials(
                user = user,
                size = size
            )
        }
    }
}

@Composable
private fun ProfileInitials(
    user: User?,
    size: androidx.compose.ui.unit.Dp
) {
    Text(
        text = getUserInitials(user),
        style = when {
            size >= 40.dp -> MaterialTheme.typography.titleMedium
            size >= 32.dp -> MaterialTheme.typography.titleSmall
            else -> MaterialTheme.typography.labelLarge
        },
        color = MaterialTheme.colorScheme.onPrimary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun UserInfoSection(
    user: User?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        // User name - smaller and cleaner
        Text(
            text = user?.name ?: "User",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        // Department on second line
        Text(
            text = user?.department ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Batch-Section / Initial on first line
        Text(
            text = getUserBatchSection(user),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Get user initials for profile picture
 */
private fun getUserInitials(user: User?): String {
    return if (user?.role?.name == "TEACHER") {
        user.initial.takeIf { !it.isNullOrBlank() } ?: "T"
    } else {
        user?.name?.split(" ")?.take(2)?.mapNotNull { it.firstOrNull()?.uppercaseChar() }?.joinToString("")?.takeIf { it.isNotBlank() } ?: "U"
    }
}

/**
 * Get batch-section for students or initial for teachers
 */
private fun getUserBatchSection(user: User?): String {
    return if (user?.role?.name == "STUDENT") {
        val batch = user.batch.takeIf { !it.isNullOrBlank() } ?: ""
        val section = user.section.takeIf { !it.isNullOrBlank() } ?: ""
        val labSection = user.labSection.takeIf { !it.isNullOrBlank() } ?: ""
        if (batch.isNotEmpty() && section.isNotEmpty()) {
            "$batch-$section • $labSection"
        } else ""
    } else {
        user?.initial.takeIf { !it.isNullOrBlank() } ?: ""
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TodayScreenPreview() {
    val navController = rememberNavController()
    TodayScreen(navController = navController)
}
