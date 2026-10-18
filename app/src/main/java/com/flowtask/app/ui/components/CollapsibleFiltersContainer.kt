package com.flowtask.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowtask.app.data.local.model.LayerProgram
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.LocalDockColors
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.OutlineVariant
import com.flowtask.app.ui.theme.SurfaceContainerLight
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.util.LocalAppStrings
import kotlin.math.abs

@Composable
fun CollapsibleFiltersContainer(
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSetExpanded: (Boolean) -> Unit,
    selectedLayer: String?,
    availableLayers: List<String>,
    onSelectLayer: (String?) -> Unit,
    layerPrograms: List<LayerProgram>,
    selectedProgramId: String?,
    onSelectProgram: (String?) -> Unit,
    lastSelectedProgramId: String?,
    activeCalendarPrograms: List<LayerProgram>,
    isCalendarMode: Boolean,
    modifier: Modifier = Modifier,
    onLongClickLayer: ((String) -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val dockColors = LocalDockColors.current
    val haptic = LocalHapticFeedback.current
    val headerInteractionSource = remember { MutableInteractionSource() }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
        label = "filtersChevronRotation"
    )

    val previewScrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = SurfaceContainerLight,
        border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f)),
        shadowElevation = if (isExpanded) 0.dp else 0.5.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header Bar with Gesture and Tap detection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = if (isExpanded) 50.dp else 60.dp)
                    .expressiveBounce(interactionSource = headerInteractionSource, pressScale = 0.98f)
                    .pointerInput(isExpanded) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val press = PressInteraction.Press(down.position)
                            headerInteractionSource.tryEmit(press)
                            var isDrag = false
                            val slop = viewConfiguration.touchSlop * 0.75f

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) {
                                    headerInteractionSource.tryEmit(PressInteraction.Release(press))
                                    if (!isDrag) {
                                        onToggleExpanded()
                                    }
                                    break
                                }
                                val dx = change.position.x - down.position.x
                                val dy = change.position.y - down.position.y
                                if (abs(dy) > slop && abs(dy) >= abs(dx)) {
                                    isDrag = true
                                    if (!isExpanded && dy > slop) {
                                        change.consume()
                                        headerInteractionSource.tryEmit(PressInteraction.Release(press))
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onSetExpanded(true)
                                        // Drain and consume all subsequent pointer events until finger is lifted
                                        while (true) {
                                            val nextEvent = awaitPointerEvent()
                                            val nextChange = nextEvent.changes.firstOrNull { it.id == down.id } ?: break
                                            nextChange.consume()
                                            if (!nextChange.pressed) break
                                        }
                                        break
                                    } else if (isExpanded && dy < -slop) {
                                        change.consume()
                                        headerInteractionSource.tryEmit(PressInteraction.Release(press))
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onSetExpanded(false)
                                        // Drain and consume all subsequent pointer events until finger is lifted
                                        while (true) {
                                            val nextEvent = awaitPointerEvent()
                                            val nextChange = nextEvent.changes.firstOrNull { it.id == down.id } ?: break
                                            nextChange.consume()
                                            if (!nextChange.pressed) break
                                        }
                                        break
                                    }
                                }
                            }
                        }
                    }
                    .padding(
                        horizontal = 16.dp,
                        vertical = if (isExpanded) 12.dp else 16.dp
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Anchor: Tune Icon (fixed 28dp width for symmetric balance)
                    Box(
                        modifier = Modifier.size(28.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = if (selectedLayer != null || (isCalendarMode && activeCalendarPrograms.isNotEmpty())) {
                                dockColors.barBackground
                            } else {
                                TextSecondary
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Center: Horizontally centered Title and Preview Badges
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.horizontalScroll(previewScrollState),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.filtersAndProgramsTitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                ),
                                maxLines = 1,
                                softWrap = false
                            )

                            if (!isExpanded) {
                                if (selectedLayer != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = dockColors.barBackground.copy(alpha = 0.12f),
                                        border = BorderStroke(0.8.dp, dockColors.barBackground.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = selectedLayer,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = dockColors.barBackground,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Clip,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (isCalendarMode && activeCalendarPrograms.isNotEmpty()) {
                                    val activeProg = activeCalendarPrograms.first()
                                    val progColor = remember(activeProg.colorHex, currentAccent) {
                                        activeProg.colorHex?.let {
                                            try {
                                                Color(android.graphics.Color.parseColor(it))
                                            } catch (_: Exception) {
                                                currentAccent
                                            }
                                        } ?: currentAccent
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = progColor.copy(alpha = 0.12f),
                                        border = BorderStroke(0.8.dp, progColor.copy(alpha = 0.35f))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(progColor)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = activeProg.layerName,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = progColor,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 11.sp
                                                ),
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Clip
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Right Anchor: Animated rotating chevron arrow (fixed 28dp width for symmetric balance)
                    Box(
                        modifier = Modifier.size(28.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = TextSecondary,
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer { rotationZ = chevronRotation }
                        )
                    }
                }
            }

            // Expanded Pills Section inside the SAME container card
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
                ) + fadeIn(animationSpec = spring(stiffness = 500f)),
                exit = shrinkVertically(
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
                ) + fadeOut(animationSpec = spring(stiffness = 500f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HorizontalDivider(
                        color = OutlineSubtle.copy(alpha = 0.5f),
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Task Layers Filter Bar
                    LayerFilterBar(
                        availableLayers = availableLayers,
                        selectedLayer = selectedLayer,
                        onSelectLayer = onSelectLayer,
                        onLongClickLayer = onLongClickLayer,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Programs Filter Bar with M3 switch (on separate line in Calendar mode)
                    if (isCalendarMode && layerPrograms.isNotEmpty()) {
                        CalendarProgramFilterBar(
                            layerPrograms = layerPrograms,
                            selectedProgramId = selectedProgramId,
                            onSelectProgram = onSelectProgram,
                            lastSelectedProgramId = lastSelectedProgramId,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Tactile Grabber Handle at the bottom of the container
                    FilterContainerCloseHandle(
                        onClose = { onSetExpanded(false) }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterContainerCloseHandle(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val handleInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .expressiveBounce(interactionSource = handleInteractionSource, pressScale = 0.90f)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val press = PressInteraction.Press(down.position)
                    handleInteractionSource.tryEmit(press)
                    var isDrag = false
                    val slop = viewConfiguration.touchSlop * 0.70f

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        // Always consume pointer changes on the handle to guarantee HorizontalPager never intercepts!
                        change.consume()

                        if (!change.pressed) {
                            handleInteractionSource.tryEmit(PressInteraction.Release(press))
                            if (!isDrag) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onClose()
                            }
                            break
                        }
                        val dy = change.position.y - down.position.y
                        if (dy < -slop) {
                            isDrag = true
                            handleInteractionSource.tryEmit(PressInteraction.Release(press))
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onClose()
                            // Drain and consume ALL remaining pointer events in this gesture until finger is lifted
                            while (true) {
                                val nextEvent = awaitPointerEvent()
                                val nextChange = nextEvent.changes.firstOrNull { it.id == down.id } ?: break
                                nextChange.consume()
                                if (!nextChange.pressed) break
                            }
                            break
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(38.dp)
                .height(4.5.dp)
                .clip(CircleShape)
                .background(OutlineVariant.copy(alpha = 0.85f))
        )
    }
}
