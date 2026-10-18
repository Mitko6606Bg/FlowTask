package com.flowtask.app.ui.components

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import kotlinx.coroutines.launch
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.LocalDarkTheme
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.SurfaceContainerLow
import com.flowtask.app.ui.theme.SurfaceLight
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.util.LocalAppStrings

// Re-export / delegate to theme package for full backward compatibility with callers and tests
typealias DockColorPalette = com.flowtask.app.ui.theme.DockColorPalette
inline fun rgbToHsl(color: Color): FloatArray = com.flowtask.app.ui.theme.rgbToHsl(color)
inline fun hslToRgb(h: Float, s: Float, l: Float, alpha: Float = 1f): Color = com.flowtask.app.ui.theme.hslToRgb(h, s, l, alpha)
inline fun calculateDockColors(accent: Color, isDark: Boolean): DockColorPalette = com.flowtask.app.ui.theme.calculateDockColors(accent, isDark)

/**
 * Material 3 Expressive Compact Pill Floating Dock Nav Bar.
 *
 * Styled after modern capsule dock ergonomics:
 * - Compact pill/capsule shape centered horizontally at the bottom.
 * - Left slot: Task Checkbox navigation button invoking [onNavigateToToday].
 * - Center slot: Prominent nested capsule button with '+' invoking [onCreateClick].
 * - Right slot: Refurbished menu/action hub button invoking expandable options palette
 *   and swipe-to-switch page gestures.
 * - Dot Layer Selector: Positioned directly ABOVE the dock, completely decoupled from
 *   the dock interior.
 */
@Composable
fun FloatingDockNavBar(
    currentPagerIndex: Int,
    screenOrder: List<Int>,
    onSelectPageIndex: (Int) -> Unit,
    onCreateClick: () -> Unit,
    onSnapPhotoClick: () -> Unit,
    onNavigateToToday: () -> Unit,
    availableLayers: List<String>,
    selectedLayer: String?,
    onSelectLayer: (String?) -> Unit,
    modifier: Modifier = Modifier,
    onLongClickLayer: ((String) -> Unit)? = null,
    isSearchActive: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onCloseSearch: () -> Unit = {}
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val isDark = LocalDarkTheme.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val animatedDragOffset by animateFloatAsState(
        targetValue = dragOffsetY,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = 500f
        ),
        label = "dockSwipeOffsetY"
    )
    val dismissThresholdPx = remember(density) { with(density) { 36.dp.toPx() } }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        } else {
            focusManager.clearFocus()
            dragOffsetY = 0f
        }
    }

    val searchSwipeModifier = if (isSearchActive) {
        Modifier.pointerInput(isSearchActive) {
            detectVerticalDragGestures(
                onDragStart = { },
                onDragEnd = {
                    if (dragOffsetY > dismissThresholdPx) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        focusManager.clearFocus()
                        onCloseSearch()
                    }
                    dragOffsetY = 0f
                },
                onDragCancel = { dragOffsetY = 0f },
                onVerticalDrag = { change, dragAmount ->
                    if (dragAmount > 0f || dragOffsetY > 0f) {
                        change.consume()
                        dragOffsetY = (dragOffsetY + dragAmount).coerceAtLeast(0f)
                    }
                }
            )
        }
    } else {
        Modifier
    }

    val currentScreenId = screenOrder.getOrElse(currentPagerIndex) { 2 }

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

    val dockColors = remember(currentAccent, isDark) {
        calculateDockColors(currentAccent, isDark)
    }

    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }

    PredictiveBackHandler(enabled = isExpanded || isSearchActive) { progress: Flow<BackEventCompat> ->
        try {
            progress.collect { backEvent ->
                predictiveBackProgress = backEvent.progress
            }
            if (isSearchActive) {
                focusManager.clearFocus()
                onCloseSearch()
            } else {
                isExpanded = false
            }
        } catch (_: CancellationException) {
        } finally {
            predictiveBackProgress = 0f
        }
    }

    val iconRotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "dockHubRotation"
    )

    Box(
        modifier = if (isExpanded) Modifier.fillMaxSize() else modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Transparent Backdrop to dismiss expanded state when tapping outside
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
            modifier = if (isExpanded) modifier.fillMaxWidth() else Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Companion slot directly above the dock (houses either expanded navigation container or dot layer selector)
            Box(
                contentAlignment = Alignment.BottomCenter
            ) {
                // 1. TOP EXPANDED COMPANION SCREEN CONTAINER
                androidx.compose.animation.AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(animationSpec = spring(stiffness = 500f)) +
                            slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f)
                            ) +
                            scaleIn(
                                initialScale = 0.85f,
                                animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f)
                            ),
                    exit = fadeOut(animationSpec = spring(stiffness = 600f)) +
                           slideOutVertically(
                               targetOffsetY = { it },
                               animationSpec = spring(dampingRatio = 0.8f, stiffness = 550f)
                           ) +
                           scaleOut(
                               targetScale = 0.85f,
                               animationSpec = spring(dampingRatio = 0.8f, stiffness = 550f)
                           )
                ) {
                    Surface(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(56.dp)
                            .graphicsLayer {
                                if (predictiveBackProgress > 0f) {
                                    val scale = 1f - (predictiveBackProgress * 0.12f)
                                    scaleX = scale
                                    scaleY = scale
                                    translationY = predictiveBackProgress * 20.dp.toPx()
                                    alpha = 1f - (predictiveBackProgress * 0.35f)
                                }
                            },
                        shape = CircleShape,
                        color = dockColors.barBackground,
                        contentColor = dockColors.iconTint,
                        border = BorderStroke(
                            1.dp,
                            if (dockColors.border != Color.Transparent) dockColors.border else if (isDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f)
                        ),
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            otherPagerIndices.forEach { pagerIndex ->
                                val screenId = screenOrder.getOrElse(pagerIndex) { pagerIndex }
                                val target = getTarget(screenId)
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .expressiveBounce(pressScale = 0.90f)
                                        .clickable {
                                            isExpanded = false
                                            onSelectPageIndex(pagerIndex)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = target.icon,
                                        contentDescription = target.title,
                                        tint = dockColors.iconTint,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Quick Snap Camera Button
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .expressiveBounce(pressScale = 0.90f)
                                    .clickable {
                                        isExpanded = false
                                        onSnapPhotoClick()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = strings.snapPhotoAction,
                                    tint = dockColors.iconTint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // 2. TIKTOK DOT LAYER SELECTOR (Floating snugly 6dp above the dock when idle)
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isExpanded && !isSearchActive && availableLayers.isNotEmpty(),
                    enter = fadeIn(animationSpec = spring(stiffness = 500f)) +
                            scaleIn(initialScale = 0.9f, animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f)),
                    exit = fadeOut(animationSpec = spring(stiffness = 600f)) +
                           scaleOut(targetScale = 0.9f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 550f))
                ) {
                    TikTokDotLayerSelector(
                        availableLayers = availableLayers,
                        selectedLayer = selectedLayer,
                        onSelectLayer = onSelectLayer,
                        isFloating = true,
                        embeddedInDock = false,
                        compactMode = false,
                        showTooltip = true,
                        customBackgroundColor = dockColors.barBackground,
                        customBorderColor = dockColors.border,
                        customDotColor = Color.White,
                        onLongClickLayer = onLongClickLayer
                    )
                }

                // 3. SEARCH DRAG HANDLE (Material 3 Expressive pill floating in the companion slot above the search bar)
                androidx.compose.animation.AnimatedVisibility(
                    visible = isSearchActive,
                    enter = fadeIn(animationSpec = spring(stiffness = 500f)) +
                            scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f)),
                    exit = fadeOut(animationSpec = spring(stiffness = 600f)) +
                           scaleOut(targetScale = 0.85f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 550f))
                ) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                if (animatedDragOffset > 0f) {
                                    translationY = animatedDragOffset
                                }
                            }
                            .size(width = 64.dp, height = 24.dp)
                            .clip(CircleShape)
                            .expressiveBounce(pressScale = 0.90f)
                            .then(searchSwipeModifier)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                focusManager.clearFocus()
                                onCloseSearch()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 38.dp, height = 4.5.dp)
                                .clip(CircleShape)
                                .background(
                                    dockColors.iconTint.copy(
                                        alpha = if (isDark) 0.45f else 0.30f
                                    )
                                )
                        )
                    }
                }
            }

            // 4. MAIN COMPACT FLOATING DOCK CAPSULE (Transforms to Search Bar in Search Mode)
            val isProjectsScreen = currentScreenId == 0

            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val targetSearchWidth = (maxWidth - 24.dp).coerceAtMost(560.dp)
                val dockWidth by animateDpAsState(
                    targetValue = if (isSearchActive) targetSearchWidth else 186.dp,
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = 380f
                    ),
                    label = "dockWidth"
                )
                val dockHeight by animateDpAsState(
                    targetValue = if (isSearchActive) 66.dp else 56.dp,
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = 380f
                    ),
                    label = "dockHeight"
                )

                Surface(
                    modifier = Modifier
                        .width(dockWidth)
                        .height(dockHeight)
                        .graphicsLayer {
                            if (animatedDragOffset > 0f || predictiveBackProgress > 0f) {
                                val backScale = if (predictiveBackProgress > 0f) 1f - (predictiveBackProgress * 0.08f) else 1f
                                val backTranslationY = if (predictiveBackProgress > 0f) predictiveBackProgress * 24.dp.toPx() else 0f
                                val backAlpha = if (predictiveBackProgress > 0f) 1f - (predictiveBackProgress * 0.25f) else 1f

                                translationY = animatedDragOffset + backTranslationY
                                scaleX = backScale
                                scaleY = backScale
                                alpha = ((1f - (animatedDragOffset / 300f)) * backAlpha).coerceIn(0.35f, 1f)
                            }
                        }
                        .then(searchSwipeModifier)
                        .zIndex(1f),
                    shape = CircleShape,
                    color = dockColors.barBackground,
                    contentColor = dockColors.iconTint,
                    border = BorderStroke(
                        1.dp,
                        if (dockColors.border != Color.Transparent) dockColors.border else if (isDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f)
                    ),
                    shadowElevation = 0.dp
                ) {
                    AnimatedContent(
                        targetState = isSearchActive,
                        contentAlignment = Alignment.Center,
                        transitionSpec = {
                            (fadeIn(animationSpec = spring(stiffness = 500f)) + scaleIn(initialScale = 0.95f))
                                .togetherWith(fadeOut(animationSpec = spring(stiffness = 500f)) + scaleOut(targetScale = 0.95f))
                        },
                        label = "dockSearchContent"
                    ) { active ->
                        if (active) {
                            // Google Search Homescreen Widget Style (Dual-Capsule layout matching reference photo)
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Inner elongated search capsule
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(dockColors.centerButtonBackground)
                                        .border(
                                            1.dp,
                                            if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f),
                                            CircleShape
                                        )
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            focusRequester.requestFocus()
                                        }
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Search Emblem on the far left (Google "G" equivalent in accent color)
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(currentAccent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // Search query text input
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = strings.searchProjectsPlaceholder,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    color = dockColors.iconTint.copy(alpha = 0.65f),
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Normal
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        BasicTextField(
                                            value = searchQuery,
                                            onValueChange = onSearchQueryChange,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(focusRequester),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            cursorBrush = SolidColor(Color.White),
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                                        )
                                    }

                                    // Trailing micro action inside the search pill (Clear button)
                                    if (searchQuery.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .expressiveBounce(pressScale = 0.88f)
                                                .clickable { onSearchQueryChange("") },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear",
                                                tint = dockColors.iconTint,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // 2. Standalone circular Close/Collapse button (matching the circular button in reference photo)
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(dockColors.centerButtonBackground)
                                        .border(
                                            1.dp,
                                            if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f),
                                            CircleShape
                                        )
                                        .expressiveBounce(pressScale = 0.88f)
                                        .clickable {
                                            focusManager.clearFocus()
                                            onCloseSearch()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Close search",
                                        tint = dockColors.iconTint,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        } else {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // LEFT BUTTON: Search on Projects screen, Camera on Today screen, Today icon on other screens
                            val isTodayScreen = currentScreenId == 1
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .expressiveBounce(pressScale = 0.90f)
                                    .clickable {
                                        if (isExpanded) isExpanded = false
                                        if (isProjectsScreen) {
                                            onOpenSearch()
                                        } else if (isTodayScreen) {
                                            onSnapPhotoClick()
                                        } else {
                                            onNavigateToToday()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Crossfade(
                                    targetState = when {
                                        isProjectsScreen -> 0
                                        isTodayScreen -> 1
                                        else -> 2
                                    },
                                    animationSpec = spring(stiffness = 500f),
                                    label = "leftDockIcon"
                                ) { mode ->
                                    when (mode) {
                                        0 -> Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = strings.searchProjectsPlaceholder,
                                            tint = dockColors.iconTint,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        1 -> Icon(
                                            imageVector = Icons.Default.PhotoCamera,
                                            contentDescription = strings.snapPhotoAction,
                                            tint = dockColors.iconTint,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        else -> Icon(
                                            imageVector = Icons.Default.Today,
                                            contentDescription = strings.navToday,
                                            tint = dockColors.iconTint,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            // CENTER BUTTON: Prominent nested capsule (+)
                            Surface(
                                modifier = Modifier
                                    .size(width = 64.dp, height = 44.dp)
                                    .clip(CircleShape)
                                    .expressiveBounce(pressScale = 0.90f)
                                    .clickable {
                                        if (isExpanded) isExpanded = false
                                        onCreateClick()
                                    },
                                shape = CircleShape,
                                color = dockColors.centerButtonBackground,
                                contentColor = dockColors.iconTint,
                                shadowElevation = 0.dp
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = createButtonTitle,
                                        tint = dockColors.iconTint,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // RIGHT BUTTON: Refurbished Menu Hub / Options
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .expressiveBounce(pressScale = 0.90f)
                                    .pointerInput(currentPagerIndex, isExpanded) {
                                        if (!isExpanded) {
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
                                    }
                                    .clickable { isExpanded = !isExpanded },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .graphicsLayer { rotationZ = iconRotation },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Crossfade(
                                        targetState = isExpanded,
                                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
                                        label = "dockHubCrossfade"
                                    ) { expanded ->
                                        Icon(
                                            imageVector = if (expanded) Icons.Default.Close else Icons.Default.GridView,
                                            contentDescription = if (expanded) strings.closeAction else strings.navTitle,
                                            tint = dockColors.iconTint,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
