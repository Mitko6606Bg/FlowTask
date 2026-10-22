package com.flowtask.app.ui.screens

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.flowtask.app.ui.components.LayerFilterBar
import com.flowtask.app.ui.components.TaskCard
import com.flowtask.app.ui.theme.BgLight
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.LocalDockColors
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.SurfaceContainerLight
import com.flowtask.app.ui.theme.TextOnAccent
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.ui.viewmodel.TaskViewModel
import com.flowtask.app.util.DateTimeUtils
import com.flowtask.app.util.LocalAppStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayUpcomingScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val dockColors = LocalDockColors.current
    val todayTasks by viewModel.todayTasks.collectAsStateWithLifecycle()
    val upcomingTasks by viewModel.upcomingTasks.collectAsStateWithLifecycle()
    val availableLayers by viewModel.availableLayers.collectAsStateWithLifecycle()
    val selectedLayer by viewModel.selectedLayer.collectAsStateWithLifecycle()

    var isCompletedExpanded by remember { mutableStateOf(false) }
    var isUpcomingCompletedExpanded by remember { mutableStateOf(false) }

    val completedToday = remember(todayTasks) { todayTasks.filter { it.task.isCompleted } }
    val pendingToday = remember(todayTasks) { todayTasks.filter { !it.task.isCompleted } }

    val completedUpcoming = remember(upcomingTasks) { upcomingTasks.filter { it.task.isCompleted } }
    val pendingUpcoming = remember(upcomingTasks) { upcomingTasks.filter { !it.task.isCompleted } }

    val hasPendingUpcoming = pendingUpcoming.isNotEmpty()
    val targetRestingHeight = if (hasPendingUpcoming) {
        SpringSheetPhysics.RESTING_UPCOMING_HEIGHT_DP.dp
    } else {
        SpringSheetPhysics.EMPTY_RESTING_UPCOMING_HEIGHT_DP.dp
    }
    val animatedRestingHeight by animateDpAsState(
        targetValue = targetRestingHeight,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f),
        label = "upcomingRestingHeight"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgLight)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Profile / Layer Filter Bar
        LayerFilterBar(
            availableLayers = availableLayers,
            selectedLayer = selectedLayer,
            onSelectLayer = { viewModel.selectLayer(it) },
            onLongClickLayer = { viewModel.openEditLayer(it) },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Coordinated Non-Overlapping Dual-Container Viewport
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val density = LocalDensity.current
            val coroutineScope = rememberCoroutineScope()
            val velocityTracker = remember { VelocityTracker() }
            var accumulatedDragY by remember { mutableFloatStateOf(0f) }

            val dockedTodayHeight = SpringSheetPhysics.DOCKED_TODAY_HEIGHT_DP.dp
            val containerGap = SpringSheetPhysics.CONTAINER_GAP_DP.dp
            val totalHeight = maxHeight

            val minUpcomingHeight = animatedRestingHeight.coerceAtMost((totalHeight - dockedTodayHeight - containerGap) * 0.65f)
            val maxUpcomingHeight = (totalHeight - dockedTodayHeight - containerGap).coerceAtLeast(minUpcomingHeight)

            val minTodayHeight = dockedTodayHeight
            val maxTodayHeight = (totalHeight - minUpcomingHeight - containerGap).coerceAtLeast(minTodayHeight)

            val totalDragDistancePx = with(density) { (maxUpcomingHeight - minUpcomingHeight).toPx().coerceAtLeast(1f) }

            // focusProgress: 0f = Today dominant, 1f = Upcoming dominant
            val focusProgress = remember { Animatable(0f) }
            val springSpec = remember {
                spring<Float>(
                    dampingRatio = 0.8f,
                    stiffness = 380f
                )
            }

            val upcomingListState = rememberLazyListState()

            // System Back Navigation: collapses Upcoming back to Today with real-time predictive feedback
            PredictiveBackHandler(enabled = focusProgress.value > SpringSheetPhysics.BACK_HANDLER_THRESHOLD) { progress: Flow<BackEventCompat> ->
                val startProgress = focusProgress.value
                try {
                    progress.collect { backEvent ->
                        val targetProgress = (startProgress * (1f - backEvent.progress)).coerceIn(0f, 1f)
                        focusProgress.snapTo(targetProgress)
                    }
                    focusProgress.animateTo(0f, animationSpec = springSpec)
                } catch (_: CancellationException) {
                    focusProgress.animateTo(startProgress, animationSpec = springSpec)
                }
            }

            val clampedProgress = focusProgress.value.coerceIn(0f, 1f)
            val todayHeight = lerp(maxTodayHeight, minTodayHeight, clampedProgress)
            val upcomingHeight = lerp(minUpcomingHeight, maxUpcomingHeight, clampedProgress)
            val isTodayDocked = clampedProgress > 0.5f
            val isUpcomingDocked = clampedProgress <= 0.5f

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(containerGap)
            ) {
                // ==========================================
                // CONTAINER 1: TODAY TASKS
                // ==========================================
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(todayHeight),
                    shape = RoundedCornerShape(24.dp),
                    color = SurfaceContainerLight,
                    shadowElevation = if (!isTodayDocked) 3.dp else 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        // Today Header Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (isTodayDocked) {
                                        coroutineScope.launch {
                                            focusProgress.animateTo(0f, animationSpec = springSpec)
                                        }
                                    }
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(dockColors.barBackground.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = dockColors.barBackground,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = strings.todayHeader,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = TextPrimary
                                        )
                                    )
                                    Text(
                                        text = strings.todayRemainingCount(pendingToday.size),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .expressiveBounce(pressScale = 0.92f)
                                        .clickable { viewModel.openAddTask(DateTimeUtils.getStartOfDay()) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = dockColors.barBackground,
                                    shadowElevation = 2.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = strings.newTaskTitle,
                                            tint = dockColors.iconTint,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            val target = if (isTodayDocked) 0f else 1f
                                            focusProgress.animateTo(target, animationSpec = springSpec)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isTodayDocked) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Today Content List
                        if (todayTasks.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(BgLight)
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = currentAccent.copy(alpha = 0.7f),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = strings.todayEmptyTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = TextPrimary
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = strings.todayEmptySubtitle,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 13.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(top = 2.dp, bottom = 12.dp)
                            ) {
                                items(
                                    pendingToday,
                                    key = { "today_${it.task.id}" },
                                    contentType = { "task" }
                                ) { taskWithSubTasks ->
                                    val index = pendingToday.indexOf(taskWithSubTasks)
                                    TaskCard(
                                        taskWithSubTasks = taskWithSubTasks,
                                        onToggleTask = { id, done -> viewModel.toggleTaskCompletion(id, done) },
                                        onToggleSubTask = { id, done -> viewModel.toggleSubTaskCompletion(id, done) },
                                        onEditTask = { viewModel.openEditTask(it) },
                                        onDeleteTask = { viewModel.deleteTask(it) },
                                        onDeleteSubTask = { viewModel.deleteSubTask(it) },
                                        onAddSubTask = { id, text -> viewModel.addSubTask(id, text) },
                                        canReorder = true,
                                        canMoveUp = index > 0,
                                        canMoveDown = index < pendingToday.size - 1,
                                        onMoveUp = { viewModel.moveTaskUp(taskWithSubTasks.task, todayTasks) },
                                        onMoveDown = { viewModel.moveTaskDown(taskWithSubTasks.task, todayTasks) }
                                    )
                                }

                                if (completedToday.isNotEmpty()) {
                                    item(key = "header_completed_today", contentType = "header") {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { isCompletedExpanded = !isCompletedExpanded }
                                                .padding(vertical = 6.dp, horizontal = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = strings.completedTasksSection(completedToday.size),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = TextSecondary
                                                )
                                            )
                                            Icon(
                                                imageVector = if (isCompletedExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = TextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    if (isCompletedExpanded) {
                                        items(
                                            completedToday,
                                            key = { "completed_today_${it.task.id}" },
                                            contentType = { "task" }
                                        ) { taskWithSubTasks ->
                                            TaskCard(
                                                taskWithSubTasks = taskWithSubTasks,
                                                onToggleTask = { id, done -> viewModel.toggleTaskCompletion(id, done) },
                                                onToggleSubTask = { id, done -> viewModel.toggleSubTaskCompletion(id, done) },
                                                onEditTask = { viewModel.openEditTask(it) },
                                                onDeleteTask = { viewModel.deleteTask(it) },
                                                onDeleteSubTask = { viewModel.deleteSubTask(it) },
                                                onAddSubTask = { id, text -> viewModel.addSubTask(id, text) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // CONTAINER 2: UPCOMING TASKS
                // ==========================================
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(upcomingHeight),
                    shape = RoundedCornerShape(24.dp),
                    color = SurfaceContainerLight,
                    shadowElevation = if (!isUpcomingDocked) 3.dp else 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        // Grab Handle & Header Area (Drag & Tap Target)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(totalDragDistancePx) {
                                    detectVerticalDragGestures(
                                        onDragStart = {
                                            coroutineScope.launch { focusProgress.stop() }
                                            velocityTracker.resetTracking()
                                            accumulatedDragY = 0f
                                        },
                                        onDragEnd = {
                                            val velocityY = velocityTracker.calculateVelocity().y
                                            val target = SpringSheetPhysics.calculateTargetProgress(velocityY, focusProgress.value)
                                            val initialVelocity = SpringSheetPhysics.calculateInitialVelocity(velocityY, totalDragDistancePx)
                                            coroutineScope.launch {
                                                focusProgress.animateTo(
                                                    targetValue = target,
                                                    animationSpec = springSpec,
                                                    initialVelocity = initialVelocity
                                                )
                                            }
                                        },
                                        onDragCancel = {
                                            val target = if (focusProgress.value >= SpringSheetPhysics.EXPAND_PROGRESS_THRESHOLD) 1f else 0f
                                            coroutineScope.launch {
                                                focusProgress.animateTo(target, animationSpec = springSpec)
                                            }
                                        },
                                        onVerticalDrag = { change, dragAmount ->
                                            change.consume()
                                            accumulatedDragY += dragAmount
                                            velocityTracker.addPosition(change.uptimeMillis, Offset(0f, accumulatedDragY))
                                            if (totalDragDistancePx > 0f) {
                                                val deltaProgress = -dragAmount / totalDragDistancePx
                                                coroutineScope.launch {
                                                    focusProgress.snapTo((focusProgress.value + deltaProgress).coerceIn(0f, 1f))
                                                }
                                            }
                                        }
                                    )
                                }
                        ) {
                            // Centered Grab Handle Pill
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        coroutineScope.launch {
                                            val target = if (isUpcomingDocked) 1f else 0f
                                            focusProgress.animateTo(target, animationSpec = springSpec)
                                        }
                                    }
                                    .padding(top = 4.dp, bottom = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(4.dp)
                                        .clip(CircleShape)
                                        .background(OutlineSubtle)
                                )
                            }

                            // Upcoming Header Row (NO create button)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        coroutineScope.launch {
                                            val target = if (isUpcomingDocked) 1f else 0f
                                            focusProgress.animateTo(target, animationSpec = springSpec)
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(dockColors.barBackground.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = dockColors.barBackground,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = strings.upcomingHeader,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = TextPrimary
                                            )
                                        )
                                        Text(
                                            text = strings.upcomingDrawerSubtitle(pendingUpcoming.size),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            val target = if (isUpcomingDocked) 1f else 0f
                                            focusProgress.animateTo(target, animationSpec = springSpec)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isUpcomingDocked) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Upcoming Task List (Scrollable when dominant)
                        if (pendingUpcoming.isNotEmpty() || completedUpcoming.isNotEmpty()) {
                            LazyColumn(
                                state = upcomingListState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(top = 2.dp, bottom = 120.dp)
                            ) {
                                items(
                                    pendingUpcoming,
                                    key = { "upcoming_${it.task.id}" },
                                    contentType = { "task" }
                                ) { taskWithSubTasks ->
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

                                if (completedUpcoming.isNotEmpty() && !isUpcomingDocked) {
                                    item(key = "header_completed_upcoming", contentType = "header") {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { isUpcomingCompletedExpanded = !isUpcomingCompletedExpanded }
                                                .padding(vertical = 6.dp, horizontal = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = strings.completedTasksSection(completedUpcoming.size),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = TextSecondary
                                                )
                                            )
                                            Icon(
                                                imageVector = if (isUpcomingCompletedExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = TextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    if (isUpcomingCompletedExpanded) {
                                        items(
                                            completedUpcoming,
                                            key = { "completed_upcoming_${it.task.id}" },
                                            contentType = { "task" }
                                        ) { taskWithSubTasks ->
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
                        } else {
                            // Empty state: completely remove the text that says "no upcoming tasks"
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
