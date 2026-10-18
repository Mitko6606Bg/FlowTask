package com.flowtask.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    private val bgLocale = Locale.forLanguageTag("bg-BG")

    // Thread-local instances prevent continuous heap allocations and GC spikes during UI scrolling
    private val threadLocalCalendar: ThreadLocal<Calendar> = ThreadLocal.withInitial {
        Calendar.getInstance()
    }

    private val threadLocalDateFormat: ThreadLocal<SimpleDateFormat> = ThreadLocal.withInitial {
        SimpleDateFormat("d MMM, yyyy", Locale.getDefault())
    }

    private val threadLocalDayOfWeekFormat: ThreadLocal<SimpleDateFormat> = ThreadLocal.withInitial {
        SimpleDateFormat("EEEE", bgLocale)
    }

    private val threadLocalMonthDayFormat: ThreadLocal<SimpleDateFormat> = ThreadLocal.withInitial {
        SimpleDateFormat("d MMMM", bgLocale)
    }

    private val threadLocalFullDateFormat: ThreadLocal<SimpleDateFormat> = ThreadLocal.withInitial {
        SimpleDateFormat("d MMM yyyy", bgLocale)
    }

    private val threadLocalMonthYearFormat: ThreadLocal<SimpleDateFormat> = ThreadLocal.withInitial {
        SimpleDateFormat("MMMM yyyy", bgLocale)
    }

    private val threadLocalMonthKeyFormat: ThreadLocal<SimpleDateFormat> = ThreadLocal.withInitial {
        SimpleDateFormat("yyyy-MM", Locale.getDefault())
    }

    private fun getCal(): Calendar = threadLocalCalendar.get()!!

    fun formatTime(minutes: Int?): String {
        if (minutes == null) return ""
        val hours = minutes / 60
        val mins = minutes % 60
        val hStr = if (hours < 10) "0$hours" else hours.toString()
        val mStr = if (mins < 10) "0$mins" else mins.toString()
        return "$hStr:$mStr"
    }

    fun formatDate(dateMillis: Long?): String {
        if (dateMillis == null) return ""
        return threadLocalDateFormat.get()!!.format(Date(dateMillis))
    }

    fun formatDateLabel(dateMillis: Long?): String {
        if (dateMillis == null) return "Без дата"

        val cal = getCal()
        cal.timeInMillis = System.currentTimeMillis()
        val todayYear = cal.get(Calendar.YEAR)
        val todayDayOfYear = cal.get(Calendar.DAY_OF_YEAR)

        cal.timeInMillis = dateMillis
        val targetYear = cal.get(Calendar.YEAR)
        val targetDayOfYear = cal.get(Calendar.DAY_OF_YEAR)

        val isSameYear = targetYear == todayYear

        return if (isSameYear) {
            val dayDiff = targetDayOfYear - todayDayOfYear
            when (dayDiff) {
                0 -> "Днес"
                1 -> "Утре"
                2 -> "Вдругиден"
                in 3..6 -> {
                    val dayOfWeek = threadLocalDayOfWeekFormat.get()!!.format(cal.time)
                    dayOfWeek.replaceFirstChar { it.uppercase() }
                }
                else -> {
                    threadLocalMonthDayFormat.get()!!.format(cal.time)
                }
            }
        } else {
            threadLocalFullDateFormat.get()!!.format(cal.time)
        }
    }

    fun formatScheduleInfo(dueDateMillis: Long?, dueTimeMinutes: Int?): String {
        val dateStr = if (dueDateMillis != null) formatDateLabel(dueDateMillis) else ""
        val timeStr = if (dueTimeMinutes != null) formatTime(dueTimeMinutes) else ""

        return when {
            dateStr.isNotEmpty() && timeStr.isNotEmpty() -> "$dateStr в $timeStr"
            dateStr.isNotEmpty() -> dateStr
            timeStr.isNotEmpty() -> "Днес в $timeStr"
            else -> "Гъвкава задача"
        }
    }

    fun getStartOfDay(timeMillis: Long = System.currentTimeMillis()): Long {
        val cal = getCal()
        cal.timeInMillis = timeMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfDay(timeMillis: Long = System.currentTimeMillis()): Long {
        return getStartOfDay(timeMillis) + 86400000L - 1
    }

    fun isToday(dateMillis: Long?): Boolean {
        if (dateMillis == null) return false
        val cal = getCal()
        cal.timeInMillis = System.currentTimeMillis()
        val curYear = cal.get(Calendar.YEAR)
        val curDay = cal.get(Calendar.DAY_OF_YEAR)

        cal.timeInMillis = dateMillis
        return cal.get(Calendar.YEAR) == curYear && cal.get(Calendar.DAY_OF_YEAR) == curDay
    }

    fun getMonthKey(dateMillis: Long): String {
        return threadLocalMonthKeyFormat.get()!!.format(Date(dateMillis))
    }

    fun getMonthDisplayName(year: Int, monthIndex: Int): String {
        val cal = getCal()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthIndex)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val monthName = threadLocalMonthYearFormat.get()!!.format(cal.time)
        return monthName.replaceFirstChar { it.uppercase() }
    }

    fun getDurationDays(startDateMillis: Long, endDateMillis: Long): Int {
        val s = getStartOfDay(startDateMillis)
        val e = getStartOfDay(endDateMillis)
        if (e < s) return 1
        val diff = e - s
        return (diff / (1000L * 60 * 60 * 24)).toInt() + 1
    }

    fun isDateInPeriod(targetDateMillis: Long, startDateMillis: Long?, endDateMillis: Long?): Boolean {
        if (startDateMillis == null || endDateMillis == null) return false
        val target = getStartOfDay(targetDateMillis)
        val start = getStartOfDay(startDateMillis)
        val end = getStartOfDay(endDateMillis)
        return target in start..end
    }

    fun formatDateRange(startDateMillis: Long?, endDateMillis: Long?): String {
        if (startDateMillis == null && endDateMillis == null) return ""
        if (startDateMillis == null) return formatDate(endDateMillis)
        if (endDateMillis == null) return formatDate(startDateMillis)

        val s = getStartOfDay(startDateMillis)
        val e = getStartOfDay(endDateMillis)
        if (s == e) return formatDate(startDateMillis)

        val sCal = Calendar.getInstance().apply { timeInMillis = s }
        val eCal = Calendar.getInstance().apply { timeInMillis = e }

        val sYear = sCal.get(Calendar.YEAR)
        val eYear = eCal.get(Calendar.YEAR)
        val sMonth = sCal.get(Calendar.MONTH)
        val eMonth = eCal.get(Calendar.MONTH)
        val sDay = sCal.get(Calendar.DAY_OF_MONTH)
        val eDay = eCal.get(Calendar.DAY_OF_MONTH)

        return if (sYear == eYear) {
            val monthFormat = threadLocalDateFormat.get()!!
            val sDateParts = monthFormat.format(Date(s)).split(" ")
            val monthStr = if (sDateParts.size > 1) sDateParts[1].trimEnd(',') else ""
            if (sMonth == eMonth) {
                "$sDay–$eDay $monthStr"
            } else {
                val eDateParts = monthFormat.format(Date(e)).split(" ")
                val eMonthStr = if (eDateParts.size > 1) eDateParts[1].trimEnd(',') else ""
                "$sDay $monthStr – $eDay $eMonthStr"
            }
        } else {
            "${formatDate(s)} – ${formatDate(e)}"
        }
    }
}
