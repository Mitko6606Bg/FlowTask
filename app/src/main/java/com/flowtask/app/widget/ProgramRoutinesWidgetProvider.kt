package com.flowtask.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import com.flowtask.app.MainActivity
import com.flowtask.app.R
import com.flowtask.app.data.local.model.LayerProgram
import com.flowtask.app.data.local.model.ScheduleItem
import com.flowtask.app.data.repository.SemesterRepository
import com.flowtask.app.data.repository.SettingsRepository
import com.flowtask.app.util.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProgramRoutinesWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            ProgramWidgetPrefs.deleteWidgetPref(context, appWidgetId)
        }
        super.onDeleted(context, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_UPDATE_PROGRAM_WIDGET,
            Intent.ACTION_TIME_TICK,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED -> {
                updateAllWidgets(context)
            }
            else -> {
                super.onReceive(context, intent)
            }
        }
    }

    data class NextRoutineRaw(
        val item: ScheduleItem,
        val dayOffset: Int,
        val isoDow: Int,
        val targetTimeMillis: Long,
        val isOngoing: Boolean
    )

    enum class DotState {
        PAST, ACTIVE, FUTURE
    }

    data class DayCoverageInfo(
        val totalMinutes: Int,
        val routineCount: Int,
        val completedCount: Int,
        val activeCount: Int,
        val upcomingCount: Int,
        val coveragePercentage: Int,
        val formattedDuration: String,
        val dots: List<DotState>
    )

    companion object {
        const val ACTION_UPDATE_PROGRAM_WIDGET = "com.flowtask.app.widget.ACTION_UPDATE_PROGRAM_WIDGET"
        const val EXTRA_PROGRAM_ID = "extra_program_id"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val componentName = ComponentName(context, ProgramRoutinesWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (widgetIds.isNotEmpty()) {
                for (widgetId in widgetIds) {
                    updateWidget(context, appWidgetManager, widgetId)
                }
            }
        }

        fun findNextRoutines(
            program: LayerProgram,
            nowMillis: Long = System.currentTimeMillis(),
            maxCount: Int = 3
        ): List<NextRoutineRaw> {
            if (program.scheduleItems.isEmpty() || maxCount <= 0) return emptyList()

            val results = mutableListOf<NextRoutineRaw>()
            val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
            val currentMinutes = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)

            val programStart = program.startDateMillis?.let { DateTimeUtils.getStartOfDay(it) }
            val programEnd = program.endDateMillis?.let { DateTimeUtils.getEndOfDay(it) }

            // Search up to 21 days ahead to find upcoming occurrences
            for (offset in 0..21) {
                val targetCal = Calendar.getInstance().apply {
                    timeInMillis = nowMillis
                    add(Calendar.DAY_OF_YEAR, offset)
                }
                val targetStartOfDay = DateTimeUtils.getStartOfDay(targetCal.timeInMillis)

                if (programStart != null && targetStartOfDay < programStart) {
                    continue
                }
                if (programEnd != null && targetStartOfDay > programEnd) {
                    break
                }

                val javaDow = targetCal.get(Calendar.DAY_OF_WEEK)
                val isoDow = if (javaDow == Calendar.SUNDAY) 7 else javaDow - 1

                val dayItems = program.scheduleItems
                    .filter { it.dayOfWeek == isoDow }
                    .sortedBy { it.startMinutes }

                for (item in dayItems) {
                    val effectiveEnd = item.endMinutes ?: (item.startMinutes + 60)
                    if (offset == 0 && effectiveEnd <= currentMinutes) {
                        // Already finished today
                        continue
                    }

                    val isOngoing = (offset == 0 && currentMinutes in item.startMinutes until effectiveEnd)

                    results.add(
                        NextRoutineRaw(
                            item = item,
                            dayOffset = offset,
                            isoDow = isoDow,
                            targetTimeMillis = targetCal.timeInMillis,
                            isOngoing = isOngoing
                        )
                    )

                    if (results.size >= maxCount) break
                }

                if (results.size >= maxCount) break
            }

            return results
        }

        fun calculateDayCoverage(
            program: LayerProgram,
            nowMillis: Long = System.currentTimeMillis()
        ): DayCoverageInfo {
            val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
            val javaDow = nowCal.get(Calendar.DAY_OF_WEEK)
            val isoDow = if (javaDow == Calendar.SUNDAY) 7 else javaDow - 1
            val currentMinutes = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)

            val todayItems = program.scheduleItems
                .filter { it.dayOfWeek == isoDow }
                .sortedBy { it.startMinutes }

            if (todayItems.isEmpty()) {
                return DayCoverageInfo(
                    totalMinutes = 0,
                    routineCount = 0,
                    completedCount = 0,
                    activeCount = 0,
                    upcomingCount = 0,
                    coveragePercentage = 0,
                    formattedDuration = "0m",
                    dots = emptyList()
                )
            }

            var totalMinutes = 0
            var completedCount = 0
            var activeCount = 0
            var upcomingCount = 0

            val dotStates = mutableListOf<DotState>()

            for (item in todayItems) {
                val effectiveEnd = item.endMinutes ?: (item.startMinutes + 60)
                val duration = (effectiveEnd - item.startMinutes).coerceAtLeast(1)
                totalMinutes += duration

                val state = when {
                    effectiveEnd <= currentMinutes -> {
                        completedCount++
                        DotState.PAST
                    }
                    currentMinutes in item.startMinutes until effectiveEnd -> {
                        activeCount++
                        DotState.ACTIVE
                    }
                    else -> {
                        upcomingCount++
                        DotState.FUTURE
                    }
                }
                dotStates.add(state)
            }

            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            val formattedDuration = when {
                hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                hours > 0 -> "${hours}h"
                else -> "${minutes}m"
            }

            // Day coverage percentage relative to standard 14h active day (840 minutes)
            val percentage = ((totalMinutes * 100) / (14 * 60)).coerceIn(1, 100)

            val finalDots = if (dotStates.size <= 5) {
                dotStates
            } else {
                val sampled = mutableListOf<DotState>()
                for (i in 0 until 5) {
                    val index = (i * (dotStates.size - 1)) / 4
                    sampled.add(dotStates[index])
                }
                sampled
            }

            return DayCoverageInfo(
                totalMinutes = totalMinutes,
                routineCount = todayItems.size,
                completedCount = completedCount,
                activeCount = activeCount,
                upcomingCount = upcomingCount,
                coveragePercentage = percentage,
                formattedDuration = formattedDuration,
                dots = finalDots
            )
        }

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_program_routines)

            val semesterRepo = SemesterRepository(context)
            val allPrograms = semesterRepo.layerPrograms.value

            // Resolve target program
            val savedProgramId = ProgramWidgetPrefs.getSelectedProgramId(context, appWidgetId)
            val selectedProgram: LayerProgram? = if (!savedProgramId.isNullOrBlank()) {
                allPrograms.firstOrNull { it.id == savedProgramId || it.layerName.equals(savedProgramId, ignoreCase = true) }
                    ?: allPrograms.firstOrNull { it.isEnabled }
                    ?: allPrograms.firstOrNull()
            } else {
                val fallback = allPrograms.firstOrNull { it.isEnabled } ?: allPrograms.firstOrNull()
                if (fallback != null) {
                    ProgramWidgetPrefs.setSelectedProgramId(context, appWidgetId, fallback.id)
                }
                fallback
            }

            // Case 1: No programs exist
            if (selectedProgram == null) {
                views.setTextViewText(R.id.widget_program_title, context.getString(R.string.widget_program_routines_title))
                views.setTextViewText(R.id.widget_program_subtitle, context.getString(R.string.widget_no_program_selected))
                views.setViewVisibility(R.id.widget_day_coverage_bar, View.GONE)
                views.setViewVisibility(R.id.widget_routines_container, View.GONE)
                views.setViewVisibility(R.id.widget_empty_view, View.GONE)
                views.setViewVisibility(R.id.widget_no_program_view, View.VISIBLE)

                val configIntent = Intent(context, ProgramWidgetConfigureActivity::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val configPendingIntent = PendingIntent.getActivity(
                    context,
                    2000 + appWidgetId,
                    configIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_button_settings, configPendingIntent)
                views.setOnClickPendingIntent(R.id.widget_no_program_view, configPendingIntent)
                views.setOnClickPendingIntent(R.id.widget_header, configPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
                return
            }

            // Dynamic colors vs Fallback style (Requirement 1)
            val accentColorInt: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Uses system Monet dynamic wallpaper accent
                context.getColor(R.color.widget_accent)
            } else {
                // Fallback style for phones without dynamic colors: user's selected app style
                val settingsRepo = SettingsRepository(context)
                val appAccentHex = settingsRepo.accentColor.value
                try {
                    Color.parseColor(appAccentHex)
                } catch (_: Exception) {
                    0xFF1D7868.toInt()
                }
            }

            // Header Info
            views.setTextViewText(R.id.widget_program_title, selectedProgram.layerName)
            val now = System.currentTimeMillis()
            val dateLabel = DateTimeUtils.formatDateLabel(now)
            views.setTextViewText(R.id.widget_program_subtitle, dateLabel)
            views.setInt(R.id.widget_program_icon, "setColorFilter", accentColorInt)

            // Settings button intent -> Launches configure activity
            val configIntent = Intent(context, ProgramWidgetConfigureActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val configPendingIntent = PendingIntent.getActivity(
                context,
                2000 + appWidgetId,
                configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_button_settings, configPendingIntent)

            // Header & Empty View tap -> Opens main app
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_OPEN_PROGRAM_ID, selectedProgram.id)
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                3000 + appWidgetId,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_header, openAppPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_empty_view, openAppPendingIntent)

            // Day Coverage Bar (Dots timeline + Coverage text)
            val dayCoverage = calculateDayCoverage(selectedProgram, now)
            views.setViewVisibility(R.id.widget_day_coverage_bar, View.VISIBLE)

            if (dayCoverage.routineCount == 0) {
                views.setViewVisibility(R.id.widget_dots_container, View.GONE)
                views.setTextViewText(R.id.widget_coverage_text, context.getString(R.string.widget_free_day))
            } else if (dayCoverage.completedCount == dayCoverage.routineCount) {
                views.setViewVisibility(R.id.widget_dots_container, View.VISIBLE)
                bindDots(views, dayCoverage.dots, accentColorInt)
                views.setTextViewText(
                    R.id.widget_coverage_text,
                    context.getString(R.string.widget_done_today, dayCoverage.formattedDuration)
                )
            } else {
                views.setViewVisibility(R.id.widget_dots_container, View.VISIBLE)
                bindDots(views, dayCoverage.dots, accentColorInt)
                views.setTextViewText(
                    R.id.widget_coverage_text,
                    context.getString(R.string.widget_coverage_format, dayCoverage.formattedDuration, dayCoverage.coveragePercentage)
                )
            }

            // Find Next Routines
            val upcomingRoutines = findNextRoutines(selectedProgram, now, maxCount = 4)
            val featuredRoutine = upcomingRoutines.firstOrNull()

            if (featuredRoutine == null) {
                views.setViewVisibility(R.id.widget_routines_container, View.GONE)
                views.setViewVisibility(R.id.widget_empty_view, View.VISIBLE)
                views.setViewVisibility(R.id.widget_no_program_view, View.GONE)
            } else {
                views.setViewVisibility(R.id.widget_routines_container, View.VISIBLE)
                views.setViewVisibility(R.id.widget_empty_view, View.GONE)
                views.setViewVisibility(R.id.widget_no_program_view, View.GONE)

                // 1. Bind Main Focus Featured Card
                bindFeaturedRoutine(views, featuredRoutine, accentColorInt, context, appWidgetId, selectedProgram.id)

                // 2. Bind Today's Other Routines (ONLY current day!)
                val nowCal = Calendar.getInstance().apply { timeInMillis = now }
                val currentMinutes = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)
                val javaDow = nowCal.get(Calendar.DAY_OF_WEEK)
                val isoDow = if (javaDow == Calendar.SUNDAY) 7 else javaDow - 1

                val todayRemaining = selectedProgram.scheduleItems
                    .filter { it.dayOfWeek == isoDow }
                    .filter { (it.endMinutes ?: (it.startMinutes + 60)) > currentMinutes }
                    .sortedBy { it.startMinutes }

                // If featuredRoutine was today's first routine, drop it to get the others
                val todayOthers = if (featuredRoutine.dayOffset == 0) {
                    todayRemaining.drop(1)
                } else {
                    // Featured routine is on a future day -> there are no remaining routines today!
                    emptyList()
                }

                if (todayOthers.isEmpty()) {
                    views.setViewVisibility(R.id.widget_today_others_container, View.GONE)
                } else {
                    views.setViewVisibility(R.id.widget_today_others_container, View.VISIBLE)

                    // Secondary Routine 1
                    val other1 = todayOthers.getOrNull(0)
                    if (other1 != null) {
                        views.setViewVisibility(R.id.widget_routine_item_1, View.VISIBLE)
                        views.setInt(R.id.routine_1_bar, "setColorFilter", accentColorInt)
                        val other1Time = if (other1.endMinutes != null) {
                            "${DateTimeUtils.formatTime(other1.startMinutes)} - ${DateTimeUtils.formatTime(other1.endMinutes)}"
                        } else {
                            DateTimeUtils.formatTime(other1.startMinutes)
                        }
                        views.setTextViewText(R.id.routine_1_time, other1Time)
                        views.setTextViewText(R.id.routine_1_title, other1.subject)
                        if (!other1.roomOrNote.isNullOrBlank()) {
                            views.setViewVisibility(R.id.routine_1_room, View.VISIBLE)
                            views.setTextViewText(R.id.routine_1_room, other1.roomOrNote)
                        } else {
                            views.setViewVisibility(R.id.routine_1_room, View.GONE)
                        }
                        views.setOnClickPendingIntent(R.id.widget_routine_item_1, openAppPendingIntent)
                    } else {
                        views.setViewVisibility(R.id.widget_routine_item_1, View.GONE)
                    }

                    // Secondary Routine 2
                    val other2 = todayOthers.getOrNull(1)
                    if (other2 != null) {
                        views.setViewVisibility(R.id.widget_routine_item_2, View.VISIBLE)
                        views.setInt(R.id.routine_2_bar, "setColorFilter", accentColorInt)
                        val other2Time = if (other2.endMinutes != null) {
                            "${DateTimeUtils.formatTime(other2.startMinutes)} - ${DateTimeUtils.formatTime(other2.endMinutes)}"
                        } else {
                            DateTimeUtils.formatTime(other2.startMinutes)
                        }
                        views.setTextViewText(R.id.routine_2_time, other2Time)
                        views.setTextViewText(R.id.routine_2_title, other2.subject)
                        if (!other2.roomOrNote.isNullOrBlank()) {
                            views.setViewVisibility(R.id.routine_2_room, View.VISIBLE)
                            views.setTextViewText(R.id.routine_2_room, other2.roomOrNote)
                        } else {
                            views.setViewVisibility(R.id.routine_2_room, View.GONE)
                        }
                        views.setOnClickPendingIntent(R.id.widget_routine_item_2, openAppPendingIntent)
                    } else {
                        views.setViewVisibility(R.id.widget_routine_item_2, View.GONE)
                    }

                    // More than 2 additional routines today
                    if (todayOthers.size > 2) {
                        views.setViewVisibility(R.id.widget_today_more_count, View.VISIBLE)
                        views.setTextViewText(
                            R.id.widget_today_more_count,
                            context.getString(R.string.widget_more_today_format, todayOthers.size - 2)
                        )
                    } else {
                        views.setViewVisibility(R.id.widget_today_more_count, View.GONE)
                    }
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun bindDots(views: RemoteViews, dots: List<DotState>, accentColorInt: Int) {
            val dotViewIds = listOf(
                R.id.widget_dot_1,
                R.id.widget_dot_2,
                R.id.widget_dot_3,
                R.id.widget_dot_4,
                R.id.widget_dot_5
            )

            for (i in dotViewIds.indices) {
                val viewId = dotViewIds[i]
                if (i < dots.size) {
                    views.setViewVisibility(viewId, View.VISIBLE)
                    when (dots[i]) {
                        DotState.PAST -> {
                            views.setImageViewResource(viewId, R.drawable.widget_dot_past)
                            views.setInt(viewId, "setColorFilter", 0)
                        }
                        DotState.ACTIVE -> {
                            views.setImageViewResource(viewId, R.drawable.widget_dot_active)
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                                views.setInt(viewId, "setColorFilter", accentColorInt)
                            }
                        }
                        DotState.FUTURE -> {
                            views.setImageViewResource(viewId, R.drawable.widget_dot_future)
                            views.setInt(viewId, "setColorFilter", 0)
                        }
                    }
                } else {
                    views.setViewVisibility(viewId, View.GONE)
                }
            }
        }

        private fun bindFeaturedRoutine(
            views: RemoteViews,
            featured: NextRoutineRaw,
            accentColorInt: Int,
            context: Context,
            appWidgetId: Int,
            programId: String
        ) {
            val targetCal = Calendar.getInstance().apply { timeInMillis = featured.targetTimeMillis }

            val timeRange = if (featured.item.endMinutes != null) {
                "${DateTimeUtils.formatTime(featured.item.startMinutes)} - ${DateTimeUtils.formatTime(featured.item.endMinutes)}"
            } else {
                DateTimeUtils.formatTime(featured.item.startMinutes)
            }

            val timeText = when {
                featured.isOngoing -> {
                    "${context.getString(R.string.widget_now_label)} • $timeRange"
                }
                featured.dayOffset == 0 -> {
                    timeRange
                }
                featured.dayOffset == 1 -> {
                    "${context.getString(R.string.widget_tomorrow_label)} • $timeRange"
                }
                featured.dayOffset in 2..6 -> {
                    val dowName = targetCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
                        ?: ""
                    val titleCaseDow = dowName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    "$titleCaseDow • $timeRange"
                }
                else -> {
                    val df = SimpleDateFormat("d MMM • ", Locale.getDefault())
                    "${df.format(targetCal.time)}$timeRange"
                }
            }

            views.setTextViewText(R.id.focus_routine_time, timeText)

            // Big font title
            views.setTextViewText(R.id.focus_routine_title, featured.item.subject)

            // Room / Note row
            val room = featured.item.roomOrNote?.trim()
            if (!room.isNullOrEmpty()) {
                views.setViewVisibility(R.id.focus_routine_room_row, View.VISIBLE)
                views.setTextViewText(R.id.focus_routine_room, room)
            } else {
                views.setViewVisibility(R.id.focus_routine_room_row, View.GONE)
            }

            // Tap focus card opens app
            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_OPEN_PROGRAM_ID, programId)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                5000 + appWidgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_focus_routine_card, pendingIntent)
        }
    }
}
