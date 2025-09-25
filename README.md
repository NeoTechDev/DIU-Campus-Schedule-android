# 📘 DIU Campus Schedule

<div align="center">

![DIU Campus Schedule Banner](https://github.com/user-attachments/assets/ccd0afd9-facf-41a1-ab2e-83dd56e1ea72)

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
- **Find Course**: Find course details easily
- **Users Notifications**: Real-Time facebook styled notifications

| Today's Schedule | No Schedule | Calendar Navigation | Users Notifications | Find Course |
|------------------|-------------|---------------------|---------------------|-------------|
| ![Today](https://github.com/user-attachments/assets/a66923ea-4f5e-4d9e-bed2-2a7034802638) | ![No Schedule](https://github.com/user-attachments/assets/171451a3-edf9-4737-b66b-d4548cbc460d) | ![Calendar](https://github.com/user-attachments/assets/059cb25b-75f9-44bb-8907-30d9318f792c) | ![Users Notifications](https://github.com/user-attachments/assets/2e603d8f-b870-476c-802c-ce7e25888b51) | ![Find Course](https://github.com/user-attachments/assets/186b6c47-b911-4f5c-9313-4ab17c77a7d4) |

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
| ![Tasks](https://github.com/user-attachments/assets/348465d2-1552-407f-8b99-74c03f88f2c7) | ![Add Task](https://github.com/user-attachments/assets/be191b4a-30fb-4b63-87e6-cf98e7390f0b) |

</details>

<details>
<summary><h3>📝 Rich Note-Taking System</h3></summary>

- **Rich Text Editor**: Full-featured note creation and editing
- **Color Organization**: Customizable note colors for better categorization
- **Search Functionality**: Quickly find notes with powerful search
- **Offline-First Architecture**: Notes are saved locally and synced when online

| Notes Overview | Note Editor |
|---------------|-------------|
| ![Notes](https://github.com/user-attachments/assets/dcb90779-e2eb-4082-8cd3-331b32a76987) | ![Editor](https://github.com/user-attachments/assets/809db73a-713c-4807-89d3-dc449fcbe93c) |

</details>

<details>
<summary><h3>👤 Role-Based User Profiles</h3></summary>

- **Student & Teacher Profiles**: Distinct interfaces for different user types
- **Persistent Data**: Profile information remains after logout
- **Customizable Settings**: Personalized experience for each user
- **Secure Authentication**: Firebase Auth integration

| Welcome Screen | Sign In | Sign Up |
|---------------|---------------|---------------|
| ![Welcome](https://github.com/user-attachments/assets/ddb1bf3a-a9b4-455d-aa49-036337d71628) | ![Sign In](https://github.com/user-attachments/assets/6d19a0f4-ec47-4ba4-9134-7694fd13b329) | ![Sign Up](https://github.com/user-attachments/assets/425875de-1ac0-4a3e-b8f9-839e8e88f30e) |

| Users Profile | Navigation |
|----------------|-----------|
| ![Users Profile](https://github.com/user-attachments/assets/1b2ca0ce-a576-4fa2-bb9d-6448501f4c3b) | ![Nav](https://github.com/user-attachments/assets/1c06fd2d-7973-4d6a-a3db-5a378f35a865) |

</details>

<details>
<summary><h3>📄 Comprehensive Routine Management</h3></summary>

- **Weekly Class Schedule**: Complete semester timetable view
- **Real-time Updates**: Automatic sync with university schedule changes
- **Export Options**: Generate PDF documents or high-quality images
- **Sharing Capabilities**: Easy sharing with classmates and family
- **Filter & Search**: Find specific classes or time slots quickly

| Weekly Routine | Schedule Status | Export & Share | Empty Classrooms |
|---------------|----------------|------------|------------------|
| ![Routine](https://github.com/user-attachments/assets/9191bb06-6cc0-4923-a6f8-3a597ab874c0) | ![Up to Date](https://github.com/user-attachments/assets/2c440732-30df-43ef-8561-2103b162fd46) | ![Export](https://github.com/user-attachments/assets/2ceb4867-90a3-42f8-bbcd-7db6346a3386) | ![Empty Rooms](https://github.com/user-attachments/assets/2645100f-ecf2-4c84-ab96-6eabe644f9ff) |

</details>

<details>
<summary><h3>🔍 Faculty Information & Web Portals</h3></summary>

- **Faculty Directory**: Comprehensive database with contact information
- **Advanced Search**: Find faculty by name, department, or initials
- **Portal Integration**: Direct access to university web portals
- **WebView Implementation**: Seamless in-app browsing experience

| Faculty Search | Student Portal | Teacher Portal | BLC Portal | Hall Management |
|---------------|----------------|----------------|------------|-----------------|
| ![Faculty](https://github.com/user-attachments/assets/058d1ae3-7ede-436c-8309-cb15f8b3242b) | ![Student Portal](https://github.com/user-attachments/assets/6a94ce92-7683-4bd7-b04f-3b1e8bd1e31e) | ![Teacher Portal](https://github.com/user-attachments/assets/ac0b5517-cdf6-4aa5-8e21-187ed944e234) | ![BLC](https://github.com/user-attachments/assets/2506dc6d-9af0-4c99-8ab2-c0f46876f80e) | ![Hall Portal](https://github.com/user-attachments/assets/c1d497f8-a179-4f85-ae7f-89c05b608914) |

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
| ![Widget](https://github.com/user-attachments/assets/57b8fc07-ec0f-431f-a2bc-c33071147f98) |

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
<img src="https://github.com/user-attachments/assets/179d7df8-a527-491b-ba7e-789c1ad2e218" width="100px;" alt="Ismam Hasan Ovi"/><br />
<sub><b>Ismam Hasan Ovi</b></sub><br />
<sub>Lead Developer & Architect</sub><br />
<sub>BSc in Software Engineering</sub><br />
<sub>6th Semester, DIU</sub>
</td>
<td align="center">
<img src="https://github.com/user-attachments/assets/32a2016b-e7fe-49ac-987d-e047e533e586" width="100px;" alt="Maruf Rayhan"/><br />
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
