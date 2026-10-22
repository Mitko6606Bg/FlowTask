package com.flowtask.app.ui.screens

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowtask.app.data.local.model.LayerProgram
import com.flowtask.app.ui.components.dialogs.M3CardDeleteProgramDialog
import com.flowtask.app.ui.components.programs.ProgramCreateCard
import com.flowtask.app.ui.components.programs.ProgramEditCard
import com.flowtask.app.ui.components.programs.ProgramFolderCard
import com.flowtask.app.ui.components.programs.ProgramWeekCalendarView
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.SurfaceContainerLow
import com.flowtask.app.ui.theme.SurfaceContainerLowest
import com.flowtask.app.ui.theme.TextOnAccent
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.ui.viewmodel.TaskViewModel
import com.flowtask.app.util.LocalAppStrings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

private enum class ProgramScreenMode {
    HUB,       // List of all programs or empty state
    DETAILS,   // Inside the program folder: Calendar Week View & routines
    CREATE,    // Task-style Create Program Card
    EDIT       // Edit Program Card
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterProgramBottomSheet(
    viewModel: TaskViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val layerPrograms by viewModel.layerPrograms.collectAsStateWithLifecycle()
    val targetProgramDetailsId by viewModel.targetProgramDetailsId.collectAsStateWithLifecycle()

    var screenMode by remember {
        mutableStateOf(
            if (!targetProgramDetailsId.isNullOrBlank()) ProgramScreenMode.DETAILS else ProgramScreenMode.HUB
        )
    }
    var selectedProgramId by remember {
        mutableStateOf(targetProgramDetailsId)
    }
    var programToDelete by remember { mutableStateOf<LayerProgram?>(null) }
    var syncSuccessMessage by remember { mutableStateOf(false) }

    LaunchedEffect(targetProgramDetailsId) {
        if (!targetProgramDetailsId.isNullOrBlank()) {
            selectedProgramId = targetProgramDetailsId
            syncSuccessMessage = false
            screenMode = ProgramScreenMode.DETAILS
            viewModel.clearTargetProgramDetails()
        }
    }

    val activeProgram = layerPrograms.find { it.id == selectedProgramId }
        ?: layerPrograms.firstOrNull()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
        var predictiveBackEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }

        // Intercept back gesture inside submenus and animate predictive back;
        // when in HUB (enabled = false), native sheet dismiss predictive back executes!
        PredictiveBackHandler(enabled = screenMode != ProgramScreenMode.HUB) { progress: Flow<BackEventCompat> ->
            try {
                progress.collect { backEvent ->
                    predictiveBackProgress = backEvent.progress
                    predictiveBackEdge = backEvent.swipeEdge
                }
                when (screenMode) {
                    ProgramScreenMode.CREATE -> screenMode = ProgramScreenMode.HUB
                    ProgramScreenMode.EDIT -> screenMode = if (selectedProgramId != null) ProgramScreenMode.DETAILS else ProgramScreenMode.HUB
                    ProgramScreenMode.DETAILS -> screenMode = ProgramScreenMode.HUB
                    ProgramScreenMode.HUB -> {}
                }
            } catch (_: CancellationException) {
                // User aborted the edge swipe gesture
            } finally {
                predictiveBackProgress = 0f
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
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
            AnimatedContent(
                targetState = screenMode,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "ProgramScreenTransition"
            ) { mode ->
            when (mode) {
                ProgramScreenMode.HUB -> {
                    // =========================================================
                    // 1. ALL PROGRAMS HUB (FOLDERS VIEW)
                    // =========================================================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 40.dp)
                    ) {
                        // Header Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(currentAccent.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EventRepeat,
                                        contentDescription = null,
                                        tint = currentAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = strings.allProgramsTitle,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = TextPrimary
                                        )
                                    )
                                    Text(
                                        text = strings.allProgramsSubtitle,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = strings.closeAction,
                                    tint = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (layerPrograms.isEmpty()) {
                            // Empty State: ONLY a button to create
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(currentAccent.copy(alpha = 0.10f)),
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
                                    text = strings.noProgramsEmptyTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = TextPrimary
                                    ),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = strings.noProgramsEmptySubtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = currentAccent,
                                    shadowElevation = 2.dp,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .expressiveBounce(pressScale = 0.94f)
                                ) {
                                    TextButton(
                                        onClick = { screenMode = ProgramScreenMode.CREATE },
                                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = TextOnAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = strings.createProgramAction,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = TextOnAccent
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            // List of Created Programs ("Folders")
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                contentPadding = PaddingValues(top = 4.dp, bottom = 40.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    layerPrograms,
                                    key = { it.id },
                                    contentType = { "program" }
                                ) { program ->
                                    ProgramFolderCard(
                                        program = program,
                                        onOpenProgram = {
                                            selectedProgramId = program.id
                                            syncSuccessMessage = false
                                            screenMode = ProgramScreenMode.DETAILS
                                        },
                                        onToggleActive = { isEnabled ->
                                            viewModel.toggleProgramEnabled(program.id, isEnabled)
                                        }
                                    )
                                }

                                item(key = "create_new_program_btn") {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(18.dp),
                                        color = SurfaceContainerLow,
                                        border = BorderStroke(1.2.dp, currentAccent.copy(alpha = 0.35f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(18.dp))
                                            .expressiveBounce(pressScale = 0.96f)
                                            .clickable { screenMode = ProgramScreenMode.CREATE }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 14.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                tint = currentAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = strings.addLayerProgram,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = currentAccent,
                                                    fontSize = 14.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ProgramScreenMode.DETAILS -> {
                    // =========================================================
                    // 2. INSIDE THE PROGRAM FOLDER: CALENDAR WEEK VIEW
                    // =========================================================
                    if (activeProgram != null) {
                        ProgramWeekCalendarView(
                            program = activeProgram,
                            onBack = { screenMode = ProgramScreenMode.HUB },
                            onEditProgram = { screenMode = ProgramScreenMode.EDIT },
                            onDeleteProgram = { programToDelete = activeProgram },
                            onSaveProgram = { updated ->
                                viewModel.saveLayerProgram(updated)
                            },
                            onSyncToCalendar = {
                                viewModel.syncLayerProgramToCalendar(activeProgram)
                                syncSuccessMessage = true
                            },
                            syncSuccessMessage = syncSuccessMessage
                        )
                    } else {
                        screenMode = ProgramScreenMode.HUB
                    }
                }

                ProgramScreenMode.CREATE -> {
                    // =========================================================
                    // 3. TASK-STYLE CREATE PROGRAM
                    // =========================================================
                    ProgramCreateCard(
                        onDismiss = { screenMode = ProgramScreenMode.HUB },
                        onProgramCreated = { newProgram ->
                            viewModel.saveLayerProgram(newProgram)
                            selectedProgramId = newProgram.id
                            syncSuccessMessage = false
                            // Automatically navigate inside the newly created program!
                            screenMode = ProgramScreenMode.DETAILS
                        }
                    )
                }

                ProgramScreenMode.EDIT -> {
                    // =========================================================
                    // 4. EDIT PROGRAM
                    // =========================================================
                    if (activeProgram != null) {
                        ProgramEditCard(
                            program = activeProgram,
                            onDismiss = {
                                screenMode = if (selectedProgramId != null) ProgramScreenMode.DETAILS else ProgramScreenMode.HUB
                            },
                            onSaveProgram = { updated ->
                                viewModel.saveLayerProgram(updated)
                                screenMode = ProgramScreenMode.DETAILS
                            }
                        )
                    } else {
                        screenMode = ProgramScreenMode.HUB
                    }
                }
            }
        }
    }
    }

    // Delete Confirmation Card Dialog
    if (programToDelete != null) {
        M3CardDeleteProgramDialog(
            programName = programToDelete!!.layerName,
            onDismiss = { programToDelete = null },
            onConfirmDelete = {
                val toDelete = programToDelete!!
                viewModel.deleteLayerProgram(toDelete.id)
                if (selectedProgramId == toDelete.id) {
                    selectedProgramId = null
                    screenMode = ProgramScreenMode.HUB
                }
                programToDelete = null
            }
        )
    }
}
