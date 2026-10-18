package com.flowtask.app.widget

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.flowtask.app.R
import com.flowtask.app.data.local.AppDatabase
import com.flowtask.app.data.local.model.TaskWithSubTasks
import com.flowtask.app.util.DateTimeUtils
import java.util.Calendar

class TodayWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TodayRemoteViewsFactory(applicationContext)
    }
}

class TodayRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private val tasks = mutableListOf<TaskWithSubTasks>()

    override fun onCreate() {
        // Initial setup
    }

    override fun onDataSetChanged() {
        val (startOfDay, endOfDay) = getTodayBounds()
        try {
            val db = AppDatabase.getInstance(context)
            val currentTasks = db.taskDao().getTodayTasksSync(startOfDay, endOfDay)
            tasks.clear()
            tasks.addAll(currentTasks)
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

        val views = RemoteViews(context.packageName, R.layout.widget_task_item)

        // Title with optional strikethrough if completed
        val titleText = task.explanation
        if (task.isCompleted) {
            val spannable = SpannableString(titleText).apply {
                setSpan(StrikethroughSpan(), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            views.setTextViewText(R.id.widget_item_text, spannable)
            views.setImageViewResource(R.id.widget_item_checkbox, R.drawable.ic_widget_check_circle)
        } else {
            views.setTextViewText(R.id.widget_item_text, titleText)
            views.setImageViewResource(R.id.widget_item_checkbox, R.drawable.ic_widget_circle_outline)
        }

        // Meta info (due time or layer)
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

        // FillInIntent for Checkbox toggle
        val toggleIntent = Intent().apply {
            action = TodayWidgetProvider.ACTION_TOGGLE_TASK
            putExtra(TodayWidgetProvider.EXTRA_TASK_ID, task.id)
            putExtra(TodayWidgetProvider.EXTRA_IS_COMPLETED, task.isCompleted)
        }
        views.setOnClickFillInIntent(R.id.widget_item_checkbox, toggleIntent)

        // FillInIntent for Row click -> Opens main app
        val rowIntent = Intent().apply {
            action = TodayWidgetProvider.ACTION_OPEN_TASK
            putExtra(TodayWidgetProvider.EXTRA_TASK_ID, task.id)
        }
        views.setOnClickFillInIntent(R.id.widget_item_container, rowIntent)

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
