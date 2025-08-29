# Screen Padding and Window Insets Guide

This guide explains how to properly handle screen padding and window insets in the DIU Campus Schedule app to avoid padding issues and ensure consistent UI across different screens.

## Problem Statement

Different screen types need different padding strategies:
- **Screens with TopAppBar**: Should not add their own status bar padding (prevents double padding)
- **Screens without TopAppBar**: Need to handle status bar insets themselves
- **MainScaffold**: Manages bottom navigation but removes default window insets

## Solution: ScreenConfig Utility

Use the `ScreenConfig` utility class to ensure consistent padding behavior.

### For Screens WITH TopAppBar

```kotlin
import com.om.diucampusschedule.ui.utils.ScreenConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScreenWithTopBar(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .run { ScreenConfig.run { withTopAppBar() } } // ← Use this
    ) {
        TopAppBar(
            title = { Text("My Screen") },
            windowInsets = ScreenConfig.getTopAppBarWindowInsets(handleStatusBar = true), // ← And this
            // ... other TopAppBar properties
        )
        
        // Your screen content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Content here
        }
    }
}
```

### For Screens WITHOUT TopAppBar

```kotlin
import com.om.diucampusschedule.ui.utils.ScreenConfig

@Composable
fun MyFullScreenContent(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .run { ScreenConfig.run { withoutTopAppBar() } } // ← Use this for status bar padding
            .padding(16.dp)
    ) {
        // Your screen content - no TopAppBar
        Text("Full screen content")
        // ... rest of content
    }
}
```

### Alternative Using Convenience Methods

You can also use the convenience methods from `ScreenConfig.Modifiers`:

```kotlin
import com.om.diucampusschedule.ui.utils.ScreenConfig.Modifiers.mainAppScreen
import com.om.diucampusschedule.ui.utils.ScreenConfig.Modifiers.fullScreen

// For screens with TopAppBar
modifier = Modifier
    .fillMaxSize()
    .background(MaterialTheme.colorScheme.background)
    .mainAppScreen() // ← Equivalent to withTopAppBar()

// For screens without TopAppBar  
modifier = Modifier
    .fillMaxSize()
    .background(MaterialTheme.colorScheme.background)
    .fullScreen() // ← Equivalent to withoutTopAppBar()
```

## Examples from Codebase

### ✅ Correct: TodayScreen (with TopAppBar)
```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .run { ScreenConfig.run { withTopAppBar() } }
) {
    CustomTopAppBar(
        // ...
        windowInsets = ScreenConfig.getTopAppBarWindowInsets(handleStatusBar = true)
    )
    // Content...
}
```

### ✅ Correct: ProfileScreen (without TopAppBar)
```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .run { ScreenConfig.run { withoutTopAppBar() } }
        .padding(16.dp)
) {
    // Full screen content...
}
```

### ❌ Incorrect: Manual status bar padding with TopAppBar
```kotlin
// DON'T DO THIS - causes double padding and cropped content
Column(
    modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.statusBars) // ← Wrong!
) {
    TopAppBar(/*...*/) // ← TopAppBar already handles status bar
    // Content...
}
```

## Migration Guide

If you have existing screens with padding issues:

1. **Identify the screen type**: Does it have a TopAppBar or not?

2. **Remove manual window insets**: Remove any `.windowInsetsPadding(WindowInsets.statusBars)`

3. **Add ScreenConfig**: Use the appropriate modifier from ScreenConfig

4. **Update TopAppBar**: If present, use `ScreenConfig.getTopAppBarWindowInsets()`

## Key Benefits

- **Consistent padding**: All screens follow the same pattern
- **No double padding**: Prevents TopAppBar + manual padding conflicts  
- **Better maintainability**: Central configuration for all screen types
- **Future-proof**: Easy to update padding behavior across all screens
- **Cross-device compatibility**: Handles different screen sizes and orientations

## Testing

When implementing these changes:

1. Test on different device sizes
2. Test in both portrait and landscape
3. Test with system UI (status bar/navigation bar) visible/hidden
4. Verify text is not cropped (especially multi-line text like "Active now")
5. Check that touch targets are properly accessible
