package com.om.diucampusschedule.ui.screens.emptyrooms

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Room
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.om.diucampusschedule.R
import com.om.diucampusschedule.domain.model.DayOfWeek
import com.om.diucampusschedule.ui.theme.InterFontFamily
import com.om.diucampusschedule.ui.utils.ScreenConfig
import com.om.diucampusschedule.ui.utils.ScreenConfig.Modifiers.mainAppScreen
import com.om.diucampusschedule.ui.viewmodel.RoutineViewModel
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmptyRoomsScreen() {
    val viewModel: RoutineViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val emptyRoomsMap = viewModel.filterEmptyRooms()

    // Get data from ViewModel
    val timeSlots = uiState.allTimeSlots
    val daysOfWeek = DayOfWeek.values().map { it.displayName }

    // State for selected day and time and showing all rooms
    var selectedTimeIndex by remember { mutableStateOf(0) }
    var selectedDay by remember { mutableStateOf(daysOfWeek.firstOrNull() ?: "Saturday") }
    var showAllRooms by remember { mutableStateOf(false) } // State to control showing all rooms

    // Search state
    var isSearchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchHistory by remember { mutableStateOf(listOf<String>()) }
    var showSearchSuggestions by remember { mutableStateOf(false) }

    // Animation states
    var isContentVisible by remember { mutableStateOf(false) }

    // Animated values for smooth transitions
    val contentAlpha by animateFloatAsState(
        targetValue = if (isContentVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "contentAlpha"
    )

    val contentScale by animateFloatAsState(
        targetValue = if (isContentVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "contentScale"
    )

    // Define days and time slots from ViewModel
    // If data is not yet loaded, use empty lists
    val selectedTabIndex = daysOfWeek.indexOf(selectedDay)

    // Current selected day and time for filtered view
    val currentDay = selectedDay
    val currentTimeSlot = if (timeSlots.isNotEmpty() && selectedTimeIndex < timeSlots.size) {
        timeSlots[selectedTimeIndex]
    } else {
        timeSlots.firstOrNull() ?: ""
    }

    // Available rooms based on state
    val availableRooms: List<String> = if (!showAllRooms && !isSearchMode) {
        emptyRoomsMap[currentDay]?.get(currentTimeSlot) ?: emptyList()
    } else {
        emptyList() // In "All Rooms" mode or search mode, we will handle room display differently
    }
    val totalEmptyRoomsCount = emptyRoomsMap.values.sumOf { dayMap -> dayMap.values.sumOf { it.size } }

    // Find available times for a specific room (for search feature)
    fun findRoomAvailability(roomNumber: String): Map<String, List<String>> {
        val availability = mutableMapOf<String, MutableList<String>>()

        emptyRoomsMap.forEach { (day, timeSlotsMap) ->
            timeSlotsMap.forEach { (timeSlot, rooms) ->
                if (rooms.any { it.contains(roomNumber, ignoreCase = true) }) {
                    availability.getOrPut(day) { mutableListOf() }.add(timeSlot)
                }
            }
        }

        return availability
    }

    // Get all unique room numbers for suggestions
    fun getAllRoomNumbers(): List<String> {
        val allRooms = mutableSetOf<String>()

        emptyRoomsMap.values.forEach { dayMap ->
            dayMap.values.forEach { rooms ->
                rooms.forEach { room ->
                    allRooms.add(room)
                }
            }
        }

        return allRooms.sorted()
    }

    // Filter rooms based on search query
    fun filterRooms(query: String): List<String> {
        if (query.isEmpty()) return emptyList()

        val allRooms = getAllRoomNumbers()
        return allRooms.filter {
            it.contains(query, ignoreCase = true)
        }.take(5) // Limit to 5 suggestions
    }

    // Add to search history
    fun addToSearchHistory(room: String) {
        if (room.isNotEmpty() && !searchHistory.contains(room)) {
            searchHistory = (listOf(room) + searchHistory).take(5) // Keep last 5 searches
        }
    }

    // Load class routines when the screen is first composed
    LaunchedEffect(key1 = Unit) {
        // Trigger content animation after a short delay
        delay(200)
        isContentVisible = true
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Empty Rooms",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Find available classrooms",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        SlidingLabelToggle(
                            showAllRooms = showAllRooms,
                            onToggle = {
                                showAllRooms = it
                                // Clear search when switching to all rooms mode
                                if (it) {
                                    searchQuery = ""
                                    isSearchMode = false
                                    showSearchSuggestions = false
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    windowInsets = ScreenConfig.getTopAppBarWindowInsets(handleStatusBar = true),
                    modifier = Modifier.fillMaxWidth()
                )
                // Divider below TopBar
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = paddingValues.calculateTopPadding() + 16.dp)
                .alpha(contentAlpha)
                .scale(contentScale)
                .mainAppScreen()
        ) {
            // Show loading state if data is not yet loaded
            if (timeSlots.isEmpty() || daysOfWeek.isEmpty()) {
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
                            text = "Loading empty rooms data...",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                return@Column
            }

            // Show search bar in both regular list view and search mode
            if (!showAllRooms) {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        animationSpec = tween(600, delayMillis = 100),
                        initialOffsetY = { -it / 3 }
                    ) + fadeIn(tween(600, delayMillis = 100))
                ) {
                    Column {

                        RoomSearchBar(
                            query = searchQuery,
                            onQueryChange = {
                                searchQuery = it
                                showSearchSuggestions = it.isNotEmpty()
                            },
                            onClearSearch = {
                                searchQuery = ""
                                showSearchSuggestions = false
                            },
                            onSearch = {
                                if (searchQuery.isNotEmpty()) {
                                    addToSearchHistory(searchQuery)
                                    isSearchMode = true
                                    showSearchSuggestions = false
                                }
                            },
                            suggestions = if (showSearchSuggestions) {
                                val filteredRooms = filterRooms(searchQuery)
                                filteredRooms.ifEmpty {
                                    searchHistory.ifEmpty {
                                        emptyList()
                                    }
                                }
                            } else {
                                emptyList()
                            },
                            onSuggestionClick = { room ->
                                searchQuery = room
                                addToSearchHistory(room)
                                isSearchMode = true
                                showSearchSuggestions = false
                            },
                            onBackClick = if (isSearchMode) {
                                {
                                    isSearchMode = false
                                    showSearchSuggestions = false
                                }
                            } else null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            AnimatedContent(
                targetState = isSearchMode,
                transitionSpec = {
                    slideInHorizontally(
                        animationSpec = tween(400),
                        initialOffsetX = { if (targetState) it else -it }
                    ) + fadeIn(tween(400)) togetherWith
                            slideOutHorizontally(
                                animationSpec = tween(400),
                                targetOffsetX = { if (targetState) -it else it }
                            ) + fadeOut(tween(400))
                },
                label = "searchModeContent"
            ) { searchMode ->
                if (searchMode) {
                    // Search results view
                    if (searchQuery.isNotEmpty()) {
                        val roomAvailability = findRoomAvailability(searchQuery)
                        RoomSearchResults(
                            roomNumber = searchQuery,
                            availability = roomAvailability,
                            daysOfWeek = daysOfWeek
                        )
                    } else {
                        EmptySearchView()
                    }
                } else {
                    Column {
                        AnimatedVisibility(
                            visible = !showAllRooms,
                            enter = slideInVertically(
                                animationSpec = tween(500, delayMillis = 200),
                                initialOffsetY = { it / 4 }
                            ) + fadeIn(tween(500, delayMillis = 200)),
                            exit = slideOutVertically(
                                animationSpec = tween(300),
                                targetOffsetY = { -it / 4 }
                            ) + fadeOut(tween(300))
                        ) {
                            Column {
                                // Day selector - horizontal pills
                                Text(
                                    text = "Search by Day and Time",
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )

                                // Day selection tabs
                                ScrollableTabRow(
                                    selectedTabIndex = selectedTabIndex,
                                    edgePadding = 0.dp,
                                    containerColor = MaterialTheme.colorScheme.background,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp),
                                    indicator = { tabPositions ->
                                        CustomTabIndicator(
                                            tabPositions = tabPositions,
                                            selectedTabIndex = selectedTabIndex,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                ) {
                                    daysOfWeek.forEachIndexed { _, day ->
                                        Tab(
                                            selected = selectedDay == day,
                                            onClick = { selectedDay = day },
                                            text = {
                                                Text(
                                                    text = day.replaceFirstChar {
                                                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                                                    },
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (selectedDay == day) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                )
                                            },
                                            modifier = Modifier
                                                .height( 50.dp )
                                                .padding(vertical = 8.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Time slot selector - Dropdown Menu
                                Text(
                                    text = "Time Slots",
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                AnimatedVisibility(
                                    visible = true,
                                    enter = slideInVertically(
                                        animationSpec = tween(600, delayMillis = 400),
                                        initialOffsetY = { it / 3 }
                                    ) + fadeIn(tween(600, delayMillis = 400)) + scaleIn(
                                        tween(600, delayMillis = 400, easing = FastOutSlowInEasing),
                                        initialScale = 0.8f
                                    )
                                ) {
                                    TimeSlotDropdown(
                                        timeSlots = timeSlots,
                                        selectedTimeIndex = selectedTimeIndex,
                                        onTimeSlotSelected = { selectedTimeIndex = it }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Available rooms display
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                animationSpec = tween(700, delayMillis = 500),
                                initialOffsetY = { it / 2 }
                            ) + fadeIn(tween(700, delayMillis = 500)) + scaleIn(
                                tween(700, delayMillis = 500, easing = FastOutSlowInEasing),
                                initialScale = 0.9f
                            )
                        ) {
                            val roomText = if(availableRooms.size == 1) "room" else "rooms"
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Text(
                                        text = if (!showAllRooms) " Available Rooms" else " All Available Rooms",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    Text(
                                        text = if (!showAllRooms) "${availableRooms.size} $roomText" else "$totalEmptyRoomsCount rooms",
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                AnimatedContent(
                                    targetState = Pair(showAllRooms, availableRooms.size),
                                    transitionSpec = {
                                        slideInVertically(
                                            animationSpec = tween(400),
                                            initialOffsetY = { it / 4 }
                                        ) + fadeIn(tween(400)) togetherWith
                                                slideOutVertically(
                                                    animationSpec = tween(300),
                                                    targetOffsetY = { -it / 4 }
                                                ) + fadeOut(tween(300))
                                    },
                                    label = "roomsContent"
                                ) { (allRooms, _) ->
                                    if (allRooms) {
                                        if (totalEmptyRoomsCount == 0) {
                                            EmptyRoomsMessage()
                                        } else {
                                            AllEmptyRoomsTable(
                                                emptyRoomsMap = emptyRoomsMap,
                                                timeSlots = timeSlots,
                                                daysOfWeek = daysOfWeek
                                            )
                                        }
                                    } else if (availableRooms.isEmpty()) {
                                        EmptyRoomsMessage()
                                    } else {
                                        AvailableRoomsList(rooms = availableRooms)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySearchView() {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "searchAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "searchScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 16.dp)
            .alpha(alpha)
            .scale(scale)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp)
            ) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = scaleIn(
                        tween(500, delayMillis = 300),
                        initialScale = 0.3f
                    ) + fadeIn(tween(500, delayMillis = 300))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        tween(400, delayMillis = 500),
                        initialOffsetY = { it / 2 }
                    ) + fadeIn(tween(400, delayMillis = 500))
                ) {
                    Text(
                        text = "Search for a Room",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        tween(400, delayMillis = 700),
                        initialOffsetY = { it / 3 }
                    ) + fadeIn(tween(400, delayMillis = 700))
                ) {
                    Text(
                        text = "Enter a room number above to find when it's free",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RoomCard(roomNumber: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.MeetingRoom,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp) // Tight spacing between texts
            ) {
                // "Room" label - small
                Text(
                    text = "Room",
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.labelMedium.lineHeight * 0.9f // Tighter line height
                )

                // Room number - largest
                Text(
                    text = roomNumber,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.titleLarge.lineHeight * 0.85f // Tighter line height
                )

                // Lab info and building info - medium size
                val normalizedRoom = roomNumber.trim().uppercase()

                val labRooms = setOf(
                    "610", "616", "710", "711A", "711B", "814A", "903",
                    "AB3-104", "AB3-106", "AB3-107"
                )
                val firstLine: String? = when (normalizedRoom) {
                    "601" -> "(DS Lab)"
                    "613" -> "(Robotics Lab)"
                    "614" -> "(CS Lab)"
                    else -> if (labRooms.contains(normalizedRoom)) "(Lab Room)" else null
                }
                val isAb3 = normalizedRoom.contains("AB3")

                if (firstLine != null || isAb3) {
                    // Balanced sizing for smooth visual flow
                    val bodyMediumSize = MaterialTheme.typography.bodyMedium.fontSize
                    val bodySmallSize = MaterialTheme.typography.bodySmall.fontSize

                    val annotated = buildAnnotatedString {
                        if (firstLine != null) {
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = bodyMediumSize // Medium size for better flow
                                )
                            ) {
                                append(firstLine)
                            }
                        }
                        if (isAb3) {
                            if (firstLine != null) append("\n")
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = bodySmallSize // Consistent with medium sizing
                                )
                            ) {
                                append("(Academic Building 3)")
                            }
                        }
                    }

                    Text(
                        text = annotated,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp // Tight line height for compact look
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onSearch: () -> Unit,
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 4.dp
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "Search by room number...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = InterFontFamily
                    )
                },
                leadingIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = query.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(onClick = onClearSearch) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearch()
                        focusManager.clearFocus()
                    }
                ),
                textStyle = TextStyle(fontFamily = InterFontFamily),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .focusRequester(focusRequester)
            )
        }

        // Search suggestions dropdown
        AnimatedVisibility(
            visible = suggestions.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(suggestions) { suggestion ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSuggestionClick(suggestion) }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (suggestion != suggestions.last()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 48.dp),
                                thickness = DividerDefaults.Thickness, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoomSearchResults(
    roomNumber: String,
    availability: Map<String, List<String>>,
    daysOfWeek: List<String>
) {
    Column {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Room,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = "Room - $roomNumber",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        // Lab info and building info - medium size
                        val normalizedRoom = roomNumber.trim().uppercase()

                        val labRooms = setOf(
                            "610", "616", "710", "711A", "711B", "814A", "903",
                            "AB3-104", "AB3-106", "AB3-107"
                        )
                        val firstLine: String? = when (normalizedRoom) {
                            "601" -> "(DS Lab)"
                            "613" -> "(Robotics Lab)"
                            "614" -> "(CS Lab)"
                            else -> if (labRooms.contains(normalizedRoom)) "(LAB)" else null
                        }

                        if (firstLine != null) {
                            // Balanced sizing for smooth visual flow
                            val bodyMediumSize = MaterialTheme.typography.bodyMedium.fontSize

                            val annotated = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = bodyMediumSize // Medium size for better flow
                                    )
                                ) {
                                    append(firstLine)
                                }
                            }

                            Text(
                                text = annotated,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                lineHeight = 16.sp // Tight line height for compact look
                            )
                        }
                    }

                    Text(
                        text = if (availability.isNotEmpty())
                            "Available at ${availability.values.sumOf { it.size }} time slots"
                        else
                            "Not available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        if (availability.isEmpty()) {
            EmptySearchResultMessage(roomNumber)
        } else {
            LazyColumn {
                // Sort days according to the order in daysOfWeek list
                val sortedDays = availability.keys.sortedBy { day -> daysOfWeek.indexOf(day) }

                itemsIndexed(sortedDays) { index, day ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(durationMillis = 300, delayMillis = index * 100)) +
                                slideInVertically(
                                    initialOffsetY = { it / 2 },
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        delayMillis = index * 100,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                    ) {
                        DayAvailabilityCard(
                            day = day,
                            timeSlots = availability[day] ?: emptyList()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun DayAvailabilityCard(
    day: String,
    timeSlots: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Day header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                DayBadge(day)

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = day.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = "${timeSlots.size} slots",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(
                Modifier,
                DividerDefaults.Thickness,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Time slots
            Column {
                timeSlots.forEachIndexed { index, timeSlot ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = timeSlot,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (index < timeSlots.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 30.dp),
                            thickness = DividerDefaults.Thickness, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayBadge(day: String) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                when (day) {
                    "SATURDAY", "SUNDAY" -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.secondary
                }.copy(alpha = 0.15f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.first().toString(),
            color = when (day) {
                "SATURDAY", "SUNDAY" -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.secondary
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp
        )
    }
}

@Composable
fun EmptySearchResultMessage(roomNumber: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Room '$roomNumber' is not available",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Try searching for a different room number or check the complete rooms list",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun EmptyRoomsMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Available Rooms",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Try selecting a different time slot or day",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun CustomTabIndicator(
    tabPositions: List<TabPosition>,
    selectedTabIndex: Int,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val indicatorLeft = tabPositions[selectedTabIndex].left
    val indicatorRight = tabPositions[selectedTabIndex].right

    val currentTabWidth = indicatorRight - indicatorLeft

    val indicatorModifier = Modifier.fillMaxWidth()
        .wrapContentSize(Alignment.BottomStart)
        .offset(x = indicatorLeft)
        .width(currentTabWidth)
    TabRowDefaults.SecondaryIndicator(
        modifier = indicatorModifier.clip(RoundedCornerShape(8.dp)),
        color = color
    )
}

@Composable
fun SlidingLabelToggle(showAllRooms: Boolean, onToggle: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val offsetX by animateFloatAsState(
        targetValue = if (showAllRooms) 40f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "Toggle Slide"
    )

    Box(
        modifier = modifier
            .width(80.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Sliding highlight
        Box(
            modifier = Modifier
                .offset(x = offsetX.dp)
                .width(40.dp)
                .height(26.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(13.dp))
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "List View",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (!showAllRooms) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 9.sp
                ),
                color = if (!showAllRooms) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .width(40.dp)
                    .clickable { onToggle(false) }
                    .padding(horizontal = 6.dp),
                textAlign = TextAlign.Center,
                lineHeight = 10.sp
            )
            Text(
                text = "Table View",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (showAllRooms) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 9.sp
                ),
                color = if (showAllRooms) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .width(40.dp)
                    .clickable { onToggle(true) }
                    .padding(horizontal = 6.dp),
                textAlign = TextAlign.Center,
                lineHeight = 10.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotDropdown(
    timeSlots: List<String>,
    selectedTimeIndex: Int,
    onTimeSlotSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            readOnly = true,
            value = if (timeSlots.isNotEmpty() && selectedTimeIndex < timeSlots.size) {
                timeSlots[selectedTimeIndex]
            } else {
                "Loading..."
            },
            onValueChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            timeSlots.forEachIndexed { index, timeSlot ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = if (selectedTimeIndex == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(20.dp)
                            )
                            Text(
                                text = timeSlot,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selectedTimeIndex == index) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (selectedTimeIndex == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    onClick = {
                        onTimeSlotSelected(index)
                        expanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                )
                if (index < timeSlots.size - 1) {
                    HorizontalDivider(
                        Modifier,
                        DividerDefaults.Thickness,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AvailableRoomsList(rooms: List<String>) {
    val itemsPerRow = 2
    val roomRows = rooms.chunked(itemsPerRow)

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = roomRows,
            key = { row -> row.joinToString() } // Unique key for each row
        ) { rowRooms ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowRooms.forEach { room ->
                    RoomCard(
                        roomNumber = room,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty slots if needed
                repeat(itemsPerRow - rowRooms.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun AllEmptyRoomsTable(
    emptyRoomsMap: Map<String, Map<String, List<String>>>,
    timeSlots: List<String>,
    daysOfWeek: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        val horizontalScrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Header Row (Days)
            Row(
                modifier = Modifier
                    .horizontalScroll(horizontalScrollState)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 12.dp)
            ) {
                // Time Column Header
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Time \\ Day",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                }

                // Days Headers
                daysOfWeek.forEach { day ->
                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Data Rows (Time Slots)
            LazyColumn {
                items(timeSlots.size) { timeSlotIndex ->
                    val timeSlot = timeSlots[timeSlotIndex]

                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            initialOffsetY = { 40 },
                            animationSpec = tween(durationMillis = 300, delayMillis = timeSlotIndex * 100)
                        ) + fadeIn(animationSpec = tween(durationMillis = 300))
                    ) {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(horizontalScrollState)
                                .padding(vertical = 4.dp)
                        ) {
                            // Time Slot Column
                            Box(
                                modifier = Modifier
                                    .width(130.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = timeSlot,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    textAlign = TextAlign.Start
                                )
                            }

                            // Empty Rooms for each day
                            daysOfWeek.forEach { day ->
                                val roomsForDayTimeSlot = emptyRoomsMap[day]?.get(timeSlot) ?: emptyList()

                                Box(
                                    modifier = Modifier
                                        .width(110.dp)
                                        .padding(horizontal = 4.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = roomsForDayTimeSlot.joinToString(", "),
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
