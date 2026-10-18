package com.flowtask.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.state.ToggleableState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowtask.app.data.local.entity.SubTaskEntity
import com.flowtask.app.data.local.model.TaskWithSubTasks
import com.flowtask.app.ui.theme.BgLight
import com.flowtask.app.ui.theme.CheckboxUnchecked
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.OutlineVariant
import com.flowtask.app.ui.theme.SurfaceLight
import com.flowtask.app.ui.theme.TextMuted
import com.flowtask.app.ui.theme.TextOnAccent
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.util.DateTimeUtils
import com.flowtask.app.util.LocalAppStrings

import com.flowtask.app.ui.theme.SurfaceContainerLow
import com.flowtask.app.ui.theme.expressiveBounce

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskCard(
    taskWithSubTasks: TaskWithSubTasks,
    onToggleTask: (Long, Boolean) -> Unit,
    onToggleSubTask: (Long, Boolean) -> Unit,
    onEditTask: (TaskWithSubTasks) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onDeleteSubTask: (Long) -> Unit,
    onAddSubTask: (Long, String) -> Unit,
    canReorder: Boolean = false,
    canMoveUp: Boolean = false,
    canMoveDown: Boolean = false,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    showDateChip: Boolean = false,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val task = taskWithSubTasks.task
    val subTasks = taskWithSubTasks.subTasks

    val hapticFeedback = LocalHapticFeedback.current
    val cardInteractionSource = remember { MutableInteractionSource() }
    var showActionsPopup by remember { mutableStateOf(false) }
    var showDetailsSheet by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val completedCount = subTasks.count { it.isCompleted }
    val totalSubtasks = subTasks.size

    val cardAlpha by animateFloatAsState(
        targetValue = if (task.isCompleted) 0.55f else 1.0f,
        label = "cardAlpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .expressiveBounce(interactionSource = cardInteractionSource, pressScale = 0.985f),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceLight,
        border = null,
        shadowElevation = if (task.isCompleted) 0.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tri-state Checkbox (M3 Expressive)
                val toggleableState = calculateSubtaskToggleableState(
                    isTaskCompleted = task.isCompleted,
                    totalSubtasks = totalSubtasks,
                    completedSubtasks = completedCount
                )

                AnimatedCheckbox(
                    state = toggleableState,
                    onClick = { onToggleTask(task.id, !task.isCompleted) },
                    modifier = Modifier.padding(end = 4.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Task Content (Clickable to open details sheet, hold to open custom action popup)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .combinedClickable(
                            interactionSource = cardInteractionSource,
                            indication = null,
                            onClick = {
                                showDetailsSheet = true
                            },
                            onLongClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                showActionsPopup = true
                            }
                        )
                ) {
                    Text(
                        text = task.explanation,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                            fontSize = 15.sp,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (task.isCompleted) TextMuted else TextPrimary,
                            lineHeight = 21.sp
                        )
                    )

                    // Unified Meta chips row (Layer, Date+Time, Period)
                    val isPeriodTask = task.startDateMillis != null && task.dueDateMillis != null
                    val hasSchedule = isPeriodTask || (showDateChip && task.dueDateMillis != null) || task.dueTimeMinutes != null
                    val hasLayer = !task.layer.isNullOrBlank()

                    if (hasSchedule || hasLayer) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (hasLayer) {
                                LayerChip(label = task.layer!!)
                            }

                            if (isPeriodTask) {
                                val periodLabel = DateTimeUtils.formatDateRange(task.startDateMillis, task.dueDateMillis)
                                ScheduleChip(
                                    icon = Icons.Default.DateRange,
                                    label = periodLabel
                                )
                            } else if (hasSchedule) {
                                val scheduleLabel = when {
                                    showDateChip && task.dueDateMillis != null && task.dueTimeMinutes != null -> {
                                        "${DateTimeUtils.formatDateLabel(task.dueDateMillis)}, ${DateTimeUtils.formatTime(task.dueTimeMinutes)}"
                                    }
                                    showDateChip && task.dueDateMillis != null -> {
                                        DateTimeUtils.formatDateLabel(task.dueDateMillis)
                                    }
                                    task.dueTimeMinutes != null -> {
                                        DateTimeUtils.formatTime(task.dueTimeMinutes)
                                    }
                                    else -> ""
                                }
                                val scheduleIcon = if (task.dueTimeMinutes != null) Icons.Default.AccessTime else Icons.Default.CalendarToday

                                ScheduleChip(
                                    icon = scheduleIcon,
                                    label = scheduleLabel
                                )
                            }
                        }
                    }
                }

                // Subtask Progress Chip (Far right, tap or swipe down to expand)
                if (totalSubtasks > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    SubtaskProgressChip(
                        completed = completedCount,
                        total = totalSubtasks,
                        isExpanded = isExpanded,
                        onClick = { isExpanded = !isExpanded },
                        onSwipeDown = { isExpanded = true },
                        onSwipeUp = { isExpanded = false }
                    )
                }
            }

            // Inline Spring Pull-Down Expandable Subtasks
            AnimatedVisibility(
                visible = isExpanded && totalSubtasks > 0,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(animationSpec = tween(150)),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeOut(animationSpec = tween(120))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 2.dp)
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
                        subTasks.forEach { subTask ->
                            SubTaskItemRow(
                                subTask = subTask,
                                onToggle = { onToggleSubTask(subTask.id, it) },
                                onDelete = { onDeleteSubTask(subTask.id) }
                            )
                        }
                    }

                    // Bar Handle to Close Subtasks (Tap or swipe up to collapse)
                    SubtaskCloseHandle(
                        onClose = { isExpanded = false }
                    )
                }
            }
        }
    }

    // Modal Details Sheet (Tucked away neatly)
    if (showDetailsSheet) {
        TaskDetailsBottomSheet(
            taskWithSubTasks = taskWithSubTasks,
            onDismiss = { showDetailsSheet = false },
            onToggleTask = onToggleTask,
            onToggleSubTask = onToggleSubTask,
            onAddSubTask = onAddSubTask,
            onDeleteSubTask = onDeleteSubTask,
            onEditTask = {
                showDetailsSheet = false
                onEditTask(it)
            },
            onDeleteTask = {
                showDetailsSheet = false
                onDeleteTask(it)
            }
        )
    }

    // Custom Actions Popup (Revealed on hold / long-press)
    if (showActionsPopup) {
        TaskActionPopup(
            taskWithSubTasks = taskWithSubTasks,
            canReorder = canReorder,
            canMoveUp = canMoveUp,
            canMoveDown = canMoveDown,
            onDismiss = { showActionsPopup = false },
            onViewDetails = {
                showActionsPopup = false
                showDetailsSheet = true
            },
            onEdit = {
                showActionsPopup = false
                onEditTask(taskWithSubTasks)
            },
            onAddSubtask = {
                showActionsPopup = false
                showDetailsSheet = true
            },
            onMoveUp = {
                showActionsPopup = false
                onMoveUp()
            },
            onMoveDown = {
                showActionsPopup = false
                onMoveDown()
            },
            onDelete = {
                showActionsPopup = false
                onDeleteTask(task.id)
            }
        )
    }
}

fun calculateSubtaskToggleableState(
    isTaskCompleted: Boolean,
    totalSubtasks: Int,
    completedSubtasks: Int
): ToggleableState {
    return when {
        isTaskCompleted -> ToggleableState.On
        totalSubtasks > 0 && completedSubtasks > 0 -> ToggleableState.Indeterminate
        else -> ToggleableState.Off
    }
}

@Composable
fun AnimatedCheckbox(
    state: ToggleableState,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp
) {
    val currentAccent = LocalAccentColor.current
    val isActive = state != ToggleableState.Off

    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 450f),
        label = "checkboxScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isActive) currentAccent else Color.Transparent,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 500f),
        label = "checkboxBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isActive) currentAccent else CheckboxUnchecked,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 500f),
        label = "checkboxBorder"
    )

    val iconSize = if (size < 22.dp) 12.dp else 16.dp

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClick() }
    } else Modifier

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.8.dp, borderColor, CircleShape)
            .then(clickableModifier),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                (fadeIn(animationSpec = tween(140)) + scaleIn(initialScale = 0.7f, animationSpec = tween(140)))
                    .togetherWith(fadeOut(animationSpec = tween(140)) + scaleOut(targetScale = 0.7f, animationSpec = tween(140)))
            },
            label = "checkboxIcon"
        ) { targetState ->
            when (targetState) {
                ToggleableState.On -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = TextOnAccent,
                        modifier = Modifier.size(iconSize)
                    )
                }
                ToggleableState.Indeterminate -> {
                    Box(
                        modifier = Modifier
                            .width(if (size < 22.dp) 8.dp else 11.dp)
                            .height(if (size < 22.dp) 2.dp else 2.5.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(TextOnAccent)
                    )
                }
                ToggleableState.Off -> {
                    Box(modifier = Modifier.size(iconSize))
                }
            }
        }
    }
}

@Composable
fun AnimatedCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp
) {
    AnimatedCheckbox(
        state = if (checked) ToggleableState.On else ToggleableState.Off,
        onClick = onCheckedChange?.let { callback -> { callback(!checked) } },
        modifier = modifier,
        size = size
    )
}

@Composable
fun SubTaskItemRow(
    subTask: SubTaskEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentAccent = LocalAccentColor.current
    val strings = LocalAppStrings.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onToggle(!subTask.isCompleted) }
            .padding(vertical = 5.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedCheckbox(
            checked = subTask.isCompleted,
            onCheckedChange = null,
            size = 20.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = subTask.explanation,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                textDecoration = if (subTask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                color = if (subTask.isCompleted) TextMuted else TextPrimary,
                lineHeight = 20.sp
            ),
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onDelete,
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

@Composable
fun ScheduleChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = SurfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SubtaskProgressChip(
    completed: Int,
    total: Int,
    isExpanded: Boolean = false,
    onClick: () -> Unit = {},
    onSwipeDown: (() -> Unit)? = null,
    onSwipeUp: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currentAccent = LocalAccentColor.current
    val haptic = LocalHapticFeedback.current
    val chipInteractionSource = remember { MutableInteractionSource() }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "subtaskChevronRotation"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .expressiveBounce(interactionSource = chipInteractionSource, pressScale = 0.94f)
            .pointerInput(isExpanded) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val press = PressInteraction.Press(down.position)
                    chipInteractionSource.tryEmit(press)
                    var isDrag = false
                    val slop = viewConfiguration.touchSlop

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            chipInteractionSource.tryEmit(PressInteraction.Release(press))
                            if (!isDrag) {
                                onClick()
                            }
                            break
                        }
                        val dy = change.position.y - down.position.y
                        if (kotlin.math.abs(dy) > slop) {
                            isDrag = true
                            if (!isExpanded && dy > slop) {
                                change.consume()
                                chipInteractionSource.tryEmit(PressInteraction.Release(press))
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                (onSwipeDown ?: onClick).invoke()
                                break
                            } else if (isExpanded && dy < -slop) {
                                change.consume()
                                chipInteractionSource.tryEmit(PressInteraction.Release(press))
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                (onSwipeUp ?: onClick).invoke()
                                break
                            }
                        }
                    }
                }
            },
        shape = RoundedCornerShape(12.dp),
        color = if (completed == total) currentAccent.copy(alpha = 0.15f) else SurfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (completed == total) currentAccent else TextSecondary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "$completed/$total",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (completed == total) currentAccent else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.width(3.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = if (completed == total) currentAccent else TextSecondary,
                modifier = Modifier
                    .size(13.dp)
                    .graphicsLayer { rotationZ = chevronRotation }
            )
        }
    }
}

@Composable
fun LayerChip(
    label: String,
    modifier: Modifier = Modifier
) {
    val currentAccent = LocalAccentColor.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = currentAccent.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Layers,
                contentDescription = null,
                tint = currentAccent,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = currentAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 110.dp)
            )
        }
    }
}

@Composable
fun SubtaskCloseHandle(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val handleInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .expressiveBounce(interactionSource = handleInteractionSource, pressScale = 0.90f)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val press = PressInteraction.Press(down.position)
                    handleInteractionSource.tryEmit(press)
                    var isDrag = false
                    val slop = viewConfiguration.touchSlop

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            handleInteractionSource.tryEmit(PressInteraction.Release(press))
                            if (!isDrag) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onClose()
                            }
                            break
                        }
                        val dy = change.position.y - down.position.y
                        if (kotlin.math.abs(dy) > slop) {
                            isDrag = true
                            if (dy < -slop || dy < 0) {
                                change.consume()
                                handleInteractionSource.tryEmit(PressInteraction.Release(press))
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onClose()
                                break
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(OutlineVariant.copy(alpha = 0.85f))
        )
    }
}

