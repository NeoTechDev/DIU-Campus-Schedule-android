
# DIU Campus Schedule

**DIU Campus Schedule** is a modern **Android app built with Jetpack Compose** to help students and teachers manage their academic life efficiently. The app is offline-first, works with Firebase for authentication and storage, and provides personalized class routines, exam schedules, tasks, notes, empty rooms, faculty information, and notifications.


## **Project Overview**

This app is designed for students and teachers of DIU to manage their schedules and academic data.

### **User Flow**

1. **Sign Up / Sign In**

   * Users can register manually with **email & password** or via **Google Sign-In** using Firebase Authentication.
   * After authentication, users complete a registration form:

     * Profile picture
     * Name
     * Department (Software Engineering)
     * Role (Student / Teacher)
     * Batch (for student)
     * Section (for student)
     * Lab section (for student)
     * Initial (for teacher)
   * All registration data is stored on Firebase Firestore.

2. **Main Features**

   * **Today Screen**: Displays today’s classes and tasks.
   * **Routine Screen**: Shows full weekly class routine.
   * **Exam Routine Screen**: Displays mid-term and final exam schedules.
   * **Tasks & Notes**: Users can create, edit, delete, and sync tasks and notes with Firebase.
   * **Empty Rooms Screen**: Search available rooms in real-time.
   * **Faculty Info Screen**: View information about faculty members.
   * **Profile Screen**: Users can view and edit their profile. Updates are synced to Firebase.
   * **Notifications**: 30-minute reminders before classes, exams, or tasks. Users can toggle task reminders.
   * **Offline Mode**: App works offline using Room database and syncs with Firebase when online.

3. **Automatic Updates**

   * Any changes made by the developer to routines or exam schedules are pushed to users via Firebase and they receive notifications.

4. **Sign Out**

   * Users can safely sign out from the profile screen.

---

## **Tech Stack**

* **Platform:** Android
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose
* **Architecture:** Clean MVVM (ViewModel + UseCase + Repository + DataSources)
* **Backend:** Firebase Firestore (data storage), Firebase Authentication (sign-in/sign-up), Firebase Cloud Messaging (notifications)
* **Offline Storage:** Room Database
* **Dependency Injection:** Hilt
* **Async & Reactive:** Kotlin Coroutines + Flow
* **Image Loading:** Coil
* **Background Tasks:** WorkManager (for local notifications)
* **Date/Time Handling:** ThreeTenABP (optional, for time zones and reminders)

---

## **Recommended Libraries**

| Feature                  Library                               |
| -------------------------------------------------------------- |
| UI / Compose ----------- Jetpack Compose                       |
| Navigation ------------- AndroidX Navigation Compose           |
| Dependency Injection --- Hilt                                  |
| Authentication --------- Firebase Authentication               |
| Data Storage / Sync ---- Firebase Firestore, Room Database     |
| Image Loading ---------- Coil                                  |
| Notifications ---------- WorkManager, Firebase Cloud Messaging |
| Coroutines / Reactive -- Kotlin Coroutines, Flow               |
| Date & Time ------------ ThreeTenABP                           |

---

## **Project Structure**

```
com.om.diucampusschedule/
│
├── core/                  
│   ├── di/                  # Hilt modules
│   ├── auth/                # Firebase Auth helpers
│   ├── db/                  # Room DB, DAOs, Entities
│   ├── network/             # Firebase/Backend services
│   ├── util/                # Result wrappers, mappers, date-time helpers
│   ├── design/              # Theme, typography, colors, reusable UI
│   └── nav/                 # Navigation graph and routes
│
├── data/                    
│   ├── local/               # Room data sources
│   ├── remote/              # Firebase remote data sources
│   ├── repository/          # Repository implementations (merge local + remote)
│   └── model/               # DTOs / entities
│
├── domain/                  
│   ├── model/               # Domain models
│   ├── repository/          # Repository interfaces
│   └── usecase/             # Business logic / use cases
│
├── ui/                      
│   ├── navigation/          # AppNavGraph.kt, Destinations.kt
│   ├── screens/
│   │   ├── welcome/         # WelcomeScreen + ViewModel
│   │   ├── auth/            # SignInScreen, SignUpScreen, AuthViewModel
│   │   ├── today/           # TodayScreen + TodayViewModel
│   │   ├── routine/         # RoutineScreen + RoutineViewModel
│   │   ├── examroutine/     # ExamRoutineScreen + ExamRoutineViewModel
│   │   ├── tasks/           # TaskScreen + TaskViewModel
│   │   ├── notes/           # NotesScreen + NotesViewModel
│   │   ├── emptyrooms/      # EmptyRoomsScreen + EmptyRoomViewModel
│   │   ├── facultyinfo/     # FacultyInfoScreen + FacultyInfoViewModel
|   |   └── profile/         # ProfileScreen + ProfileViewModel
│   ├── components/          # Reusable Compose widgets
│   └── theme/               # App colors, shapes, typography
│
└── App.kt                   # Application class, entry point, setContent, NavHost
```

---

## **Project Flow**

1. Welcome Screen → Sign In / Sign Up
2. Registration Form → Profile Setup (stored in Firebase)
3. Main Screens:

   * Today → displays today’s schedule + tasks
   * Routine → weekly class routine
   * Exam Routine → mid-term and final exams
   * Tasks → CRUD with offline-first sync
   * Notes → offline-first notes sync
   * Empty Rooms → search availability
   * Faculty Info → browse faculty details
   * Profile → edit, update, sign out
4. Notifications → 30-minute reminders for classes, exams, tasks
5. Offline Mode → Room DB caches all data; sync occurs automatically when online