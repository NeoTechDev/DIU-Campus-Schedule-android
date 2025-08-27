package com.om.diucampusschedule.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.om.diucampusschedule.R
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    currentRoute: String?,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val title = when (currentRoute) {
        Screen.Today.route -> "Today"
        Screen.Routine.route -> "Routines"
        Screen.EmptyRooms.route -> "Empty Rooms"
        Screen.Tasks.route -> "Tasks"
        Screen.Notes.route -> "Notes"
        Screen.Profile.route -> "Profile"
        Screen.ExamRoutine.route -> "Exam Routines"
        Screen.FacultyInfo.route -> "Faculty Info"
        else -> "DIU Campus Schedule"
    }

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
            // Menu icon for main app screens
            IconButton(onClick = { 
                // TODO: Implement drawer or menu functionality
                // For now, it's just a visual element
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.nav_drawer_filled),
                    contentDescription = "Menu",
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        actions = {
            // Profile icon for all main screens except Profile screen itself
            if (currentRoute != Screen.Profile.route) {
                IconButton(
                    onClick = { 
                        navController.navigate(Screen.Profile.route)
                    }
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.profile_filled),
                        contentDescription = "Profile",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
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
