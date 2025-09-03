package com.om.diucampusschedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.om.diucampusschedule.R
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.model.UserRole
import com.om.diucampusschedule.ui.navigation.Screen
import com.om.diucampusschedule.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawer(
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Get app version
    val packageInfo = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: Exception) {
            null
        }
    }
    val appVersion = packageInfo?.versionName ?: "1.0.0"

    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        windowInsets = WindowInsets.systemBars
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Section
            DrawerHeader(user = authState.user) {
                onNavigate(Screen.Profile.route)
                onCloseDrawer()
            }
            
            // Divider under profile section
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Navigation Content
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Main Section Items (Faculty Info & Previous Questions)
                items(getMainSectionItems(onNavigate)) { item ->
                    NavigationItem(
                        item = item,
                        onClick = {
                            item.action()
                            onCloseDrawer()
                        }
                    )
                }

                // Community Section
                item {
                    SectionDivider()
                    SectionHeader(title = "Community")
                }
                items(getCommunitySectionItems(onNavigate)) { item ->
                    SupportItem(
                        item = item,
                        onClick = {
                            item.action()
                            onCloseDrawer()
                        }
                    )
                }

                // Notification Section
                item {
                    SectionDivider()
                    SectionHeader(title = "Notifications")
                }
                items(getNotificationSectionItems(onNavigate)) { item ->
                    SupportItem(
                        item = item,
                        onClick = {
                            item.action()
                            onCloseDrawer()
                        }
                    )
                }

                // Support & More Section
                item {
                    SectionDivider()
                    SectionHeader(title = "Support & More")
                }
                items(getSupportSectionItems(onNavigate, context)) { item ->
                    SupportItem(
                        item = item,
                        onClick = {
                            item.action()
                            onCloseDrawer()
                        }
                    )
                }
            }

            // Footer
            DrawerFooter(appVersion = appVersion)
        }
    }
}

@Composable
private fun DrawerHeader(
    user: User?,
    onProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProfileClick() }
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Profile Picture
        if (user?.profilePictureUrl?.isNotBlank() == true) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.profilePictureUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_person_placeholder),
                error = painterResource(R.drawable.ic_person_placeholder)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getUserInitials(user),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Name
        Text(
            text = user?.name ?: "User",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        // View Profile Link
        Text(
            text = "View profile",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        letterSpacing = 1.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@Composable
private fun NavigationItem(
    item: DrawerItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun SupportItem(
    item: DrawerItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(15.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            item.subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (item.isExternal) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "External link",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun DrawerFooter(appVersion: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "App Version: $appVersion",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Data classes
data class DrawerItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String? = null,
    val isExternal: Boolean = false,
    val action: () -> Unit
)

// Section 1: Main Section (Faculty Info & Previous Questions)
private fun getMainSectionItems(
    onNavigate: (String) -> Unit
): List<DrawerItem> {
    return listOf(
        DrawerItem(
            icon = Icons.Default.School,
            title = "Faculty Info",
            action = {
                // TODO: Navigate to Faculty Info screen
                // onNavigate(Screen.FacultyInfo.route)
            }
        ),
        DrawerItem(
            icon = Icons.Default.Quiz,
            title = "Previous Questions",
            action = {
                // TODO: Navigate to Previous Questions screen
                // onNavigate(Screen.PreviousQuestions.route)
            }
        )
    )
}

// Section 2: Community (DIUCS Connect)
private fun getCommunitySectionItems(
    onNavigate: (String) -> Unit
): List<DrawerItem> {
    return listOf(
        DrawerItem(
            icon = Icons.Default.Groups,
            title = "DIUCS Connect",
            subtitle = "Join our newest community!",
            action = {
                // TODO: Navigate to Community screen
                // onNavigate(Screen.Community.route)
            }
        )
    )
}

// Section 3: Notifications (Fixed Notification Delays)
private fun getNotificationSectionItems(
    onNavigate: (String) -> Unit
): List<DrawerItem> {
    return listOf(
        DrawerItem(
            icon = Icons.Outlined.Error,
            title = "Fix Notification Delays",
            action = {
                // TODO: Show battery optimization dialog
            }
        )
    )
}

// Section 4: Support & More
private fun getSupportSectionItems(
    onNavigate: (String) -> Unit,
    context: android.content.Context
): List<DrawerItem> {
    return listOf(
        DrawerItem(
            icon = Icons.Default.Star,
            title = "Rate This App",
            action = {
                // TODO: Open Play Store rating
            }
        ),
        DrawerItem(
            icon = Icons.Outlined.Feedback,
            title = "Send Feedback",
            subtitle = "Help us to improve",
            isExternal = true,
            action = {
                // TODO: Open feedback form/email
            }
        ),
        DrawerItem(
            icon = Icons.Outlined.ThumbUp,
            title = "Follow & Support",
            isExternal = true,
            action = {
                // TODO: Open social media links
            }
        ),
        DrawerItem(
            icon = Icons.Outlined.Groups,
            title = "Meet The Developers",
            action = {
                // TODO: Show developers dialog
            }
        )
    )
}

// Helper functions
private fun getUserInitials(user: User?): String {
    return if (user?.role == UserRole.TEACHER) {
        user.initial.takeIf { !it.isNullOrBlank() } ?: "T"
    } else {
        user?.name?.split(" ")?.take(2)?.mapNotNull {
            it.firstOrNull()?.uppercaseChar()
        }?.joinToString("")?.takeIf { it.isNotBlank() } ?: "U"
    }
}

private fun getUserDetails(user: User?): String {
    return if (user?.role == UserRole.STUDENT) {
        buildString {
            append(user.department)
            if (user.batch.isNotBlank() && user.section.isNotBlank()) {
                append(" • ${user.batch}-${user.section}")
            }
            if (user.labSection.isNotBlank()) {
                append(" (${user.labSection})")
            }
        }
    } else {
        "${user?.department ?: ""} • ${user?.role?.name ?: "Teacher"}"
    }
}