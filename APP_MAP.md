# FlowTask — App Architecture, Navigation Map & Style Guide

Welcome to the definitive map and style specification for **FlowTask**. This document serves as an exhaustive reference of the application's architecture, screen directory, component hierarchy, design philosophies, and the exact user preferences for visual styling and interactions.

---

## 1. Design Philosophy & User Style Preferences (What the User Likes)

### 🎨 Visual Language: Material 3 Expressive
- **Squircles & Rounded Radii**:
  - Main cards, dialogs, and sheets use rounded squircle shapes with **20dp to 28dp** corner radii.
  - Interactive chips and buttons use **12dp to 18dp** radii or fully rounded pills (`CircleShape`).
- **No Platform / Default Android Dialogs**:
  - **NEVER** use standard Android dialogs or basic `AlertDialog`.
  - Always design modal interactions as custom floating squircle cards or bottom sheets with `SurfaceContainerLowest`, subtle borders (`OutlineSubtle.copy(alpha = 0.5f)`), and gentle shadow elevation (4dp–8dp).
- **No "Container-in-Container" Clutter**:
  - Avoid nested visual shells (e.g. putting an inner card inside an outer card that already has background and border).
  - Content within cards must be flush, clean, and properly spaced with direct internal padding.
- **Symmetrical Alignment & Perfect Centering**:
  - Headers and selectors (like the `CollapsibleFiltersContainer`) must be strictly centered.
  - Symmetrical 3-part layout: Left icon (e.g. 28dp Box), Center content (`Modifier.weight(1f)` with `Alignment.Center`), and Right icon (e.g. 28dp chevron Box) so text never drifts off-center.
- **Dark Theme & Bright Slate Palette**:
  - FlowTask embraces a refined dark slate aesthetic:
    - Primary Background: `BgLight` (`0xFF0D1117` in dark mode)
    - Surface / Card: `SurfaceLight` (`0xFF161B22`), `SurfaceContainerLow`, `SurfaceContainerLowest`
    - Dynamic Accents: User-selectable accents (`LocalAccentColor.current`) with high-contrast text and icons.
- **M3 Segmented Button Rows (`SingleChoiceSegmentedButtonRow`)**:
  - Mutually exclusive discrete options use official Material 3 `SingleChoiceSegmentedButtonRow` and `SegmentedButton`.
  - Dynamic corner shaping via `SegmentedButtonDefaults.itemShape(index, count)`.
  - Seamless icon transitions (`SegmentedButtonDefaults.Icon(active = ...)`) and tonal container states with tactile `Modifier.expressiveBounce`.
- **M3 Filter Chips & Input Chips with Checkmarks (`FilterChip`, `InputChip`)**:
  - **Filter Chips**: Filter bars across the app (`FileFilterChip`, `LayerFilterBar`, `CalendarProgramFilterBar`, and `PillOption` in task edit sheets) use Material 3 `FilterChip`.
    - **Click & Long-Press Handling**: Standard tap selection passes `onClick` directly to `FilterChip(onClick = ...)`. When a chip supports long-press (such as layer items opening `LayerOptionsDialog`), it uses `Surface` with `combinedClickable(onClick = ..., onLongClick = ...)` to prevent gesture collision where empty click handlers consume touch down events.
    - **High-Contrast Text & Icon Guarantee**: Selected chips feature animated leading checkmarks (`Icons.Default.Check`, `tint = Color.White`), tonal container fills, and explicit `color = if (isSelected) Color.White else TextPrimary` text overrides. This prevents `TextStyle` defaults (like `bodyMedium`'s `textSecondary`) from blending into the selected background, ensuring >7:1 contrast in both light and dark modes with dynamic colors.
    - **Unselected Indicator Icons**: Preserved for non-selected chips (e.g. `Icons.Default.Layers` for All Layers, colored circular dots for Calendar Programs, and calendar glyphs for Date options).
  - **Input Chips**: User-added checklist items and subtasks use Material 3 `InputChip` with leading accent dot avatars, explicit `TextPrimary` label color, and trailing dismiss icons (`Icons.Default.Close`) for intuitive removal and standard M3 a11y semantics.
- **M3 Subtask Tri-State Checkboxes (`TriStateCheckbox` & `ToggleableState`)**:
  - Tasks containing subtasks dynamically display a 3-state checkbox on the parent card (`TaskCard`, `TaskDetailsBottomSheet`, `ProjectsScreen`):
    - **Unchecked (`ToggleableState.Off`)**: Empty circle/outline when 0 subtasks are completed and parent task is incomplete.
    - **Indeterminate (`ToggleableState.Indeterminate`)**: Tonal accent container with a centered white horizontal dash (`−`) when 1 or more subtasks are completed while the parent task itself is still pending.
    - **Checked (`ToggleableState.On`)**: Tonal accent container with an animated checkmark (`✓`) when the parent task is marked complete.
  - **Fluid State Transitions**: Spring-animated `AnimatedContent` morphing seamlessly between empty, dash, and checkmark.
  - **Independent Subtask Completion**: Toggling subtasks operates independently without forcibly mutating parent tasks, and full-row click targets (`10dp` squircle with generous touch slop) ensure immediate, reliable single-tap toggling.

### ⚡ Motion, Physics & Spring Dynamics
- **`Modifier.expressiveBounce`**:
  - Every interactive element (buttons, chips, cards, icons, list items) must include `Modifier.expressiveBounce(pressScale = 0.90f..0.96f)`.
- **Spring Physics**:
  - Use physics-based springs (`spring(dampingRatio = 0.75f..0.82f, stiffness = 380f..500f)`) rather than linear or tween animations for expand/collapse and pull-sheets.

### 🔄 Native Android Predictive Back Everywhere
- **Real-Time Touch Feedback**:
  - All hierarchical screens, sheets, subviews, drawers, and modal cards implement `PredictiveBackHandler`.
  - Real-time gesture tracking using `Flow<BackEventCompat>`:
    - Scale: `1.0f -> 0.92f` (`1f - (progress * 0.08f)`)
    - Translation: Directional along `backEvent.swipeEdge` (`progress * 28.dp.toPx()`)
    - Alpha fade: `1.0f -> 0.82f` (`1f - (progress * 0.18f)`)
- **Safe Submenu Navigation**:
  - Back gesture inside submenus/cards (e.g. routine editing, folder navigation, program create/edit) **must navigate back to the parent menu inside the card**, never exiting the card or dismissing the whole sheet abruptly.
  - Aborted gestures cleanly reset `progress = 0f` in `finally`, springing back into place.
  - Root screens set `enabled = false` to seamlessly delegate to Android's OS-level system predictive back (wallpaper peek or pager scroll).

---

## 2. Global Navigation & Floating Dock

FlowTask features a unified bottom navigation system driven by `MainPagerScreen` and `FloatingDockNavBar`:

### Horizontal Main Pager (`MainPagerScreen.kt`)
- Hosts 4 core screens in a swipeable `HorizontalPager` with predictive back returning to Page 0:
  - **Index 0**: Projects (`ProjectsScreen`)
  - **Index 1**: Today & Upcoming (`TodayUpcomingScreen`)
  - **Index 2**: All Tasks & Smart Calendar (`AllTasksCalendarScreen`)
  - **Index 3**: Instants Archive (`InstantsScreen`)
- Scrollable content on every screen reserves **100dp to 120dp bottom padding** to clear the floating dock.

### Material 3 Expressive Floating Dock (`FloatingDockNavBar.kt`)
- **Centered Pill Capsule**: Floating horizontally at the bottom of the screen.
- **Left Slot (Context-Aware)**:
  - **Projects Screen (Page 0)**: Dedicated Search button (`Icons.Default.Search`). Tapping transforms the dock into an M3 Expressive Search Bar floating directly above the soft keyboard.
  - **Today Screen (Page 1)**: Quick Camera photo snap launcher (`Icons.Default.PhotoCamera`).
  - **Calendar & Instants (Pages 2 & 3)**: Task checkbox navigation button (`Icons.Default.Today`, jumps directly to Today screen).
- **Dock-to-Search-Bar Morphing**: On the Projects screen, tapping Search expands the capsule symmetrically from the center with spring physics (66dp height, `dockWidth` animating smoothly from 186dp to full width) into a Google Search Homescreen Widget dual-capsule layout:
  - Inner search pill with 38dp accent search emblem, 16sp text field, and trailing clear button (`✕`).
  - Standalone 54dp circular close button (`⌵`) on the right.
  - **Material 3 Expressive Drag Handle**: Positioned directly above the search bar capsule (38dp x 4.5dp pill with 20dp touch target). Tapping the handle or swiping it down dismisses search.
  - **Swipe-Down-to-Dismiss Gesture**: Dragging down on the drag handle or search bar capsule moves the search bar downwards with 1:1 physical tracking and subtle alpha attenuation. Pulling past 36dp and releasing triggers a tactile haptic click, slides the search bar down into dismissal, and drops the soft keyboard; releasing before 36dp springs the search bar back up with snappy M3 spring physics.
  - Soft-keyboard tracking via `Modifier.imePadding()` with animated bottom padding (`24dp` down to `6dp`) placing the search bar snugly above the keyboard.
  - Zero shadow clipping glitches: replaced artificial drop shadows with clean M3 border strokes.
- **Center Slot**: Prominent nested capsule '+' button (triggers context-aware create sheet).
- **Right Slot**: Action hub button (toggles expandable navigation palette with 90° icon rotation).
- **Companion Dot Layer Selector**: Positioned cleanly **directly ABOVE** the dock, completely decoupled from the dock body (hidden automatically while search is active).
- **Predictive Back & Gestural Dismiss**: Expanded palette shrinks toward dock on back swipe (`isExpanded = false`); search bar collapses back to dock on back swipe, drag handle swipe, circular down-button click, or handle tap (`isSearchActive = false`).

---

## 3. Screen Directory & Component Map

### 📁 Screen 0: Projects (`ProjectsScreen.kt`)
- **Purpose**: Project management, pinned projects, nested folder hierarchies, project tasks with subtasks, and rich notes.
- **Key Views**:
  - `ProjectsDashboardView`:
    - **Minimal Distinguishable Cards (`ProjectCard`)**: Clean, modern cards without card descriptions or progress/indicator chips. Distinguishable at a glance via saturated color branding (4.5dp left vertical accent stripe in `projectColor`, tinted background `0.05f`, and colored border `0.28f`), 50dp squircle hero icon badge, prominent bold title, gold pin badge, and trailing action menu.
    - **Real-Time Reactive Search & Deep Folder Matching**: When search query is entered in the morphed dock search bar, matches immediately appear at the top prioritized by pin status, direct project matches, and nested subfolders. Matching subfolders render as an expanded hierarchy preview card (`📁 Подкаст > 📁 Епизод 1 > 📁 Аудио обработка`) with direct one-tap navigation into the subfolder.
  - `Hold-to-Rearrange ("pined seprete")`: Press-and-hold any project card to drag and rearrange. Features M3 Expressive tactile motion: scale spring (`1.035f`), drop shadow elevation (`14.dp`), glowing border tint (`0.85f`), `zIndex(10f)`, continuous tracking (`translationY`), zero-jump offset compensation math, and `Modifier.animateItem()` on surrounding cards. Pinned projects reorder strictly within "📌 Закачени" and unpinned projects reorder strictly within "Всички проекти", persisting new orders across restarts via `sortOrder`.
  - `ProjectDetailsBottomSheet`: M3 bottom sheet displaying the project's full description, 3 M3 stat tiles (Tasks completed/total, Folders count, Notes count), created & updated timeline dates, and full-width "Отвори проект" button. Access full options (Details, Pin/Unpin, Edit, Delete) via the 3-dot overflow menu.
  - `ProjectDetailView`: Hierarchical folder navigation stack (`folderNavigationStack`), breadcrumbs, task checklist with subtask chips, and upgraded project notes.
  - `ProjectNoteEditor` & Card-in-a-Card Note Upgrade:
    - **Borderless Title**: Frameless `BasicTextField` (bold 20sp/24sp, zero outline box or container shell).
    - **Card-in-a-Card Editor**: Nested content container (`Surface` with `RoundedCornerShape(20.dp)`, `SurfaceContainerLowest`, subtle border, full screen width).
    - **Fluid M3 Container Morphing**: Transitions seamlessly between a comfortable Card sheet (`cornerRadius = 26.dp`, `fillMaxHeight(0.85f)`, horizontal/bottom padding) and an immersive Full-Screen note canvas (`cornerRadius = 0.dp`, `fillMaxSize()`) using spring physics (`spring(dampingRatio = 0.82f, stiffness = 400f)`). Full-screen mode features breadcrumbs (`Project › Folder`), back navigation collapse, and header save checkmark.
    - **Floating Formatting Toolbar**: Anchored snugly directly above the soft keyboard via `Modifier.imePadding()` with horizontal scroll and tactile haptics. Supports Bold (`**`), Italic (`*`), H1 (`# `), H2 (`## `), Checklist (`- [ ] `), Bullet (`• `), Numbered (`1. `), Quote (`> `), Code (```), and Horizontal Divider (`---`).
    - **Rich Note Card Preview**: `ProjectNoteCard` displays structured markdown previews with interactive checklist indicators (`RadioButtonUnchecked`, `CheckCircle`), strikethrough for completed items, bullet chips, and zero hardware shadow clipping (`shadowElevation = 0.dp`).
- **Back Handling**:
  - `PredictiveBackHandler(enabled = selectedProject != null)`: Navigates up folder hierarchy or back to dashboard with directional scale and translation.
  - `BackHandler` in `ProjectNoteEditor`: Automatically collapses full-screen note view back to card sheet before dismissing.

### 📅 Screen 1: Today & Upcoming (`TodayUpcomingScreen.kt`)
- **Purpose**: Daily execution screen with coordinated dual-container viewport.
- **Key Views**:
  - **Top Container (Today)**: Dominant at rest. Contains today's tasks, completion counters, quick-add input.
  - **Bottom Container (Upcoming Spring Sheet)**: Pull-up sheet displaying upcoming days and future tasks.
  - `SpringSheetPhysics`: Smooth spring dragging between Today-dominant and Upcoming-dominant states.
- **Back Handling**:
  - `PredictiveBackHandler(enabled = focusProgress.value > BACK_HANDLER_THRESHOLD)`: Interactively collapses the Upcoming sheet back to Today with real-time finger tracking.

### 🗓️ Screen 2: All Tasks & Smart Calendar (`AllTasksCalendarScreen.kt`)
- **Purpose**: Comprehensive task oversight and interactive calendar view.
- **View Modes & Switcher**:
  - `SingleChoiceSegmentedButtonRow`: Material 3 native segmented control switching between `LIST` and `SMART_CALENDAR` with dynamic item shapes (`SegmentedButtonDefaults.itemShape(index, 2)`), tonal dock colors, and spring bounce.
  - `LIST`: Full task list with layer chips, filters, and subtask progress chips.
  - `SMART_CALENDAR`: Month-by-month calendar grid with activity dots, multi-day period lines, semester week numbers, and dynamic program color representation.
- **Smart Calendar Color Representation & Highlights**:
  - **Program Color Shades**: Days with active program routines display a nice shade of the program's color (`progColor.copy(alpha = 0.20f)` background, `progColor` text, program dot indicator).
  - **Task & Program Interference**: When normal tasks and program occurrences fall on the same date, their colors combine with the app's selected accent/bar color via `blendColors(progColor, barColor, 0.5f)` into a distinct hybrid hue (with dual side-by-side indicator dots).
  - **Dark Mode Date Legibility Brightening**: In dark mode (both standard dark theme and dynamic colors dark mode via `LocalDarkTheme.current`), calendar dates with tasks, programs, and interference blends dynamically boost color lightness to `0.78f` via `brightenColorForDark()`. This keeps date numbers crisp, vivid, and highly legible (>8:1 contrast) against dark surface cards while retaining their exact hue and color identity.
  - **Today vs Selected Highlights (Inverted)**:
    - **Today**: Displays solid fill (`barColor` background, white/contrast text). If today is also selected, adds a crisp high-contrast border ring (`dockColors.iconTint`).
    - **Selected Date**: Outlined with the **2.dp border-only highlight** (`Modifier.border(2.dp, barColor, DayCellShape)`); retains its date's background shade (program shade, interference shade, task shade, or transparent) without solid fill.
  - **Passed Months Filtering**: Passed months before the current month that have no active (uncompleted) tasks are automatically excluded from the calendar list via `monthHasActiveTasks()`, preventing empty past months from cluttering the view above current month.
- **Key Components**:
  - `CollapsibleFiltersContainer.kt`: Symmetrical header with `Tune` icon, centered layer/program filter badges, chevron, and swipe handle for collapsing/expanding.
  - `SmartCalendarView`: Lazy vertical column of monthly calendar grids.
  - `CalendarDateTasksBottomSheet.kt`: M3 modal sheet showing scheduled tasks and routine occurrences for a tapped date.
- **Back Handling**:
  - If filters expanded: `PredictiveBackHandler` collapses filters.
  - If in calendar mode: `PredictiveBackHandler` smoothly scales and transitions calendar back to `LIST` view.

### 📸 Screen 3: Instants (`InstantsScreen.kt`)
- **Purpose**: Visual capture and photographic task notes / memories.
- **Key Views**:
  - `Albums`: Root grid of instant photo folders.
  - `Folder`: Grid of captured snaps within a folder.
  - `PhotoDetail`: Full resolution snap view with metadata.
  - `FullScreenImageViewer.kt`: Full-screen pinch-to-zoom and pan viewer with interactive predictive back unzoom.
- **Back Handling**:
  - `PredictiveBackHandler`: `PhotoDetail -> Folder -> Albums` with real-time scale, translation, and alpha.

---

## 4. Overlays, Sheets & Modular Hubs

### 🎓 Programs & Routines Hub (`SemesterProgramBottomSheet.kt`)
- **Location**: Triggered from Settings or Calendar.
- **Architecture**:
  - `ProgramScreenMode.HUB`: List of all user-created programs with enable/disable toggles, color badges, and clean card styling.
  - `ProgramScreenMode.CREATE`: M3 squircle card with name, color, and optional date range.
  - `ProgramScreenMode.EDIT`: Rename, change color, update date range, and per-program task visibility toggle.
  - `ProgramScreenMode.DETAILS`: Detailed view containing `ProgramWeekCalendarView.kt`.
- **`ProgramWeekCalendarView.kt`**:
  - Displays top active week of current month highlighted, other days disabled.
  - Inline routine creation/editing without exiting the program view.
- **Predictive Back**: In-menu back handling (`CREATE -> HUB`, `EDIT -> DETAILS`, `DETAILS -> HUB`, routine cancel -> view) keeping the user inside the sheet until reaching root `HUB`.

### ⚙️ Settings Sheet (`SettingsBottomSheet.kt`)
- **Purpose**: Global preferences, theme customization, and data backup.
- **Key Controls**:
  - **Language Selector**: M3 `SingleChoiceSegmentedButtonRow` with Bulgarian (`Български`) and English (`English`), tonal selection, and animated checkmark.
  - **Theme Mode Selector**: M3 `SingleChoiceSegmentedButtonRow` with 3 discrete options: System (`Системен`), Light (`Светъл`), Dark (`Тъмен`), integrated icons, and checkmark morphing.
  - Accent color picker palette and Android 12+ Material You Dynamic Color toggle.
  - Semester / Period Settings: Show week numbers on calendar toggle, start date, and end date pickers.
  - JSON database backup and restore.

### 🏷️ Layer Editing & Management Bottom Card Sheet (`LayerOptionsDialog.kt`)
- **Trigger**: Long-press / long-hold on any layer pill or chip across the app (`LayerFilterBar`, `CollapsibleFiltersContainer`, `TikTokDotLayerSelector`, `EditTaskBottomSheet`).
- **Styling**: Material 3 Expressive bottom card sheet (`ModalBottomSheet` with `RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)`), `SurfaceContainerLowest`, M3 drag handle, and `imePadding()` to comfortably position above the software keyboard.
- **Save Name Styling**: Save button uses dynamic accent container with crisp white text (`Color.White`) and check icon, eliminating low-contrast black text.
- **Capabilities & Multi-System Synchronization**:
  - **Rename Layer**: In-place text field updating all assigned tasks in Room SQLite database via `UPDATE tasks SET layer = :newLayerName WHERE TRIM(layer) = TRIM(:oldLayerName) COLLATE NOCASE`.
  - **Full Program Sync**: Atomically synchronizes with `SemesterRepository.layerPrograms` and `semesterConfig`, updating any matching program's `layerName` so layers never disappear from any selector.
  - **Delete Layer**: Expandable confirmation offering two safe pathways:
    1. *Remove layer tag*: Keeps tasks intact and clears layer reference (`UPDATE tasks SET layer = NULL WHERE TRIM(layer) = TRIM(:layerName) COLLATE NOCASE`).
    2. *Delete tasks in layer*: Purges tasks belonging to the layer.
- **Predictive Back & Dismissal**: Seamless native drag down and predictive back dismiss.

### 📱 Home Screen Widgets (Google Stock / Pixel Aesthetic)
- **Today Tasks Widget (`TodayWidgetProvider.kt`)**:
  - Displays today's task checklist, completion toggle checkbox, task counter pill badge, and '+' quick-create launch button.
- **Program Routines Widget (`ProgramRoutinesWidgetProvider.kt`)**:
  - **Purpose**: Displays the next upcoming/ongoing routines for a user-selected Program in the signature style of Google stock apps (Pixel Weather / Calendar / Keep / At a Glance).
  - **Dynamic Colors & Homescreen Matching**:
    - On Android 12+ (API 31+), matches the phone's homescreen wallpaper Monet dynamic colors using `@color/widget_accent`, `@color/widget_accent_container`, `@color/widget_surface`, `@color/widget_background`, etc.
    - Fallback style for older Android versions (API < 31) uses the user's selected app style (`SettingsRepository.accentColor` and `themeMode`).
  - **Day Timeline Coverage Bar**:
    - Real-time indicator displaying up to 5 dots representing routine progression (`PAST` dimmed, `ACTIVE` accent, `FUTURE` outline).
    - Summary label showing total duration and percentage of active daytime (e.g. "3h 15m today (23%)", "Completed for today (3h)", or "Free day • No routines today").
  - **Featured Next Routine Focus Card**:
    - Featured routine displayed as the main focus in its own prominent 20dp squircle container (`widget_focus_routine_card` with `widget_focus_background`).
    - Large 16sp black title, distinct clean time/day header (without empty dark pills or badges).
    - Room / location with pin icon when available.
    - **Direct In-Program Navigation**: Tapping the widget (focus card, routines, or header) launches FlowTask and navigates directly into the program itself (`ProgramScreenMode.DETAILS` showing the program's weekly calendar view and routines), rather than stopping at the programs folder list. Pressing back smoothly returns to the programs list (`HUB`).
  - **Today's Other Routines Section**:
    - Strictly displays subsequent routines scheduled for the **current day only**.
    - If no more routines remain today (or next routine is tomorrow/later), the secondary list automatically collapses (`View.GONE`).
    - Displays up to 2 other routines from today with pill bars and an optional "+X more today" count indicator.
  - **Transparent Program Configuration Activity (`ProgramWidgetConfigureActivity.kt`)**:
    - Opens with a truly transparent activity window (`Theme.FlowTask.TransparentQuickCreate`, `updateWindowBackground = false`, `FLAG_DIM_BEHIND`) dimming over the homescreen.
    - Features Material 3 Expressive bottom card sheet with predictive back gesture physics, list of programs, routine counts, and single-tap selection.
    - Synchronized via `ProgramWidgetPrefs` (`appWidgetId` -> `programId`).
  - **Multi-System Auto Sync**:
    - Automatically refreshes widgets whenever programs or routines are added, edited, toggled, or deleted in `TaskViewModel`.

---

## 5. Data Architecture & Storage Layout

| Layer | Technology | Key Files | Purpose |
|---|---|---|---|
| **SQLite DB** | Room Database v4 | `AppDatabase.kt`, `TaskDao.kt`, `ProjectDao.kt`, `InstantDao.kt` | Persistent storage for tasks, subtasks, projects, folders, notes, and photos. |
| **Preferences** | Android SharedPreferences | `flowtask_semester` via `SemesterRepository.kt` | JSON storage for Layer Programs and weekly `ScheduleItem` routines. |
| **Preferences** | Android SharedPreferences | `flowtask_settings` via `SettingsRepository.kt` | User settings: Theme, accent color, semester dates, week numbers toggle, screen order. |
| **ViewModels** | Android Jetpack ViewModel | `TaskViewModel.kt`, `ProjectViewModel.kt`, `SettingsViewModel.kt`, `InstantViewModel.kt` | Unidirectional data flow via Kotlin Coroutines `StateFlow` and `SharedFlow`. |
| **Localization**| Kotlin Type-safe Strings | `AppStrings.kt` | Bulgarian (`BulgarianStrings`) and English (`EnglishStrings`). General domain terms (no academic jargon). |

---

## 6. Development & Build Rules

1. **Always Build Offline**: Always use `--offline` flag for Gradle:
   - `./gradlew testDebugUnitTest --offline`
   - `./gradlew assembleDebug --offline`
2. **Predictive Back Contract**:
   - Always track `BackEventCompat.progress` and `BackEventCompat.swipeEdge`.
   - Always reset progress state in a `finally` block to prevent visual stickiness on gesture cancel.
   - Disable (`enabled = false`) at the root view to allow native Android system navigation.
3. **No Container Shells**: Never nest a `Surface` or card inside another styled `Surface` unless strictly required for a separate floating overlay.
