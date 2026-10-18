package com.flowtask.app.util

import androidx.compose.runtime.compositionLocalOf

interface Strings {

    // App & Brand
    val appName: String
    val appSubtitle: String

    // Navigation & Tabs
    val navProjects: String
    val navToday: String
    val navAllTasks: String
    val navTitle: String
    val navSubtitle: String

    // Today & Upcoming Screen
    val todayHeader: String
    val todayRemainingCount: (Int) -> String
    val todayEmptyTitle: String
    val todayEmptySubtitle: String
    val upcomingHeader: String
    val upcomingMoreCount: (Int) -> String
    val upcomingDrawerSubtitle: (Int) -> String
    val upcomingEmpty: String

    // Projects Screen
    val projectsHeader: String
    val projectsSubtitle: String
    val projectsEmptyTitle: String
    val projectsEmptySubtitle: String
    val foldersSection: (Int) -> String
    val foldersEmpty: String
    val tasksSection: (Int) -> String
    val tasksEmpty: String
    val notesSection: (Int) -> String
    val notesEmpty: String
    val newProjectTitle: String
    val projectNameLabel: String
    val projectNamePlaceholder: String
    val projectDescLabel: String
    val projectDescPlaceholder: String
    val useAppColorLabel: String
    val projectColorLabel: String
    val projectIconLabel: String
    val createProjectButton: String
    val newFolderTitle: String
    val folderNamePlaceholder: String
    val newNoteTitle: String
    val editNoteTitle: String
    val noteTitlePlaceholder: String
    val noteContentPlaceholder: String
    val saveNoteButton: String
    val expandNoteAction: String
    val collapseNoteAction: String
    val formatBold: String
    val formatItalic: String
    val formatHeading1: String
    val formatHeading2: String
    val formatChecklist: String
    val formatBulletList: String
    val formatNumberedList: String
    val formatQuote: String
    val formatCode: String
    val formatDivider: String
    val formatStrikethrough: String
    val formatHeading3: String
    val formatUndo: String
    val formatRedo: String
    val formatDismissKeyboard: String
    val pinnedProjectsSection: (Int) -> String
    val allProjectsSection: (Int) -> String
    val pinProjectAction: String
    val unpinProjectAction: String
    val projectPinnedBadge: String
    val openProjectAction: String
    val projectDetailsTitle: String
    val projectNoDescription: String
    val projectStatsTasks: String
    val projectStatsFolders: String
    val projectStatsNotes: String
    val projectTimelineCreated: String
    val projectTimelineUpdated: String
    val pinProjectToggleLabel: String
    val useAppColorToggleLabel: String
    val useAppColorToggleSubtitle: String
    val searchProjectsPlaceholder: String
    val searchResultsHeader: (Int) -> String
    val searchNoResultsTitle: String
    val searchNoResultsSubtitle: String
    val matchingFoldersSubtitle: (Int) -> String
    val openFolderDirectly: String

    // All Tasks & Calendar Screen
    val allTasksHeader: String
    val allTasksActiveCount: (Int) -> String
    val searchPlaceholder: String
    val activeTasksSection: (Int) -> String
    val completedTasksSection: (Int) -> String
    val noTasksFound: String
    val tasksForDateTitle: (String, Int) -> String
    val noTasksForDate: String

    // Task Edit / Add Sheet
    val newTaskTitle: String
    val editTaskTitle: String
    val taskExplanationPlaceholder: String
    val subtasksHeader: (Int) -> String
    val addSubtaskPlaceholder: String
    val dateOptional: String
    val timeOptional: String
    val layerSectionTitle: String
    val noLayer: String
    val allLayers: String
    val addLayerPrompt: String
    val newLayerPlaceholder: String
    val customLayer: String
    val editLayerTitle: String
    val layerNameLabel: String
    val renameLayerAction: String
    val deleteLayerAction: String
    val deleteLayerConfirmTitle: (String) -> String
    val deleteLayerSubtitle: (Int) -> String
    val deleteLayerKeepTasks: String
    val deleteLayerDeleteTasks: String
    val saveTaskButton: String
    val taskDetailsTitle: String
    val statusActive: String
    val statusCompleted: String
    val taskScheduleTitle: String
    val noTimeSet: String
    val viewDetailsAction: String

    // General Actions & Menus
    val addAction: String
    val saveAction: String
    val cancelAction: String
    val deleteAction: String
    val editAction: String
    val openAction: String
    val closeAction: String
    val createNewAction: String
    val deleteFolderTitle: String
    val deleteProjectTitle: String
    val itemsCount: (Int) -> String
    val folderEmptyStateTitle: String
    val folderEmptyStateSubtitle: String
    val moveToFolderAction: String
    val moveToFolderTitle: String
    val projectRootFolder: String
    val filterAll: String
    val filterFolders: String
    val filterTasks: String
    val filterNotes: String
    val addFolderChip: String
    val addTaskChip: String
    val addNoteChip: String
    val renameFolderAction: String
    val editProjectAction: String
    val updateAction: String

    // Settings Screen
    val settingsTitle: String
    val languageSection: String
    val languageBulgarian: String
    val languageEnglish: String
    val appearanceSection: String
    val darkModeTitle: String
    val darkModeDesc: String
    val darkModeSystem: String
    val darkModeLight: String
    val darkModeDark: String
    val materialYouToggle: String
    val materialYouDesc: String
    val amoledToggle: String
    val amoledDesc: String
    val dynamicStylesTitle: String
    val dynamicStylesDesc: String
    val dynamicStyleBalanced: String
    val dynamicStyleVibrant: String
    val dynamicStyleMuted: String
    val dynamicStyleHighContrast: String
    val customAccentColors: String
    val screenOrderSection: String
    val screenOrderDesc: String
    val moveUp: String
    val moveDown: String
    val resetOrder: String
    val versionInfo: String

    // Programs & Routines
    val semesterSettingsTitle: String
    val semesterSettingsDesc: String
    val semesterSheetTitle: String
    val semesterTabDates: String
    val semesterTabSchedule: String
    val semesterEnableSwitch: String
    val semesterEnableDesc: String
    val semesterStartDateLabel: String
    val semesterEndDateLabel: String
    val semesterNotSet: String
    val semesterTotalWeeksCount: (Int) -> String
    val semesterCurrentWeekLabel: (Int) -> String
    val semesterOutsideRange: String
    val semesterWeekShort: (Int) -> String
    val semesterWeekHeader: (Int) -> String
    val semesterProgramLayerLabel: String
    val addClassButton: String
    val editClassTitle: String
    val newClassTitle: String
    val subjectLabel: String
    val subjectPlaceholder: String
    val classTimeLabel: String
    val roomOrNoteLabel: String
    val roomOrNotePlaceholder: String
    val dayOfWeekLabel: String
    val daysOfWeek: List<String>
    val syncProgramToCalendar: String
    val syncProgramDesc: String
    val clearProgramFromCalendar: String
    val noClassesForDay: String
    val deleteClassAction: String

    // Calendar Pill Selector (R2) & Visibility (R3)
    val programFilterAll: String
    val programFilterNone: String
    val programVisibilityTitle: String
    val programVisibilityDesc: String
    val programVisibilityEverywhere: String
    val programVisibilityCalendarOnly: String
    val filtersAndProgramsTitle: String

    // Layer Programs & Period Tasks
    val layerProgramsTitle: String
    val layerProgramsDesc: String
    val addLayerProgram: String
    val deleteLayerProgram: String
    val newLayerProgramTitle: String
    val programLayerPrompt: String
    val taskPeriodOption: String
    val selectDateRangeTitle: String
    val startDateLabel: String
    val endDateLabel: String
    val daysCount: (Int) -> String
    val periodInProgress: String
    val syncLayerProgram: (String) -> String
    val selectProgramLayer: String
    val programPresetsTitle: String
    val presetUniversity: String
    val presetWork: String
    val presetGym: String
    val presetSchool: String
    val hasDateRangeLabel: String
    val ongoingRoutineDesc: String
    val programColorLabel: String
    val createProgramAction: String
    val confirmDeleteProgramTitle: String
    val confirmDeleteProgramDesc: (String) -> String
    val routineForDayHeader: (String) -> String
    val addRoutineForDayButton: (String) -> String
    val allProgramsTitle: String
    val allProgramsSubtitle: String
    val noProgramsEmptyTitle: String
    val noProgramsEmptySubtitle: String
    val allYearRound: String
    val programRoutinesCount: (Int) -> String
    val weekNumbersDropdownTitle: String
    val trackWeeksSwitchLabel: String
    val editProgramTitle: String
    val saveChangesAction: String
    val calendarWeekViewTitle: String
    val tapDateToAddRoutineHint: String
    val programTasksEverywhereSwitchLabel: String
    val programTasksEverywhereSwitchDesc: String
    val semesterWeeksSettingsTitle: String
    val semesterWeeksSettingsDesc: String

    // Instants (Quick Photo Notes)
    val instantsTitle: String
    val instantsSubtitle: String
    val snapPhotoAction: String
    val addPhotoNotePlaceholder: String
    val selectFolder: String
    val folderUnsorted: String
    val newFolderPrompt: String
    val saveSnapButton: String
    val deleteSnapAction: String
    val moveFolderAction: String
    val editNoteAction: String
    val noSnapsTitle: String
    val noSnapsSubtitle: String
    val allFoldersChip: String
    val photoNoteSavedToast: String
    val cameraPermissionRequired: String
    val pickFromGalleryAction: String
    val takePhotoAction: String
    val totalSnapsCount: (Int) -> String
    val navInstants: String
    val tapFolderToSave: String
    val tapFolderToSaveSubtitle: String
    val folderPhotosCount: (Int) -> String
    val newPhotoFolderTitle: String
    val createFolderAction: String
    val deleteFolderConfirmation: (String) -> String
    val zoomHint: String
    val resetZoomAction: String
    val fullScreenPhotoTitle: String
}

data object BulgarianStrings : Strings {

    override val appName: String = "FlowTask"
    override val appSubtitle: String = "Твоят интелигентен органайзер"

    override val navProjects: String = "Проекти"
    override val navToday: String = "Днес"
    override val navAllTasks: String = "Всички"
    override val navTitle: String = "Навигация"
    override val navSubtitle: String = "Избери екран или плъзни бутона"

    override val todayHeader: String = "Настоящи задачи"
    override val todayRemainingCount: (Int) -> String = { count -> "$count оставащи за днес" }
    override val todayEmptyTitle: String = "Всички задачи за днес са изпълнени!"
    override val todayEmptySubtitle: String = "Натисни '+' за нова задача"
    override val upcomingHeader: String = "Предстоящи задачи"
    override val upcomingMoreCount: (Int) -> String = { count -> "+$count още" }
    override val upcomingDrawerSubtitle: (Int) -> String = { count -> "Следващите 10 дни ($count задачи)" }
    override val upcomingEmpty: String = "Няма планирани предстоящи задачи"

    override val projectsHeader: String = "Проекти & Идеи"
    override val projectsSubtitle: String = "Свободно пространство за твоите цели и идеи"
    override val projectsEmptyTitle: String = "Все още няма създадени проекти"
    override val projectsEmptySubtitle: String = "Натисни '+' горе, за да създадеш първия си проект"
    override val foldersSection: (Int) -> String = { count -> "Папки ($count)" }
    override val foldersEmpty: String = "Няма подпапки в тази секция"
    override val tasksSection: (Int) -> String = { count -> "Задачи ($count)" }
    override val tasksEmpty: String = "Няма задачи в тази папка"
    override val notesSection: (Int) -> String = { count -> "Бележки & Идеи ($count)" }
    override val notesEmpty: String = "Няма добавени бележки"
    override val newProjectTitle: String = "Нов проект / Идея"
    override val projectNameLabel: String = "Име на проекта"
    override val projectNamePlaceholder: String = "Напр. Редизайн на уебсайт, Нов бизнес plan..."
    override val projectDescLabel: String = "Описание / Визия (незадължително)"
    override val projectDescPlaceholder: String = "Каква е основната цел на този проект?"
    override val useAppColorLabel: String = "Използвай цвета на приложението"
    override val projectColorLabel: String = "Цвят на проекта"
    override val projectIconLabel: String = "Икона"
    override val createProjectButton: String = "Създай проект"
    override val newFolderTitle: String = "Нова папка"
    override val folderNamePlaceholder: String = "Име на папката"
    override val newNoteTitle: String = "Нова бележка"
    override val editNoteTitle: String = "Редактиране на бележка"
    override val noteTitlePlaceholder: String = "Заглавие на бележката (незадължително)"
    override val noteContentPlaceholder: String = "Запиши своята идея, мисъл или детайл тук..."
    override val saveNoteButton: String = "Запази бележката"
    override val expandNoteAction: String = "Цял екран"
    override val collapseNoteAction: String = "Свий"
    override val formatBold: String = "Удебелен"
    override val formatItalic: String = "Курсив"
    override val formatHeading1: String = "Заглавие 1"
    override val formatHeading2: String = "Заглавие 2"
    override val formatChecklist: String = "Списък с отметки"
    override val formatBulletList: String = "Списък с точки"
    override val formatNumberedList: String = "Номериран списък"
    override val formatQuote: String = "Цитат"
    override val formatCode: String = "Код"
    override val formatDivider: String = "Разделител"
    override val formatStrikethrough: String = "Зачеркнат"
    override val formatHeading3: String = "Заглавие 3"
    override val formatUndo: String = "Отмени"
    override val formatRedo: String = "Повтори"
    override val formatDismissKeyboard: String = "Скрий клавиатурата"
    override val pinnedProjectsSection: (Int) -> String = { count -> "Закачени ($count)" }
    override val allProjectsSection: (Int) -> String = { count -> "Всички проекти ($count)" }
    override val pinProjectAction: String = "Закачи проект"
    override val unpinProjectAction: String = "Откачи проект"
    override val projectPinnedBadge: String = "Закачен"
    override val openProjectAction: String = "Отвори проект"
    override val projectDetailsTitle: String = "Детайли за проекта"
    override val projectNoDescription: String = "Няма въведено описание за този проект"
    override val projectStatsTasks: String = "Задачи"
    override val projectStatsFolders: String = "Папки"
    override val projectStatsNotes: String = "Бележки"
    override val projectTimelineCreated: String = "Създаден"
    override val projectTimelineUpdated: String = "Последна промяна"
    override val pinProjectToggleLabel: String = "Закачи в горната секция"
    override val useAppColorToggleLabel: String = "Цвят от темата / Динамичен цвят"
    override val useAppColorToggleSubtitle: String = "Използвай цвета на приложението"
    override val searchProjectsPlaceholder: String = "Търси в проекти и папки..."
    override val searchResultsHeader: (Int) -> String = { count -> "Резултати ($count)" }
    override val searchNoResultsTitle: String = "Няма намерени проекти или папки"
    override val searchNoResultsSubtitle: String = "Опитайте с друга дума или папка"
    override val matchingFoldersSubtitle: (Int) -> String = { count -> "Съвпадащи папки ($count)" }
    override val openFolderDirectly: String = "Отвори папка"

    override val allTasksHeader: String = "Всички задачи"
    override val allTasksActiveCount: (Int) -> String = { count -> "$count активни задачи" }
    override val searchPlaceholder: String = "Търси задача..."
    override val activeTasksSection: (Int) -> String = { count -> "Активни ($count)" }
    override val completedTasksSection: (Int) -> String = { count -> "Завършени ($count)" }
    override val noTasksFound: String = "Няма намерени задачи"
    override val tasksForDateTitle: (String, Int) -> String = { date, count -> "Задачи за $date ($count)" }
    override val noTasksForDate: String = "Няма планирани задачи за този ден"

    override val newTaskTitle: String = "Нова задача"
    override val editTaskTitle: String = "Редактиране на задача"
    override val taskExplanationPlaceholder: String = "Какво трябва да се свърши?..."
    override val subtasksHeader: (Int) -> String = { count -> "Подзадачи ($count)" }
    override val addSubtaskPlaceholder: String = "Добави подзадача..."
    override val dateOptional: String = "Дата (незадължителна)"
    override val timeOptional: String = "Час (незадължителен)"
    override val layerSectionTitle: String = "Слой / Профил"
    override val noLayer: String = "Без слой"
    override val allLayers: String = "Всички"
    override val addLayerPrompt: String = "Нов слой / профил"
    override val newLayerPlaceholder: String = "Име на слоя (напр. Дом, Работа)..."
    override val customLayer: String = "Персонализиран"
    override val editLayerTitle: String = "Редактиране на слой"
    override val layerNameLabel: String = "Име на слоя"
    override val renameLayerAction: String = "Запази името"
    override val deleteLayerAction: String = "Изтрий слой"
    override val deleteLayerConfirmTitle: (String) -> String = { name -> "Изтриване на слой \"$name\"?" }
    override val deleteLayerSubtitle: (Int) -> String = { count -> if (count == 1) "Този слой се използва от 1 задача." else "Този слой се използва от $count задачи." }
    override val deleteLayerKeepTasks: String = "Запази задачите (премахни слоя)"
    override val deleteLayerDeleteTasks: String = "Изтрий и задачите в слоя"
    override val saveTaskButton: String = "Запази задачата"
    override val taskDetailsTitle: String = "Детайли за задачата"
    override val statusActive: String = "Активна"
    override val statusCompleted: String = "Завършена"
    override val taskScheduleTitle: String = "График"
    override val noTimeSet: String = "Без конкретен час"
    override val viewDetailsAction: String = "Детайли"

    override val addAction: String = "Добави"
    override val saveAction: String = "Запази"
    override val cancelAction: String = "Отказ"
    override val deleteAction: String = "Изтрий"
    override val editAction: String = "Редактирай"
    override val openAction: String = "Отвори"
    override val closeAction: String = "Затвори"
    override val createNewAction: String = "Нова задача"
    override val deleteFolderTitle: String = "Изтрий папка"
    override val deleteProjectTitle: String = "Изтрий проекта"
    override val itemsCount: (Int) -> String = { count -> if (count == 1) "1 елемент" else "$count елемента" }
    override val folderEmptyStateTitle: String = "Папката е празна"
    override val folderEmptyStateSubtitle: String = "Създай папка, задача или бележка тук"
    override val moveToFolderAction: String = "Премести в..."
    override val moveToFolderTitle: String = "Преместване в папка"
    override val projectRootFolder: String = "Корен на проекта (Начало)"
    override val filterAll: String = "Всички"
    override val filterFolders: String = "Папки"
    override val filterTasks: String = "Задачи"
    override val filterNotes: String = "Бележки"
    override val addFolderChip: String = "+ Папка"
    override val addTaskChip: String = "+ Задача"
    override val addNoteChip: String = "+ Бележка"
    override val renameFolderAction: String = "Преименувай папка"
    override val editProjectAction: String = "Редактирай проект"
    override val updateAction: String = "Обнови"

    override val settingsTitle: String = "Настройки"
    override val languageSection: String = "Език (Language)"
    override val languageBulgarian: String = "Български"
    override val languageEnglish: String = "English"
    override val appearanceSection: String = "Тема и Цветове"
    override val darkModeTitle: String = "Режим на темата"
    override val darkModeDesc: String = "Избери между светла, тъмна тема или системна по подразбиране"
    override val darkModeSystem: String = "Системен"
    override val darkModeLight: String = "Светъл"
    override val darkModeDark: String = "Тъмен"
    override val materialYouToggle: String = "Material You Динамични цветове"
    override val materialYouDesc: String = "Използва акцентните цветове от твоя тапет (Android 12+)"
    override val amoledToggle: String = "AMOLED Тъмен режим"
    override val amoledDesc: String = "Чисто черен фон (#000000) за пестене на енергия при OLED екрани"
    override val dynamicStylesTitle: String = "Стил на динамичните цветове"
    override val dynamicStylesDesc: String = "Избери нюанси и контраст за динамичната палитра"
    override val dynamicStyleBalanced: String = "Балансиран"
    override val dynamicStyleVibrant: String = "Наситен"
    override val dynamicStyleMuted: String = "Пастелен"
    override val dynamicStyleHighContrast: String = "Контрастен"
    override val customAccentColors: String = "Избери акцентен цвят"
    override val screenOrderSection: String = "Подредба на екраните"
    override val screenOrderDesc: String = "Промени реда на страниците при плъзгане"
    override val moveUp: String = "Нагоре"
    override val moveDown: String = "Надолу"
    override val resetOrder: String = "Възстанови началния ред"
    override val versionInfo: String = "FlowTask v1.0 • Направен с Jetpack Compose"

    override val semesterSettingsTitle: String = "Програми и графици"
    override val semesterSettingsDesc: String = "Седмични графици, рутини и активни периоди"
    override val semesterSheetTitle: String = "Програми & График"
    override val semesterTabDates: String = "Период"
    override val semesterTabSchedule: String = "График и събития"
    override val semesterEnableSwitch: String = "Активирай програмата"
    override val semesterEnableDesc: String = "Показва седмици и събития в календара"
    override val semesterStartDateLabel: String = "Начална дата"
    override val semesterEndDateLabel: String = "Крайна дата"
    override val semesterNotSet: String = "Не е избрано"
    override val semesterTotalWeeksCount: (Int) -> String = { count -> "Общо: $count седмици" }
    override val semesterCurrentWeekLabel: (Int) -> String = { week -> "Текуща: Седмица $week" }
    override val semesterOutsideRange: String = "Извън активния период"
    override val semesterWeekShort: (Int) -> String = { week -> "С$week" }
    override val semesterWeekHeader: (Int) -> String = { week -> "Седмица $week" }
    override val semesterProgramLayerLabel: String = "Профил / Слой на програмата"
    override val addClassButton: String = "Добави рутина"
    override val editClassTitle: String = "Редактиране на събитие"
    override val newClassTitle: String = "Нова рутина в графика"
    override val subjectLabel: String = "Заглавие / Дейност"
    override val subjectPlaceholder: String = "напр. Тренировка, Екипна среща, Фокус време..."
    override val classTimeLabel: String = "Час на провеждане"
    override val roomOrNoteLabel: String = "Локация / Организатор / Бележка"
    override val roomOrNotePlaceholder: String = "напр. Зала 1, Онлайн, Иван..."
    override val dayOfWeekLabel: String = "Ден от седмицата"
    override val daysOfWeek: List<String> = listOf("Понеделник", "Вторник", "Сряда", "Четвъртък", "Петък", "Събота", "Неделя")
    override val syncProgramToCalendar: String = "Синхронизирай с календара"
    override val syncProgramDesc: String = "Генерира повтарящи се събития в календара за активния период"
    override val clearProgramFromCalendar: String = "Изчисти от календара"
    override val noClassesForDay: String = "Няма събития за този ден"
    override val deleteClassAction: String = "Премахни събитие"

    // Calendar Pill Selector (R2) & Visibility (R3)
    override val programFilterAll: String = "Всички програми"
    override val programFilterNone: String = "Без програми"
    override val programVisibilityTitle: String = "Показване на рутините от програмите"
    override val programVisibilityDesc: String = "Определя дали събитията се появяват като задачи в цялото приложение"
    override val programVisibilityEverywhere: String = "Навсякъде като задачи (Днес, Всички, Уиджет)"
    override val programVisibilityCalendarOnly: String = "Само в календара"
    override val filtersAndProgramsTitle: String = "Слоеве и програми"

    // Layer Programs & Period Tasks
    override val layerProgramsTitle: String = "Програми по слоеве"
    override val layerProgramsDesc: String = "Седмични графици, рутини и периоди за всеки профил (Работа, Дом, Спорт)"
    override val addLayerProgram: String = "Нова програма"
    override val deleteLayerProgram: String = "Изтрий програма"
    override val newLayerProgramTitle: String = "Създай програма за слой"
    override val programLayerPrompt: String = "Име на слоя за тази програма"
    override val taskPeriodOption: String = "Период..."
    override val selectDateRangeTitle: String = "Избери период"
    override val startDateLabel: String = "Начална дата"
    override val endDateLabel: String = "Крайна дата"
    override val daysCount: (Int) -> String = { count -> "$count дни" }
    override val periodInProgress: String = "В ход"
    override val syncLayerProgram: (String) -> String = { layer -> "Синхронизирай с '$layer'" }
    override val selectProgramLayer: String = "Избери слой"
    override val programPresetsTitle: String = "Бързи шаблони"
    override val presetUniversity: String = "Университет"
    override val presetWork: String = "Работа"
    override val presetGym: String = "Фитнес"
    override val presetSchool: String = "Уроци / Курсове"
    override val hasDateRangeLabel: String = "Определен период (семестър / срок)"
    override val ongoingRoutineDesc: String = "Постоянна седмична рутина през цялата година"
    override val programColorLabel: String = "Цвят на програмата"
    override val createProgramAction: String = "Създай програма"
    override val confirmDeleteProgramTitle: String = "Изтриване на програма"
    override val confirmDeleteProgramDesc: (String) -> String = { name -> "Сигурни ли сте, че искате да изтриете програма '$name' и нейните рутини?" }
    override val routineForDayHeader: (String) -> String = { day -> "Рутини за $day" }
    override val addRoutineForDayButton: (String) -> String = { day -> "Добави рутина за $day" }
    override val allProgramsTitle: String = "Програми"
    override val allProgramsSubtitle: String = "Седмични графици и рутини"
    override val noProgramsEmptyTitle: String = "Все още няма създадени програми"
    override val noProgramsEmptySubtitle: String = "Създай програма за своя график, университет или спорт"
    override val allYearRound: String = "Целогодишна"
    override val programRoutinesCount: (Int) -> String = { count -> if (count == 1) "1 рутина" else "$count рутини" }
    override val weekNumbersDropdownTitle: String = "Номера на седмиците (Седмица 1, Седмица 2...)"
    override val trackWeeksSwitchLabel: String = "Показвай номера на седмиците в календара"
    override val editProgramTitle: String = "Редактиране на програма"
    override val saveChangesAction: String = "Запази промените"
    override val calendarWeekViewTitle: String = "Седмичен график"
    override val tapDateToAddRoutineHint: String = "Избери ден, за да добавиш рутина"
    override val programTasksEverywhereSwitchLabel: String = "Показвай като задачи в Днес и Всички"
    override val programTasksEverywhereSwitchDesc: String = "Ако е изключено, рутините ще се виждат само в календара"
    override val semesterWeeksSettingsTitle: String = "Семестриални седмици в календара"
    override val semesterWeeksSettingsDesc: String = "Показване на номерата на седмиците (С1, С2...) и период"

    // Instants (Quick Photo Notes)
    override val instantsTitle: String = "Моменти"
    override val instantsSubtitle: String = "Бързи фото бележки за сортиране"
    override val snapPhotoAction: String = "Снимай момент"
    override val addPhotoNotePlaceholder: String = "Добави бележка или мисъл (незадължително)..."
    override val selectFolder: String = "Папка за сортиране"
    override val folderUnsorted: String = "Несортирани"
    override val newFolderPrompt: String = "Нова папка"
    override val saveSnapButton: String = "Запази снимката"
    override val deleteSnapAction: String = "Изтрий снимката"
    override val moveFolderAction: String = "Премести в папка"
    override val editNoteAction: String = "Редактирай бележката"
    override val noSnapsTitle: String = "Все още няма заснети моменти"
    override val noSnapsSubtitle: String = "Натисни бутона за камера в навигацията, за да заснемеш фото бележка"
    override val allFoldersChip: String = "Всички"
    override val photoNoteSavedToast: String = "Снимката е запазена успешно"
    override val cameraPermissionRequired: String = "Нужно е разрешение за камера за заснемане на момента"
    override val pickFromGalleryAction: String = "Избери от галерия"
    override val takePhotoAction: String = "Снимай с камера"
    override val totalSnapsCount: (Int) -> String = { count -> if (count == 1) "1 момент" else "$count момента" }
    override val navInstants: String = "Снимки"
    override val tapFolderToSave: String = "Избери папка за запазване"
    override val tapFolderToSaveSubtitle: String = "Натисни папката, за да запазиш снимката веднага"
    override val folderPhotosCount: (Int) -> String = { count -> if (count == 1) "1 снимка" else "$count снимки" }
    override val newPhotoFolderTitle: String = "Нова папка за снимки"
    override val createFolderAction: String = "Създай папка"
    override val deleteFolderConfirmation: (String) -> String = { name -> "Сигурни ли сте, че искате да изтриете папка '$name'? Снимките ще бъдат преместени в 'Несортирани'." }
    override val zoomHint: String = "Цял екран"
    override val resetZoomAction: String = "Възстанови мащаба"
    override val fullScreenPhotoTitle: String = "Преглед"
}

data object EnglishStrings : Strings {

    override val appName: String = "FlowTask"
    override val appSubtitle: String = "Your intelligent task companion"

    override val navProjects: String = "Projects"
    override val navToday: String = "Today"
    override val navAllTasks: String = "All Tasks"
    override val navTitle: String = "Navigation"
    override val navSubtitle: String = "Select screen or swipe button"

    override val todayHeader: String = "Today's Tasks"
    override val todayRemainingCount: (Int) -> String = { count -> "$count remaining today" }
    override val todayEmptyTitle: String = "All tasks for today are completed!"
    override val todayEmptySubtitle: String = "Tap '+' to add a new task"
    override val upcomingHeader: String = "Upcoming Tasks"
    override val upcomingMoreCount: (Int) -> String = { count -> "+$count more" }
    override val upcomingDrawerSubtitle: (Int) -> String = { count -> "Next 10 days ($count tasks)" }
    override val upcomingEmpty: String = "No upcoming tasks planned"

    override val projectsHeader: String = "Projects & Ideas"
    override val projectsSubtitle: String = "Creative space for your goals and ideas"
    override val projectsEmptyTitle: String = "No projects created yet"
    override val projectsEmptySubtitle: String = "Tap '+' above to create your first project"
    override val foldersSection: (Int) -> String = { count -> "Folders ($count)" }
    override val foldersEmpty: String = "No subfolders in this section"
    override val tasksSection: (Int) -> String = { count -> "Tasks ($count)" }
    override val tasksEmpty: String = "No tasks in this folder"
    override val notesSection: (Int) -> String = { count -> "Notes & Ideas ($count)" }
    override val notesEmpty: String = "No notes added yet"
    override val newProjectTitle: String = "New Project / Idea"
    override val projectNameLabel: String = "Project Name"
    override val projectNamePlaceholder: String = "e.g. Website Redesign, New Business Plan..."
    override val projectDescLabel: String = "Description / Vision (Optional)"
    override val projectDescPlaceholder: String = "What is the primary vision for this project?"
    override val useAppColorLabel: String = "Use App Color (Dynamic)"
    override val projectColorLabel: String = "Project Color"
    override val projectIconLabel: String = "Icon"
    override val createProjectButton: String = "Create Project"
    override val newFolderTitle: String = "New Folder"
    override val folderNamePlaceholder: String = "Folder name"
    override val newNoteTitle: String = "New Note"
    override val editNoteTitle: String = "Edit Note"
    override val noteTitlePlaceholder: String = "Note title (Optional)"
    override val noteContentPlaceholder: String = "Write your idea, thought, or detail here..."
    override val saveNoteButton: String = "Save Note"
    override val expandNoteAction: String = "Full screen"
    override val collapseNoteAction: String = "Collapse"
    override val formatBold: String = "Bold"
    override val formatItalic: String = "Italic"
    override val formatHeading1: String = "Heading 1"
    override val formatHeading2: String = "Heading 2"
    override val formatChecklist: String = "Checklist"
    override val formatBulletList: String = "Bullet list"
    override val formatNumberedList: String = "Numbered list"
    override val formatQuote: String = "Quote"
    override val formatCode: String = "Code"
    override val formatDivider: String = "Divider"
    override val formatStrikethrough: String = "Strikethrough"
    override val formatHeading3: String = "Heading 3"
    override val formatUndo: String = "Undo"
    override val formatRedo: String = "Redo"
    override val formatDismissKeyboard: String = "Hide keyboard"
    override val pinnedProjectsSection: (Int) -> String = { count -> "Pinned ($count)" }
    override val allProjectsSection: (Int) -> String = { count -> "All Projects ($count)" }
    override val pinProjectAction: String = "Pin Project"
    override val unpinProjectAction: String = "Unpin Project"
    override val projectPinnedBadge: String = "Pinned"
    override val openProjectAction: String = "Open Project"
    override val projectDetailsTitle: String = "Project Details"
    override val projectNoDescription: String = "No description provided for this project"
    override val projectStatsTasks: String = "Tasks"
    override val projectStatsFolders: String = "Folders"
    override val projectStatsNotes: String = "Notes"
    override val projectTimelineCreated: String = "Created"
    override val projectTimelineUpdated: String = "Last Updated"
    override val pinProjectToggleLabel: String = "Pin to top section"
    override val useAppColorToggleLabel: String = "App / Dynamic theme color"
    override val useAppColorToggleSubtitle: String = "Use app's dynamic accent color"
    override val searchProjectsPlaceholder: String = "Search in projects and folders..."
    override val searchResultsHeader: (Int) -> String = { count -> "Results ($count)" }
    override val searchNoResultsTitle: String = "No projects or folders found"
    override val searchNoResultsSubtitle: String = "Try another keyword or folder"
    override val matchingFoldersSubtitle: (Int) -> String = { count -> "Matching folders ($count)" }
    override val openFolderDirectly: String = "Open folder"

    override val allTasksHeader: String = "All Tasks"
    override val allTasksActiveCount: (Int) -> String = { count -> "$count active tasks" }
    override val searchPlaceholder: String = "Search tasks..."
    override val activeTasksSection: (Int) -> String = { count -> "Active ($count)" }
    override val completedTasksSection: (Int) -> String = { count -> "Completed ($count)" }
    override val noTasksFound: String = "No tasks found"
    override val tasksForDateTitle: (String, Int) -> String = { date, count -> "Tasks for $date ($count)" }
    override val noTasksForDate: String = "No tasks scheduled for this day"

    override val newTaskTitle: String = "New Task"
    override val editTaskTitle: String = "Edit Task"
    override val taskExplanationPlaceholder: String = "What needs to be done?..."
    override val subtasksHeader: (Int) -> String = { count -> "Subtasks ($count)" }
    override val addSubtaskPlaceholder: String = "Add subtask..."
    override val dateOptional: String = "Date (Optional)"
    override val timeOptional: String = "Time (Optional)"
    override val layerSectionTitle: String = "Profile / Layer"
    override val noLayer: String = "No Layer"
    override val allLayers: String = "All"
    override val addLayerPrompt: String = "New Layer / Profile"
    override val newLayerPlaceholder: String = "Layer name (e.g. House, Work)..."
    override val customLayer: String = "Custom"
    override val editLayerTitle: String = "Edit Layer"
    override val layerNameLabel: String = "Layer Name"
    override val renameLayerAction: String = "Save Name"
    override val deleteLayerAction: String = "Delete Layer"
    override val deleteLayerConfirmTitle: (String) -> String = { name -> "Delete layer \"$name\"?" }
    override val deleteLayerSubtitle: (Int) -> String = { count -> if (count == 1) "This layer is used by 1 task." else "This layer is used by $count tasks." }
    override val deleteLayerKeepTasks: String = "Keep tasks (remove layer tag)"
    override val deleteLayerDeleteTasks: String = "Delete tasks in layer too"
    override val saveTaskButton: String = "Save Task"
    override val taskDetailsTitle: String = "Task Details"
    override val statusActive: String = "Active"
    override val statusCompleted: String = "Completed"
    override val taskScheduleTitle: String = "Schedule"
    override val noTimeSet: String = "All day / No time"
    override val viewDetailsAction: String = "Details"

    override val addAction: String = "Add"
    override val saveAction: String = "Save"
    override val cancelAction: String = "Cancel"
    override val deleteAction: String = "Delete"
    override val editAction: String = "Edit"
    override val openAction: String = "Open"
    override val closeAction: String = "Close"
    override val createNewAction: String = "New Task"
    override val deleteFolderTitle: String = "Delete folder"
    override val deleteProjectTitle: String = "Delete project"
    override val itemsCount: (Int) -> String = { count -> if (count == 1) "1 item" else "$count items" }
    override val folderEmptyStateTitle: String = "This folder is empty"
    override val folderEmptyStateSubtitle: String = "Create a folder, task, or note here"
    override val moveToFolderAction: String = "Move to..."
    override val moveToFolderTitle: String = "Move to Folder"
    override val projectRootFolder: String = "Project Root (Main)"
    override val filterAll: String = "All"
    override val filterFolders: String = "Folders"
    override val filterTasks: String = "Tasks"
    override val filterNotes: String = "Notes"
    override val addFolderChip: String = "+ Folder"
    override val addTaskChip: String = "+ Task"
    override val addNoteChip: String = "+ Note"
    override val renameFolderAction: String = "Rename Folder"
    override val editProjectAction: String = "Edit Project"
    override val updateAction: String = "Update"

    override val settingsTitle: String = "Settings"
    override val languageSection: String = "Language"
    override val languageBulgarian: String = "Български (Bulgarian)"
    override val languageEnglish: String = "English"
    override val appearanceSection: String = "Appearance & Accent Colors"
    override val darkModeTitle: String = "Theme Mode"
    override val darkModeDesc: String = "Choose between light, dark, or system default mode"
    override val darkModeSystem: String = "System"
    override val darkModeLight: String = "Light"
    override val darkModeDark: String = "Dark"
    override val materialYouToggle: String = "Material You Dynamic Colors"
    override val materialYouDesc: String = "Uses accent tones from your wallpaper (Android 12+)"
    override val amoledToggle: String = "AMOLED Dark Mode"
    override val amoledDesc: String = "Pure black background (#000000) to save battery on OLED screens"
    override val dynamicStylesTitle: String = "Dynamic Palette Style"
    override val dynamicStylesDesc: String = "Choose contrast and saturation for dynamic wallpaper colors"
    override val dynamicStyleBalanced: String = "Balanced"
    override val dynamicStyleVibrant: String = "Vibrant"
    override val dynamicStyleMuted: String = "Muted"
    override val dynamicStyleHighContrast: String = "High Contrast"
    override val customAccentColors: String = "Choose Accent Color"
    override val screenOrderSection: String = "Screen Order"
    override val screenOrderDesc: String = "Customize the order of swipeable pages"
    override val moveUp: String = "Move Up"
    override val moveDown: String = "Move Down"
    override val resetOrder: String = "Reset to Default Order"
    override val versionInfo: String = "FlowTask v1.0 • Built with Jetpack Compose"

    override val semesterSettingsTitle: String = "Programs & Routines"
    override val semesterSettingsDesc: String = "Weekly routines, schedules, and active periods"
    override val semesterSheetTitle: String = "Programs & Routines"
    override val semesterTabDates: String = "Period"
    override val semesterTabSchedule: String = "Routines & Events"
    override val semesterEnableSwitch: String = "Enable Program"
    override val semesterEnableDesc: String = "Displays weeks and routine markers in the calendar"
    override val semesterStartDateLabel: String = "Start Date"
    override val semesterEndDateLabel: String = "End Date"
    override val semesterNotSet: String = "Not set"
    override val semesterTotalWeeksCount: (Int) -> String = { count -> "Total: $count weeks" }
    override val semesterCurrentWeekLabel: (Int) -> String = { week -> "Current: Week $week" }
    override val semesterOutsideRange: String = "Outside active period"
    override val semesterWeekShort: (Int) -> String = { week -> "W$week" }
    override val semesterWeekHeader: (Int) -> String = { week -> "Week $week" }
    override val semesterProgramLayerLabel: String = "Program layer / profile"
    override val addClassButton: String = "Add Routine"
    override val editClassTitle: String = "Edit Routine / Event"
    override val newClassTitle: String = "New Routine in Schedule"
    override val subjectLabel: String = "Routine Title / Event"
    override val subjectPlaceholder: String = "e.g. Team Standup, Workout, Focus Block..."
    override val classTimeLabel: String = "Time Slot"
    override val roomOrNoteLabel: String = "Location / Organizer / Note (Optional)"
    override val roomOrNotePlaceholder: String = "e.g. Room 101, Online, Lead Organizer..."
    override val dayOfWeekLabel: String = "Day of Week"
    override val daysOfWeek: List<String> = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    override val syncProgramToCalendar: String = "Sync to Calendar"
    override val syncProgramDesc: String = "Generates recurring routines across all weeks in the active period"
    override val clearProgramFromCalendar: String = "Clear Routines from Calendar"
    override val noClassesForDay: String = "No routines scheduled for this day"
    override val deleteClassAction: String = "Remove Routine"

    // Calendar Pill Selector (R2) & Visibility (R3)
    override val programFilterAll: String = "All Programs"
    override val programFilterNone: String = "None"
    override val programVisibilityTitle: String = "Program Routine Visibility"
    override val programVisibilityDesc: String = "Configure whether routines appear as tasks app-wide or stay calendar-only"
    override val programVisibilityEverywhere: String = "Everywhere as tasks (Today, All Tasks, Widgets)"
    override val programVisibilityCalendarOnly: String = "Calendar only"
    override val filtersAndProgramsTitle: String = "Layers & Programs"

    // Layer Programs & Period Tasks
    override val layerProgramsTitle: String = "Layer Programs"
    override val layerProgramsDesc: String = "Weekly routines and periods for each profile (Work, Home, Fitness)"
    override val addLayerProgram: String = "New Program"
    override val deleteLayerProgram: String = "Delete Program"
    override val newLayerProgramTitle: String = "Create Program for Layer"
    override val programLayerPrompt: String = "Layer name for this program"
    override val taskPeriodOption: String = "Period..."
    override val selectDateRangeTitle: String = "Select Date Range"
    override val startDateLabel: String = "Start Date"
    override val endDateLabel: String = "End Date"
    override val daysCount: (Int) -> String = { count -> "$count days" }
    override val periodInProgress: String = "In Progress"
    override val syncLayerProgram: (String) -> String = { layer -> "Sync to '$layer'" }
    override val selectProgramLayer: String = "Select Layer"
    override val programPresetsTitle: String = "Quick Presets"
    override val presetUniversity: String = "University"
    override val presetWork: String = "Work"
    override val presetGym: String = "Fitness"
    override val presetSchool: String = "Classes / Courses"
    override val hasDateRangeLabel: String = "Specific date range (semester / term)"
    override val ongoingRoutineDesc: String = "Ongoing recurring schedule all year round"
    override val programColorLabel: String = "Program Color"
    override val createProgramAction: String = "Create Program"
    override val confirmDeleteProgramTitle: String = "Delete Program"
    override val confirmDeleteProgramDesc: (String) -> String = { name -> "Are you sure you want to delete program '$name' and its scheduled routines?" }
    override val routineForDayHeader: (String) -> String = { day -> "Routines for $day" }
    override val addRoutineForDayButton: (String) -> String = { day -> "Add routine for $day" }
    override val allProgramsTitle: String = "Programs"
    override val allProgramsSubtitle: String = "Weekly routines and schedules"
    override val noProgramsEmptyTitle: String = "No programs created yet"
    override val noProgramsEmptySubtitle: String = "Create a program for your schedule, university, or fitness"
    override val allYearRound: String = "All year round"
    override val programRoutinesCount: (Int) -> String = { count -> if (count == 1) "1 routine" else "$count routines" }
    override val weekNumbersDropdownTitle: String = "Week numbers (Week 1, Week 2...)"
    override val trackWeeksSwitchLabel: String = "Show week numbers on calendar"
    override val editProgramTitle: String = "Edit Program"
    override val saveChangesAction: String = "Save Changes"
    override val calendarWeekViewTitle: String = "Weekly Timetable"
    override val tapDateToAddRoutineHint: String = "Select a day to add a routine"
    override val programTasksEverywhereSwitchLabel: String = "Show as tasks in Today & All Tasks"
    override val programTasksEverywhereSwitchDesc: String = "If turned off, routines will appear only on the calendar"
    override val semesterWeeksSettingsTitle: String = "Semester Weeks in Calendar"
    override val semesterWeeksSettingsDesc: String = "Display week numbers (W1, W2...) and active period"

    // Instants (Quick Photo Notes)
    override val instantsTitle: String = "Instants"
    override val instantsSubtitle: String = "Quick photo notes to be sorted"
    override val snapPhotoAction: String = "Snap Instant"
    override val addPhotoNotePlaceholder: String = "Add a note or thought (optional)..."
    override val selectFolder: String = "Sort into folder"
    override val folderUnsorted: String = "Unsorted"
    override val newFolderPrompt: String = "New Folder"
    override val saveSnapButton: String = "Save Snap"
    override val deleteSnapAction: String = "Delete Snap"
    override val moveFolderAction: String = "Move to folder"
    override val editNoteAction: String = "Edit note"
    override val noSnapsTitle: String = "No snaps captured yet"
    override val noSnapsSubtitle: String = "Tap the camera button in navigation to quickly snap a photo note"
    override val allFoldersChip: String = "All"
    override val photoNoteSavedToast: String = "Photo note saved successfully"
    override val cameraPermissionRequired: String = "Camera permission is needed to take a snap"
    override val pickFromGalleryAction: String = "Pick from gallery"
    override val takePhotoAction: String = "Take photo with camera"
    override val totalSnapsCount: (Int) -> String = { count -> if (count == 1) "1 instant" else "$count instants" }
    override val navInstants: String = "Photos"
    override val tapFolderToSave: String = "Select folder to save"
    override val tapFolderToSaveSubtitle: String = "Tap a folder to immediately save the photo"
    override val folderPhotosCount: (Int) -> String = { count -> if (count == 1) "1 photo" else "$count photos" }
    override val newPhotoFolderTitle: String = "New Photo Folder"
    override val createFolderAction: String = "Create Folder"
    override val deleteFolderConfirmation: (String) -> String = { name -> "Are you sure you want to delete folder '$name'? Photos will be moved to 'Unsorted'." }
    override val zoomHint: String = "Full screen"
    override val resetZoomAction: String = "Reset zoom"
    override val fullScreenPhotoTitle: String = "View"
}

val LocalAppStrings = compositionLocalOf<Strings> { BulgarianStrings }
