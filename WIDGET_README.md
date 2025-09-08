# DIU Campus Schedule Widget

A professional Android widget implementation using Jetpack Glance that displays daily class schedules on the home screen.

## Features

- 📅 **Today's Class Schedule**: Displays current day's classes with time, course, and location
- 🕐 **Real-time Status**: Shows "Now" indicator for currently running classes
- 🎨 **Material 3 Design**: Beautiful light and dark theme support that adapts to system settings
- ⚡ **Auto-refresh**: Updates every 30 minutes and on system time changes
- 📱 **Responsive Layout**: Adapts to different widget sizes (minimum 4x3 cells)
- 🔗 **Deep Integration**: Uses existing TodayViewModel and repository architecture

## Architecture

### Core Components

1. **ClassScheduleWidgetProvider**: Main widget provider registered in AndroidManifest
2. **ClassScheduleWidget**: Glance-based widget content using Jetpack Compose for widgets
3. **WidgetDataRepository**: Dedicated repository for widget data management
4. **WidgetUpdateReceiver**: Handles automatic updates on system events
5. **WidgetConfigurationActivity**: Setup screen for widget configuration

### Data Flow

```
TodayViewModel → WidgetDataRepository → ClassScheduleWidget → User's Home Screen
```

### Key Files

- `ClassScheduleWidgetProvider.kt` - Widget provider entry point
- `ClassScheduleWidget.kt` - Main widget UI using Glance
- `WidgetDataRepository.kt` - Data management for widgets
- `WidgetUpdateReceiver.kt` - Automatic update handling
- `WidgetConfigurationActivity.kt` - Widget setup UI

## UI Design

The widget closely matches the main app's Today screen design:

### Header Section
- App title "Today's Classes"
- Current date (e.g., "MONDAY, SEP 08")
- Current time indicator in a rounded container

### Class Cards
- **Time Column**: Start and end times (e.g., "11:30 AM - 1:00 PM")
- **Course Info**: Course code, room number, and teacher initial
- **Live Indicator**: Pulsing dot for currently running classes
- **Color Coding**: Different background colors for active vs. upcoming classes

### States
- **Loading**: Shows loading message
- **No User**: Prompts to sign in
- **No Classes**: Friendly "No classes today" message
- **Classes List**: Up to 4 classes with overflow indicator

## Theme Support

### Light Theme
- Background: Pure white (#FFFFFF)
- Primary: DIU Blue (#1A56DB)
- Text: Dark gray (#1A1A1A)
- Cards: Light blue (#E1EFFE) for active, light gray (#F5F5F5) for inactive

### Dark Theme
- Background: Dark gray (#1A1A1A)
- Primary: Light blue (#6B9FFF)
- Text: White (#FFFFFF)
- Cards: Dark blue (#2D3748) for active, darker gray for inactive

## Setup Instructions

### 1. Dependencies Added
```kotlin
// Glance for App Widgets
implementation("androidx.glance:glance-appwidget:1.0.0")
implementation("androidx.glance:glance-material3:1.0.0")
```

### 2. Manifest Registration
```xml
<!-- Widget Provider -->
<receiver android:name=".widget.ClassScheduleWidgetProvider"
    android:exported="true"
    android:label="@string/app_name">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data android:name="android.appwidget.provider"
        android:resource="@xml/widget_info" />
</receiver>
```

### 3. Widget Configuration
- Widget size: Minimum 250dp x 180dp (4x3 cells)
- Update interval: 30 minutes
- Resize modes: Horizontal and vertical
- Configuration activity included

## Professional Implementation Details

### Caching Strategy
- Intelligent caching with 5-minute expiration
- Professional cache invalidation on data changes
- Memory-efficient storage of routine items

### Error Handling
- Graceful fallbacks for network issues
- User-friendly error messages
- Automatic retry mechanisms

### Performance Optimization
- Lazy loading of course names
- Efficient data synchronization
- Background thread processing

### Accessibility
- Screen reader support
- Clear content descriptions
- High contrast support

## Usage

1. **Adding Widget**: Long press on home screen → Widgets → DIU Campus Schedule
2. **Configuration**: Widget setup screen verifies user authentication
3. **Automatic Updates**: Widget refreshes every 30 minutes and on time changes
4. **Manual Refresh**: Integrated with app's refresh mechanisms

## Integration with Existing App

The widget seamlessly integrates with the existing app architecture:

- Uses the same `RoutineRepository` and `TodayViewModel`
- Shares authentication state via `GetCurrentUserUseCase`
- Leverages existing course name service
- Follows the same error handling patterns
- Maintains consistency with app's design system

## Troubleshooting

### Common Issues
1. **Widget not updating**: Check if user is signed in to the main app
2. **No classes showing**: Verify user has routine data in the app
3. **Configuration fails**: Ensure app permissions are granted

### Debug Information
- All widget operations are logged with "Widget" tags
- Check Android logs for detailed error information
- Widget state is maintained in WidgetDataRepository

## Future Enhancements

- [ ] Click actions to open specific classes in main app
- [ ] Multiple widget sizes (2x2, 4x2, etc.)
- [ ] Customizable update intervals
- [ ] Week view widget option
- [ ] Task integration display

---

This widget provides a professional, polished experience that enhances the DIU Campus Schedule app's functionality while maintaining consistency with the main application's design and architecture.
