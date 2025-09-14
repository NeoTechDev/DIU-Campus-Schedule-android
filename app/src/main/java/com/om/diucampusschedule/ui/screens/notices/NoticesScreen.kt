package com.om.diucampusschedule.ui.screens.notices

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButtonDefaults.Icon
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticesScreen(
    todayViewModel: TodayViewModel = hiltViewModel(),
    onBack: () -> Unit
) {

    val notices by todayViewModel.notices.collectAsState()
    val isLoading by todayViewModel.isNoticesLoading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        todayViewModel.fetchNotices()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Department Notices",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (notices.isEmpty()) {
                Text("No notices found.", modifier = Modifier.align(Alignment.Center), style = MaterialTheme.typography.bodyLarge)
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
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(notice.link))
                                    context.startActivity(intent)
                                }
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
}
