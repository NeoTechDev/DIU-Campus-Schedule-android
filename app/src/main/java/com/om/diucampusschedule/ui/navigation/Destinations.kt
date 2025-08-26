package com.om.diucampusschedule.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

// Navigation Routes
sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")  // initial screen for new users
    object SignIn : Screen("signin")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object EmailVerification : Screen("email_verification")
    object RegsitrationForm : Screen("registration_form")
    object Today : Screen("today")  // usual start screen
    object Routine : Screen("routine")
    object ExamRoutine : Screen("exam_routine")
    object Tasks : Screen("tasks")
    object Notes : Screen("notes")
    object EmptyRooms : Screen("empty_rooms")
    object FacultyInfo : Screen("faculty_info")
    object Profile : Screen("profile")
    object Debug : Screen("debug")
}

// Bottom Navigation Items
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Today : BottomNavItem(
        route = Screen.Today.route,
        title = "Today",
        selectedIcon = Icons.Filled.Today,
        unselectedIcon = Icons.Outlined.Today
    )
    
    object Routine : BottomNavItem(
        route = Screen.Routine.route,
        title = "Routine",
        selectedIcon = Icons.Filled.Schedule,
        unselectedIcon = Icons.Outlined.Schedule
    )
    
    object Empty : BottomNavItem(
        route = Screen.EmptyRooms.route,
        title = "Empty",
        selectedIcon = Icons.Filled.Room,
        unselectedIcon = Icons.Outlined.Room
    )
    
    object Tasks : BottomNavItem(
        route = Screen.Tasks.route,
        title = "Tasks",
        selectedIcon = Icons.Filled.Task,
        unselectedIcon = Icons.Outlined.Task
    )
    
    object Notes : BottomNavItem(
        route = Screen.Notes.route,
        title = "Notes",
        selectedIcon = Icons.Filled.Note,
        unselectedIcon = Icons.Outlined.Note
    )
    
    companion object {
        val items = listOf(Today, Routine, Empty, Tasks, Notes)
    }
}
