package com.om.diucampusschedule.ui.screens.notices

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.om.diucampusschedule.ui.screens.today.TodayViewModel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import com.om.diucampusschedule.R
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.material3.HorizontalDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticesScreen(
    todayViewModel: TodayViewModel = hiltViewModel(),
    noticesViewModel: NoticesViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToWebView: (url: String, title: String) -> Unit
) {

    val notices by todayViewModel.notices.collectAsState()
    val isNoticesLoading by todayViewModel.isNoticesLoading.collectAsState()
    val notifications by noticesViewModel.notifications.collectAsState()
    val isNotificationsLoading by noticesViewModel.isNotificationsLoading.collectAsState()
    val unreadNotificationCount by noticesViewModel.unreadNotificationCount.collectAsState()
    val context = LocalContext.current
    
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        todayViewModel.fetchNotices()
        noticesViewModel.loadNotifications()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Notices & Notifications",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                }
            },
            actions = {
                // Test button - remove in production
                IconButton(onClick = { noticesViewModel.addTestNotifications() }) {
                    Text(
                        text = "TEST",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            divider = { 
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
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
                            color = if (pagerState.currentPage == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                        color = if (pagerState.currentPage == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                    }
                )
                1 -> DepartmentNoticesTab(
                    notices = notices,
                    isLoading = isNoticesLoading,
                    onNoticeClick = { notice ->
                        onNavigateToWebView(notice.link, notice.title)
                    }
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
    onMarkAllAsRead: () -> Unit
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
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    "No notifications yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Mark All as Read button
                if (notifications.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onMarkAllAsRead,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp, 16.dp, 20.dp, 8.dp)
                    ) {
                        Text("Mark All as Read")
                    }
                }
                
                LazyColumn(
                    contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notifications) { notification ->
                        NotificationCard(
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

@Composable
fun DepartmentNoticesTab(
    notices: List<com.om.diucampusschedule.domain.model.Notice>,
    isLoading: Boolean,
    onNoticeClick: (com.om.diucampusschedule.domain.model.Notice) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (notices.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.bells),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    "No notices found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(20.dp, 24.dp, 20.dp, 24.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(notices) { notice ->
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNoticeClick(notice) }
                    ) {
                        Row(
                            modifier = Modifier
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                                        )
                                    )
                                )
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Notice icon on left - bigger size
                            Box(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            )
                                        ),
                                        shape = MaterialTheme.shapes.large
                                    )
                                    .padding(16.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.bells),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            // Main content
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 20.dp, end = 12.dp)
                            ) {
                                Text(
                                    text = notice.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = MaterialTheme.typography.titleMedium.lineHeight
                                )
                                Row(
                                    modifier = Modifier.padding(top = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = notice.date,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(start = 6.dp)
                                    )
                                }
                            }
                            // Arrow icon on right
                            Box(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                                            )
                                        ),
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .padding(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier
                                        .graphicsLayer(rotationZ = 180f)
                                        .size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: com.om.diucampusschedule.domain.model.Notification,
    onClick: () -> Unit,
    onDelete: () -> Unit = {}
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.isRead) 4.dp else 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Notification type icon
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                when (notification.type) {
                                    com.om.diucampusschedule.domain.model.NotificationType.ROUTINE_UPDATE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    com.om.diucampusschedule.domain.model.NotificationType.ADMIN_MESSAGE -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                    com.om.diucampusschedule.domain.model.NotificationType.MAINTENANCE -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                },
                                when (notification.type) {
                                    com.om.diucampusschedule.domain.model.NotificationType.ROUTINE_UPDATE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    com.om.diucampusschedule.domain.model.NotificationType.ADMIN_MESSAGE -> MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                                    com.om.diucampusschedule.domain.model.NotificationType.MAINTENANCE -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                                    else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                                }
                            )
                        ),
                        shape = MaterialTheme.shapes.large
                    )
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = when (notification.type) {
                        com.om.diucampusschedule.domain.model.NotificationType.ROUTINE_UPDATE -> Icons.Default.CalendarToday
                        com.om.diucampusschedule.domain.model.NotificationType.ADMIN_MESSAGE -> Icons.Default.Notifications
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    tint = when (notification.type) {
                        com.om.diucampusschedule.domain.model.NotificationType.ROUTINE_UPDATE -> MaterialTheme.colorScheme.primary
                        com.om.diucampusschedule.domain.model.NotificationType.ADMIN_MESSAGE -> MaterialTheme.colorScheme.error
                        com.om.diucampusschedule.domain.model.NotificationType.MAINTENANCE -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.secondary
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 12.dp)
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.SemiBold
                )
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatNotificationTime(notification.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete notification",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            // Unread indicator
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .padding(start = 8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

private fun formatNotificationTime(timestamp: java.time.LocalDateTime): String {
    val now = java.time.LocalDateTime.now()
    val duration = java.time.Duration.between(timestamp, now)
    
    return when {
        duration.toDays() > 0 -> "${duration.toDays()}d ago"
        duration.toHours() > 0 -> "${duration.toHours()}h ago"
        duration.toMinutes() > 0 -> "${duration.toMinutes()}m ago"
        else -> "Just now"
    }
}
