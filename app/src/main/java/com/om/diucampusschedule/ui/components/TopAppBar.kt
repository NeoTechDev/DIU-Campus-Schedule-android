package com.om.diucampusschedule.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.om.diucampusschedule.ui.navigation.Screen
import com.om.diucampusschedule.ui.theme.DIUCampusScheduleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DIUTopAppBar(
    title: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean = false,
    navigateUp: () -> Unit = { navController.navigateUp() },
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun MainTopAppBar(
    currentRoute: String?,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val title = when (currentRoute) {
        Screen.Routine.route -> "Class Routine"
        Screen.Profile.route -> "Profile"
        // TODO: Add other screen titles when implemented
        /*
        Screen.Today.route -> "Today's Schedule"
        Screen.ExamRoutine.route -> "Exam Schedule"
        Screen.Tasks.route -> "Tasks"
        Screen.Notes.route -> "Notes"
        Screen.EmptyRooms.route -> "Empty Rooms"
        Screen.FacultyInfo.route -> "Faculty Info"
        */
        else -> "DIU Campus Schedule"
    }

    DIUTopAppBar(
        title = title,
        navController = navController,
        modifier = modifier,
        canNavigateBack = false,
        actions = {
            // Menu actions for screens
            when (currentRoute) {
                Screen.Routine.route -> {
                    // Profile button for Routine screen
                    IconButton(
                        onClick = { 
                            navController.navigate(Screen.Profile.route)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Profile"
                        )
                    }
                }
                Screen.Profile.route -> {
                    // No additional actions for Profile screen
                }
                // TODO: Add other screen actions when implemented
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DIUTopAppBarPreview() {
    DIUCampusScheduleTheme {
        DIUTopAppBar(
            title = "Today's Schedule",
            navController = rememberNavController(),
            canNavigateBack = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainTopAppBarPreview() {
    DIUCampusScheduleTheme {
        MainTopAppBar(
            currentRoute = Screen.Today.route,
            navController = rememberNavController()
        )
    }
}
