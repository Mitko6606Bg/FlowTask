package com.flowtask.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.flowtask.app.ui.theme.AccentPrimary
import com.flowtask.app.ui.theme.BgLight
import com.flowtask.app.ui.theme.ExpressiveMotion
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.LocalDarkTheme
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.SurfaceContainerLow
import com.flowtask.app.ui.theme.SurfaceLight
import com.flowtask.app.ui.theme.TextOnAccent
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.util.LocalAppStrings

data class NavTarget(
    val pageIndex: Int,
    val title: String,
    val icon: ImageVector
)

@Composable
fun FloatingSlideNavigator(
    currentPagerIndex: Int,
    screenOrder: List<Int>,
    onSelectPageIndex: (Int) -> Unit,
    onCreateClick: () -> Unit,
    onSnapPhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current

    val currentScreenId = screenOrder.getOrElse(currentPagerIndex) { 1 }

    fun getTarget(screenId: Int): NavTarget {
        return when (screenId) {
            0 -> NavTarget(0, strings.navProjects, Icons.Default.Folder)
            1 -> NavTarget(1, strings.navToday, Icons.Default.Schedule)
            2 -> NavTarget(2, strings.navAllTasks, Icons.Default.CalendarMonth)
            3 -> NavTarget(3, strings.navInstants, Icons.Default.PhotoLibrary)
            else -> NavTarget(2, strings.navAllTasks, Icons.Default.CalendarMonth)
        }
    }

    val currentTarget = getTarget(currentScreenId)
    val otherPagerIndices = (0 until screenOrder.size).filter { it != currentPagerIndex }

    var isExpanded by remember { mutableStateOf(false) }
    var totalDragX by remember { mutableFloatStateOf(0f) }

    val createButtonTitle = when (currentScreenId) {
        0 -> strings.newProjectTitle
        3 -> strings.newPhotoFolderTitle
        else -> strings.newTaskTitle
    }

    val iconRotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "mainIconRotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        // Transparent Backdrop
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isExpanded = false }
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // TOP WINGS: Stacked options above main hub
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }) + scaleIn(initialScale = 0.85f),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }) + scaleOut(targetScale = 0.85f)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Option 2 (Top): Quick Snap Photo
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .expressiveBounce(pressScale = 0.95f)
                            .clickable {
                                isExpanded = false
                                onSnapPhotoClick()
                            },
                        shape = RoundedCornerShape(22.dp),
                        color = SurfaceLight,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, currentAccent.copy(alpha = 0.45f)),
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = strings.snapPhotoAction,
                                tint = currentAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.snapPhotoAction,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            )
                        }
                    }

                    // Option 1 (Middle): Contextual Create (Task / Project / Folder)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .expressiveBounce(pressScale = 0.95f)
                            .clickable {
                                isExpanded = false
                                onCreateClick()
                            },
                        shape = RoundedCornerShape(22.dp),
                        color = currentAccent,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, currentAccent.copy(alpha = 0.3f)),
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = createButtonTitle,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = createButtonTitle,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // BOTTOM ROW: Left Wing (Other screen targets) + Main Circle Hub
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // LEFT WING: The other Navigation Target Buttons
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + slideInHorizontally(initialOffsetX = { it / 2 }) + scaleIn(initialScale = 0.85f),
                    exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it / 2 }) + scaleOut(targetScale = 0.85f)
                ) {
                    Surface(
                        modifier = Modifier.padding(end = 10.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = SurfaceLight,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, OutlineSubtle.copy(alpha = 0.7f)),
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            otherPagerIndices.forEach { pagerIndex ->
                                val screenId = screenOrder[pagerIndex]
                                val target = getTarget(screenId)

                                Surface(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .expressiveBounce(pressScale = 0.92f)
                                        .clickable {
                                            isExpanded = false
                                            onSelectPageIndex(pagerIndex)
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    color = SurfaceContainerLow,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.5f))
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = target.icon,
                                            contentDescription = target.title,
                                            tint = currentAccent,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // MAIN CIRCLE HUB (Click-to-Expand & Direct Swipe-to-Switch)
                Surface(
                    modifier = Modifier
                        .size(58.dp)
                        .expressiveBounce(pressScale = 0.93f)
                        .pointerInput(currentPagerIndex) {
                            detectHorizontalDragGestures(
                                onDragStart = { totalDragX = 0f },
                                onDragEnd = {
                                    val swipeThreshold = 35f
                                    if (totalDragX < -swipeThreshold) {
                                        if (currentPagerIndex < screenOrder.size - 1) onSelectPageIndex(currentPagerIndex + 1)
                                    } else if (totalDragX > swipeThreshold) {
                                        if (currentPagerIndex > 0) onSelectPageIndex(currentPagerIndex - 1)
                                    }
                                    totalDragX = 0f
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    totalDragX += dragAmount
                                }
                            )
                        }
                        .clickable { isExpanded = !isExpanded },
                    shape = CircleShape,
                    color = if (LocalDarkTheme.current) currentAccent else AccentPrimary,
                    contentColor = TextOnAccent,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, TextOnAccent.copy(alpha = 0.25f)),
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.Close else currentTarget.icon,
                            contentDescription = strings.navTitle,
                            tint = TextOnAccent,
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(iconRotation)
                        )
                    }
                }
            }
        }
    }
}
