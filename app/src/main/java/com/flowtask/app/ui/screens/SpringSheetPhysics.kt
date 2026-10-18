package com.flowtask.app.ui.screens

object SpringSheetPhysics {
    const val FLICK_VELOCITY_THRESHOLD = 400f
    const val EXPAND_PROGRESS_THRESHOLD = 0.5f
    const val CONTENT_EXPAND_THRESHOLD = 0.2f
    const val BACK_HANDLER_THRESHOLD = 0.05f
    const val DEFAULT_MAX_SCRIM_ALPHA = 0.25f
    const val DOCKED_TODAY_HEIGHT_DP = 64f
    const val RESTING_UPCOMING_HEIGHT_DP = 295f
    const val EMPTY_RESTING_UPCOMING_HEIGHT_DP = 180f
    const val DOCKED_CONTAINER_HEIGHT_DP = 64f
    const val CONTAINER_GAP_DP = 10f

    /**
     * Determines whether the sheet should snap to expanded (1f) or collapsed (0f)
     * based on release velocity and current progress.
     */
    fun calculateTargetProgress(velocityY: Float, currentProgress: Float): Float = when {
        velocityY < -FLICK_VELOCITY_THRESHOLD -> 1f // Fast flick up -> expand
        velocityY > FLICK_VELOCITY_THRESHOLD -> 0f  // Fast flick down -> collapse
        currentProgress >= EXPAND_PROGRESS_THRESHOLD -> 1f
        else -> 0f
    }

    /**
     * Calculates the alpha of the scrim backdrop, safely clamped to [0f, maxAlpha]
     * to prevent IllegalArgumentException during spring overshoot/undershoot.
     */
    fun calculateScrimAlpha(progress: Float, maxAlpha: Float = DEFAULT_MAX_SCRIM_ALPHA): Float {
        return (maxAlpha * progress).coerceIn(0f, maxAlpha)
    }

    /**
     * Calculates the initial velocity to hand off from finger release to spring animation.
     */
    fun calculateInitialVelocity(velocityY: Float, totalDragDistancePx: Float): Float {
        return if (totalDragDistancePx > 0f) {
            (-velocityY / totalDragDistancePx).coerceIn(-15f, 15f)
        } else 0f
    }
}
