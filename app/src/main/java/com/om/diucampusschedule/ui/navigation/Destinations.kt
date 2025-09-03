package com.om.diucampusschedule.ui.navigation

import com.om.diucampusschedule.R

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
    object NoteEditor : Screen("note_editor")
    object EmptyRooms : Screen("empty_rooms")
    object FacultyInfo : Screen("faculty_info")
    object Profile : Screen("profile")
    object Community : Screen("community")
    object Debug : Screen("debug")
}

// Bottom Navigation Items
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: Int,
    val unselectedIcon: Int
) {
    object Today : BottomNavItem(
        route = Screen.Today.route,
        title = "Today",
        selectedIcon = R.drawable.today_filled,
        unselectedIcon = R.drawable.today_outlined
    )
    
    object Routine : BottomNavItem(
        route = Screen.Routine.route,
        title = "Routines",
        selectedIcon = R.drawable.routine_filled,
        unselectedIcon = R.drawable.routine_outlined
    )
    
    object Empty : BottomNavItem(
        route = Screen.EmptyRooms.route,
        title = "Rooms",
        selectedIcon = R.drawable.location_filled,
        unselectedIcon = R.drawable.location_outlined
    )
    
    object Tasks : BottomNavItem(
        route = Screen.Tasks.route,
        title = "Tasks",
        selectedIcon = R.drawable.task_filled,
        unselectedIcon = R.drawable.task_outlined
    )
    
    object Notes : BottomNavItem(
        route = Screen.Notes.route,
        title = "Notes",
        selectedIcon = R.drawable.notes_filled,
        unselectedIcon = R.drawable.notes_outlined
    )
    
    companion object {
        val items = listOf(Today, Routine, Empty, Tasks, Notes)
    }
}
