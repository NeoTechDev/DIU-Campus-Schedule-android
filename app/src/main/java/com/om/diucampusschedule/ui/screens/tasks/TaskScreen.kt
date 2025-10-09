package com.om.diucampusschedule.ui.screens.tasks

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.om.diucampusschedule.R
import com.om.diucampusschedule.domain.model.ReminderOption
import com.om.diucampusschedule.domain.model.Task
import com.om.diucampusschedule.domain.model.TaskGroup
import com.om.diucampusschedule.ui.components.AddTaskBottomSheet
import com.om.diucampusschedule.ui.components.AddTaskGroupDialog
import com.om.diucampusschedule.ui.components.CustomToast
import com.om.diucampusschedule.ui.components.EditTaskGroupDialog
import com.om.diucampusschedule.ui.components.MultipleTaskShareDialog
import com.om.diucampusschedule.ui.components.ShareTaskDialog
import com.om.diucampusschedule.ui.components.ToastType
import com.om.diucampusschedule.ui.theme.AccentGreen
import com.om.diucampusschedule.ui.theme.AppPrimaryColorLight
import com.om.diucampusschedule.ui.theme.DIUCampusScheduleTheme
import com.om.diucampusschedule.ui.utils.ScreenConfig
import com.om.diucampusschedule.ui.viewmodel.ModernTaskViewModel
import com.om.diucampusschedule.ui.viewmodel.ModernTaskViewModel.SharingStatus
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(navController: NavController) {
    DIUCampusScheduleTheme {
        val context = LocalContext.current
        val taskViewModel: ModernTaskViewModel = hiltViewModel()
        val tasks by taskViewModel.tasks.collectAsState(initial = emptyList())
        val taskGroups by taskViewModel.taskGroups.collectAsState(initial = emptyList())
        val selectedGroupId by taskViewModel.selectedGroupId.collectAsState()

        // Multi-task sharing state
        val selectedTasks = remember { mutableStateListOf<Task>() }
        var isInSelectionMode by remember { mutableStateOf(false) }
        var isMainFabExpanded by remember { mutableStateOf(false) }

        // Confirmation dialog states  
        var showSingleShareConfirmation by remember { mutableStateOf<Task?>(null) }
        var showMultipleShareConfirmation by remember { mutableStateOf(false) }

        // Sharing state variables
        var taskToShare by remember { mutableStateOf<Task?>(null) }
        var showMultipleTaskShareDialog by remember { mutableStateOf(false) }
        val discoveredServices =
            remember { mutableStateListOf<android.net.nsd.NsdServiceInfo>() } // Network Service Discovery services
        val sharingStatus by taskViewModel.sharingStatus.collectAsState(initial = ModernTaskViewModel.SharingStatus.Idle)
        val taskReceived by taskViewModel.taskReceived.collectAsState(initial = null)

        // Load tasks when screen first loads
        LaunchedEffect(Unit) {
            taskViewModel.loadTasks()
        }

        var showBottomSheet by remember { mutableStateOf(false) }
        var showAddGroupDialog by remember { mutableStateOf(false) }
        var showEditGroupDialog by remember { mutableStateOf<Pair<Long, String>?>(null) }
        var taskToEdit by remember { mutableStateOf<Task?>(null) }
        var selectedTab by remember { mutableStateOf(0) } // 0 for Pending, 1 for Completed
        val tabCount = 2 // Number of tabs

        // Filter tasks by group and completion status
        val filteredTasks = if (selectedGroupId == 0L) {
            // "All Tasks" group - show all tasks
            tasks
        } else {
            // Filter by selected group
            tasks.filter { it.groupId == selectedGroupId }
        }

        // Sort tasks by id in descending order (newest added first, assuming id is auto-incremented)
        val pendingTasks = filteredTasks.filter { !it.isCompleted }.sortedByDescending { it.id }
        val completedTasks = filteredTasks.filter { it.isCompleted }.sortedByDescending { it.id }

        // Scroll state with reset capability
        val lazyListState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        // Reset scroll to top when screen loads or tasks change
        LaunchedEffect(tasks, selectedTab, selectedGroupId) {
            coroutineScope.launch {
                lazyListState.scrollToItem(0)
            }
        }

        // Exit selection mode when navigating between tabs or task groups
        LaunchedEffect(selectedTab, selectedGroupId) {
            if (isInSelectionMode) {
                isInSelectionMode = false
                selectedTasks.clear()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .run { ScreenConfig.run { withTopAppBar() } }
        ) {
            // Task Top App Bar - essential info
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Tasks",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Plan ahead with confidence",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if(isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                windowInsets = ScreenConfig.getTopAppBarWindowInsets(handleStatusBar = true),
                modifier = Modifier.fillMaxWidth()
            )
            if(!isSystemInDarkTheme()){
                // Subtle divider for modern look
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                )
            }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Animated menu items
                    AnimatedVisibility(
                        visible = isMainFabExpanded,
                        enter = fadeIn() + slideInVertically { it / 2 } + scaleIn(initialScale = 0.8f),
                        exit = fadeOut() + slideOutVertically { it / 2 } + scaleOut(targetScale = 0.8f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(bottom = 8.dp, end = 4.dp)
                        ) {
                            // Add Task action
                            ActionMenuItem(
                                icon = R.drawable.add_task,
                                label = "Add Task",
                                onClick = {
                                    showBottomSheet = true
                                    isMainFabExpanded = false
                                }
                            )

                            // Share Tasks action
                            if(selectedTab == 0){
                                ActionMenuItem(
                                    icon = R.drawable.share_solid_full,
                                    label = "Share Tasks",
                                    onClick = {
                                        if (isInSelectionMode) {
                                            if (selectedTasks.isEmpty()) {
                                                Toast.makeText(
                                                    context,
                                                    "Select tasks to share",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                showMultipleShareConfirmation = true
                                            }
                                        } else {
                                            isInSelectionMode = true
                                            Toast.makeText(
                                                context,
                                                "Select tasks to share",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        isMainFabExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Main FAB
                    val fabElevation by animateDpAsState(
                        targetValue = if (isMainFabExpanded) 8.dp else 6.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "fabElevation"
                    )

                    FloatingActionButton(
                        onClick = {
                            if (isInSelectionMode && selectedTasks.isEmpty()) {
                                // Exit selection mode if no tasks are selected
                                isInSelectionMode = false
                            } else if (isInSelectionMode && selectedTasks.isNotEmpty()) {
                                // If in selection mode with tasks selected, show confirmation dialog
                                showMultipleShareConfirmation = true
                            } else {
                                // Toggle FAB menu
                                isMainFabExpanded = !isMainFabExpanded
                            }
                        },
                        containerColor =
                            if (isInSelectionMode && selectedTasks.isEmpty() || isMainFabExpanded)
                                MaterialTheme.colorScheme.secondary
                            else if (isInSelectionMode && selectedTasks.isNotEmpty())
                                AccentGreen
                            else
                                MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.shadow(fabElevation, RoundedCornerShape(16.dp))
                    ) {
                        if (isInSelectionMode && selectedTasks.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.share_solid_full),
                                    contentDescription = "Share Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Share",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else if (isInSelectionMode) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = "Cancel Selection",
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                painter = if (isMainFabExpanded)
                                    painterResource(R.drawable.ic_close)
                                else
                                    painterResource(R.drawable.add),
                                contentDescription = if (isMainFabExpanded) "Close menu" else "Open menu",
                                tint = if(isMainFabExpanded) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = innerPadding.calculateStartPadding(LayoutDirection.Ltr), end = innerPadding.calculateEndPadding(LayoutDirection.Ltr), bottom = innerPadding.calculateBottomPadding())
                        .background(MaterialTheme.colorScheme.background)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                if (dragAmount > 20) { // Swipe right threshold reduced from 50 to 20
                                    selectedTab = (selectedTab - 1).coerceAtLeast(0)
                                } else if (dragAmount < -20) { // Swipe left threshold reduced from 50 to 20
                                    selectedTab = (selectedTab + 1).coerceAtMost(tabCount - 1)
                                }
                            }
                        }
                ) {
                    // Tab Row
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                        exit = fadeOut(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    ) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    Modifier
                                        .tabIndicatorOffset(tabPositions[selectedTab])
                                        .clip(
                                            RoundedCornerShape(8.dp)
                                        ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Pending",
                                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 18.sp,
                                            color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (pendingTasks.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        MaterialTheme.colorScheme.primary.copy(
                                                            alpha = 0.3f
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = pendingTasks.size.toString(),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.SemiBold,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Completed",
                                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 18.sp,
                                            color = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (completedTasks.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        MaterialTheme.colorScheme.primary.copy(
                                                            alpha = 0.3f
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = completedTasks.size.toString(),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.SemiBold,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Selection mode indicator
                    if (isInSelectionMode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if(pendingTasks.isNotEmpty()) {
                                Text(
                                    text = "${selectedTasks.size} selected",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                            else{
                                Text(
                                    text = "First create a task to select",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    lineHeight = 15.sp,
                                    maxLines = 2
                                )
                            }

                            TextButton(
                                onClick = {
                                    isInSelectionMode = false
                                    selectedTasks.clear()
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "Cancel",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Task Group Row - only show when not in selection mode
                    AnimatedVisibility(
                        visible = !isInSelectionMode,
                        enter = fadeIn() + slideInVertically { -it },
                        exit = fadeOut() + slideOutVertically { -it }
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            TaskGroupRow(
                                groups = taskGroups,
                                selectedGroupId = selectedGroupId,
                                onGroupSelected = { groupId ->
                                    taskViewModel.selectTaskGroup(groupId)
                                },
                                onAddGroupClick = {
                                    showAddGroupDialog = true
                                },
                                onDeleteGroup = { groupId ->
                                    taskViewModel.deleteTaskGroup(groupId)
                                },
                                onEditGroup = { groupId, groupName ->
                                    showEditGroupDialog = Pair(groupId, groupName)
                                }
                            )
                        }
                    }

                    // Task List with forced scroll reset
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ) { width -> width } + fadeIn() togetherWith
                                        slideOutHorizontally(
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        ) { width -> -width } + fadeOut()
                            } else {
                                slideInHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ) { width -> -width } + fadeIn() togetherWith
                                        slideOutHorizontally(
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        ) { width -> width } + fadeOut()
                            }
                        },
                        label = "TabContentAnimation"
                    ) { targetTab ->
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            when (targetTab) {
                                0 -> { // Pending Tasks
                                    if (pendingTasks.isEmpty()) {
                                        item {
                                            if (completedTasks.isEmpty()) {
                                                Spacer(modifier = Modifier.height(80.dp))
                                                EmptyState(
                                                    imageRes = R.drawable.no_task,
                                                    title = "No Tasks Yet",
                                                    message = "Add your to-dos and turn plans into action"
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.height(80.dp))
                                                EmptyState(
                                                    imageRes = R.drawable.all_task_completed,
                                                    title = "All Tasks Completed",
                                                    message = "Nice work!"
                                                )
                                            }
                                        }
                                    } else {
                                        items(pendingTasks, key = { it.id }) { task ->
                                            AnimatedVisibility(
                                                visible = true,
                                                enter = fadeIn(
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessLow
                                                    )
                                                ) +
                                                        slideInHorizontally(
                                                            animationSpec = spring(
                                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                                stiffness = Spring.StiffnessLow
                                                            )
                                                        ) { it },
                                                exit = fadeOut(
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessLow
                                                    )
                                                ) +
                                                        slideOutHorizontally(
                                                            animationSpec = spring(
                                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                                stiffness = Spring.StiffnessLow
                                                            )
                                                        ) { -it }
                                            ) {
                                                Column {
                                                    TaskCard(
                                                        task = task,
                                                        onUpdateTask = { updatedTask ->
                                                            taskViewModel.updateTask(updatedTask)
                                                            taskToEdit = null
                                                        },
                                                        onDeleteTask = { taskToDelete ->
                                                            taskViewModel.deleteTask(taskToDelete)
                                                            Toast.makeText(
                                                                context,
                                                                "Task has been deleted",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        },
                                                        onEditTask = {
                                                            taskToEdit = it; showBottomSheet = true
                                                        },
                                                        onShareTask = { task ->
                                                            showSingleShareConfirmation = task
                                                        },
                                                        isInSelectionMode = isInSelectionMode,
                                                        isSelected = selectedTasks.contains(task),
                                                        onSelectionChange = { task, isSelected ->
                                                            if (isSelected) {
                                                                if (!selectedTasks.contains(task)) {
                                                                    selectedTasks.add(task)
                                                                }
                                                            } else {
                                                                selectedTasks.remove(task)
                                                            }
                                                        },
                                                        enableContextMenu = !isInSelectionMode
                                                    )
                                                    HorizontalDivider(
                                                        thickness = 1.dp,
                                                        color = MaterialTheme.colorScheme.outline.copy(
                                                            alpha = 0.2f
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                1 -> { // Completed Tasks
                                    if (completedTasks.isEmpty()) {
                                        item {
                                            Spacer(modifier = Modifier.height(80.dp))
                                            EmptyState(
                                                imageRes = R.drawable.no_task,
                                                title = "No Completed Tasks",
                                                message = "Complete some tasks to see them here"
                                            )
                                        }
                                    } else {
                                        item {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        start = 16.dp,
                                                        end = 16.dp,
                                                        bottom = 8.dp
                                                    ),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                TextButton(
                                                    onClick = {
                                                        taskViewModel.deleteAllCompletedTasks()
                                                        Toast.makeText(
                                                            context,
                                                            "All completed tasks cleared",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    },
                                                    modifier = Modifier
                                                        .size(width = 75.dp, height = 32.dp),
                                                    shape = RoundedCornerShape(16.dp),
                                                    border = BorderStroke(
                                                        1.dp,
                                                        MaterialTheme.colorScheme.error
                                                    ),
                                                    colors = ButtonDefaults.textButtonColors(
                                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                                        contentColor = if (!isSystemInDarkTheme()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onErrorContainer
                                                    ),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Text(
                                                        text = "Clear All",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                        items(completedTasks, key = { it.id }) { task ->
                                            AnimatedVisibility(
                                                visible = true,
                                                enter = fadeIn(
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessLow
                                                    )
                                                ) +
                                                        slideInHorizontally(
                                                            animationSpec = spring(
                                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                                stiffness = Spring.StiffnessLow
                                                            )
                                                        ) { it },
                                                exit = fadeOut(
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessLow
                                                    )
                                                ) +
                                                        slideOutHorizontally(
                                                            animationSpec = spring(
                                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                                stiffness = Spring.StiffnessLow
                                                            )
                                                        ) { -it }
                                            ) {
                                                Column {
                                                    TaskCard(
                                                        task = task,
                                                        onUpdateTask = { updatedTask ->
                                                            taskViewModel.updateTask(updatedTask)
                                                            taskToEdit = null
                                                        },
                                                        onDeleteTask = { taskToDelete ->
                                                            taskViewModel.deleteTask(taskToDelete)
                                                        },
                                                        onEditTask = {
                                                            taskToEdit = it; showBottomSheet = true
                                                        },
                                                        onShareTask = { task ->
                                                            showSingleShareConfirmation = task
                                                        },
                                                        isInSelectionMode = isInSelectionMode,
                                                        isSelected = selectedTasks.contains(task),
                                                        onSelectionChange = { task, isSelected ->
                                                            if (isSelected) {
                                                                if (!selectedTasks.contains(task)) {
                                                                    selectedTasks.add(task)
                                                                }
                                                            } else {
                                                                selectedTasks.remove(task)
                                                            }
                                                        },
                                                        enableContextMenu = !isInSelectionMode
                                                    )
                                                    HorizontalDivider(
                                                        thickness = 1.dp,
                                                        color = MaterialTheme.colorScheme.outline.copy(
                                                            alpha = 0.2f
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        )

        if (showBottomSheet) {
            AddTaskBottomSheet(
                onAddTask = { task ->
                    if (taskToEdit == null) {
                        taskViewModel.addTask(task)
                    } else {
                        taskViewModel.updateTask(task.copy(id = taskToEdit!!.id))
                        taskToEdit = null
                    }
                    showBottomSheet = false
                },
                onDismiss = {
                    showBottomSheet = false
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

        if (showAddGroupDialog) {
            AddTaskGroupDialog(
                onDismissRequest = { showAddGroupDialog = false },
                onConfirm = { groupName ->
                    taskViewModel.addTaskGroup(groupName)
                    showAddGroupDialog = false
                }
            )
        }

        showEditGroupDialog?.let { (groupId, groupName) ->
            EditTaskGroupDialog(
                groupId = groupId,
                currentGroupName = groupName,
                onDismissRequest = { showEditGroupDialog = null },
                onConfirm = { id, newName ->
                    taskViewModel.updateTaskGroup(id, newName)
                    showEditGroupDialog = null
                }
            )
        }

        // Show what was in dialog before based on taskToShare being non-null
        if (taskToShare != null) {
            ShareTaskDialog(
                discoveredServices = discoveredServices,
                onDeviceSelected = { service ->
                    taskToShare?.let { task ->
                        taskViewModel.shareTask(service, task)
                    }
                    taskToShare = null // Dismiss dialog
                },
                onDismiss = { taskToShare = null },
                task = taskToShare
            )
        }

        // Multiple task sharing dialog
        if (showMultipleTaskShareDialog) {
            MultipleTaskShareDialog(
                discoveredServices = discoveredServices,
                onDeviceSelected = { service ->
                    if (selectedTasks.isNotEmpty()) {
                        taskViewModel.shareMultipleTasks(
                            service,
                            selectedTasks.toList(),
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Tasks shared successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                        showMultipleTaskShareDialog = false
                        isInSelectionMode = false
                        selectedTasks.clear()
                    }
                },
                onDismiss = {
                    showMultipleTaskShareDialog = false
                },
                taskCount = selectedTasks.size
            )
        }

        // Show sharing status feedback
        LaunchedEffect(sharingStatus) {
            when (val status = sharingStatus) {
                is SharingStatus.Success -> {
                    Toast.makeText(
                        context,
                        "Task shared with ${status.deviceName}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is SharingStatus.Error -> {
                    Toast.makeText(
                        context,
                        status.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {} // No action for other states
            }
        }

        // Task receiving notification
        var showTaskReceivedToast by remember {
            mutableStateOf<ModernTaskViewModel.ReceivedTaskInfo?>(
                null
            )
        }

        // Show received task feedback
        LaunchedEffect(taskReceived) {
            taskReceived?.let { taskInfo ->
                showTaskReceivedToast = taskInfo
                taskViewModel.onTaskReceivedEventHandled() // Reset the event
            }
        }

        // Show custom toast notification for received task
        showTaskReceivedToast?.let { taskInfo ->
            CustomToast(
                message = "New Task Received",
                subMessage = "\"${taskInfo.task.title}\" from ${taskInfo.senderName}",
                type = ToastType.RECEIVED_TASK,
                duration = 4000,
                onDismiss = { showTaskReceivedToast = null }
            )
        }

        // Single Task Share Handler
        showSingleShareConfirmation?.let { task ->
            taskToShare = task
            showSingleShareConfirmation = null
        }

        // Multiple Tasks Share Handler
        if (showMultipleShareConfirmation) {
            showMultipleTaskShareDialog = true
            showMultipleShareConfirmation = false
        }
        } // End of Scaffold
    } // End of Column
}

@Composable
fun EmptyState(imageRes: Int, title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ActionMenuItem(
    icon: Int,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun TaskCard(
    task: Task,
    onUpdateTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    onShareTask: (Task) -> Unit,
    enableContextMenu: Boolean = true,
    isInSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectionChange: (Task, Boolean) -> Unit = { _, _ -> },
    bgColor: Color = MaterialTheme.colorScheme.background,
    cardShape: RoundedCornerShape = RoundedCornerShape(0.dp)
) {
    var expanded by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateDpAsState(targetValue = Dp(offsetX))
    val scope = rememberCoroutineScope()
    var showContextMenu by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(Offset(0f, 0f)) }

    // Animation state for check/uncheck transition
    var isAnimating by remember { mutableStateOf(false) }

    // Animation progress
    val animationProgress by animateFloatAsState(
        targetValue = if (isAnimating) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        finishedListener = {
            if (isAnimating) {
                // Animation completed
                isAnimating = false
                // Update the actual task state
                onUpdateTask(task.copy(isCompleted = !task.isCompleted))
            }
        }
    )

    // Scale animation for pop effect
    val iconScale by animateFloatAsState(
        targetValue = if (isAnimating) 1.2f else 1f,
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        )
    )

    val swipeThreshold = 200f
    val maxSwipeDistance = 250f

    val checkIcon = painterResource(id = R.drawable.check)
    val uncheckIcon = painterResource(id = R.drawable.uncheck)
    val deleteIcon = painterResource(id = R.drawable.delete)
    val editIcon = painterResource(id = R.drawable.edit_24px)
    val reminderIcon = painterResource(id = R.drawable.active_reminder)
    val shareIcon = painterResource(id = R.drawable.share)
    val selectIcon =
        painterResource(id = if (isSelected) R.drawable.check_circle else R.drawable.circle)

    // Date formatting and comparison
    val dateFormat = SimpleDateFormat("MM-dd-yyyy", Locale.US)
    val todayDate = dateFormat.format(Date())
    val currentDate = Date()

    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -1)
    val yesterdayDate = dateFormat.format(calendar.time)

    // Reset calendar to today and add 1 day for tomorrow
    calendar.time = Date()
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    val tomorrowDate = dateFormat.format(calendar.time)

    val isToday = task.date == todayDate
    val isYesterday = task.date == yesterdayDate
    val isTomorrow = task.date == tomorrowDate
    val isPastDate = try {
        val taskDate = dateFormat.parse(task.date)
        taskDate != null && taskDate.before(currentDate) && !isToday
    } catch (e: Exception) {
        false
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(
                color = if (isInSelectionMode && isSelected)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else if (offsetX > 0)
                    MaterialTheme.colorScheme.error
                else if (offsetX < 0)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.background
            )
            .pointerInput(Unit) {
                if (!isInSelectionMode) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < swipeThreshold && offsetX > -swipeThreshold) {
                                offsetX = 0f
                            }
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount / density).coerceIn(
                            -maxSwipeDistance,
                            maxSwipeDistance
                        )
                    }
                }
            }
    ) {
        if (offsetX > 0) {
            Icon(
                painter = deleteIcon,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
            )
        } else if (offsetX < 0) {
            Icon(
                painter = editIcon,
                contentDescription = "Edit",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = animatedOffsetX)
                .background(if (!isSystemInDarkTheme()) Color.White else bgColor)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (isInSelectionMode) {
                                onSelectionChange(task, !isSelected)
                            } else {
                                expanded = !expanded
                            }
                        },
                        onLongPress = { offset ->
                            if (enableContextMenu && !isInSelectionMode) {
                                menuOffset = offset
                                showContextMenu = true
                            }
                        }
                    )
                }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isInSelectionMode) {
                    // Selection mode UI
                    Icon(
                        painter = selectIcon,
                        contentDescription = if (isSelected) "Selected" else "Not Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(28.dp)
                            .padding(end = 8.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (isInSelectionMode) {
                                    onSelectionChange(task, !isSelected)
                                }
                            }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    // Task title with strike-through animation when completed
                    Text(
                        text = task.title,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                    )

                    AnimatedVisibility(
                        visible = expanded && task.description.isNotEmpty(),
                        enter = expandVertically(animationSpec = tween(300)) + fadeIn(
                            animationSpec = tween(
                                300
                            )
                        ),
                        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(
                            animationSpec = tween(
                                300
                            )
                        )
                    ) {
                        Text(
                            text = task.description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Date and time formatting (unchanged)
                    val formattedDateTime = formatDateTime(task.date, task.time)
                    if (formattedDateTime.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            val dateLabelColor = when {
                                isPastDate && !task.isCompleted -> MaterialTheme.colorScheme.error
                                isPastDate && task.isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant
                                isToday -> MaterialTheme.colorScheme.primary
                                isTomorrow -> Color(0xFF00ab84)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            val timeLabelColor = when {
                                isPastDate && !task.isCompleted -> MaterialTheme.colorScheme.error
                                isPastDate && task.isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant
                                isToday -> MaterialTheme.colorScheme.primary
                                isTomorrow -> Color(0xFF00ab84)
                                else -> MaterialTheme.colorScheme.primary
                            }

                            if (isYesterday) {
                                DateLabel(
                                    text = "Yesterday",
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                                )
                            } else if (isToday) {
                                DateLabel(text = "Today", color = MaterialTheme.colorScheme.primary)
                            } else if (isTomorrow) {
                                DateLabel(
                                    text = "Tomorrow",
                                    color = Color(0xFF00ab84)
                                )
                            } else {
                                Text(
                                    text = formattedDateTime,
                                    color = dateLabelColor,
                                    fontSize = 14.sp,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            if ((isToday || isYesterday || isTomorrow) && task.time.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                DateLabel(
                                    text = task.time,
                                    color = if (isYesterday) {
                                        if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                                    } else timeLabelColor
                                )
                            } else if (!isToday && !isYesterday && !isTomorrow && task.time.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = task.time,
                                    color = if (isPastDate && !task.isCompleted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            // Reminder Icon Display
                            if (task.reminderOption != ReminderOption.NONE) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center // Center the content horizontally
                                ) {
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Icon(
                                        painter = reminderIcon,
                                        contentDescription = "Reminder Set",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .align(Alignment.CenterVertically) // Align icon vertically
                                    )
                                }
                            }
                        }
                    }
                }

                // Check/uncheck button (only shown when not in selection mode)
                if (!isInSelectionMode) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(4.dp)
                    ) {
                        // Background circle with pulse effect
                        if (isAnimating) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = AppPrimaryColorLight,
                                    radius = size.minDimension / 2 * (1f + animationProgress * 0.3f),
                                    alpha = (1f - animationProgress) * 0.3f
                                )
                            }
                        }

                        // Animation transition between check and uncheck icons
                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                    alpha = if (isAnimating) {
                                        // Fade out the current icon during the first half of animation
                                        if (animationProgress < 0.5f) 1f - (animationProgress * 2)
                                        // Fade in the new icon during the second half
                                        else (animationProgress - 0.5f) * 2
                                    } else 1f
                                }
                        ) {
                            // When animation crosses the midpoint, switch icons
                            if (isAnimating && animationProgress >= 0.5f) {
                                // During animation, show the opposite of the current state
                                Icon(
                                    painter = if (task.isCompleted) uncheckIcon else checkIcon,
                                    contentDescription = if (task.isCompleted) "Uncheck" else "Check",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                // Show current state
                                Icon(
                                    painter = if (task.isCompleted) checkIcon else uncheckIcon,
                                    contentDescription = if (task.isCompleted) "Check" else "Uncheck",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Clickable overlay for the entire check area
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null // Remove default ripple
                                ) {
                                    if (!isAnimating) {
                                        isAnimating = true
                                    }
                                }
                        )
                    }
                }
            }

            // Context Menu for actions
            if (enableContextMenu && showContextMenu) {
                Popup(
                    alignment = Alignment.TopEnd, // or customize based on position
                    onDismissRequest = { showContextMenu = false }
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp,
                    ) {
                        Column(
                            modifier = Modifier
                                .width(120.dp)
                                .padding(vertical = 8.dp)
                        ) {
                            // Title
                            Text(
                                text = "Task Actions",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )

                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            MenuItem("Share", shareIcon, onClick = {
                                onShareTask(task)
                                showContextMenu = false
                            })

                            MenuItem("Edit", editIcon, onClick = {
                                onEditTask(task)
                                showContextMenu = false
                            })

                            MenuItem(
                                "Delete", deleteIcon,
                                textColor = MaterialTheme.colorScheme.error,
                                iconTint = MaterialTheme.colorScheme.error,
                                onClick = {
                                    onDeleteTask(task)
                                    showContextMenu = false
                                }
                            )
                        }
                    }
                }
            }

        }

        when {
            offsetX >= swipeThreshold -> {
                scope.launch {
                    offsetX = 0f
                    onDeleteTask(task)
                }
            }

            offsetX <= -swipeThreshold -> {
                scope.launch {
                    offsetX = 0f
                    onEditTask(task)
                }
            }
        }
    }
}

@Composable
private fun DateLabel(text: String, color: Color) {
    Box(
        modifier = Modifier
            .padding(top = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.3f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun formatDateTime(date: String, time: String): String {
    if (date.isEmpty()) {
        return ""
    }

    return when {
        date.isNotEmpty() && time.isNotEmpty() -> {
            try {
                val inputDateFormat = SimpleDateFormat("MM-dd-yyyy", Locale.US)
                val outputDateFormat = SimpleDateFormat("EEEE, MMM d", Locale.US)
                val parsedDate = inputDateFormat.parse(date)
                val formattedDate = outputDateFormat.format(parsedDate!!)
                "$formattedDate"
            } catch (e: Exception) {
                "$date"
            }
        }

        date.isNotEmpty() -> {
            try {
                val inputFormat = SimpleDateFormat("MM-dd-yyyy", Locale.US)
                val outputFormat = SimpleDateFormat("EEEE, MMM d", Locale.US)
                val parsedDate = inputFormat.parse(date)
                outputFormat.format(parsedDate!!)
            } catch (e: Exception) {
                date
            }
        }

        time.isNotEmpty() -> time
        else -> ""
    }
}

@Composable
fun MenuItem(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = text,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = textColor
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskGroupRow(
    groups: List<TaskGroup>,
    selectedGroupId: Long,
    onGroupSelected: (Long) -> Unit,
    onAddGroupClick: () -> Unit,
    onDeleteGroup: (Long) -> Unit,
    onEditGroup: ((Long, String) -> Unit)? = null
) {
    // Keep track of which group's dropdown is expanded and its position
    var expandedDropdownGroupId by remember { mutableStateOf<Long?>(null) }
    // Offset for positioning the dropdown
    var dropdownOffset by remember { mutableStateOf(DpOffset(0.dp, 0.dp)) }

    val deletableGroups = groups.filter { it.id != 0L } // Exclude default group

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display all groups
        items(groups) { group ->
            val isSelected = group.id == selectedGroupId
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surface,
                animationSpec = tween(durationMillis = 300),
                label = "BackgroundColor"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface,
                animationSpec = tween(durationMillis = 300),
                label = "TextColor"
            )

            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(
                                alpha = 0.3f
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .combinedClickable(
                            onClick = { onGroupSelected(group.id) },
                            onLongClick = {
                                if (group.id != 0L) { // Don't show options for default "All Tasks" group
                                    expandedDropdownGroupId = group.id
                                    // Set dropdown to appear below the item
                                    dropdownOffset = DpOffset(0.dp, 8.dp)
                                }
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = group.name,
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 13.sp
                    )
                }

                // Dropdown Menu for the group
                DropdownMenu(
                    containerColor = MaterialTheme.colorScheme.surface,
                    expanded = expandedDropdownGroupId == group.id,
                    onDismissRequest = { expandedDropdownGroupId = null },
                    offset = dropdownOffset
                ) {
                    // Edit option
                    if (onEditGroup != null) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Edit Group",
                                    fontSize = 16.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit group",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                onEditGroup(group.id, group.name)
                                expandedDropdownGroupId = null
                            }
                        )
                    }

                    // Delete option
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Delete Group",
                                fontSize = 16.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete group",
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            onDeleteGroup(group.id)
                            expandedDropdownGroupId = null
                        }
                    )
                }
            }
        }

        // Add new group button
        item {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable { onAddGroupClick() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new group",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}