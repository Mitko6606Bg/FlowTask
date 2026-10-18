package com.flowtask.app

import com.flowtask.app.data.local.model.LayerProgram
import com.flowtask.app.data.local.model.ScheduleItem
import com.flowtask.app.data.local.model.SemesterConfig
import com.flowtask.app.util.SemesterUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class SemesterUtilsTest {

    @Test
    fun testGetMondayOfWeek() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.FEBRUARY, 18, 14, 30, 0) // Wednesday Feb 18, 2026
            set(Calendar.MILLISECOND, 0)
        }
        val mondayMillis = SemesterUtils.getMondayOfWeek(cal.timeInMillis)
        val resultCal = Calendar.getInstance().apply { timeInMillis = mondayMillis }

        assertEquals(2026, resultCal.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, resultCal.get(Calendar.MONTH))
        assertEquals(16, resultCal.get(Calendar.DAY_OF_MONTH)) // Monday is Feb 16
        assertEquals(0, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCal.get(Calendar.MINUTE))
    }

    @Test
    fun testGetSundayOfWeek() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.FEBRUARY, 18, 14, 30, 0) // Wednesday Feb 18, 2026
        }
        val sundayMillis = SemesterUtils.getSundayOfWeek(cal.timeInMillis)
        val resultCal = Calendar.getInstance().apply { timeInMillis = sundayMillis }

        assertEquals(2026, resultCal.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, resultCal.get(Calendar.MONTH))
        assertEquals(22, resultCal.get(Calendar.DAY_OF_MONTH)) // Sunday is Feb 22
        assertEquals(23, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, resultCal.get(Calendar.MINUTE))
    }

    @Test
    fun testTotalWeeksAndWeekNumberCalculation() {
        // Semester from Feb 16, 2026 (Week 1 Monday) to May 31, 2026 (Sunday)
        val startCal = Calendar.getInstance().apply {
            set(2026, Calendar.FEBRUARY, 16, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(2026, Calendar.MAY, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val totalWeeks = SemesterUtils.getTotalWeeks(startCal.timeInMillis, endCal.timeInMillis)
        assertEquals(15, totalWeeks)

        // Week 1 date
        val week1Date = Calendar.getInstance().apply {
            set(2026, Calendar.FEBRUARY, 18, 10, 0, 0)
        }.timeInMillis
        assertEquals(1, SemesterUtils.getWeekNumber(week1Date, startCal.timeInMillis, endCal.timeInMillis))

        // Week 8 date (April 8, 2026 - exactly 7 weeks after Feb 18)
        val week8Date = Calendar.getInstance().apply {
            set(2026, Calendar.APRIL, 8, 12, 0, 0)
        }.timeInMillis
        assertEquals(8, SemesterUtils.getWeekNumber(week8Date, startCal.timeInMillis, endCal.timeInMillis))

        // Date before semester
        val beforeDate = Calendar.getInstance().apply {
            set(2026, Calendar.FEBRUARY, 10, 10, 0, 0)
        }.timeInMillis
        assertNull(SemesterUtils.getWeekNumber(beforeDate, startCal.timeInMillis, endCal.timeInMillis))

        // Date after semester
        val afterDate = Calendar.getInstance().apply {
            set(2026, Calendar.JUNE, 5, 10, 0, 0)
        }.timeInMillis
        assertNull(SemesterUtils.getWeekNumber(afterDate, startCal.timeInMillis, endCal.timeInMillis))
    }

    @Test
    fun testGenerateSemesterProgramTasks() {
        // 3-week mini semester
        val startCal = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 2, 0, 0, 0) // Monday March 2, 2026
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 22, 23, 59, 59) // Sunday March 22, 2026
            set(Calendar.MILLISECOND, 999)
        }

        val config = SemesterConfig(
            isEnabled = true,
            startDateMillis = startCal.timeInMillis,
            endDateMillis = endCal.timeInMillis,
            programLayerName = "Програма",
            scheduleItems = listOf(
                ScheduleItem(
                    dayOfWeek = 1, // Monday
                    subject = "Висша математика",
                    startMinutes = 540, // 09:00
                    roomOrNote = "Зала 101"
                ),
                ScheduleItem(
                    dayOfWeek = 3, // Wednesday
                    subject = "Обектно-ориентирано програмиране",
                    startMinutes = 660, // 11:00
                    roomOrNote = "Лаб 3"
                )
            )
        )

        val generatedTasks = SemesterUtils.generateSemesterProgramTasks(config)
        // 3 weeks * 2 classes per week = 6 tasks
        assertEquals(6, generatedTasks.size)

        for (task in generatedTasks) {
            assertEquals("Програма", task.layer)
            assertNotNull(task.dueDateMillis)
            assertTrue(task.dueTimeMinutes == 540 || task.dueTimeMinutes == 660)
            assertTrue(task.explanation.contains("Зала 101") || task.explanation.contains("Лаб 3"))
        }
    }

    @Test
    fun testGenerateProgramTasksForMultiLayer() {
        val startCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 5, 0, 0, 0) // Monday Oct 5, 2026
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 18, 23, 59, 59) // Sunday Oct 18, 2026 (2 weeks)
            set(Calendar.MILLISECOND, 999)
        }

        val program = LayerProgram(
            layerName = "Работа",
            startDateMillis = startCal.timeInMillis,
            endDateMillis = endCal.timeInMillis,
            scheduleItems = listOf(
                ScheduleItem(
                    dayOfWeek = 1, // Monday
                    subject = "Daily Standup",
                    startMinutes = 600, // 10:00 AM
                    roomOrNote = "Zoom"
                )
            )
        )

        val tasks = SemesterUtils.generateProgramTasks(program)
        // 2 weeks * 1 Monday = 2 tasks
        assertEquals(2, tasks.size)
        for (t in tasks) {
            assertEquals("Работа", t.layer)
            assertEquals(600, t.dueTimeMinutes)
            assertTrue(t.explanation.contains("Daily Standup"))
            assertTrue(t.explanation.contains("Zoom"))
        }
    }

    @Test
    fun testScheduleItemGeneralizedHelperProperties() {
        val item = ScheduleItem(
            dayOfWeek = 2,
            subject = "Тренировка",
            startMinutes = 480,
            roomOrNote = "Фитнес зала"
        )
        // Verify backward compatible fields
        assertEquals("Тренировка", item.subject)
        assertEquals("Фитнес зала", item.roomOrNote)

        // Verify generalized helper getters
        assertEquals("Тренировка", item.title)
        assertEquals("Фитнес зала", item.locationOrNote)
    }
}
