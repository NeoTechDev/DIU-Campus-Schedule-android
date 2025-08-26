package com.om.diucampusschedule.ui.screens.routine

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.om.diucampusschedule.core.error.AppError
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.ui.theme.DIUCampusScheduleTheme
import com.om.diucampusschedule.ui.viewmodel.RoutineUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoutineScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testRoutineItems = listOf(
        RoutineItem(
            id = "1",
            day = "Saturday",
            time = "08:30 AM - 10:00 AM",
            room = "604",
            courseCode = "CSE 4108",
            teacherInitial = "ABC",
            batch = "59",
            section = "A",
            labSection = "A1",
            semester = "Summer 2025",
            department = "Software Engineering",
            effectiveFrom = "22-07-2025"
        ),
        RoutineItem(
            id = "2",
            day = "Saturday",
            time = "10:15 AM - 11:45 AM",
            room = "605",
            courseCode = "CSE 4110",
            teacherInitial = "XYZ",
            batch = "59",
            section = "A",
            labSection = null,
            semester = "Summer 2025",
            department = "Software Engineering",
            effectiveFrom = "22-07-2025"
        )
    )

    private val testActiveDays = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday")

    @Test
    fun routineScreen_displaysLoadingState() {
        composeTestRule.setContent {
            DIUCampusScheduleTheme {
                val navController = rememberNavController()
                RoutineContent(
                    uiState = RoutineUiState(isLoading = true),
                    selectedDay = "Saturday",
                    onDaySelected = {},
                    onRefresh = {},
                    onRetry = {},
                    onErrorDismiss = {},
                    navController = navController
                )
            }
        }

        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }

    @Test
    fun routineScreen_displaysRoutineItems() {
        composeTestRule.setContent {
            DIUCampusScheduleTheme {
                val navController = rememberNavController()
                RoutineContent(
                    uiState = RoutineUiState(
                        routineItems = testRoutineItems,
                        activeDays = testActiveDays
                    ),
                    selectedDay = "Saturday",
                    onDaySelected = {},
                    onRefresh = {},
                    onRetry = {},
                    onErrorDismiss = {},
                    navController = navController
                )
            }
        }

        // Check if routine items are displayed
        composeTestRule.onNodeWithText("CSE 4108").assertIsDisplayed()
        composeTestRule.onNodeWithText("CSE 4110").assertIsDisplayed()
        composeTestRule.onNodeWithText("08:30 AM - 10:00 AM").assertIsDisplayed()
        composeTestRule.onNodeWithText("Room 604").assertIsDisplayed()
        composeTestRule.onNodeWithText("ABC").assertIsDisplayed()
    }

    @Test
    fun routineScreen_displaysActiveDays() {
        composeTestRule.setContent {
            DIUCampusScheduleTheme {
                val navController = rememberNavController()
                RoutineContent(
                    uiState = RoutineUiState(
                        routineItems = testRoutineItems,
                        activeDays = testActiveDays
                    ),
                    selectedDay = "Saturday",
                    onDaySelected = {},
                    onRefresh = {},
                    onRetry = {},
                    onErrorDismiss = {},
                    navController = navController
                )
            }
        }

        // Check if day tabs are displayed
        testActiveDays.forEach { day ->
            composeTestRule.onNodeWithText(day.take(3)).assertIsDisplayed()
        }
    }

    @Test
    fun routineScreen_daySelectionWorks() {
        var selectedDay = "Saturday"
        
        composeTestRule.setContent {
            DIUCampusScheduleTheme {
                val navController = rememberNavController()
                RoutineContent(
                    uiState = RoutineUiState(
                        routineItems = testRoutineItems,
                        activeDays = testActiveDays
                    ),
                    selectedDay = selectedDay,
                    onDaySelected = { selectedDay = it },
                    onRefresh = {},
                    onRetry = {},
                    onErrorDismiss = {},
                    navController = navController
                )
            }
        }

        // Click on Sunday
        composeTestRule.onNodeWithText("Sun").performClick()
        
        // Verify callback was called
        assert(selectedDay == "Sunday")
    }

    @Test
    fun routineScreen_displaysErrorState() {
        composeTestRule.setContent {
            DIUCampusScheduleTheme {
                val navController = rememberNavController()
                RoutineContent(
                    uiState = RoutineUiState(
                        error = AppError.NetworkError("Network connection failed")
                    ),
                    selectedDay = "Saturday",
                    onDaySelected = {},
                    onRefresh = {},
                    onRetry = {},
                    onErrorDismiss = {},
                    navController = navController
                )
            }
        }

        composeTestRule.onNodeWithText("Network connection failed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun routineScreen_displaysOfflineState() {
        composeTestRule.setContent {
            DIUCampusScheduleTheme {
                val navController = rememberNavController()
                RoutineContent(
                    uiState = RoutineUiState(
                        routineItems = testRoutineItems,
                        activeDays = testActiveDays,
                        isOffline = true
                    ),
                    selectedDay = "Saturday",
                    onDaySelected = {},
                    onRefresh = {},
                    onRetry = {},
                    onErrorDismiss = {},
                    navController = navController
                )
            }
        }

        composeTestRule.onNodeWithText("Offline").assertIsDisplayed()
    }

    @Test
    fun routineScreen_displaysRefreshingState() {
        composeTestRule.setContent {
            DIUCampusScheduleTheme {
                val navController = rememberNavController()
                RoutineContent(
                    uiState = RoutineUiState(
                        routineItems = testRoutineItems,
                        activeDays = testActiveDays,
                        isRefreshing = true
                    ),
                    selectedDay = "Saturday",
                    onDaySelected = {},
                    onRefresh = {},
                    onRetry = {},
                    onErrorDismiss = {},
                    navController = navController
                )
            }
        }

        composeTestRule.onNodeWithTag("refresh_indicator").assertIsDisplayed()
    }

    @Test
    fun routineScreen_refreshButtonWorks() {
        var refreshCalled = false
        
        composeTestRule.setContent {
            DIUCampusScheduleTheme {
                val navController = rememberNavController()
                RoutineContent(
                    uiState = RoutineUiState(
                        routineItems = testRoutineItems,
                        activeDays = testActiveDays
                    ),
                    selectedDay = "Saturday",
                    onDaySelected = {},
                    onRefresh = { refreshCalled = true },
                    onRetry = {},
                    onErrorDismiss = {},
                    navController = navController
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Refresh").performClick()
        
        assert(refreshCalled)
    }

    @Test
    fun routineScreen_displaysEmptyState() {
        composeTestRule.setContent {
            DIUCampusScheduleTheme {
                val navController = rememberNavController()
                RoutineContent(
                    uiState = RoutineUiState(
                        routineItems = emptyList(),
                        activeDays = testActiveDays
                    ),
                    selectedDay = "Saturday",
                    onDaySelected = {},
                    onRefresh = {},
                    onRetry = {},
                    onErrorDismiss = {},
                    navController = navController
                )
            }
        }

        composeTestRule.onNodeWithText("No classes scheduled").assertIsDisplayed()
    }

    @Test
    fun routineScreen_classItemsDisplayCorrectInfo() {
        composeTestRule.setContent {
            DIUCampusScheduleTheme {
                val navController = rememberNavController()
                RoutineContent(
                    uiState = RoutineUiState(
                        routineItems = testRoutineItems,
                        activeDays = testActiveDays
                    ),
                    selectedDay = "Saturday",
                    onDaySelected = {},
                    onRefresh = {},
                    onRetry = {},
                    onErrorDismiss = {},
                    navController = navController
                )
            }
        }

        // Check first routine item details
        composeTestRule.onNodeWithText("CSE 4108").assertIsDisplayed()
        composeTestRule.onNodeWithText("08:30 AM - 10:00 AM").assertIsDisplayed()
        composeTestRule.onNodeWithText("Room 604").assertIsDisplayed()
        composeTestRule.onNodeWithText("ABC").assertIsDisplayed()
        
        // Check second routine item details
        composeTestRule.onNodeWithText("CSE 4110").assertIsDisplayed()
        composeTestRule.onNodeWithText("10:15 AM - 11:45 AM").assertIsDisplayed()
        composeTestRule.onNodeWithText("Room 605").assertIsDisplayed()
        composeTestRule.onNodeWithText("XYZ").assertIsDisplayed()
    }
}
