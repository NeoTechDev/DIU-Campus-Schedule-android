# Class Routine Notification Feature

## Overview
This feature implements professional class routine notifications that alert users 30 minutes before each class starts. The implementation uses `AlarmManager.setExactAndAllowWhileIdle()` for reliable notifications that work even during Android's Doze mode.

## Components

### 1. ClassReminderAlarmManager
- **Location**: `core/reminder/ClassReminderAlarmManager.kt`
- **Purpose**: Professional alarm management using `setExactAndAllowWhileIdle()`
- **Features**:
  - Precise scheduling with Doze mode compatibility
  - Efficient alarm cancellation
  - Professional error handling and logging
  - Support for multiple reminder types

### 2. ClassReminderReceiver
- **Location**: `receiver/ClassReminderReceiver.kt`
- **Purpose**: Broadcast receiver for alarm triggers
- **Features**:
  - Rich notification design matching the provided image
  - Proper notification channel management
  - Intent handling for app navigation

### 3. ClassReminderService
- **Location**: `core/reminder/ClassReminderService.kt`
- **Purpose**: High-level service for managing class reminders
- **Features**:
  - Integration with user routine data
  - Course name enhancement
  - Weekly reminder scheduling
  - Automatic refresh on data changes

### 4. ClassReminderScheduler
- **Location**: `core/reminder/ClassReminderScheduler.kt`
- **Purpose**: Lifecycle-aware scheduler
- **Features**:
  - Automatic initialization
  - User authentication handling
  - App lifecycle integration
  - Professional resource management

## Integration

### TodayViewModel Integration
The `TodayViewModel` has been enhanced to:
- Initialize the reminder scheduler
- Schedule reminders when data is refreshed
- Handle cleanup on ViewModel destruction

### App-level Integration
The `App` class initializes the reminder scheduler at startup.

## Notification Design

The notifications are designed to match the provided image exactly:

```
📱 DIU Campus Schedule • in 4m
   Class Reminder: Object Oriented Design
   🕐 Time: 1:00 PM
   🏢 Room: 812
   👨‍🏫 Teacher: MIM
   📚 Batch: 41 | Section: J
```

## Features

1. **30-minute advance notifications** - Users receive notifications exactly 30 minutes before each class
2. **Doze mode compatibility** - Uses `setExactAndAllowWhileIdle()` for reliable delivery
3. **Rich notification content** - Includes course name, time, room, teacher, batch, and section
4. **Professional error handling** - Comprehensive logging and graceful error recovery
5. **Lifecycle awareness** - Automatically manages reminders based on user authentication
6. **Data integration** - Seamlessly integrates with existing routine data from TodayScreen

## Usage

The system works automatically:

1. **User Login**: Reminders are scheduled for the upcoming week
2. **Daily Usage**: Today's reminders are scheduled when user opens TodayScreen
3. **Data Refresh**: Reminders are updated when routine data changes
4. **User Logout**: All reminders are cancelled

## Permissions

Required permissions (already in AndroidManifest.xml):
- `SCHEDULE_EXACT_ALARM` - For exact alarm scheduling
- `USE_EXACT_ALARM` - For using exact alarms
- `POST_NOTIFICATIONS` - For showing notifications

## Testing

To test the notification system:

1. Log in with a user who has classes scheduled
2. The system will automatically schedule reminders
3. Wait for notification delivery (or use developer tools to trigger)
4. Verify notification content and navigation

## Technical Notes

- Uses unique request codes for each class to avoid conflicts
- Implements proper PendingIntent management
- Handles edge cases like past class times
- Provides comprehensive logging for debugging
- Follows Android's best practices for background work

## Future Enhancements

Potential improvements:
- Customizable reminder timing (5, 15, 30 minutes)
- Sound and vibration preferences
- Class cancellation notifications
- Integration with calendar apps
