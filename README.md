# 📘 DIU Campus Schedule

<div align="center">

![DIU Campus Schedule Banner](https://github.com/user-attachments/assets/d326ad72-e9fc-4ffa-a621-6f4402ce1f19)

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com)
[![Material 3](https://img.shields.io/badge/Design-Material%203-purple.svg)](https://m3.material.io)
[![API Level](https://img.shields.io/badge/API%20Level-26+-red.svg)](https://developer.android.com/guide/topics/manifest/uses-sdk-element#ApiLevels)
[![Version](https://img.shields.io/badge/Version-5.3.0-blue.svg)](https://github.com/NeoTechDev/DIU-Campus-Schedule-android/releases)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**A comprehensive campus companion application for Daffodil International University (DIU) students and faculty, built with modern Android development practices and clean architecture.**

[📱 Download from Play Store](https://play.google.com/store/apps/details?id=com.om.diucampusschedule) • [📋 Features](#-key-features) • [🔧 Setup](#-development-setup) • [📖 Docs](#-documentation)

</div>

---

## 🎯 Overview

DIU Campus Schedule is a feature-rich Android application designed to streamline academic life for DIU students and faculty. Built with Jetpack Compose and following clean architecture principles, it provides an intuitive interface for managing class schedules, academic tasks, notes, and campus resources.

### 🌟 What Makes It Special

- **🔄 Real-time Synchronization**: Automatic sync with Firebase for seamless data consistency
- **📱 Home Screen Widget**: Quick access to today's schedule without opening the app  
- **🌙 Dark Theme Support**: Full Material 3 theming with system-wide dark mode
- **📄 Export Capabilities**: Generate and share PDF/image versions of schedules
- **🔔 Smart Notifications**: Contextual reminders for classes, exams, and tasks
- **🏢 Empty Room Finder**: Locate available classrooms in real-time
- **👥 Faculty Directory**: Comprehensive faculty information and contact details
- **🌐 Web Portal Integration**: Seamless access to university portals via WebView
- **📶 Offline Support**: Core functionality works without internet connection

---

## ✨ Key Features

<details>
<summary><h3>📅 Daily Schedule & Today View</h3></summary>

- **Today's Schedule**: Clean, organized view of daily classes with live status indicators
- **Calendar Navigation**: Quick date selection with bottom sheet interface
- **Quick Access Bar**: Direct shortcuts to frequently used features
- **Smart Notifications**: Automatic reminders 30 minutes before classes

| Today's Schedule | No Schedule | Calendar Navigation | Quick Access Bar |
|------------------|-------------|---------------------|------------------|
| ![Today](https://github.com/user-attachments/assets/d326ad72-e9fc-4ffa-a621-6f4402ce1f19) | ![No Schedule](https://github.com/user-attachments/assets/53ed35d8-c9e1-4bf7-abea-ce6516597f51) | ![Calendar](https://github.com/user-attachments/assets/ba0fe492-5137-4ab2-b13f-845a68dd4082) | ![Quick Access](https://github.com/user-attachments/assets/655e540f-b516-48d7-9f06-1f6535270526) |

</details>

<details>
<summary><h3>🔔 Smart Notification System</h3></summary>

- **Class Reminders**: Automatic notifications 30 minutes before classes
- **Task Alerts**: Custom reminder times for assignments and deadlines
- **Exam Notifications**: Special alerts for upcoming examinations
- **System Integration**: Native Android notification channels

| Class Notifications |
|---------------------|
| ![Notifications](https://github.com/user-attachments/assets/87a202c4-0e83-44ab-be0e-0705861ccb1e) |

</details>

<details>
<summary><h3>✅ Advanced Task Management</h3></summary>

- **Full CRUD Operations**: Create, read, update, and delete tasks
- **Priority System**: Color-coded priority levels for better organization
- **Custom Reminders**: Set specific notification times for each task
- **Offline Support**: Works seamlessly without internet connection
- **Cross-device Sync**: Tasks sync across all your devices

| Task Management | Add New Task |
|----------------|--------------|
| ![Tasks](https://github.com/user-attachments/assets/2b0d06b4-39e1-4539-8486-14b6591370ae) | ![Add Task](https://github.com/user-attachments/assets/981e149d-3bf8-4301-9462-65dc65a4ad43) |

</details>

<details>
<summary><h3>📝 Rich Note-Taking System</h3></summary>

- **Rich Text Editor**: Full-featured note creation and editing
- **Color Organization**: Customizable note colors for better categorization
- **Search Functionality**: Quickly find notes with powerful search
- **Offline-First Architecture**: Notes are saved locally and synced when online

| Notes Overview | Note Editor |
|---------------|-------------|
| ![Notes](https://github.com/user-attachments/assets/d3897bef-65d9-410b-8aa1-a60809deb2ed) | ![Editor](https://github.com/user-attachments/assets/56512dc1-91ce-4740-a053-27f234f81d33) |

</details>

<details>
<summary><h3>👤 Role-Based User Profiles</h3></summary>

- **Student & Teacher Profiles**: Distinct interfaces for different user types
- **Persistent Data**: Profile information remains after logout
- **Customizable Settings**: Personalized experience for each user
- **Secure Authentication**: Firebase Auth integration

| Welcome Screen | Student Login | Teacher Login |
|---------------|---------------|---------------|
| ![Welcome](https://github.com/user-attachments/assets/66c01792-5d32-401e-b3ab-3c37b862cf44) | ![Student Login](https://github.com/user-attachments/assets/eb12ed18-dce2-483e-8743-c25e75a2c771) | ![Teacher Login](https://github.com/user-attachments/assets/a1bb1d11-e902-4286-b10d-e7949263bfd1) |

| Student Profile | Teacher Profile |
|----------------|-----------------|
| ![Student Profile](https://github.com/user-attachments/assets/26df1b60-0f41-49ed-b2b1-6bd0ff8a98df) | ![Teacher Profile](https://github.com/user-attachments/assets/f58d6a61-fd8a-4e12-b63d-eb93b795c3fe) |

| Student Navigation | Teacher Navigation |
|-------------------|-------------------|
| ![Student Nav](https://github.com/user-attachments/assets/dd78a88c-281f-4f19-9a9c-d249b348b82a) | ![Teacher Nav](https://github.com/user-attachments/assets/a9d18729-3d84-4de2-8c1d-2dc207b5cdfb) |

</details>

<details>
<summary><h3>📄 Comprehensive Routine Management</h3></summary>

- **Weekly Class Schedule**: Complete semester timetable view
- **Real-time Updates**: Automatic sync with university schedule changes
- **Export Options**: Generate PDF documents or high-quality images
- **Sharing Capabilities**: Easy sharing with classmates and family
- **Filter & Search**: Find specific classes or time slots quickly

| Weekly Routine | Schedule Status | PDF Export | Empty Classrooms |
|---------------|----------------|------------|------------------|
| ![Routine](https://github.com/user-attachments/assets/74ecc015-2cf2-4b84-bf20-9055cbe87e48) | ![Up to Date](https://github.com/user-attachments/assets/aed8e728-8333-4216-a8e5-1fa917092997) | ![PDF Export](https://github.com/user-attachments/assets/5f02bc29-cb39-4f5e-8d9d-528135b45982) | ![Empty Rooms](https://github.com/user-attachments/assets/f2bb0ddd-ddd5-4122-af01-3077a5c1d3f3) |

</details>

<details>
<summary><h3>🔍 Faculty Information & Web Portals</h3></summary>

- **Faculty Directory**: Comprehensive database with contact information
- **Advanced Search**: Find faculty by name, department, or initials
- **Portal Integration**: Direct access to university web portals
- **WebView Implementation**: Seamless in-app browsing experience

| Faculty Search | Student Portal | Teacher Portal | BLC Portal | Hall Management |
|---------------|----------------|----------------|------------|-----------------|
| ![Faculty](https://github.com/user-attachments/assets/dec0dbac-8333-4d55-bd87-eeb3ef317eb5) | ![Student Portal](https://github.com/user-attachments/assets/6a94ce92-7683-4bd7-b04f-3b1e8bd1e31e) | ![Teacher Portal](https://github.com/user-attachments/assets/ac0b5517-cdf6-4aa5-8e21-187ed944e234) | ![BLC](https://github.com/user-attachments/assets/2506dc6d-9af0-4c99-8ab2-c0f46876f80e) | ![Hall Portal](https://github.com/user-attachments/assets/c1d497f8-a179-4f85-ae7f-89c05b608914) |

</details>

<details>
<summary><h3>📱 Home Screen Widget</h3></summary>

- **Live Class Information**: Always up-to-date schedule on your home screen
- **Intelligent Display**: Shows current class or next upcoming class
- **Theme Adaptive**: Automatically matches your system theme (light/dark)
- **Quick Launch**: Tap to open the full app for detailed information
- **Battery Efficient**: Optimized refresh cycles to preserve battery life

| Home Screen Widget |
|--------------------|
| ![Widget](https://github.com/user-attachments/assets/b5f06b50-6303-4c97-a0fb-802c4b4f5eb2) |

</details>

---

## 🏗️ Architecture & Tech Stack

### Core Architecture
- **Clean Architecture**: Clear separation of concerns with domain, data, and presentation layers
- **MVVM Pattern**: ViewModel-driven UI with reactive state management
- **Repository Pattern**: Centralized data access with local and remote sources
- **Dependency Injection**: Hilt for compile-time dependency resolution

### Technology Stack

| Category | Technologies |
|----------|-------------|
| **Language** | ![Kotlin](https://img.shields.io/badge/Kotlin-100%25-blue.svg) |
| **UI Framework** | Jetpack Compose, Material 3 Design System |
| **Architecture** | Clean Architecture, MVVM, Repository Pattern |
| **Dependency Injection** | Hilt |
| **Database** | Room (Local), Firebase Firestore (Remote) |
| **Networking** | Firebase SDK, Retrofit |
| **Authentication** | Firebase Auth |
| **Notifications** | Firebase Cloud Messaging, WorkManager |
| **Image Loading** | Coil |
| **Widgets** | Glance API |
| **Data Serialization** | Kotlinx Serialization, Gson |
| **Background Tasks** | WorkManager, AlarmManager |
| **Testing** | JUnit, Espresso |

### Project Structure
```
app/src/main/java/com/om/diucampusschedule/
├── core/                    # Core utilities and common components
│   ├── auth/               # Authentication helpers
│   ├── db/                 # Room database components
│   ├── design/             # Design system and theming
│   └── util/               # Utility functions and helpers
├── data/                   # Data layer
│   ├── local/              # Room data sources
│   ├── remote/             # Firebase data sources
│   ├── repository/         # Repository implementations
│   └── model/              # Data models and DTOs
├── domain/                 # Domain layer
│   ├── model/              # Domain models
│   ├── repository/         # Repository interfaces
│   └── usecase/            # Business logic use cases
├── ui/                     # Presentation layer
│   ├── screens/            # UI screens and ViewModels
│   ├── components/         # Reusable UI components
│   ├── navigation/         # Navigation graph
│   └── theme/              # App theming
└── widget/                 # Home screen widget implementation
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Hedgehog | 2023.1.1 or later
- **JDK**: 11 or higher
- **Android SDK**: API level 26-35
- **Gradle**: 8.0+
- **Firebase Project**: Required for backend services

### 🔧 Development Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/NeoTechDev/DIU-Campus-Schedule-android.git
   cd DIU-Campus-Schedule-android
   ```

2. **Firebase Configuration**
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Download `google-services.json` and place it in the `app/` directory
   - Enable Authentication, Firestore, and Cloud Messaging
   - See [Firebase Setup Guide](FIREBASE_SETUP_GUIDE.md) for detailed instructions

3. **Build and Run**
   ```bash
   ./gradlew build
   ./gradlew installDebug
   ```

### 📋 Configuration Files

- **`app/google-services.json`**: Firebase configuration (not included in repo)
- **`firestore.rules`**: Database security rules
- **`functions/`**: Cloud Functions for advanced features

---

## 📖 Documentation

| Document | Description |
|----------|-------------|
| [📋 Project Overview](project_overview.md) | Comprehensive project documentation and flow |
| [🔥 Firebase Setup Guide](FIREBASE_SETUP_GUIDE.md) | Step-by-step Firebase configuration |
| [🚀 Production Deployment](PRODUCTION_DEPLOYMENT_GUIDE.md) | Production build and release guide |
| [🧩 Widget Implementation](WIDGET_IMPLEMENTATION_STATUS.md) | Home screen widget documentation |
| [⚡ Developer Quick Start](DEVELOPER_QUICK_START.md) | Quick development environment setup |

---

## 🤝 Contributing

We welcome contributions from the community! Whether it's bug fixes, feature enhancements, or documentation improvements, your help is appreciated.

### How to Contribute

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add some amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Development Guidelines

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful commit messages
- Add unit tests for new features
- Update documentation as needed
- Ensure all existing tests pass

### Code Style
- **Architecture**: Follow Clean Architecture principles
- **UI**: Use Jetpack Compose best practices
- **Testing**: Write comprehensive unit and integration tests
- **Documentation**: Keep README and code comments updated

---

## 📊 Project Statistics

- **📁 Source Files**: 315+ files
- **💻 Lines of Code**: 15,000+ lines of Kotlin
- **🧩 Screens**: 13+ main application screens
- **📱 Min SDK**: API 26 (Android 8.0)
- **🎯 Target SDK**: API 35 (Android 15)
- **📦 App Size**: ~8MB download size
- **🌟 Features**: 25+ core features and capabilities

---

## 🐛 Known Issues & Limitations

- Some features may require specific DIU network access
- Widget refresh rate depends on system limitations and battery optimization
- PDF export quality may vary based on device capabilities
- Certain web portals may require university network access

For bug reports and feature requests, please use our [issue tracker](https://github.com/NeoTechDev/DIU-Campus-Schedule-android/issues).

---

## 🔮 Future Roadmap

- [ ] **Multi-language Support**: Bengali and English language options
- [ ] **Enhanced Dark Mode**: Improved theming and customization options
- [ ] **Campus Map Integration**: Interactive campus navigation system
- [ ] **Social Features**: Student collaboration and study group tools
- [ ] **Analytics Dashboard**: Academic progress tracking and insights
- [ ] **Voice Reminders**: Audio notifications for classes and tasks
- [ ] **Wear OS Support**: Companion app for Android smartwatches
- [ ] **Tablet Optimization**: Enhanced UI for larger screen devices

---

## 📞 Support & Contact

- **🐛 Bug Reports**: [GitHub Issues](https://github.com/NeoTechDev/DIU-Campus-Schedule-android/issues)
- **💬 Feature Requests**: [GitHub Discussions](https://github.com/NeoTechDev/DIU-Campus-Schedule-android/discussions)
- **📧 Email Support**: Contact the development team for urgent issues
- **📖 Documentation**: Comprehensive guides available in the repository

---

## 👥 Development Team

<table>
<tr>
<td align="center">
<img src="https://github.com/user-attachments/assets/default-avatar.png" width="100px;" alt="Ismam Hasan Ovi"/><br />
<sub><b>Ismam Hasan Ovi</b></sub><br />
<sub>Lead Developer & Architect</sub><br />
<sub>BSc in Software Engineering</sub><br />
<sub>6th Semester, DIU</sub>
</td>
<td align="center">
<img src="https://github.com/user-attachments/assets/default-avatar.png" width="100px;" alt="Maruf Rayhan"/><br />
<sub><b>Maruf Rayhan</b></sub><br />
<sub>Co-Developer & Designer</sub><br />
<sub>BSc in Software Engineering</sub><br />
<sub>6th Semester, DIU</sub>
</td>
</tr>
</table>

---

## 🏆 Acknowledgments

- **Daffodil International University** for inspiration and institutional support
- **Android Developer Community** for excellent documentation and resources
- **Firebase Team** for providing robust backend services
- **Material Design Team** for beautiful and intuitive design guidelines
- **Open Source Contributors** who made various libraries and tools possible
- **Beta Testers** from the DIU community for valuable feedback

---

## 📄 License

```
MIT License

Copyright (c) 2024 DIU Campus Schedule Development Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

<div align="center">

**⭐ Star this repository if you find it helpful!**

[![Download from Play Store](https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png)](https://play.google.com/store/apps/details?id=com.om.diucampusschedule)

*Made with ❤️ for the DIU community*

</div>