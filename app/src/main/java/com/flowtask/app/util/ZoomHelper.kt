package com.flowtask.app.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.math.min

object ZoomHelper {

    /**
     * Calculates the maximum horizontal and vertical pan offsets allowed
     * for a 1:1 square image fitted inside [containerSize] at [scale].
     */
    fun calculateMaxOffset(containerSize: IntSize, scale: Float): Offset {
        if (scale <= 1f || containerSize.width <= 0 || containerSize.height <= 0) {
            return Offset.Zero
        }

        // Base size of 1:1 image displayed with ContentScale.Fit inside container
        val baseSize = min(containerSize.width, containerSize.height).toFloat()
        val scaledSize = baseSize * scale

        // Max pan X
        val maxPanX = if (scaledSize > containerSize.width) {
            (scaledSize - containerSize.width) / 2f
        } else {
            0f
        }

        // Max pan Y
        val maxPanY = if (scaledSize > containerSize.height) {
            (scaledSize - containerSize.height) / 2f
        } else {
            // Allow panning within vertical viewport bounds when zoomed
            ((containerSize.height - baseSize) / 2f).coerceAtMost((scaledSize - baseSize) / 2f)
        }

        return Offset(
            x = maxPanX.coerceAtLeast(0f),
            y = maxPanY.coerceAtLeast(0f)
        )
    }

    /**
     * Calculates target pan offset when double tapping at [tapOffset] to zoom to [targetScale].
     */
    fun calculateDoubleTapTarget(
        tapOffset: Offset,
        center: Offset,
        targetScale: Float,
        maxOffset: Offset
    ): Offset {
        if (targetScale <= 1f) return Offset.Zero

        // Shift away from tap position to bring tapped area towards the center
        val targetX = (center.x - tapOffset.x) * (targetScale - 1f)
        val targetY = (center.y - tapOffset.y) * (targetScale - 1f)

        return Offset(
            x = targetX.coerceIn(-maxOffset.x, maxOffset.x),
            y = targetY.coerceIn(-maxOffset.y, maxOffset.y)
        )
    }

    /**
     * Clamps an arbitrary pan offset within bounds.
     */
    fun clampOffset(offset: Offset, maxOffset: Offset): Offset {
        return Offset(
            x = offset.x.coerceIn(-maxOffset.x, maxOffset.x),
            y = offset.y.coerceIn(-maxOffset.y, maxOffset.y)
        )
    }
}
