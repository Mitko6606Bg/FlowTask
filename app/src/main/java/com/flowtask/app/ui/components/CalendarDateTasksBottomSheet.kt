package com.flowtask.app.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowtask.app.data.local.model.ScheduleItem
import com.flowtask.app.data.local.model.SemesterConfig
import com.flowtask.app.data.local.model.TaskWithSubTasks
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.DockBarColor
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.SurfaceContainerLow
import com.flowtask.app.ui.theme.SurfaceContainerLowest
import com.flowtask.app.ui.theme.TextOnAccent
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.ui.viewmodel.TaskViewModel
import com.flowtask.app.util.DateTimeUtils
import com.flowtask.app.util.LocalAppStrings
import com.flowtask.app.util.SemesterUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarDateTasksBottomSheet(
    dateMillis: Long,
    tasks: List<TaskWithSubTasks>,
    semesterConfig: SemesterConfig,
    viewModel: TaskViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current

    val layerPrograms by viewModel.layerPrograms.collectAsStateWithLifecycle()
    val selectedCalendarProgramId by viewModel.selectedCalendarProgramId.collectAsStateWithLifecycle()

    val filteredPrograms = remember(layerPrograms, selectedCalendarProgramId) {
        when (selectedCalendarProgramId) {
            "NONE" -> emptyList()
            null -> {
                val firstEnabled = layerPrograms.firstOrNull { it.isEnabled } ?: layerPrograms.firstOrNull()
                if (firstEnabled != null) listOf(firstEnabled) else emptyList()
            }
            else -> layerPrograms.filter { it.id == selectedCalendarProgramId && it.isEnabled }
        }
    }

    val weekNumForDate = remember(dateMillis, semesterConfig) {
        if (semesterConfig.isEnabled) {
            SemesterUtils.getWeekNumber(dateMillis, semesterConfig.startDateMillis, semesterConfig.endDateMillis)
        } else null
    }

    val activeWeekNum = remember(dateMillis, semesterConfig, filteredPrograms) {
        weekNumForDate ?: run {
            for (prog in filteredPrograms) {
                if (prog.isEnabled && prog.trackWeeks && prog.startDateMillis != null && prog.endDateMillis != null) {
                    val w = SemesterUtils.getWeekNumber(dateMillis, prog.startDateMillis, prog.endDateMillis)
                    if (w != null) return@run w
                }
            }
            null
        }
    }

    val dayProgramItems = remember(dateMillis, filteredPrograms) {
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val d = cal.get(Calendar.DAY_OF_WEEK)
        val dayOfWeek = if (d == Calendar.SUNDAY) 7 else d - 1 // 1..7 (Mon..Sun)
        val list = mutableListOf<Pair<String, ScheduleItem>>()
        for (prog in filteredPrograms) {
            if (!prog.isEnabled) continue
            val inRange = (prog.startDateMillis == null || dateMillis >= DateTimeUtils.getStartOfDay(prog.startDateMillis)) &&
                          (prog.endDateMillis == null || dateMillis <= DateTimeUtils.getEndOfDay(prog.endDateMillis))
            if (inRange) {
                for (item in prog.scheduleItems) {
                    if (item.dayOfWeek == dayOfWeek) {
                        list.add(Pair(prog.layerName, item))
                    }
                }
            }
        }
        list.sortedBy { it.second.startMinutes }
    }

    val deduplicatedTasks = remember(tasks, dayProgramItems) {
        if (dayProgramItems.isEmpty()) tasks
        else {
            val routineSignatures = dayProgramItems.map {
                "${it.second.title.trim().lowercase()}_${it.second.startMinutes}"
            }.toSet()
            tasks.filter { taskWithSubTasks ->
                val sig = "${taskWithSubTasks.task.explanation.trim().lowercase()}_${taskWithSubTasks.task.dueTimeMinutes ?: -1}"
                sig !in routineSignatures
            }
        }
    }

    val dateLabel = remember(dateMillis) {
        DateTimeUtils.formatDateLabel(dateMillis)
    }

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
            // Pinned Card Header: Date Badge + Title/Count + Add Button + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(currentAccent.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = currentAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val subtitle = if (activeWeekNum != null) {
                            "${strings.semesterWeekHeader(activeWeekNum)} • ${strings.allTasksActiveCount(tasks.size)}"
                        } else {
                            strings.allTasksActiveCount(tasks.size)
                        }

                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Chunky, elevated + Button for adding a task directly for this date
                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .expressiveBounce(pressScale = 0.90f)
                            .clickable {
                                viewModel.openAddTask(dateMillis)
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = currentAccent,
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = strings.newTaskTitle,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .expressiveBounce(pressScale = 0.90f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.closeAction,
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (tasks.isEmpty() && dayProgramItems.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = SurfaceContainerLow,
                    border = null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(currentAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventAvailable,
                                contentDescription = null,
                                tint = currentAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = strings.noTasksForDate,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.openAddTask(dateMillis) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DockBarColor,
                                contentColor = TextOnAccent
                            ),
                            modifier = Modifier.expressiveBounce(pressScale = 0.94f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = TextOnAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.newTaskTitle,
                                color = TextOnAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    contentPadding = PaddingValues(top = 2.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (dayProgramItems.isNotEmpty()) {
                        item(key = "program_schedule_header") {
                            Text(
                                text = strings.taskScheduleTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = currentAccent
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        items(
                            dayProgramItems,
                            key = { "prog_${it.second.id}" },
                            contentType = { "program_item" }
                        ) { (layerName, item) ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = SurfaceContainerLow,
                                border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = currentAccent.copy(alpha = 0.12f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AccessTime,
                                                    contentDescription = null,
                                                    tint = currentAccent,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = DateTimeUtils.formatTime(item.startMinutes),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = currentAccent,
                                                        fontSize = 12.sp
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = item.subject,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary,
                                                    fontSize = 14.sp
                                                )
                                            )
                                            if (!item.roomOrNote.isNullOrBlank()) {
                                                Text(
                                                    text = item.roomOrNote,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = TextSecondary,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    LayerChip(label = layerName)
                                }
                            }
                        }

                        if (deduplicatedTasks.isNotEmpty()) {
                            item(key = "tasks_divider_header") {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = strings.allTasksHeader,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextSecondary
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (deduplicatedTasks.isNotEmpty()) {
                        items(
                            deduplicatedTasks,
                            key = { it.task.id },
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
                                showDateChip = false
                            )
                        }
                    }
                }
            }
        }
    }
}
