package com.om.diucampusschedule.ui.screens.today

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.om.diucampusschedule.R
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.ui.navigation.Screen
import com.om.diucampusschedule.ui.utils.ScreenConfig
import com.om.diucampusschedule.ui.utils.TopAppBarIconSize.topbarIconSize
import com.om.diucampusschedule.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .run { ScreenConfig.run { withTopAppBar() } }
    ) {
        // Custom Top App Bar with modern design
        CustomTopAppBar(
            user = authState.user,
            onProfileClick = {
                navController.navigate(Screen.Profile.route)
            },
            onMenuClick = {
                // TODO: Implement menu functionality
            },
            onNotificationClick = {
                // TODO: Implement notification functionality
            }
        )
        
        // Subtle divider for modern look
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
        
        // Main content area (currently empty as requested)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Content will be implemented later
            Text(
                text = "Today Screen Content\nComing Soon...",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTopAppBar(
    user: User?,
    onProfileClick: () -> Unit,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
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
            // Right side: Notification + Menu icons
            IconButton(
                onClick = onNotificationClick
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(topbarIconSize)
                )
            }
            
            IconButton(
                onClick = onMenuClick
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.nav_drawer_filled),
                    contentDescription = "Menu",
                    modifier = Modifier.size(topbarIconSize),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
