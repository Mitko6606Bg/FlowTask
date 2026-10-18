# FlowTask 🚀

An expressive, modern Android productivity app for task management, calendar scheduling, weekly routines & programs, projects, and rich markdown notes — crafted entirely with **Jetpack Compose** and **Material 3 Expressive**.

---

## ✨ Features

- **📋 Tasks & Subtasks**: Flexible task management with custom layers, priority levels, due dates/times, and subtasks.
- **📁 Projects & Folders**: Organize goals and ideas into hierarchical projects and nested folders with color-coded tags and item counts.
- **📝 Rich Notes**: Block-based markdown editor with real-time WYSIWYG formatting, interactive checklists, heading styles, bullet/numbered lists, quotes, dividers, and undo/redo history.
- **📅 Interactive Calendar & Timeline**: Monthly grid view with dynamic activity dots and period highlights, horizontal program filter pills, and a detailed day schedule timeline.
- **🔄 Programs & Weekly Routines**: Set up recurring weekly schedules and programs with customizable task visibility modes (everywhere as tasks or calendar-only).
- **📸 Instants & Media**: Dedicated photo albums and snapshot logs stored locally.
- **🎨 Material 3 Expressive Motion**: Fluid predictive back navigation on cards and full-screen pages, spring physics bounces (`expressiveBounce`), and elegant squircle card dialogs.
- **🌐 Localization**: Full Bulgarian and English language support.
- **📱 Home Screen Widgets**: Quick-access interactive widgets for daily schedules and programs.

---

## 🛠️ Tech Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 (Material Design 3 / M3 Expressive)
- **Language**: Kotlin 2.0+ (Coroutines & StateFlow)
- **Architecture**: MVVM with Repository Pattern & Clean Architecture separation
- **Persistence**: 
  - Room Database (SQLite v4) for local tasks, projects, folders, and notes
  - Encrypted / Type-safe SharedPreferences for user preferences and program routines
- **Navigation & Motion**: Predictive Back Handler (Android 14/15 `BackEventCompat`) with hardware-accelerated `Modifier.graphicsLayer`
- **Build System**: Gradle with Kotlin DSL (`build.gradle.kts`)

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Koala / Ladybug or newer
- Android SDK 35 (compileSdk 35, minSdk 26, targetSdk 35)
- JDK 17 or JDK 21

### Building the Project

Clone the repository and run:

```bash
# Compile debug Kotlin sources
./gradlew compileDebugKotlin

# Run unit tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug
```

The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
