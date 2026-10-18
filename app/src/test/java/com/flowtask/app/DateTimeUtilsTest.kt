package com.flowtask.app

import com.flowtask.app.util.DateTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DateTimeUtilsTest {

    @Test
    fun testFormatTime() {
        assertEquals("09:00", DateTimeUtils.formatTime(540))
        assertEquals("14:30", DateTimeUtils.formatTime(870))
        assertEquals("00:00", DateTimeUtils.formatTime(0))
        assertEquals("23:59", DateTimeUtils.formatTime(1439))
        assertEquals("", DateTimeUtils.formatTime(null))
    }

    @Test
    fun testIsToday() {
        val today = System.currentTimeMillis()
        assertTrue(DateTimeUtils.isToday(today))

        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis
        assertFalse(DateTimeUtils.isToday(yesterday))

        assertFalse(DateTimeUtils.isToday(null))
    }

    @Test
    fun testFormatDateLabelTodayAndTomorrow() {
        val today = DateTimeUtils.getStartOfDay()
        val tomorrow = Calendar.getInstance().apply {
            timeInMillis = today
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

        assertEquals("Днес", DateTimeUtils.formatDateLabel(today))
        assertEquals("Утре", DateTimeUtils.formatDateLabel(tomorrow))
        assertEquals("Без дата", DateTimeUtils.formatDateLabel(null))
    }

    @Test
    fun testFormatScheduleInfo() {
        val today = DateTimeUtils.getStartOfDay()
        // Today with time
        val todaySchedule = DateTimeUtils.formatScheduleInfo(today, 540)
        assertEquals("Днес в 09:00", todaySchedule)

        // Time only (no date) -> defaults label to "Днес в HH:mm"
        val timeOnly = DateTimeUtils.formatScheduleInfo(null, 840)
        assertEquals("Днес в 14:00", timeOnly)

        // No date, no time -> flexible
        val flexible = DateTimeUtils.formatScheduleInfo(null, null)
        assertEquals("Гъвкава задача", flexible)
    }

    @Test
    fun testPeriodCalculations() {
        val startCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 5, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 10, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }

        // Duration in days (Oct 5 to Oct 10 inclusive is 6 days)
        val durationDays = DateTimeUtils.getDurationDays(startCal.timeInMillis, endCal.timeInMillis)
        assertEquals(6, durationDays)

        // isDateInPeriod
        val midCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 7, 12, 0, 0)
        }
        assertTrue(DateTimeUtils.isDateInPeriod(midCal.timeInMillis, startCal.timeInMillis, endCal.timeInMillis))

        val beforeCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 4, 23, 59, 0)
        }
        assertFalse(DateTimeUtils.isDateInPeriod(beforeCal.timeInMillis, startCal.timeInMillis, endCal.timeInMillis))

        val afterCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 11, 0, 1, 0)
        }
        assertFalse(DateTimeUtils.isDateInPeriod(afterCal.timeInMillis, startCal.timeInMillis, endCal.timeInMillis))

        // formatDateRange
        val formattedRange = DateTimeUtils.formatDateRange(startCal.timeInMillis, endCal.timeInMillis)
        assertTrue(formattedRange.contains("5") && formattedRange.contains("10"))
    }
}
