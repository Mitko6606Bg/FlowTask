package com.flowtask.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.SurfaceContainerLow
import com.flowtask.app.ui.theme.SurfaceContainerLowest
import com.flowtask.app.ui.theme.SurfaceLight
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.util.LocalAppStrings
import kotlinx.coroutines.delay

/**
 * TikTok photo carousel-inspired dot layer selector.
 *
 * Features:
 * - Sleek, compact visual styling matching Material 3 Expressive aesthetics.
 * - Generous 54dp touch target hit box ensuring effortless swiping without missing.
 * - Stable pointerInput coroutine lifecycle (not keyed on selectedIndex), fully consuming
 *   all horizontal drag events so parent HorizontalPagers NEVER intercept or switch screens.
 * - Crisp mechanical haptic feedback ticks on each layer step.
 * - Compact floating tooltip badge displaying the active layer name.
 */
@Composable
fun TikTokDotLayerSelector(
    availableLayers: List<String>,
    selectedLayer: String?,
    onSelectLayer: (String?) -> Unit,
    modifier: Modifier = Modifier,
    isFloating: Boolean = false,
    embeddedInDock: Boolean = false,
    compactMode: Boolean = false,
    showTooltip: Boolean = true,
    customBackgroundColor: Color? = null,
    customBorderColor: Color? = null,
    customDotColor: Color? = null,
    onScrubbingChanged: ((isScrubbing: Boolean, layerTitle: String) -> Unit)? = null,
    onLongClickLayer: ((String) -> Unit)? = null
) {
    if (availableLayers.isEmpty()) return

    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val density = LocalDensity.current

    // Options: index 0 is null ("All"), subsequent indices are availableLayers
    val layerOptions = remember(availableLayers) {
        listOf<String?>(null) + availableLayers
    }

    val selectedIndex = remember(selectedLayer, layerOptions) {
        if (selectedLayer == null) 0
        else {
            val idx = availableLayers.indexOfFirst { it.equals(selectedLayer, ignoreCase = true) }
            if (idx >= 0) idx + 1 else 0
        }
    }

    // Stable references that do NOT restart pointerInput
    val currentSelectedIndex by rememberUpdatedState(selectedIndex)
    val currentOnSelectLayer by rememberUpdatedState(onSelectLayer)
    val currentOnLongClickLayer by rememberUpdatedState(onLongClickLayer)

    var isScrubbing by remember { mutableStateOf(false) }
    var showTooltipTemporary by remember { mutableStateOf(false) }
    var isFirstComposition by remember { mutableStateOf(true) }

    // Auto-hide tooltip after 1.5 seconds if not actively scrubbing
    LaunchedEffect(selectedLayer) {
        if (isFirstComposition) {
            isFirstComposition = false
            return@LaunchedEffect
        }
        showTooltipTemporary = true
        delay(1500L)
        showTooltipTemporary = false
    }

    fun triggerHapticTick() {
        try {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        } catch (_: Exception) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val currentLayerTitle = remember(selectedIndex, layerOptions, strings) {
        val layer = layerOptions.getOrNull(selectedIndex)
        layer ?: strings.allLayers
    }

    LaunchedEffect(isScrubbing, showTooltipTemporary, currentLayerTitle) {
        onScrubbingChanged?.invoke(isScrubbing || showTooltipTemporary, currentLayerTitle)
    }

    Column(
        modifier = modifier.wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Floating Micro-Tooltip Badge (Appears when scrubbing or switching layers)
        if (showTooltip) {
            AnimatedVisibility(
                visible = isScrubbing || showTooltipTemporary,
            enter = fadeIn() + scaleIn(
                initialScale = 0.85f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
            exit = fadeOut() + scaleOut(targetScale = 0.85f)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SurfaceLight,
                border = BorderStroke(1.dp, currentAccent.copy(alpha = 0.35f)),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .clickable(enabled = selectedLayer != null && onLongClickLayer != null) {
                        selectedLayer?.let {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentOnLongClickLayer?.invoke(it)
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = currentAccent,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currentLayerTitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                    )
                }
            }
        }
    }

        // Generous touch target hit box surrounding the sleek visual pill
        Box(
            modifier = Modifier
                .wrapContentSize()
                .height(if (embeddedInDock) 46.dp else if (customBackgroundColor != null) 44.dp else 54.dp)
                .pointerInput(layerOptions) {
                    val stepPx = with(density) { 16.dp.toPx() }
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        isScrubbing = true

                        var activeIdx = currentSelectedIndex
                        var accumulatedDragX = 0f
                        var isDrag = false
                        val downTime = System.currentTimeMillis()
                        var isLongPressTriggered = false

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break

                            if (!change.pressed) {
                                change.consume()
                                // If it was a tap without significant drag or long press:
                                if (!isDrag && !isLongPressTriggered) {
                                    val trackWidth = size.width.toFloat().coerceAtLeast(1f)
                                    val fraction = (down.position.x / trackWidth).coerceIn(0f, 0.999f)
                                    val tappedIdx = (fraction * layerOptions.size).toInt().coerceIn(0, layerOptions.size - 1)
                                    if (tappedIdx != currentSelectedIndex) {
                                        triggerHapticTick()
                                        currentOnSelectLayer(layerOptions[tappedIdx])
                                    }
                                }
                                isScrubbing = false
                                break
                            }

                            // CONSUME EVERY MOVE EVENT SO HORIZONTAL PAGER NEVER SEES IT!
                            change.consume()

                            val dx = change.position.x - change.previousPosition.x
                            val totalMovement = kotlin.math.abs(change.position.x - down.position.x)

                            if (totalMovement > with(density) { 5.dp.toPx() }) {
                                isDrag = true
                            }

                            // Detect long press on a specific layer dot if held still
                            if (!isDrag && !isLongPressTriggered && (System.currentTimeMillis() - downTime >= 400L)) {
                                val trackWidth = size.width.toFloat().coerceAtLeast(1f)
                                val fraction = (down.position.x / trackWidth).coerceIn(0f, 0.999f)
                                val heldIdx = (fraction * layerOptions.size).toInt().coerceIn(0, layerOptions.size - 1)
                                val heldLayer = layerOptions.getOrNull(heldIdx)
                                if (heldLayer != null && currentOnLongClickLayer != null) {
                                    isLongPressTriggered = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    currentOnLongClickLayer?.invoke(heldLayer)
                                    isScrubbing = false
                                    break
                                }
                            }

                            if (isDrag) {
                                accumulatedDragX += dx
                                if (accumulatedDragX > stepPx) {
                                    if (activeIdx < layerOptions.size - 1) {
                                        activeIdx++
                                        triggerHapticTick()
                                        currentOnSelectLayer(layerOptions[activeIdx])
                                    }
                                    accumulatedDragX = 0f
                                } else if (accumulatedDragX < -stepPx) {
                                    if (activeIdx > 0) {
                                        activeIdx--
                                        triggerHapticTick()
                                        currentOnSelectLayer(layerOptions[activeIdx])
                                    }
                                    accumulatedDragX = 0f
                                }
                            }
                        }
                    }
                },
            contentAlignment = if (customBackgroundColor != null) Alignment.BottomCenter else Alignment.Center
        ) {
            // Sleek, compact visual pill track
            val trackShape = CircleShape
            val trackBg = when {
                customBackgroundColor != null -> customBackgroundColor
                embeddedInDock -> SurfaceContainerLow
                isFloating -> SurfaceContainerLowest
                else -> SurfaceLight
            }
            val trackBorder = when {
                customBorderColor != null -> if (customBorderColor != Color.Transparent) BorderStroke(1.dp, customBorderColor) else null
                else -> BorderStroke(1.dp, OutlineSubtle.copy(alpha = if (embeddedInDock) 0.35f else 0.55f))
            }
            val trackElevation = 0.dp

            Surface(
                shape = trackShape,
                color = trackBg,
                border = trackBorder,
                shadowElevation = trackElevation,
                modifier = Modifier
                    .wrapContentSize()
                    .expressiveBounce(pressScale = 0.98f)
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = if (compactMode) 8.dp else 12.dp,
                        vertical = if (compactMode) 4.dp else 6.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(if (compactMode) 4.dp else 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    layerOptions.forEachIndexed { index, _ ->
                        val isSelected = index == selectedIndex
                        val distance = kotlin.math.abs(index - selectedIndex)

                        // Sleek, compact dot sizes
                        val targetWidth = when {
                            customDotColor != null -> if (isSelected) 18.dp else 6.dp
                            isSelected -> if (compactMode) 16.dp else 22.dp
                            layerOptions.size <= 4 -> if (compactMode) 5.dp else 6.dp
                            distance == 1 -> if (compactMode) 5.dp else 6.dp
                            distance == 2 -> if (compactMode) 4.dp else 5.dp
                            else -> if (compactMode) 3.5.dp else 4.dp
                        }

                        val targetHeight = when {
                            customDotColor != null -> 6.dp
                            isSelected -> if (compactMode) 5.dp else 6.dp
                            layerOptions.size <= 4 -> if (compactMode) 5.dp else 6.dp
                            distance == 1 -> if (compactMode) 5.dp else 6.dp
                            distance == 2 -> if (compactMode) 4.dp else 5.dp
                            else -> if (compactMode) 3.5.dp else 4.dp
                        }

                        val targetColor = when {
                            customDotColor != null -> if (isSelected) customDotColor else customDotColor.copy(alpha = 0.40f)
                            isSelected -> currentAccent
                            distance == 1 -> TextSecondary.copy(alpha = 0.45f)
                            distance == 2 -> TextSecondary.copy(alpha = 0.32f)
                            else -> TextSecondary.copy(alpha = 0.22f)
                        }

                        val animatedWidth by animateDpAsState(
                            targetValue = targetWidth,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "dotWidth_$index"
                        )

                        val animatedHeight by animateDpAsState(
                            targetValue = targetHeight,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "dotHeight_$index"
                        )

                        val animatedColor by animateColorAsState(
                            targetValue = targetColor,
                            animationSpec = spring(stiffness = 500f),
                            label = "dotColor_$index"
                        )

                        Box(
                            modifier = Modifier
                                .size(width = animatedWidth, height = animatedHeight)
                                .clip(CircleShape)
                                .background(animatedColor)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Floating bar wrapper that houses the TikTokDotLayerSelector at the bottom of screens
 * (e.g. AllTasksCalendarScreen in both List and Calendar views), centered and elevated
 * for clear visibility and effortless interaction.
 */
@Composable
fun FloatingDotLayerBar(
    availableLayers: List<String>,
    selectedLayer: String?,
    onSelectLayer: (String?) -> Unit,
    modifier: Modifier = Modifier,
    customBackgroundColor: Color? = null,
    customBorderColor: Color? = null,
    customDotColor: Color? = null
) {
    if (availableLayers.isEmpty()) return

    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        TikTokDotLayerSelector(
            availableLayers = availableLayers,
            selectedLayer = selectedLayer,
            onSelectLayer = onSelectLayer,
            isFloating = true,
            customBackgroundColor = customBackgroundColor,
            customBorderColor = customBorderColor,
            customDotColor = customDotColor
        )
    }
}
