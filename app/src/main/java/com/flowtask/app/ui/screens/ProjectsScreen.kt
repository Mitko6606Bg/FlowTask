package com.flowtask.app.ui.screens
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.Spring
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.platform.LocalDensity

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.flowtask.app.ui.components.ProjectActionPopup
import com.flowtask.app.ui.components.ProjectDetailsBottomSheet
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import com.flowtask.app.ui.components.ProjectNoteEditor
import com.flowtask.app.ui.components.NoteBlockType
import com.flowtask.app.ui.components.NoteRichParser
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFolderUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.ui.state.ToggleableState
import com.flowtask.app.data.local.entity.ProjectEntity
import com.flowtask.app.data.local.entity.ProjectFolderEntity
import com.flowtask.app.data.local.entity.ProjectNoteEntity
import com.flowtask.app.data.local.model.ProjectTaskWithSubTasks
import com.flowtask.app.ui.components.AnimatedCheckbox
import com.flowtask.app.ui.components.calculateSubtaskToggleableState
import com.flowtask.app.ui.components.AppDropdownMenu
import com.flowtask.app.ui.components.AppDropdownMenuItem
import com.flowtask.app.ui.components.SubtaskProgressChip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import com.flowtask.app.ui.theme.LocalDockColors
import com.flowtask.app.ui.theme.AccentPrimary
import com.flowtask.app.ui.theme.BgLight
import com.flowtask.app.ui.theme.DockBarColor
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.OutlineVariant
import com.flowtask.app.ui.theme.SurfaceContainerHighest
import com.flowtask.app.ui.theme.SurfaceLight
import com.flowtask.app.ui.theme.SurfaceContainerLow
import com.flowtask.app.ui.theme.SurfaceContainerLowest
import com.flowtask.app.ui.theme.TextMuted
import com.flowtask.app.ui.theme.TextOnAccent
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.ui.theme.getM3TonalProjectColor
import com.flowtask.app.ui.viewmodel.FolderMatch
import com.flowtask.app.ui.viewmodel.MoveItemTarget
import com.flowtask.app.ui.viewmodel.ProjectFilterType
import com.flowtask.app.ui.viewmodel.ProjectSearchMatch
import com.flowtask.app.ui.viewmodel.ProjectViewModel
import com.flowtask.app.util.LocalAppStrings

@Composable
fun ProjectsScreen(
    viewModel: ProjectViewModel,
    modifier: Modifier = Modifier
) {
    val selectedProject by viewModel.selectedProject.collectAsState()
    val folderStack by viewModel.folderNavigationStack.collectAsState()

    val isCreateProjectDialogVisible by viewModel.isCreateProjectDialogVisible.collectAsState()
    val isCreateFolderDialogVisible by viewModel.isCreateFolderDialogVisible.collectAsState()
    val isTaskSheetVisible by viewModel.isTaskSheetVisible.collectAsState()
    val editingTask by viewModel.editingProjectTask.collectAsState()
    val moveTarget by viewModel.moveTarget.collectAsState()
    val editingProject by viewModel.editingProject.collectAsState()
    val renamingFolder by viewModel.renamingFolder.collectAsState()
    val allProjectFolders by viewModel.allProjectFolders.collectAsState()

    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    var predictiveBackEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }

    PredictiveBackHandler(enabled = selectedProject != null) { progress: Flow<BackEventCompat> ->
        try {
            progress.collect { backEvent ->
                predictiveBackProgress = backEvent.progress
                predictiveBackEdge = backEvent.swipeEdge
            }
            viewModel.navigateUpFolder()
        } catch (_: CancellationException) {
        } finally {
            predictiveBackProgress = 0f
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        if (selectedProject == null) {
            ProjectsDashboardView(viewModel = viewModel)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val scale = 1f - (predictiveBackProgress * 0.08f)
                        scaleX = scale
                        scaleY = scale
                        translationX = if (predictiveBackEdge == BackEventCompat.EDGE_LEFT) {
                            predictiveBackProgress * 28.dp.toPx()
                        } else {
                            -predictiveBackProgress * 28.dp.toPx()
                        }
                        alpha = 1f - (predictiveBackProgress * 0.18f)
                    }
            ) {
                ProjectDetailView(
                    project = selectedProject!!,
                    folderStack = folderStack,
                    viewModel = viewModel
                )
            }
        }

        // Bottom Sheets wrapped in null overscroll for smooth gestures
        CompositionLocalProvider(LocalOverscrollFactory provides null) {
            if (isCreateProjectDialogVisible) {
                NewProjectBottomSheet(
                    onDismiss = { viewModel.closeCreateProjectDialog() },
                    onCreate = { name, desc, color, icon, isPinned ->
                        viewModel.createProject(name, desc, color, icon, isPinned)
                    }
                )
            }

            editingProject?.let { proj ->
                EditProjectBottomSheet(
                    project = proj,
                    onDismiss = { viewModel.closeEditProjectDialog() },
                    onSave = { id, name, desc, color, icon, isPinned ->
                        viewModel.updateProject(id, name, desc, color, icon, isPinned)
                    }
                )
            }

            if (isCreateFolderDialogVisible) {
                CreateFolderBottomSheet(
                    onDismiss = { viewModel.closeCreateFolderDialog() },
                    onCreate = { name, color ->
                        viewModel.createFolder(name, color)
                    }
                )
            }

            renamingFolder?.let { folder ->
                RenameFolderBottomSheet(
                    folder = folder,
                    onDismiss = { viewModel.closeRenameFolderDialog() },
                    onSave = { id, name, color ->
                        viewModel.renameFolder(id, name, color)
                    }
                )
            }

            moveTarget?.let { target ->
                val currentFolderId = when (target) {
                    is MoveItemTarget.Task -> target.taskWithSubTasks.task.folderId
                    is MoveItemTarget.Note -> target.note.folderId
                    is MoveItemTarget.Folder -> target.folder.parentFolderId
                }
                MoveToFolderBottomSheet(
                    target = target,
                    allFolders = allProjectFolders,
                    currentFolderId = currentFolderId,
                    onDismiss = { viewModel.closeMoveDialog() },
                    onSelectFolder = { targetFolderId ->
                        viewModel.moveItemToFolder(targetFolderId)
                    }
                )
            }

            if (isTaskSheetVisible) {
                ProjectTaskBottomSheet(
                    taskWithSubTasks = editingTask,
                    onDismiss = { viewModel.closeTaskSheet() },
                    onSave = { id, text, subtasks ->
                        viewModel.saveProjectTask(id, text, subtasks)
                    }
                )
            }
        }
    }
}

@Composable
fun MatchingFoldersContainer(
    match: ProjectSearchMatch,
    projectColor: Color,
    onFolderClick: (FolderMatch) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 4.dp),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceContainerLow,
        border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.55f)),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = projectColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = strings.matchingFoldersSubtitle(match.matchingFolders.size),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            match.matchingFolders.forEachIndexed { index, folderMatch ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(projectColor.copy(alpha = 0.07f))
                        .clickable { onFolderClick(folderMatch) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(projectColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = projectColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = folderMatch.folder.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (folderMatch.pathString.isNotEmpty() && folderMatch.pathString != folderMatch.folder.name) {
                            Text(
                                text = folderMatch.pathString,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = strings.openFolderDirectly,
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Projects Dashboard (Overview of all projects)
// ---------------------------------------------------------------------------
@Composable
fun ProjectsDashboardView(
    viewModel: ProjectViewModel
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val projects by viewModel.projects.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var pinnedItems by remember { mutableStateOf(projects.filter { it.isPinned }) }
    var otherItems by remember { mutableStateOf(projects.filter { !it.isPinned }) }

    var activeDragId by remember { mutableStateOf<Long?>(null) }
    var isDraggingPinned by remember { mutableStateOf(false) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    // Sync from database only when not dragging
    LaunchedEffect(projects) {
        if (activeDragId == null) {
            pinnedItems = projects.filter { it.isPinned }
            otherItems = projects.filter { !it.isPinned }
        }
    }

    // Standard card height + spacing is approx 92dp
    val itemHeightPx = with(density) { 92.dp.toPx() }
    val swapThreshold = itemHeightPx * 0.45f

    fun onDragFinish() {
        if (activeDragId != null) {
            val fullList = pinnedItems + otherItems
            viewModel.reorderProjects(fullList)
            activeDragId = null
            dragOffsetY = 0f
        }
    }

    fun handleDragDelta(deltaY: Float) {
        dragOffsetY += deltaY
        if (isDraggingPinned) {
            var currentIndex = pinnedItems.indexOfFirst { it.id == activeDragId }
            if (currentIndex != -1) {
                while (dragOffsetY > swapThreshold && currentIndex < pinnedItems.lastIndex) {
                    val mutable = pinnedItems.toMutableList()
                    val item = mutable.removeAt(currentIndex)
                    mutable.add(currentIndex + 1, item)
                    pinnedItems = mutable
                    dragOffsetY -= itemHeightPx
                    currentIndex += 1
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                while (dragOffsetY < -swapThreshold && currentIndex > 0) {
                    val mutable = pinnedItems.toMutableList()
                    val item = mutable.removeAt(currentIndex)
                    mutable.add(currentIndex - 1, item)
                    pinnedItems = mutable
                    dragOffsetY += itemHeightPx
                    currentIndex -= 1
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                if (currentIndex == 0 && dragOffsetY < 0f) {
                    dragOffsetY = dragOffsetY.coerceAtLeast(-itemHeightPx * 0.7f)
                } else if (currentIndex == pinnedItems.lastIndex && dragOffsetY > 0f) {
                    dragOffsetY = dragOffsetY.coerceAtMost(itemHeightPx * 0.7f)
                }
            }
        } else {
            var currentIndex = otherItems.indexOfFirst { it.id == activeDragId }
            if (currentIndex != -1) {
                while (dragOffsetY > swapThreshold && currentIndex < otherItems.lastIndex) {
                    val mutable = otherItems.toMutableList()
                    val item = mutable.removeAt(currentIndex)
                    mutable.add(currentIndex + 1, item)
                    otherItems = mutable
                    dragOffsetY -= itemHeightPx
                    currentIndex += 1
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                while (dragOffsetY < -swapThreshold && currentIndex > 0) {
                    val mutable = otherItems.toMutableList()
                    val item = mutable.removeAt(currentIndex)
                    mutable.add(currentIndex - 1, item)
                    otherItems = mutable
                    dragOffsetY += itemHeightPx
                    currentIndex -= 1
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                if (currentIndex == 0 && dragOffsetY < 0f) {
                    dragOffsetY = dragOffsetY.coerceAtLeast(-itemHeightPx * 0.7f)
                } else if (currentIndex == otherItems.lastIndex && dragOffsetY > 0f) {
                    dragOffsetY = dragOffsetY.coerceAtMost(itemHeightPx * 0.7f)
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "projects_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.projectsHeader,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = strings.projectsSubtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                val dockColors = LocalDockColors.current
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .expressiveBounce(pressScale = 0.92f)
                        .clickable { viewModel.openCreateProjectDialog() },
                    shape = RoundedCornerShape(16.dp),
                    color = dockColors.barBackground,
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = strings.newProjectTitle,
                            tint = dockColors.iconTint,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        if (searchQuery.isNotBlank()) {
            item(key = "search_results_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = currentAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.searchResultsHeader(searchResults.size),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                    )
                }
            }

            if (searchResults.isEmpty()) {
                item(key = "search_no_results") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceLight)
                            .border(1.dp, OutlineSubtle.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = strings.searchNoResultsTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = strings.searchNoResultsSubtitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            } else {
                items(searchResults, key = { "search_match_${it.project.id}" }) { match ->
                    val projectColor = getM3TonalProjectColor(match.project.colorHex)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                    ) {
                        ProjectCard(
                            project = match.project,
                            onClick = { viewModel.selectProject(match.project) },
                            onViewDetails = { viewModel.openProjectDetails(match.project) },
                            onTogglePin = { viewModel.toggleProjectPinned(match.project) },
                            onEdit = { viewModel.openEditProjectDialog(match.project) },
                            onDelete = { viewModel.deleteProject(match.project.id) }
                        )

                        if (match.matchingFolders.isNotEmpty()) {
                            MatchingFoldersContainer(
                                match = match,
                                projectColor = projectColor,
                                onFolderClick = { folderMatch ->
                                    viewModel.selectProjectAndNavigateToFolder(
                                        match.project,
                                        folderMatch.hierarchy
                                    )
                                }
                            )
                        }
                    }
                }
            }
        } else if (projects.isEmpty()) {
            item(key = "projects_empty") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceLight)
                        .border(1.dp, OutlineSubtle.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                        .padding(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = currentAccent,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = strings.projectsEmptyTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = strings.projectsEmptySubtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        } else if (projects.isEmpty()) {
            item(key = "projects_empty") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceLight)
                        .border(1.dp, OutlineSubtle.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                        .padding(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = currentAccent,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = strings.projectsEmptyTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = strings.projectsEmptySubtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        } else {
            // Pinned Section (Reordered independently)
            if (pinnedItems.isNotEmpty()) {
                item(key = "pinned_section_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = null,
                            tint = currentAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.pinnedProjectsSection(pinnedItems.size),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        )
                    }
                }

                items(pinnedItems, key = { it.id }) { project ->
                    val isDragging = activeDragId == project.id
                    ProjectCard(
                        project = project,
                        isDragging = isDragging,
                        dragOffsetY = if (isDragging) dragOffsetY else 0f,
                        onDragStart = {
                            activeDragId = project.id
                            isDraggingPinned = true
                            dragOffsetY = 0f
                        },
                        onDrag = { deltaY -> handleDragDelta(deltaY) },
                        onDragEnd = { onDragFinish() },
                        onDragCancel = { onDragFinish() },
                        onClick = { viewModel.selectProject(project) },
                        onViewDetails = { viewModel.openProjectDetails(project) },
                        onTogglePin = { viewModel.toggleProjectPinned(project) },
                        onEdit = { viewModel.openEditProjectDialog(project) },
                        onDelete = { viewModel.deleteProject(project.id) },
                        modifier = if (isDragging) Modifier else Modifier.animateItem()
                    )
                }

                if (otherItems.isNotEmpty()) {
                    item(key = "all_projects_section_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.allProjectsSection(otherItems.size),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            )
                        }
                    }

                    items(otherItems, key = { it.id }) { project ->
                        val isDragging = activeDragId == project.id
                        ProjectCard(
                            project = project,
                            isDragging = isDragging,
                            dragOffsetY = if (isDragging) dragOffsetY else 0f,
                            onDragStart = {
                                activeDragId = project.id
                                isDraggingPinned = false
                                dragOffsetY = 0f
                            },
                            onDrag = { deltaY -> handleDragDelta(deltaY) },
                            onDragEnd = { onDragFinish() },
                            onDragCancel = { onDragFinish() },
                            onClick = { viewModel.selectProject(project) },
                            onViewDetails = { viewModel.openProjectDetails(project) },
                            onTogglePin = { viewModel.toggleProjectPinned(project) },
                            onEdit = { viewModel.openEditProjectDialog(project) },
                            onDelete = { viewModel.deleteProject(project.id) },
                            modifier = if (isDragging) Modifier else Modifier.animateItem()
                        )
                    }
                }
            } else {
                items(otherItems, key = { it.id }) { project ->
                    val isDragging = activeDragId == project.id
                    ProjectCard(
                        project = project,
                        isDragging = isDragging,
                        dragOffsetY = if (isDragging) dragOffsetY else 0f,
                        onDragStart = {
                            activeDragId = project.id
                            isDraggingPinned = false
                            dragOffsetY = 0f
                        },
                        onDrag = { deltaY -> handleDragDelta(deltaY) },
                        onDragEnd = { onDragFinish() },
                        onDragCancel = { onDragFinish() },
                        onClick = { viewModel.selectProject(project) },
                        onViewDetails = { viewModel.openProjectDetails(project) },
                        onTogglePin = { viewModel.toggleProjectPinned(project) },
                        onEdit = { viewModel.openEditProjectDialog(project) },
                        onDelete = { viewModel.deleteProject(project.id) },
                        modifier = if (isDragging) Modifier else Modifier.animateItem()
                    )
                }
            }
        }

        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ProjectCard(
    project: ProjectEntity,
    isDragging: Boolean = false,
    dragOffsetY: Float = 0f,
    onDragStart: () -> Unit = {},
    onDrag: (Float) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
    onClick: () -> Unit,
    onViewDetails: () -> Unit,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }

    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnDragCancel by rememberUpdatedState(onDragCancel)

    val projectColor = getM3TonalProjectColor(project.colorHex)

    val iconVector = when (project.iconName) {
        "rocket" -> Icons.Default.RocketLaunch
        "lightbulb" -> Icons.Default.Lightbulb
        "work" -> Icons.Default.Work
        "palette" -> Icons.Default.Palette
        "code" -> Icons.Default.Code
        "star" -> Icons.Default.Star
        "bookmark" -> Icons.Default.Bookmark
        else -> Icons.Default.Folder
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (isDragging) 1.035f else 1f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 450f
        ),
        label = "dragScale"
    )

    val animatedElevation by animateDpAsState(
        targetValue = if (isDragging) 14.dp else 1.dp,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 450f
        ),
        label = "dragElevation"
    )

    val animatedBorderAlpha by animateFloatAsState(
        targetValue = if (isDragging) 0.85f else 0.28f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 450f
        ),
        label = "dragBorderAlpha"
    )

    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isDragging) 2.dp else 1.2.dp,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 450f
        ),
        label = "dragBorderWidth"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 10f else 0f)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                translationY = if (isDragging) dragOffsetY else 0f
            }
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .pointerInput(project.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentOnDragStart()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentOnDrag(dragAmount.y)
                    },
                    onDragEnd = { currentOnDragEnd() },
                    onDragCancel = { currentOnDragCancel() }
                )
            },
        shape = RoundedCornerShape(22.dp),
        color = SurfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(
            width = animatedBorderWidth,
            color = projectColor.copy(alpha = animatedBorderAlpha)
        ),
        shadowElevation = animatedElevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(projectColor.copy(alpha = if (isDragging) 0.08f else 0.05f))
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left color accent stripe
            Box(
                modifier = Modifier
                    .width(4.5.dp)
                    .height(32.dp)
                    .clip(CircleShape)
                    .background(projectColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Squircle Hero Icon badge
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(projectColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = projectColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Project Title + Gold Pin Badge
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (project.isPinned) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(currentAccent.copy(alpha = 0.16f))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTogglePin()
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = strings.projectPinnedBadge,
                            tint = currentAccent,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Trailing 3-dot menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                AppDropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    AppDropdownMenuItem(
                        text = { Text(strings.openAction, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary)) },
                        leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showMenu = false
                            onClick()
                        }
                    )
                    AppDropdownMenuItem(
                        text = { Text(strings.viewDetailsAction, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary)) },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showMenu = false
                            onViewDetails()
                        }
                    )
                    AppDropdownMenuItem(
                        text = { Text(if (project.isPinned) strings.unpinProjectAction else strings.pinProjectAction, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary)) },
                        leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null, tint = if (project.isPinned) currentAccent else TextSecondary, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showMenu = false
                            onTogglePin()
                        }
                    )
                    AppDropdownMenuItem(
                        text = { Text(strings.editProjectAction, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary)) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )
                    AppDropdownMenuItem(
                        text = { Text(strings.deleteProjectTitle, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)) },
                        leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectDetailView(
    project: ProjectEntity,
    folderStack: List<ProjectFolderEntity>,
    viewModel: ProjectViewModel
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val projectColor = getM3TonalProjectColor(project.colorHex)
    val folders by viewModel.currentFolders.collectAsState()
    val tasks by viewModel.currentTasks.collectAsState()
    val notes by viewModel.currentNotes.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val folderItemCounts by viewModel.folderItemCounts.collectAsState()

    val currentFolder = folderStack.lastOrNull()
    val totalItemsAtLevel = folders.size + tasks.size + notes.size
    var showProjectMenu by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    var orderedNotes by remember { mutableStateOf(notes) }
    var activeDragNoteId by remember { mutableStateOf<Long?>(null) }
    var noteDragOffsetY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(notes) {
        if (activeDragNoteId == null) {
            orderedNotes = notes
        }
    }

    val noteItemHeightPx = with(density) { 88.dp.toPx() }
    val noteSwapThreshold = noteItemHeightPx * 0.45f

    fun onNoteDragFinish() {
        if (activeDragNoteId != null) {
            viewModel.reorderProjectNotes(orderedNotes)
            activeDragNoteId = null
            noteDragOffsetY = 0f
        }
    }

    fun handleNoteDragDelta(deltaY: Float) {
        noteDragOffsetY += deltaY
        var currentIndex = orderedNotes.indexOfFirst { it.id == activeDragNoteId }
        if (currentIndex != -1) {
            while (noteDragOffsetY > noteSwapThreshold && currentIndex < orderedNotes.lastIndex) {
                val mutable = orderedNotes.toMutableList()
                val item = mutable.removeAt(currentIndex)
                mutable.add(currentIndex + 1, item)
                orderedNotes = mutable
                noteDragOffsetY -= noteItemHeightPx
                currentIndex += 1
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            while (noteDragOffsetY < -noteSwapThreshold && currentIndex > 0) {
                val mutable = orderedNotes.toMutableList()
                val item = mutable.removeAt(currentIndex)
                mutable.add(currentIndex - 1, item)
                orderedNotes = mutable
                noteDragOffsetY += noteItemHeightPx
                currentIndex -= 1
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            if (currentIndex == 0 && noteDragOffsetY < 0f) {
                noteDragOffsetY = noteDragOffsetY.coerceAtLeast(-noteItemHeightPx * 0.7f)
            } else if (currentIndex == orderedNotes.lastIndex && noteDragOffsetY > 0f) {
                noteDragOffsetY = noteDragOffsetY.coerceAtMost(noteItemHeightPx * 0.7f)
            }
        }
    }

    fun moveNoteUp(index: Int) {
        if (index > 0) {
            val mutable = orderedNotes.toMutableList()
            val item = mutable.removeAt(index)
            mutable.add(index - 1, item)
            orderedNotes = mutable
            viewModel.reorderProjectNotes(mutable)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    fun moveNoteDown(index: Int) {
        if (index < orderedNotes.size - 1) {
            val mutable = orderedNotes.toMutableList()
            val item = mutable.removeAt(index)
            mutable.add(index + 1, item)
            orderedNotes = mutable
            viewModel.reorderProjectNotes(mutable)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val showFolders = (filterType == ProjectFilterType.ALL || filterType == ProjectFilterType.FOLDERS) && folders.isNotEmpty()
    val showTasks = (filterType == ProjectFilterType.ALL || filterType == ProjectFilterType.TASKS) && tasks.isNotEmpty()
    val showNotes = (filterType == ProjectFilterType.ALL || filterType == ProjectFilterType.NOTES) && notes.isNotEmpty()
    val isDirectoryEmpty = folders.isEmpty() && tasks.isEmpty() && notes.isEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        // Top Header Bar: Back Button, Current Folder / Project Title, Item count, and Options Menu
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateUpFolder() },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerLow)
                        .border(androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.55f)), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentFolder?.name ?: project.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = strings.itemsCount(totalItemsAtLevel),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    // Compact Breadcrumb Path Container tucked under the title
                    if (folderStack.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceContainerLow,
                            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.5f))
                        ) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { viewModel.navigateToBreadcrumb(-1) }
                                            .padding(horizontal = 5.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Home,
                                            contentDescription = null,
                                            tint = projectColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = project.name,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp,
                                                color = projectColor
                                            )
                                        )
                                    }
                                }

                                itemsIndexed(folderStack) { index, folder ->
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    val isCurrent = index == folderStack.size - 1
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isCurrent) projectColor.copy(alpha = 0.14f) else Color.Transparent)
                                            .clickable { viewModel.navigateToBreadcrumb(index) }
                                            .padding(horizontal = 5.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = if (isCurrent) projectColor else TextSecondary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = folder.name,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 11.sp,
                                                color = if (isCurrent) projectColor else TextPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3-dot overflow menu for project/folder management
                Box {
                    IconButton(
                        onClick = { showProjectMenu = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    AppDropdownMenu(
                        expanded = showProjectMenu,
                        onDismissRequest = { showProjectMenu = false }
                    ) {
                        if (currentFolder == null) {
                            AppDropdownMenuItem(
                                text = { Text(strings.editProjectAction, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary)) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showProjectMenu = false
                                    viewModel.openEditProjectDialog(project)
                                }
                            )
                            AppDropdownMenuItem(
                                text = { Text(strings.deleteProjectTitle, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)) },
                                leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showProjectMenu = false
                                    viewModel.deleteProject(project.id)
                                }
                            )
                        } else {
                            AppDropdownMenuItem(
                                text = { Text(strings.renameFolderAction, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary)) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showProjectMenu = false
                                    viewModel.openRenameFolderDialog(currentFolder)
                                }
                            )
                            AppDropdownMenuItem(
                                text = { Text(strings.moveToFolderAction, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary)) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showProjectMenu = false
                                    viewModel.openMoveDialog(MoveItemTarget.Folder(currentFolder))
                                }
                            )
                            AppDropdownMenuItem(
                                text = { Text(strings.deleteFolderTitle, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)) },
                                leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showProjectMenu = false
                                    viewModel.deleteFolder(currentFolder.id)
                                }
                            )
                        }
                    }
                }
            }
        }



        // Filter Pills Toolbar (Only shown if directory has items)
        if (!isDirectoryEmpty) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FileFilterChip(
                            label = "${strings.filterAll} ($totalItemsAtLevel)",
                            selected = filterType == ProjectFilterType.ALL,
                            onClick = { viewModel.setFilterType(ProjectFilterType.ALL) }
                        )
                        if (folders.isNotEmpty()) {
                            FileFilterChip(
                                label = "${strings.filterFolders} (${folders.size})",
                                selected = filterType == ProjectFilterType.FOLDERS,
                                onClick = { viewModel.setFilterType(ProjectFilterType.FOLDERS) }
                            )
                        }
                        if (tasks.isNotEmpty()) {
                            FileFilterChip(
                                label = "${strings.filterTasks} (${tasks.size})",
                                selected = filterType == ProjectFilterType.TASKS,
                                onClick = { viewModel.setFilterType(ProjectFilterType.TASKS) }
                            )
                        }
                        if (notes.isNotEmpty()) {
                            FileFilterChip(
                                label = "${strings.filterNotes} (${notes.size})",
                                selected = filterType == ProjectFilterType.NOTES,
                                onClick = { viewModel.setFilterType(ProjectFilterType.NOTES) }
                            )
                        }
                    }
                }
            }
        }

        // Empty Directory State (Unified Windows File Manager empty state)
        if (isDirectoryEmpty) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(currentAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = currentAccent,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = strings.folderEmptyStateTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = strings.folderEmptyStateSubtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        )
                    }
                }
            }
        }

        // Folders Section (Rendered dynamically if folders exist)
        // Folders Section (Rendered dynamically if folders exist)
        if (showFolders) {
            if (filterType == ProjectFilterType.ALL && (tasks.isNotEmpty() || notes.isNotEmpty())) {
                item(key = "folders_header") {
                    Text(
                        text = "${strings.filterFolders} • ${folders.size}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        ),
                        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                    )
                }
            }

            items(folders, key = { "folder_${it.id}" }) { folder ->
                val count = folderItemCounts[folder.id] ?: 0
                FolderCard(
                    folder = folder,
                    itemCount = count,
                    onClick = { viewModel.openFolder(folder) },
                    onRename = { viewModel.openRenameFolderDialog(folder) },
                    onMove = { viewModel.openMoveDialog(MoveItemTarget.Folder(folder)) },
                    onDelete = { viewModel.deleteFolder(folder.id) }
                )
            }
        }

        // Tasks Section (Rendered dynamically if tasks exist)
        if (showTasks) {
            if (filterType == ProjectFilterType.ALL && (folders.isNotEmpty() || notes.isNotEmpty())) {
                item(key = "tasks_header") {
                    Text(
                        text = "${strings.filterTasks} • ${tasks.size}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        ),
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }
            }

            items(tasks, key = { "task_${it.task.id}" }) { taskWithSubTasks ->
                ProjectTaskCard(
                    taskWithSubTasks = taskWithSubTasks,
                    onToggleTask = { id, done -> viewModel.toggleProjectTaskCompletion(id, done) },
                    onToggleSubTask = { id, done -> viewModel.toggleProjectSubTaskCompletion(id, done) },
                    onEditTask = { viewModel.openEditTaskSheet(it) },
                    onMoveTask = { viewModel.openMoveDialog(MoveItemTarget.Task(taskWithSubTasks)) },
                    onDeleteTask = { viewModel.deleteProjectTask(it) },
                    onDeleteSubTask = { viewModel.deleteProjectSubTask(it) },
                    onAddSubTask = { id, text -> viewModel.addProjectSubTask(id, text) }
                )
            }
        }

        // Notes Section (Rendered dynamically if notes exist)
        if (showNotes) {
            if (filterType == ProjectFilterType.ALL && (folders.isNotEmpty() || tasks.isNotEmpty())) {
                item(key = "notes_header") {
                    Text(
                        text = "${strings.filterNotes} • ${notes.size}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        ),
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }
            }

            itemsIndexed(orderedNotes, key = { _, note -> "note_${note.id}" }) { index, note ->
                val isDragging = activeDragNoteId == note.id
                ProjectNoteCard(
                    note = note,
                    canMoveUp = index > 0,
                    canMoveDown = index < orderedNotes.size - 1,
                    onMoveUp = { moveNoteUp(index) },
                    onMoveDown = { moveNoteDown(index) },
                    isDragging = isDragging,
                    dragOffsetY = if (isDragging) noteDragOffsetY else 0f,
                    onDragStart = {
                        activeDragNoteId = note.id
                        noteDragOffsetY = 0f
                    },
                    onDrag = { deltaY -> handleNoteDragDelta(deltaY) },
                    onDragEnd = { onNoteDragFinish() },
                    onDragCancel = { onNoteDragFinish() },
                    onEdit = { bounds -> viewModel.openEditNoteSheet(note, bounds) },
                    onMove = { viewModel.openMoveDialog(MoveItemTarget.Note(note)) },
                    onDelete = { viewModel.deleteProjectNote(note.id) },
                    modifier = if (isDragging) Modifier else Modifier.animateItem()
                )
            }
        }

        item(key = "project_detail_bottom_spacer") {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

        // M3 Expressive Multi-Action Speed Dial FAB (Floats on the right of the dock)
        ProjectSpeedDial(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 96.dp),
            onCreateFolder = { viewModel.openCreateFolderDialog() },
            onCreateTask = { viewModel.openCreateTaskSheet() },
            onCreateNote = { bounds -> viewModel.openCreateNoteSheet(bounds) }
        )
    }
}

// ---------------------------------------------------------------------------
// M3 Expressive Multi-Action Speed Dial (Vertical Pop-up for Project Actions)
// ---------------------------------------------------------------------------
@Composable
fun ProjectSpeedDial(
    modifier: Modifier = Modifier,
    onCreateFolder: () -> Unit,
    onCreateTask: () -> Unit,
    onCreateNote: (Rect?) -> Unit
) {
    val strings = LocalAppStrings.current
    val dockColors = LocalDockColors.current
    val currentAccent = LocalAccentColor.current
    val haptic = LocalHapticFeedback.current
    var isExpanded by remember { mutableStateOf(false) }
    var fabBounds by remember { mutableStateOf<Rect?>(null) }

    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "fabRotation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Pop-up options stacked vertically
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(150)) +
                    slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) +
                    scaleIn(
                        initialScale = 0.8f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ),
            exit = fadeOut(animationSpec = tween(100)) +
                    slideOutVertically(
                        targetOffsetY = { it / 2 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) +
                    scaleOut(targetScale = 0.8f)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Create Note Option
                SpeedDialActionItem(
                    label = strings.newNoteTitle,
                    icon = Icons.Default.Edit,
                    accentColor = dockColors.barBackground,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isExpanded = false
                        onCreateNote(fabBounds)
                    }
                )

                // 2. Create Task Option
                SpeedDialActionItem(
                    label = strings.newTaskTitle,
                    icon = Icons.AutoMirrored.Filled.PlaylistAddCheck,
                    accentColor = dockColors.barBackground,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isExpanded = false
                        onCreateTask()
                    }
                )

                // 3. Create Folder Option
                SpeedDialActionItem(
                    label = strings.newFolderTitle,
                    icon = Icons.Default.CreateNewFolder,
                    accentColor = dockColors.barBackground,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isExpanded = false
                        onCreateFolder()
                    }
                )
            }
        }

        // Main M3 Expressive Floating Action Button
        Surface(
            modifier = Modifier
                .size(56.dp)
                .onGloballyPositioned { coordinates ->
                    fabBounds = coordinates.boundsInRoot()
                }
                .clip(RoundedCornerShape(18.dp))
                .expressiveBounce(pressScale = 0.90f)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isExpanded = !isExpanded
                },
            shape = RoundedCornerShape(18.dp),
            color = dockColors.barBackground,
            shadowElevation = if (isExpanded) 6.dp else 4.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create",
                    tint = dockColors.iconTint,
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer { rotationZ = rotation }
                )
            }
        }
    }
}

@Composable
private fun SpeedDialActionItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .expressiveBounce(pressScale = 0.93f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceLight,
        border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.8f)),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val dockColors = LocalDockColors.current
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                color = if (selected) dockColors.iconTint else TextPrimary,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            )
        },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = dockColors.iconTint,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else null,
        shape = RoundedCornerShape(14.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = SurfaceLight,
            labelColor = TextPrimary,
            selectedContainerColor = dockColors.barBackground,
            selectedLabelColor = dockColors.iconTint,
            selectedLeadingIconColor = dockColors.iconTint
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = OutlineSubtle.copy(alpha = 0.6f),
            selectedBorderColor = dockColors.barBackground
        ),
        modifier = Modifier.expressiveBounce(pressScale = 0.94f)
    )
}

// ---------------------------------------------------------------------------
// Folder Card (Windows File Manager style - Roomy, Clear, Full Width)
// ---------------------------------------------------------------------------
@Composable
fun FolderCard(
    folder: ProjectFolderEntity,
    itemCount: Int,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    var showMenu by remember { mutableStateOf(false) }

    val folderColor = getM3TonalProjectColor(folder.colorHex)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .expressiveBounce(pressScale = 0.985f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = SurfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.55f)),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(folderColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = folderColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = strings.itemsCount(itemCount),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                AppDropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    AppDropdownMenuItem(
                        text = { Text(strings.openAction, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary)) },
                        leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showMenu = false
                            onClick()
                        }
                    )
                    AppDropdownMenuItem(
                        text = { Text(strings.renameFolderAction, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary)) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showMenu = false
                            onRename()
                        }
                    )
                    AppDropdownMenuItem(
                        text = { Text(strings.moveToFolderAction, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary)) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showMenu = false
                            onMove()
                        }
                    )
                    AppDropdownMenuItem(
                        text = { Text(strings.deleteFolderTitle, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)) },
                        leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Project Task Card (Windows File Manager full-width task item)
// ---------------------------------------------------------------------------
@Composable
fun ProjectTaskCard(
    taskWithSubTasks: ProjectTaskWithSubTasks,
    onToggleTask: (Long, Boolean) -> Unit,
    onToggleSubTask: (Long, Boolean) -> Unit,
    onEditTask: (ProjectTaskWithSubTasks) -> Unit,
    onMoveTask: () -> Unit,
    onDeleteTask: (Long) -> Unit,
    onDeleteSubTask: (Long) -> Unit,
    onAddSubTask: (Long, String) -> Unit
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val task = taskWithSubTasks.task
    val subTasks = taskWithSubTasks.subTasks

    var isSubtasksExpanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var isAddingSubtask by remember { mutableStateOf(false) }
    var newSubtaskText by remember { mutableStateOf("") }

    val completedCount = subTasks.count { it.isCompleted }
    val totalSubtasks = subTasks.size

    val cardAlpha by animateFloatAsState(
        targetValue = if (task.isCompleted) 0.55f else 1.0f,
        label = "cardAlpha"
    )

    val hapticFeedback = LocalHapticFeedback.current
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { distance -> distance * 0.38f }
    )

    LaunchedEffect(dismissState.currentValue) {
        when (dismissState.currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleTask(task.id, !task.isCompleted)
                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
            }
            SwipeToDismissBoxValue.EndToStart -> {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onDeleteTask(task.id)
            }
            SwipeToDismissBoxValue.Settled -> {}
        }
    }

    LaunchedEffect(task.isCompleted) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    val dismissDirection = dismissState.dismissDirection
    val isSwipingRight = dismissDirection == SwipeToDismissBoxValue.StartToEnd
    val isSwipingLeft = dismissDirection == SwipeToDismissBoxValue.EndToStart

    val swipeBackgroundColor by animateColorAsState(
        targetValue = when (dismissDirection) {
            SwipeToDismissBoxValue.StartToEnd -> if (!task.isCompleted) Color(0xFF16A34A) else Color(0xFFD97706)
            SwipeToDismissBoxValue.EndToStart -> Color(0xFFDC2626)
            SwipeToDismissBoxValue.Settled -> Color.Transparent
        },
        animationSpec = spring(stiffness = 600f),
        label = "swipeBgColor"
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = !isSubtasksExpanded,
        enableDismissFromEndToStart = !isSubtasksExpanded,
        gesturesEnabled = !isSubtasksExpanded,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(swipeBackgroundColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = if (isSwipingRight) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (isSwipingRight) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (!task.isCompleted) Icons.Default.Check else Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (!task.isCompleted) strings.statusCompleted else strings.statusActive,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else if (isSwipingLeft) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = strings.deleteAction,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(cardAlpha)
                .expressiveBounce(pressScale = 0.985f),
            shape = RoundedCornerShape(18.dp),
            color = SurfaceContainerLow,
            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.55f)),
            shadowElevation = 0.dp
        ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                val toggleableState = calculateSubtaskToggleableState(
                    isTaskCompleted = task.isCompleted,
                    totalSubtasks = totalSubtasks,
                    completedSubtasks = completedCount
                )

                AnimatedCheckbox(
                    state = toggleableState,
                    onClick = { onToggleTask(task.id, !task.isCompleted) },
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (subTasks.isNotEmpty()) {
                                isSubtasksExpanded = !isSubtasksExpanded
                            } else {
                                onEditTask(taskWithSubTasks)
                            }
                        }
                ) {
                    Text(
                        text = task.explanation,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (task.isCompleted) TextMuted else TextPrimary,
                            fontSize = 16.sp,
                            lineHeight = 23.sp
                        )
                    )

                    if (totalSubtasks > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SubtaskProgressChip(
                            completed = completedCount,
                            total = totalSubtasks,
                            isExpanded = isSubtasksExpanded,
                            onClick = { isSubtasksExpanded = !isSubtasksExpanded }
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    AppDropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        AppDropdownMenuItem(
                            text = { Text(strings.editAction, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary)) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMenu = false
                                onEditTask(taskWithSubTasks)
                            }
                        )
                        AppDropdownMenuItem(
                            text = { Text(strings.addSubtaskPlaceholder, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary)) },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMenu = false
                                isSubtasksExpanded = true
                                isAddingSubtask = true
                            }
                        )
                        AppDropdownMenuItem(
                            text = { Text(strings.moveToFolderAction, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary)) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMenu = false
                                onMoveTask()
                            }
                        )
                        AppDropdownMenuItem(
                            text = { Text(strings.deleteAction, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)) },
                            leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMenu = false
                                onDeleteTask(task.id)
                            }
                        )
                    }
                }
            }

            // Subtasks accordion
            AnimatedVisibility(
                visible = isSubtasksExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 32.dp)
                ) {
                    subTasks.forEach { subTask ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onToggleSubTask(subTask.id, !subTask.isCompleted) }
                                .padding(vertical = 5.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimatedCheckbox(
                                checked = subTask.isCompleted,
                                onCheckedChange = null,
                                size = 20.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = subTask.explanation,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    textDecoration = if (subTask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (subTask.isCompleted) TextMuted else TextPrimary,
                                    lineHeight = 19.sp
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onDeleteSubTask(subTask.id) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted, modifier = Modifier.size(15.dp))
                            }
                        }
                    }

                    if (isAddingSubtask) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = newSubtaskText,
                                onValueChange = { newSubtaskText = it },
                                placeholder = { Text(strings.addSubtaskPlaceholder, fontSize = 13.sp, color = TextMuted) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = BgLight,
                                    unfocusedContainerColor = BgLight,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(10.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (newSubtaskText.isNotBlank()) {
                                            onAddSubTask(task.id, newSubtaskText)
                                            newSubtaskText = ""
                                        }
                                    }
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (newSubtaskText.isNotBlank()) {
                                        onAddSubTask(task.id, newSubtaskText)
                                        newSubtaskText = ""
                                    }
                                    isAddingSubtask = false
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(currentAccent)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = strings.saveAction, tint = TextOnAccent, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

    }
// ---------------------------------------------------------------------------
// Project Note Card (Windows File Manager full-width note item - Large & Clear)
// ---------------------------------------------------------------------------
@Composable
fun ProjectNoteCard(
    note: ProjectNoteEntity,
    canMoveUp: Boolean = false,
    canMoveDown: Boolean = false,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    isDragging: Boolean = false,
    dragOffsetY: Float = 0f,
    onDragStart: () -> Unit = {},
    onDrag: (Float) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
    onEdit: (Rect?) -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }
    var cardBounds by remember { mutableStateOf<Rect?>(null) }

    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnDragCancel by rememberUpdatedState(onDragCancel)

    val animatedScale by animateFloatAsState(
        targetValue = if (isDragging) 1.035f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
        label = "noteDragScale"
    )
    val animatedElevation by animateDpAsState(
        targetValue = if (isDragging) 14.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
        label = "noteDragElevation"
    )
    val animatedBorderAlpha by animateFloatAsState(
        targetValue = if (isDragging) 0.85f else 0.55f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
        label = "noteDragBorderAlpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 10f else 0f)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                translationY = if (isDragging) dragOffsetY else 0f
            }
            .onGloballyPositioned { coordinates ->
                cardBounds = coordinates.boundsInRoot()
            }
            .clip(RoundedCornerShape(12.dp))
            .expressiveBounce(pressScale = 0.985f)
            .clickable(onClick = { onEdit(cardBounds) })
            .pointerInput(note.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentOnDragStart()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentOnDrag(dragAmount.y)
                    },
                    onDragEnd = { currentOnDragEnd() },
                    onDragCancel = { currentOnDragCancel() }
                )
            },
        shape = RoundedCornerShape(12.dp),
        color = SurfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDragging) currentAccent.copy(alpha = animatedBorderAlpha) else OutlineSubtle.copy(alpha = animatedBorderAlpha)),
        shadowElevation = animatedElevation
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (note.title.isNotBlank()) note.title else strings.newNoteTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    AppDropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        AppDropdownMenuItem(
                            text = { Text(strings.editAction, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary)) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMenu = false
                                onEdit(cardBounds)
                            }
                        )
                        if (canMoveUp) {
                            AppDropdownMenuItem(
                                text = { Text(strings.moveUp, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary)) },
                                leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showMenu = false
                                    onMoveUp()
                                }
                            )
                        }
                        if (canMoveDown) {
                            AppDropdownMenuItem(
                                text = { Text(strings.moveDown, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary)) },
                                leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showMenu = false
                                    onMoveDown()
                                }
                            )
                        }
                        AppDropdownMenuItem(
                            text = { Text(strings.moveToFolderAction, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary)) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMenu = false
                                onMove()
                            }
                        )
                        AppDropdownMenuItem(
                            text = { Text(strings.deleteAction, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)) },
                            leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                val previewBlocks = remember(note.content) {
                    NoteRichParser.parseMarkdownToBlocks(note.content).filter {
                        it.type == NoteBlockType.DIVIDER || it.text.isNotBlank()
                    }.take(5)
                }
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    var numberedCounter = 1
                    previewBlocks.forEach { block ->
                        when (block.type) {
                            NoteBlockType.CHECKLIST -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    if (block.isChecked) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = TextSecondary.copy(alpha = 0.65f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = NoteRichParser.renderInlineMarkdown(
                                            raw = block.text,
                                            baseColor = if (block.isChecked) TextSecondary.copy(alpha = 0.60f) else TextPrimary,
                                            accentColor = currentAccent,
                                            isStrikethrough = block.isChecked
                                        ),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            NoteBlockType.HEADING_1, NoteBlockType.HEADING_2, NoteBlockType.HEADING_3 -> {
                                Text(
                                    text = NoteRichParser.renderInlineMarkdown(
                                        raw = block.text,
                                        baseColor = TextPrimary,
                                        accentColor = currentAccent
                                    ),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (block.type == NoteBlockType.HEADING_1) 15.sp else 14.sp,
                                        lineHeight = 20.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            NoteBlockType.BULLET -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(currentAccent)
                                    )
                                    Text(
                                        text = NoteRichParser.renderInlineMarkdown(
                                            raw = block.text,
                                            baseColor = TextSecondary,
                                            accentColor = currentAccent
                                        ),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            NoteBlockType.NUMBERED -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${numberedCounter++}.",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = currentAccent
                                        )
                                    )
                                    Text(
                                        text = NoteRichParser.renderInlineMarkdown(
                                            raw = block.text,
                                            baseColor = TextSecondary,
                                            accentColor = currentAccent
                                        ),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            NoteBlockType.QUOTE -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(currentAccent.copy(alpha = 0.08f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(14.dp)
                                            .clip(CircleShape)
                                            .background(currentAccent)
                                    )
                                    Text(
                                        text = NoteRichParser.renderInlineMarkdown(
                                            raw = block.text,
                                            baseColor = TextSecondary,
                                            accentColor = currentAccent
                                        ),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontStyle = FontStyle.Italic,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            NoteBlockType.DIVIDER -> {
                                HorizontalDivider(
                                    color = OutlineVariant.copy(alpha = 0.4f),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                            NoteBlockType.CODE_BLOCK -> {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SurfaceContainerHighest,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = block.text,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            color = currentAccent
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            NoteBlockType.PARAGRAPH -> {
                                Text(
                                    text = NoteRichParser.renderInlineMarkdown(
                                        raw = block.text,
                                        baseColor = TextSecondary,
                                        accentColor = currentAccent
                                    ),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Bottom Sheet: Move To Folder (File Explorer Move Dialog)
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveToFolderBottomSheet(
    target: MoveItemTarget,
    allFolders: List<ProjectFolderEntity>,
    currentFolderId: Long?,
    onDismiss: () -> Unit,
    onSelectFolder: (Long?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current

    val itemLabel = when (target) {
        is MoveItemTarget.Task -> target.taskWithSubTasks.task.explanation
        is MoveItemTarget.Note -> if (target.note.title.isNotBlank()) target.note.title else target.note.content
        is MoveItemTarget.Folder -> target.folder.name
    }

    // Exclude folder itself and all its descendant subfolders if target is a folder
    val excludedFolderIds = remember(target, allFolders) {
        if (target is MoveItemTarget.Folder) {
            val set = mutableSetOf(target.folder.id)
            val queue = mutableListOf(target.folder.id)
            while (queue.isNotEmpty()) {
                val current = queue.removeAt(0)
                val children = allFolders.filter { it.parentFolderId == current }.map { it.id }
                set.addAll(children)
                queue.addAll(children)
            }
            set
        } else {
            emptySet()
        }
    }

    val availableFolders = allFolders.filterNot { it.id in excludedFolderIds }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(currentAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DriveFileMove,
                            contentDescription = null,
                            tint = currentAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = strings.moveToFolderTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = strings.closeAction, tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = BgLight,
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle)
            ) {
                Text(
                    text = itemLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Дестинация (Избери папка):",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Option 1: Project Root (Home)
                val isCurrentlyInRoot = currentFolderId == null
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = !isCurrentlyInRoot) {
                            onSelectFolder(null)
                        },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isCurrentlyInRoot) currentAccent.copy(alpha = 0.08f) else BgLight,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isCurrentlyInRoot) currentAccent else OutlineSubtle
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = if (isCurrentlyInRoot) currentAccent else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = strings.projectRootFolder,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isCurrentlyInRoot) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 15.sp,
                                color = if (isCurrentlyInRoot) currentAccent else TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (isCurrentlyInRoot) {
                            Text(
                                text = "Текуща",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = currentAccent
                                )
                            )
                        }
                    }
                }

                // Option 2..N: Available Project Folders
                availableFolders.forEach { folder ->
                    val isCurrentLocation = folder.id == currentFolderId
                    val fColor = remember(folder.colorHex) {
                        try {
                            Color(android.graphics.Color.parseColor(folder.colorHex))
                        } catch (_: Exception) {
                            Color(0xFF6366F1)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(enabled = !isCurrentLocation) {
                                onSelectFolder(folder.id)
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCurrentLocation) currentAccent.copy(alpha = 0.08f) else BgLight,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCurrentLocation) currentAccent else OutlineSubtle
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = fColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = folder.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isCurrentLocation) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = if (isCurrentLocation) currentAccent else TextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            if (isCurrentLocation) {
                                Text(
                                    text = "Текуща",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = currentAccent
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Bottom Sheet: Rename Folder
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameFolderBottomSheet(
    folder: ProjectFolderEntity,
    onDismiss: () -> Unit,
    onSave: (Long, String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    var name by remember { mutableStateOf(folder.name) }
    var selectedColor by remember { mutableStateOf(folder.colorHex) }

    val colors = listOf(
        "#6366F1", "#2A8575", "#2563EB", "#E11D48", "#D97706", "#9333EA", "#475569"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = strings.renameFolderAction,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = strings.closeAction, tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(strings.folderNamePlaceholder) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BgLight,
                    unfocusedContainerColor = BgLight,
                    focusedBorderColor = currentAccent,
                    unfocusedBorderColor = OutlineSubtle
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = strings.projectColorLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                colors.forEach { hex ->
                    val color = Color(android.graphics.Color.parseColor(hex))
                    val isSelected = selectedColor == hex

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) AccentPrimary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(folder.id, name, selectedColor)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DockBarColor,
                    contentColor = TextOnAccent
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = TextOnAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.updateAction, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextOnAccent)
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Bottom Sheet: Edit Project
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProjectBottomSheet(
    project: ProjectEntity,
    onDismiss: () -> Unit,
    onSave: (Long, String, String, String, String, Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current

    var name by remember { mutableStateOf(project.name) }
    var description by remember { mutableStateOf(project.description) }
    var useAppThemeColor by remember { mutableStateOf(project.colorHex == "app" || project.colorHex.isBlank()) }
    var selectedColor by remember { mutableStateOf(if (project.colorHex == "app") "#2A8575" else project.colorHex) }
    var selectedIcon by remember { mutableStateOf(project.iconName) }
    var isPinned by remember { mutableStateOf(project.isPinned) }

    val colors = listOf(
        "#2A8575", "#6366F1", "#2563EB", "#0284C7", "#0D9488", "#16A34A", "#CA8A04", "#D97706", "#EA580C", "#E11D48", "#9333EA", "#475569"
    )

    val icons = listOf(
        Pair("folder", Icons.Default.Folder),
        Pair("rocket", Icons.Default.RocketLaunch),
        Pair("lightbulb", Icons.Default.Lightbulb),
        Pair("work", Icons.Default.Work),
        Pair("palette", Icons.Default.Palette),
        Pair("code", Icons.Default.Code),
        Pair("star", Icons.Default.Star),
        Pair("bookmark", Icons.Default.Bookmark)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(currentAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = currentAccent, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = strings.editProjectAction,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = strings.closeAction, tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(strings.projectNameLabel) },
                placeholder = { Text(strings.projectNamePlaceholder) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BgLight,
                    unfocusedContainerColor = BgLight,
                    focusedBorderColor = currentAccent,
                    unfocusedBorderColor = OutlineSubtle
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(strings.projectDescLabel) },
                placeholder = { Text(strings.projectDescPlaceholder) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BgLight,
                    unfocusedContainerColor = BgLight,
                    focusedBorderColor = currentAccent,
                    unfocusedBorderColor = OutlineSubtle
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle for App/Dynamic Theme Color
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = BgLight,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (useAppThemeColor) currentAccent.copy(alpha = 0.16f) else currentAccent.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = if (useAppThemeColor) currentAccent else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.useAppColorToggleLabel,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = strings.useAppColorToggleSubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            )
                        }
                    }

                    Switch(
                        checked = useAppThemeColor,
                        onCheckedChange = { useAppThemeColor = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = currentAccent,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = BgLight
                        )
                    )
                }
            }

            if (!useAppThemeColor) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(strings.projectColorLabel, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp))
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colors.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = selectedColor == hex

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) AccentPrimary else Color.Transparent,
                                    shape = CircleShape
                                )
                            .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(strings.projectIconLabel, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp))
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icons.forEach { (iconKey, iconVec) ->
                    val isSelected = selectedIcon == iconKey
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedIcon = iconKey },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) currentAccent else BgLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) currentAccent else OutlineSubtle)
                    ) {
                        Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = iconVec,
                                contentDescription = iconKey,
                                tint = if (isSelected) TextOnAccent else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pin Project Option Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = BgLight,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isPinned) currentAccent.copy(alpha = 0.16f) else currentAccent.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = null,
                                tint = if (isPinned) currentAccent else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = strings.pinProjectToggleLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        )
                    }

                    Switch(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = currentAccent,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = BgLight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val finalColor = if (useAppThemeColor) "app" else selectedColor
                        onSave(project.id, name, description, finalColor, selectedIcon, isPinned)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DockBarColor,
                    contentColor = TextOnAccent
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = TextOnAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.updateAction, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextOnAccent)
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Bottom Sheet: New Project
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewProjectBottomSheet(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var useAppThemeColor by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf("#2A8575") }
    var selectedIcon by remember { mutableStateOf("folder") }
    var isPinned by remember { mutableStateOf(false) }

    val colors = listOf(
        "#2A8575", "#6366F1", "#2563EB", "#0284C7", "#0D9488", "#16A34A", "#CA8A04", "#D97706", "#EA580C", "#E11D48", "#9333EA", "#475569"
    )

    val icons = listOf(
        Pair("folder", Icons.Default.Folder),
        Pair("rocket", Icons.Default.RocketLaunch),
        Pair("lightbulb", Icons.Default.Lightbulb),
        Pair("work", Icons.Default.Work),
        Pair("palette", Icons.Default.Palette),
        Pair("code", Icons.Default.Code),
        Pair("star", Icons.Default.Star),
        Pair("bookmark", Icons.Default.Bookmark)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(currentAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = currentAccent, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = strings.newProjectTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = strings.closeAction, tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(strings.projectNameLabel) },
                placeholder = { Text(strings.projectNamePlaceholder) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BgLight,
                    unfocusedContainerColor = BgLight,
                    focusedBorderColor = currentAccent,
                    unfocusedBorderColor = OutlineSubtle
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(strings.projectDescLabel) },
                placeholder = { Text(strings.projectDescPlaceholder) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BgLight,
                    unfocusedContainerColor = BgLight,
                    focusedBorderColor = currentAccent,
                    unfocusedBorderColor = OutlineSubtle
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle for App/Dynamic Theme Color
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = BgLight,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (useAppThemeColor) currentAccent.copy(alpha = 0.16f) else currentAccent.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = if (useAppThemeColor) currentAccent else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.useAppColorToggleLabel,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = strings.useAppColorToggleSubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            )
                        }
                    }

                    Switch(
                        checked = useAppThemeColor,
                        onCheckedChange = { useAppThemeColor = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = currentAccent,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = BgLight
                        )
                    )
                }
            }

            if (!useAppThemeColor) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(strings.projectColorLabel, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp))
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colors.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = selectedColor == hex

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) AccentPrimary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(strings.projectIconLabel, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp))
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icons.forEach { (iconKey, iconVec) ->
                    val isSelected = selectedIcon == iconKey
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedIcon = iconKey },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) currentAccent else BgLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) currentAccent else OutlineSubtle)
                    ) {
                        Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = iconVec,
                                contentDescription = iconKey,
                                tint = if (isSelected) TextOnAccent else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pin Project Option Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = BgLight,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isPinned) currentAccent.copy(alpha = 0.16f) else currentAccent.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = null,
                                tint = if (isPinned) currentAccent else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = strings.pinProjectToggleLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        )
                    }

                    Switch(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = currentAccent,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = BgLight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val finalColor = if (useAppThemeColor) "app" else selectedColor
                        onCreate(name, description, finalColor, selectedIcon, isPinned)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DockBarColor,
                    contentColor = TextOnAccent
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = TextOnAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.createProjectButton, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextOnAccent)
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Bottom Sheet: Create Folder
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFolderBottomSheet(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#6366F1") }

    val colors = listOf(
        "#6366F1", "#2A8575", "#2563EB", "#E11D48", "#D97706", "#9333EA", "#475569"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = strings.newFolderTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = strings.closeAction, tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(strings.folderNamePlaceholder) },
                placeholder = { Text("Напр. Документи, Маркетинг, Задачи...") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BgLight,
                    unfocusedContainerColor = BgLight,
                    focusedBorderColor = currentAccent,
                    unfocusedBorderColor = OutlineSubtle
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(strings.projectColorLabel, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                colors.forEach { hex ->
                    val color = Color(android.graphics.Color.parseColor(hex))
                    val isSelected = selectedColor == hex

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) AccentPrimary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name, selectedColor)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DockBarColor,
                    contentColor = TextOnAccent
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = TextOnAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.newFolderTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextOnAccent)
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Bottom Sheet: Project Task (Undated, Explanation only + Subtasks)
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectTaskBottomSheet(
    taskWithSubTasks: ProjectTaskWithSubTasks?,
    onDismiss: () -> Unit,
    onSave: (Long?, String, List<String>) -> Unit
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var explanation by remember { mutableStateOf(taskWithSubTasks?.task?.explanation ?: "") }
    val subtasks = remember {
        mutableStateListOf<String>().apply {
            taskWithSubTasks?.subTasks?.map { it.explanation }?.let { addAll(it) }
        }
    }
    var newSubtaskInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(currentAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.PlaylistAddCheck,
                            contentDescription = null,
                            tint = currentAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (taskWithSubTasks == null) strings.newTaskTitle else strings.editTaskTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp
                        )
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = strings.closeAction, tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary Unified Container: Task Input & Subtasks Checklist (Matching Today Screen)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = SurfaceContainerLow,
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Explanation Input
                    OutlinedTextField(
                        value = explanation,
                        onValueChange = { explanation = it },
                        placeholder = {
                            Text(
                                strings.taskExplanationPlaceholder,
                                style = MaterialTheme.typography.bodyLarge.copy(color = TextMuted)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 22.sp),
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainerLowest,
                            unfocusedContainerColor = SurfaceContainerLowest,
                            focusedBorderColor = currentAccent,
                            unfocusedBorderColor = OutlineSubtle.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(
                        color = OutlineSubtle.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Subtasks Header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                            contentDescription = null,
                            tint = currentAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.subtasksHeader(subtasks.size),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        subtasks.forEachIndexed { index, subtask ->
                            InputChip(
                                selected = false,
                                onClick = {},
                                label = {
                                    Text(
                                        text = subtask,
                                        color = TextPrimary,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                                    )
                                },
                                avatar = {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(currentAccent)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { subtasks.removeAt(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = strings.deleteAction,
                                            tint = TextMuted,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = InputChipDefaults.inputChipColors(
                                    containerColor = SurfaceContainerLowest,
                                    labelColor = TextPrimary,
                                    trailingIconColor = TextMuted
                                ),
                                border = InputChipDefaults.inputChipBorder(
                                    enabled = true,
                                    selected = false,
                                    borderColor = OutlineSubtle.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .expressiveBounce(pressScale = 0.98f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newSubtaskInput,
                                onValueChange = { newSubtaskInput = it },
                                placeholder = { Text(strings.addSubtaskPlaceholder, fontSize = 14.sp, color = TextMuted) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceContainerLowest,
                                    unfocusedContainerColor = SurfaceContainerLowest,
                                    focusedBorderColor = currentAccent,
                                    unfocusedBorderColor = OutlineSubtle.copy(alpha = 0.5f)
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (newSubtaskInput.isNotBlank()) {
                                            subtasks.add(newSubtaskInput.trim())
                                            newSubtaskInput = ""
                                        }
                                    }
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    if (newSubtaskInput.isNotBlank()) {
                                        subtasks.add(newSubtaskInput.trim())
                                        newSubtaskInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(currentAccent)
                                    .expressiveBounce(pressScale = 0.92f)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = strings.addAction, tint = TextOnAccent, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (explanation.isNotBlank()) {
                        onSave(taskWithSubTasks?.task?.id, explanation, subtasks.toList())
                    }
                },
                enabled = explanation.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DockBarColor,
                    contentColor = TextOnAccent
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = TextOnAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.saveTaskButton, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextOnAccent)
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Bottom Sheet: Project Note (Delegates to modern ProjectNoteEditor)
// ---------------------------------------------------------------------------
@Composable
fun ProjectNoteBottomSheet(
    note: ProjectNoteEntity?,
    originBounds: Rect? = null,
    onDismiss: () -> Unit,
    onSave: (Long?, String, String) -> Unit,
    onAutoSave: ((Long?, String, String) -> Unit)? = null
) {
    ProjectNoteEditor(
        note = note,
        originBounds = originBounds,
        onDismiss = onDismiss,
        onAutoSave = onAutoSave,
        onSave = onSave
    )
}
