# Implementation Plan - Gym Tracker App

The goal is to implement a comprehensive gym tracking application based on the requirements in `README.md`. The app will feature workout tracking, exercise management, body weight tracking, progress visualization, and an intelligent recommendation system.

## User Review Required

> [!IMPORTANT]
> The current project is set up with **Jetpack Compose**, while the `README.md` mentions XML and ViewBinding. I will proceed with **Jetpack Compose** as it is more modern and already integrated into the project structure.

## Proposed Changes

### 1. Project Configuration
- [MODIFY] [libs.versions.toml](file:///C:/Users/olkov/AndroidStudioProjects/GymTracker/gradle/libs.versions.toml): Add Room, Navigation, and Chart library (e.g., Vico or MPAndroidChart) dependencies.
- [MODIFY] [build.gradle.kts](file:///C:/Users/olkov/AndroidStudioProjects/GymTracker/app/build.gradle.kts): Apply plugins and add dependencies.

### 2. Data Layer
- [NEW] `data/model`: Entities for `Exercise`, `Workout`, `WorkoutExercise`, `Set`, and `BodyWeight`.
- [NEW] `data/dao`: DAOs for Room database operations.
- [NEW] `data/GymDatabase.kt`: Room database configuration with initial data population (predefined exercises).
- [NEW] `data/repository`: Repository implementation.

### 3. Domain Layer
- [NEW] `domain/model`: Domain models (if needed, otherwise use entities for simplicity).
- [NEW] `domain/repository`: Repository interface.
- [NEW] `domain/usecase`: Use cases for recommendation logic and statistics calculation.

### 4. UI Layer (Jetpack Compose)
- [NEW] `ui/navigation`: Navigation setup (Bottom Bar, Screens).
- [NEW] `ui/screens/workout`: Workout list and detail screens.
- [NEW] `ui/screens/exercise`: Exercise selection and management.
- [NEW] `ui/screens/weight`: Body weight tracking.
- [NEW] `ui/screens/stats`: Progress charts.
- [MODIFY] `MainActivity.kt`: Set up NavHost and MainScaffold.

## Verification Plan

### Automated Tests
- Unit tests for `RecommendationService` logic.
- Room database integration tests.

### Manual Verification
- Deploy to device/emulator.
- Create a workout, add exercises (standard and custom).
- Verify recommendation weight appears.
- Add body weight entries.
- Check charts for data visualization.
