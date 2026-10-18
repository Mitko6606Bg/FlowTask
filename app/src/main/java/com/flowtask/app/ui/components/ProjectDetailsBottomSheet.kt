package com.flowtask.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowtask.app.data.local.entity.ProjectEntity
import com.flowtask.app.data.local.entity.ProjectFolderEntity
import com.flowtask.app.data.local.entity.ProjectNoteEntity
import com.flowtask.app.data.local.model.ProjectTaskWithSubTasks
import com.flowtask.app.ui.theme.DockBarColor
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.OutlineVariant
import com.flowtask.app.ui.theme.SurfaceContainerLight
import com.flowtask.app.ui.theme.SurfaceContainerLowest
import com.flowtask.app.ui.theme.TextMuted
import com.flowtask.app.ui.theme.TextOnAccent
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.getM3TonalProjectColor
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.util.DateTimeUtils
import com.flowtask.app.util.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsBottomSheet(
    project: ProjectEntity,
    tasks: List<ProjectTaskWithSubTasks>,
    folders: List<ProjectFolderEntity>,
    notes: List<ProjectNoteEntity>,
    onDismiss: () -> Unit,
    onOpenProject: () -> Unit,
    onTogglePin: () -> Unit,
    onEditProject: () -> Unit,
    onDeleteProject: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current

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

    val completedTasksCount = remember(tasks) { tasks.count { it.task.isCompleted } }

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
            // Pinned Header
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
                            .size(40.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(projectColor.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = projectColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pin Button
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier
                            .size(36.dp)
                            .expressiveBounce(pressScale = 0.90f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = if (project.isPinned) strings.unpinProjectAction else strings.pinProjectAction,
                            tint = if (project.isPinned) projectColor else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Edit Button
                    IconButton(
                        onClick = {
                            onDismiss()
                            onEditProject()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .expressiveBounce(pressScale = 0.90f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = strings.editProjectAction,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Delete Button
                    IconButton(
                        onClick = {
                            onDismiss()
                            onDeleteProject()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .expressiveBounce(pressScale = 0.90f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = strings.deleteProjectTitle,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Close Button
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

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Card 1: Description Card
                item(key = "description_card") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = SurfaceContainerLight,
                        border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {
                            Text(
                                text = strings.projectDescLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (project.description.isNotBlank()) {
                                Text(
                                    text = project.description,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp,
                                        color = TextPrimary
                                    )
                                )
                            } else {
                                Text(
                                    text = strings.projectNoDescription,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = TextMuted
                                    )
                                )
                            }
                        }
                    }
                }

                // Card 2: 3 Stat Tiles in a Row
                item(key = "stats_card") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Tasks Stat Tile
                        ProjectStatTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.AutoMirrored.Filled.PlaylistAddCheck,
                            iconTint = projectColor,
                            title = strings.projectStatsTasks,
                            value = "${completedTasksCount}/${tasks.size}"
                        )

                        // Folders Stat Tile
                        ProjectStatTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Folder,
                            iconTint = currentAccent,
                            title = strings.projectStatsFolders,
                            value = "${folders.size}"
                        )

                        // Notes Stat Tile
                        ProjectStatTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.NoteAlt,
                            iconTint = Color(0xFFD97706),
                            title = strings.projectStatsNotes,
                            value = "${notes.size}"
                        )
                    }
                }

                // Card 3: Timeline & Meta Info
                item(key = "timeline_card") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = SurfaceContainerLight,
                        border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = strings.projectTimelineCreated,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 13.sp
                                        )
                                    )
                                }
                                Text(
                                    text = DateTimeUtils.formatDate(project.createdAtMillis),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        fontSize = 13.sp
                                    )
                                )
                            }

                            HorizontalDivider(
                                color = OutlineVariant.copy(alpha = 0.35f),
                                thickness = 1.dp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = strings.projectTimelineUpdated,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 13.sp
                                        )
                                    )
                                }
                                Text(
                                    text = DateTimeUtils.formatDate(project.updatedAtMillis),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // Card 4: Open Project Button
                item(key = "open_action_button") {
                    Button(
                        onClick = {
                            onDismiss()
                            onOpenProject()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .expressiveBounce(pressScale = 0.98f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DockBarColor,
                            contentColor = TextOnAccent
                        )
                    ) {
                        Text(
                            text = strings.openProjectAction,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextOnAccent
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = TextOnAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}

@Composable
private fun ProjectStatTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = SurfaceContainerLight,
        border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    color = TextSecondary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
