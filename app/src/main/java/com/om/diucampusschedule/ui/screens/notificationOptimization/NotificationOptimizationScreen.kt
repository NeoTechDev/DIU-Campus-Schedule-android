package com.om.diucampusschedule.ui.screens.notificationOptimization

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.diucampusschedule.R
import com.om.diucampusschedule.ui.utils.ScreenConfig
import com.om.diucampusschedule.ui.utils.ScreenConfig.withTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationOptimizationScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding() // Add navigation bar padding to prevent overlap
            .background(MaterialTheme.colorScheme.background)
            .run { ScreenConfig.run { withTopAppBar() } }
    ) {
        // Material You style top bar
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Notification Optimization",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            windowInsets = ScreenConfig.getTopAppBarWindowInsets(handleStatusBar = true)
        )
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Header section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Fix your class notification delay issue",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 20.sp,
                            lineHeight = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Follow these steps to make sure notifications arrive when they should",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Step items (Google style list items)
            item {
                StepListItem(
                    icon = R.drawable.eco_battery,
                    title = "Turn off battery optimization",
                    subtitle = "Let the app run in the background",
                    steps = listOf(
                        "Go to Settings > Battery > Battery Optimization",
                        "Find \"DIU Campus Schedule\" in the app list",
                        "Select \"Don't optimize\" or \"Unrestricted\"",
                        "Save your changes"
                    )
                )
            }

            item {
                StepListItem(
                    icon = R.drawable.delete_24,
                    title = "Clear app cache",
                    subtitle = "Remove temporary files",
                    steps = listOf(
                        "Go to Settings > Apps > DIU Campus Schedule",
                        "Tap on 'Storage & cache'",
                        "Tap 'Clear cache'",
                        "Restart the app"
                    )
                )
            }

            item {
                StepListItem(
                    icon = R.drawable.baseline_access_time_24,
                    title = "Allow background activity",
                    subtitle = "Keep the app running when not in use",
                    steps = listOf(
                        "Go to Settings > Apps > DIU Campus Schedule",
                        "Tap on 'Battery'",
                        "Select 'Allow background activity'",
                        "Toggle any 'Autostart' option if available"
                    )
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Bottom button outside LazyColumn
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Couldn't open app settings", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open app settings")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepListItem(
    icon: Int,
    title: String,
    subtitle: String,
    steps: List<String>
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Steps
            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(24.dp)
                    )
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}