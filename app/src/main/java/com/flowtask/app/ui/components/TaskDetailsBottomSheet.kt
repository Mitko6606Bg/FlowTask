package com.flowtask.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.state.ToggleableState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowtask.app.data.local.model.TaskWithSubTasks
import com.flowtask.app.ui.theme.BgLight
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.OutlineVariant
import com.flowtask.app.ui.theme.SurfaceContainerLowest
import com.flowtask.app.ui.theme.SurfaceLight
import com.flowtask.app.ui.theme.TextMuted
import com.flowtask.app.ui.theme.TextOnAccent
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.util.DateTimeUtils
import com.flowtask.app.util.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsBottomSheet(
    taskWithSubTasks: TaskWithSubTasks,
    onDismiss: () -> Unit,
    onToggleTask: (Long, Boolean) -> Unit,
    onToggleSubTask: (Long, Boolean) -> Unit,
    onAddSubTask: (Long, String) -> Unit,
    onDeleteSubTask: (Long) -> Unit,
    onEditTask: (TaskWithSubTasks) -> Unit,
    onDeleteTask: (Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val task = taskWithSubTasks.task
    val subTasks = taskWithSubTasks.subTasks

    var newSubtaskText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 6.dp)
        ) {
            // Pinned Header: Icon Badge + Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = currentAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = strings.taskDetailsTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            onDismiss()
                            onEditTask(taskWithSubTasks)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .expressiveBounce(pressScale = 0.90f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = strings.editAction,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = {
                            onDismiss()
                            onDeleteTask(task.id)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .expressiveBounce(pressScale = 0.90f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = strings.deleteAction,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .expressiveBounce(pressScale = 0.90f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.closeAction,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Hero Task Card Template (matches normal view TaskCard styling and container)
                item(key = "task_and_subtasks_card") {
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
                                val completedCount = subTasks.count { it.isCompleted }
                                val totalSubtasks = subTasks.size
                                val toggleableState = calculateSubtaskToggleableState(
                                    isTaskCompleted = task.isCompleted,
                                    totalSubtasks = totalSubtasks,
                                    completedSubtasks = completedCount
                                )

                                // Checkbox (size 26.dp, matching normal view)
                                AnimatedCheckbox(
                                    state = toggleableState,
                                    size = 26.dp,
                                    onClick = { onToggleTask(task.id, !task.isCompleted) },
                                    modifier = Modifier.padding(top = 2.dp, end = 4.dp)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                // Task Content & Live Chips
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = task.explanation,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = if (task.isCompleted) TextMuted else TextPrimary,
                                            lineHeight = 21.sp,
                                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                        )
                                    )

                                    // Live Meta chips row (Layer, Date+Time, Period)
                                    val isPeriodTask = task.startDateMillis != null && task.dueDateMillis != null && task.dueDateMillis!! > task.startDateMillis!!
                                    val hasSchedule = isPeriodTask || task.dueDateMillis != null || task.dueTimeMinutes != null
                                    val hasLayer = !task.layer.isNullOrBlank()

                                    if (hasSchedule || hasLayer) {
                                        Spacer(modifier = Modifier.height(8.dp))
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
                                                    task.dueDateMillis != null && task.dueTimeMinutes != null -> {
                                                        "${DateTimeUtils.formatDateLabel(task.dueDateMillis)}, ${DateTimeUtils.formatTime(task.dueTimeMinutes)}"
                                                    }
                                                    task.dueDateMillis != null -> {
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

                                // Subtask count chip on the far right (matches normal view)
                                if (subTasks.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    SubtaskProgressChip(
                                        completed = completedCount,
                                        total = subTasks.size,
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
                                    subTasks.forEach { subTask ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { onToggleSubTask(subTask.id, !subTask.isCompleted) }
                                                .padding(vertical = 4.dp, horizontal = 2.dp),
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
                                                    color = if (subTask.isCompleted) TextMuted else TextPrimary,
                                                    lineHeight = 20.sp,
                                                    textDecoration = if (subTask.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )

                                            IconButton(
                                                onClick = { onDeleteSubTask(subTask.id) },
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
                                            value = newSubtaskText,
                                            onValueChange = { newSubtaskText = it },
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
                                                    if (newSubtaskText.isNotBlank()) {
                                                        onAddSubTask(task.id, newSubtaskText.trim())
                                                        newSubtaskText = ""
                                                    }
                                                }
                                            ),
                                            decorationBox = { innerTextField ->
                                                Box(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    contentAlignment = Alignment.CenterStart
                                                ) {
                                                    if (newSubtaskText.isEmpty()) {
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

                                        if (newSubtaskText.isNotBlank()) {
                                            IconButton(
                                                onClick = {
                                                    if (newSubtaskText.isNotBlank()) {
                                                        onAddSubTask(task.id, newSubtaskText.trim())
                                                        newSubtaskText = ""
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
                }

                // Frameless Schedule & Details Section
                val isPeriodTask = task.startDateMillis != null && task.dueDateMillis != null && task.dueDateMillis!! > task.startDateMillis!!
                val hasDate = isPeriodTask || task.dueDateMillis != null
                val hasTime = task.dueTimeMinutes != null
                val hasLayer = !task.layer.isNullOrBlank()

                item(key = "divider_schedule") {
                    HorizontalDivider(
                        color = OutlineVariant.copy(alpha = 0.35f),
                        thickness = 1.dp
                    )
                }

                // Date / Period Row
                item(key = "schedule_date_row") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(currentAccent.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPeriodTask) Icons.Default.DateRange else Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = currentAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (isPeriodTask) strings.taskPeriodOption else strings.dateOptional.substringBefore(" ("),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isPeriodTask) {
                                    "${DateTimeUtils.formatDateRange(task.startDateMillis, task.dueDateMillis)} (${strings.daysCount(DateTimeUtils.getDurationDays(task.startDateMillis!!, task.dueDateMillis!!))})"
                                } else {
                                    task.dueDateMillis?.let { DateTimeUtils.formatDate(it) } ?: strings.semesterNotSet
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = if (hasDate) TextSecondary else TextMuted,
                                    fontSize = 14.sp
                                )
                            )
                            if (isPeriodTask && DateTimeUtils.isDateInPeriod(System.currentTimeMillis(), task.startDateMillis, task.dueDateMillis)) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = currentAccent.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = strings.periodInProgress,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = currentAccent
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item(key = "divider_time") {
                    HorizontalDivider(
                        color = OutlineVariant.copy(alpha = 0.35f),
                        thickness = 1.dp
                    )
                }

                // Time Row
                item(key = "schedule_time_row") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(currentAccent.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = currentAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = strings.timeOptional.substringBefore(" ("),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            )
                        }

                        Text(
                            text = task.dueTimeMinutes?.let { DateTimeUtils.formatTime(it) } ?: strings.noTimeSet,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = if (hasTime) TextSecondary else TextMuted,
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                if (hasLayer) {
                    item(key = "divider_layer") {
                        HorizontalDivider(
                            color = OutlineVariant.copy(alpha = 0.35f),
                            thickness = 1.dp
                        )
                    }

                    // Layer Row
                    item(key = "layer_row") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(currentAccent.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Layers,
                                        contentDescription = null,
                                        tint = currentAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = strings.layerSectionTitle,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                )
                            }

                            LayerChip(label = task.layer!!)
                        }
                    }
                }

                item(key = "divider_bottom") {
                    HorizontalDivider(
                        color = OutlineVariant.copy(alpha = 0.35f),
                        thickness = 1.dp
                    )
                }

                item(key = "edit_btn_spacer") {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Primary Edit Button
                item(key = "edit_btn") {
                    Button(
                        onClick = {
                            onDismiss()
                            onEditTask(taskWithSubTasks)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .expressiveBounce(pressScale = 0.96f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = currentAccent,
                            contentColor = TextOnAccent
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = TextOnAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.editTaskTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextOnAccent
                            )
                        )
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
