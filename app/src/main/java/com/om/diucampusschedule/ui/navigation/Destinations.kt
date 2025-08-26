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

// Bottom Navigation Items - For now only Routine screen
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Routine : BottomNavItem(
        route = Screen.Routine.route,
        title = "Routine",
        selectedIcon = Icons.Filled.Schedule,
        unselectedIcon = Icons.Outlined.Schedule
    )
    
    // TODO: Add other navigation items when their screens are implemented
    /*
    object Today : BottomNavItem(
        route = Screen.Today.route,
        title = "Today",
        selectedIcon = Icons.Filled.Today,
        unselectedIcon = Icons.Outlined.Today
    )
    
    object ExamRoutine : BottomNavItem(
        route = Screen.ExamRoutine.route,
        title = "Exams",
        selectedIcon = Icons.Filled.Quiz,
        unselectedIcon = Icons.Outlined.Quiz
    )
    
    object Tasks : BottomNavItem(
        route = Screen.Tasks.route,
        title = "Tasks",
        selectedIcon = Icons.Filled.Task,
        unselectedIcon = Icons.Outlined.Task
    )
    
    object Profile : BottomNavItem(
        route = Screen.Profile.route,
        title = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
    */
    
    companion object {
        val items = listOf(Routine) // Only Routine for now
    }
}
