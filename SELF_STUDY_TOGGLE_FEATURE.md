# Self Study Exam Routine Toggle Feature

## Overview

A toggle switch has been implemented in the Exam Routine screen that allows users to switch between viewing their batch-specific exam courses and Self Study courses that are available for all students.

## Feature Description

### What are Self Study Courses?
Self Study courses are exam courses that are available for all students regardless of their batch. These courses have their batch field set to "Self Study" (case insensitive).

### Toggle Functionality
- **Toggle OFF (Default)**: Shows the user's batch-specific exam courses only
- **Toggle ON**: Shows Self Study courses instead of batch-specific courses

## Implementation Details

### Domain Model Changes (`ExamRoutine.kt`)

1. **`getExamCoursesForUser(user: User)`**: Now returns only batch-specific courses (excluding Self Study)
2. **`getSelfStudyExamCourses()`**: New method that returns only Self Study courses
3. **`getCombinedExamCoursesForUser(user: User, includeSelfStudy: Boolean)`**: Returns combined courses based on the toggle state

### UI Components (`ExamRoutineContent.kt`)

#### New Components:
- **`SectionHeaderWithToggle`**: Section header with integrated toggle switch on the right
- **`SectionHeader`**: Standard section headers for other areas
- **`DateSection`**: Organized display of exams by date
- **`groupExamsByDate`**: Helper function for date-based organization

#### Updated Components:
- **`ExamRoutineContent`**: Now handles toggle state and switches between course types
- **`ImportantNoticeCard`**: Dynamic text based on current toggle state
- **Header card**: Updated note about the toggle switch functionality

## User Experience

### When Toggle is OFF (Default):
- Shows "Your Batch (X) Exams" header with toggle switch
- Displays only the user's batch-specific exam courses
- Helper text indicates Self Study courses are available via toggle

### When Toggle is ON:
- Shows "Self Study Courses (Available for Everyone)" header
- Displays only Self Study courses instead of batch courses  
- Helper text shows count of Self Study courses being displayed
- User can toggle back to see their batch courses

## Benefits

1. **Focused View**: Users see only relevant courses at a time (either their batch or Self Study)
2. **Clear Switching**: Toggle switch makes it easy to switch between course types
3. **Space Efficient**: No duplicate content or long scrolling through mixed courses
4. **Intuitive Design**: Toggle is placed right in the section header for easy access

## Technical Features

- **State Management**: Uses Compose state management for toggle persistence during session
- **Exclusive Display**: Only one type of course is shown at a time, reducing confusion
- **Performance**: Efficient filtering with remember and derivedStateOf
- **Smart Toggle**: Toggle only appears when Self Study courses are available
- **Dynamic Headers**: Section title changes based on current toggle state

## Usage

1. Open the Exam Routine screen
2. Look for the toggle switch next to "Your Batch (X) Exams" header
3. Toggle ON to switch to Self Study courses view
4. Toggle OFF to return to your batch courses
5. The toggle state persists during the session
6. If no Self Study courses exist, no toggle will be shown

## Future Enhancements

- Persist toggle state across app sessions using preferences
- Add animations for section transitions
- Include statistics about Self Study course participation
- Add filtering options within each section