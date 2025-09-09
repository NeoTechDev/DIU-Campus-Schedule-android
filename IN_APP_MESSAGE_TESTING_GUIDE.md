# Firebase In-App Message System - Testing Guide

## Overview
Your Firebase in-app message system has been successfully implemented! This system allows you to show contextual messages to users as banners or dialogs.

## Features Implemented

### 1. **Core Components**
- ✅ `InAppMessage` data model with full serialization support
- ✅ `InAppMessageRepository` for Firebase Firestore integration
- ✅ `InAppMessageViewModel` with smart caching and network handling
- ✅ `InAppMessageBanner` - Non-intrusive banner messages
- ✅ `InAppMessageDialog` - Modal dialog messages
- ✅ `InAppMessageHandler` - Main orchestrator component

### 2. **UI Integration**
- ✅ Integrated into `MainScaffold` for all main app screens
- ✅ Screen-specific message targeting
- ✅ Smart animation and positioning
- ✅ Proper z-index layering for banners

### 3. **Network & Performance**
- ✅ Network connectivity checking before fetching
- ✅ Smart refresh logic (app resume, periodic checks)
- ✅ User-specific message filtering
- ✅ Message dismissal persistence

### 4. **Security**
- ✅ Updated Firestore rules for read-only message access
- ✅ Write permissions restricted to admin console only

## How to Test

### Step 1: Create Sample Messages
1. Open your app and navigate to the **Debug Screen** (if available in your navigation)
2. Look for the "📱 In-App Messages" section
3. Tap **"Create Sample Messages"** button
4. Wait for the success confirmation

### Step 2: Test Message Display
1. **Navigate to Today Screen** - You should see a welcome dialog
2. **Navigate to Routine Screen** - You should see a sync reminder banner
3. **Navigate to Tasks Screen** - You should see an exam preparation dialog
4. **Test different screens** - Some messages appear on all screens

### Step 3: Test Message Interactions
1. **Dismiss messages** - Tap the X button or action buttons
2. **Check persistence** - Dismissed messages shouldn't reappear
3. **Test navigation actions** - Some buttons navigate to different screens

### Step 4: Test Network Scenarios
1. **Offline testing** - Turn off internet, messages should still work from cache
2. **App resume** - Minimize and restore app, new messages should load
3. **Background refresh** - Messages refresh every 30 minutes when app is active

## Message Types

### Banner Messages
- Appear at the top of screens
- Non-intrusive, can be dismissed
- Slide in/out animations
- Support 1-3 action buttons

### Dialog Messages
- Modal overlays requiring user interaction
- Center-positioned with backdrop
- Support unlimited action buttons
- Can be made non-dismissible

## Message Targeting

### Screen Targeting
- `"today"` - Today/Dashboard screen only
- `"routine"` - Routine screen only  
- `"tasks"` - Tasks screen only
- `"notes"` - Notes screen only
- `"empty_rooms"` - Empty Rooms screen only
- `"profile"` - Profile screen only
- `"community"` - Community screen only
- `"faculty_info"` - Faculty Info screen only
- `""` (empty) - All screens

### User Targeting
- `showToNewUsers: true` - Show to all users
- `showToNewUsers: false` - Only show to existing users (before message creation)

### Time-based Targeting
- `expiresAt: 0` - Never expires
- `expiresAt: timestamp` - Expires at specific time

## Button Actions

### Built-in Actions
- `"dismiss"` - Simply dismiss the message
- `"navigate:today"` - Navigate to Today/Dashboard screen
- `"navigate:routine"` - Navigate to Routine screen
- `"navigate:tasks"` - Navigate to Tasks screen
- `"navigate:notes"` - Navigate to Notes screen
- `"navigate:empty_rooms"` - Navigate to Empty Rooms screen
- `"navigate:profile"` - Navigate to Profile screen
- `"navigate:community"` - Navigate to Community screen
- `"navigate:faculty_info"` - Navigate to Faculty Info screen
- `"url:https://..."` - Open external URL

### Custom Actions
You can add custom action handling in `InAppMessageHandler.kt` in the `handleButtonAction` function.

## Firebase Console Management

### Creating Messages Manually
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select your project
3. Navigate to Firestore Database
4. Go to `inAppMessages` collection
5. Create new document with structure:

```json
{
  "id": "unique_message_id",
  "title": "Message Title",
  "message": "Message content here",
  "type": "dialog", // or "banner"
  "isActive": true,
  "targetScreen": "today", // or "" for all screens
  "buttons": [
    {
      "text": "Action Text",
      "action": "dismiss" // or "navigate:screen" or "url:..."
    }
  ],
  "createdAt": 1641234567890,
  "expiresAt": 0, // or future timestamp
  "showToNewUsers": true
}
```

## Troubleshooting

### Messages Not Appearing
1. Check internet connection
2. Verify `isActive: true` in Firestore
3. Check if message was previously dismissed
4. Verify screen targeting matches current screen

### Performance Issues
1. Check message count in Firestore (should be reasonable)
2. Monitor network usage in debug logs
3. Verify smart refresh is working (not over-fetching)

### Reset for Testing
1. Use "Delete Sample Messages" in Debug Screen
2. Clear app data to reset dismissed messages
3. Or use: `InAppMessageViewModel.resetDismissedMessages(context)`

## Development Tips

### Adding New Screens
Update `targetScreen` values to match your navigation route names from `Screen.kt`.

### Custom Message Types
Extend `MessageType` and `DialogType` enums in the respective UI files.

### Analytics Integration
Add analytics calls in `handleButtonAction` to track message interactions.

### A/B Testing
Use different message IDs with same content but different targeting to test effectiveness.

## Sample Messages Created
The debug utility creates these sample messages:
- **Welcome Dialog** (today screen) - New user onboarding
- **App Update Banner** (all screens) - Feature announcement
- **Sync Reminder Banner** (routine screen) - Action reminder
- **Exam Tip Dialog** (tasks screen) - Helpful tip
- **Community Feature** (disabled) - Feature promotion

## Next Steps
1. Test all message scenarios
2. Create your own custom messages via Firebase Console
3. Monitor user engagement with message analytics
4. Adjust message timing and targeting based on user behavior

## Support
If you encounter any issues:
1. Check Android Studio logs for error messages
2. Verify Firebase project configuration
3. Test network connectivity
4. Check Firestore security rules

---
**Happy messaging! 🎉**
