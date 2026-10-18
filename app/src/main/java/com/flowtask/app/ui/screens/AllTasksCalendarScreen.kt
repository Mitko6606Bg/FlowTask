package com.flowtask.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowtask.app.data.local.model.SemesterConfig
import com.flowtask.app.data.local.model.TaskWithSubTasks
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import androidx.compose.runtime.mutableFloatStateOf
import com.flowtask.app.data.local.model.LayerProgram
import com.flowtask.app.ui.components.CalendarDateTasksBottomSheet
import com.flowtask.app.ui.components.CalendarProgramFilterBar
import com.flowtask.app.ui.components.CollapsibleFiltersContainer
import com.flowtask.app.ui.components.LayerFilterBar
import com.flowtask.app.ui.components.TaskCard
import com.flowtask.app.ui.theme.BgLight
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.LocalDarkTheme
import com.flowtask.app.ui.theme.LocalDockColors
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.SurfaceContainerLight
import com.flowtask.app.ui.theme.rgbToHsl
import com.flowtask.app.ui.theme.hslToRgb
import com.flowtask.app.ui.theme.SurfaceContainerLow
import com.flowtask.app.ui.theme.SurfaceContainerLowest
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.ui.theme.TextMuted
import com.flowtask.app.ui.theme.TextOnAccent
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.viewmodel.TaskViewModel
import com.flowtask.app.util.DateTimeUtils
import com.flowtask.app.util.LocalAppStrings
import com.flowtask.app.util.SemesterUtils
import java.util.Calendar
import kotlinx.coroutines.launch

enum class AllTasksViewMode {
    LIST, SMART_CALENDAR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTasksCalendarScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier,
    screenOrder: List<Int> = listOf(0, 1, 2, 3),
    currentPagerIndex: Int = 2,
    onSelectPageIndex: (Int) -> Unit = {},
    onCreateClick: () -> Unit = { viewModel.openAddTask(null) },
    onSnapPhotoClick: () -> Unit = {},
    onNavigateToToday: (() -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val allTasks by viewModel.allTasks.collectAsState()
    val availableLayers by viewModel.availableLayers.collectAsState()
    val selectedLayer by viewModel.selectedLayer.collectAsState()
    val semesterConfig by viewModel.semesterConfig.collectAsState()
    val layerPrograms by viewModel.layerPrograms.collectAsState()
    val selectedCalendarProgramId by viewModel.selectedCalendarProgramId.collectAsState()
    val isFiltersExpanded by viewModel.calendarFiltersExpanded.collectAsState()

    val currentViewModeName by viewModel.allTasksViewMode.collectAsState()
    val viewMode = remember(currentViewModeName) {
        try {
            AllTasksViewMode.valueOf(currentViewModeName)
        } catch (_: Exception) {
            AllTasksViewMode.LIST
        }
    }
    var selectedCalendarDate by remember { mutableStateOf<Long?>(null) }
    var isDateTasksSheetVisible by remember { mutableStateOf(false) }
    val calendarScrollToTodayTrigger by viewModel.scrollToTodayCalendarTrigger.collectAsState()

    val activeCalendarPrograms = remember(layerPrograms, selectedCalendarProgramId) {
        when (selectedCalendarProgramId) {
            "NONE" -> emptyList()
            null -> {
                val firstEnabled = layerPrograms.firstOrNull { it.isEnabled } ?: layerPrograms.firstOrNull()
                if (firstEnabled != null) listOf(firstEnabled) else emptyList()
            }
            else -> layerPrograms.filter { it.id == selectedCalendarProgramId && it.isEnabled }
        }
    }

    val allProgramLayerNames = remember(layerPrograms, semesterConfig) {
        val names = layerPrograms.map { it.layerName.trim().lowercase() }.toMutableSet()
        if (semesterConfig.programLayerName.isNotBlank()) {
            names.add(semesterConfig.programLayerName.trim().lowercase())
        }
        names
    }

    val activeProgramLayerNames = remember(activeCalendarPrograms) {
        activeCalendarPrograms.map { it.layerName.trim().lowercase() }.toSet()
    }

    // Filter tasks so program tasks only appear if their program is active on the calendar
    val calendarTasks = remember(allTasks, allProgramLayerNames, activeProgramLayerNames) {
        allTasks.filter { item ->
            val layer = item.task.layer?.trim()?.lowercase()
            layer == null || layer !in allProgramLayerNames || layer in activeProgramLayerNames
        }
    }

    val normalTaskDates = remember(calendarTasks, allProgramLayerNames) {
        val set = mutableSetOf<Long>()
        // Regular tasks (excluding program tasks)
        for (item in calendarTasks) {
            val layer = item.task.layer?.trim()?.lowercase()
            if (layer != null && layer in allProgramLayerNames) continue
            val s = item.task.startDateMillis?.let { DateTimeUtils.getStartOfDay(it) }
            val e = item.task.dueDateMillis?.let { DateTimeUtils.getStartOfDay(it) }
            if (s != null && e != null && e >= s) {
                val cal = Calendar.getInstance().apply { timeInMillis = s }
                while (cal.timeInMillis <= e) {
                    set.add(cal.timeInMillis)
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
            } else if (e != null) {
                set.add(e)
            } else if (s != null) {
                set.add(s)
            }
        }
        set
    }

    val periodDates = remember(calendarTasks, allProgramLayerNames) {
        val set = mutableSetOf<Long>()
        // Multi-day regular tasks ONLY (not entire program duration)
        for (item in calendarTasks) {
            val layer = item.task.layer?.trim()?.lowercase()
            if (layer != null && layer in allProgramLayerNames) continue
            val s = item.task.startDateMillis?.let { DateTimeUtils.getStartOfDay(it) }
            val e = item.task.dueDateMillis?.let { DateTimeUtils.getStartOfDay(it) }
            if (s != null && e != null && e > s) {
                val cal = Calendar.getInstance().apply { timeInMillis = s }
                while (cal.timeInMillis <= e) {
                    set.add(cal.timeInMillis)
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }
        set
    }

    val programDatesWithColors = remember(calendarTasks, activeCalendarPrograms, currentAccent) {
        val map = mutableMapOf<Long, MutableList<Color>>()
        for (prog in activeCalendarPrograms) {
            if (!prog.isEnabled) continue
            val progColor = prog.colorHex?.let {
                try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { currentAccent }
            } ?: currentAccent

            // A. Tasks belonging to this active program
            val progLayerName = prog.layerName.trim().lowercase()
            for (item in calendarTasks) {
                val layer = item.task.layer?.trim()?.lowercase()
                if (layer == progLayerName) {
                    val s = item.task.startDateMillis?.let { DateTimeUtils.getStartOfDay(it) }
                    val e = item.task.dueDateMillis?.let { DateTimeUtils.getStartOfDay(it) }
                    if (s != null && e != null && e >= s) {
                        val cal = Calendar.getInstance().apply { timeInMillis = s }
                        while (cal.timeInMillis <= e) {
                            val list = map.getOrPut(cal.timeInMillis) { mutableListOf() }
                            if (!list.contains(progColor)) list.add(progColor)
                            cal.add(Calendar.DAY_OF_YEAR, 1)
                        }
                    } else if (e != null) {
                        val list = map.getOrPut(e) { mutableListOf() }
                        if (!list.contains(progColor)) list.add(progColor)
                    } else if (s != null) {
                        val list = map.getOrPut(s) { mutableListOf() }
                        if (!list.contains(progColor)) list.add(progColor)
                    }
                }
            }

            // B. Program routine occurrences on scheduled days
            val progItems = prog.scheduleItems
            if (progItems.isEmpty()) continue
            val activeDaysOfWeek = progItems.map { it.dayOfWeek }.toSet() // 1..7 (Mon..Sun)

            val today = DateTimeUtils.getStartOfDay()
            val startBound = prog.startDateMillis?.let { DateTimeUtils.getStartOfDay(it) }
                ?: (today - 90L * 86400000L)
            val endBound = prog.endDateMillis?.let { DateTimeUtils.getEndOfDay(it) }
                ?: (today + 270L * 86400000L)

            val cal = Calendar.getInstance().apply { timeInMillis = startBound }
            while (cal.timeInMillis <= endBound) {
                val d = cal.get(Calendar.DAY_OF_WEEK)
                val dow = if (d == Calendar.SUNDAY) 7 else d - 1 // 1..7
                if (dow in activeDaysOfWeek) {
                    val list = map.getOrPut(cal.timeInMillis) { mutableListOf() }
                    if (!list.contains(progColor)) list.add(progColor)
                }
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        map
    }

    val dayActivities = remember(normalTaskDates, periodDates, programDatesWithColors) {
        val map = mutableMapOf<Long, CalendarDayActivity>()
        val allDates = normalTaskDates + periodDates + programDatesWithColors.keys
        for (date in allDates) {
            map[date] = CalendarDayActivity(
                hasNormalTasks = normalTaskDates.contains(date),
                isPeriod = periodDates.contains(date),
                programColors = programDatesWithColors[date].orEmpty()
            )
        }
        map
    }

    val taskDatesWithActivity = remember(dayActivities) {
        dayActivities.keys
    }

    val selectedDateTasks = remember(calendarTasks, selectedCalendarDate) {
        if (selectedCalendarDate == null) emptyList()
        else {
            val targetDay = DateTimeUtils.getStartOfDay(selectedCalendarDate!!)
            calendarTasks.filter { taskWithSubTasks ->
                val task = taskWithSubTasks.task
                val s = task.startDateMillis?.let { DateTimeUtils.getStartOfDay(it) }
                val e = task.dueDateMillis?.let { DateTimeUtils.getStartOfDay(it) }
                if (s != null && e != null) {
                    targetDay in s..e
                } else if (e != null) {
                    e == targetDay
                } else if (s != null) {
                    s == targetDay
                } else {
                    false
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.allTasksHeader,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )
                    Text(
                        text = strings.allTasksActiveCount(allTasks.count { !it.task.isCompleted }),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val dockColors = LocalDockColors.current

                    // Discreet Academic Semester & Program Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (semesterConfig.isEnabled) dockColors.barBackground.copy(alpha = 0.14f) else SurfaceContainerLight,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (semesterConfig.isEnabled) dockColors.barBackground.copy(alpha = 0.40f) else OutlineSubtle.copy(alpha = 0.7f)
                        ),
                        shadowElevation = 1.dp,
                        modifier = Modifier.expressiveBounce(pressScale = 0.92f)
                    ) {
                        IconButton(
                            onClick = { viewModel.openSemesterSheet() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventRepeat,
                                contentDescription = strings.semesterSettingsTitle,
                                tint = if (semesterConfig.isEnabled) dockColors.barBackground else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // View Mode Switcher: Material 3 SingleChoiceSegmentedButtonRow
                    val isList = viewMode == AllTasksViewMode.LIST
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .height(40.dp)
                            .width(96.dp)
                    ) {
                        SegmentedButton(
                            selected = isList,
                            onClick = { viewModel.setAllTasksViewMode(AllTasksViewMode.LIST.name) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = dockColors.barBackground,
                                activeContentColor = dockColors.iconTint,
                                activeBorderColor = OutlineSubtle.copy(alpha = 0.6f),
                                inactiveContainerColor = SurfaceContainerLight,
                                inactiveContentColor = TextSecondary,
                                inactiveBorderColor = OutlineSubtle.copy(alpha = 0.6f)
                            ),
                            border = SegmentedButtonDefaults.borderStroke(
                                color = OutlineSubtle.copy(alpha = 0.5f),
                                width = 1.dp
                            ),
                            icon = {},
                            label = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                    contentDescription = "List",
                                    modifier = Modifier.size(19.dp)
                                )
                            },
                            modifier = Modifier.expressiveBounce(pressScale = 0.94f)
                        )

                        SegmentedButton(
                            selected = !isList,
                            onClick = { viewModel.setAllTasksViewMode(AllTasksViewMode.SMART_CALENDAR.name) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = dockColors.barBackground,
                                activeContentColor = dockColors.iconTint,
                                activeBorderColor = OutlineSubtle.copy(alpha = 0.6f),
                                inactiveContainerColor = SurfaceContainerLight,
                                inactiveContentColor = TextSecondary,
                                inactiveBorderColor = OutlineSubtle.copy(alpha = 0.6f)
                            ),
                            border = SegmentedButtonDefaults.borderStroke(
                                color = OutlineSubtle.copy(alpha = 0.5f),
                                width = 1.dp
                            ),
                            icon = {},
                            label = {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Calendar",
                                    modifier = Modifier.size(19.dp)
                                )
                            },
                            modifier = Modifier.expressiveBounce(pressScale = 0.94f)
                        )
                    }
                }
            }

            // Expandable Filters & Programs Container with Gestures, Handle & Inner Pills
            CollapsibleFiltersContainer(
                isExpanded = isFiltersExpanded,
                onToggleExpanded = { viewModel.toggleCalendarFiltersExpanded() },
                onSetExpanded = { viewModel.setCalendarFiltersExpanded(it) },
                selectedLayer = selectedLayer,
                availableLayers = availableLayers,
                onSelectLayer = { viewModel.selectLayer(it) },
                layerPrograms = layerPrograms,
                selectedProgramId = selectedCalendarProgramId,
                onSelectProgram = { viewModel.selectCalendarProgram(it) },
                lastSelectedProgramId = viewModel.getLastSelectedProgramId(),
                activeCalendarPrograms = activeCalendarPrograms,
                isCalendarMode = viewMode == AllTasksViewMode.SMART_CALENDAR,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                onLongClickLayer = { viewModel.openEditLayer(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Body Content based on Mode
            if (viewMode == AllTasksViewMode.LIST) {
                AllTasksListView(
                    allTasks = allTasks,
                    viewModel = viewModel
                )
            } else {
                var calPredictiveBackProgress by remember { mutableFloatStateOf(0f) }
                var calPredictiveBackEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }

                PredictiveBackHandler(enabled = !isDateTasksSheetVisible && !isFiltersExpanded) { progress: Flow<BackEventCompat> ->
                    try {
                        progress.collect { backEvent ->
                            calPredictiveBackProgress = backEvent.progress
                            calPredictiveBackEdge = backEvent.swipeEdge
                        }
                        viewModel.setAllTasksViewMode(AllTasksViewMode.LIST.name)
                    } catch (_: CancellationException) {
                    } finally {
                        calPredictiveBackProgress = 0f
                    }
                }

                PredictiveBackHandler(enabled = isFiltersExpanded && !isDateTasksSheetVisible) { progress: Flow<BackEventCompat> ->
                    try {
                        progress.collect { _ -> }
                        viewModel.setCalendarFiltersExpanded(false)
                    } catch (_: CancellationException) {
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            if (calPredictiveBackProgress > 0f) {
                                val scale = 1f - (calPredictiveBackProgress * 0.08f)
                                scaleX = scale
                                scaleY = scale
                                translationX = if (calPredictiveBackEdge == BackEventCompat.EDGE_LEFT) {
                                    calPredictiveBackProgress * 28.dp.toPx()
                                } else {
                                    -calPredictiveBackProgress * 28.dp.toPx()
                                }
                                alpha = 1f - (calPredictiveBackProgress * 0.18f)
                            }
                        }
                ) {
                    SmartCalendarView(
                        allTasks = calendarTasks,
                        dayActivities = dayActivities,
                        taskDatesWithActivity = taskDatesWithActivity,
                        periodDates = periodDates,
                        selectedDate = selectedCalendarDate,
                        onSelectDate = { dateMillis ->
                            selectedCalendarDate = dateMillis
                            isDateTasksSheetVisible = true
                        },
                        semesterConfig = semesterConfig,
                        activeCalendarPrograms = activeCalendarPrograms,
                        scrollToTodayTrigger = calendarScrollToTodayTrigger
                    )
                }
            }
        }

        // Card style pop up for selected calendar date tasks
        if (isDateTasksSheetVisible && selectedCalendarDate != null) {
            CalendarDateTasksBottomSheet(
                dateMillis = selectedCalendarDate!!,
                tasks = selectedDateTasks,
                semesterConfig = semesterConfig,
                viewModel = viewModel,
                onDismiss = { isDateTasksSheetVisible = false }
            )
        }
    }
}

@Composable
fun AllTasksListView(
    allTasks: List<TaskWithSubTasks>,
    viewModel: TaskViewModel
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val pendingTasks = remember(allTasks) { allTasks.filter { !it.task.isCompleted } }
    val completedTasks = remember(allTasks) { allTasks.filter { it.task.isCompleted } }

    if (allTasks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceContainerLight)
                .border(1.dp, OutlineSubtle.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                .padding(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.EventNote,
                    contentDescription = null,
                    tint = currentAccent,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = strings.noTasksFound,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.todayEmptySubtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (pendingTasks.isNotEmpty()) {
                item {
                    Text(
                        text = strings.activeTasksSection(pendingTasks.size),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        ),
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                    )
                }
                items(pendingTasks, key = { "all_pending_${it.task.id}" }) { taskWithSubTasks ->
                    TaskCard(
                        taskWithSubTasks = taskWithSubTasks,
                        onToggleTask = { id, done -> viewModel.toggleTaskCompletion(id, done) },
                        onToggleSubTask = { id, done -> viewModel.toggleSubTaskCompletion(id, done) },
                        onEditTask = { viewModel.openEditTask(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onDeleteSubTask = { viewModel.deleteSubTask(it) },
                        onAddSubTask = { id, text -> viewModel.addSubTask(id, text) },
                        showDateChip = true
                    )
                }
            }

            if (completedTasks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = strings.completedTasksSection(completedTasks.size),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextMuted
                        ),
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                    )
                }
                items(completedTasks, key = { "all_completed_${it.task.id}" }) { taskWithSubTasks ->
                    TaskCard(
                        taskWithSubTasks = taskWithSubTasks,
                        onToggleTask = { id, done -> viewModel.toggleTaskCompletion(id, done) },
                        onToggleSubTask = { id, done -> viewModel.toggleSubTaskCompletion(id, done) },
                        onEditTask = { viewModel.openEditTask(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onDeleteSubTask = { viewModel.deleteSubTask(it) },
                        onAddSubTask = { id, text -> viewModel.addSubTask(id, text) },
                        showDateChip = true
                    )
                }
            }
        }
    }
}

@Composable
fun SmartCalendarView(
    allTasks: List<TaskWithSubTasks>,
    dayActivities: Map<Long, CalendarDayActivity> = emptyMap(),
    taskDatesWithActivity: Set<Long> = emptySet(),
    periodDates: Set<Long> = emptySet(),
    selectedDate: Long?,
    onSelectDate: (Long) -> Unit,
    semesterConfig: SemesterConfig,
    activeCalendarPrograms: List<LayerProgram> = emptyList(),
    scrollToTodayTrigger: Int = 0
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val coroutineScope = rememberCoroutineScope()

    val activeMonths = remember(allTasks, semesterConfig, activeCalendarPrograms) {
        val currentCal = Calendar.getInstance()
        val currentYear = currentCal.get(Calendar.YEAR)
        val currentMonth = currentCal.get(Calendar.MONTH)
        val monthsSet = mutableSetOf<Pair<Int, Int>>()

        // Continuous calendar forward range: current month to next 12 months
        val rangeCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        for (i in 0..12) {
            monthsSet.add(Pair(rangeCal.get(Calendar.YEAR), rangeCal.get(Calendar.MONTH)))
            rangeCal.add(Calendar.MONTH, 1)
        }

        // Include all months in the active semester if enabled
        if (semesterConfig.isEnabled && semesterConfig.startDateMillis != null && semesterConfig.endDateMillis != null) {
            val sCal = Calendar.getInstance().apply { timeInMillis = semesterConfig.startDateMillis }
            val eCal = Calendar.getInstance().apply { timeInMillis = semesterConfig.endDateMillis }
            val curIter = Calendar.getInstance().apply {
                set(Calendar.YEAR, sCal.get(Calendar.YEAR))
                set(Calendar.MONTH, sCal.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            while (curIter.timeInMillis <= eCal.timeInMillis ||
                (curIter.get(Calendar.YEAR) == eCal.get(Calendar.YEAR) && curIter.get(Calendar.MONTH) == eCal.get(Calendar.MONTH))
            ) {
                monthsSet.add(Pair(curIter.get(Calendar.YEAR), curIter.get(Calendar.MONTH)))
                curIter.add(Calendar.MONTH, 1)
            }
        }

        // Include all months from active layer programs
        activeCalendarPrograms.forEach { prog ->
            if (prog.isEnabled && prog.startDateMillis != null && prog.endDateMillis != null) {
                val sCal = Calendar.getInstance().apply { timeInMillis = prog.startDateMillis }
                val eCal = Calendar.getInstance().apply { timeInMillis = prog.endDateMillis }
                val curIter = Calendar.getInstance().apply {
                    set(Calendar.YEAR, sCal.get(Calendar.YEAR))
                    set(Calendar.MONTH, sCal.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                while (curIter.timeInMillis <= eCal.timeInMillis ||
                    (curIter.get(Calendar.YEAR) == eCal.get(Calendar.YEAR) && curIter.get(Calendar.MONTH) == eCal.get(Calendar.MONTH))
                ) {
                    monthsSet.add(Pair(curIter.get(Calendar.YEAR), curIter.get(Calendar.MONTH)))
                    curIter.add(Calendar.MONTH, 1)
                }
            }
        }

        // Include all months spanned by tasks
        allTasks.forEach { item ->
            val s = item.task.startDateMillis?.let { DateTimeUtils.getStartOfDay(it) }
            val e = item.task.dueDateMillis?.let { DateTimeUtils.getStartOfDay(it) }
            if (s != null && e != null && e >= s) {
                val cal = Calendar.getInstance().apply { timeInMillis = s }
                while (cal.timeInMillis <= e) {
                    monthsSet.add(Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)))
                    cal.add(Calendar.MONTH, 1)
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                }
            } else if (e != null) {
                val cal = Calendar.getInstance().apply { timeInMillis = e }
                monthsSet.add(Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)))
            } else if (s != null) {
                val cal = Calendar.getInstance().apply { timeInMillis = s }
                monthsSet.add(Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)))
            }
        }

        // FILTER: Don't show passed months that don't have active (uncompleted) tasks!
        monthsSet
            .filter { (year, monthIndex) ->
                val isPassed = year < currentYear || (year == currentYear && monthIndex < currentMonth)
                if (!isPassed) {
                    true // Current and future months are kept
                } else {
                    // Passed month: ONLY show if it contains active (uncompleted) tasks
                    monthHasActiveTasks(year, monthIndex, allTasks)
                }
            }
            .sortedWith(compareBy({ it.first }, { it.second }))
    }

    val activeProgramForWeeks = remember(activeCalendarPrograms) {
        activeCalendarPrograms.firstOrNull { it.trackWeeks && it.startDateMillis != null && it.endDateMillis != null }
    }

    val currentCal = remember { Calendar.getInstance() }
    val currentYear = currentCal.get(Calendar.YEAR)
    val currentMonth = currentCal.get(Calendar.MONTH)
    val currentMonthIndex = remember(activeMonths) {
        val idx = activeMonths.indexOfFirst { it.first == currentYear && it.second == currentMonth }
        if (idx >= 0) idx else 0
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = currentMonthIndex)

    // Ensure we are firmly positioned at the current month when entering calendar or when list loads
    LaunchedEffect(currentMonthIndex) {
        if (currentMonthIndex in activeMonths.indices) {
            listState.scrollToItem(currentMonthIndex)
        }
    }

    // Scroll to current month when trigger fires
    LaunchedEffect(scrollToTodayTrigger) {
        if (scrollToTodayTrigger > 0) {
            val currentIndex = listState.firstVisibleItemIndex
            val distance = currentMonthIndex - currentIndex
            if (kotlin.math.abs(distance) > 1) {
                val intermediateIndex = if (distance > 0) currentMonthIndex - 1 else currentMonthIndex + 1
                listState.scrollToItem(intermediateIndex)
            }
            listState.animateScrollToItem(currentMonthIndex)
        }
    }

    val flingBehavior = ScrollableDefaults.flingBehavior()

    Box(modifier = Modifier.fillMaxSize()) {
        // High-performance smooth scrolling LazyColumn
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 160.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = activeMonths,
                key = { (year, monthIndex) -> year * 100 + monthIndex }
            ) { (year, monthIndex) ->
                MonthCalendarCard(
                    year = year,
                    monthIndex = monthIndex,
                    dayActivities = dayActivities,
                    taskDatesWithActivity = taskDatesWithActivity,
                    periodDates = periodDates,
                    selectedDate = selectedDate,
                    semesterConfig = semesterConfig,
                    activeProgramForWeeks = activeProgramForWeeks,
                    onSelectDate = onSelectDate
                )
            }
        }
    }
}

private data class MonthDayInfo(
    val dayNumber: Int,
    val dayMillis: Long,
    val isToday: Boolean
)

private data class MonthWeekRowData(
    val weekNum: Int?,
    val isCurrentWeek: Boolean,
    val sampleDayMillis: Long?,
    val days: List<MonthDayInfo?>
)

private data class MonthCardData(
    val monthTitle: String,
    val rows: List<MonthWeekRowData>
)

data class CalendarDayActivity(
    val hasNormalTasks: Boolean = false,
    val isPeriod: Boolean = false,
    val programColors: List<Color> = emptyList()
) {
    val hasProgram: Boolean get() = programColors.isNotEmpty()
    val isInterference: Boolean get() = hasNormalTasks && hasProgram
}

fun blendColors(color1: Color, color2: Color, ratio: Float = 0.5f): Color {
    val r = (color1.red * ratio + color2.red * (1f - ratio)).coerceIn(0f, 1f)
    val g = (color1.green * ratio + color2.green * (1f - ratio)).coerceIn(0f, 1f)
    val b = (color1.blue * ratio + color2.blue * (1f - ratio)).coerceIn(0f, 1f)
    return Color(red = r, green = g, blue = b, alpha = 1f)
}

fun blendColorList(colors: List<Color>): Color {
    if (colors.isEmpty()) return Color.Unspecified
    if (colors.size == 1) return colors[0]
    var r = 0f
    var g = 0f
    var b = 0f
    val count = colors.size.toFloat()
    for (c in colors) {
        r += c.red
        g += c.green
        b += c.blue
    }
    return Color(
        red = (r / count).coerceIn(0f, 1f),
        green = (g / count).coerceIn(0f, 1f),
        blue = (b / count).coerceIn(0f, 1f),
        alpha = 1f
    )
}

fun brightenColorForDark(color: Color, targetLightness: Float = 0.78f): Color {
    val hsl = rgbToHsl(color)
    val h = hsl[0]
    val origS = hsl[1]
    val origL = hsl[2]

    if (origL >= targetLightness) return color

    val s = if (origS < 0.12f) origS else origS.coerceIn(0.45f, 0.90f)
    return hslToRgb(h, s, targetLightness, color.alpha)
}

private fun monthHasActiveTasks(year: Int, monthIndex: Int, tasks: List<TaskWithSubTasks>): Boolean {
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, monthIndex)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val monthStart = cal.timeInMillis
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    cal.set(Calendar.DAY_OF_MONTH, daysInMonth)
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    val monthEnd = cal.timeInMillis

    return tasks.any { item ->
        if (item.task.isCompleted) return@any false
        val s = item.task.startDateMillis?.let { DateTimeUtils.getStartOfDay(it) }
        val e = item.task.dueDateMillis?.let { DateTimeUtils.getEndOfDay(it) }
        when {
            s != null && e != null && e >= s -> s <= monthEnd && e >= monthStart
            e != null -> e in monthStart..monthEnd
            s != null -> s in monthStart..monthEnd
            else -> false
        }
    }
}

private val DayCellShape = RoundedCornerShape(14.dp)
private val WeekDayLabels = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")

@Composable
fun MonthCalendarCard(
    year: Int,
    monthIndex: Int,
    dayActivities: Map<Long, CalendarDayActivity> = emptyMap(),
    taskDatesWithActivity: Set<Long> = emptySet(),
    periodDates: Set<Long> = emptySet(),
    selectedDate: Long?,
    semesterConfig: SemesterConfig,
    activeProgramForWeeks: LayerProgram? = null,
    onSelectDate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isDark = LocalDarkTheme.current
    val currentAccent = LocalAccentColor.current
    val effectiveAccent = remember(currentAccent, isDark) {
        if (isDark) brightenColorForDark(currentAccent) else currentAccent
    }

    val progStart = activeProgramForWeeks?.startDateMillis ?: semesterConfig.startDateMillis
    val progEnd = activeProgramForWeeks?.endDateMillis ?: semesterConfig.endDateMillis
    val showSemesterWeeks = (activeProgramForWeeks?.isEnabled == true && progStart != null && progEnd != null) ||
        (semesterConfig.isEnabled && semesterConfig.startDateMillis != null && semesterConfig.endDateMillis != null)

    val currentActiveWeek = remember(semesterConfig, activeProgramForWeeks) {
        if (showSemesterWeeks && progStart != null && progEnd != null) {
            SemesterUtils.getCurrentSemesterWeek(progStart, progEnd)
        } else null
    }

    val monthData = remember(year, monthIndex, semesterConfig, activeProgramForWeeks) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthIndex)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0
        val title = DateTimeUtils.getMonthDisplayName(year, monthIndex)
        val todayStart = DateTimeUtils.getStartOfDay()

        val allDays = (1..daysInMonth).map { d ->
            cal.set(Calendar.DAY_OF_MONTH, d)
            val dMillis = cal.timeInMillis
            MonthDayInfo(
                dayNumber = d,
                dayMillis = dMillis,
                isToday = dMillis == todayStart
            )
        }

        val totalCells = firstDayOfWeek + daysInMonth
        val totalRows = (totalCells + 6) / 7
        val rowsList = ArrayList<MonthWeekRowData>(totalRows)

        for (row in 0 until totalRows) {
            val rowDays = ArrayList<MonthDayInfo?>(7)
            var sampleDay: MonthDayInfo? = null

            for (col in 0..6) {
                val dayIndex = row * 7 + col - firstDayOfWeek
                if (dayIndex in 0 until daysInMonth) {
                    val dayInfo = allDays[dayIndex]
                    rowDays.add(dayInfo)
                    if (sampleDay == null) {
                        sampleDay = dayInfo
                    }
                } else {
                    rowDays.add(null)
                }
            }

            val weekNum = sampleDay?.let {
                if (showSemesterWeeks && progStart != null && progEnd != null) {
                    SemesterUtils.getWeekNumber(it.dayMillis, progStart, progEnd)
                } else null
            }
            val isCurrentWeek = weekNum != null && weekNum == currentActiveWeek

            rowsList.add(
                MonthWeekRowData(
                    weekNum = weekNum,
                    isCurrentWeek = isCurrentWeek,
                    sampleDayMillis = sampleDay?.dayMillis,
                    days = rowDays
                )
            )
        }

        MonthCardData(
            monthTitle = title,
            rows = rowsList
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceContainerLight,
        border = null,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = monthData.monthTitle,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showSemesterWeeks) {
                    Text(
                        text = strings.semesterWeekShort(1).take(1),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = effectiveAccent.copy(alpha = if (isDark) 0.85f else 0.65f),
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.width(34.dp),
                        textAlign = TextAlign.Center
                    )
                }
                WeekDayLabels.forEach { dayName ->
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            for (rowData in monthData.rows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(vertical = 1.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showSemesterWeeks) {
                        val weekNum = rowData.weekNum
                        val isCurrentWeek = rowData.isCurrentWeek
                        Box(
                            modifier = Modifier
                                .width(34.dp)
                                .height(40.dp)
                                .padding(1.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isCurrentWeek -> effectiveAccent.copy(alpha = if (isDark) 0.22f else 0.16f)
                                        weekNum != null -> BgLight
                                        else -> Color.Transparent
                                    }
                                )
                                .then(
                                    if (isCurrentWeek) Modifier.border(1.2.dp, effectiveAccent, RoundedCornerShape(10.dp))
                                    else if (weekNum != null) Modifier.border(0.8.dp, OutlineSubtle, RoundedCornerShape(10.dp))
                                    else Modifier
                                )
                                .expressiveBounce(pressScale = 0.94f)
                                .clickable(enabled = weekNum != null) {
                                    rowData.sampleDayMillis?.let { onSelectDate(it) }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (weekNum != null) {
                                Text(
                                    text = strings.semesterWeekShort(weekNum),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isCurrentWeek) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isCurrentWeek) effectiveAccent else TextSecondary,
                                        fontSize = if (weekNum >= 10) 9.sp else 11.sp,
                                        letterSpacing = if (weekNum >= 10) (-0.5).sp else 0.sp
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    for (day in rowData.days) {
                        if (day != null) {
                            CalendarDayCell(
                                dayNumber = day.dayNumber,
                                dayMillis = day.dayMillis,
                                activity = dayActivities[day.dayMillis],
                                isToday = day.isToday,
                                isSelected = selectedDate != null && selectedDate == day.dayMillis,
                                onSelectDate = onSelectDate,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    dayNumber: Int,
    dayMillis: Long,
    activity: CalendarDayActivity?,
    isToday: Boolean,
    isSelected: Boolean,
    onSelectDate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalDarkTheme.current
    val dockColors = LocalDockColors.current
    val barColor = dockColors.barBackground

    val hasNormalTasks = activity?.hasNormalTasks == true
    val isPeriod = activity?.isPeriod == true
    val programColors = activity?.programColors.orEmpty()
    val hasProgram = programColors.isNotEmpty()
    val isInterference = hasNormalTasks && hasProgram

    val progColor = remember(programColors) {
        if (programColors.isEmpty()) null else blendColorList(programColors)
    }

    val distinctInterferenceColor = remember(progColor, barColor) {
        if (progColor != null) {
            blendColors(progColor, barColor, ratio = 0.5f)
        } else {
            barColor
        }
    }

    // High-contrast brightened colors for dark mode (both standard dark theme and dynamic colors dark theme)
    val textTaskColor = remember(barColor, isDark) {
        if (isDark) brightenColorForDark(barColor) else barColor
    }
    val textProgColor = remember(progColor, isDark) {
        if (progColor != null) {
            if (isDark) brightenColorForDark(progColor) else progColor
        } else null
    }
    val textInterferenceColor = remember(distinctInterferenceColor, isDark) {
        if (isDark) brightenColorForDark(distinctInterferenceColor) else distinctInterferenceColor
    }

    // 1. Background color
    // - Today gets solid fill (barColor)
    // - Interference gets distinct combined shade
    // - Program gets nice shade of program color
    // - Tasks get normal task shade
    // - Empty gets transparent
    val bgColor = when {
        isToday -> barColor
        isInterference -> (if (isDark) textInterferenceColor else distinctInterferenceColor).copy(alpha = if (isDark) 0.22f else 0.26f)
        hasProgram && progColor != null -> (if (isDark) textProgColor ?: progColor else progColor).copy(alpha = 0.20f)
        isPeriod -> (if (isDark) textTaskColor else barColor).copy(alpha = 0.22f)
        hasNormalTasks -> (if (isDark) textTaskColor else barColor).copy(alpha = 0.15f)
        else -> Color.Transparent
    }

    // 2. Text color
    // - Today gets white/iconTint on solid barColor
    // - Interference gets brightened combined color in dark mode
    // - Program gets brightened program color in dark mode
    // - Tasks get brightened barColor in dark mode
    // - Selected without activity gets TextPrimary
    // - Default is TextSecondary
    val textColor = when {
        isToday -> dockColors.iconTint
        isInterference -> textInterferenceColor
        hasProgram && textProgColor != null -> textProgColor
        isPeriod || hasNormalTasks -> textTaskColor
        isSelected -> TextPrimary
        else -> TextSecondary
    }

    // 3. Border highlight
    // - Selected date gets the border-only highlight
    // - If today is also selected, gets high-contrast outline on top of solid fill
    val borderModifier = when {
        isSelected && isToday -> {
            Modifier.border(2.dp, dockColors.iconTint.copy(alpha = 0.9f), DayCellShape)
        }
        isSelected -> {
            Modifier.border(2.dp, if (isDark) textTaskColor else barColor, DayCellShape)
        }
        else -> Modifier
    }

    val cellModifier = if (isSelected) {
        val scale by animateFloatAsState(
            targetValue = 1.06f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = 550f),
            label = "cellScale"
        )
        modifier
            .height(42.dp)
            .padding(2.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    } else {
        modifier
            .height(42.dp)
            .padding(2.dp)
    }

    Box(
        modifier = cellModifier
            .background(bgColor, DayCellShape)
            .then(borderModifier)
            .clickable { onSelectDate(dayMillis) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayNumber.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = if (hasNormalTasks || hasProgram || isToday || isSelected || isPeriod) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },
                color = textColor
            )
        )

        // Activity indicators at the bottom
        if (isToday) {
            when {
                isInterference -> {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(textProgColor ?: dockColors.iconTint)
                        )
                        Box(
                            modifier = Modifier
                                .size(if (isPeriod) 5.dp else 4.dp)
                                .clip(if (isPeriod) RoundedCornerShape(1.5.dp) else CircleShape)
                                .background(dockColors.iconTint)
                        )
                    }
                }
                hasProgram && textProgColor != null -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 3.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(textProgColor)
                    )
                }
                hasNormalTasks || isPeriod -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 3.dp)
                            .size(if (isPeriod) 6.dp else 5.dp)
                            .clip(if (isPeriod) RoundedCornerShape(2.dp) else CircleShape)
                            .background(dockColors.iconTint)
                    )
                }
            }
        } else {
            when {
                isInterference -> {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(textProgColor ?: textTaskColor)
                        )
                        Box(
                            modifier = Modifier
                                .size(if (isPeriod) 5.dp else 4.dp)
                                .clip(if (isPeriod) RoundedCornerShape(1.5.dp) else CircleShape)
                                .background(textTaskColor)
                        )
                    }
                }
                hasProgram && textProgColor != null -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 3.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(textProgColor)
                    )
                }
                hasNormalTasks || isPeriod -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 3.dp)
                            .size(if (isPeriod) 6.dp else 5.dp)
                            .clip(if (isPeriod) RoundedCornerShape(2.dp) else CircleShape)
                            .background(textTaskColor)
                    )
                }
            }
        }
    }
}
