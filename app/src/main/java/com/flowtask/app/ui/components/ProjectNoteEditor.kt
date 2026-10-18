package com.flowtask.app.ui.components

import androidx.activity.BackEventCompat
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.delay
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowtask.app.data.local.entity.ProjectNoteEntity
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.OutlineVariant
import com.flowtask.app.ui.theme.SurfaceContainerHigh
import com.flowtask.app.ui.theme.SurfaceContainerHighest
import com.flowtask.app.ui.theme.SurfaceContainerLow
import com.flowtask.app.ui.theme.SurfaceContainerLowest
import com.flowtask.app.ui.theme.TextOnAccent
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.util.LocalAppStrings
import java.util.UUID

private const val ZWS = "\u200B"

/**
 * Mutable state wrapper for each block inside the note editor.
 */
class NoteBlockState(
    val id: String = UUID.randomUUID().toString(),
    initialType: NoteBlockType = NoteBlockType.PARAGRAPH,
    initialText: String = "",
    initialChecked: Boolean = false
) {
    var type by mutableStateOf(initialType)
    var textValue by mutableStateOf(
        if (initialText.isEmpty()) TextFieldValue(ZWS, selection = TextRange(1))
        else TextFieldValue(initialText, selection = TextRange(initialText.length))
    )
    var isChecked by mutableStateOf(initialChecked)

    fun toNoteBlock(): NoteBlock = NoteBlock(id, type, textValue.text.replace(ZWS, ""), isChecked)

    fun copy(): NoteBlockState = NoteBlockState(
        id = id,
        initialType = type,
        initialText = textValue.text.replace(ZWS, ""),
        initialChecked = isChecked
    )
}

data class NoteHistorySnapshot(
    val blocks: List<NoteBlock>,
    val title: String,
    val activeBlockId: String
)

/**
 * Material 3 Expressive Project Note Editor with full WYSIWYG rich-text,
 * real interactive checkboxes, and keyboard-docked toolbar with bottom fill.
 */
@Composable
fun ProjectNoteEditor(
    note: ProjectNoteEntity?,
    projectTitle: String = "",
    folderPath: String? = null,
    originBounds: Rect? = null,
    onDismiss: () -> Unit,
    onAutoSave: ((Long?, String, String) -> Unit)? = null,
    onSave: (Long?, String, String) -> Unit
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    var isFullScreen by remember { mutableStateOf(false) }
    var isCardVisible by remember { mutableStateOf(true) }
    var isDismissing by remember { mutableStateOf(false) }
    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    var predictiveBackEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var verticalDragAccumulator by remember { mutableFloatStateOf(0f) }
    var titleValue by remember { mutableStateOf(TextFieldValue(note?.title ?: "")) }

    // Parse initial blocks from Markdown
    val blocks = remember {
        mutableStateListOf<NoteBlockState>().apply {
            val initial = NoteRichParser.parseMarkdownToBlocks(note?.content ?: "")
            addAll(initial.map { NoteBlockState(it.id, it.type, it.text, it.isChecked) })
            if (isEmpty()) {
                add(NoteBlockState())
            }
        }
    }

    var activeBlockId by remember { mutableStateOf(blocks.firstOrNull()?.id ?: "") }
    var pendingFocusId by remember { mutableStateOf<String?>(null) }
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }
    val titleFocusRequester = remember { FocusRequester() }

    // Handle programmatic focus transition
    LaunchedEffect(pendingFocusId) {
        pendingFocusId?.let { id ->
            if (id == "TITLE") {
                try {
                    titleFocusRequester.requestFocus()
                } catch (_: Exception) {}
            } else {
                kotlinx.coroutines.yield()
                try {
                    focusRequesters[id]?.requestFocus()
                } catch (_: Exception) {
                    try {
                        kotlinx.coroutines.delay(50)
                        focusRequesters[id]?.requestFocus()
                    } catch (_: Exception) {}
                }
            }
            pendingFocusId = null
        }
    }

    // Dismiss with automatic saving of pending content and fluid card / fullscreen transition
    val animatedDismiss: () -> Unit = {
        if (!isDismissing) {
            isDismissing = true
            keyboardController?.hide()
            isCardVisible = false
            val serialized = NoteRichParser.serializeBlocksToMarkdown(blocks.map { it.toNoteBlock() })
            val hasContent = titleValue.text.isNotBlank() || blocks.any { it.textValue.text.replace(ZWS, "").isNotBlank() }

            coroutineScope.launch {
                delay(220)
                if (hasContent) {
                    onSave(note?.id, titleValue.text, serialized)
                } else {
                    onDismiss()
                }
            }
        }
    }

    fun dismissWithAutoSave() {
        animatedDismiss()
    }

    // Track state fingerprint for debounced autosave
    var initialContentCaptured by remember { mutableStateOf(false) }
    var lastSavedFingerprint by remember { mutableStateOf("") }
    val currentFingerprint by remember {
        derivedStateOf {
            titleValue.text + "|||" + blocks.joinToString("---") { "${it.id}:${it.type.name}:${it.isChecked}:${it.textValue.text.replace(ZWS, "")}" }
        }
    }

    LaunchedEffect(Unit) {
        lastSavedFingerprint = currentFingerprint
        initialContentCaptured = true
    }

    LaunchedEffect(currentFingerprint) {
        if (!initialContentCaptured) return@LaunchedEffect
        if (currentFingerprint != lastSavedFingerprint) {
            val hasContent = titleValue.text.isNotBlank() || blocks.any { it.textValue.text.replace(ZWS, "").isNotBlank() }
            if (hasContent && onAutoSave != null) {
                delay(800)
                val serialized = NoteRichParser.serializeBlocksToMarkdown(blocks.map { it.toNoteBlock() })
                onAutoSave(note?.id, titleValue.text, serialized)
                lastSavedFingerprint = currentFingerprint
            }
        }
    }

    // Undo / Redo history stacks
    val undoStack = remember { mutableStateListOf<NoteHistorySnapshot>() }
    val redoStack = remember { mutableStateListOf<NoteHistorySnapshot>() }

    fun captureSnapshot(): NoteHistorySnapshot {
        return NoteHistorySnapshot(
            blocks = blocks.map { it.toNoteBlock() },
            title = titleValue.text,
            activeBlockId = activeBlockId
        )
    }

    fun recordUndo() {
        undoStack.add(captureSnapshot())
        redoStack.clear()
        if (undoStack.size > 50) {
            undoStack.removeAt(0)
        }
    }

    fun executeUndo() {
        if (undoStack.isNotEmpty()) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            redoStack.add(captureSnapshot())
            val snapshot = undoStack.removeLast()
            titleValue = TextFieldValue(snapshot.title)
            blocks.clear()
            blocks.addAll(snapshot.blocks.map { NoteBlockState(it.id, it.type, it.text, it.isChecked) })
            activeBlockId = snapshot.activeBlockId
            pendingFocusId = snapshot.activeBlockId
        }
    }

    fun executeRedo() {
        if (redoStack.isNotEmpty()) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            undoStack.add(captureSnapshot())
            val snapshot = redoStack.removeLast()
            titleValue = TextFieldValue(snapshot.title)
            blocks.clear()
            blocks.addAll(snapshot.blocks.map { NoteBlockState(it.id, it.type, it.text, it.isChecked) })
            activeBlockId = snapshot.activeBlockId
            pendingFocusId = snapshot.activeBlockId
        }
    }

    fun handleBackspace(index: Int, block: NoteBlockState): Boolean {
        // If there is an active selection (not a collapsed cursor), allow default deletion of selection
        if (block.textValue.selection.start != block.textValue.selection.end) {
            return false
        }
        val cleanText = block.textValue.text.replace(ZWS, "")
        val cursor = block.textValue.selection.start

        // If the block has text and cursor is not at the very start (0, or 1 if ZWS anchored), allow normal character deletion
        if (cleanText.isNotEmpty() && cursor > 1) {
            return false
        }
        if (cleanText.isNotEmpty() && cursor == 1 && !block.textValue.text.startsWith(ZWS)) {
            return false
        }

        // Case 1: Formatted block (Heading, Bullet, Numbered, Checklist, Quote, Code)
        // Immediately reset format to standard Paragraph so template text / dots / numbers disappear
        if (block.type != NoteBlockType.PARAGRAPH) {
            recordUndo()
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            block.type = NoteBlockType.PARAGRAPH
            block.isChecked = false
            if (cleanText.isEmpty()) {
                block.textValue = TextFieldValue(ZWS, selection = TextRange(1))
            }
            activeBlockId = block.id
            pendingFocusId = block.id
            return true
        }

        // Case 2: Standard Paragraph at index > 0 -> merge with previous line or delete empty line
        if (index > 0) {
            recordUndo()
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            val prevBlock = blocks[index - 1]

            if (prevBlock.type == NoteBlockType.DIVIDER) {
                blocks.removeAt(index - 1)
                activeBlockId = block.id
                pendingFocusId = block.id
                return true
            }

            val prevClean = prevBlock.textValue.text.replace(ZWS, "")
            val currentClean = cleanText
            val mergedText = prevClean + currentClean

            prevBlock.textValue = if (mergedText.isEmpty()) {
                TextFieldValue(ZWS, selection = TextRange(1))
            } else {
                TextFieldValue(
                    text = mergedText,
                    selection = TextRange(prevClean.length)
                )
            }
            blocks.removeAt(index)
            activeBlockId = prevBlock.id
            pendingFocusId = prevBlock.id
            return true
        }

        // Case 3: First block at index 0 -> navigate back up to Title
        if (index == 0) {
            recordUndo()
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            pendingFocusId = "TITLE"
            titleValue = titleValue.copy(
                selection = TextRange(titleValue.text.length)
            )
            return true
        }

        return false
    }

    fun handleEnter(index: Int, block: NoteBlockState, newVal: TextFieldValue) {
        recordUndo()
        val clean = newVal.text.replace(ZWS, "")
        val lines = clean.split("\n")
        val before = lines[0]
        val after = lines.drop(1).joinToString("\n")

        if (block.type == NoteBlockType.CHECKLIST || block.type == NoteBlockType.BULLET || block.type == NoteBlockType.NUMBERED) {
            if (before.isBlank()) {
                // Empty list item reverts to paragraph
                block.type = NoteBlockType.PARAGRAPH
                block.textValue = TextFieldValue(ZWS, selection = TextRange(1))
                pendingFocusId = block.id
                return
            }
        }

        block.textValue = if (before.isEmpty()) {
            TextFieldValue(ZWS, selection = TextRange(1))
        } else {
            TextFieldValue(before, selection = TextRange(before.length))
        }

        val nextType = when (block.type) {
            NoteBlockType.CHECKLIST -> NoteBlockType.CHECKLIST
            NoteBlockType.BULLET -> NoteBlockType.BULLET
            NoteBlockType.NUMBERED -> NoteBlockType.NUMBERED
            else -> NoteBlockType.PARAGRAPH
        }
        val newBlock = NoteBlockState(initialType = nextType, initialText = after)
        blocks.add(index + 1, newBlock)
        activeBlockId = newBlock.id
        pendingFocusId = newBlock.id
    }

    fun handleBlockValueChange(index: Int, block: NoteBlockState, newVal: TextFieldValue) {
        val prevText = block.textValue.text
        val newText = newVal.text

        // Soft keyboard deleted the zero-width space on an empty block
        if (newText.isEmpty() && (prevText == ZWS || prevText.isEmpty())) {
            handleBackspace(index, block)
            return
        }

        // Soft keyboard inserted newline (Enter key)
        if (newText.contains("\n")) {
            handleEnter(index, block, newVal)
            return
        }

        val clean = newText.replace(ZWS, "")

        if (clean.isEmpty()) {
            // Text was just deleted down to empty; restore ZWS anchor for next backspace
            block.textValue = TextFieldValue(ZWS, selection = TextRange(1))
            return
        }

        // User typed characters into the block
        if (newText.contains(ZWS)) {
            val cursor = (newVal.selection.start - 1).coerceIn(0, clean.length)
            block.textValue = TextFieldValue(clean, selection = TextRange(cursor))
        } else {
            block.textValue = newVal
        }
    }

    // Predictive Back Gesture Handler:
    // When in bottom-sheet card mode: scales and pulls card down into dismiss (matching EditTaskBottomSheet).
    // When in full-screen mode: scales, rounds sheet corners, and peeks background (matching native Android full-screen pages).
    PredictiveBackHandler(enabled = isCardVisible && !isDismissing) { progress: Flow<BackEventCompat> ->
        try {
            progress.collect { backEvent ->
                predictiveBackProgress = backEvent.progress
                predictiveBackEdge = backEvent.swipeEdge
            }
            animatedDismiss()
        } catch (_: CancellationException) {
            predictiveBackProgress = 0f
        } finally {
            predictiveBackProgress = 0f
        }
    }

    val cornerRadius by animateDpAsState(
        targetValue = if (isFullScreen) 0.dp else 28.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "noteCornerRadius"
    )

    val heightFraction by animateFloatAsState(
        targetValue = if (isFullScreen) 1.0f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "noteHeightFraction"
    )

    fun handleDragDelta(delta: Float) {
        if (isDismissing) return
        verticalDragAccumulator += delta
        if (verticalDragAccumulator < -36f && !isFullScreen) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            isFullScreen = true
            verticalDragAccumulator = 0f
        } else if (verticalDragAccumulator > 36f) {
            if (isFullScreen) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isFullScreen = false
                verticalDragAccumulator = 0f
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                animatedDismiss()
                verticalDragAccumulator = 0f
            }
        }
    }

    val primaryColor = TextPrimary
    val visualTransformation = remember(currentAccent, primaryColor) {
        CleanMarkdownVisualTransformation(
            textPrimary = primaryColor,
            accentColor = currentAccent
        )
    }

    val activeBlock = blocks.find { it.id == activeBlockId } ?: blocks.firstOrNull()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Scrim backdrop (active in card mode, hidden in full-screen)
        AnimatedVisibility(
            visible = isCardVisible && !isFullScreen,
            enter = fadeIn(animationSpec = tween(durationMillis = 180)),
            exit = fadeOut(animationSpec = tween(durationMillis = 150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.52f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = animatedDismiss
                    )
            )
        }

        // Note Card / Full-Screen Page Surface
        AnimatedVisibility(
            visible = isCardVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            enter = if (isFullScreen) {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(
                        durationMillis = 280,
                        easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
                    )
                ) + fadeIn(animationSpec = tween(durationMillis = 180))
            } else {
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(
                        durationMillis = 280,
                        easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
                    )
                ) + fadeIn(animationSpec = tween(durationMillis = 180))
            },
            exit = if (isFullScreen) {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth ->
                        if (predictiveBackEdge == BackEventCompat.EDGE_RIGHT) -fullWidth else fullWidth
                    },
                    animationSpec = tween(
                        durationMillis = 220,
                        easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
                    )
                ) + fadeOut(animationSpec = tween(durationMillis = 180))
            } else {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
                    )
                ) + fadeOut(animationSpec = tween(durationMillis = 150))
            }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(heightFraction)
                    .graphicsLayer {
                        if (predictiveBackProgress > 0f) {
                            if (isFullScreen) {
                                // Native Predictive Back for Full-Screen Pages:
                                // Scales slightly down, translates with the swipe edge, rounds corners to reveal background
                                val scale = 1f - (predictiveBackProgress * 0.08f)
                                scaleX = scale
                                scaleY = scale
                                translationX = if (predictiveBackEdge == BackEventCompat.EDGE_LEFT) {
                                    predictiveBackProgress * 28.dp.toPx()
                                } else {
                                    -predictiveBackProgress * 28.dp.toPx()
                                }
                                alpha = 1f - (predictiveBackProgress * 0.18f)
                                val cornerPx = predictiveBackProgress * 28.dp.toPx()
                                shape = RoundedCornerShape(cornerPx)
                                clip = true
                            } else {
                                // Predictive Back for Bottom Cards throughout the app (EditTaskBottomSheet)
                                val scale = 1f - (predictiveBackProgress * 0.08f)
                                scaleX = scale
                                scaleY = scale
                                translationX = if (predictiveBackEdge == BackEventCompat.EDGE_LEFT) {
                                    predictiveBackProgress * 24.dp.toPx()
                                } else {
                                    -predictiveBackProgress * 24.dp.toPx()
                                }
                                translationY = predictiveBackProgress * 140.dp.toPx()
                                alpha = 1f - (predictiveBackProgress * 0.18f)
                            }
                        }
                    }
                    .offset { IntOffset(0, dragOffsetY.roundToInt().coerceAtLeast(0)) }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
                color = SurfaceContainerLowest,
                border = BorderStroke(
                    1.dp,
                    OutlineSubtle.copy(alpha = if (isFullScreen) 0f else 0.5f)
                ),
                shadowElevation = if (isFullScreen) 0.dp else 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (isFullScreen) Modifier.statusBarsPadding() else Modifier)
                        .imePadding()
                ) {
                // 1. DRAG HANDLE
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .pointerInput(isFullScreen) {
                            detectVerticalDragGestures(
                                onDragStart = { verticalDragAccumulator = 0f },
                                onDragEnd = { verticalDragAccumulator = 0f },
                                onDragCancel = { verticalDragAccumulator = 0f },
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    handleDragDelta(dragAmount)
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(38.dp)
                            .height(4.5.dp)
                            .clip(CircleShape)
                            .background(TextSecondary.copy(alpha = 0.35f))
                    )
                }

                // 2. HEADER ROW
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
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
                                .clip(RoundedCornerShape(12.dp))
                                .background(currentAccent.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NoteAlt,
                                contentDescription = null,
                                tint = currentAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (note == null) strings.newNoteTitle else strings.editNoteTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = TextPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (projectTitle.isNotBlank()) {
                                val breadcrumbText = if (!folderPath.isNullOrBlank()) "$projectTitle › $folderPath" else projectTitle
                                Text(
                                    text = breadcrumbText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary.copy(alpha = 0.75f),
                                        fontSize = 12.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Header Action Buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Expand / Collapse Tonal Button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainerHigh)
                                .border(1.dp, OutlineSubtle.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                .expressiveBounce(pressScale = 0.88f)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    isFullScreen = !isFullScreen
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFullScreen) Icons.Default.CloseFullscreen else Icons.Default.OpenInFull,
                                contentDescription = if (isFullScreen) strings.collapseNoteAction else strings.expandNoteAction,
                                tint = TextPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Save Checkmark Accent Button
                        val hasContent = blocks.any { it.textValue.text.replace(ZWS, "").isNotBlank() } || titleValue.text.isNotBlank()
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (hasContent) currentAccent else currentAccent.copy(alpha = 0.35f))
                                .expressiveBounce(pressScale = 0.88f)
                                .clickable(enabled = hasContent) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    dismissWithAutoSave()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = strings.saveNoteButton,
                                tint = if (hasContent) TextOnAccent else TextOnAccent.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Close "X" Button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainerHigh)
                                .border(1.dp, OutlineSubtle.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                .expressiveBounce(pressScale = 0.88f)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    dismissWithAutoSave()
                                },
                            contentAlignment = Alignment.Center
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

                Spacer(modifier = Modifier.height(4.dp))

                // 3. FRAMELESS TITLE INPUT
                BasicTextField(
                    value = titleValue,
                    onValueChange = { titleValue = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .focusRequester(titleFocusRequester)
                        .onPreviewKeyEvent { keyEvent ->
                            if ((keyEvent.key == Key.Enter || keyEvent.key == Key.DirectionDown) && keyEvent.type == KeyEventType.KeyDown) {
                                blocks.firstOrNull()?.let { firstBlock ->
                                    activeBlockId = firstBlock.id
                                    pendingFocusId = firstBlock.id
                                }
                                true
                            } else false
                        },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            blocks.firstOrNull()?.let { firstBlock ->
                                activeBlockId = firstBlock.id
                                pendingFocusId = firstBlock.id
                            }
                        }
                    ),
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isFullScreen) 24.sp else 20.sp,
                        color = TextPrimary
                    ),
                    cursorBrush = SolidColor(currentAccent),
                    decorationBox = { innerTextField ->
                        if (titleValue.text.isEmpty()) {
                            Text(
                                text = strings.noteTitlePlaceholder,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isFullScreen) 24.sp else 20.sp,
                                    color = TextSecondary.copy(alpha = 0.45f)
                                )
                            )
                        }
                        innerTextField()
                    }
                )

                // 4. SUBTLE DIVIDER
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    thickness = 1.dp,
                    color = OutlineSubtle.copy(alpha = 0.45f)
                )

                // 5. RICH BLOCK DOCUMENT CONTENT
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var numberedCounter = 1

                    blocks.forEachIndexed { index, block ->
                        val focusRequester = focusRequesters.getOrPut(block.id) { FocusRequester() }

                        when (block.type) {
                            NoteBlockType.CHECKLIST -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // REAL CLICKABLE CHECKBOX
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (block.isChecked) currentAccent else Color.Transparent)
                                            .border(
                                                width = 1.8.dp,
                                                color = if (block.isChecked) currentAccent else OutlineVariant.copy(alpha = 0.8f),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .expressiveBounce(pressScale = 0.90f)
                                            .clickable {
                                                recordUndo()
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                block.isChecked = !block.isChecked
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (block.isChecked) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = TextOnAccent,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }

                                    // EDITABLE CHECKLIST TEXT
                                    BasicTextField(
                                        value = block.textValue,
                                        onValueChange = { newVal -> handleBlockValueChange(index, block, newVal) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .focusRequester(focusRequester)
                                            .onPreviewKeyEvent { event ->
                                                if (event.key == Key.Backspace && event.type == KeyEventType.KeyDown) {
                                                    handleBackspace(index, block)
                                                } else false
                                            }
                                            .onFocusChanged { if (it.isFocused) activeBlockId = block.id },
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 16.sp,
                                            lineHeight = 24.sp,
                                            color = if (block.isChecked) TextSecondary.copy(alpha = 0.55f) else TextPrimary,
                                            textDecoration = if (block.isChecked) TextDecoration.LineThrough else null
                                        ),
                                        cursorBrush = SolidColor(currentAccent),
                                        visualTransformation = visualTransformation,
                                        decorationBox = { inner ->
                                            if (block.textValue.text.replace(ZWS, "").isEmpty()) {
                                                Text(
                                                    text = strings.addSubtaskPlaceholder,
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontSize = 16.sp,
                                                        color = TextSecondary.copy(alpha = 0.40f)
                                                    )
                                                )
                                            }
                                            inner()
                                        }
                                    )
                                }
                            }

                            NoteBlockType.BULLET -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(currentAccent)
                                    )
                                    BasicTextField(
                                        value = block.textValue,
                                        onValueChange = { newVal -> handleBlockValueChange(index, block, newVal) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .focusRequester(focusRequester)
                                            .onPreviewKeyEvent { event ->
                                                if (event.key == Key.Backspace && event.type == KeyEventType.KeyDown) {
                                                    handleBackspace(index, block)
                                                } else false
                                            }
                                            .onFocusChanged { if (it.isFocused) activeBlockId = block.id },
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 16.sp,
                                            lineHeight = 24.sp,
                                            color = TextPrimary
                                        ),
                                        cursorBrush = SolidColor(currentAccent),
                                        visualTransformation = visualTransformation,
                                        decorationBox = { inner ->
                                            if (block.textValue.text.replace(ZWS, "").isEmpty()) {
                                                Text(
                                                    text = "• ...",
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, color = TextSecondary.copy(alpha = 0.40f))
                                                )
                                            }
                                            inner()
                                        }
                                    )
                                }
                            }

                            NoteBlockType.NUMBERED -> {
                                val displayNum = numberedCounter++
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "$displayNum.",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = currentAccent
                                        )
                                    )
                                    BasicTextField(
                                        value = block.textValue,
                                        onValueChange = { newVal -> handleBlockValueChange(index, block, newVal) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .focusRequester(focusRequester)
                                            .onPreviewKeyEvent { event ->
                                                if (event.key == Key.Backspace && event.type == KeyEventType.KeyDown) {
                                                    handleBackspace(index, block)
                                                } else false
                                            }
                                            .onFocusChanged { if (it.isFocused) activeBlockId = block.id },
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 16.sp,
                                            lineHeight = 24.sp,
                                            color = TextPrimary
                                        ),
                                        cursorBrush = SolidColor(currentAccent),
                                        visualTransformation = visualTransformation,
                                        decorationBox = { inner ->
                                            if (block.textValue.text.replace(ZWS, "").isEmpty()) {
                                                Text(
                                                    text = "$displayNum. ...",
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, color = TextSecondary.copy(alpha = 0.40f))
                                                )
                                            }
                                            inner()
                                        }
                                    )
                                }
                            }

                            NoteBlockType.HEADING_1 -> {
                                BasicTextField(
                                    value = block.textValue,
                                    onValueChange = { newVal -> handleBlockValueChange(index, block, newVal) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester)
                                        .onPreviewKeyEvent { event ->
                                            if (event.key == Key.Backspace && event.type == KeyEventType.KeyDown) {
                                                handleBackspace(index, block)
                                            } else false
                                        }
                                        .onFocusChanged { if (it.isFocused) activeBlockId = block.id },
                                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    ),
                                    cursorBrush = SolidColor(currentAccent),
                                    visualTransformation = visualTransformation,
                                    decorationBox = { inner ->
                                        if (block.textValue.text.replace(ZWS, "").isEmpty()) {
                                            Text(
                                                text = "Heading 1",
                                                style = MaterialTheme.typography.headlineMedium.copy(
                                                    fontSize = 22.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextSecondary.copy(alpha = 0.40f)
                                                )
                                            )
                                        }
                                        inner()
                                    }
                                )
                            }

                            NoteBlockType.HEADING_2 -> {
                                BasicTextField(
                                    value = block.textValue,
                                    onValueChange = { newVal -> handleBlockValueChange(index, block, newVal) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester)
                                        .onPreviewKeyEvent { event ->
                                            if (event.key == Key.Backspace && event.type == KeyEventType.KeyDown) {
                                                handleBackspace(index, block)
                                            } else false
                                        }
                                        .onFocusChanged { if (it.isFocused) activeBlockId = block.id },
                                    textStyle = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    ),
                                    cursorBrush = SolidColor(currentAccent),
                                    visualTransformation = visualTransformation,
                                    decorationBox = { inner ->
                                        if (block.textValue.text.replace(ZWS, "").isEmpty()) {
                                            Text(
                                                text = "Heading 2",
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontSize = 19.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextSecondary.copy(alpha = 0.40f)
                                                )
                                            )
                                        }
                                        inner()
                                    }
                                )
                            }

                            NoteBlockType.HEADING_3 -> {
                                BasicTextField(
                                    value = block.textValue,
                                    onValueChange = { newVal -> handleBlockValueChange(index, block, newVal) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester)
                                        .onPreviewKeyEvent { event ->
                                            if (event.key == Key.Backspace && event.type == KeyEventType.KeyDown) {
                                                handleBackspace(index, block)
                                            } else false
                                        }
                                        .onFocusChanged { if (it.isFocused) activeBlockId = block.id },
                                    textStyle = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    ),
                                    cursorBrush = SolidColor(currentAccent),
                                    visualTransformation = visualTransformation,
                                    decorationBox = { inner ->
                                        if (block.textValue.text.replace(ZWS, "").isEmpty()) {
                                            Text(
                                                text = "Heading 3",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = TextSecondary.copy(alpha = 0.40f)
                                                )
                                            )
                                        }
                                        inner()
                                    }
                                )
                            }

                            NoteBlockType.QUOTE -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(currentAccent.copy(alpha = 0.08f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(3.5.dp)
                                            .height(28.dp)
                                            .clip(CircleShape)
                                            .background(currentAccent)
                                    )
                                    BasicTextField(
                                        value = block.textValue,
                                        onValueChange = { newVal -> handleBlockValueChange(index, block, newVal) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .focusRequester(focusRequester)
                                            .onPreviewKeyEvent { event ->
                                                if (event.key == Key.Backspace && event.type == KeyEventType.KeyDown) {
                                                    handleBackspace(index, block)
                                                } else false
                                            }
                                            .onFocusChanged { if (it.isFocused) activeBlockId = block.id },
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            fontStyle = FontStyle.Italic,
                                            fontSize = 16.sp,
                                            lineHeight = 24.sp,
                                            color = TextPrimary
                                        ),
                                        cursorBrush = SolidColor(currentAccent),
                                        visualTransformation = visualTransformation,
                                        decorationBox = { inner ->
                                            if (block.textValue.text.replace(ZWS, "").isEmpty()) {
                                                Text(
                                                    text = "Quote...",
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontStyle = FontStyle.Italic,
                                                        fontSize = 16.sp,
                                                        color = TextSecondary.copy(alpha = 0.40f)
                                                    )
                                                )
                                            }
                                            inner()
                                        }
                                    )
                                }
                            }

                            NoteBlockType.DIVIDER -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    HorizontalDivider(
                                        modifier = Modifier.weight(1f),
                                        color = OutlineVariant.copy(alpha = 0.5f),
                                        thickness = 1.5.dp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(SurfaceContainerHigh)
                                            .clickable {
                                                recordUndo()
                                                blocks.removeAt(index)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }

                            NoteBlockType.CODE_BLOCK -> {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = SurfaceContainerHighest,
                                    border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.4f))
                                ) {
                                    BasicTextField(
                                        value = block.textValue,
                                        onValueChange = { newVal -> handleBlockValueChange(index, block, newVal) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                            .focusRequester(focusRequester)
                                            .onPreviewKeyEvent { event ->
                                                if (event.key == Key.Backspace && event.type == KeyEventType.KeyDown) {
                                                    handleBackspace(index, block)
                                                } else false
                                            }
                                            .onFocusChanged { if (it.isFocused) activeBlockId = block.id },
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp,
                                            color = currentAccent
                                        ),
                                        cursorBrush = SolidColor(currentAccent),
                                        decorationBox = { inner ->
                                            if (block.textValue.text.replace(ZWS, "").isEmpty()) {
                                                Text(
                                                    text = "// Code block...",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 14.sp,
                                                        color = TextSecondary.copy(alpha = 0.40f)
                                                    )
                                                )
                                            }
                                            inner()
                                        }
                                    )
                                }
                            }

                            NoteBlockType.PARAGRAPH -> {
                                BasicTextField(
                                    value = block.textValue,
                                    onValueChange = { newVal -> handleBlockValueChange(index, block, newVal) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 28.dp)
                                        .focusRequester(focusRequester)
                                        .onPreviewKeyEvent { event ->
                                            if (event.key == Key.Backspace && event.type == KeyEventType.KeyDown) {
                                                handleBackspace(index, block)
                                            } else false
                                        }
                                        .onFocusChanged { if (it.isFocused) activeBlockId = block.id },
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 16.sp,
                                        lineHeight = 24.sp,
                                        color = TextPrimary
                                    ),
                                    cursorBrush = SolidColor(currentAccent),
                                    visualTransformation = visualTransformation,
                                    decorationBox = { inner ->
                                        if (block.textValue.text.replace(ZWS, "").isEmpty() && blocks.size == 1) {
                                            Text(
                                                text = strings.noteContentPlaceholder,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontSize = 16.sp,
                                                    lineHeight = 24.sp,
                                                    color = TextSecondary.copy(alpha = 0.40f)
                                                )
                                            )
                                        }
                                        inner()
                                    }
                                )
                            }
                        }
                    }

                    // Bottom padding inside scroll container (non-clickable, does not capture cursor)
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                    )
                }

                // 6. DOCKED FULL-WIDTH TEXT FORMATTING TOOLBAR
                val currentBlock = activeBlock ?: blocks.firstOrNull()
                val currentText = currentBlock?.textValue?.text?.replace(ZWS, "") ?: ""
                val cursor = (currentBlock?.textValue?.selection?.start ?: 0).coerceIn(0, currentText.length)
                val isBoldActive = NoteRichParser.isFormatActive(currentText, cursor, "**")
                val isItalicActive = NoteRichParser.isFormatActive(currentText, cursor, "*")
                val isStrikeActive = NoteRichParser.isFormatActive(currentText, cursor, "~~")
                val isCodeActive = NoteRichParser.isFormatActive(currentText, cursor, "`")

                NoteFormattingToolbar(
                    currentBlockType = currentBlock?.type ?: NoteBlockType.PARAGRAPH,
                    isBold = isBoldActive,
                    isItalic = isItalicActive,
                    isStrikethrough = isStrikeActive,
                    isCode = isCodeActive,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = redoStack.isNotEmpty(),
                    onUndo = { executeUndo() },
                    onRedo = { executeRedo() },
                    onToggleInline = { prefix, suffix ->
                        recordUndo()
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        currentBlock?.let { b ->
                            val cleanVal = b.textValue.copy(text = b.textValue.text.replace(ZWS, ""))
                            b.textValue = NoteRichParser.toggleInlineFormat(cleanVal, prefix, suffix)
                            pendingFocusId = b.id
                        }
                    },
                    onSetBlockType = { newType ->
                        recordUndo()
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        currentBlock?.let { b ->
                            b.type = if (b.type == newType) NoteBlockType.PARAGRAPH else newType
                            if (b.textValue.text.replace(ZWS, "").isEmpty()) {
                                b.textValue = TextFieldValue(ZWS, selection = TextRange(1))
                            }
                            pendingFocusId = b.id
                        }
                    },
                    onInsertDivider = {
                        recordUndo()
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val currIdx = blocks.indexOfFirst { it.id == activeBlockId }
                        val insertIdx = if (currIdx != -1) currIdx + 1 else blocks.size
                        val dividerBlock = NoteBlockState(initialType = NoteBlockType.DIVIDER, initialText = "")
                        val nextBlock = NoteBlockState(initialType = NoteBlockType.PARAGRAPH, initialText = "")
                        blocks.add(insertIdx, dividerBlock)
                        blocks.add(insertIdx + 1, nextBlock)
                        activeBlockId = nextBlock.id
                        pendingFocusId = nextBlock.id
                    },
                    onDismissKeyboard = {
                        keyboardController?.hide()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
}

/**
 * Full-width docked formatting toolbar aligned with the app's M3 Expressive design language.
 * Covers the entire area through the bottom of the device when keyboard is dismissed.
 */
@Composable
fun NoteFormattingToolbar(
    currentBlockType: NoteBlockType,
    isBold: Boolean,
    isItalic: Boolean,
    isStrikethrough: Boolean,
    isCode: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleInline: (String, String) -> Unit,
    onSetBlockType: (NoteBlockType) -> Unit,
    onInsertDivider: () -> Unit,
    onDismissKeyboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val navBarsPadding = WindowInsets.navigationBars.asPaddingValues()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceContainerLowest,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = navBarsPadding.calculateBottomPadding())
        ) {
            HorizontalDivider(
                color = OutlineVariant.copy(alpha = 0.35f),
                thickness = 1.dp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 1. UNDO / REDO
                ToolbarIconButton(
                    icon = Icons.AutoMirrored.Filled.Undo,
                    contentDesc = strings.formatUndo,
                    enabled = canUndo,
                    onClick = onUndo
                )
                ToolbarIconButton(
                    icon = Icons.AutoMirrored.Filled.Redo,
                    contentDesc = strings.formatRedo,
                    enabled = canRedo,
                    onClick = onRedo
                )

                ToolbarGroupDivider()

                // 2. INLINE FORMATTING (B, I, S, Code)
                ToolbarTextButton(
                    label = "B",
                    fontWeight = FontWeight.Bold,
                    isActive = isBold,
                    contentDesc = strings.formatBold,
                    onClick = { onToggleInline("**", "**") }
                )
                ToolbarTextButton(
                    label = "I",
                    fontStyle = FontStyle.Italic,
                    isActive = isItalic,
                    contentDesc = strings.formatItalic,
                    onClick = { onToggleInline("*", "*") }
                )
                ToolbarTextButton(
                    label = "S",
                    textDecoration = TextDecoration.LineThrough,
                    isActive = isStrikethrough,
                    contentDesc = strings.formatStrikethrough,
                    onClick = { onToggleInline("~~", "~~") }
                )
                ToolbarTextButton(
                    label = "</>",
                    fontFamily = FontFamily.Monospace,
                    isActive = isCode,
                    contentDesc = strings.formatCode,
                    onClick = { onToggleInline("`", "`") }
                )

                ToolbarGroupDivider()

                // 3. HEADINGS (H1, H2, H3)
                ToolbarTextButton(
                    label = "H1",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    isActive = currentBlockType == NoteBlockType.HEADING_1,
                    contentDesc = strings.formatHeading1,
                    onClick = { onSetBlockType(NoteBlockType.HEADING_1) }
                )
                ToolbarTextButton(
                    label = "H2",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    isActive = currentBlockType == NoteBlockType.HEADING_2,
                    contentDesc = strings.formatHeading2,
                    onClick = { onSetBlockType(NoteBlockType.HEADING_2) }
                )
                ToolbarTextButton(
                    label = "H3",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    isActive = currentBlockType == NoteBlockType.HEADING_3,
                    contentDesc = strings.formatHeading3,
                    onClick = { onSetBlockType(NoteBlockType.HEADING_3) }
                )

                ToolbarGroupDivider()

                // 4. LISTS (Checklist, Bullets, Numbers)
                ToolbarIconButton(
                    icon = Icons.Default.Checklist,
                    contentDesc = strings.formatChecklist,
                    isActive = currentBlockType == NoteBlockType.CHECKLIST,
                    onClick = { onSetBlockType(NoteBlockType.CHECKLIST) }
                )
                ToolbarIconButton(
                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                    contentDesc = strings.formatBulletList,
                    isActive = currentBlockType == NoteBlockType.BULLET,
                    onClick = { onSetBlockType(NoteBlockType.BULLET) }
                )
                ToolbarIconButton(
                    icon = Icons.Default.FormatListNumbered,
                    contentDesc = strings.formatNumberedList,
                    isActive = currentBlockType == NoteBlockType.NUMBERED,
                    onClick = { onSetBlockType(NoteBlockType.NUMBERED) }
                )

                ToolbarGroupDivider()

                // 5. BLOCKS (Quote, Divider, Code Block)
                ToolbarIconButton(
                    icon = Icons.Default.FormatQuote,
                    contentDesc = strings.formatQuote,
                    isActive = currentBlockType == NoteBlockType.QUOTE,
                    onClick = { onSetBlockType(NoteBlockType.QUOTE) }
                )
                ToolbarIconButton(
                    icon = Icons.Default.HorizontalRule,
                    contentDesc = strings.formatDivider,
                    onClick = onInsertDivider
                )
                ToolbarIconButton(
                    icon = Icons.Default.Code,
                    contentDesc = strings.formatCode,
                    isActive = currentBlockType == NoteBlockType.CODE_BLOCK,
                    onClick = { onSetBlockType(NoteBlockType.CODE_BLOCK) }
                )

                ToolbarGroupDivider()

                // 6. DISMISS KEYBOARD
                ToolbarIconButton(
                    icon = Icons.Default.KeyboardHide,
                    contentDesc = strings.formatDismissKeyboard,
                    onClick = onDismissKeyboard
                )
            }
        }
    }
}

@Composable
private fun ToolbarGroupDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(1.dp)
            .height(20.dp)
            .background(OutlineVariant.copy(alpha = 0.35f))
    )
}

@Composable
private fun ToolbarIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDesc: String,
    isActive: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val currentAccent = LocalAccentColor.current
    val activeBg = currentAccent.copy(alpha = 0.18f)
    val inactiveBg = Color.Transparent

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isActive) activeBg else inactiveBg)
            .border(
                width = if (isActive) 1.2.dp else 0.dp,
                color = if (isActive) currentAccent.copy(alpha = 0.45f) else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .expressiveBounce(pressScale = 0.90f)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = when {
                !enabled -> TextSecondary.copy(alpha = 0.35f)
                isActive -> currentAccent
                else -> TextPrimary
            },
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun ToolbarTextButton(
    label: String,
    contentDesc: String,
    isActive: Boolean = false,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    textDecoration: TextDecoration? = null,
    fontFamily: FontFamily? = null,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    onClick: () -> Unit
) {
    val currentAccent = LocalAccentColor.current
    val activeBg = currentAccent.copy(alpha = 0.18f)
    val inactiveBg = Color.Transparent

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isActive) activeBg else inactiveBg)
            .border(
                width = if (isActive) 1.2.dp else 0.dp,
                color = if (isActive) currentAccent.copy(alpha = 0.45f) else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .expressiveBounce(pressScale = 0.90f)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontWeight = fontWeight ?: FontWeight.Medium,
            fontStyle = fontStyle,
            textDecoration = textDecoration,
            fontFamily = fontFamily,
            fontSize = fontSize,
            color = if (isActive) currentAccent else TextPrimary
        )
    }
}
