package com.flowtask.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import com.flowtask.app.ui.components.EditTaskBottomSheet
import com.flowtask.app.ui.components.FloatingDockNavBar
import com.flowtask.app.ui.components.InstantCaptureBottomSheet
import com.flowtask.app.ui.components.LayerOptionsDialog
import com.flowtask.app.ui.components.ProjectNoteEditor
import com.flowtask.app.ui.theme.AccentPrimary
import com.flowtask.app.ui.theme.BgLight
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.SurfaceLight
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.viewmodel.InstantViewModel
import com.flowtask.app.ui.viewmodel.ProjectViewModel
import com.flowtask.app.ui.viewmodel.SettingsViewModel
import com.flowtask.app.ui.viewmodel.TaskViewModel
import com.flowtask.app.util.DateTimeUtils
import com.flowtask.app.util.LocalAppStrings
import kotlinx.coroutines.launch

@Composable
fun MainPagerScreen(
    taskViewModel: TaskViewModel,
    projectViewModel: ProjectViewModel,
    settingsViewModel: SettingsViewModel,
    instantViewModel: InstantViewModel
) {
    val context = LocalContext.current
    val screenOrder by settingsViewModel.screenOrder.collectAsStateWithLifecycle()
    val isSettingsVisible by settingsViewModel.isSettingsSheetVisible.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { screenOrder.size }
    )
    val coroutineScope = rememberCoroutineScope()
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current

    val isSheetVisible by taskViewModel.isAddOrEditSheetVisible.collectAsStateWithLifecycle()
    val isSemesterSheetVisible by taskViewModel.isSemesterSheetVisible.collectAsStateWithLifecycle()
    val isCaptureSheetVisible by instantViewModel.isCaptureSheetVisible.collectAsStateWithLifecycle()
    val editingTask by taskViewModel.editingTask.collectAsStateWithLifecycle()
    val defaultDateForNewTask by taskViewModel.defaultDateForNewTask.collectAsStateWithLifecycle()
    val defaultStartDateForNewTask by taskViewModel.defaultStartDateForNewTask.collectAsStateWithLifecycle()
    val availableLayers by taskViewModel.availableLayers.collectAsStateWithLifecycle()
    val selectedLayer by taskViewModel.selectedLayer.collectAsStateWithLifecycle()
    val allTasksViewMode by taskViewModel.allTasksViewMode.collectAsStateWithLifecycle()
    val editingLayer by taskViewModel.editingLayer.collectAsStateWithLifecycle()
    val editingLayerTaskCount by taskViewModel.editingLayerTaskCount.collectAsStateWithLifecycle()
    val lastRenamedLayer by taskViewModel.lastRenamedLayer.collectAsStateWithLifecycle()
    val lastDeletedLayer by taskViewModel.lastDeletedLayer.collectAsStateWithLifecycle()

    val isSearchActive by projectViewModel.isSearchActive.collectAsStateWithLifecycle()
    val searchQuery by projectViewModel.searchQuery.collectAsStateWithLifecycle()
    val isNoteSheetVisible by projectViewModel.isNoteSheetVisible.collectAsStateWithLifecycle()
    val editingNote by projectViewModel.editingProjectNote.collectAsStateWithLifecycle()
    val selectedProject by projectViewModel.selectedProject.collectAsStateWithLifecycle()
    val folderNavigationStack by projectViewModel.folderNavigationStack.collectAsStateWithLifecycle()

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != 0 && isSearchActive) {
            projectViewModel.closeSearch()
        }
    }

    val isAnySheetOpen = isSheetVisible || isSettingsVisible || isSemesterSheetVisible || isCaptureSheetVisible || editingLayer != null || isNoteSheetVisible
    val isPagerBackEnabled = pagerState.currentPage != 0 && !isAnySheetOpen && !isSearchActive

    // Camera Launchers for Quick Snap
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            instantViewModel.onPhotoCaptured()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val (_, uri) = instantViewModel.prepareTempCaptureUri(context)
            cameraLauncher.launch(uri)
        }
    }

    fun launchCamera() {
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            val (_, uri) = instantViewModel.prepareTempCaptureUri(context)
            cameraLauncher.launch(uri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = BgLight,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 22.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.appName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            letterSpacing = (-0.5).sp,
                            color = AccentPrimary
                        )
                    )

                    // Settings Gear Button only (Clean Top Bar)
                    IconButton(
                        onClick = { settingsViewModel.openSettings() },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SurfaceLight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = strings.settingsTitle,
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                var pagerPredictiveBackProgress by remember { mutableFloatStateOf(0f) }
                var pagerPredictiveBackEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }

                PredictiveBackHandler(enabled = isPagerBackEnabled) { progress: Flow<BackEventCompat> ->
                    try {
                        progress.collect { backEvent ->
                            pagerPredictiveBackProgress = backEvent.progress
                            pagerPredictiveBackEdge = backEvent.swipeEdge
                        }
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    } catch (_: CancellationException) {
                    } finally {
                        pagerPredictiveBackProgress = 0f
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 2,
                    key = { pageIndex -> screenOrder.getOrElse(pageIndex) { pageIndex } },
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            if (pagerPredictiveBackProgress > 0f) {
                                val scale = 1f - (pagerPredictiveBackProgress * 0.06f)
                                scaleX = scale
                                scaleY = scale
                                translationX = if (pagerPredictiveBackEdge == BackEventCompat.EDGE_LEFT) {
                                    pagerPredictiveBackProgress * 24.dp.toPx()
                                } else {
                                    -pagerPredictiveBackProgress * 24.dp.toPx()
                                }
                                alpha = 1f - (pagerPredictiveBackProgress * 0.12f)
                            }
                        }
                ) { pageIndex ->
                    val screenId = screenOrder.getOrElse(pageIndex) { 1 }
                    when (screenId) {
                        0 -> ProjectsScreen(viewModel = projectViewModel)
                        1 -> TodayUpcomingScreen(viewModel = taskViewModel)
                        2 -> AllTasksCalendarScreen(viewModel = taskViewModel)
                        3 -> InstantsScreen(viewModel = instantViewModel)
                    }
                }

                val currentScreenId = screenOrder.getOrElse(pagerState.currentPage) { 1 }

                val dockBottomPadding by animateDpAsState(
                    targetValue = if (isSearchActive) 6.dp else 24.dp,
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = 380f
                    ),
                    label = "dockBottomPadding"
                )

                AnimatedVisibility(
                    visible = !isNoteSheetVisible,
                    enter = fadeIn(animationSpec = spring(stiffness = 500f)),
                    exit = fadeOut(animationSpec = spring(stiffness = 500f)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .imePadding()
                        .padding(bottom = dockBottomPadding)
                ) {
                    FloatingDockNavBar(
                        currentPagerIndex = pagerState.currentPage,
                        screenOrder = screenOrder,
                        onSelectPageIndex = { targetIndex ->
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(targetIndex)
                            }
                        },
                        onCreateClick = {
                            when (currentScreenId) {
                                0 -> projectViewModel.openCreateProjectDialog()
                                1 -> taskViewModel.openAddTask(DateTimeUtils.getStartOfDay())
                                2 -> taskViewModel.openAddTask(null)
                                3 -> instantViewModel.openCreateFolderDialog()
                            }
                        },
                        onSnapPhotoClick = {
                            launchCamera()
                        },
                        onNavigateToToday = {
                            val todayIndex = screenOrder.indexOf(1)
                            val allTasksIndex = screenOrder.indexOf(2)
                            if (pagerState.currentPage == allTasksIndex && allTasksViewMode == "SMART_CALENDAR") {
                                taskViewModel.triggerScrollToTodayCalendar()
                            } else if (todayIndex >= 0 && pagerState.currentPage != todayIndex) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(todayIndex)
                                }
                            }
                        },
                        availableLayers = if (currentScreenId in listOf(1, 2)) availableLayers else emptyList(),
                        selectedLayer = selectedLayer,
                        onSelectLayer = { taskViewModel.selectLayer(it) },
                        onLongClickLayer = { taskViewModel.openEditLayer(it) },
                        isSearchActive = isSearchActive,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { projectViewModel.setSearchQuery(it) },
                        onOpenSearch = { projectViewModel.openSearch() },
                        onCloseSearch = { projectViewModel.closeSearch() }
                    )
                }
            }
        }

        // Edit/Add Task Bottom Sheet (Placed OUTSIDE Scaffold to overlay App Title and Settings gear)
        if (isSheetVisible) {
            EditTaskBottomSheet(
                taskWithSubTasks = editingTask,
                defaultDateMillis = defaultDateForNewTask,
                defaultStartDateMillis = defaultStartDateForNewTask,
                availableLayers = availableLayers,
                onDismiss = { taskViewModel.closeTaskSheet() },
                onSave = { id, text, dateMillis, timeMinutes, layer, subtasks, startDateMillis ->
                    taskViewModel.saveTask(id, text, dateMillis, timeMinutes, layer, subtasks, startDateMillis)
                },
                onLongClickLayer = { taskViewModel.openEditLayer(it) },
                lastRenamedLayer = lastRenamedLayer,
                lastDeletedLayer = lastDeletedLayer
            )
        }

        // Settings Bottom Sheet
        if (isSettingsVisible) {
            SettingsBottomSheet(
                viewModel = settingsViewModel,
                taskViewModel = taskViewModel,
                onOpenSemesterSettings = { taskViewModel.openSemesterSheet() },
                onDismiss = { settingsViewModel.closeSettings() }
            )
        }

        // Semester & Academic Program Bottom Sheet
        if (isSemesterSheetVisible) {
            SemesterProgramBottomSheet(
                viewModel = taskViewModel,
                onDismiss = { taskViewModel.closeSemesterSheet() }
            )
        }

        // Instant Capture Bottom Sheet (Note input & Google Photos style folder cards after 4:3 shoot)
        val pendingCapturePath by instantViewModel.pendingCapturePath.collectAsStateWithLifecycle()

        if (isCaptureSheetVisible && pendingCapturePath != null) {
            InstantCaptureBottomSheet(
                viewModel = instantViewModel,
                imagePath = pendingCapturePath!!,
                onDismiss = { instantViewModel.dismissCaptureSheet() }
            )
        }

        // Layer Options Dialog (Rename & Delete)
        if (editingLayer != null) {
            LayerOptionsDialog(
                layerName = editingLayer!!,
                taskCount = editingLayerTaskCount,
                onDismiss = { taskViewModel.closeEditLayer() },
                onRename = { oldName, newName -> taskViewModel.renameLayer(oldName, newName) },
                onDelete = { name, deleteTasks -> taskViewModel.deleteLayer(name, deleteTasks) }
            )
        }

        // Project Note Editor (In-screen fluid morphing bottom card & full-screen canvas)
        val noteOriginBounds by projectViewModel.noteOriginBounds.collectAsStateWithLifecycle()

        if (isNoteSheetVisible) {
            ProjectNoteEditor(
                note = editingNote,
                projectTitle = selectedProject?.name ?: "",
                folderPath = folderNavigationStack.lastOrNull()?.name,
                originBounds = noteOriginBounds,
                onDismiss = { projectViewModel.closeNoteSheet() },
                onAutoSave = { id, title, content ->
                    projectViewModel.saveProjectNote(id, title, content, closeOnSave = false)
                },
                onSave = { id, title, content ->
                    projectViewModel.saveProjectNote(id, title, content, closeOnSave = true)
                }
            )
        }
    }
}
