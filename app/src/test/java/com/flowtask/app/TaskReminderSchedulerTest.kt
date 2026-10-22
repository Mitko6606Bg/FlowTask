package com.flowtask.app

import com.flowtask.app.reminder.TaskReminderScheduler
import com.flowtask.app.util.DateTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.Calendar

class TaskReminderSchedulerTest {

    @Test
    fun testCalculateTriggerTimeMillisExact() {
        val baseDate = DateTimeUtils.getStartOfDay(System.currentTimeMillis())
        val dueTimeMinutes = 14 * 60 + 30 // 14:30
        val advanceMinutes = 0

        val expected = baseDate + (dueTimeMinutes * 60_000L)
        val actual = TaskReminderScheduler.calculateTriggerTimeMillis(baseDate, dueTimeMinutes, advanceMinutes)

        assertEquals(expected, actual)
    }

    @Test
    fun testCalculateTriggerTimeMillisWithAdvanceNotice() {
        val baseDate = DateTimeUtils.getStartOfDay(System.currentTimeMillis())
        val dueTimeMinutes = 10 * 60 // 10:00 AM
        val advance5Min = 5
        val advance15Min = 15

        val exactTime = baseDate + (dueTimeMinutes * 60_000L)

        val trigger5 = TaskReminderScheduler.calculateTriggerTimeMillis(baseDate, dueTimeMinutes, advance5Min)
        assertEquals(exactTime - (5 * 60_000L), trigger5)

        val trigger15 = TaskReminderScheduler.calculateTriggerTimeMillis(baseDate, dueTimeMinutes, advance15Min)
        assertEquals(exactTime - (15 * 60_000L), trigger15)
    }

    @Test
    fun testCalculateTriggerTimeMillisNullDueTimeReturnsMinusOne() {
        val baseDate = System.currentTimeMillis()
        val result = TaskReminderScheduler.calculateTriggerTimeMillis(baseDate, null, 0)
        assertEquals(-1L, result)
    }
}
