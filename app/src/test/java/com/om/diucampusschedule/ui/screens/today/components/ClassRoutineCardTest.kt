package com.om.diucampusschedule.ui.screens.today.components

import com.om.diucampusschedule.domain.model.RoutineItem
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalTime

class ClassRoutineCardTest {

    @Test
    fun `RoutineItem toClassRoutine conversion should work correctly`() {
        // Given
        val routineItem = RoutineItem(
            id = "test_id",
            day = "Monday",
            time = "10:00 AM - 11:30 AM",
            room = "A-101",
            courseCode = "CSE420",
            teacherInitial = "ABC",
            batch = "55",
            section = "A",
            labSection = "A1",
            semester = "Fall 2024",
            department = "CSE"
        )

        // When
        val classRoutine = routineItem.toClassRoutine()

        // Then
        assertEquals("Monday", classRoutine.day)
        assertEquals("10:00 AM - 11:30 AM", classRoutine.time)
        assertEquals("A-101", classRoutine.room)
        assertEquals("CSE420", classRoutine.courseCode)
        assertEquals("ABC", classRoutine.teacherInitial)
        assertEquals("55", classRoutine.batch)
        assertEquals("A", classRoutine.section)
        assertEquals(LocalTime.of(10, 0), classRoutine.startTime)
        assertEquals(LocalTime.of(11, 30), classRoutine.endTime)
    }

    @Test
    fun `CourseUtils should cache and retrieve course names correctly`() {
        // Given
        val courseNames = mapOf(
            "CSE420" to "Software Engineering",
            "MAT101" to "Mathematics"
        )

        // When
        CourseUtils.setCourseNames(courseNames)

        // Then
        assertEquals("Software Engineering", CourseUtils.getCourseName("CSE420"))
        assertEquals("Mathematics", CourseUtils.getCourseName("MAT101"))
        assertNull(CourseUtils.getCourseName("UNKNOWN"))
    }

    @Test
    fun `ClassRoutine should handle null values correctly`() {
        // Given
        val classRoutine = ClassRoutine(
            day = "Monday",
            time = "10:00 AM - 11:30 AM",
            room = "A-101",
            courseCode = "CSE420",
            teacherInitial = null,
            batch = null,
            section = null,
            startTime = null,
            endTime = null
        )

        // Then
        assertNull(classRoutine.teacherInitial)
        assertNull(classRoutine.batch)
        assertNull(classRoutine.section)
        assertNull(classRoutine.startTime)
        assertNull(classRoutine.endTime)
    }
}