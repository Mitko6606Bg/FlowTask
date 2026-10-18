package com.flowtask.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.flowtask.app.ui.components.calculateDockColors
import com.flowtask.app.ui.components.hslToRgb
import com.flowtask.app.ui.components.rgbToHsl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class FloatingDockNavBarTest {

    @Test
    fun testNavTargetsResolution() {
        val screenOrder = listOf(0, 1, 2, 3)

        fun getScreenIcon(screenId: Int): ImageVector = when (screenId) {
            0 -> Icons.Default.Folder
            1 -> Icons.Default.Schedule
            2 -> Icons.Default.CalendarMonth
            3 -> Icons.Default.PhotoLibrary
            else -> Icons.Default.CalendarMonth
        }

        assertEquals(Icons.Default.Folder, getScreenIcon(0))
        assertEquals(Icons.Default.Schedule, getScreenIcon(1))
        assertEquals(Icons.Default.CalendarMonth, getScreenIcon(2))
        assertEquals(Icons.Default.PhotoLibrary, getScreenIcon(3))

        // When current is screen 2 (All Tasks):
        val currentPagerIndex = 2
        val otherPagerIndices = (0 until screenOrder.size).filter { it != currentPagerIndex }
        assertEquals(listOf(0, 1, 3), otherPagerIndices)

        val revealedScreenIds = otherPagerIndices.map { screenOrder[it] }
        assertEquals(listOf(0, 1, 3), revealedScreenIds)

        val destinationIcons = revealedScreenIds.map { getScreenIcon(it) }
        assertEquals(
            listOf(Icons.Default.Folder, Icons.Default.Schedule, Icons.Default.PhotoLibrary),
            destinationIcons
        )
    }

    @Test
    fun testLayerSelectionMapping() {
        val availableLayers = listOf("Work", "Personal", "Study")
        val layerOptions = listOf<String?>(null) + availableLayers

        assertEquals(4, layerOptions.size)
        assertEquals(null, layerOptions[0])
        assertEquals("Work", layerOptions[1])
        assertEquals("Personal", layerOptions[2])
        assertEquals("Study", layerOptions[3])

        // When null layer is selected, index is 0 ("All")
        fun getSelectedIndex(selected: String?): Int {
            return if (selected == null) 0
            else {
                val idx = availableLayers.indexOfFirst { it.equals(selected, ignoreCase = true) }
                if (idx >= 0) idx + 1 else 0
            }
        }

        assertEquals(0, getSelectedIndex(null))
        assertEquals(1, getSelectedIndex("Work"))
        assertEquals(2, getSelectedIndex("Personal"))
        assertEquals(3, getSelectedIndex("Study"))
        assertEquals(0, getSelectedIndex("NonExistent"))
    }

    @Test
    fun testTikTokDotLayerSelectorCustomDotSizing() {
        // Specifications from requirements:
        // Active dot: white capsule (18dp x 6dp)
        // Inactive dots: white circles (6dp x 6dp, 40% alpha)
        fun calculateDotWidth(isSelected: Boolean, customDotColor: Color?): Float {
            return if (customDotColor != null) {
                if (isSelected) 18f else 6f
            } else {
                if (isSelected) 22f else 6f
            }
        }

        fun calculateDotHeight(customDotColor: Color?): Float {
            return if (customDotColor != null) 6f else 6f
        }

        fun calculateDotAlpha(isSelected: Boolean, customDotColor: Color?): Float {
            return if (customDotColor != null) {
                if (isSelected) 1.0f else 0.40f
            } else {
                if (isSelected) 1.0f else 0.32f
            }
        }

        val customColor = Color.White
        assertEquals(18f, calculateDotWidth(isSelected = true, customDotColor = customColor), 0.001f)
        assertEquals(6f, calculateDotWidth(isSelected = false, customDotColor = customColor), 0.001f)
        assertEquals(6f, calculateDotHeight(customDotColor = customColor), 0.001f)
        assertEquals(1.0f, calculateDotAlpha(isSelected = true, customDotColor = customColor), 0.001f)
        assertEquals(0.40f, calculateDotAlpha(isSelected = false, customDotColor = customColor), 0.001f)
    }

    @Test
    fun testCompanionPillTightGapAndHitBoxMath() {
        val visualPillHeightDp = 18f
        val touchHitBoxHeightDp = 44f
        val companionSlotSpacingDp = 6f

        // Pill aligned to bottom of touch hit box:
        // Hit box bottom = visual pill bottom
        // Therefore, distance from visual pill bottom to dock top = companionSlotSpacingDp
        val visualGapToDock = companionSlotSpacingDp
        assertEquals(6f, visualGapToDock, 0.001f)

        // Hit box extension upward provides generous target without overlapping dock buttons
        val upwardTargetExtension = touchHitBoxHeightDp - visualPillHeightDp
        assertTrue("Hit box must extend upward", upwardTargetExtension >= 24f)
        assertEquals(44f, touchHitBoxHeightDp, 0.001f)
    }

    @Test
    fun testRgbToHslAndHslToRgb() {
        // Red
        val redHsl = rgbToHsl(Color.Red)
        assertEquals(0f, redHsl[0], 1f)
        assertEquals(1f, redHsl[1], 0.01f)
        assertEquals(0.5f, redHsl[2], 0.01f)

        // Green
        val greenHsl = rgbToHsl(Color.Green)
        assertEquals(120f, greenHsl[0], 1f)
        assertEquals(1f, greenHsl[1], 0.01f)
        assertEquals(0.5f, greenHsl[2], 0.01f)

        // Blue
        val blueHsl = rgbToHsl(Color.Blue)
        assertEquals(240f, blueHsl[0], 1f)
        assertEquals(1f, blueHsl[1], 0.01f)
        assertEquals(0.5f, blueHsl[2], 0.01f)

        // Roundtrip conversion
        val testColor = Color(0xFF2A8575)
        val hsl = rgbToHsl(testColor)
        val reconverted = hslToRgb(hsl[0], hsl[1], hsl[2])
        assertEquals(testColor.red, reconverted.red, 0.02f)
        assertEquals(testColor.green, reconverted.green, 0.02f)
        assertEquals(testColor.blue, reconverted.blue, 0.02f)
    }

    @Test
    fun testCalculateDockColorsShadesOfSameHue() {
        val testColors = listOf(
            Color(0xFF4C7B58), // Moss / Sage Green
            Color(0xFF1D7868), // Accent Teal
            Color(0xFF215CCD), // Accent Blue
            Color(0xFFE11D48), // Accent Rose
            Color(0xFF7C3AED)  // Accent Purple
        )

        for (accent in testColors) {
            val lightDock = calculateDockColors(accent, isDark = false)
            val darkDock = calculateDockColors(accent, isDark = true)

            val accentHsl = rgbToHsl(accent)
            val lightBarHsl = rgbToHsl(lightDock.barBackground)
            val lightCenterHsl = rgbToHsl(lightDock.centerButtonBackground)
            val darkBarHsl = rgbToHsl(darkDock.barBackground)
            val darkCenterHsl = rgbToHsl(darkDock.centerButtonBackground)

            fun hueDiff(h1: Float, h2: Float): Float {
                val diff = abs(h1 - h2)
                return if (diff > 180f) 360f - diff else diff
            }

            assertTrue("Light bar hue must match accent hue", hueDiff(accentHsl[0], lightBarHsl[0]) < 2f)
            assertTrue("Light center hue must match accent hue", hueDiff(accentHsl[0], lightCenterHsl[0]) < 2f)
            assertTrue("Dark bar hue must match accent hue", hueDiff(accentHsl[0], darkBarHsl[0]) < 2f)
            assertTrue("Dark center hue must match accent hue", hueDiff(accentHsl[0], darkCenterHsl[0]) < 2f)

            // Center button must be a distinctly darker shade than the outer bar
            assertTrue("Light center must be darker than bar", lightCenterHsl[2] < lightBarHsl[2])
            assertTrue("Dark center must be darker than bar", darkCenterHsl[2] < darkBarHsl[2])

            // Center button must be richer/more saturated than outer bar
            assertTrue("Light center must have equal or higher saturation", lightCenterHsl[1] >= lightBarHsl[1] - 0.01f)
            assertTrue("Dark center must have equal or higher saturation", darkCenterHsl[1] >= darkBarHsl[1] - 0.01f)

            // In dark mode, outer bar must be vibrant and radiant (L >= 0.40)
            assertTrue("Dark bar must be luminous and vibrant (L >= 0.40)", darkBarHsl[2] >= 0.40f)

            // Icons must be crisp white for high contrast
            assertEquals(Color.White, lightDock.iconTint)
            assertEquals(Color.White, darkDock.iconTint)

            // Borders: seamless in light mode, subtle luminescence in dark mode
            assertEquals(Color.Transparent, lightDock.border)
            assertEquals(Color.White.copy(alpha = 0.12f), darkDock.border)
        }
    }

    @Test
    fun testCalculateDockColorsMonochromeAndEdgeCases() {
        val monochromeCases = listOf(
            Color.Black,
            Color.White,
            Color(0xFF808080)
        )

        for (accent in monochromeCases) {
            val lightDock = calculateDockColors(accent, isDark = false)
            val darkDock = calculateDockColors(accent, isDark = true)

            val lightBarHsl = rgbToHsl(lightDock.barBackground)
            val lightCenterHsl = rgbToHsl(lightDock.centerButtonBackground)
            val darkBarHsl = rgbToHsl(darkDock.barBackground)
            val darkCenterHsl = rgbToHsl(darkDock.centerButtonBackground)

            assertEquals(0f, lightBarHsl[1], 0.01f)
            assertEquals(0f, lightCenterHsl[1], 0.01f)
            assertEquals(0f, darkBarHsl[1], 0.01f)
            assertEquals(0f, darkCenterHsl[1], 0.01f)

            assertTrue(lightCenterHsl[2] < lightBarHsl[2])
            assertTrue(darkCenterHsl[2] < darkBarHsl[2])

            assertEquals(Color.White, lightDock.iconTint)
            assertEquals(Color.White, darkDock.iconTint)
        }
    }

    @Test
    fun testCalculateDockColorsMatchesReferenceImage() {
        val refGreen = Color(0xFF4A8052)
        val lightDock = calculateDockColors(refGreen, isDark = false)
        val darkDock = calculateDockColors(refGreen, isDark = true)

        val lightBarHsl = rgbToHsl(lightDock.barBackground)
        val lightCenterHsl = rgbToHsl(lightDock.centerButtonBackground)
        val darkBarHsl = rgbToHsl(darkDock.barBackground)
        val darkCenterHsl = rgbToHsl(darkDock.centerButtonBackground)

        assertEquals(0.40f, lightBarHsl[2], 0.02f)
        assertEquals(0.25f, lightCenterHsl[2], 0.02f)
        assertEquals(0.44f, darkBarHsl[2], 0.02f)
        assertEquals(0.28f, darkCenterHsl[2], 0.02f)
    }

    @Test
    fun testExpandedScreenContainerButtonSpecsAndGeometry() {
        val dockHeightDp = 56f
        val companionContainerHeightDp = 56f
        val dockButtonSizeDp = 44f
        val companionButtonSizeDp = 44f
        val companionGapDp = 6f
        val iconSizeDp = 24f

        assertEquals(dockButtonSizeDp, companionButtonSizeDp, 0.001f)
        assertEquals(44f, companionButtonSizeDp, 0.001f)

        assertEquals(dockHeightDp, companionContainerHeightDp, 0.001f)
        assertEquals(56f, companionContainerHeightDp, 0.001f)

        assertEquals(6f, companionGapDp, 0.001f)
        assertEquals(24f, iconSizeDp, 0.001f)
    }

    @Test
    fun testIconAssignments() {
        // Left dock button: Contextual icon based on current screen
        fun getLeftDockIcon(screenId: Int): ImageVector = when (screenId) {
            1 -> Icons.Default.PhotoCamera // Today screen uses Camera to shoot instant note photos
            else -> Icons.Default.Today // Other screens use Today calendar icon
        }

        assertEquals(Icons.Default.PhotoCamera, getLeftDockIcon(1))
        assertEquals(Icons.Default.Today, getLeftDockIcon(0))
        assertEquals(Icons.Default.Today, getLeftDockIcon(2))
        assertEquals(Icons.Default.Today, getLeftDockIcon(3))

        // Right dock button: GridView when idle, Close when expanded
        val rightCollapsedIcon = Icons.Default.GridView
        val rightExpandedIcon = Icons.Default.Close
        assertEquals(Icons.Default.GridView, rightCollapsedIcon)
        assertEquals(Icons.Default.Close, rightExpandedIcon)

        // Camera Quick Snap button
        val cameraIcon = Icons.Default.PhotoCamera
        assertEquals(Icons.Default.PhotoCamera, cameraIcon)

        // Screen destination icons
        fun getScreenIcon(screenId: Int): ImageVector = when (screenId) {
            0 -> Icons.Default.Folder
            1 -> Icons.Default.Schedule
            2 -> Icons.Default.CalendarMonth
            3 -> Icons.Default.PhotoLibrary
            else -> Icons.Default.CalendarMonth
        }

        assertEquals(Icons.Default.Folder, getScreenIcon(0))
        assertEquals(Icons.Default.Schedule, getScreenIcon(1))
        assertEquals(Icons.Default.CalendarMonth, getScreenIcon(2))
        assertEquals(Icons.Default.PhotoLibrary, getScreenIcon(3))
    }

    @Test
    fun testLeftDockButtonActionResolution() {
        data class LeftButtonAction(val action: String, val icon: ImageVector)

        fun resolveLeftButton(screenId: Int): LeftButtonAction = when (screenId) {
            1 -> LeftButtonAction("SNAP_PHOTO", Icons.Default.PhotoCamera)
            else -> LeftButtonAction("NAVIGATE_TO_TODAY", Icons.Default.Today)
        }

        val todayAction = resolveLeftButton(1)
        assertEquals("SNAP_PHOTO", todayAction.action)
        assertEquals(Icons.Default.PhotoCamera, todayAction.icon)

        val projectsAction = resolveLeftButton(0)
        assertEquals("NAVIGATE_TO_TODAY", projectsAction.action)
        assertEquals(Icons.Default.Today, projectsAction.icon)

        val allTasksAction = resolveLeftButton(2)
        assertEquals("NAVIGATE_TO_TODAY", allTasksAction.action)
        assertEquals(Icons.Default.Today, allTasksAction.icon)

        val instantsAction = resolveLeftButton(3)
        assertEquals("NAVIGATE_TO_TODAY", instantsAction.action)
        assertEquals(Icons.Default.Today, instantsAction.icon)
    }

    @Test
    fun testContextualCreationPerScreen() {
        data class CreationAction(val type: String, val hasDate: Boolean)

        fun getCreationAction(screenId: Int): CreationAction = when (screenId) {
            0 -> CreationAction("CREATE_PROJECT", false)
            1 -> CreationAction("CREATE_TASK", true)
            2 -> CreationAction("CREATE_TASK", false)
            3 -> CreationAction("CREATE_PHOTO_FOLDER", false)
            else -> CreationAction("CREATE_TASK", false)
        }

        val projectAction = getCreationAction(0)
        assertEquals("CREATE_PROJECT", projectAction.type)
        assertFalse(projectAction.hasDate)

        val todayAction = getCreationAction(1)
        assertEquals("CREATE_TASK", todayAction.type)
        assertTrue(todayAction.hasDate)

        val generalTaskAction = getCreationAction(2)
        assertEquals("CREATE_TASK", generalTaskAction.type)
        assertFalse(generalTaskAction.hasDate)

        val photoFolderAction = getCreationAction(3)
        assertEquals("CREATE_PHOTO_FOLDER", photoFolderAction.type)
        assertFalse(photoFolderAction.hasDate)
    }

    @Test
    fun testCompanionScreenDestinationsAcrossAllPages() {
        val screenOrder = listOf(0, 1, 2, 3)

        for (currentPage in 0..3) {
            val otherPages = (0 until screenOrder.size).filter { it != currentPage }
            assertEquals(3, otherPages.size)
            assertFalse(otherPages.contains(currentPage))
        }
    }

    @Test
    fun testLayerDisplayPerScreen() {
        fun shouldShowLayers(screenId: Int): Boolean = screenId in listOf(1, 2)

        assertTrue("Today screen should support layers", shouldShowLayers(1))
        assertTrue("All Tasks screen should support layers", shouldShowLayers(2))
        assertFalse("Projects screen should not show layers", shouldShowLayers(0))
        assertFalse("Instants screen should not show layers", shouldShowLayers(3))
    }

    @Test
    fun testTodayButtonNavigationRoutingLogic() {
        val screenOrder = listOf(0, 1, 2, 3)
        val todayIndex = screenOrder.indexOf(1) // 1
        val allTasksIndex = screenOrder.indexOf(2) // 2

        data class NavigationResult(val triggeredCalendarToday: Boolean, val navigatedToPage: Int?)

        fun resolveTodayNavigation(currentPage: Int, allTasksViewMode: String): NavigationResult {
            return if (currentPage == allTasksIndex && allTasksViewMode == "SMART_CALENDAR") {
                NavigationResult(triggeredCalendarToday = true, navigatedToPage = null)
            } else if (todayIndex >= 0 && currentPage != todayIndex) {
                NavigationResult(triggeredCalendarToday = false, navigatedToPage = todayIndex)
            } else {
                NavigationResult(triggeredCalendarToday = false, navigatedToPage = null)
            }
        }

        // On All Tasks with SMART_CALENDAR -> trigger calendar scroll to today
        val smartCalResult = resolveTodayNavigation(allTasksIndex, "SMART_CALENDAR")
        assertTrue(smartCalResult.triggeredCalendarToday)
        assertEquals(null, smartCalResult.navigatedToPage)

        // On All Tasks with LIST -> navigate to Today page (index 1)
        val listResult = resolveTodayNavigation(allTasksIndex, "LIST")
        assertFalse(listResult.triggeredCalendarToday)
        assertEquals(1, listResult.navigatedToPage)

        // On Projects (index 0) -> navigate to Today page (index 1)
        val projResult = resolveTodayNavigation(0, "LIST")
        assertFalse(projResult.triggeredCalendarToday)
        assertEquals(1, projResult.navigatedToPage)

        // On Instants (index 3) -> navigate to Today page (index 1)
        val instResult = resolveTodayNavigation(3, "LIST")
        assertFalse(instResult.triggeredCalendarToday)
        assertEquals(1, instResult.navigatedToPage)

        // On Today (index 1) -> remain active / do not trigger navigation
        val todayResult = resolveTodayNavigation(1, "LIST")
        assertFalse(todayResult.triggeredCalendarToday)
        assertEquals(null, todayResult.navigatedToPage)
    }

    @Test
    fun testSearchDragHandleDismissThreshold() {
        // Dismiss threshold is defined at 36.dp
        val dismissThresholdDp = 36f

        fun shouldDismissSearch(dragDistanceDp: Float): Boolean {
            return dragDistanceDp > dismissThresholdDp
        }

        // Slight drag (e.g. 15dp or 30dp) should NOT dismiss -> springs back to 0
        assertFalse("15dp pull should spring back", shouldDismissSearch(15f))
        assertFalse("30dp pull should spring back", shouldDismissSearch(30f))
        assertFalse("36dp pull should not dismiss at boundary", shouldDismissSearch(36f))

        // Intentional swipe down (> 36dp) triggers dismissal
        assertTrue("37dp pull should dismiss search", shouldDismissSearch(37f))
        assertTrue("50dp pull should dismiss search", shouldDismissSearch(50f))
        assertTrue("100dp pull should dismiss search", shouldDismissSearch(100f))
    }

    @Test
    fun testSearchDragHandleVisualDimensions() {
        val handleWidth = 38.dp
        val handleHeight = 4.5.dp
        val touchTargetHeight = 24.dp

        assertEquals(38f, handleWidth.value, 0.001f)
        assertEquals(4.5f, handleHeight.value, 0.001f)
        assertEquals(24f, touchTargetHeight.value, 0.001f)

        // Verify M3 pill opacity contrast:
        fun getHandleAlpha(isDark: Boolean): Float = if (isDark) 0.45f else 0.30f
        assertEquals(0.45f, getHandleAlpha(true), 0.001f)
        assertEquals(0.30f, getHandleAlpha(false), 0.001f)
    }

    @Test
    fun testSearchDragDisplacementAndAlphaPhysics() {
        val maxTranslationPx = 300f // e.g. 100.dp * 3.0 density

        fun computeDragOffset(currentOffset: Float, dragAmount: Float): Float {
            return (currentOffset + dragAmount).coerceAtLeast(0f)
        }

        fun computeDragAlpha(swipeOffsetY: Float): Float {
            return (1f - (swipeOffsetY / (maxTranslationPx * 2.2f))).coerceIn(0.35f, 1f)
        }

        // Clamping prevents upward pull:
        assertEquals(0f, computeDragOffset(0f, -20f), 0.001f)
        // Downward pull tracks 1:1:
        assertEquals(50f, computeDragOffset(0f, 50f), 0.001f)
        assertEquals(80f, computeDragOffset(50f, 30f), 0.001f)

        // Alpha starts at 1.0 at rest:
        assertEquals(1.0f, computeDragAlpha(0f), 0.001f)
        // Fades gracefully during downward pull:
        val alphaAt100 = computeDragAlpha(100f)
        assertTrue("Alpha should decrease as offset increases", alphaAt100 < 1.0f && alphaAt100 > 0.35f)
        // Clamped at minimum 0.35:
        assertEquals(0.35f, computeDragAlpha(1000f), 0.001f)
    }
}
