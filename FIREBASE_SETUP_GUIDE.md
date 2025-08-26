# 🔥 Firebase Setup Guide for DIU Campus Schedule (Developer)

This guide will help you as a **developer** set up Firebase Firestore and upload your routine data from `routines.json` **without any admin role in the app**.

## 📋 Prerequisites

1. **Firebase Project**: You should already have a Firebase project set up
2. **Firestore Enabled**: Make sure Cloud Firestore is enabled in your Firebase console
3. **App Connected**: Your Android app should be connected to Firebase with `google-services.json`
4. **Developer Access**: You have direct access to Firebase console and can modify Firestore rules

## 🚀 Quick Setup Steps

### Step 1: Enable Firestore Database

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Click on **"Firestore Database"** in the left sidebar
4. Click **"Create database"**
5. Choose **"Start in test mode"** (you can configure security rules later)
6. Select your preferred location
7. Click **"Done"**

### Step 2: Configure Firestore Security Rules (Developer Mode)

**For Development** - In your Firebase console, go to Firestore → Rules and use this configuration:

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    // Allow read access to routines for authenticated users
    match /routines/{document} {
      allow read: if request.auth != null;
      allow write: if true; // Open for development - CHANGE IN PRODUCTION!
    }
    
    // Allow users to read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

**⚠️ Important**: The `allow write: if true;` rule is for development only. In production, you should restrict write access to routines.

**For Production** - Use these rules instead:

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    // Allow read access to routines for authenticated users
    match /routines/{document} {
      allow read: if request.auth != null;
      allow write: if false; // Only allow writes through Firebase Admin SDK or console
    }
    
    // Allow users to read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### Step 3: Upload Routine Data Using the Debug Screen

#### Option A: Using the Debug Screen (Recommended)

1. **Build and run your app**
2. **Navigate to debug screen** by adding `/debug` to your navigation or:
   - Add a temporary button in your app that navigates to `Screen.Debug.route`
   - Or modify your start destination temporarily to `Screen.Debug.route`

3. **In the Debug Screen:**
   - **🚀 RECOMMENDED**: Tap **"Quick Setup"** to upload data and set version in one step
   - OR manually: Tap **"Upload Routine Data"** then **"Update Version"**
   - Wait for the success message
   - Use **"List Routines"** to verify the upload

#### Option B: Using Code (Alternative)

Add this code to any screen or create a test function:

```kotlin
// In your Activity or Composable
val context = LocalContext.current
val scope = rememberCoroutineScope()

Button(
    onClick = {
        scope.launch {
            // Quick setup (recommended)
            val result = DeveloperRoutineUpload.quickSetup(context)
            result.fold(
                onSuccess = { message -> 
                    Log.d("Upload", "Success: $message")
                },
                onFailure = { error -> 
                    Log.e("Upload", "Error: ${error.message}")
                }
            )
        }
    }
) {
    Text("🚀 Quick Setup")
}
```

#### Option C: Direct Firebase Console Upload (Manual)

If the app method doesn't work, you can manually upload via Firebase Console:

1. Go to **Firestore Database** in Firebase Console
2. Click **"Start collection"**
3. Collection ID: `routines`
4. Document ID: Leave auto-generated
5. Add fields manually from your `routines.json`

> **Note**: This is tedious for large datasets. Use the app method instead.

## 📊 Firebase Data Structure

After upload, your Firestore will have this structure:

```
📁 routines (collection)
  📄 [auto-generated-id] (document)
    ├── 📝 semester: "Summer 2025"
    ├── 📝 department: "Software Engineering"  
    ├── 📝 effectiveFrom: "22-07-2025"
    ├── 📝 version: 1
    ├── 📝 createdAt: 1704067200000
    ├── 📝 updatedAt: 1704067200000
    └── 📋 schedule: [
         {
           day: "Saturday",
           time: "08:30 AM - 10:00 AM", 
           room: "611",
           courseCode: "SE214",
           teacherInitial: "MHS",
           batch: "42",
           section: "D"
         },
         // ... more routine items
       ]
```

## 🔄 How to Update Routine Data

### When You Make Changes to routines.json:

1. **Update the JSON file** with your changes
2. **Re-upload using Debug Screen:**
   - Go to Debug Screen
   - Tap **"Upload Routine Data"** (this will update existing data)
   - Tap **"Update Version"** to increment version number

3. **Notify Users:**
   - The version update will trigger push notifications
   - Users will automatically sync the new data

## 🔔 Push Notifications Setup

### For routine update notifications:

1. **Firebase Cloud Messaging** is already configured in your app
2. **Send notifications** when you update routines:

```kotlin
// Update version to trigger notifications
RoutineDataSetup.updateRoutineVersion("Software Engineering")
```

### Manual Push Notification (Optional):

You can send manual notifications from Firebase Console:
1. Go to **Cloud Messaging** in Firebase Console
2. Click **"Send your first message"**
3. Use this payload:

```json
{
  "to": "/topics/routine_updates",
  "data": {
    "type": "routine_update",
    "title": "Routine Updated",
    "body": "Your class schedule has been updated",
    "department": "Software Engineering"
  }
}
```

## 🛠️ Debugging & Troubleshooting

### Check if data was uploaded:

```kotlin
// Use this in your debug screen or logcat
RoutineDataSetup.listAllRoutines()
```

### Common Issues:

1. **"Permission denied" error:**
   - Check Firestore security rules
   - Make sure user is authenticated

2. **"Network error":**
   - Check internet connection
   - Verify Firebase configuration

3. **"No data found":**
   - Check if `routines.json` exists in assets folder
   - Verify JSON format is correct

### Verify Upload in Firebase Console:

1. Go to **Firestore Database** in Firebase Console
2. Look for **"routines"** collection
3. Check if documents exist with your data

## 📱 Testing the Complete Flow

1. **Upload routine data** using debug screen
2. **Create test users** with different batches/sections
3. **Login as different users** and check if they see their specific routines
4. **Update version** and verify push notifications work
5. **Test offline mode** by turning off internet

## 🎯 Production Checklist

Before going live:

- [ ] Upload routine data to Firebase
- [ ] Test with multiple user types (students/teachers)
- [ ] Configure proper Firestore security rules
- [ ] Remove or hide debug screen access
- [ ] Test push notifications
- [ ] Verify offline functionality
- [ ] Set up proper Firebase project for production

## 🔒 Security Notes

- **Never expose admin credentials** in your app
- **Use proper Firestore security rules** in production
- **Limit write access** to routine data to admins only
- **Test security rules** thoroughly before going live

---

## 🚀 Quick Start Command

To get started immediately, add this temporary button to any screen:

```kotlin
Button(
    onClick = {
        // Navigate to debug screen
        navController.navigate("debug")
    }
) {
    Text("🔧 Debug Panel")
}
```

Then use the debug panel to upload your routine data to Firebase! 🎉
