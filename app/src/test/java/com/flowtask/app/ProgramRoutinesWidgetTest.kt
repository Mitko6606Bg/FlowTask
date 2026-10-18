package com.flowtask.app

import com.flowtask.app.data.local.model.LayerProgram
import com.flowtask.app.data.local.model.ScheduleItem
import com.flowtask.app.widget.ProgramRoutinesWidgetProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ProgramRoutinesWidgetTest {

    @Test
    fun testEmptyProgramHasNoRoutines() {
        val program = LayerProgram(
            id = "prog_1",
            layerName = "Empty",
            scheduleItems = emptyList()
        )
        val routines = ProgramRoutinesWidgetProvider.findNextRoutines(program)
        assertTrue(routines.isEmpty())
    }

    @Test
    fun testCapsAtMaxCount3() {
        // Program with 5 routines on different days
        val items = listOf(
            ScheduleItem(dayOfWeek = 1, subject = "Mon 9", startMinutes = 540),
            ScheduleItem(dayOfWeek = 2, subject = "Tue 9", startMinutes = 540),
            ScheduleItem(dayOfWeek = 3, subject = "Wed 9", startMinutes = 540),
            ScheduleItem(dayOfWeek = 4, subject = "Thu 9", startMinutes = 540),
            ScheduleItem(dayOfWeek = 5, subject = "Fri 9", startMinutes = 540)
        )
        val program = LayerProgram(
            id = "prog_2",
            layerName = "Full Week",
            scheduleItems = items
        )
        val routines = ProgramRoutinesWidgetProvider.findNextRoutines(program, maxCount = 3)
        assertEquals(3, routines.size)
    }

    @Test
    fun testOngoingRoutineDetection() {
        // Fixed date: Monday 09:30
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 5, 9, 30, 0) // Monday
            set(Calendar.MILLISECOND, 0)
        }
        val nowMillis = cal.timeInMillis

        val items = listOf(
            ScheduleItem(
                dayOfWeek = 1, // Monday
                subject = "Ongoing Math",
                startMinutes = 540, // 09:00
                endMinutes = 630    // 10:30
            ),
            ScheduleItem(
                dayOfWeek = 1, // Monday
                subject = "Upcoming Physics",
                startMinutes = 660, // 11:00
                endMinutes = 750    // 12:30
            ),
            ScheduleItem(
                dayOfWeek = 2, // Tuesday
                subject = "Tuesday Bio",
                startMinutes = 600, // 10:00
                endMinutes = 700    // 11:40
            )
        )
        val program = LayerProgram(
            id = "prog_3",
            layerName = "University",
            scheduleItems = items
        )

        val nextRoutines = ProgramRoutinesWidgetProvider.findNextRoutines(program, nowMillis = nowMillis, maxCount = 3)
        assertEquals(3, nextRoutines.size)

        // First item should be ongoing Math
        assertEquals("Ongoing Math", nextRoutines[0].item.subject)
        assertEquals(0, nextRoutines[0].dayOffset)
        assertTrue(nextRoutines[0].isOngoing)

        // Second item should be upcoming Physics today
        assertEquals("Upcoming Physics", nextRoutines[1].item.subject)
        assertEquals(0, nextRoutines[1].dayOffset)
        assertFalse(nextRoutines[1].isOngoing)

        // Third item should be Tuesday Bio tomorrow
        assertEquals("Tuesday Bio", nextRoutines[2].item.subject)
        assertEquals(1, nextRoutines[2].dayOffset)
        assertFalse(nextRoutines[2].isOngoing)
    }

    @Test
    fun testPastRoutinesTodayAreSkipped() {
        // Fixed date: Monday 15:00
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 5, 15, 0, 0) // Monday 15:00
            set(Calendar.MILLISECOND, 0)
        }
        val nowMillis = cal.timeInMillis

        val items = listOf(
            ScheduleItem(
                dayOfWeek = 1, // Monday
                subject = "Morning Finished",
                startMinutes = 540, // 09:00
                endMinutes = 630    // 10:30 (already ended before 15:00)
            ),
            ScheduleItem(
                dayOfWeek = 1, // Monday
                subject = "Afternoon Finished",
                startMinutes = 780, // 13:00
                endMinutes = 840    // 14:00 (already ended before 15:00)
            ),
            ScheduleItem(
                dayOfWeek = 2, // Tuesday
                subject = "Tuesday Tomorrow",
                startMinutes = 600
            )
        )
        val program = LayerProgram(
            id = "prog_4",
            layerName = "Work",
            scheduleItems = items
        )

        val nextRoutines = ProgramRoutinesWidgetProvider.findNextRoutines(program, nowMillis = nowMillis, maxCount = 3)
        // Today's finished routines must be skipped; next upcoming routines are Tuesday Tomorrow, then next Monday's routines
        assertEquals(3, nextRoutines.size)
        assertEquals("Tuesday Tomorrow", nextRoutines[0].item.subject)
        assertEquals(1, nextRoutines[0].dayOffset)
        assertEquals("Morning Finished", nextRoutines[1].item.subject)
        assertEquals(7, nextRoutines[1].dayOffset)
        assertEquals("Afternoon Finished", nextRoutines[2].item.subject)
        assertEquals(7, nextRoutines[2].dayOffset)
    }

    @Test
    fun testSingleWeeklyRoutineRepeatsAcrossWeeks() {
        // Only 1 routine on Friday
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 5, 10, 0, 0) // Monday Oct 5
            set(Calendar.MILLISECOND, 0)
        }
        val nowMillis = cal.timeInMillis

        val items = listOf(
            ScheduleItem(
                dayOfWeek = 5, // Friday
                subject = "Weekly Friday Seminar",
                startMinutes = 600 // 10:00
            )
        )
        val program = LayerProgram(
            id = "prog_5",
            layerName = "Seminars",
            scheduleItems = items
        )

        val nextRoutines = ProgramRoutinesWidgetProvider.findNextRoutines(program, nowMillis = nowMillis, maxCount = 3)
        // In 21 days window, should find Friday week 1, Friday week 2, Friday week 3
        assertEquals(3, nextRoutines.size)
        assertEquals(4, nextRoutines[0].dayOffset) // Monday -> Friday is 4 days offset
        assertEquals(11, nextRoutines[1].dayOffset) // +7 days
        assertEquals(18, nextRoutines[2].dayOffset) // +14 days
    }

    @Test
    fun testDayCoverageEmpty() {
        val program = LayerProgram(
            id = "prog_empty",
            layerName = "Empty",
            scheduleItems = emptyList()
        )
        val coverage = ProgramRoutinesWidgetProvider.calculateDayCoverage(program)
        assertEquals(0, coverage.routineCount)
        assertEquals(0, coverage.totalMinutes)
        assertEquals(0, coverage.coveragePercentage)
        assertTrue(coverage.dots.isEmpty())
    }

    @Test
    fun testDayCoverageOngoingAndUpcoming() {
        // Monday 10:00 AM
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 5, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val items = listOf(
            ScheduleItem(
                dayOfWeek = 1, // Monday
                subject = "Past Routine",
                startMinutes = 480, // 08:00
                endMinutes = 540    // 09:00
            ),
            ScheduleItem(
                dayOfWeek = 1, // Monday
                subject = "Ongoing Routine",
                startMinutes = 570, // 09:30
                endMinutes = 660    // 11:00
            ),
            ScheduleItem(
                dayOfWeek = 1, // Monday
                subject = "Upcoming Routine",
                startMinutes = 720, // 12:00
                endMinutes = 810    // 13:30
            )
        )
        val program = LayerProgram(
            id = "prog_cov",
            layerName = "Coverage",
            scheduleItems = items
        )
        val coverage = ProgramRoutinesWidgetProvider.calculateDayCoverage(program, cal.timeInMillis)
        assertEquals(3, coverage.routineCount)
        assertEquals(1, coverage.completedCount)
        assertEquals(1, coverage.activeCount)
        assertEquals(1, coverage.upcomingCount)
        // Total duration: 60m + 90m + 90m = 240m (4h)
        assertEquals(240, coverage.totalMinutes)
        assertEquals("4h", coverage.formattedDuration)
        // 240 / 840 = ~28%
        assertEquals(28, coverage.coveragePercentage)
        assertEquals(3, coverage.dots.size)
        assertEquals(ProgramRoutinesWidgetProvider.DotState.PAST, coverage.dots[0])
        assertEquals(ProgramRoutinesWidgetProvider.DotState.ACTIVE, coverage.dots[1])
        assertEquals(ProgramRoutinesWidgetProvider.DotState.FUTURE, coverage.dots[2])
    }

    @Test
    fun testDayCoverageAllCompleted() {
        // Monday 20:00 PM
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 5, 20, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val items = listOf(
            ScheduleItem(
                dayOfWeek = 1, // Monday
                subject = "Morning",
                startMinutes = 540, // 09:00
                endMinutes = 600    // 10:00
            ),
            ScheduleItem(
                dayOfWeek = 1, // Monday
                subject = "Afternoon",
                startMinutes = 780, // 13:00
                endMinutes = 840    // 14:00
            )
        )
        val program = LayerProgram(
            id = "prog_done",
            layerName = "Done",
            scheduleItems = items
        )
        val coverage = ProgramRoutinesWidgetProvider.calculateDayCoverage(program, cal.timeInMillis)
        assertEquals(2, coverage.routineCount)
        assertEquals(2, coverage.completedCount)
        assertEquals(0, coverage.activeCount)
        assertEquals(0, coverage.upcomingCount)
        assertEquals(2, coverage.dots.size)
        assertEquals(ProgramRoutinesWidgetProvider.DotState.PAST, coverage.dots[0])
        assertEquals(ProgramRoutinesWidgetProvider.DotState.PAST, coverage.dots[1])
    }
}
