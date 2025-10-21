package com.om.diucampusschedule.domain.model

import org.junit.Test
import org.junit.Assert.*

class ExamRoutineSelfStudyTest {

    @Test
    fun `getExamCoursesForUser should only return user batch courses excluding Self Study`() {
        // Arrange
        val user = User(
            id = "test-user",
            name = "Test User",
            department = "Software Engineering",
            role = UserRole.STUDENT,
            batch = "55"
        )
        
        val examCourses = listOf(
            ExamCourse(
                code = "CSE101",
                name = "Programming Fundamentals",
                students = 50,
                batch = "55",
                slot = "A"
            ),
            ExamCourse(
                code = "STUDY101",
                name = "Study Skills Workshop",
                students = 200,
                batch = "Self Study",
                slot = "B"
            ),
            ExamCourse(
                code = "CSE102",
                name = "Data Structures",
                students = 40,
                batch = "56",
                slot = "C"
            )
        )
        
        val examDay = ExamDay(
            dayNumber = 1,
            date = "15/12/2024",
            weekday = "Sunday",
            courses = examCourses
        )
        
        val examRoutine = ExamRoutine(
            id = "test-routine",
            university = "DIU",
            department = "Software Engineering",
            examType = "Final",
            semester = "Fall 2024",
            startDate = "15/12/2024",
            endDate = "25/12/2024",
            message = "Final Exam Schedule",
            slots = mapOf(
                "A" to "9:00 AM - 12:00 PM",
                "B" to "1:00 PM - 4:00 PM",
                "C" to "5:00 PM - 8:00 PM"
            ),
            schedule = listOf(examDay)
        )
        
        // Act
        val userExamCourses = examRoutine.getExamCoursesForUser(user)
        
        // Assert
        assertEquals(1, userExamCourses.size)
        assertTrue("Should include user's batch course", 
                  userExamCourses.any { it.code == "CSE101" && it.batch == "55" })
        assertFalse("Should not include Self Study course", 
                   userExamCourses.any { it.code == "STUDY101" && it.batch == "Self Study" })
        assertFalse("Should not include other batch courses", 
                   userExamCourses.any { it.code == "CSE102" && it.batch == "56" })
    }
    
    @Test
    fun `getSelfStudyExamCourses should return only Self Study courses`() {
        // Arrange
        val examCourses = listOf(
            ExamCourse(
                code = "CSE101",
                name = "Programming Fundamentals",
                students = 50,
                batch = "55",
                slot = "A"
            ),
            ExamCourse(
                code = "STUDY101",
                name = "Study Skills Workshop",
                students = 200,
                batch = "Self Study",
                slot = "B"
            ),
            ExamCourse(
                code = "CSE102",
                name = "Data Structures",
                students = 40,
                batch = "56",
                slot = "C"
            )
        )
        
        val examDay = ExamDay(
            dayNumber = 1,
            date = "15/12/2024",
            weekday = "Sunday",
            courses = examCourses
        )
        
        val examRoutine = ExamRoutine(
            id = "test-routine",
            university = "DIU",
            department = "Software Engineering",
            examType = "Final",
            semester = "Fall 2024",
            startDate = "15/12/2024",
            endDate = "25/12/2024",
            message = "Final Exam Schedule",
            slots = mapOf(
                "A" to "9:00 AM - 12:00 PM",
                "B" to "1:00 PM - 4:00 PM",
                "C" to "5:00 PM - 8:00 PM"
            ),
            schedule = listOf(examDay)
        )
        
        // Act
        val selfStudyCourses = examRoutine.getSelfStudyExamCourses()
        
        // Assert
        assertEquals(1, selfStudyCourses.size)
        assertTrue("Should include Self Study course", 
                  selfStudyCourses.any { it.code == "STUDY101" && it.batch == "Self Study" })
        assertFalse("Should not include batch-specific courses", 
                   selfStudyCourses.any { it.code == "CSE101" })
    }
    
    @Test
    fun `getCombinedExamCoursesForUser should include both batch and Self Study when includeSelfStudy is true`() {
        // Arrange
        val user = User(
            id = "test-user",
            name = "Test User",
            department = "Software Engineering",
            role = UserRole.STUDENT,
            batch = "55"
        )
        
        val examCourses = listOf(
            ExamCourse(
                code = "CSE101",
                name = "Programming Fundamentals",
                students = 50,
                batch = "55",
                slot = "A"
            ),
            ExamCourse(
                code = "STUDY101",
                name = "Study Skills Workshop",
                students = 200,
                batch = "Self Study",
                slot = "B"
            ),
            ExamCourse(
                code = "CSE102",
                name = "Data Structures",
                students = 40,
                batch = "56",
                slot = "C"
            )
        )
        
        val examDay = ExamDay(
            dayNumber = 1,
            date = "15/12/2024",
            weekday = "Sunday",
            courses = examCourses
        )
        
        val examRoutine = ExamRoutine(
            id = "test-routine",
            university = "DIU",
            department = "Software Engineering",
            examType = "Final",
            semester = "Fall 2024",
            startDate = "15/12/2024",
            endDate = "25/12/2024",
            message = "Final Exam Schedule",
            slots = mapOf(
                "A" to "9:00 AM - 12:00 PM",
                "B" to "1:00 PM - 4:00 PM",
                "C" to "5:00 PM - 8:00 PM"
            ),
            schedule = listOf(examDay)
        )
        
        // Act
        val combinedCourses = examRoutine.getCombinedExamCoursesForUser(user, includeSelfStudy = true)
        val batchOnlyCourses = examRoutine.getCombinedExamCoursesForUser(user, includeSelfStudy = false)
        
        // Assert
        assertEquals(2, combinedCourses.size)
        assertEquals(1, batchOnlyCourses.size)
        
        assertTrue("Combined should include user's batch course", 
                  combinedCourses.any { it.code == "CSE101" && it.batch == "55" })
        assertTrue("Combined should include Self Study course", 
                  combinedCourses.any { it.code == "STUDY101" && it.batch == "Self Study" })
        
        assertTrue("Batch only should include user's batch course", 
                  batchOnlyCourses.any { it.code == "CSE101" && it.batch == "55" })
        assertFalse("Batch only should not include Self Study course", 
                   batchOnlyCourses.any { it.code == "STUDY101" && it.batch == "Self Study" })
    }
    
    @Test
    fun `getExamCoursesForUser should return only Self Study courses for user without batch`() {
        // Arrange
        val userWithoutBatch = User(
            id = "test-user-2",
            name = "Test User 2",
            department = "Software Engineering",
            role = UserRole.STUDENT,
            batch = "" // Empty batch
        )
        
        val examCourses = listOf(
            ExamCourse(
                code = "CSE101",
                name = "Programming Fundamentals",
                students = 50,
                batch = "55",
                slot = "A"
            ),
            ExamCourse(
                code = "STUDY101",
                name = "Study Skills Workshop",
                students = 200,
                batch = "Self Study",
                slot = "B"
            ),
            ExamCourse(
                code = "GENERAL001",
                name = "General Course",
                students = 100,
                batch = "", // Empty batch - should also be included
                slot = "C"
            )
        )
        
        val examDay = ExamDay(
            dayNumber = 1,
            date = "15/12/2024",
            weekday = "Sunday",
            courses = examCourses
        )
        
        val examRoutine = ExamRoutine(
            id = "test-routine",
            university = "DIU",
            department = "Software Engineering",
            examType = "Final",
            semester = "Fall 2024",
            startDate = "15/12/2024",
            endDate = "25/12/2024",
            message = "Final Exam Schedule",
            slots = mapOf(
                "A" to "9:00 AM - 12:00 PM",
                "B" to "1:00 PM - 4:00 PM",
                "C" to "5:00 PM - 8:00 PM"
            ),
            schedule = listOf(examDay)
        )
        
        // Act
        val userExamCourses = examRoutine.getExamCoursesForUser(userWithoutBatch)
        
        // Assert
        assertEquals(2, userExamCourses.size)
        assertTrue("Should include Self Study course", 
                  userExamCourses.any { it.code == "STUDY101" && it.batch == "Self Study" })
        assertTrue("Should include course with empty batch", 
                  userExamCourses.any { it.code == "GENERAL001" && it.batch == "" })
        assertFalse("Should not include specific batch courses", 
                   userExamCourses.any { it.code == "CSE101" && it.batch == "55" })
    }
    
    @Test
    fun `getExamCoursesForUser should handle case insensitive Self Study batch matching`() {
        // Arrange
        val user = User(
            id = "test-user",
            name = "Test User",
            department = "Software Engineering",
            role = UserRole.STUDENT,
            batch = "55"
        )
        
        val examCourses = listOf(
            ExamCourse(
                code = "STUDY101",
                name = "Study Skills Workshop 1",
                students = 200,
                batch = "SELF STUDY", // Uppercase
                slot = "A"
            ),
            ExamCourse(
                code = "STUDY102",
                name = "Study Skills Workshop 2",
                students = 200,
                batch = "self study", // Lowercase
                slot = "B"
            ),
            ExamCourse(
                code = "STUDY103",
                name = "Study Skills Workshop 3",
                students = 200,
                batch = "Self Study", // Mixed case
                slot = "C"
            )
        )
        
        val examDay = ExamDay(
            dayNumber = 1,
            date = "15/12/2024",
            weekday = "Sunday",
            courses = examCourses
        )
        
        val examRoutine = ExamRoutine(
            id = "test-routine",
            university = "DIU",
            department = "Software Engineering",
            examType = "Final",
            semester = "Fall 2024",
            startDate = "15/12/2024",
            endDate = "25/12/2024",
            message = "Final Exam Schedule",
            slots = mapOf(
                "A" to "9:00 AM - 12:00 PM",
                "B" to "1:00 PM - 4:00 PM",
                "C" to "5:00 PM - 8:00 PM"
            ),
            schedule = listOf(examDay)
        )
        
        // Act
        val userExamCourses = examRoutine.getExamCoursesForUser(user)
        
        // Assert
        assertEquals(3, userExamCourses.size)
        assertTrue("Should include uppercase Self Study course", 
                  userExamCourses.any { it.code == "STUDY101" })
        assertTrue("Should include lowercase Self Study course", 
                  userExamCourses.any { it.code == "STUDY102" })
        assertTrue("Should include mixed case Self Study course", 
                  userExamCourses.any { it.code == "STUDY103" })
    }
}