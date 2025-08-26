# 🚀 Production Deployment Guide

This comprehensive guide covers deploying the DIU Campus Schedule app to production with all necessary configurations.

## 📋 Pre-Deployment Checklist

### ✅ Code Quality & Testing
- [ ] All unit tests pass (`./gradlew test`)
- [ ] All instrumentation tests pass (`./gradlew connectedAndroidTest`)
- [ ] Code coverage > 80%
- [ ] Static analysis passes (lint, detekt)
- [ ] Security scan completed
- [ ] Performance profiling done

### ✅ Firebase Configuration
- [ ] Production Firebase project created
- [ ] Firestore security rules deployed
- [ ] Cloud Functions deployed
- [ ] Authentication providers configured
- [ ] FCM configured for push notifications
- [ ] Analytics and Crashlytics enabled

### ✅ App Configuration
- [ ] Build variants configured (debug/release)
- [ ] ProGuard/R8 rules configured
- [ ] App signing configured
- [ ] Version code and name updated
- [ ] Network security config set

---

## 🔥 Firebase Setup

### 1. Create Production Firebase Project

```bash
# Install Firebase CLI
npm install -g firebase-tools

# Login to Firebase
firebase login

# Create new project
firebase projects:create diu-campus-schedule-prod

# Initialize Firebase in your project
firebase init
```

### 2. Configure Firestore

```bash
# Deploy security rules
firebase deploy --only firestore:rules

# Create indexes (if needed)
firebase deploy --only firestore:indexes
```

**Production Firestore Rules** (`firestore.rules`):
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Production-ready rules with strict access control
    match /routines/{routineId} {
      allow read: if isAuthenticated() && 
                     belongsToSameDepartment(resource.data.department);
      allow write: if false; // Only Cloud Functions can write
    }
    
    match /users/{userId} {
      allow read, write: if isOwner(userId);
    }
    
    // Helper functions
    function isAuthenticated() {
      return request.auth != null;
    }
    
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    
    function belongsToSameDepartment(department) {
      return isAuthenticated() && 
             request.auth.token.department == department;
    }
  }
}
```

### 3. Deploy Cloud Functions

```bash
cd functions
npm install
npm run deploy
```

### 4. Configure Authentication

1. Go to Firebase Console → Authentication → Sign-in method
2. Enable Email/Password and Google sign-in
3. Add authorized domains for production
4. Configure OAuth consent screen

### 5. Set Up FCM

1. Go to Firebase Console → Cloud Messaging
2. Generate server key for backend
3. Configure APNs certificates (if supporting iOS later)

---

## 📱 Android App Configuration

### 1. Build Configuration

**`app/build.gradle.kts`**:
```kotlin
android {
    compileSdk 34
    
    defaultConfig {
        applicationId "com.om.diucampusschedule"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0.0"
        
        // Production configuration
        buildConfigField("String", "BASE_URL", "\"https://your-api.com/\"")
        buildConfigField("boolean", "DEBUG_MODE", "false")
    }
    
    signingConfigs {
        create("release") {
            storeFile = file("../keystore/release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            
            // Disable debugging
            isDebuggable = false
            isJniDebuggable = false
            renderscriptDebuggable = false
            
            // Enable R8 full mode
            android.enableR8.fullMode = true
        }
    }
}
```

### 2. ProGuard Rules

**`app/proguard-rules.pro`**:
```proguard
# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Gson
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep data classes
-keep class com.om.diucampusschedule.domain.model.** { *; }
-keep class com.om.diucampusschedule.data.model.** { *; }
```

### 3. Network Security Config

**`app/src/main/res/xml/network_security_config.xml`**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">your-api-domain.com</domain>
        <domain includeSubdomains="true">firebaseapp.com</domain>
        <domain includeSubdomains="true">googleapis.com</domain>
    </domain-config>
    
    <!-- Block all cleartext traffic in production -->
    <base-config cleartextTrafficPermitted="false" />
</network-security-config>
```

### 4. App Manifest Updates

**`app/src/main/AndroidManifest.xml`**:
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    android:allowBackup="false"
    android:extractNativeLibs="false"
    tools:targetApi="31">
    
    <!-- Remove debug-only components -->
    <!-- <activity android:name=".ui.screens.debug.DebugAccessScreen" /> -->
    
</application>
```

---

## 🔐 Security Configuration

### 1. App Signing

Generate production keystore:
```bash
keytool -genkey -v -keystore release.keystore -alias diu_campus_schedule -keyalg RSA -keysize 2048 -validity 10000
```

### 2. Environment Variables

Set up CI/CD environment variables:
```bash
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=diu_campus_schedule
KEY_PASSWORD=your_key_password
FIREBASE_PROJECT_ID=diu-campus-schedule-prod
```

### 3. API Security

- Enable Firebase App Check
- Set up API rate limiting
- Configure CORS for web clients
- Use Firebase Security Rules

---

## 🏗️ CI/CD Pipeline

### GitHub Actions Workflow

**`.github/workflows/deploy.yml`**:
```yaml
name: Deploy to Production

on:
  push:
    tags:
      - 'v*'

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run Tests
        run: ./gradlew test
      
      - name: Run Lint
        run: ./gradlew lint

  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Decode Keystore
        run: echo ${{ secrets.KEYSTORE_BASE64 }} | base64 -d > keystore/release.keystore
      
      - name: Build Release APK
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: ./gradlew assembleRelease
      
      - name: Build AAB
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: ./gradlew bundleRelease
      
      - name: Upload to Play Store
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.GOOGLE_PLAY_SERVICE_ACCOUNT }}
          packageName: com.om.diucampusschedule
          releaseFiles: app/build/outputs/bundle/release/app-release.aab
          track: production
```

---

## 📊 Monitoring & Analytics

### 1. Firebase Analytics Events

Key events to track:
- User sign-in/sign-up
- Routine views by department
- App crashes and errors
- Feature usage patterns
- Performance metrics

### 2. Crashlytics Setup

```kotlin
// In your Application class
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Enable Crashlytics in production only
        FirebaseCrashlytics.getInstance()
            .setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }
}
```

### 3. Performance Monitoring

```kotlin
// Add performance traces for critical operations
val trace = FirebasePerformance.getInstance().newTrace("routine_load")
trace.start()
// ... load routine
trace.stop()
```

---

## 🚀 Deployment Steps

### 1. Pre-Release Testing

```bash
# Run all tests
./gradlew test connectedAndroidTest

# Generate test coverage report
./gradlew jacocoTestReport

# Run security checks
./gradlew dependencyCheckAnalyze
```

### 2. Build Production APK/AAB

```bash
# Clean build
./gradlew clean

# Build release AAB for Play Store
./gradlew bundleRelease

# Build release APK for direct distribution
./gradlew assembleRelease
```

### 3. Deploy Firebase Components

```bash
# Deploy Cloud Functions
firebase deploy --only functions --project diu-campus-schedule-prod

# Deploy Firestore rules
firebase deploy --only firestore:rules --project diu-campus-schedule-prod

# Deploy hosting (if you have a web dashboard)
firebase deploy --only hosting --project diu-campus-schedule-prod
```

### 4. Upload to Google Play Store

1. Go to Google Play Console
2. Create new app listing
3. Upload AAB file
4. Fill in store listing details
5. Set up content rating
6. Configure pricing & distribution
7. Submit for review

---

## 📱 Post-Deployment

### 1. Monitor Key Metrics

- App crashes and ANRs
- User retention rates
- Feature adoption
- Performance metrics
- User feedback

### 2. Set Up Alerts

```javascript
// Cloud Function for monitoring
exports.monitorAppHealth = functions.pubsub
  .schedule('every 5 minutes')
  .onRun(async (context) => {
    // Check app health metrics
    // Send alerts if issues detected
  });
```

### 3. User Support

- Set up in-app feedback system
- Create user documentation
- Monitor app store reviews
- Prepare update rollout plan

---

## 🔄 Update Strategy

### 1. Version Management

```kotlin
// Semantic versioning
versionName = "1.0.0" // MAJOR.MINOR.PATCH
versionCode = 1       // Increment for each release
```

### 2. Staged Rollout

1. Internal testing (5-10 users)
2. Closed testing (50-100 users)
3. Open testing (1000+ users)
4. Production (gradual rollout: 1% → 5% → 20% → 50% → 100%)

### 3. Rollback Plan

- Keep previous APK ready
- Monitor crash rates for 24-48 hours
- Have hotfix process ready
- Prepare rollback to previous version if needed

---

## ⚠️ Important Notes

### Security Considerations
- Remove all debug screens and development tools
- Disable logging in production builds
- Use certificate pinning for API calls
- Implement root detection if needed
- Enable Firebase App Check

### Performance Optimization
- Enable R8 code shrinking
- Optimize images and resources
- Use vector drawables where possible
- Implement lazy loading
- Monitor memory usage

### Legal Compliance
- Add privacy policy
- Implement GDPR compliance
- Add terms of service
- Handle user data deletion requests
- Ensure proper consent flows

---

## 📞 Support & Maintenance

### Monitoring Dashboard
- Firebase Console for analytics
- Play Console for app metrics
- Crashlytics for crash reports
- Custom monitoring for business metrics

### Regular Maintenance Tasks
- Update dependencies monthly
- Security patches as needed
- Performance optimization quarterly
- User feedback review weekly
- Backup verification monthly

---

## 🎉 Launch Checklist

Final checks before going live:

- [ ] All tests passing
- [ ] Security audit completed
- [ ] Performance benchmarks met
- [ ] User acceptance testing done
- [ ] Marketing materials ready
- [ ] Support documentation complete
- [ ] Monitoring alerts configured
- [ ] Rollback plan tested
- [ ] Team trained on production procedures
- [ ] Legal requirements met

**🚀 Ready for Production Launch!**
