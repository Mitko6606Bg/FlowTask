package com.flowtask.app

import com.flowtask.app.ui.screens.SpringSheetPhysics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpcomingSpringPullSheetTest {

    @Test
    fun testCalculateTargetProgress_fastFlickUp() {
        // Fast upward flick should snap to expanded (1f) regardless of low progress
        val target = SpringSheetPhysics.calculateTargetProgress(
            velocityY = -600f,
            currentProgress = 0.1f
        )
        assertEquals(1f, target, 0.001f)
    }

    @Test
    fun testCalculateTargetProgress_fastFlickDown() {
        // Fast downward flick should snap to collapsed (0f) regardless of high progress
        val target = SpringSheetPhysics.calculateTargetProgress(
            velocityY = 600f,
            currentProgress = 0.9f
        )
        assertEquals(0f, target, 0.001f)
    }

    @Test
    fun testCalculateTargetProgress_slowDragThresholds() {
        // Without fast flick velocity, progress >= 0.5f snaps to 1f, < 0.5f snaps to 0f
        assertEquals(1f, SpringSheetPhysics.calculateTargetProgress(0f, 0.5f), 0.001f)
        assertEquals(1f, SpringSheetPhysics.calculateTargetProgress(0f, 0.8f), 0.001f)
        assertEquals(0f, SpringSheetPhysics.calculateTargetProgress(0f, 0.49f), 0.001f)
        assertEquals(0f, SpringSheetPhysics.calculateTargetProgress(0f, 0.05f), 0.001f)
    }

    @Test
    fun testCalculateScrimAlpha_safeClampingAgainstSpringBounce() {
        // Normal ranges
        assertEquals(0f, SpringSheetPhysics.calculateScrimAlpha(0f), 0.001f)
        assertEquals(0.25f, SpringSheetPhysics.calculateScrimAlpha(1f), 0.001f)
        assertEquals(0.125f, SpringSheetPhysics.calculateScrimAlpha(0.5f), 0.001f)

        // Spring undershoot (negative values) MUST be clamped to 0f to prevent Color constructor crash
        val negativeAlpha = SpringSheetPhysics.calculateScrimAlpha(-0.08f)
        assertEquals(0f, negativeAlpha, 0.0001f)
        assertTrue("Alpha must never be negative", negativeAlpha >= 0f)

        // Spring overshoot (> 1f) MUST be clamped to maxAlpha (0.25f)
        val overshootAlpha = SpringSheetPhysics.calculateScrimAlpha(1.18f)
        assertEquals(0.25f, overshootAlpha, 0.0001f)
        assertTrue("Alpha must never exceed maxAlpha", overshootAlpha <= 0.25f)
    }

    @Test
    fun testCalculateInitialVelocity_momentumHandoff() {
        val totalDistance = 1000f

        // Upward flick (-1200 px/s) becomes positive velocity in progress space (+1.2/s)
        val upwardInitialVelocity = SpringSheetPhysics.calculateInitialVelocity(-1200f, totalDistance)
        assertEquals(1.2f, upwardInitialVelocity, 0.001f)

        // Downward flick (+1200 px/s) becomes negative velocity in progress space (-1.2/s)
        val downwardInitialVelocity = SpringSheetPhysics.calculateInitialVelocity(1200f, totalDistance)
        assertEquals(-1.2f, downwardInitialVelocity, 0.001f)

        // Zero distance does not divide by zero
        assertEquals(0f, SpringSheetPhysics.calculateInitialVelocity(500f, 0f), 0.001f)

        // Extreme flings are clamped safely
        val extremeVelocity = SpringSheetPhysics.calculateInitialVelocity(-50000f, totalDistance)
        assertEquals(15f, extremeVelocity, 0.001f)
    }

    @Test
    fun testThresholdConstants() {
        assertEquals(400f, SpringSheetPhysics.FLICK_VELOCITY_THRESHOLD, 0.001f)
        assertEquals(0.5f, SpringSheetPhysics.EXPAND_PROGRESS_THRESHOLD, 0.001f)
        assertEquals(0.2f, SpringSheetPhysics.CONTENT_EXPAND_THRESHOLD, 0.001f)
        assertEquals(0.05f, SpringSheetPhysics.BACK_HANDLER_THRESHOLD, 0.001f)
        assertEquals(0.25f, SpringSheetPhysics.DEFAULT_MAX_SCRIM_ALPHA, 0.001f)
        assertEquals(64f, SpringSheetPhysics.DOCKED_TODAY_HEIGHT_DP, 0.001f)
        assertEquals(295f, SpringSheetPhysics.RESTING_UPCOMING_HEIGHT_DP, 0.001f)
        assertEquals(180f, SpringSheetPhysics.EMPTY_RESTING_UPCOMING_HEIGHT_DP, 0.001f)
        assertEquals(64f, SpringSheetPhysics.DOCKED_CONTAINER_HEIGHT_DP, 0.001f)
        assertEquals(10f, SpringSheetPhysics.CONTAINER_GAP_DP, 0.001f)
    }
}
