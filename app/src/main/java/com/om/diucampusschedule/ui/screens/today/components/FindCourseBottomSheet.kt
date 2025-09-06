package com.om.diucampusschedule.ui.screens.today.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.om.diucampusschedule.R
import com.om.diucampusschedule.ui.theme.AppPrimaryColorLight
import com.om.diucampusschedule.ui.viewmodel.CourseSearchViewModel

@Composable
fun FindCourseBottomSheetContent(
    onDismiss: () -> Unit,
    courseSearchViewModel: CourseSearchViewModel = hiltViewModel()
) {
    val searchResults by courseSearchViewModel.searchResults.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    // Search immediately when query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            courseSearchViewModel.searchCourses(searchQuery)
        } else {
            courseSearchViewModel.clearResults()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp)
        ) {
            // Title
            Text(
                text = "Find Course",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = DividerDefaults.Thickness, color = Color.LightGray.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { 
                Text(
                    text = "Search by course code or name",
                    fontSize = 14.sp,
                    color = Color.Gray
                ) 
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.Gray
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = Color.Gray
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppPrimaryColorLight,
                unfocusedBorderColor = Color.LightGray,
                cursorColor = AppPrimaryColorLight
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // Results Section
        when {
            searchQuery.isEmpty() -> {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.search_course_sticker),
                            contentDescription = "Search",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Enter a course code or name to search",
                            color = Color.Gray
                        )
                    }
                }
            }
            searchResults.isEmpty() -> {
                // No results state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.not_found),
                            contentDescription = "No results",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No courses found!",
                            color = Color.Gray
                        )
                    }
                }
            }
            else -> {
                // Results list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(searchResults) { courseInfo ->
                        SimpleCourseListItem(
                            courseCode = courseInfo.courseCode,
                            courseName = courseInfo.courseName,
                            credit = courseInfo.credit
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleCourseListItem(
    courseCode: String,
    courseName: String,
    credit: Int
) {
    val surfaceVariant = if(isSystemInDarkTheme()) Color(0xFF2F3540) else Color(0xFFF0F5FF)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = surfaceVariant,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Course Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Course Code
                Text(
                    text = courseCode,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 16.sp,
                    color = AppPrimaryColorLight
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                // Course Name
                Text(
                    text = courseName,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Credit display with circular progress
            Box(
                contentAlignment = Alignment.Center
            ) {
                val creditProgress = (credit.coerceIn(1, 6) / 6f)

                // Circular indicator
                CircularProgressIndicator(
                    progress = { creditProgress },
                    modifier = Modifier.size(36.dp),
                    color = AppPrimaryColorLight,
                    strokeWidth = 3.dp,
                    trackColor = Color.LightGray.copy(alpha = 0.3f),
                    strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
                )

                // Credit number in center
                Text(
                    text = "$credit",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppPrimaryColorLight
                )
            }
        }
    }
}
