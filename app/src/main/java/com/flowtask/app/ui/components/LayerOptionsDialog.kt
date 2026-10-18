package com.flowtask.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LabelOff
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.SurfaceContainerLow
import com.flowtask.app.ui.theme.SurfaceContainerLowest
import com.flowtask.app.ui.theme.TextMuted
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.util.LocalAppStrings

/**
 * Material 3 Expressive Bottom Card Sheet for Editing and Deleting Layers.
 *
 * Rendered as an elegant squircle bottom sheet card rather than a floating middle-of-screen popup,
 * seamlessly shifting above the software keyboard with tactile haptics, white high-contrast button
 * labels, and responsive actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayerOptionsDialog(
    layerName: String,
    taskCount: Int = 0,
    onDismiss: () -> Unit,
    onRename: (oldName: String, newName: String) -> Unit,
    onDelete: (layerName: String, deleteTasks: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val haptic = LocalHapticFeedback.current

    var layerNameInput by remember(layerName) { mutableStateOf(layerName) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val isNameChanged = layerNameInput.trim().isNotBlank() &&
            !layerNameInput.trim().equals(layerName.trim(), ignoreCase = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Icon badge, Title, Subtitle, and Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(currentAccent.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = currentAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = strings.editLayerTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = strings.deleteLayerSubtitle(taskCount),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = TextSecondary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerLow)
                        .expressiveBounce(pressScale = 0.92f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = strings.cancelAction,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Rename Layer Section
            Text(
                text = strings.layerNameLabel,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = layerNameInput,
                onValueChange = { layerNameInput = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceContainerLow,
                    unfocusedContainerColor = SurfaceContainerLow,
                    focusedBorderColor = currentAccent,
                    unfocusedBorderColor = OutlineSubtle.copy(alpha = 0.7f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                trailingIcon = {
                    if (layerNameInput.isNotEmpty()) {
                        IconButton(onClick = { layerNameInput = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (isNameChanged) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onRename(layerName, layerNameInput.trim())
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Save Name Button
            Button(
                onClick = {
                    if (isNameChanged) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRename(layerName, layerNameInput.trim())
                    }
                },
                enabled = isNameChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .expressiveBounce(pressScale = 0.94f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = currentAccent,
                    contentColor = Color.White,
                    disabledContainerColor = SurfaceContainerLow,
                    disabledContentColor = TextMuted
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (isNameChanged) Color.White else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.renameLayerAction,
                    color = if (isNameChanged) Color.White else TextMuted,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(
                color = OutlineSubtle.copy(alpha = 0.5f),
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Delete Layer Section
            if (!showDeleteConfirmation) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDeleteConfirmation = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .expressiveBounce(pressScale = 0.94f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.deleteLayerAction,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    )
                }
            } else {
                // Expandable Confirmation Panel
                AnimatedVisibility(
                    visible = showDeleteConfirmation,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = strings.deleteLayerConfirmTitle(layerName),
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )

                            if (taskCount > 0) {
                                // Choice A: Remove layer tag from tasks (preserves task items)
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onDelete(layerName, false)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .expressiveBounce(pressScale = 0.94f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SurfaceContainerLowest,
                                        contentColor = TextPrimary
                                    ),
                                    border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f))
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.LabelOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.deleteLayerKeepTasks,
                                        color = TextPrimary,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }

                                // Choice B: Delete tasks along with the layer (destructive)
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onDelete(layerName, true)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .expressiveBounce(pressScale = 0.94f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.deleteLayerDeleteTasks,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            } else {
                                // 0 tasks: single confirm button
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onDelete(layerName, false)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .expressiveBounce(pressScale = 0.94f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.deleteLayerAction,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            // Cancel button
                            TextButton(
                                onClick = { showDeleteConfirmation = false },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    text = strings.cancelAction,
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
