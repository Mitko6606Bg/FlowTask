package com.flowtask.app.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.flowtask.app.R
import com.flowtask.app.data.local.AppDatabase
import com.flowtask.app.data.local.model.TaskWithSubTasks
import com.flowtask.app.util.DateTimeUtils
import java.util.Calendar

class TodayWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        var appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            val dataStr = intent.dataString ?: ""
            val regex = Regex("""appWidgetId=(\d+)""")
            regex.find(dataStr)?.let {
                appWidgetId = it.groupValues[1].toIntOrNull() ?: AppWidgetManager.INVALID_APPWIDGET_ID
            }
        }
        return TodayRemoteViewsFactory(applicationContext, appWidgetId)
    }
}

class TodayRemoteViewsFactory(
    private val context: Context,
    private val appWidgetId: Int
) : RemoteViewsService.RemoteViewsFactory {

    private data class SubtaskSlot(
        val rowId: Int,
        val checkId: Int,
        val textId: Int
    )

    private val subtaskSlots = listOf(
        SubtaskSlot(R.id.widget_subtask_row_0, R.id.widget_subtask_check_0, R.id.widget_subtask_text_0),
        SubtaskSlot(R.id.widget_subtask_row_1, R.id.widget_subtask_check_1, R.id.widget_subtask_text_1),
        SubtaskSlot(R.id.widget_subtask_row_2, R.id.widget_subtask_check_2, R.id.widget_subtask_text_2),
        SubtaskSlot(R.id.widget_subtask_row_3, R.id.widget_subtask_check_3, R.id.widget_subtask_text_3),
        SubtaskSlot(R.id.widget_subtask_row_4, R.id.widget_subtask_check_4, R.id.widget_subtask_text_4),
        SubtaskSlot(R.id.widget_subtask_row_5, R.id.widget_subtask_check_5, R.id.widget_subtask_text_5),
        SubtaskSlot(R.id.widget_subtask_row_6, R.id.widget_subtask_check_6, R.id.widget_subtask_text_6),
        SubtaskSlot(R.id.widget_subtask_row_7, R.id.widget_subtask_check_7, R.id.widget_subtask_text_7),
    )

    private val tasks = mutableListOf<TaskWithSubTasks>()

    override fun onCreate() {
        onDataSetChanged()
    }

    override fun onDataSetChanged() {
        val (startOfDay, endOfDay) = getTodayBounds()
        try {
            val db = AppDatabase.getInstance(context)
            val currentTasks = db.taskDao().getTodayTasksSync(startOfDay, endOfDay)
            val selectedLayer = TodayWidgetPrefs.getSelectedLayer(context, appWidgetId)
            val filteredTasks = if (selectedLayer == null) {
                currentTasks
            } else {
                currentTasks.filter { it.task.layer?.equals(selectedLayer, ignoreCase = true) == true }
            }

            tasks.clear()
            tasks.addAll(filteredTasks)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        tasks.clear()
    }

    override fun getCount(): Int = tasks.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position < 0 || position >= tasks.size) return null

        val taskWithSub = tasks[position]
        val task = taskWithSub.task
        val subTasks = taskWithSub.subTasks

        val primaryColor = ContextCompat.getColor(context, R.color.widget_text_primary)
        val mutedColor = ContextCompat.getColor(context, R.color.widget_text_muted)
        val accentColor = ContextCompat.getColor(context, R.color.widget_accent)

        val views = RemoteViews(context.packageName, R.layout.widget_task_item)

        // 1. Main Task Title & Completion Checkbox
        val titleText = task.explanation
        if (task.isCompleted) {
            val spannable = SpannableString(titleText).apply {
                setSpan(StrikethroughSpan(), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            views.setTextViewText(R.id.widget_item_text, spannable)
            views.setTextColor(R.id.widget_item_text, mutedColor)
            views.setImageViewResource(R.id.widget_item_checkbox, R.drawable.ic_widget_check_circle)
        } else {
            views.setTextViewText(R.id.widget_item_text, titleText)
            views.setTextColor(R.id.widget_item_text, primaryColor)
            views.setImageViewResource(R.id.widget_item_checkbox, R.drawable.ic_widget_circle_outline)
        }

        // 2. Meta Info (Time and/or Layer)
        val metaParts = mutableListOf<String>()
        task.dueTimeMinutes?.let { minutes ->
            metaParts.add(DateTimeUtils.formatTime(minutes))
        }
        task.layer?.let { layerName ->
            if (layerName.isNotBlank()) {
                metaParts.add(layerName)
            }
        }
        if (metaParts.isNotEmpty()) {
            views.setViewVisibility(R.id.widget_item_meta, View.VISIBLE)
            views.setTextViewText(R.id.widget_item_meta, metaParts.joinToString(" • "))
        } else {
            views.setViewVisibility(R.id.widget_item_meta, View.GONE)
        }

        // 3. Dropdown Pill (Subtask Progress Chip matching TaskCard.kt)
        val totalSubtasks = subTasks.size
        val completedSubtasks = subTasks.count { it.isCompleted }
        val isExpanded = TodayWidgetPrefs.isTaskExpanded(context, appWidgetId, task.id)
        val allCompleted = totalSubtasks > 0 && completedSubtasks == totalSubtasks

        if (totalSubtasks > 0) {
            views.setViewVisibility(R.id.widget_item_subtask_chip, View.VISIBLE)
            views.setTextViewText(R.id.widget_item_subtask_chip_text, "$completedSubtasks/$totalSubtasks")

            if (allCompleted) {
                views.setInt(R.id.widget_item_subtask_chip, "setBackgroundResource", R.drawable.widget_subtask_chip_completed_background)
                views.setImageViewResource(R.id.widget_item_subtask_chip_check, R.drawable.ic_widget_check_accent)
                views.setTextColor(R.id.widget_item_subtask_chip_text, accentColor)
                views.setImageViewResource(
                    R.id.widget_item_subtask_chip_icon,
                    if (isExpanded) R.drawable.ic_widget_chevron_up_accent else R.drawable.ic_widget_chevron_down_accent
                )
            } else {
                views.setInt(R.id.widget_item_subtask_chip, "setBackgroundResource", R.drawable.widget_subtask_chip_background)
                views.setImageViewResource(R.id.widget_item_subtask_chip_check, R.drawable.ic_widget_check)
                views.setTextColor(R.id.widget_item_subtask_chip_text, primaryColor)
                views.setImageViewResource(
                    R.id.widget_item_subtask_chip_icon,
                    if (isExpanded) R.drawable.ic_widget_chevron_up else R.drawable.ic_widget_chevron_down
                )
            }

            // Click listener for Dropdown Pill -> Toggles expand/collapse
            val expandIntent = Intent().apply {
                action = TodayWidgetProvider.ACTION_TOGGLE_EXPAND_TASK
                putExtra(TodayWidgetProvider.EXTRA_TASK_ID, task.id)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            views.setOnClickFillInIntent(R.id.widget_item_subtask_chip, expandIntent)
        } else {
            views.setViewVisibility(R.id.widget_item_subtask_chip, View.GONE)
        }

        // 4. Expandable Subtasks Section (Inside the SAME Card Container with static checkable slots!)
        if (isExpanded && totalSubtasks > 0) {
            views.setViewVisibility(R.id.widget_subtasks_section, View.VISIBLE)

            for (i in subtaskSlots.indices) {
                val slot = subtaskSlots[i]
                if (i < subTasks.size) {
                    val subTask = subTasks[i]
                    views.setViewVisibility(slot.rowId, View.VISIBLE)

                    if (subTask.isCompleted) {
                        val subSpannable = SpannableString(subTask.explanation).apply {
                            setSpan(StrikethroughSpan(), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        views.setTextViewText(slot.textId, subSpannable)
                        views.setTextColor(slot.textId, mutedColor)
                        views.setImageViewResource(slot.checkId, R.drawable.ic_widget_check_circle)
                    } else {
                        views.setTextViewText(slot.textId, subTask.explanation)
                        views.setTextColor(slot.textId, primaryColor)
                        views.setImageViewResource(slot.checkId, R.drawable.ic_widget_circle_outline)
                    }

                    val subToggleIntent = Intent().apply {
                        action = TodayWidgetProvider.ACTION_TOGGLE_SUBTASK
                        putExtra(TodayWidgetProvider.EXTRA_SUBTASK_ID, subTask.id)
                        putExtra(TodayWidgetProvider.EXTRA_TASK_ID, task.id)
                        putExtra(TodayWidgetProvider.EXTRA_IS_COMPLETED, subTask.isCompleted)
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    }
                    views.setOnClickFillInIntent(slot.checkId, subToggleIntent)
                    views.setOnClickFillInIntent(slot.rowId, subToggleIntent)
                } else {
                    views.setViewVisibility(slot.rowId, View.GONE)
                }
            }

            if (subTasks.size > subtaskSlots.size) {
                val extra = subTasks.size - subtaskSlots.size
                views.setViewVisibility(R.id.widget_subtask_more_text, View.VISIBLE)
                views.setTextViewText(R.id.widget_subtask_more_text, "+$extra more in FlowTask")
                val openTaskIntent = Intent().apply {
                    action = TodayWidgetProvider.ACTION_OPEN_TASK
                    putExtra(TodayWidgetProvider.EXTRA_TASK_ID, task.id)
                }
                views.setOnClickFillInIntent(R.id.widget_subtask_more_text, openTaskIntent)
            } else {
                views.setViewVisibility(R.id.widget_subtask_more_text, View.GONE)
            }
        } else {
            views.setViewVisibility(R.id.widget_subtasks_section, View.GONE)
        }

        // 5. FillInIntent for Checkbox toggle (Main task)
        val toggleIntent = Intent().apply {
            action = TodayWidgetProvider.ACTION_TOGGLE_TASK
            putExtra(TodayWidgetProvider.EXTRA_TASK_ID, task.id)
            putExtra(TodayWidgetProvider.EXTRA_IS_COMPLETED, task.isCompleted)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        views.setOnClickFillInIntent(R.id.widget_item_checkbox, toggleIntent)

        // 6. FillInIntent for Main Task Text Row -> Opens main app
        val rowIntent = Intent().apply {
            action = TodayWidgetProvider.ACTION_OPEN_TASK
            putExtra(TodayWidgetProvider.EXTRA_TASK_ID, task.id)
        }
        views.setOnClickFillInIntent(R.id.widget_item_text_container, rowIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return if (position in tasks.indices) tasks[position].task.id else position.toLong()
    }

    override fun hasStableIds(): Boolean = true

    private fun getTodayBounds(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return Pair(start, end)
    }
}
