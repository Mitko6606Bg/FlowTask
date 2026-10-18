package com.flowtask.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.flowtask.app.data.local.entity.ProjectEntity
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.OutlineVariant
import com.flowtask.app.ui.theme.SurfaceContainerLow
import com.flowtask.app.ui.theme.SurfaceLight
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.getM3TonalProjectColor
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.util.LocalAppStrings

@Composable
fun ProjectActionPopup(
    project: ProjectEntity,
    onDismiss: () -> Unit,
    onViewDetails: () -> Unit,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
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

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.42f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    initialScale = 0.88f
                ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                exit = scaleOut(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    targetScale = 0.88f
                ) + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {} // Consume click
                        ),
                    shape = RoundedCornerShape(24.dp),
                    color = SurfaceContainerLow,
                    border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.55f)),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        // Header: Project Preview
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(24.dp)
                                    .clip(CircleShape)
                                    .background(projectColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(projectColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = null,
                                    tint = projectColor,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = project.name,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            if (project.isPinned) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = strings.projectPinnedBadge,
                                    tint = projectColor,
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = strings.closeAction,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        HorizontalDivider(
                            color = OutlineVariant.copy(alpha = 0.35f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
                        )

                        // Action Items
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            PopupActionRow(
                                icon = Icons.Default.Info,
                                iconTint = currentAccent,
                                label = strings.viewDetailsAction,
                                onClick = onViewDetails
                            )

                            PopupActionRow(
                                icon = Icons.Default.PushPin,
                                iconTint = if (project.isPinned) projectColor else currentAccent,
                                label = if (project.isPinned) strings.unpinProjectAction else strings.pinProjectAction,
                                onClick = onTogglePin
                            )

                            PopupActionRow(
                                icon = Icons.Default.Edit,
                                iconTint = currentAccent,
                                label = strings.editProjectAction,
                                onClick = onEdit
                            )

                            PopupActionRow(
                                icon = Icons.Default.FolderOpen,
                                iconTint = projectColor,
                                label = strings.openProjectAction,
                                onClick = onOpen
                            )

                            HorizontalDivider(
                                color = OutlineVariant.copy(alpha = 0.35f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            PopupActionRow(
                                icon = Icons.Default.DeleteOutline,
                                label = strings.deleteProjectTitle,
                                isDestructive = true,
                                onClick = onDelete
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PopupActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    iconTint: Color = LocalAccentColor.current,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val tint = if (isDestructive) MaterialTheme.colorScheme.error else TextPrimary
    val finalIconTint = if (isDestructive) MaterialTheme.colorScheme.error else iconTint
    val iconBg = if (isDestructive) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.30f)
    } else {
        SurfaceContainerLow
    }

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .expressiveBounce(pressScale = 0.97f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = finalIconTint,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = tint
                ),
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
