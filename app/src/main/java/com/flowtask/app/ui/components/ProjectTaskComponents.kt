package com.flowtask.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.state.ToggleableState
import com.flowtask.app.data.local.model.ProjectTaskWithSubTasks
import com.flowtask.app.ui.theme.*
import com.flowtask.app.util.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectTaskCard(
    taskWithSubTasks: ProjectTaskWithSubTasks,
    onToggleTask: (Long, Boolean) -> Unit,
    onToggleSubTask: (Long, Boolean) -> Unit,
    onEditTask: (ProjectTaskWithSubTasks) -> Unit,
    onMoveTask: () -> Unit,
    onDeleteTask: (Long) -> Unit,
    onDeleteSubTask: (Long) -> Unit,
    onAddSubTask: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val task = taskWithSubTasks.task
    val subTasks = taskWithSubTasks.subTasks

    var isSubtasksExpanded by remember { mutableStateOf(false) }
    var isAddingSubtask by remember { mutableStateOf(false) }
    var newSubtaskText by remember { mutableStateOf("") }

    val completedCount = subTasks.count { it.isCompleted }
    val totalSubtasks = subTasks.size

    val cardAlpha by animateFloatAsState(
        targetValue = if (task.isCompleted) 0.55f else 1.0f,
        label = "cardAlpha"
    )

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDeleteTask(task.id)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color = MaterialTheme.colorScheme.error
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(color)
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onError)
            }
        },
        modifier = modifier
            .padding(vertical = 4.dp)
            .expressiveBounce(pressScale = 0.985f)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(cardAlpha)
                .clickable { onEditTask(taskWithSubTasks) },
            shape = RoundedCornerShape(22.dp),
            color = SurfaceLight,
            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f)),
            shadowElevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.explanation,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = if (task.isCompleted) TextSecondary else TextPrimary,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
                        )

                        if (totalSubtasks > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { isSubtasksExpanded = !isSubtasksExpanded }
                                    .padding(vertical = 2.dp, horizontal = 4.dp)
                            ) {
                                val progressColor = if (completedCount == totalSubtasks && totalSubtasks > 0) currentAccent else TextMuted
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(progressColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = progressColor,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$completedCount/$totalSubtasks",
                                    style = MaterialTheme.typography.labelSmall.copy(color = progressColor, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = isSubtasksExpanded && totalSubtasks > 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, start = 34.dp)
                    ) {
                        subTasks.forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onToggleSubTask(sub.id, !sub.isCompleted) }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AnimatedCheckbox(
                                    checked = sub.isCompleted, onCheckedChange = { _ ->  onToggleSubTask(sub.id, !sub.isCompleted) },
                                    size = 18.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = sub.explanation,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (sub.isCompleted) TextSecondary else TextPrimary,
                                        textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onDeleteSubTask(sub.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

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
    val subtasks = remember { mutableStateListOf<String>().apply { taskWithSubTasks?.subTasks?.map { it.explanation }?.let { addAll(it) } } }
    var newSubtaskInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceLight,
        dragHandle = { BottomSheetDefaults.DragHandle(color = OutlineVariant) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (taskWithSubTasks == null) strings.newTaskTitle else strings.editTaskTitle,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = strings.closeAction, tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = explanation,
                onValueChange = { explanation = it },
                placeholder = { Text(strings.taskExplanationPlaceholder, color = TextMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BgLight,
                    unfocusedContainerColor = BgLight,
                    focusedBorderColor = currentAccent,
                    unfocusedBorderColor = OutlineSubtle
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(strings.subtasksHeader(subtasks.size), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                subtasks.forEachIndexed { index, subtask ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BgLight)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(subtask, color = TextPrimary, modifier = Modifier.weight(1f))
                        IconButton(onClick = { subtasks.removeAt(index) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newSubtaskInput,
                        onValueChange = { newSubtaskInput = it },
                        placeholder = { Text(strings.addSubtaskPlaceholder, color = TextMuted) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BgLight,
                            unfocusedContainerColor = BgLight,
                            focusedBorderColor = currentAccent,
                            unfocusedBorderColor = OutlineSubtle
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
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(currentAccent)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = TextOnAccent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { if (explanation.isNotBlank()) onSave(taskWithSubTasks?.task?.id, explanation, subtasks) },
                enabled = explanation.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DockBarColor, contentColor = TextOnAccent)
            ) {
                Text(strings.saveTaskButton, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}







