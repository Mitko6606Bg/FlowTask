# Project: FlowTask Layer Programs Overhaul

## Architecture
FlowTask is an Android task and schedule management app built with Jetpack Compose, Kotlin Coroutines/Flow, Material 3 Expressive, Room SQLite Database (v4), and SharedPreferences for preferences and layer programs.

### Module & Package Boundaries
- **Data Layer (`com.flowtask.app.data`)**:
  - `local/model/`: `LayerProgram.kt`, `SemesterModel.kt` (routine items, schedules), `TaskEntity.kt` (Room SQLite)
  - `repository/`: `SemesterRepository.kt` (JSON in `flowtask_semester` SharedPreferences), `SettingsRepository.kt` (`flowtask_settings` SharedPreferences), `TaskRepository.kt` (Room SQLite `tasks` table)
- **UI / Presentation Layer (`com.flowtask.app.ui`)**:
  - `viewmodel/`: `TaskViewModel.kt`, `SettingsViewModel.kt`
  - `screens/`: `AllTasksCalendarScreen.kt`, `SemesterProgramBottomSheet.kt`, `SettingsBottomSheet.kt`, `MainPagerScreen.kt`
  - `components/`: `CalendarDateTasksBottomSheet.kt`, `CalendarDayCell.kt`, `NewInstantFolderDialog.kt`
  - `theme/`: `Theme.kt` (`AppShapes`, `Modifier.expressiveBounce`, `ExpressiveMotion`)
- **Utility / Localization (`com.flowtask.app.util`)**:
  - `AppStrings.kt`: Type-safe localization (`BulgarianStrings`, `EnglishStrings`)
  - `SemesterUtils.kt`: Date and week calculation, routine task generation engine
  - `DateTimeUtils.kt`: Midnight timestamp normalization, date formatting

### Data Flow
1. User defines programs (`LayerProgram`) and weekly routines (`ScheduleItem`) in `SemesterProgramBottomSheet`. Stored as JSON in `flowtask_semester` SharedPreferences via `SemesterRepository`.
2. Visibility Mode (Settings):
   - **Mode A (Everywhere - default)**: `SemesterUtils.generateProgramTasks` synthesizes `TaskEntity` objects into SQLite `tasks` table via `TaskRepository.syncProgramTasks`. Tasks appear across Today, Upcoming, All Tasks, and Home Widgets.
   - **Mode B (Calendar Only)**: `TaskRepository.deleteTasksByLayer` purges program task rows from SQLite. Programs remain intact in SharedPreferences.
3. Calendar Projection:
   - `AllTasksCalendarScreen` collects `layerPrograms` and `selectedCalendarProgramId`.
   - Grid indicator dots (`combinedDatesWithActivity`) and period highlights (`combinedPeriodDates`) are dynamically computed from active programs and regular tasks.
   - Horizontal pill selector (All, Specific Program, None) dynamically filters grid markers and the `CalendarDateTasksBottomSheet` day schedule timeline.

---

## Feature Inventory

| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | F1_STRINGS_GENERALIZATION | Universal Terminology & Domain Generalization in `AppStrings.kt` (BG and EN strings), removing all occurrences of semester, university, faculty, classes, lectures, exams, professors across all UI strings, placeholders, and descriptions. Adding new string resources for program pill selector and program task visibility settings. | M1 | survey |
| 2 | F2_MODELS_REPOSITORIES_GENERALIZATION | Domain generalization in data models and repositories (`LayerProgram`, `ScheduleItem`, `SemesterModel`, `SemesterRepository`, `SemesterUtils`). Retain SharedPreferences `"flowtask_semester"` and backward-compatible fields (`subject`, `roomOrNote`) while exposing generalized aliases (`title`, `locationOrNote`, routines & events). | M1 | survey |
| 3 | F3_ICONOGRAPHY_GENERALIZATION | Replace academic icon `Icons.Default.School` across all screens (`AllTasksCalendarScreen.kt`, `SettingsBottomSheet.kt`, `SemesterProgramBottomSheet.kt`) with generalized routine/calendar icons (`Icons.Default.EventRepeat`). | M1 | survey |
| 4 | F4_CALENDAR_PROGRAM_DISPLAY_FIX | Fix calendar indicator dots and period highlights rendering bug in `AllTasksCalendarScreen.kt` and `CalendarDateTasksBottomSheet.kt` by collecting `layerPrograms` from ViewModel and dynamically calculating `periodDates` and `taskDatesWithActivity` from active programs' date ranges and schedule items. | M3 | survey |
| 5 | F5_CALENDAR_PILL_SELECTOR_BAR | Add dedicated, horizontal pill-shaped selector bar directly below the month/year header and above the calendar grid in `AllTasksCalendarScreen.kt`. Filter options: specific program, all enabled programs, and "None" (view only regular tasks). Reactive real-time updates to calendar grid indicators and day schedule timeline. | M3 | survey |
| 6 | F6_DAY_SCHEDULE_TIMELINE_FILTER_DEDUP | Update `CalendarDateTasksBottomSheet.kt` to respect the selected calendar program filter (All, Specific, None) and eliminate duplicate items between Room tasks and routine schedule items. | M3 | survey |
| 7 | F7_SETTINGS_VISIBILITY_CONFIG | Add configurable program task visibility setting in `SettingsRepository` (`flowtask_settings` SharedPreferences) and `SettingsViewModel`: Mode A (Everywhere as tasks - default) vs Mode B (Calendar only). | M2 | survey |
| 8 | F8_SETTINGS_UI_TOGGLE_CARD | Add M3 Expressive toggle card in `SettingsBottomSheet.kt` (in generalized Programs & Routines section) allowing users to switch between Mode A and Mode B with clear descriptions. | M2 | survey |
| 9 | F9_TASK_SYNC_CLEAR_DB_ENGINE | In `TaskViewModel` / `TaskRepository`, implement atomic sync / clear logic triggered on visibility mode change: Mode B purges program tasks from SQLite (`deleteTasksByLayer`) with zero data loss to SharedPreferences programs; Mode A regenerates tasks via `SemesterUtils.generateProgramTasks` + `syncProgramTasks`. | M2 | survey |
| 10 | F10_M3_EXPRESSIVE_PROGRAM_SCREEN | Redesign Program Management screen (`SemesterProgramBottomSheet.kt`) into a clean, elegant M3 Expressive interface with fluid page/tab transitions, intuitive controls for creating, editing, reordering, and deleting programs and routines. | M4 | survey |
| 11 | F11_CUSTOM_SQUIRCLE_CARD_DIALOGS | Replace all platform default popups and dialogs (`androidx.compose.material3.AlertDialog`) with custom floating card sheets and rounded squircle dialogs (28dp corner radii, `SurfaceContainerLowest`, 6dp elevation, border styling like `NewInstantFolderDialog`). | M4 | survey |
| 12 | F12_SPRING_PHYSICS_AND_DOCK_PADDING | Ensure buttons, cards, and interactive chips utilize `Modifier.expressiveBounce` with spring physics across the program screens and dialogs, and ensure scroll containers maintain proper bottom content padding (100dp-120dp) clearing the floating dock navigation bar. | M4 | survey |
| 13 | F13_UNIT_TESTS_PROGRAM_SWITCHING_AND_VISIBILITY | Author comprehensive unit tests covering: program visibility settings (Mode A vs Mode B sync/clear operations, SharedPreferences persistence), program selector pill filtering and date/schedule mapping, and generalized domain/semester utils regression testing. | M5 | survey |
| 14 | F14_OFFLINE_BUILD_AND_TEST_VERIFICATION | Full offline Gradle verification: `compileDebugKotlin --offline`, `testDebugUnitTest --offline`, and `assembleDebug --offline`. | M5 | survey |

---

## Milestones

| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | M1: Universal Domain & Terminology Generalization | R1: `AppStrings.kt`, `SemesterModel.kt`, `LayerProgram.kt`, `SemesterRepository.kt`, `SemesterUtils.kt`, iconography updates across all screens | none | DONE |
| 2 | M2: Configurable Program Task Visibility in Settings & DB Sync Engine | R3: `SettingsRepository.kt`, `SettingsViewModel.kt`, `SettingsBottomSheet.kt`, `TaskViewModel.kt`, `TaskRepository.kt` | M1 | IN_PROGRESS |
| 3 | M3: Calendar Program Display & Dedicated Pill Selector Bar | R2: `AllTasksCalendarScreen.kt`, `CalendarDateTasksBottomSheet.kt`, `CalendarDayCell.kt`, `TaskViewModel.kt` (calendar program filter state) | M1, M2 | PLANNED |
| 4 | M4: Redesigned M3 Expressive Program Screen & Card Dialogs | R4: `SemesterProgramBottomSheet.kt`, custom squircle card dialogs (28dp), routine create/edit/reorder, spring physics, dock padding | M1 | PLANNED |
| 5 | M5: E2E Unit Testing, Hardening & Final Offline Build Verification | All acceptance criteria: author unit tests for visibility and pill selector, regression tests, and verify `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug` all passing `--offline` | M1, M2, M3, M4 | PLANNED |

---

## Interface Contracts

### M1 ↔ M2, M3, M4
- `AppStrings.kt`:
  - `Strings` interface provides all generalized labels (`semesterSettingsTitle` -> Programs & Routines, `programFilterAll`, `programFilterNone`, `programVisibilityTitle`, `programVisibilityEverywhere`, `programVisibilityCalendarOnly`, `newClassTitle` -> New Routine, `editClassTitle` -> Edit Routine, `subjectLabel` -> Routine Title / Activity, `roomOrNoteLabel` -> Location / Organizer / Note).
  - Both `BulgarianStrings` and `EnglishStrings` implement these without any academic terms ("semester", "university", "faculty", "classes", "lectures", "exams", "professors").
- `ScheduleItem`:
  - Retains `subject: String` and `roomOrNote: String?` for SharedPreferences serialization backward compatibility.
  - Exposes `val title: String get() = subject` and `val locationOrNote: String? get() = roomOrNote`.
- `Icons`:
  - `Icons.Default.EventRepeat` replaces `Icons.Default.School`.

### M2 ↔ M3
- `SettingsRepository`:
  - `val programTaskVisibility: StateFlow<Boolean>` (or enum `ProgramTaskVisibility.EVERYWHERE` vs `CALENDAR_ONLY`).
  - `fun setProgramTaskVisibility(everywhere: Boolean)`
- `TaskViewModel`:
  - `fun setProgramTasksEverywhere(everywhere: Boolean)`: When toggled to `false` (Calendar only), calls `repository.deleteTasksByLayer(layerName)` for all enabled programs. When toggled to `true` (Everywhere), calls `SemesterUtils.generateProgramTasks` + `repository.syncProgramTasks(layerName, tasks)`.

### M3 ↔ Calendar UI
- `TaskViewModel`:
  - `val selectedCalendarProgramId: StateFlow<String?>` (null = All Programs, "NONE" = No program routines, or specific `program.id`).
  - `fun selectCalendarProgram(programId: String?)`
- `AllTasksCalendarScreen`:
  - Dedicated horizontal pill selector bar below month/year header.
  - Dynamically computes `combinedDatesWithActivity` and `combinedPeriodDates` from `allTasks` and `filteredPrograms`.
  - Passes `filteredPrograms` to `CalendarDateTasksBottomSheet` to filter the day schedule timeline in real time.

---

## Code Layout
- `app/src/main/java/com/flowtask/app/util/AppStrings.kt`: String tokens & localizations
- `app/src/main/java/com/flowtask/app/data/local/model/`: Data models (`LayerProgram.kt`, `SemesterModel.kt`)
- `app/src/main/java/com/flowtask/app/data/repository/`: Storage & business logic (`SemesterRepository.kt`, `SettingsRepository.kt`, `TaskRepository.kt`)
- `app/src/main/java/com/flowtask/app/ui/viewmodel/`: State holders (`TaskViewModel.kt`, `SettingsViewModel.kt`)
- `app/src/main/java/com/flowtask/app/ui/screens/`: Composable screens (`AllTasksCalendarScreen.kt`, `SettingsBottomSheet.kt`, `SemesterProgramBottomSheet.kt`, `MainPagerScreen.kt`)
- `app/src/main/java/com/flowtask/app/ui/components/`: Sub-composables (`CalendarDateTasksBottomSheet.kt`, `CalendarDayCell.kt`, dialogs)
- `app/src/test/java/com/flowtask/app/`: Unit tests (`SemesterUtilsTest.kt`, `ProgramVisibilitySettingsTest.kt`, `ProgramSelectorPillTest.kt`)
