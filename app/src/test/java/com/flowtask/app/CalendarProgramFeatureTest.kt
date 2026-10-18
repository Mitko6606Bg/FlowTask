package com.flowtask.app

import com.flowtask.app.data.local.entity.TaskEntity
import com.flowtask.app.data.local.model.LayerProgram
import com.flowtask.app.data.local.model.ScheduleItem
import com.flowtask.app.data.local.model.TaskWithSubTasks
import com.flowtask.app.util.DateTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CalendarProgramFeatureTest {

    private fun createTestProgram(
        id: String,
        name: String,
        isEnabled: Boolean,
        startMillis: Long?,
        endMillis: Long?,
        items: List<ScheduleItem>
    ): LayerProgram {
        return LayerProgram(
            id = id,
            layerName = name,
            isEnabled = isEnabled,
            startDateMillis = startMillis,
            endDateMillis = endMillis,
            scheduleItems = items
        )
    }

    private fun filterActivePrograms(
        layerPrograms: List<LayerProgram>,
        selectedProgramId: String?
    ): List<LayerProgram> {
        return when (selectedProgramId) {
            "NONE" -> emptyList()
            null -> {
                val firstEnabled = layerPrograms.firstOrNull { it.isEnabled } ?: layerPrograms.firstOrNull()
                if (firstEnabled != null) listOf(firstEnabled) else emptyList()
            }
            else -> layerPrograms.filter { it.id == selectedProgramId && it.isEnabled }
        }
    }

    private fun calculateActivityDates(
        tasks: List<TaskEntity>,
        activePrograms: List<LayerProgram>
    ): Set<Long> {
        val set = mutableSetOf<Long>()
        // 1. Regular tasks
        for (item in tasks) {
            val s = item.startDateMillis?.let { DateTimeUtils.getStartOfDay(it) }
            val e = item.dueDateMillis?.let { DateTimeUtils.getStartOfDay(it) }
            if (s != null && e != null && e >= s) {
                val cal = Calendar.getInstance().apply { timeInMillis = s }
                while (cal.timeInMillis <= e) {
                    set.add(cal.timeInMillis)
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
            } else if (e != null) {
                set.add(e)
            } else if (s != null) {
                set.add(s)
            }
        }
        // 2. Program routine occurrences for active programs strictly on their scheduled days
        for (prog in activePrograms) {
            if (!prog.isEnabled) continue
            val progItems = prog.scheduleItems
            if (progItems.isEmpty()) continue
            val activeDaysOfWeek = progItems.map { it.dayOfWeek }.toSet()

            val today = DateTimeUtils.getStartOfDay()
            val startBound = prog.startDateMillis?.let { DateTimeUtils.getStartOfDay(it) }
                ?: (today - 90L * 86400000L)
            val endBound = prog.endDateMillis?.let { DateTimeUtils.getEndOfDay(it) }
                ?: (today + 270L * 86400000L)

            val cal = Calendar.getInstance().apply { timeInMillis = startBound }
            while (cal.timeInMillis <= endBound) {
                val d = cal.get(Calendar.DAY_OF_WEEK)
                val dow = if (d == Calendar.SUNDAY) 7 else d - 1 // 1..7 (Mon..Sun)
                if (dow in activeDaysOfWeek) {
                    set.add(cal.timeInMillis)
                }
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return set
    }

    private fun calculatePeriodDates(
        tasks: List<TaskEntity>
    ): Set<Long> {
        val set = mutableSetOf<Long>()
        // Multi-day regular tasks ONLY:
        for (item in tasks) {
            val s = item.startDateMillis?.let { DateTimeUtils.getStartOfDay(it) }
            val e = item.dueDateMillis?.let { DateTimeUtils.getStartOfDay(it) }
            if (s != null && e != null && e > s) {
                val cal = Calendar.getInstance().apply { timeInMillis = s }
                while (cal.timeInMillis <= e) {
                    set.add(cal.timeInMillis)
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }
        return set
    }

    @Test
    fun testProgramFilterSelection_noAllProgramsPill() {
        val prog1 = createTestProgram("p1", "Work", true, null, null, emptyList())
        val prog2 = createTestProgram("p2", "Personal", true, null, null, emptyList())
        val prog3 = createTestProgram("p3", "Archived", false, null, null, emptyList())
        val all = listOf(prog1, prog2, prog3)

        // Case 1: Initial (null) -> Resolves to first enabled program (p1), NOT All
        val initialSelected = filterActivePrograms(all, null)
        assertEquals(1, initialSelected.size)
        assertEquals("p1", initialSelected.first().id)

        // Case 2: NONE -> No programs (switch turned off / none button clicked)
        val noneSelected = filterActivePrograms(all, "NONE")
        assertTrue(noneSelected.isEmpty())

        // Case 3: Specific program id
        val p2Selected = filterActivePrograms(all, "p2")
        assertEquals(1, p2Selected.size)
        assertEquals("p2", p2Selected.first().id)

        // Case 4: Disabled program selected
        val p3Selected = filterActivePrograms(all, "p3")
        assertTrue(p3Selected.isEmpty()) // p3 is not enabled
    }

    @Test
    fun testCalendarActivityProjection_routineDaysOnlyNotAllDays() {
        // Monday = 1 in 1..7 (Mon..Sun)
        val cal = Calendar.getInstance()
        cal.set(2025, Calendar.JANUARY, 6, 0, 0, 0) // Mon Jan 6, 2025
        cal.set(Calendar.MILLISECOND, 0)
        val monJan6 = cal.timeInMillis

        cal.set(2025, Calendar.JANUARY, 19, 23, 59, 59) // Sun Jan 19, 2025 (2 full weeks)
        val sunJan19 = cal.timeInMillis

        val routineItem = ScheduleItem(
            id = "item1",
            dayOfWeek = 1, // Every Monday
            subject = "Team Standup",
            startMinutes = 540 // 09:00
        )
        val workProgram = createTestProgram("p_work", "Work", true, monJan6, sunJan19, listOf(routineItem))

        // Regular task on Wednesday Jan 8, 2025
        cal.set(2025, Calendar.JANUARY, 8, 12, 0, 0)
        val wedJan8 = DateTimeUtils.getStartOfDay(cal.timeInMillis)
        val task = TaskEntity(
            id = 1L,
            explanation = "Doctor appointment",
            dueDateMillis = wedJan8
        )

        // 1. With active program
        val activeProg = filterActivePrograms(listOf(workProgram), "p_work")
        val activitySet = calculateActivityDates(listOf(task), activeProg)

        // Expect: Mon Jan 6 (routine), Wed Jan 8 (task), Mon Jan 13 (routine)
        assertTrue(activitySet.contains(monJan6))
        assertTrue(activitySet.contains(wedJan8))
        cal.set(2025, Calendar.JANUARY, 13, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val monJan13 = cal.timeInMillis
        assertTrue(activitySet.contains(monJan13))

        // IMPORTANT: Non-routine days (Tue Jan 7, Thu Jan 9, etc.) must NOT have activity
        cal.set(2025, Calendar.JANUARY, 7, 0, 0, 0)
        val tueJan7 = cal.timeInMillis
        assertFalse(activitySet.contains(tueJan7))

        cal.set(2025, Calendar.JANUARY, 9, 0, 0, 0)
        val thuJan9 = cal.timeInMillis
        assertFalse(activitySet.contains(thuJan9))

        // Beyond program end bound (Mon Jan 20) must NOT have activity
        cal.set(2025, Calendar.JANUARY, 20, 0, 0, 0)
        val monJan20 = cal.timeInMillis
        assertFalse(activitySet.contains(monJan20))

        // 2. With NONE filter (program switched off)
        val noneProg = filterActivePrograms(listOf(workProgram), "NONE")
        val activitySetNone = calculateActivityDates(listOf(task), noneProg)
        // Only regular task remains, no routines
        assertTrue(activitySetNone.contains(wedJan8))
        assertFalse(activitySetNone.contains(monJan6))
        assertFalse(activitySetNone.contains(monJan13))
    }

    @Test
    fun testPeriodDatesCalculation_programDurationNeverAdded() {
        val cal = Calendar.getInstance()
        cal.set(2025, Calendar.FEBRUARY, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startFeb1 = cal.timeInMillis

        cal.set(2025, Calendar.FEBRUARY, 15, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val endFeb15 = cal.timeInMillis

        // 1. A 3-day regular task from Feb 2 to Feb 4
        cal.set(2025, Calendar.FEBRUARY, 2, 0, 0, 0)
        val feb2 = cal.timeInMillis
        cal.set(2025, Calendar.FEBRUARY, 4, 0, 0, 0)
        val feb4 = cal.timeInMillis

        val multiDayTask = TaskEntity(
            id = 10L,
            explanation = "Team Offsite",
            startDateMillis = feb2,
            dueDateMillis = feb4
        )

        val periods = calculatePeriodDates(listOf(multiDayTask))

        // Multi-day task dates ARE in periodDates
        assertTrue(periods.contains(feb2))
        cal.set(2025, Calendar.FEBRUARY, 3, 0, 0, 0)
        val feb3 = cal.timeInMillis
        assertTrue(periods.contains(feb3))
        assertTrue(periods.contains(feb4))
        assertEquals(3, periods.size)

        // Program boundaries (Feb 1, Feb 5-15) must NOT be in periodDates
        assertFalse(periods.contains(startFeb1))
        cal.set(2025, Calendar.FEBRUARY, 5, 0, 0, 0)
        val feb5 = cal.timeInMillis
        assertFalse(periods.contains(feb5))
        assertFalse(periods.contains(endFeb15))
    }

    @Test
    fun testSwitchStateAndNoneToggleEquivalence() {
        val prog = createTestProgram("p1", "Work", true, null, null, emptyList())
        val programs = listOf(prog)

        // Initial: switch is ON, prog is active
        var selectedId: String? = "p1"
        var isSwitchChecked = selectedId != null && selectedId != "NONE"
        assertTrue(isSwitchChecked)
        assertEquals("p1", filterActivePrograms(programs, selectedId).first().id)

        // User flips switch OFF -> sets to "NONE"
        selectedId = "NONE"
        isSwitchChecked = selectedId != null && selectedId != "NONE"
        assertFalse(isSwitchChecked)
        assertTrue(filterActivePrograms(programs, selectedId).isEmpty())

        // User flips switch ON -> restores program
        selectedId = prog.id
        isSwitchChecked = selectedId != null && selectedId != "NONE"
        assertTrue(isSwitchChecked)
        assertEquals("p1", filterActivePrograms(programs, selectedId).first().id)

        // User flips switch OFF again -> sets to "NONE"
        selectedId = "NONE"
        isSwitchChecked = selectedId != null && selectedId != "NONE"
        assertFalse(isSwitchChecked)
        assertTrue(filterActivePrograms(programs, selectedId).isEmpty())
    }

    @Test
    fun testProgramNamesExcludedFromAvailableLayers() {
        val dbLayers = listOf("Personal", "Work", "University Semester", "Fitness")
        val programs = listOf(
            createTestProgram("p1", "University Semester", true, null, null, emptyList()),
            createTestProgram("p2", "WORK", true, null, null, emptyList())
        )
        val programNames = programs.map { it.layerName.trim().lowercase() }.toSet()
        val availableLayers = dbLayers.filter { it.trim().lowercase() !in programNames }

        assertEquals(listOf("Personal", "Fitness"), availableLayers)
        assertFalse(availableLayers.contains("University Semester"))
        assertFalse(availableLayers.contains("Work"))
    }

    @Test
    fun testCalendarTasksFiltering_whenProgramSwitchToggled() {
        val regularTask = TaskWithSubTasks(
            task = TaskEntity(id = 1, explanation = "Doctor appointment", layer = "Personal"),
            subTasks = emptyList()
        )
        val workProgramTask = TaskWithSubTasks(
            task = TaskEntity(id = 2, explanation = "Standup", layer = "Work Program"),
            subTasks = emptyList()
        )
        val uniProgramTask = TaskWithSubTasks(
            task = TaskEntity(id = 3, explanation = "Lecture", layer = "University"),
            subTasks = emptyList()
        )

        val allTasks = listOf(regularTask, workProgramTask, uniProgramTask)
        val allProgramLayerNames = setOf("work program", "university")

        // 1. When switch is OFF (selectedCalendarProgramId == "NONE")
        val activeProgramLayerNamesOff = emptySet<String>()
        val calendarTasksWhenOff = allTasks.filter { item ->
            val layer = item.task.layer?.trim()?.lowercase()
            layer == null || layer !in allProgramLayerNames || layer in activeProgramLayerNamesOff
        }
        assertEquals(1, calendarTasksWhenOff.size)
        assertEquals(1L, calendarTasksWhenOff.first().task.id) // Only doctor appointment

        // 2. When switch is ON and "Work Program" is selected
        val activeProgramLayerNamesWork = setOf("work program")
        val calendarTasksWhenWork = allTasks.filter { item ->
            val layer = item.task.layer?.trim()?.lowercase()
            layer == null || layer !in allProgramLayerNames || layer in activeProgramLayerNamesWork
        }
        assertEquals(2, calendarTasksWhenWork.size)
        assertTrue(calendarTasksWhenWork.any { it.task.id == 1L })
        assertTrue(calendarTasksWhenWork.any { it.task.id == 2L })
        assertFalse(calendarTasksWhenWork.any { it.task.id == 3L })
    }

    @Test
    fun testRoutineDeduplication_sheetBehavior() {
        val routineItem = ScheduleItem(
            id = "r1",
            dayOfWeek = 1,
            subject = "Algorithms",
            startMinutes = 600
        )

        val dayRoutines = listOf(Pair("University", routineItem))

        // A synthesized TaskEntity in SQLite from Mode A
        val generatedTask = TaskWithSubTasks(
            task = TaskEntity(
                id = 100L,
                explanation = "Algorithms",
                dueDateMillis = 1735689600000L,
                dueTimeMinutes = 600
            ),
            subTasks = emptyList()
        )

        // A distinct personal user task
        val userTask = TaskWithSubTasks(
            task = TaskEntity(
                id = 200L,
                explanation = "Buy groceries",
                dueDateMillis = 1735689600000L,
                dueTimeMinutes = 720
            ),
            subTasks = emptyList()
        )

        val allTasks = listOf(generatedTask, userTask)

        // Deduplication logic identical to CalendarDateTasksBottomSheet
        val routineSignatures = dayRoutines.map {
            "${it.second.title.trim().lowercase()}_${it.second.startMinutes}"
        }.toSet()

        val deduplicated = allTasks.filter { taskWithSubTasks ->
            val sig = "${taskWithSubTasks.task.explanation.trim().lowercase()}_${taskWithSubTasks.task.dueTimeMinutes ?: -1}"
            sig !in routineSignatures
        }

        // Generated task is deduplicated out, personal user task remains
        assertEquals(1, deduplicated.size)
        assertEquals(200L, deduplicated.first().task.id)
        assertEquals("Buy groceries", deduplicated.first().task.explanation)
    }

    @Test
    fun testCollapsibleFiltersContainer_gestureAndExpansionStates() {
        var isExpanded = false
        val touchSlop = 16f

        fun handleDrag(dy: Float) {
            if (kotlin.math.abs(dy) > touchSlop) {
                if (!isExpanded && dy > touchSlop) {
                    isExpanded = true
                } else if (isExpanded && dy < -touchSlop) {
                    isExpanded = false
                }
            }
        }

        fun handleTap() {
            isExpanded = !isExpanded
        }

        // 1. Initially collapsed
        assertFalse(isExpanded)

        // 2. Drag down exceeds slop -> expands container
        handleDrag(32f)
        assertTrue(isExpanded)

        // 3. Small jitter below slop -> maintains expanded state
        handleDrag(-5f)
        assertTrue(isExpanded)

        // 4. Drag up exceeds slop -> collapses container
        handleDrag(-30f)
        assertFalse(isExpanded)

        // 5. Direct tap toggles state
        handleTap()
        assertTrue(isExpanded)
        handleTap()
        assertFalse(isExpanded)
    }

    @Test
    fun testFilterHandle_gestureDrainsPointerEvents() {
        var isClosed = false
        var eventsDrained = 0
        val touchSlop = 12f

        // Simulated event stream of a user swiping up with natural leftward thumb arc
        data class PointerMove(val dx: Float, val dy: Float, val isPressed: Boolean)
        val pointerEvents = listOf(
            PointerMove(dx = -2f, dy = -5f, isPressed = true),   // below slop
            PointerMove(dx = -6f, dy = -15f, isPressed = true),  // passes slop -> triggers close
            PointerMove(dx = -12f, dy = -28f, isPressed = true), // remaining finger follow-through
            PointerMove(dx = -18f, dy = -35f, isPressed = false) // finger lifted
        )

        var gestureCaptured = false
        for (event in pointerEvents) {
            if (!event.isPressed && !gestureCaptured) {
                isClosed = true
                break
            }
            if (event.dy < -touchSlop && !gestureCaptured) {
                gestureCaptured = true
                isClosed = true
            } else if (gestureCaptured) {
                eventsDrained++
            }
        }

        assertTrue(isClosed)
        assertEquals(2, eventsDrained)
    }

    @Test
    fun testProgram_defaultShowTasksEverywhereIsFalse() {
        val program = LayerProgram(layerName = "Университет")
        assertFalse("Programs must default to showTasksEverywhere = false (calendar only)", program.showTasksEverywhere)
    }

    @Test
    fun testProgram_taskGenerationOnlyWhenShowTasksEverywhereIsTrue() {
        val cal = Calendar.getInstance()
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 30)
        val end = cal.timeInMillis

        val items = listOf(
            ScheduleItem(dayOfWeek = 1, subject = "Математика", startMinutes = 540),
            ScheduleItem(dayOfWeek = 3, subject = "Физика", startMinutes = 600)
        )

        // Program with showTasksEverywhere = false (default)
        val calOnlyProgram = LayerProgram(
            layerName = "Курсове",
            isEnabled = true,
            startDateMillis = start,
            endDateMillis = end,
            scheduleItems = items,
            showTasksEverywhere = false
        )

        // Program with showTasksEverywhere = true
        val appWideProgram = calOnlyProgram.copy(showTasksEverywhere = true)

        val shouldGenerateTasksForCalOnly = calOnlyProgram.isEnabled && calOnlyProgram.showTasksEverywhere
        val shouldGenerateTasksForAppWide = appWideProgram.isEnabled && appWideProgram.showTasksEverywhere

        assertFalse(shouldGenerateTasksForCalOnly)
        assertTrue(shouldGenerateTasksForAppWide)
    }

    @Test
    fun testProgram_topWeekCalculation_coversMondayToSunday() {
        // Test month grid generation for top week:
        // Top week should always contain exactly 7 cells corresponding to Mon(1)..Sun(7)
        for (month in 0..11) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, 2026)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0
            val prevCal = (cal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
            val daysInPrevMonth = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)

            val topWeek = (0..6).map { col ->
                val dayOfWeek = col + 1
                val dayNum = if (col < firstDayOfWeek) {
                    daysInPrevMonth - (firstDayOfWeek - 1 - col)
                } else {
                    col - firstDayOfWeek + 1
                }
                Pair(dayNum, dayOfWeek)
            }

            assertEquals("Top week must have exactly 7 days", 7, topWeek.size)
            for (col in 0..6) {
                assertEquals(col + 1, topWeek[col].second)
                assertTrue("Day number must be valid", topWeek[col].first in 1..31)
            }
        }
    }
}
