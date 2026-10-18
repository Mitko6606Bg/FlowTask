package com.flowtask.app

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.flowtask.app.util.ZoomHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ZoomHelperTest {

    @Test
    fun calculateMaxOffset_atScaleOne_returnsZero() {
        val container = IntSize(1080, 2400)
        val maxOffset = ZoomHelper.calculateMaxOffset(container, 1.0f)
        assertEquals(0f, maxOffset.x, 0.001f)
        assertEquals(0f, maxOffset.y, 0.001f)
    }

    @Test
    fun calculateMaxOffset_atScaleLessThanOne_returnsZero() {
        val container = IntSize(1080, 2400)
        val maxOffset = ZoomHelper.calculateMaxOffset(container, 0.8f)
        assertEquals(0f, maxOffset.x, 0.001f)
        assertEquals(0f, maxOffset.y, 0.001f)
    }

    @Test
    fun calculateMaxOffset_zoomedBeyondWidthAndHeight_calculatesCorrectBounds() {
        val container = IntSize(1080, 2400)
        // At scale 2.5, baseSize = 1080, scaledSize = 2700
        // maxPanX = (2700 - 1080) / 2 = 810
        // maxPanY = (2700 - 2400) / 2 = 150
        val maxOffset = ZoomHelper.calculateMaxOffset(container, 2.5f)
        assertEquals(810f, maxOffset.x, 0.01f)
        assertEquals(150f, maxOffset.y, 0.01f)
    }

    @Test
    fun calculateMaxOffset_intermediateZoom_allowsComfortableVerticalPan() {
        val container = IntSize(1080, 2400)
        // At scale 1.5, baseSize = 1080, scaledSize = 1620
        // maxPanX = (1620 - 1080) / 2 = 270
        // maxPanY = min((2400 - 1080)/2 = 660, (1620 - 1080)/2 = 270) = 270
        val maxOffset = ZoomHelper.calculateMaxOffset(container, 1.5f)
        assertEquals(270f, maxOffset.x, 0.01f)
        assertEquals(270f, maxOffset.y, 0.01f)
    }

    @Test
    fun calculateDoubleTapTarget_centersTappedLocation() {
        val container = IntSize(1080, 2400)
        val center = Offset(540f, 1200f)
        val maxOffset = ZoomHelper.calculateMaxOffset(container, 2.5f) // (810, 150)

        // Tap on left side: x = 140, y = 1200
        val tapOffset = Offset(140f, 1200f)
        val target = ZoomHelper.calculateDoubleTapTarget(
            tapOffset = tapOffset,
            center = center,
            targetScale = 2.5f,
            maxOffset = maxOffset
        )

        // (540 - 140) * 1.5 = 600
        assertEquals(600f, target.x, 0.01f)
        assertEquals(0f, target.y, 0.01f)
    }

    @Test
    fun calculateDoubleTapTarget_clampedToMaxOffset() {
        val container = IntSize(1080, 2400)
        val center = Offset(540f, 1200f)
        val maxOffset = ZoomHelper.calculateMaxOffset(container, 2.5f) // (810, 150)

        // Tap on extreme left: x = 0
        // (540 - 0) * 1.5 = 810 (matches maxPanX exactly)
        val tapOffset = Offset(0f, 1200f)
        val target = ZoomHelper.calculateDoubleTapTarget(
            tapOffset = tapOffset,
            center = center,
            targetScale = 2.5f,
            maxOffset = maxOffset
        )
        assertEquals(810f, target.x, 0.01f)
        assertTrue(target.x <= maxOffset.x)
    }

    @Test
    fun clampOffset_constrainsValuesWithinBounds() {
        val maxOffset = Offset(300f, 200f)
        val clamped = ZoomHelper.clampOffset(Offset(500f, -400f), maxOffset)
        assertEquals(300f, clamped.x, 0.001f)
        assertEquals(-200f, clamped.y, 0.001f)
    }
}
