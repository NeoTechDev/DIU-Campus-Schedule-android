package com.om.diucampusschedule.ui.screens.routine

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.om.diucampusschedule.R
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.model.UserRole
import com.om.diucampusschedule.ui.components.DownloadOptionsBottomSheet
import com.om.diucampusschedule.ui.components.FilterRoutinesBottomSheet
import com.om.diucampusschedule.ui.components.GenerationProgressDialog
import com.om.diucampusschedule.ui.components.SuccessDialog
import com.om.diucampusschedule.ui.components.generateRoutineImage
import com.om.diucampusschedule.ui.components.generateRoutinePdf
import com.om.diucampusschedule.ui.theme.DIUCampusScheduleTheme
import com.om.diucampusschedule.ui.theme.RobotoFontFamily
import com.om.diucampusschedule.ui.utils.ScreenConfig
import com.om.diucampusschedule.ui.utils.TopAppBarIconSize.topbarIconSize
import com.om.diucampusschedule.ui.viewmodel.RoutineFilter
import com.om.diucampusschedule.ui.viewmodel.RoutineViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Custom colors for the table design
private val AppPrimaryColorLight = Color(0xFF3B82F6)
private val AppPrimaryColor = Color(0xFF1A56DB)
private val AppBackgroundColorLight = Color(0xFFF8FAFD)
private val SoftBlue = Color(0xFFE0F2FE)

// Time formatter for 12-hour format
private val formatter12HourUS = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(
    navController: NavController,
    viewModel: RoutineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showDownloadSheet by remember { mutableStateOf(false) }
    var showProgressDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var progressTitle by remember { mutableStateOf("") }
    var progressMessage by remember { mutableStateOf("") }
    var progressType by remember { mutableStateOf("PDF") }
    var isPdfGenerating by remember { mutableStateOf(false) }
    var isImageGenerating by remember { mutableStateOf(false) }
    var generatedFileUri by remember { mutableStateOf<Uri?>(null) }
    var generatedFileName by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Only trigger maintenance check when screen is first composed
    LaunchedEffect(Unit) {
        android.util.Log.d("RoutineScreen", "Screen composed - triggering onScreenResumed")
        viewModel.onScreenResumed()
    }

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
        android.util.Log.d("RoutineScreen", "  - isFiltered: ${uiState.isFiltered}")
        android.util.Log.d("RoutineScreen", "  - filteredItems: ${uiState.filteredRoutineItems.size}")
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
                .run { ScreenConfig.run { withTopAppBar() } }
        ) {
            // Clean Top App Bar - only essential info
            CleanRoutineTopAppBar(
                user = uiState.currentUser,
                onRefreshClick = { viewModel.refreshRoutine() },
                isRefreshing = uiState.isRefreshing,
                refreshRotation = refreshRotation,
                onDownloadClick = { showDownloadSheet = true }
            )

            // Offline indicator below top app bar
            if (uiState.isOffline) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Offline Mode",
                        fontFamily = RobotoFontFamily,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        lineHeight = 10.sp
                    )
                }
            }

            // Minimal update notification
            if (uiState.hasAvailableUpdates && uiState.updateMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            RoundedCornerShape(0.dp)
                        )
                        .clickable { viewModel.refreshRoutine() }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Subtle indicator dot
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                        )

                        // Compact message
                        Text(
                            text = "Updates available",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )

                        // Subtle call to action
                        Text(
                            text = "Tap to refresh",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
            
            // Routine Info Bar - secondary information below top bar
            RoutineInfoBar(
                effectiveFrom = uiState.effectiveFrom,
                isFiltered = uiState.isFiltered,
                currentFilter = uiState.currentFilter,
                defaultFilterText = viewModel.getDefaultFilterText(),
                onFilterClick = { showFilterSheet = true },
                onClearFilterClick = { viewModel.clearFilter() }
            )

            when {
                uiState.isLoading && uiState.routineItems.isEmpty() -> {
                    android.util.Log.d("RoutineScreen", "Showing LoadingContent")
                    LoadingContent()
                }

                // Check maintenance mode FIRST, before error states
                uiState.isMaintenanceMode || uiState.activeDays.isEmpty() -> {
                    android.util.Log.d(
                        "RoutineScreen",
                        "Showing EmptyContent - maintenance: ${uiState.isMaintenanceMode}, activeDays empty: ${uiState.activeDays.isEmpty()}"
                    )
                    EmptyContent(
                        isMaintenanceMode = uiState.isMaintenanceMode,
                        maintenanceMessage = uiState.maintenanceMessage,
                        isSemesterBreak = uiState.isSemesterBreak,
                        updateType = uiState.updateType
                    )
                }

                uiState.error != null && uiState.routineItems.isEmpty() -> {
                    android.util.Log.d(
                        "RoutineScreen",
                        "Showing ErrorContent: ${uiState.error!!.message}"
                    )
                    ErrorContent(
                        error = uiState.error!!.message ?: "An unknown error occurred",
                        isOffline = uiState.isOffline,
                        onRetry = { viewModel.retryLastAction() },
                        onDismiss = { viewModel.clearError() }
                    )
                }

                else -> {
                    android.util.Log.d(
                        "RoutineScreen",
                        "Showing RoutineContent with ${uiState.routineItems.size} items"
                    )
                    RoutineContent(
                        allDays = uiState.allDays,
                        activeDays = uiState.activeDays,
                        selectedDay = uiState.selectedDay,
                        routineItems = viewModel.getDisplayRoutineItems(), // Use filtered or all routine items
                        allTimeSlots = uiState.allTimeSlots, // Pass the sorted time slots
                        isRefreshing = uiState.isRefreshing,
                        currentUser = uiState.currentUser,
                        onDaySelected = { day -> viewModel.selectDay(day) },
                        onRefresh = { viewModel.refreshRoutine() }
                    )
                }
            }
        }
    }
    
    // Helper functions for generation
    fun generatePdf() {
        coroutineScope.launch {
            isPdfGenerating = true
            progressType = "PDF"
            progressTitle = "Generating PDF"
            progressMessage = "Please wait while we create your PDF document..."
            showProgressDialog = true
            
            val displayItems = viewModel.getDisplayRoutineItems()
            val currentUser = uiState.currentUser
            val role = when (currentUser?.role?.name) {
                "STUDENT" -> "Student"
                "TEACHER" -> "Teacher"
                else -> "Student"
            }
            
            val startTimes = uiState.allTimeSlots.mapNotNull { timeSlot ->
                try {
                    timeSlot.split(" - ")[0].trim()
                } catch (e: Exception) {
                    null
                }
            }.distinct()
            
            generateRoutinePdf(
                context = context,
                routineItems = displayItems,
                role = role,
                batch = currentUser?.batch ?: "",
                section = currentUser?.section ?: "",
                teacherInitial = currentUser?.initial ?: "",
                room = "",
                startTimes = startTimes,
                effectiveFrom = uiState.effectiveFrom,
                snackbarHostState = snackbarHostState,
                onPdfSaved = { uri, fileName ->
                    generatedFileUri = uri
                    generatedFileName = fileName
                    showProgressDialog = false
                    showSuccessDialog = true
                    isPdfGenerating = false
                }
            )
        }
    }
    
    fun generateImage() {
        coroutineScope.launch {
            isImageGenerating = true
            progressType = "Image"
            progressTitle = "Generating Image"
            progressMessage = "Please wait while we create your image..."
            showProgressDialog = true
            
            val displayItems = viewModel.getDisplayRoutineItems()
            val currentUser = uiState.currentUser
            val role = when (currentUser?.role?.name) {
                "STUDENT" -> "Student"
                "TEACHER" -> "Teacher"
                else -> "Student"
            }
            
            val startTimes = uiState.allTimeSlots.mapNotNull { timeSlot ->
                try {
                    timeSlot.split(" - ")[0].trim()
                } catch (e: Exception) {
                    null
                }
            }.distinct()
            
            generateRoutineImage(
                context = context,
                routineItems = displayItems,
                role = role,
                batch = currentUser?.batch ?: "",
                section = currentUser?.section ?: "",
                teacherInitial = currentUser?.initial ?: "",
                room = "",
                startTimes = startTimes,
                effectiveFrom = uiState.effectiveFrom,
                snackbarHostState = snackbarHostState,
                onImageSaved = { uri, fileName ->
                    generatedFileUri = uri
                    generatedFileName = fileName
                    showProgressDialog = false
                    showSuccessDialog = true
                    isImageGenerating = false
                }
            )
        }
    }
    
    fun shareFile(uri: Uri) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = if (generatedFileName.endsWith(".pdf")) "application/pdf" else "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share $generatedFileName"))
    }
    
    fun viewFile(uri: Uri) {
        val viewIntent = Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(uri, if (generatedFileName.endsWith(".pdf")) "application/pdf" else "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            shareFile(uri) // Fallback to share if no viewer app available
        }
    }
    
    // Download Options Bottom Sheet
    if (showDownloadSheet) {
        DownloadOptionsBottomSheet(
            isVisible = showDownloadSheet,
            onDismiss = { showDownloadSheet = false },
            onPdfClick = { generatePdf() },
            onImageClick = { generateImage() },
            isPdfGenerating = isPdfGenerating,
            isImageGenerating = isImageGenerating
        )
    }
    
    // Progress Dialog
    GenerationProgressDialog(
        isVisible = showProgressDialog,
        title = progressTitle,
        message = progressMessage,
        type = progressType,
        onDismissRequest = { }
    )
    
    // Success Dialog
    SuccessDialog(
        isVisible = showSuccessDialog,
        title = if (progressType == "PDF") "PDF Generated Successfully!" else "Image Generated Successfully!",
        message = if (progressType == "PDF") 
            "Your PDF document has been saved to Downloads and is ready for sharing." 
        else 
            "Your image has been saved to Pictures and is ready for sharing.",
        type = progressType,
        onDismiss = { 
            showSuccessDialog = false
            generatedFileUri = null
            generatedFileName = ""
        },
        onShare = { 
            generatedFileUri?.let { shareFile(it) }
            showSuccessDialog = false
        },
        onView = { 
            generatedFileUri?.let { viewFile(it) }
            showSuccessDialog = false
        }
    )
    
    // Filter Bottom Sheet
    if (showFilterSheet) {
        FilterRoutinesBottomSheet(
            viewModel = viewModel,
            onDismissRequest = { showFilterSheet = false },
            onFilterApplied = { filter ->
                viewModel.applyFilter(filter)
                showFilterSheet = false
            }
        )
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
private fun EmptyContent(
    isMaintenanceMode: Boolean = false,
    maintenanceMessage: String? = null,
    isSemesterBreak: Boolean = false,
    updateType: String? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Debug logging for maintenance state
            android.util.Log.d("EmptyContent", "Maintenance state - isMaintenanceMode: $isMaintenanceMode, isSemesterBreak: $isSemesterBreak, updateType: $updateType, message: $maintenanceMessage")
            
            // Different icons and messages based on the state - use updateType for accurate detection
            val (icon, iconColor, title, message) = when {
                updateType == "semester_break" -> {
                    android.util.Log.d("EmptyContent", "Showing SEMESTER BREAK content (from updateType)")
                    Quadruple(
                        Icons.Default.EventBusy,
                        MaterialTheme.colorScheme.primary,
                        "Semester Break",
                        maintenanceMessage ?: "Semester break is in progress. New semester routine will be available soon."
                    )
                }
                updateType == "maintenance_enabled" || (isMaintenanceMode && !isSemesterBreak) -> {
                    android.util.Log.d("EmptyContent", "Showing SYSTEM MAINTENANCE content (from updateType or flags)")
                    Quadruple(
                        Icons.Default.Refresh,
                        MaterialTheme.colorScheme.tertiary,
                        "System Maintenance",
                        maintenanceMessage ?: "System is under maintenance. New routine will be available soon."
                    )
                }
                isMaintenanceMode && isSemesterBreak -> {
                    android.util.Log.d("EmptyContent", "Showing SEMESTER BREAK content (from flags fallback)")
                    Quadruple(
                        Icons.Default.EventBusy,
                        MaterialTheme.colorScheme.primary,
                        "Semester Break", 
                        maintenanceMessage ?: "Semester break is in progress. New semester routine will be available soon."
                    )
                }
                else -> {
                    android.util.Log.d("EmptyContent", "Showing NO CLASSES content")
                    Quadruple(
                        Icons.Default.EventBusy,
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        "No Classes Scheduled",
                        "Your routine will appear here once it's available."
                    )
                }
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = iconColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
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
private fun RoutineContent(
    allDays: List<String>,
    activeDays: List<String>,
    selectedDay: String,
    routineItems: List<RoutineItem>,
    allTimeSlots: List<String>,
    isRefreshing: Boolean,
    currentUser: User?,
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
            routineItems = routineItems,
            allTimeSlots = allTimeSlots
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CleanRoutineTopAppBar(
    user: User?,
    onRefreshClick: () -> Unit,
    isRefreshing: Boolean,
    refreshRotation: Float,
    onDownloadClick: () -> Unit
) {
    TopAppBar(
        title = {
            // Simple clean title section
            Column {
                Text(
                    text = "Class Routine",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = user?.department ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            // Routine Generate Button
            IconButton(onClick = onDownloadClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.share_square_24),
                    contentDescription = "Generate",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(topbarIconSize)
                )
            }
            // Only refresh button for clean design
            IconButton(
                onClick = onRefreshClick,
                enabled = !isRefreshing
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.deep_sync),
                    contentDescription = "Refresh",
                    modifier = Modifier
                        .graphicsLayer(rotationZ = refreshRotation)
                        .size(topbarIconSize),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        windowInsets = ScreenConfig.getTopAppBarWindowInsets(handleStatusBar = true),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun RoutineInfoBar(
    effectiveFrom: String?,
    isFiltered: Boolean,
    currentFilter: RoutineFilter?,
    defaultFilterText: String,
    onFilterClick: () -> Unit,
    onClearFilterClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(top= 8.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                // Filter status with visual distinction
                Text(
                    text = if (isFiltered) {
                        "Filtered: ${currentFilter?.getDisplayText() ?: "Unknown"}"
                    } else {
                        "Showing: $defaultFilterText"
                    },
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Effective date with proper sizing
                if (!effectiveFrom.isNullOrBlank()) {
                    Text(
                        text = "Effective From: $effectiveFrom",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

            }

            Spacer(modifier = Modifier.width(16.dp))

            // Action button with proper presence
            if (isFiltered) {
                Button(
                    onClick = onClearFilterClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp
                    )
                ) {
                    Text(
                        text = "Clear Filter",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            } else {
                Button(
                    onClick = onFilterClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp
                    )
                ) {
                    Text(
                        text = "Filter",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TableRoutineView(
    currentUser: com.om.diucampusschedule.domain.model.User?,
    routineItems: List<RoutineItem>,
    allTimeSlots: List<String>
) {
    val days = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday")

    // Use the pre-sorted time slots from database, fallback to routine-based generation if empty
    val startTimes = if (allTimeSlots.isNotEmpty()) {
        allTimeSlots.mapNotNull { timeSlot ->
            try {
                // Extract start time from the time slot (format: "08:30 AM - 10:00 AM")
                timeSlot.split(" - ")[0].trim()
            } catch (e: Exception) {
                null
            }
        }.distinct()
    } else {
        // Fallback to generating from routine items with proper sorting
        routineItems.mapNotNull { routine ->
            try {
                routine.time.split(" - ")[0].trim()
            } catch (e: Exception) {
                null
            }
        }.distinct().sortedBy { timeString ->
            try {
                // Parse time string to LocalTime for proper chronological sorting
                LocalTime.parse(timeString, formatter12HourUS)
            } catch (e: Exception) {
                LocalTime.MAX // Put invalid times at the end
            }
        }
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
                .padding(8.dp)
                .clip(shape = RoundedCornerShape(20.dp))
                .background(color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else AppBackgroundColorLight)
                .border(
                    width = 2.dp,
                    color = AppPrimaryColorLight,
                    shape = RoundedCornerShape(20.dp)
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
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center,
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
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = Color.Gray)

                // Days rows
                days.forEachIndexed { index, day ->
                    val routinesForDay =
                        routineItems.filter { it.day.equals(day, ignoreCase = true) }
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
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    textAlign = TextAlign.Center,
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
                                        val startTime = try {
                                            LocalTime.parse(time, formatter12HourUS)
                                        } catch (e: Exception) {
                                            null
                                        }
                                        val firstClassTime = firstClass?.startTime ?: LocalTime.MIN
                                        val lastClassTime = lastClass?.endTime ?: LocalTime.MAX

                                        if (startTime != null && startTime.isAfter(firstClassTime) && startTime.isBefore(
                                                lastClassTime
                                            )
                                        ) {
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
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = if (!isSystemInDarkTheme()) Color.DarkGray else MaterialTheme.colorScheme.onSurface,
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
                        HorizontalDivider(thickness = 1.dp, color = Color.Gray)
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
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    textAlign = TextAlign.Center,
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
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = if (!isSystemInDarkTheme()) Color.Gray else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        HorizontalDivider(thickness = 1.dp, color = Color.Gray)
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
            animationSpec = tween(
                durationMillis = 400,
                easing = EaseOutBack
            ) // EaseOutBack for overshoot effect
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
                            animationSpec = tween(
                                durationMillis = 350,
                                delayMillis = 50,
                                easing = EaseInOut
                            )
                        ) + fadeIn(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = 50,
                                easing = EaseInOut
                            )
                        )
                    ) {
                        Text(
                            text = "${routine.courseCode} - ${routine.teacherInitial.orEmpty()}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
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
                                    animationSpec = tween(
                                        durationMillis = 350,
                                        delayMillis = 100,
                                        easing = EaseInOut
                                    )
                                ) + fadeIn(
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        delayMillis = 100,
                                        easing = EaseInOut
                                    )
                                )
                            ) {
                                Text(
                                    text = routine.room.orEmpty(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White,
                                    textAlign = TextAlign.Center
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
                                        animationSpec = tween(
                                            durationMillis = 350,
                                            delayMillis = 150,
                                            easing = EaseInOut
                                        )
                                    ) + fadeIn(
                                        animationSpec = tween(
                                            durationMillis = 300,
                                            delayMillis = 150,
                                            easing = EaseInOut
                                        )
                                    )
                                ) {
                                    Text(
                                        text = routine.batch.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = SoftBlue,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                AnimatedVisibility(
                                    visible = cellVisible,
                                    enter = fadeIn(
                                        animationSpec = tween(
                                            durationMillis = 250,
                                            delayMillis = 200,
                                            easing = EaseInOut
                                        )
                                    ) // Just fade for '-'
                                ) {
                                    Text(
                                        text = "-",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = SoftBlue,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                AnimatedVisibility(
                                    visible = cellVisible,
                                    enter = slideInHorizontally(
                                        initialOffsetX = { it },
                                        animationSpec = tween(
                                            durationMillis = 350,
                                            delayMillis = 250,
                                            easing = EaseInOut
                                        )
                                    ) + fadeIn(
                                        animationSpec = tween(
                                            durationMillis = 300,
                                            delayMillis = 250,
                                            easing = EaseInOut
                                        )
                                    )
                                ) {
                                    Text(
                                        text = routine.section.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = SoftBlue,
                                        textAlign = TextAlign.Center
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

// Helper data class for multiple return values
private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)