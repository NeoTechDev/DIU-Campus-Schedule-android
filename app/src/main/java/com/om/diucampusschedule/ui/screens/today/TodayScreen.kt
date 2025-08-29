package com.om.diucampusschedule.ui.screens.today

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
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
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Custom Top App Bar with minimal padding
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
        
        // Main content area (currently empty as requested)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Content will be implemented later
            Text(
                text = "Today Screen Content\nComing Soon...",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
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
            // Left side: Menu + User profile section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Menu icon
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(36.dp) // Reduce button size
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.nav_drawer_filled),
                        contentDescription = "Menu",
                        modifier = Modifier.size(18.dp), // Reduce icon size
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // User profile section (clickable)
                Row(
                    modifier = Modifier
                        .clickable { onProfileClick() }
                        .padding(horizontal = 4.dp, vertical = 4.dp), // Reduce vertical padding
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Enhanced profile picture with AsyncImage
                    ProfilePicture(
                        user = user,
                        size = 28.dp // Further reduce profile picture size
                    )
                    
                    Spacer(modifier = Modifier.width(6.dp)) // Further reduce spacing
                    
                    // Enhanced user info with better UX
                    UserInfoSection(
                        user = user,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        actions = {
            // Right side: Notification icon (moved to actions for proper display)
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier.size(36.dp) // Reduce button size
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp) // Keep notification icon slightly larger
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        windowInsets = WindowInsets(0), // Remove default window insets
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .height(50.dp) // Increase height slightly to accommodate "Active now" text
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
            MaterialTheme.colorScheme.secondary
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
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.7f)
                    )
                )
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
            .padding(vertical = 2.dp), // Add some vertical padding
        verticalArrangement = Arrangement.spacedBy(1.dp) // Space between items
    ) {
        // User name with enhanced styling
        Text(
            text = user?.name ?: "User",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        // Enhanced subtitle with better formatting
        Text(
            text = getUserSubtitle(user),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Medium
        )
        
        // Active status with better spacing and visibility
        if (user != null) {
            Text(
                text = "Active now",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp // Slightly smaller to fit better
                ),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                fontWeight = FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}

/**
 * Get user initials for profile picture
 */
private fun getUserInitials(user: User?): String {
    return if (user?.role?.name == "TEACHER") {
        user.initial ?: "T"
    } else {
        user?.name?.split(" ")?.take(2)?.mapNotNull { it.firstOrNull()?.uppercaseChar() }?.joinToString("") ?: "U"
    }
}

/**
 * Get user subtitle based on role
 */
private fun getUserSubtitle(user: User?): String {
    return if (user?.role?.name == "STUDENT") {
        val batch = user.batch ?: ""
        val section = user.section ?: ""
        val department = user.department ?: ""
        "$batch-$section • $department"
    } else {
        val department = user?.department ?: ""
        department
    }
}

private fun getProfilePictureUrl(user: User?): String {
    return user?.profilePictureUrl ?: ""
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TodayScreenPreview() {
    val navController = rememberNavController()
    TodayScreen(navController = navController)
}
