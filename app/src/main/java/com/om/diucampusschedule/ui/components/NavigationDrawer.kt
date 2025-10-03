package com.om.diucampusschedule.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.om.diucampusschedule.R
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.model.UserRole
import com.om.diucampusschedule.ui.navigation.Screen
import com.om.diucampusschedule.ui.screens.webview.PortalTitles
import com.om.diucampusschedule.ui.screens.webview.PortalUrls
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
    val lazyListState = LazyListState()

    var showMeetTheDevelopersDialog by remember { mutableStateOf(false) }

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
        modifier = Modifier
            .clip(RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp))
            .width(280.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        windowInsets = WindowInsets.systemBars
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Navigation Content
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                state = lazyListState
            ) {
                // Header Section
                item {
                    DrawerHeader(user = authState.user) {
                        onNavigate(Screen.Profile.route)
                        onCloseDrawer()
                    }
                }

                // Divider under profile section
                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }

                // Main Section Items (Faculty Info & Previous Questions)
                items(getMainSectionItems(onNavigate, context, authState.user)) { item ->
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
                // Class reminder toggle
                item {
                    NotificationToggleItem(
                        onItemClick = { /* Toggle handled internally */ }
                    )
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
                items(getSupportSectionItems(context) { showMeetTheDevelopersDialog = true }) { item ->
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
        if (showMeetTheDevelopersDialog) {
            val developers = listOf(
                Developer(
                    pictureRes = R.drawable.ovi,
                    name = "Ismam Hasan Ovi",
                    gmail = "ismamhasanovi@gmail.com",
                    department = "SWE-41"
                ),
                Developer(
                    pictureRes = R.drawable.maruf,
                    name = "Md Maruf Rayhan",
                    gmail = "marufrayhan2002@gmail.com",
                    department = "SWE-41"
                )
            )
            AboutUsDialog(
                showDialog = showMeetTheDevelopersDialog,
                onDismiss = { showMeetTheDevelopersDialog = false },
                developers = developers,
                dialogTitle = "The Developers"
            )
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
        Spacer(modifier = Modifier.height(2.dp))
        user?.email?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // View Profile Link
        Text(
            text = "View profile",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
            if (item.icon != null) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else if (item.iconRes != null) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = item.iconRes),
                    contentDescription = item.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
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
        if (item.icon != null) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        } else if (item.iconRes != null) {
            Icon(
                imageVector = ImageVector.vectorResource(id = item.iconRes),
                contentDescription = item.title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
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
            .padding(4.dp),
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
    val icon: ImageVector? = null,
    val iconRes: Int? = null,
    val title: String,
    val subtitle: String? = null,
    val isExternal: Boolean = false,
    val action: () -> Unit
)

// Section 1: Main Section (Faculty Info & Previous Questions)
private fun getMainSectionItems(
    onNavigate: (String) -> Unit,
    context: Context,
    user: User?
): List<DrawerItem> {
    val portalItems = mutableListOf<DrawerItem>()

    // Add portal items based on user role
    when (user?.role) {
        UserRole.STUDENT -> {
            // Students see Student Portal and Hall Portal
            portalItems.add(
                DrawerItem(
                    iconRes = R.drawable.student_portal,
                    title = "Student Portal",
                    action = {
                        onNavigate(Screen.WebView.createRoute(PortalUrls.STUDENT_PORTAL, PortalTitles.STUDENT_PORTAL))
                    }
                )
            )
            portalItems.add(
                DrawerItem(
                    iconRes = R.drawable.hall_portal,
                    title = "Hall Portal",
                    action = {
                        onNavigate(Screen.WebView.createRoute(PortalUrls.HALL_PORTAL, PortalTitles.HALL_PORTAL))
                    }
                )
            )
        }
        UserRole.TEACHER -> {
            // Teachers see Teacher Portal
            portalItems.add(
                DrawerItem(
                    iconRes = R.drawable.teacher_portal,
                    title = "Teacher Portal",
                    action = {
                        onNavigate(Screen.WebView.createRoute(PortalUrls.TEACHER_PORTAL, PortalTitles.TEACHER_PORTAL))
                    }
                )
            )
        }
        else -> {
            // If no role or unknown role, show all portals
            portalItems.addAll(listOf(
                DrawerItem(
                    iconRes = R.drawable.student_portal,
                    title = "Student Portal",
                    action = {
                        onNavigate(Screen.WebView.createRoute(PortalUrls.STUDENT_PORTAL, PortalTitles.STUDENT_PORTAL))
                    }
                ),
                DrawerItem(
                    iconRes = R.drawable.hall_portal,
                    title = "Hall Portal",
                    action = {
                        onNavigate(Screen.WebView.createRoute(PortalUrls.HALL_PORTAL, PortalTitles.HALL_PORTAL))
                    }
                ),
                DrawerItem(
                    iconRes = R.drawable.teacher_portal,
                    title = "Teacher Portal",
                    action = {
                        onNavigate(Screen.WebView.createRoute(PortalUrls.TEACHER_PORTAL, PortalTitles.TEACHER_PORTAL))
                    }
                )
            ))
        }
    }

    // Add common items that everyone sees
    portalItems.addAll(
        listOf(
            DrawerItem(
                iconRes = R.drawable.blc,
                title = "BLC",
                action = {
                    onNavigate(Screen.WebView.createRoute(PortalUrls.BLC, PortalTitles.BLC))
                }
            ),
            DrawerItem(
                iconRes = R.drawable.faculty_info,
                title = "Faculty Info",
                action = {
                     onNavigate(Screen.FacultyInfo.route)
                }
            ),
            DrawerItem(
                iconRes =  R.drawable.previous_question,
                title = "Previous Questions",
                action = {
                    val previousQuestionDriveUrl = "https://drive.google.com/drive/folders/1oGno4UzJxg65H-iyWR-z2uB9kBgs2Nba?usp=sharing"
                    val intent = Intent(Intent.ACTION_VIEW, previousQuestionDriveUrl.toUri())
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.setPackage("com.google.android.apps.docs")
                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "Google Docs not found", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        )
    )

    return portalItems
}

// Section 2: Community (DIUCS Connect)
private fun getCommunitySectionItems(
    onNavigate: (String) -> Unit
): List<DrawerItem> {
    return listOf(
        DrawerItem(
            iconRes = R.drawable.department_24,
            title = "DIUCS Connect",
            subtitle = "Join our newest community!",
            action = {
                 onNavigate(Screen.Community.route)
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
                onNavigate(Screen.NotificationOptimization.route)
            }
        )
    )
}

// Section 4: Support & More
private fun getSupportSectionItems(
    context: Context,
    onShowMeetTheDevelopersDialog: () -> Unit
): List<DrawerItem> {
    return listOf(
        DrawerItem(
            iconRes = R.drawable.star_24,
            title = "Rate This App",
            action = {
                val packageName = context.packageName
                val playStoreUrl = "market://details?id=$packageName"
                val intent = Intent(Intent.ACTION_VIEW, playStoreUrl.toUri())
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "Play Store not found", Toast.LENGTH_SHORT).show()
                }
            }
        ),
        DrawerItem(
            icon = Icons.Outlined.Feedback,
            title = "Send Feedback",
            subtitle = "Help us to improve",
            isExternal = true,
            action = {
                val communityFeedbackChannelUrl = "https://m.me/ch/AbZ7MiSzbx1f5-rc/?send_source=cm%3Acopy_invite_link"
                val intent = Intent(Intent.ACTION_VIEW, communityFeedbackChannelUrl.toUri())
                intent.setPackage("com.facebook.orca")
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    // Messenger not installed — fallback to browser or Facebook Lite
                    val fallbackIntent = Intent(Intent.ACTION_VIEW, communityFeedbackChannelUrl.toUri())
                    context.startActivity(fallbackIntent)
                }

            }
        ),
        DrawerItem(
            icon = Icons.Outlined.ThumbUp,
            title = "Follow & Support",
            isExternal = true,
            action = {
                val appFacebookPageUrl = "https://www.facebook.com/profile.php?id=61572247479723"
                val intent = Intent(Intent.ACTION_VIEW, appFacebookPageUrl.toUri())
                intent.setPackage("com.facebook.katana")
                try{
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    // Facebook not installed — fallback to browser
                    val fallbackIntent = Intent(Intent.ACTION_VIEW, appFacebookPageUrl.toUri())
                    context.startActivity(fallbackIntent)
                }
            }
        ),
        DrawerItem(
            icon = Icons.Outlined.Groups,
            title = "Meet The Developers",
            action = {
                    onShowMeetTheDevelopersDialog()
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


// Developer data class
data class Developer(
    val pictureRes: Int,
    val name: String,
    val gmail: String,
    val department: String
)

@Composable
fun AboutUsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    developers: List<Developer>,
    dialogTitle: String // Added dialogTitle parameter
){
    if(showDialog){
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 16.dp
            ){
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ){
                    Text(
                        text = dialogTitle.uppercase(), // Using dynamic dialogTitle
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    developers.forEach { developer ->
                        DeveloperCard(developer)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DeveloperCard(developer: Developer) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
            .shadow(6.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                painter = painterResource(developer.pictureRes),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    developer.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    developer.department,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    developer.gmail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    SocialIconButton(R.drawable.facebook) {
                        val url = if (developer.name == "Ismam Hasan Ovi") {
                            "https://www.facebook.com/coder.OVI"
                        } else {
                            "https://www.facebook.com/marufrayhan606"
                        }
                        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    SocialIconButton(R.drawable.github) {
                        val url = if (developer.name == "Ismam Hasan Ovi") {
                            "https://github.com/oviii-001"
                        } else {
                            "https://github.com/marufrayhan606"
                        }
                        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    SocialIconButton(R.drawable.linkedin) {
                        val url = if (developer.name == "Ismam Hasan Ovi") {
                            "https://www.linkedin.com/in/ismamovi"
                        } else {
                            "https://www.linkedin.com/in/marufrayhan606/"
                        }
                        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                    }
                }
            }
        }
    }
}

@Composable
fun SocialIconButton(iconRes: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
    }
}
