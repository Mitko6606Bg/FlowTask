package com.flowtask.app.ui.components

import android.graphics.Bitmap
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.flowtask.app.data.local.entity.InstantSnapEntity
import com.flowtask.app.util.DateTimeUtils
import com.flowtask.app.util.ImageUtils
import com.flowtask.app.util.LocalAppStrings
import com.flowtask.app.util.ZoomHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Full-screen immersive photo viewer with smooth pinch-to-zoom,
 * pan navigation when zoomed, double-tap zoom toggle, and top action controls.
 */
@Composable
fun FullScreenImageViewer(
    snap: InstantSnapEntity,
    initialBitmap: Bitmap?,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val coroutineScope = rememberCoroutineScope()

    var highResBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var showControls by remember { mutableStateOf(true) }

    val scaleAnim = remember { Animatable(1f) }
    val offsetXAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(0f) }

    // Progressive loading: Load crisp high-resolution bitmap in the background
    LaunchedEffect(snap.imagePath) {
        withContext(Dispatchers.IO) {
            val highRes = ImageUtils.loadHighResBitmap(snap.imagePath, maxDimension = 2048)
            withContext(Dispatchers.Main) {
                highResBitmap = highRes
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        val isZoomed = scaleAnim.value > 1.15f

        // If zoomed in, back gesture interactively resets zoom first
        PredictiveBackHandler(enabled = isZoomed) { progress: Flow<BackEventCompat> ->
            val startScale = scaleAnim.value
            val startOffsetX = offsetXAnim.value
            val startOffsetY = offsetYAnim.value
            try {
                progress.collect { backEvent ->
                    val p = backEvent.progress
                    val currentTarget = (startScale - (startScale - 1f) * p).coerceAtLeast(1f)
                    scaleAnim.snapTo(currentTarget)
                    offsetXAnim.snapTo(startOffsetX * (1f - p))
                    offsetYAnim.snapTo(startOffsetY * (1f - p))
                }
                coroutineScope.launch {
                    launch { scaleAnim.animateTo(1f, tween(250)) }
                    launch { offsetXAnim.animateTo(0f, tween(250)) }
                    launch { offsetYAnim.animateTo(0f, tween(250)) }
                }
            } catch (_: CancellationException) {
                coroutineScope.launch {
                    launch { scaleAnim.animateTo(startScale, tween(250)) }
                    launch { offsetXAnim.animateTo(startOffsetX, tween(250)) }
                    launch { offsetYAnim.animateTo(startOffsetY, tween(250)) }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .onSizeChanged { containerSize = it }
        ) {
            // Centered Image with Zoom & Pan Gestures
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(containerSize) {
                        detectTapGestures(
                            onDoubleTap = { tapOffset ->
                                coroutineScope.launch {
                                    if (scaleAnim.value > 1.15f) {
                                        // Reset zoom back to 1.0x
                                        launch { scaleAnim.animateTo(1f, tween(250)) }
                                        launch { offsetXAnim.animateTo(0f, tween(250)) }
                                        launch { offsetYAnim.animateTo(0f, tween(250)) }
                                    } else {
                                        // Double-tap zoom into tapped point
                                        val targetScale = 2.5f
                                        val center = Offset(containerSize.width / 2f, containerSize.height / 2f)
                                        val maxOffset = ZoomHelper.calculateMaxOffset(
                                            containerSize = containerSize,
                                            scale = targetScale
                                        )
                                        val targetOffset = ZoomHelper.calculateDoubleTapTarget(
                                            tapOffset = tapOffset,
                                            center = center,
                                            targetScale = targetScale,
                                            maxOffset = maxOffset
                                        )
                                        launch { scaleAnim.animateTo(targetScale, tween(250)) }
                                        launch { offsetXAnim.animateTo(targetOffset.x, tween(250)) }
                                        launch { offsetYAnim.animateTo(targetOffset.y, tween(250)) }
                                    }
                                }
                            },
                            onTap = {
                                showControls = !showControls
                            }
                        )
                    }
                    .pointerInput(containerSize) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val currentScale = scaleAnim.value
                            val newScale = (currentScale * zoom).coerceIn(1f, 5f)
                            coroutineScope.launch {
                                scaleAnim.snapTo(newScale)
                                if (newScale > 1.01f) {
                                    val maxOffset = ZoomHelper.calculateMaxOffset(
                                        containerSize = containerSize,
                                        scale = newScale
                                    )
                                    val newX = (offsetXAnim.value + pan.x).coerceIn(-maxOffset.x, maxOffset.x)
                                    val newY = (offsetYAnim.value + pan.y).coerceIn(-maxOffset.y, maxOffset.y)
                                    offsetXAnim.snapTo(newX)
                                    offsetYAnim.snapTo(newY)
                                } else {
                                    offsetXAnim.snapTo(0f)
                                    offsetYAnim.snapTo(0f)
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val displayBitmap = highResBitmap ?: initialBitmap

                if (displayBitmap != null) {
                    Image(
                        bitmap = displayBitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .graphicsLayer {
                                scaleX = scaleAnim.value
                                scaleY = scaleAnim.value
                                translationX = offsetXAnim.value
                                translationY = offsetYAnim.value
                            }
                    )
                } else {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Top Bar Overlay
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.closeAction,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Folder & Date Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (snap.folderName == "Unsorted") strings.folderUnsorted else snap.folderName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "•",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = DateTimeUtils.formatDate(snap.createdAtMillis),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    // Zoom Indicator / Reset Button
                    val isZoomed = scaleAnim.value > 1.05f
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isZoomed) Color(0xFF2563EB).copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                coroutineScope.launch {
                                    if (isZoomed) {
                                        launch { scaleAnim.animateTo(1f, tween(250)) }
                                        launch { offsetXAnim.animateTo(0f, tween(250)) }
                                        launch { offsetYAnim.animateTo(0f, tween(250)) }
                                    } else {
                                        launch { scaleAnim.animateTo(2.5f, tween(250)) }
                                    }
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            if (isZoomed) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = strings.resetZoomAction,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = String.format(Locale.US, "%.1fx", scaleAnim.value),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            // Bottom Note Bar Overlay (if note exists)
            if (!snap.noteText.isNullOrBlank()) {
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(200)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = snap.noteText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                ),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
