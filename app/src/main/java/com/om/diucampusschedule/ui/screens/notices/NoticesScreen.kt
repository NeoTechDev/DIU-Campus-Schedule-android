package com.om.diucampusschedule.ui.screens.notices

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Badge
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.om.diucampusschedule.R
import com.om.diucampusschedule.core.network.rememberConnectivityState
import com.om.diucampusschedule.ui.utils.TopAppBarIconSize.topbarIconSize
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticesScreen(
    noticesViewModel: NoticesViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToWebView: (url: String, title: String) -> Unit
) {

    val notices by noticesViewModel.notices.collectAsState()
    val isNoticesLoading by noticesViewModel.isNoticesLoading.collectAsState()
    val notifications by noticesViewModel.notifications.collectAsState()
    val isNotificationsLoading by noticesViewModel.isNotificationsLoading.collectAsState()
    val unreadNotificationCount by noticesViewModel.unreadNotificationCount.collectAsState()
    val isConnected = rememberConnectivityState()

    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        noticesViewModel.fetchNotices()
        noticesViewModel.loadNotifications()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding() // Add navigation bar padding to prevent overlap
    ) {
        // Top App Bar
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = if(isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            title = {
                Text(
                    text = "Notifications & Notices",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(topbarIconSize)
                    )
                }
            },
           /* actions = {
                // Test button - remove in production
                IconButton(onClick = { noticesViewModel.addTestNotifications() }) {
                    Text(
                        text = "TEST",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },*/
            modifier = Modifier.fillMaxWidth()
        )

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
            containerColor = if(isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            divider = {
                HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            }
        ) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Notifications",
                            color = if (pagerState.currentPage == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
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
                }
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
                text = {
                    Text(
                        "Department Notices",
                        color = if (pagerState.currentPage == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            )
        }

        // Content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> NotificationsTab(
                    notifications = notifications,
                    isLoading = isNotificationsLoading,
                    onNotificationClick = { notification ->
                        noticesViewModel.markAsRead(notification.id)
                        notification.actionRoute?.let { route ->
                            // Handle navigation based on action route
                            when (route) {
                                "routine" -> onBack() // Navigate back to routine view
                                else -> {
                                    // Handle other routes or external links
                                }
                            }
                        }
                    },
                    onDeleteNotification = { notificationId ->
                        noticesViewModel.deleteNotification(notificationId)
                    },
                    onMarkAllAsRead = {
                        noticesViewModel.markAllAsRead()
                    },
                    onDeleteAll = {
                        noticesViewModel.deleteAllNotifications()
                    }
                )
                1 -> DepartmentNoticesTab(
                    notices = notices,
                    isLoading = isNoticesLoading,
                    onNoticeClick = { notice ->
                        onNavigateToWebView(notice.link, notice.title)
                    },
                    isConnected = isConnected
                )
            }
        }
    }
}

@Composable
fun NotificationsTab(
    notifications: List<com.om.diucampusschedule.domain.model.Notification>,
    isLoading: Boolean,
    onNotificationClick: (com.om.diucampusschedule.domain.model.Notification) -> Unit,
    onDeleteNotification: (String) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onDeleteAll: () -> Unit
) {

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (notifications.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    "No notifications yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Action buttons row
                if (notifications.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 8.dp, 16.dp, 0.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Mark All as Read button
                        if (notifications.any { !it.isRead }) {
                            OutlinedButton(
                                onClick = onMarkAllAsRead,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Mark All As Read",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        // Delete All button
                        OutlinedButton(
                            onClick = onDeleteAll,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Delete All",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }

                // Notifications list with time-based grouping
                val groupedNotifications = notifications.groupByTimeCategory()

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    groupedNotifications.forEach { (category, categoryNotifications) ->
                        // Category header
                        item {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        // Notifications in this category
                        items(categoryNotifications) { notification ->
                            CompactNotificationCard(
                                notification = notification,
                                onClick = { onNotificationClick(notification) },
                                onDelete = { onDeleteNotification(notification.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DepartmentNoticesTab(
    notices: List<com.om.diucampusschedule.domain.model.Notice>,
    isLoading: Boolean,
    onNoticeClick: (com.om.diucampusschedule.domain.model.Notice) -> Unit,
    isConnected: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if(!isConnected){
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.wifi_slash),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(72.dp)
                )
                Text(
                    "Check your internet connection",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }else{
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (notices.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Article,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "No notices found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notices) { notice ->
                        CompactNoticeCard(
                            notice = notice,
                            onClick = { onNoticeClick(notice) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactNotificationCard(
    notification: com.om.diucampusschedule.domain.model.Notification,
    onClick: () -> Unit,
    onDelete: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (notification.isRead)
            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        else
            MaterialTheme.colorScheme.surface, // More visible unread background
        tonalElevation = if (notification.isRead) 0.dp else 3.dp, // Higher elevation for unread
        border = BorderStroke(
            width = 1.dp,
            color = if (!notification.isRead && !isSystemInDarkTheme()) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            } else {
                Color.Transparent
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                isExpanded = !isExpanded
                onClick()
            }
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type indicator with enhanced unread state
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = when (notification.type) {
                            com.om.diucampusschedule.domain.model.NotificationType.ROUTINE_UPDATE -> {
                                if (notification.isRead)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) // Stronger color for unread
                            }
                            com.om.diucampusschedule.domain.model.NotificationType.ADMIN_MESSAGE -> {
                                if (notification.isRead)
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                else
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                            }
                            com.om.diucampusschedule.domain.model.NotificationType.MAINTENANCE -> {
                                if (notification.isRead)
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                                else
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)
                            }
                            else -> {
                                if (notification.isRead)
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                else
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notification.type) {
                        com.om.diucampusschedule.domain.model.NotificationType.ROUTINE_UPDATE -> Icons.Outlined.Schedule
                        com.om.diucampusschedule.domain.model.NotificationType.ADMIN_MESSAGE -> Icons.AutoMirrored.Filled.Message
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    tint = when (notification.type) {
                        com.om.diucampusschedule.domain.model.NotificationType.ROUTINE_UPDATE -> {
                            if (notification.isRead)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) // Dimmed for read
                            else
                                MaterialTheme.colorScheme.primary // Full color for unread
                        }
                        com.om.diucampusschedule.domain.model.NotificationType.ADMIN_MESSAGE -> {
                            if (notification.isRead)
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                            else
                                MaterialTheme.colorScheme.secondary
                        }
                        com.om.diucampusschedule.domain.model.NotificationType.MAINTENANCE -> {
                            if (notification.isRead)
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                            else
                                MaterialTheme.colorScheme.tertiary
                        }
                        else -> {
                            if (notification.isRead)
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                            else
                                MaterialTheme.colorScheme.secondary
                        }
                    },
                    modifier = Modifier.size(16.dp)
                )
            }

            // Content with enhanced read/unread styling
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp)
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (notification.isRead)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) // Slightly dimmed for read
                    else
                        MaterialTheme.colorScheme.onSurface, // Full opacity for unread
                    fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold
                )
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (notification.isRead)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) // More dimmed for read
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = if (notification.isRead)
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = formatNotificationTime(notification.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (notification.isRead)
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Actions row with enhanced unread indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Delete button with contextual styling
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = if (notification.isRead)
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Enhanced unread indicator
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp) // Slightly larger for better visibility
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    )
                } else {
                    // Placeholder space to maintain consistent layout
                    Box(modifier = Modifier.size(8.dp))
                }
            }
        }
    }
}

@Composable
fun CompactNoticeCard(
    notice: com.om.diucampusschedule.domain.model.Notice,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Notice icon - compact
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Article,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Content - more compact
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp)
            ) {
                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = notice.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Chevron icon - minimal
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Extension function to group notifications by time categories
private fun List<com.om.diucampusschedule.domain.model.Notification>.groupByTimeCategory(): List<Pair<String, List<com.om.diucampusschedule.domain.model.Notification>>> {
    val now = LocalDateTime.now()
    val grouped = this.groupBy { notification ->
        val daysDiff = ChronoUnit.DAYS.between(notification.timestamp.toLocalDate(), now.toLocalDate())
        when {
            daysDiff == 0L -> "New"
            daysDiff <= 7L -> "This Week"
            else -> "Earlier"
        }
    }

    // Return in desired order
    val order = listOf("New", "This Week", "Earlier")
    return order.mapNotNull { category ->
        grouped[category]?.let { notifications ->
            category to notifications.sortedByDescending { it.timestamp }
        }
    }
}

private fun formatNotificationTime(timestamp: java.time.LocalDateTime): String {
    val now = java.time.LocalDateTime.now()
    val duration = java.time.Duration.between(timestamp, now)

    return when {
        duration.toDays() > 0 -> "${duration.toDays()}d"
        duration.toHours() > 0 -> "${duration.toHours()}h"
        duration.toMinutes() > 0 -> "${duration.toMinutes()}m"
        else -> "now"
    }
}