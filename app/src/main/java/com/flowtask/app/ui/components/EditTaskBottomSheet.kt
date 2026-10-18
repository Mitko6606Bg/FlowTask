package com.flowtask.app.ui.components

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import kotlin.math.roundToInt
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import kotlin.math.abs
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.flowtask.app.ui.theme.LocalDockColors
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.state.ToggleableState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.flowtask.app.data.local.model.TaskWithSubTasks
import com.flowtask.app.ui.theme.BgLight
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.CheckboxUnchecked
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.OutlineVariant
import com.flowtask.app.ui.theme.SurfaceContainerHigh
import com.flowtask.app.ui.theme.SurfaceContainerLow
import com.flowtask.app.ui.theme.SurfaceContainerLowest
import com.flowtask.app.ui.theme.SurfaceLight
import com.flowtask.app.ui.theme.TextMuted
import com.flowtask.app.ui.theme.TextOnAccent
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.util.DateTimeUtils
import com.flowtask.app.util.LocalAppStrings
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditTaskBottomSheet(
    taskWithSubTasks: TaskWithSubTasks?,
    defaultDateMillis: Long?,
    defaultStartDateMillis: Long? = null,
    availableLayers: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Long?, String, Long?, Int?, String?, List<String>, Long?) -> Unit,
    onLongClickLayer: ((String) -> Unit)? = null,
    lastRenamedLayer: Pair<String, String>? = null,
    lastDeletedLayer: String? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    var isCardVisible by remember { mutableStateOf(false) }
    var isDismissing by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        isCardVisible = true
        delay(260)
        try {
            focusRequester.requestFocus()
            keyboardController?.show()
        } catch (_: Exception) {
            // Safe guard
        }
    }

    val animatedDismiss: () -> Unit = {
        if (!isDismissing) {
            isDismissing = true
            keyboardController?.hide()
            isCardVisible = false
            coroutineScope.launch {
                delay(220)
                onDismiss()
            }
        }
    }

    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    var predictiveBackEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }

    PredictiveBackHandler(enabled = isCardVisible && !isDismissing) { progress: Flow<BackEventCompat> ->
        try {
            progress.collect { backEvent ->
                predictiveBackProgress = backEvent.progress
                predictiveBackEdge = backEvent.swipeEdge
            }
            animatedDismiss()
        } catch (_: CancellationException) {
            predictiveBackProgress = 0f
        } finally {
            predictiveBackProgress = 0f
        }
    }

    val handleDismissThresholdPx = with(density) { 70.dp.toPx() }
    val contentDismissThresholdPx = with(density) { 180.dp.toPx() }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val scrollState = rememberScrollState()

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < 0f && dragOffsetY > 0f) {
                    val newOffset = (dragOffsetY + delta).coerceAtLeast(0f)
                    val consumed = dragOffsetY - newOffset
                    dragOffsetY = newOffset
                    return Offset(0f, -consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                // Zone 2: Intentional downward drag from content body (0.5f damping).
                // CRITICAL: Only accumulate from direct UserInput touch, NEVER from fling SideEffect!
                if (source == NestedScrollSource.UserInput && delta > 0f && scrollState.value == 0) {
                    dragOffsetY = (dragOffsetY + delta * 0.5f).coerceAtLeast(0f)
                    return Offset(0f, delta)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (dragOffsetY > 0f) {
                    // Firm threshold: only dismiss if intentionally pulled past 180dp of card offset (~360dp touch swipe)
                    if (dragOffsetY >= contentDismissThresholdPx) {
                        animatedDismiss()
                    } else {
                        Animatable(dragOffsetY).animateTo(
                            targetValue = 0f,
                            animationSpec = spring(dampingRatio = 0.82f, stiffness = 500f)
                        ) {
                            dragOffsetY = value
                        }
                    }
                    return available
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (dragOffsetY >= contentDismissThresholdPx) {
                    animatedDismiss()
                } else if (dragOffsetY > 0f) {
                    Animatable(dragOffsetY).animateTo(
                        targetValue = 0f,
                        animationSpec = spring(dampingRatio = 0.82f, stiffness = 500f)
                    ) {
                        dragOffsetY = value
                    }
                }
                return super.onPostFling(consumed, available)
            }
        }
    }

    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current

    var explanation by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedStartDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedTimeMinutes by remember { mutableStateOf<Int?>(null) }
    var selectedLayer by remember { mutableStateOf<String?>(null) }
    var isAddingNewLayer by remember { mutableStateOf(false) }
    var customLayerInput by remember { mutableStateOf("") }
    var isLayerSectionExpanded by remember { mutableStateOf(false) }
    var isDateSectionExpanded by remember { mutableStateOf(false) }
    var isTimeSectionExpanded by remember { mutableStateOf(false) }

    val subtasks = remember { mutableStateListOf<String>() }
    var newSubtaskInput by remember { mutableStateOf("") }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showDateRangePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    LaunchedEffect(lastRenamedLayer) {
        if (lastRenamedLayer != null && selectedLayer?.equals(lastRenamedLayer.first, ignoreCase = true) == true) {
            selectedLayer = lastRenamedLayer.second
        }
    }

    LaunchedEffect(lastDeletedLayer) {
        if (lastDeletedLayer != null && selectedLayer?.equals(lastDeletedLayer, ignoreCase = true) == true) {
            selectedLayer = null
        }
    }

    LaunchedEffect(taskWithSubTasks, defaultDateMillis, defaultStartDateMillis) {
        if (taskWithSubTasks != null) {
            explanation = taskWithSubTasks.task.explanation
            selectedDateMillis = taskWithSubTasks.task.dueDateMillis
            selectedStartDateMillis = taskWithSubTasks.task.startDateMillis
            selectedTimeMinutes = taskWithSubTasks.task.dueTimeMinutes
            selectedLayer = taskWithSubTasks.task.layer
            subtasks.clear()
            subtasks.addAll(taskWithSubTasks.subTasks.map { it.explanation })
        } else {
            explanation = ""
            selectedDateMillis = defaultDateMillis
            selectedStartDateMillis = defaultStartDateMillis
            selectedTimeMinutes = null
            selectedLayer = null
            subtasks.clear()
        }
        isAddingNewLayer = false
        customLayerInput = ""
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Transparent dismiss backdrop (NO black tint behind card)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = animatedDismiss
                )
        )

        AnimatedVisibility(
            visible = isCardVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(
                    durationMillis = 280,
                    easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
                )
            ) + fadeIn(animationSpec = tween(durationMillis = 180)),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(
                    durationMillis = 200,
                    easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
                )
            ) + fadeOut(animationSpec = tween(durationMillis = 150))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .graphicsLayer {
                        if (predictiveBackProgress > 0f) {
                            val scale = 1f - (predictiveBackProgress * 0.08f)
                            scaleX = scale
                            scaleY = scale
                            translationX = if (predictiveBackEdge == BackEventCompat.EDGE_LEFT) {
                                predictiveBackProgress * 24.dp.toPx()
                            } else {
                                -predictiveBackProgress * 24.dp.toPx()
                            }
                            translationY = predictiveBackProgress * 140.dp.toPx()
                            alpha = 1f - (predictiveBackProgress * 0.18f)
                        }
                    }
                    .offset { IntOffset(0, dragOffsetY.roundToInt().coerceAtLeast(0)) }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = SurfaceContainerLowest,
                border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.5f)),
                shadowElevation = 16.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(nestedScrollConnection)
                    ) {
                    // Pull Handle with direct fluid 1:1 vertical drag detector (Zone 1: Easy to dismiss)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .draggable(
                                orientation = Orientation.Vertical,
                                state = rememberDraggableState { delta ->
                                    dragOffsetY = (dragOffsetY + delta).coerceAtLeast(0f)
                                },
                                onDragStopped = { velocity ->
                                    if (dragOffsetY >= handleDismissThresholdPx || velocity > 800f) {
                                        animatedDismiss()
                                    } else {
                                        coroutineScope.launch {
                                            Animatable(dragOffsetY).animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(dampingRatio = 0.82f, stiffness = 500f)
                                            ) {
                                                dragOffsetY = value
                                            }
                                        }
                                    }
                                }
                            )
                            .padding(top = 14.dp, bottom = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 38.dp, height = 4.5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(TextSecondary.copy(alpha = 0.35f))
                        )
                    }

                    @Suppress("DEPRECATION")
                    @OptIn(ExperimentalFoundationApi::class)
                    CompositionLocalProvider(
                        LocalOverscrollConfiguration provides null,
                        LocalOverscrollFactory provides null
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 12.dp)
                                .imePadding()
                                .verticalScroll(scrollState)
                        ) {
                            // Header with Icon Badge (Zone 1: Also easy to pull down from top)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .draggable(
                                        orientation = Orientation.Vertical,
                                        state = rememberDraggableState { delta ->
                                            dragOffsetY = (dragOffsetY + delta).coerceAtLeast(0f)
                                        },
                                        onDragStopped = { velocity ->
                                            if (dragOffsetY >= handleDismissThresholdPx || velocity > 800f) {
                                                animatedDismiss()
                                            } else {
                                                coroutineScope.launch {
                                                    Animatable(dragOffsetY).animateTo(
                                                        targetValue = 0f,
                                                        animationSpec = spring(dampingRatio = 0.82f, stiffness = 500f)
                                                    ) {
                                                        dragOffsetY = value
                                                    }
                                                }
                                            }
                                        }
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(currentAccent.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (taskWithSubTasks == null) Icons.Default.TaskAlt else Icons.Default.EditNote,
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
                                IconButton(onClick = animatedDismiss) {
                                    Icon(Icons.Default.Close, contentDescription = strings.closeAction, tint = TextSecondary)
                                }
                            }

            Spacer(modifier = Modifier.height(18.dp))

            // Static Expanded Task Card Template (matches normal view TaskCard styling and container)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = SurfaceLight,
                border = null,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Checkbox (size 26.dp, matching normal view)
                        AnimatedCheckbox(
                            state = if (taskWithSubTasks?.task?.isCompleted == true) ToggleableState.On else ToggleableState.Off,
                            size = 26.dp,
                            onClick = null,
                            modifier = Modifier.padding(top = 2.dp, end = 4.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Task Content & Live Chips
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            // In-place editable Task Explanation
                            BasicTextField(
                                value = explanation,
                                onValueChange = { explanation = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = TextPrimary,
                                    lineHeight = 21.sp
                                ),
                                cursorBrush = SolidColor(currentAccent),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (explanation.isEmpty()) {
                                            Text(
                                                text = strings.taskExplanationPlaceholder,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 15.sp,
                                                    color = TextMuted,
                                                    lineHeight = 21.sp
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )

                            // Live Meta chips row (Layer, Date+Time, Period)
                            val isPeriodTask = selectedStartDateMillis != null && selectedDateMillis != null && selectedDateMillis!! > selectedStartDateMillis!!
                            val hasSchedule = isPeriodTask || selectedDateMillis != null || selectedTimeMinutes != null
                            val hasLayer = !selectedLayer.isNullOrBlank()

                            if (hasSchedule || hasLayer) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (hasLayer) {
                                        LayerChip(label = selectedLayer!!)
                                    }

                                    if (isPeriodTask) {
                                        val periodLabel = DateTimeUtils.formatDateRange(selectedStartDateMillis, selectedDateMillis)
                                        ScheduleChip(
                                            icon = Icons.Default.DateRange,
                                            label = periodLabel
                                        )
                                    } else if (hasSchedule) {
                                        val scheduleLabel = when {
                                            selectedDateMillis != null && selectedTimeMinutes != null -> {
                                                "${DateTimeUtils.formatDateLabel(selectedDateMillis)}, ${DateTimeUtils.formatTime(selectedTimeMinutes)}"
                                            }
                                            selectedDateMillis != null -> {
                                                DateTimeUtils.formatDateLabel(selectedDateMillis)
                                            }
                                            selectedTimeMinutes != null -> {
                                                DateTimeUtils.formatTime(selectedTimeMinutes)
                                            }
                                            else -> ""
                                        }
                                        val scheduleIcon = if (selectedTimeMinutes != null) Icons.Default.AccessTime else Icons.Default.CalendarToday

                                        ScheduleChip(
                                            icon = scheduleIcon,
                                            label = scheduleLabel
                                        )
                                    }
                                }
                            }
                        }

                        // Subtask count chip on the far right (matches normal view)
                        if (subtasks.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            SubtaskProgressChip(
                                completed = 0,
                                total = subtasks.size,
                                isExpanded = true,
                                onClick = {}
                            )
                        }
                    }

                    // Statically Expanded Subtasks Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 36.dp, end = 4.dp)
                        ) {
                            HorizontalDivider(
                                color = OutlineVariant.copy(alpha = 0.35f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            // Existing Subtasks Checklist
                            subtasks.forEachIndexed { index, subtask ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .padding(vertical = 4.dp, horizontal = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AnimatedCheckbox(
                                        checked = false,
                                        onCheckedChange = null,
                                        size = 20.dp
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = subtask,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp,
                                            color = TextPrimary,
                                            lineHeight = 20.sp
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(
                                        onClick = { subtasks.removeAt(index) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = strings.deleteAction,
                                            tint = TextMuted.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Inline Add Subtask Input Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(currentAccent.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = currentAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                BasicTextField(
                                    value = newSubtaskInput,
                                    onValueChange = { newSubtaskInput = it },
                                    modifier = Modifier.weight(1f),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        color = TextPrimary,
                                        lineHeight = 20.sp
                                    ),
                                    cursorBrush = SolidColor(currentAccent),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            if (newSubtaskInput.isNotBlank()) {
                                                subtasks.add(newSubtaskInput.trim())
                                                newSubtaskInput = ""
                                            }
                                        }
                                    ),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (newSubtaskInput.isEmpty()) {
                                                Text(
                                                    text = strings.addSubtaskPlaceholder,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontSize = 14.sp,
                                                        color = TextMuted,
                                                        lineHeight = 20.sp
                                                    )
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )

                                if (newSubtaskInput.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            if (newSubtaskInput.isNotBlank()) {
                                                subtasks.add(newSubtaskInput.trim())
                                                newSubtaskInput = ""
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = strings.addAction,
                                            tint = currentAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = OutlineVariant.copy(alpha = 0.35f),
                thickness = 1.dp
            )

            // Collapsible Section: Profile / Layer
            TaskOptionCollapsibleSection(
                title = strings.layerSectionTitle,
                icon = Icons.Default.Layers,
                isExpanded = isLayerSectionExpanded,
                onToggleExpanded = { isLayerSectionExpanded = !isLayerSectionExpanded },
                isActive = !selectedLayer.isNullOrBlank(),
                previewBadge = {
                    val layerLabel = if (!selectedLayer.isNullOrBlank()) selectedLayer!! else strings.noLayer
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!selectedLayer.isNullOrBlank()) currentAccent.copy(alpha = 0.15f) else SurfaceContainerHigh)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = layerLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (!selectedLayer.isNullOrBlank()) currentAccent else TextSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "No Layer" Option
                        PillOption(
                            label = strings.noLayer,
                            isSelected = selectedLayer.isNullOrBlank(),
                            onClick = {
                                selectedLayer = null
                                isAddingNewLayer = false
                            }
                        )

                        // Combined available layers + currently selected if not in list
                        val allUniqueLayers = remember(availableLayers, selectedLayer) {
                            val set = availableLayers.toMutableSet()
                            if (!selectedLayer.isNullOrBlank()) {
                                set.add(selectedLayer!!)
                            }
                            set.toList().sorted()
                        }

                        allUniqueLayers.forEach { layerName ->
                            PillOption(
                                label = layerName,
                                isSelected = selectedLayer.equals(layerName, ignoreCase = true),
                                onClick = {
                                    selectedLayer = layerName
                                    isAddingNewLayer = false
                                },
                                onLongClick = if (onLongClickLayer != null) {
                                    { onLongClickLayer(layerName) }
                                } else null
                            )
                        }

                        // Add Custom Layer Button
                        PillOption(
                            label = "+ ${strings.addLayerPrompt}",
                            isSelected = isAddingNewLayer,
                            icon = Icons.Default.Add,
                            onClick = { isAddingNewLayer = !isAddingNewLayer }
                        )
                    }

                    // Expandable Custom Layer Input
                    AnimatedVisibility(
                        visible = isAddingNewLayer,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customLayerInput,
                                onValueChange = { customLayerInput = it },
                                placeholder = { Text(strings.newLayerPlaceholder, fontSize = 13.sp, color = TextMuted) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceContainerLowest,
                                    unfocusedContainerColor = SurfaceContainerLowest,
                                    focusedBorderColor = currentAccent,
                                    unfocusedBorderColor = OutlineSubtle.copy(alpha = 0.7f)
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (customLayerInput.isNotBlank()) {
                                            val trimmed = customLayerInput.trim()
                                            selectedLayer = trimmed
                                            customLayerInput = ""
                                            isAddingNewLayer = false
                                        }
                                    }
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    if (customLayerInput.isNotBlank()) {
                                        val trimmed = customLayerInput.trim()
                                        selectedLayer = trimmed
                                        customLayerInput = ""
                                        isAddingNewLayer = false
                                    }
                                },
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(currentAccent)
                                    .expressiveBounce(pressScale = 0.92f)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = strings.saveAction, tint = TextOnAccent, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val todayStart = DateTimeUtils.getStartOfDay()
            val tomorrowStart = remember(todayStart) {
                Calendar.getInstance().apply {
                    timeInMillis = todayStart
                    add(Calendar.DAY_OF_YEAR, 1)
                }.timeInMillis
            }
            val isPeriod = selectedStartDateMillis != null && selectedDateMillis != null && selectedDateMillis!! > selectedStartDateMillis!!
            val hasCustomDate = selectedDateMillis != null || selectedStartDateMillis != null

            val datePreviewLabel = remember(selectedDateMillis, selectedStartDateMillis, strings) {
                when {
                    isPeriod -> "${DateTimeUtils.formatDateRange(selectedStartDateMillis, selectedDateMillis)}"
                    selectedDateMillis != null && DateTimeUtils.isToday(selectedDateMillis) -> if (strings == com.flowtask.app.util.BulgarianStrings) "Днес" else "Today"
                    selectedDateMillis != null && selectedDateMillis == tomorrowStart -> if (strings == com.flowtask.app.util.BulgarianStrings) "Утре" else "Tomorrow"
                    selectedDateMillis != null -> DateTimeUtils.formatDate(selectedDateMillis)
                    else -> if (strings == com.flowtask.app.util.BulgarianStrings) "Без дата" else "No Date"
                }
            }

            // Collapsible Section: Date Selection
            TaskOptionCollapsibleSection(
                title = strings.dateOptional,
                icon = Icons.Default.CalendarToday,
                isExpanded = isDateSectionExpanded,
                onToggleExpanded = { isDateSectionExpanded = !isDateSectionExpanded },
                isActive = hasCustomDate,
                previewBadge = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (hasCustomDate) currentAccent.copy(alpha = 0.15f) else SurfaceContainerHigh)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = datePreviewLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasCustomDate) currentAccent else TextSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isSingleCustomDate = selectedDateMillis != null &&
                            !isPeriod &&
                            !DateTimeUtils.isToday(selectedDateMillis) &&
                            selectedDateMillis != tomorrowStart

                    PillOption(
                        label = if (strings == com.flowtask.app.util.BulgarianStrings) "Без дата" else "No Date",
                        isSelected = selectedDateMillis == null && selectedStartDateMillis == null,
                        onClick = {
                            selectedDateMillis = null
                            selectedStartDateMillis = null
                        }
                    )

                    PillOption(
                        label = if (strings == com.flowtask.app.util.BulgarianStrings) "Днес" else "Today",
                        isSelected = !isPeriod && selectedDateMillis != null && DateTimeUtils.isToday(selectedDateMillis),
                        onClick = {
                            selectedDateMillis = todayStart
                            selectedStartDateMillis = null
                        }
                    )

                    PillOption(
                        label = if (strings == com.flowtask.app.util.BulgarianStrings) "Утре" else "Tomorrow",
                        isSelected = !isPeriod && selectedDateMillis == tomorrowStart,
                        onClick = {
                            selectedDateMillis = tomorrowStart
                            selectedStartDateMillis = null
                        }
                    )

                    PillOption(
                        label = if (isSingleCustomDate) DateTimeUtils.formatDate(selectedDateMillis) else (if (strings == com.flowtask.app.util.BulgarianStrings) "Календар..." else "Calendar..."),
                        icon = Icons.Default.CalendarToday,
                        isSelected = isSingleCustomDate,
                        onClick = {
                            selectedStartDateMillis = null
                            showDatePickerDialog = true
                        }
                    )

                    PillOption(
                        label = if (isPeriod) {
                            "${DateTimeUtils.formatDateRange(selectedStartDateMillis, selectedDateMillis)} (${strings.daysCount(DateTimeUtils.getDurationDays(selectedStartDateMillis!!, selectedDateMillis!!))})"
                        } else {
                            strings.taskPeriodOption
                        },
                        icon = Icons.Default.DateRange,
                        isSelected = isPeriod,
                        onClick = { showDateRangePickerDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val hasTime = selectedTimeMinutes != null
            val timePreviewLabel = remember(selectedTimeMinutes, strings) {
                if (selectedTimeMinutes != null) {
                    DateTimeUtils.formatTime(selectedTimeMinutes)
                } else {
                    if (strings == com.flowtask.app.util.BulgarianStrings) "Без час" else "No Time"
                }
            }

            // Collapsible Section: Time Selection
            TaskOptionCollapsibleSection(
                title = strings.timeOptional,
                icon = Icons.Default.AccessTime,
                isExpanded = isTimeSectionExpanded,
                onToggleExpanded = { isTimeSectionExpanded = !isTimeSectionExpanded },
                isActive = hasTime,
                previewBadge = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (hasTime) currentAccent.copy(alpha = 0.15f) else SurfaceContainerHigh)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = timePreviewLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasTime) currentAccent else TextSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PillOption(
                        label = if (strings == com.flowtask.app.util.BulgarianStrings) "Без час" else "No Time",
                        isSelected = selectedTimeMinutes == null,
                        onClick = { selectedTimeMinutes = null }
                    )

                    PillOption(
                        label = "09:00",
                        isSelected = selectedTimeMinutes == 540,
                        onClick = { selectedTimeMinutes = 540 }
                    )

                    PillOption(
                        label = "14:00",
                        isSelected = selectedTimeMinutes == 840,
                        onClick = { selectedTimeMinutes = 840 }
                    )

                    PillOption(
                        label = "18:00",
                        isSelected = selectedTimeMinutes == 1080,
                        onClick = { selectedTimeMinutes = 1080 }
                    )

                    val isCustomTime = selectedTimeMinutes != null &&
                            selectedTimeMinutes != 540 && selectedTimeMinutes != 840 && selectedTimeMinutes != 1080
                    PillOption(
                        label = if (isCustomTime) DateTimeUtils.formatTime(selectedTimeMinutes) else (if (strings == com.flowtask.app.util.BulgarianStrings) "Друг час..." else "Custom Time..."),
                        icon = Icons.Default.AccessTime,
                        isSelected = isCustomTime,
                        onClick = { showTimePickerDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Submit Button
            Button(
                onClick = {
                    if (explanation.isNotBlank()) {
                        onSave(
                            taskWithSubTasks?.task?.id,
                            explanation,
                            selectedDateMillis,
                            selectedTimeMinutes,
                            selectedLayer?.trim()?.ifBlank { null },
                            subtasks.toList(),
                            selectedStartDateMillis
                        )
                        animatedDismiss()
                    }
                },
                enabled = explanation.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .expressiveBounce(pressScale = 0.96f),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = currentAccent,
                    contentColor = TextOnAccent,
                    disabledContainerColor = SurfaceContainerHigh,
                    disabledContentColor = TextMuted
                )
            ) {
                Icon(
                    imageVector = if (taskWithSubTasks == null) Icons.Default.Add else Icons.Default.Check,
                    contentDescription = null,
                    tint = if (explanation.isNotBlank()) TextOnAccent else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (taskWithSubTasks == null) strings.saveTaskButton else strings.saveAction,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (explanation.isNotBlank()) TextOnAccent else TextMuted
                    )
                )
            }

            Spacer(modifier = Modifier.navigationBarsPadding().height(28.dp))
                        }
                    }
                }

                // Floating Pill Create Button docked right above keyboard on the top right
                val isImeVisible = WindowInsets.isImeVisible || WindowInsets.ime.getBottom(density) > 0
                val thresholdPx = with(density) { 70.dp.toPx() }
                val showFloatingCreateButton by remember(isImeVisible, thresholdPx) {
                    derivedStateOf {
                        if (!isImeVisible) {
                            false
                        } else {
                            val remainingScroll = scrollState.maxValue - scrollState.value
                            val isBottomButtonVisible = scrollState.maxValue == 0 || remainingScroll <= thresholdPx
                            !isBottomButtonVisible
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showFloatingCreateButton,
                    enter = fadeIn(animationSpec = tween(180)) + scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f)) + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut(animationSpec = tween(140)) + scaleOut(targetScale = 0.8f) + slideOutVertically(targetOffsetY = { it / 2 }),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .imePadding()
                        .padding(end = 20.dp, bottom = 12.dp)
                ) {
                    Surface(
                        onClick = {
                            if (explanation.isNotBlank()) {
                                onSave(
                                    taskWithSubTasks?.task?.id,
                                    explanation,
                                    selectedDateMillis,
                                    selectedTimeMinutes,
                                    selectedLayer?.trim()?.ifBlank { null },
                                    subtasks.toList(),
                                    selectedStartDateMillis
                                )
                                animatedDismiss()
                            }
                        },
                        enabled = explanation.isNotBlank(),
                        shape = CircleShape,
                        color = if (explanation.isNotBlank()) currentAccent else SurfaceContainerHigh,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .height(44.dp)
                            .expressiveBounce(pressScale = 0.94f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (taskWithSubTasks == null) Icons.Default.Add else Icons.Default.Check,
                                contentDescription = null,
                                tint = if (explanation.isNotBlank()) TextOnAccent else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (taskWithSubTasks == null) strings.saveTaskButton else strings.saveAction,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (explanation.isNotBlank()) TextOnAccent else TextMuted
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
                        showDatePickerDialog = false
                    }
                ) {
                    Text(strings.saveAction, color = currentAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text(strings.cancelAction, color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDateRangePickerDialog) {
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = selectedStartDateMillis ?: selectedDateMillis ?: System.currentTimeMillis(),
            initialSelectedEndDateMillis = selectedDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDateRangePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (dateRangePickerState.selectedStartDateMillis != null) {
                            selectedStartDateMillis = dateRangePickerState.selectedStartDateMillis
                            selectedDateMillis = dateRangePickerState.selectedEndDateMillis ?: dateRangePickerState.selectedStartDateMillis
                        }
                        showDateRangePickerDialog = false
                    }
                ) {
                    Text(strings.saveAction, color = currentAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePickerDialog = false }) {
                    Text(strings.cancelAction, color = TextSecondary)
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = {
                    Text(
                        text = strings.selectDateRangeTitle,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            )
        }
    }

    if (showTimePickerDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTimeMinutes?.div(60) ?: 9,
            initialMinute = selectedTimeMinutes?.rem(60) ?: 0,
            is24Hour = true
        )
        Dialog(onDismissRequest = { showTimePickerDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = SurfaceContainerLowest,
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f)),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        strings.timeOptional,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePickerDialog = false }) {
                            Text(strings.cancelAction, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                selectedTimeMinutes = timePickerState.hour * 60 + timePickerState.minute
                                showTimePickerDialog = false
                            }
                        ) {
                            Text(strings.saveAction, color = currentAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PillOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null
) {
    val dockColors = LocalDockColors.current
    val haptic = LocalHapticFeedback.current

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) dockColors.barBackground else SurfaceLight,
        border = BorderStroke(
            1.dp,
            if (isSelected) dockColors.barBackground else OutlineSubtle
        ),
        shadowElevation = 0.dp,
        modifier = modifier
            .expressiveBounce(pressScale = 0.94f)
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickable(
                        onClick = onClick,
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongClick()
                        }
                    )
                } else {
                    Modifier.clickable(onClick = onClick)
                }
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = dockColors.barBackground,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = label,
                color = if (isSelected) Color.White else TextPrimary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun TaskOptionCollapsibleSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    isActive: Boolean = false,
    previewBadge: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val currentAccent = LocalAccentColor.current
    val haptic = LocalHapticFeedback.current
    val headerInteractionSource = remember { MutableInteractionSource() }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
        label = "optionChevronRotation"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header Bar with Clean Tap toggle (frameless, no colored container box)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .expressiveBounce(interactionSource = headerInteractionSource, pressScale = 0.98f)
                .clickable(
                    interactionSource = headerInteractionSource,
                    indication = null
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleExpanded()
                }
                .padding(horizontal = 4.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) currentAccent else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                if (!isExpanded && previewBadge != null) {
                    previewBadge()
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer { rotationZ = chevronRotation }
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(160)) + expandVertically(tween(200, easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f))),
            exit = fadeOut(tween(130)) + shrinkVertically(tween(180, easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp, start = 4.dp, end = 4.dp)
            ) {
                content()
            }
        }

        HorizontalDivider(
            color = OutlineVariant.copy(alpha = 0.35f),
            thickness = 1.dp
        )
    }
}

