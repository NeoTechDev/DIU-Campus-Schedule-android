package com.om.diucampusschedule.domain.usecase.routine

import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.repository.RoutineRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetUserRoutineUseCaseTest {

    private lateinit var routineRepository: RoutineRepository
    private lateinit var getUserRoutineUseCase: GetUserRoutineUseCase

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

    @Before
    fun setup() {
        routineRepository = mockk()
        getUserRoutineUseCase = GetUserRoutineUseCase(routineRepository)
    }

    @Test
    fun `invoke should return success when repository returns routine items`() = runTest {
        // Given
        coEvery { routineRepository.getRoutineForUser(testUser) } returns Result.success(testRoutineItems)

        // When
        val result = getUserRoutineUseCase(testUser)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(testRoutineItems, result.getOrNull())
        coVerify { routineRepository.getRoutineForUser(testUser) }
    }

    @Test
    fun `invoke should return failure when repository throws exception`() = runTest {
        // Given
        val exception = Exception("Network error")
        coEvery { routineRepository.getRoutineForUser(testUser) } returns Result.failure(exception)

        // When
        val result = getUserRoutineUseCase(testUser)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { routineRepository.getRoutineForUser(testUser) }
    }

    @Test
    fun `invoke should return empty list when no routine items found`() = runTest {
        // Given
        coEvery { routineRepository.getRoutineForUser(testUser) } returns Result.success(emptyList())

        // When
        val result = getUserRoutineUseCase(testUser)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
        coVerify { routineRepository.getRoutineForUser(testUser) }
    }

    @Test
    fun `invoke should filter routine items by user criteria`() = runTest {
        // Given
        val mixedRoutineItems = listOf(
            testRoutineItems[0], // Matches user criteria
            testRoutineItems[1], // Matches user criteria
            RoutineItem(
                id = "3",
                day = "Sunday",
                time = "08:30 AM - 10:00 AM",
                room = "606",
                courseCode = "CSE 4112",
                teacherInitial = "PQR",
                batch = "60", // Different batch
                section = "B", // Different section
                labSection = null,
                semester = "Summer 2025",
                department = "Software Engineering",
                effectiveFrom = "22-07-2025"
            )
        )

        coEvery { routineRepository.getRoutineForUser(testUser) } returns Result.success(mixedRoutineItems)

        // When
        val result = getUserRoutineUseCase(testUser)

        // Then
        assertTrue(result.isSuccess)
        val filteredItems = result.getOrNull()!!
        
        // Should only include items matching user's batch and section
        assertTrue(filteredItems.all { it.batch == testUser.batch && it.section == testUser.section })
        assertEquals(2, filteredItems.size)
    }
}
