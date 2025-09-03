package com.om.diucampusschedule.ui.screens.facultyinfo

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.om.diucampusschedule.R
import com.om.diucampusschedule.domain.model.Faculty
import com.om.diucampusschedule.ui.theme.EaseInOutCubic
import com.om.diucampusschedule.util.FacultyUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun FacultyInfoScreen(onBack: NavHostController) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var facultyList by remember { mutableStateOf<List<Faculty>>(emptyList()) }
    var filteredFacultyList by remember { mutableStateOf<List<Faculty>>(emptyList()) }

    // Pagination variables
    val pageSize = 15
    var visibleItemCount by remember { mutableIntStateOf(pageSize) }
    var isLoadingMore by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        facultyList = FacultyUtils.loadFacultyData(context)
        filteredFacultyList = facultyList // Initially show all faculty
    }

    LaunchedEffect(key1 = searchQuery) {
        filteredFacultyList = if (searchQuery.isNotEmpty()) {
            facultyList.filter { faculty ->
                faculty.name.contains(searchQuery, ignoreCase = true) ||
                        faculty.faculty_initial.contains(searchQuery, ignoreCase = true)
            }
        } else {
            facultyList // Reset to full list when search query is empty
        }
        // Reset pagination when search query changes
        visibleItemCount = pageSize
    }


    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic))
    ) {
        Scaffold(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Crossfade(
                                targetState = isSearchActive,
                                animationSpec = tween(300)
                            ) { searching ->
                                when (searching) {
                                    true -> {
                                        TextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(focusRequester),
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                                disabledContainerColor = MaterialTheme.colorScheme.surface,
                                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                                                cursorColor = MaterialTheme.colorScheme.primary
                                            ),
                                            placeholder = {
                                                Text(
                                                    "Search faculty by name or initial...",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            },
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                            keyboardActions = KeyboardActions(
                                                onSearch = {
                                                    keyboardController?.hide()
                                                }
                                            ),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }

                                    false -> {
                                        AnimatedVisibility(
                                            visible = true,
                                            enter = fadeIn(animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic)) + slideInHorizontally(animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic))
                                        ) {
                                            Text(
                                                "Faculty Information",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic)) + slideInHorizontally(animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic), initialOffsetX = { -it })
                        ) {
                            IconButton(
                                onClick = {
                                    if (isSearchActive) {
                                        isSearchActive = false
                                        searchQuery = ""
                                        keyboardController?.hide()
                                    } else {
                                        onBack.popBackStack()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    actions = {
                        AnimatedVisibility(
                            visible = !isSearchActive,
                            enter = fadeIn(animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic)) + slideInHorizontally(initialOffsetX = { it }),
                            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it }),
                        ) {
                            IconButton(
                                onClick = {
                                    isSearchActive = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                AnimatedVisibility(
                    visible = filteredFacultyList.isEmpty() && searchQuery.isNotEmpty(),
                    enter = fadeIn(animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Faculty Not Found!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = filteredFacultyList.isNotEmpty(),
                    enter = fadeIn(animationSpec = tween(durationMillis = 500, easing = EaseInOutCubic)) + slideInVertically(animationSpec = tween(durationMillis = 500, easing = EaseInOutCubic))
                ) {
                    LazyColumn(state = lazyListState) {
                        val paginatedList = filteredFacultyList.take(visibleItemCount)

                        items(paginatedList) { faculty ->
                            FacultyCard(faculty = faculty, contactNumber = faculty.contact_number)
                        }

                        // Show more button if there are more items to display
                        item {
                            if (visibleItemCount < filteredFacultyList.size) {
                                TextButton(
                                    onClick = {
                                        if (!isLoadingMore) {
                                            isLoadingMore = true
                                            coroutineScope.launch {
                                                // Simulate loading delay
                                                delay(800)
                                                // Increase visible items by another page size
                                                visibleItemCount = minOf(visibleItemCount + pageSize, filteredFacultyList.size)
                                                isLoadingMore = false
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    enabled = !isLoadingMore
                                ) {
                                    if (isLoadingMore) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = MaterialTheme.colorScheme.primary,
                                                strokeWidth = 2.dp
                                            )
                                            Text(
                                                text = "Loading",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.primary,
                                                textAlign = TextAlign.Center,
                                                letterSpacing = 2.sp,
                                                modifier = Modifier.padding(start = 4.dp)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "See more",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary,
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
}

@Composable
fun FacultyCard(faculty: Faculty, contactNumber: String) {
    val context = LocalContext.current
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 400, easing = EaseInOutCubic)) + slideInVertically(animationSpec = tween(durationMillis = 400, easing = EaseInOutCubic))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                // Header with faculty name and designation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Circular avatar with first letter of the faculty's name
                    Column(
                        modifier = Modifier
                            .width(200.dp)
                    ) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(durationMillis = 200, easing = EaseInOutCubic))
                        ) {
                            Text(
                                text = faculty.name.uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                color = if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                        }
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(durationMillis = 400, easing = EaseInOutCubic))
                        ) {
                            Text(
                                text = faculty.designation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                maxLines = 2
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600, easing = EaseInOutCubic))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ){
                            //Phone Call icon
                            Icon(
                                painter = painterResource(id = R.drawable.call),
                                contentDescription = "Call Faculty",
                                tint = if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(35.dp)
                                    .clickable(
                                        onClick = {
                                            // Launch the phone dialer with the contact number pre-filled
                                            val intent = Intent(Intent.ACTION_DIAL,
                                                "tel:$contactNumber".toUri())
                                            context.startActivity(intent)
                                        }
                                    )
                            )

                            //Gmail icon
                            Icon(
                                painter = painterResource(id = R.drawable.envelope_24),
                                contentDescription = "Email Faculty",
                                tint = if(!isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(30.dp)
                                    .clickable(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                data = "mailto:${faculty.email}".toUri()
                                            }
                                            try {
                                                ContextCompat.startActivity(context, intent, null)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "No email app found.", Toast.LENGTH_SHORT).show()
                                                e.printStackTrace()
                                            }
                                        }
                                    )
                            )
                        }
                    }
                }

                // Details section with faculty information
                Column(modifier = Modifier.padding(16.dp)) {
                    FacultyInfoRow(label = "Initial:", value = faculty.faculty_initial)
                    FacultyInfoRow(label = "ID:", value = faculty.employee_id)
                    FacultyInfoRow(label = "Contact:", value = faculty.contact_number)
                    FacultyInfoRow(label = "Email:", value = faculty.email)
                    FacultyInfoRow(label = "Room:", value = faculty.room_no.ifEmpty { "N/A" })
                    FacultyInfoRow(label = "Course:", value = faculty.course.ifEmpty { "N/A" })
                }
            }
        }
    }
}

@Composable
fun FacultyInfoRow(label: String, value: String) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 500, easing = EaseInOutCubic)) + slideInHorizontally(animationSpec = tween(durationMillis = 500, easing = EaseInOutCubic))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if(label == "Email:" && value.isNotEmpty()){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ){
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.duplicate),
                        contentDescription = "Copy",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                            .clickable {
                                clipboardManager.setText(AnnotatedString(value))
                                Toast.makeText(context, "Email copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                    )
                }
            }
            else{
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FacultyInfoScreenPreview() {
    FacultyInfoScreen(onBack = rememberNavController())
}