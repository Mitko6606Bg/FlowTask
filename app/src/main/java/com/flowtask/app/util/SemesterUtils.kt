package com.flowtask.app.util

import com.flowtask.app.data.local.entity.TaskEntity
import com.flowtask.app.data.local.model.LayerProgram
import com.flowtask.app.data.local.model.SemesterConfig
import java.util.Calendar

object SemesterUtils {

    /**
     * Returns the 00:00:00.000 timestamp of the Monday of the week containing [dateMillis].
     */
    fun getMondayOfWeek(dateMillis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon, ..., 7=Sat
        val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
        cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
        return cal.timeInMillis
    }

    /**
     * Returns the 23:59:59.999 timestamp of the Sunday of the week containing [dateMillis].
     */
    fun getSundayOfWeek(dateMillis: Long): Long {
        val monday = getMondayOfWeek(dateMillis)
        val cal = Calendar.getInstance().apply {
            timeInMillis = monday
            add(Calendar.DAY_OF_MONTH, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    /**
     * Returns the 1-based program week number for [dateMillis] if it falls within the active period
     * (defined from the Monday of [startDateMillis] through the Sunday of [endDateMillis]).
     * Returns null if outside the active period or if dates are invalid.
     */
    fun getWeekNumber(dateMillis: Long, startDateMillis: Long?, endDateMillis: Long?): Int? {
        if (startDateMillis == null || endDateMillis == null || endDateMillis < startDateMillis) return null

        val startMonday = getMondayOfWeek(startDateMillis)
        val endSunday = getSundayOfWeek(endDateMillis)

        if (dateMillis < startMonday || dateMillis > endSunday) return null

        val targetMonday = getMondayOfWeek(dateMillis)
        val diffDays = Math.round((targetMonday - startMonday).toDouble() / (1000.0 * 60 * 60 * 24)).toInt()
        val weekNumber = (diffDays / 7) + 1
        return if (weekNumber >= 1) weekNumber else null
    }

    /**
     * Computes the total number of weeks in the program period.
     */
    fun getTotalWeeks(startDateMillis: Long?, endDateMillis: Long?): Int {
        if (startDateMillis == null || endDateMillis == null || endDateMillis < startDateMillis) return 0
        val startMonday = getMondayOfWeek(startDateMillis)
        val endMonday = getMondayOfWeek(endDateMillis)
        val diffDays = Math.round((endMonday - startMonday).toDouble() / (1000.0 * 60 * 60 * 24)).toInt()
        return (diffDays / 7) + 1
    }

    /**
     * Returns the current week number of the program based on today's date.
     */
    fun getCurrentSemesterWeek(startDateMillis: Long?, endDateMillis: Long?): Int? {
        return getWeekNumber(System.currentTimeMillis(), startDateMillis, endDateMillis)
    }

    /**
     * Returns the Monday timestamp for a given [weekNumber] (1-based).
     */
    fun getMondayForSemesterWeek(weekNumber: Int, startDateMillis: Long): Long {
        val startMonday = getMondayOfWeek(startDateMillis)
        val cal = Calendar.getInstance().apply {
            timeInMillis = startMonday
            add(Calendar.DAY_OF_MONTH, (weekNumber - 1) * 7)
        }
        return cal.timeInMillis
    }

    /**
     * Generates TaskEntity objects for all routine schedule items across each week of a [LayerProgram].
     */
    fun generateProgramTasks(program: LayerProgram): List<TaskEntity> {
        val startMillis = program.startDateMillis ?: return emptyList()
        val endMillis = program.endDateMillis ?: return emptyList()
        if (endMillis < startMillis || program.scheduleItems.isEmpty()) return emptyList()

        val totalWeeks = getTotalWeeks(startMillis, endMillis)
        if (totalWeeks <= 0) return emptyList()

        val layerName = program.layerName.trim().ifBlank { "Програма" }
        val startOfDayStart = DateTimeUtils.getStartOfDay(startMillis)
        val endOfDayEnd = getSundayOfWeek(endMillis)

        val taskList = mutableListOf<TaskEntity>()

        for (week in 1..totalWeeks) {
            val weekMonday = getMondayForSemesterWeek(week, startMillis)

            for (item in program.scheduleItems) {
                // item.dayOfWeek: 1 = Monday .. 7 = Sunday
                val routineCal = Calendar.getInstance().apply {
                    timeInMillis = weekMonday
                    add(Calendar.DAY_OF_MONTH, (item.dayOfWeek - 1).coerceIn(0, 6))
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val routineDateMillis = routineCal.timeInMillis
                if (routineDateMillis in startOfDayStart..endOfDayEnd) {
                    val note = item.locationOrNote?.trim()
                    val explanation = if (!note.isNullOrBlank()) {
                        "${item.title} ($note)"
                    } else {
                        item.title.trim()
                    }

                    taskList.add(
                        TaskEntity(
                            id = 0,
                            explanation = explanation,
                            dueDateMillis = routineDateMillis,
                            dueTimeMinutes = item.startMinutes,
                            startDateMillis = null,
                            layer = layerName,
                            isCompleted = false,
                            sortOrder = item.startMinutes,
                            createdAtMillis = System.currentTimeMillis()
                        )
                    )
                }
            }
        }

        return taskList
    }

    /**
     * Generates TaskEntity objects for all schedule items across each week of the semester program.
     */
    fun generateSemesterProgramTasks(config: SemesterConfig): List<TaskEntity> {
        val program = LayerProgram(
            layerName = config.programLayerName,
            isEnabled = config.isEnabled,
            startDateMillis = config.startDateMillis,
            endDateMillis = config.endDateMillis,
            trackWeeks = true,
            scheduleItems = config.scheduleItems
        )
        return generateProgramTasks(program)
    }
}
