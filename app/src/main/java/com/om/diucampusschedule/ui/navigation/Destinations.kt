package com.om.diucampusschedule.ui.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")  // initial screen for new users
    object SignIn : Screen("signin")
    object SignUp : Screen("signup")
    object RegsitrationForm : Screen("registration_form")
    object Today : Screen("today")  // usual start screen
    object Routine : Screen("routine")
    object ExamRoutine : Screen("exam_routine")
    object Tasks : Screen("tasks")
    object Notes : Screen("notes")
    object EmptyRooms : Screen("empty_rooms")
    object FacultyInfo : Screen("faculty_info")
    object Profile : Screen("profile")
}
