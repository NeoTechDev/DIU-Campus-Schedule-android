# Break/Counselling Text Display Fix

## Problem Description

The Break/Counselling text display logic was using the **logged-in user's role** instead of the **filter context**, causing incorrect text to show in filtered results.

### Specific Issues:
1. **Student login + Filter by teacher**: Shows "Break" instead of "Counselling"
2. **Teacher login + Filter by batch-section**: Shows "Counselling" instead of "Break"

## Root Cause

In `RoutineScreen.kt` line 1110, the logic was:
```kotlin
text = if (currentUser?.role == UserRole.TEACHER) "Counselling" else "Break"
```

This always used the logged-in user's role, ignoring the filter context.

## Solution

### 1. Enhanced Function Signatures

Updated function signatures to pass filter information through the component hierarchy:

```kotlin
// RoutineContent now accepts currentFilter parameter
@Composable
private fun RoutineContent(
    // ... existing parameters ...
    currentUser: User?,
    currentFilter: RoutineFilter?, // NEW PARAMETER
    // ... rest of parameters ...
)

// TableRoutineView now accepts currentFilter parameter  
@Composable
private fun TableRoutineView(
    currentUser: com.om.diucampusschedule.domain.model.User?,
    currentFilter: RoutineFilter?, // NEW PARAMETER
    routineItems: List<RoutineItem>,
    allTimeSlots: List<String>,
    onCourseClick: (String) -> Unit = {}
)
```

### 2. New Helper Function

Added `getBreakCounsellingText()` function that determines text based on filter context:

```kotlin
private fun getBreakCounsellingText(
    currentUser: com.om.diucampusschedule.domain.model.User?,
    currentFilter: RoutineFilter?
): String {
    return when {
        // When filter is applied, determine text based on filter type
        currentFilter != null -> {
            when (currentFilter.type) {
                FilterType.TEACHER -> "Counselling" // Viewing teacher's schedule
                FilterType.STUDENT -> "Break" // Viewing student's schedule  
                FilterType.ROOM -> if (currentUser?.role == UserRole.TEACHER) "Counselling" else "Break"
            }
        }
        // No filter applied, use logged-in user's role
        else -> if (currentUser?.role == UserRole.TEACHER) "Counselling" else "Break"
    }
}
```

### 3. Updated Break/Counselling Logic

Replaced the hardcoded logic with the new helper function:

```kotlin
// OLD (line 1110):
text = if (currentUser?.role == UserRole.TEACHER) "Counselling" else "Break"

// NEW:
text = getBreakCounsellingText(currentUser, currentFilter)
```

## Logic Rules

The new logic follows these rules:

| Scenario | Filter Type | Text Displayed | Reasoning |
|----------|-------------|----------------|-----------|
| Student filters by teacher | `FilterType.TEACHER` | "Counselling" | Viewing teacher's perspective |
| Teacher filters by student | `FilterType.STUDENT` | "Break" | Viewing student's perspective |
| Any user filters by room | `FilterType.ROOM` | Based on user role | Room filter doesn't change perspective |
| No filter applied | `null` | Based on user role | Default behavior |

## Expected Results

After this fix:

1. **Student login + Filter by teacher**: ✅ Shows "Counselling" (correct)
2. **Teacher login + Filter by batch-section**: ✅ Shows "Break" (correct)
3. **No filter applied**: ✅ Shows based on logged-in user role (unchanged)
4. **Room filter**: ✅ Shows based on logged-in user role (unchanged)

## Files Modified

- `RoutineScreen.kt`: Updated function signatures and Break/Counselling logic

## Testing Recommendations

1. **Student user filtering by teacher**: Verify "Counselling" appears in break slots
2. **Teacher user filtering by student**: Verify "Break" appears in break slots  
3. **No filter scenarios**: Verify original behavior is preserved
4. **Room filter scenarios**: Verify user-role-based behavior is preserved