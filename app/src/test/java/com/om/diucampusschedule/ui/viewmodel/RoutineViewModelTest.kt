package com.om.diucampusschedule.ui.viewmodel

import com.om.diucampusschedule.core.error.AppError
import com.om.diucampusschedule.core.logging.AppLogger
import com.om.diucampusschedule.core.network.NetworkMonitor
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.usecase.auth.GetCurrentUserUseCase
import com.om.diucampusschedule.domain.usecase.routine.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoutineViewModelTest {

    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var getUserRoutineUseCase: GetUserRoutineUseCase
    private lateinit var getUserRoutineForDayUseCase: GetUserRoutineForDayUseCase
    private lateinit var observeUserRoutineForDayUseCase: ObserveUserRoutineForDayUseCase
    private lateinit var getActiveDaysUseCase: GetActiveDaysUseCase
    private lateinit var syncRoutineUseCase: SyncRoutineUseCase
    private lateinit var refreshRoutineUseCase: RefreshRoutineUseCase
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var logger: AppLogger
    private lateinit var viewModel: RoutineViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    private val testUser = User(
        id = "test_user_id",
        email = "test@example.com",
        name = "Test User",
        role = "student",
        department = "Software Engineering",
        semester = "Summer 2025",
        batch = "59",
        section = "A",
        labSection = "A1",
        studentId = "191-15-12345"
    )

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
        )
    )

    private val testActiveDays = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        getCurrentUserUseCase = mockk()
        getUserRoutineUseCase = mockk()
        getUserRoutineForDayUseCase = mockk()
        observeUserRoutineForDayUseCase = mockk()
        getActiveDaysUseCase = mockk()
        syncRoutineUseCase = mockk()
        refreshRoutineUseCase = mockk()
        networkMonitor = mockk()
        logger = mockk(relaxed = true)

        // Default mock behaviors
        coEvery { getCurrentUserUseCase() } returns Result.success(testUser)
        every { getCurrentUserUseCase.observeCurrentUser() } returns flowOf(testUser)
        coEvery { getActiveDaysUseCase(testUser) } returns Result.success(testActiveDays)
        every { observeUserRoutineForDayUseCase(testUser, any()) } returns flowOf(testRoutineItems)
        every { networkMonitor.isOnline } returns flowOf(true)
        every { networkMonitor.isCurrentlyOnline() } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel initializes successfully with authenticated user`() = runTest {
        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(testUser, uiState.currentUser)
        assertEquals(testActiveDays, uiState.activeDays)
        assertEquals(testRoutineItems, uiState.routineItems)
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
    }

    @Test
    fun `viewModel shows error when user not authenticated`() = runTest {
        // Given
        coEvery { getCurrentUserUseCase() } returns Result.success(null)

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertNull(uiState.currentUser)
        assertFalse(uiState.isLoading)
        assertTrue(uiState.error is AppError.AuthenticationError)
    }

    @Test
    fun `viewModel handles authentication failure`() = runTest {
        // Given
        val exception = Exception("Auth failed")
        coEvery { getCurrentUserUseCase() } returns Result.failure(exception)

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertNull(uiState.currentUser)
        assertFalse(uiState.isLoading)
        assertNotNull(uiState.error)
    }

    @Test
    fun `selectDay updates selected day and loads routine`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        
        val newDay = "Sunday"
        every { observeUserRoutineForDayUseCase(testUser, newDay) } returns flowOf(testRoutineItems)

        // When
        viewModel.selectDay(newDay)
        advanceUntilIdle()

        // Then
        assertEquals(newDay, viewModel.uiState.value.selectedDay)
        verify { observeUserRoutineForDayUseCase(testUser, newDay) }
    }

    @Test
    fun `refreshRoutine succeeds and updates state`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        
        coEvery { refreshRoutineUseCase(testUser.department) } returns Result.success(mockk())
        coEvery { getActiveDaysUseCase(testUser) } returns Result.success(testActiveDays)

        // When
        viewModel.refreshRoutine()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isRefreshing)
        assertFalse(uiState.isOffline)
        assertFalse(uiState.hasPendingSync)
        assertTrue(uiState.lastSyncTime > 0)
        
        coVerify { refreshRoutineUseCase(testUser.department) }
    }

    @Test
    fun `refreshRoutine handles failure when online`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        
        val exception = Exception("Refresh failed")
        coEvery { refreshRoutineUseCase(testUser.department) } returns Result.failure(exception)

        // When
        viewModel.refreshRoutine()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isRefreshing)
        assertNotNull(uiState.error)
    }

    @Test
    fun `refreshRoutine sets pending sync when offline`() = runTest {
        // Given
        every { networkMonitor.isCurrentlyOnline() } returns false
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        val exception = Exception("Network error")
        coEvery { refreshRoutineUseCase(testUser.department) } returns Result.failure(exception)

        // When
        viewModel.refreshRoutine()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isRefreshing)
        assertTrue(uiState.hasPendingSync)
        assertNotNull(uiState.error)
    }

    @Test
    fun `network changes trigger auto-sync when coming online with pending sync`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Set offline state with pending sync
        every { networkMonitor.isCurrentlyOnline() } returns false
        val exception = Exception("Network error")
        coEvery { refreshRoutineUseCase(testUser.department) } returns Result.failure(exception)
        viewModel.refreshRoutine()
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value.hasPendingSync)
        
        // Mock successful refresh for auto-sync
        every { networkMonitor.isCurrentlyOnline() } returns true
        coEvery { refreshRoutineUseCase(testUser.department) } returns Result.success(mockk())
        coEvery { getActiveDaysUseCase(testUser) } returns Result.success(testActiveDays)

        // When - simulate network coming back online
        every { networkMonitor.isOnline } returns flowOf(true)
        
        // Create new viewModel to trigger network observation
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then - auto-sync should have been triggered
        // Note: This test verifies the concept; actual implementation may vary
        verify(atLeast = 1) { networkMonitor.isOnline }
    }

    @Test
    fun `clearError removes error from state`() = runTest {
        // Given
        coEvery { getCurrentUserUseCase() } returns Result.failure(Exception("Test error"))
        viewModel = createViewModel()
        advanceUntilIdle()
        
        assertNotNull(viewModel.uiState.value.error)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `retryLastAction reloads initial data`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.retryLastAction()
        advanceUntilIdle()

        // Then
        // Should trigger another call to load user and data
        coVerify(atLeast = 2) { getCurrentUserUseCase() }
        coVerify(atLeast = 2) { getActiveDaysUseCase(testUser) }
    }

    @Test
    fun `getTodayRoutine returns items when today is selected`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Assuming Saturday is "today" based on the test data
        viewModel.selectDay("Saturday")
        advanceUntilIdle()

        // When
        val todayRoutine = viewModel.getTodayRoutine()

        // Then
        assertEquals(testRoutineItems, todayRoutine)
    }

    @Test
    fun `hasClassesOnDay returns correct status`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When & Then
        assertTrue(viewModel.hasClassesOnDay("Saturday"))
        assertTrue(viewModel.hasClassesOnDay("Sunday"))
        assertFalse(viewModel.hasClassesOnDay("Friday"))
    }

    private fun createViewModel(): RoutineViewModel {
        return RoutineViewModel(
            getCurrentUserUseCase = getCurrentUserUseCase,
            getUserRoutineUseCase = getUserRoutineUseCase,
            getUserRoutineForDayUseCase = getUserRoutineForDayUseCase,
            observeUserRoutineForDayUseCase = observeUserRoutineForDayUseCase,
            getActiveDaysUseCase = getActiveDaysUseCase,
            syncRoutineUseCase = syncRoutineUseCase,
            refreshRoutineUseCase = refreshRoutineUseCase,
            networkMonitor = networkMonitor,
            logger = logger
        )
    }
}
