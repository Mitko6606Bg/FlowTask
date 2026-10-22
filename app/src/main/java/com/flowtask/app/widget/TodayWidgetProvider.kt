package com.flowtask.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.flowtask.app.MainActivity
import com.flowtask.app.R
import com.flowtask.app.data.local.AppDatabase
import com.flowtask.app.data.repository.SemesterRepository
import com.flowtask.app.ui.quickcreate.QuickCreateActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_task_list)
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            TodayWidgetPrefs.deleteWidgetPrefs(context, appWidgetId)
        }
        super.onDeleted(context, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TOGGLE_TASK -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
                val isCompleted = intent.getBooleanExtra(EXTRA_IS_COMPLETED, false)
                if (taskId != -1L) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = AppDatabase.getInstance(context)
                            val newCompletedState = !isCompleted
                            val completedAt = if (newCompletedState) System.currentTimeMillis() else null
                            db.taskDao().updateTaskCompletion(taskId, newCompletedState, completedAt)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            updateAllWidgets(context)
                            pendingResult.finish()
                        }
                    }
                }
            }
            ACTION_TOGGLE_SUBTASK -> {
                val subTaskId = intent.getLongExtra(EXTRA_SUBTASK_ID, -1L)
                val isCompleted = intent.getBooleanExtra(EXTRA_IS_COMPLETED, false)
                if (subTaskId != -1L) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = AppDatabase.getInstance(context)
                            val newCompletedState = !isCompleted
                            db.taskDao().updateSubTaskCompletion(subTaskId, newCompletedState)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            updateAllWidgets(context)
                            pendingResult.finish()
                        }
                    }
                }
            }
            ACTION_TOGGLE_EXPAND_TASK -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                if (taskId != -1L && appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    TodayWidgetPrefs.toggleTaskExpanded(context, appWidgetId, taskId)
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_task_list)
                }
            }
            ACTION_SELECT_LAYER -> {
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                val layerName = intent.getStringExtra(EXTRA_LAYER_NAME)
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    TodayWidgetPrefs.setSelectedLayer(context, appWidgetId, layerName)
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    updateWidget(context, appWidgetManager, appWidgetId)
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_task_list)
                }
            }
            ACTION_OPEN_TASK -> {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(launchIntent)
            }
            ACTION_UPDATE_WIDGET -> {
                updateAllWidgets(context)
            }
            else -> {
                super.onReceive(context, intent)
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE_TASK = "com.flowtask.app.widget.ACTION_TOGGLE_TASK"
        const val ACTION_TOGGLE_SUBTASK = "com.flowtask.app.widget.ACTION_TOGGLE_SUBTASK"
        const val ACTION_TOGGLE_EXPAND_TASK = "com.flowtask.app.widget.ACTION_TOGGLE_EXPAND_TASK"
        const val ACTION_SELECT_LAYER = "com.flowtask.app.widget.ACTION_SELECT_LAYER"
        const val ACTION_OPEN_TASK = "com.flowtask.app.widget.ACTION_OPEN_TASK"
        const val ACTION_UPDATE_WIDGET = "com.flowtask.app.widget.ACTION_UPDATE_WIDGET"

        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_SUBTASK_ID = "extra_subtask_id"
        const val EXTRA_IS_COMPLETED = "extra_is_completed"
        const val EXTRA_LAYER_NAME = "extra_layer_name"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val componentName = ComponentName(context, TodayWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (widgetIds.isNotEmpty()) {
                for (widgetId in widgetIds) {
                    updateWidget(context, appWidgetManager, widgetId)
                }
                appWidgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widget_task_list)
            }
        }

        fun getAvailableLayers(context: Context): List<String> {
            val set = linkedSetOf<String>()
            try {
                val db = AppDatabase.getInstance(context)
                val dbLayers = db.taskDao().getDistinctLayersSync()
                set.addAll(dbLayers.map { it.trim() }.filter { it.isNotBlank() })
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val semesterRepo = SemesterRepository(context)
                for (p in semesterRepo.layerPrograms.value) {
                    if (p.layerName.isNotBlank()) {
                        set.add(p.layerName.trim())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return set.toList().sorted()
        }

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_today)

            // 1. Formatted Calendar Date Subtitle (e.g. Четвъртък, 22 октомври)
            val now = System.currentTimeMillis()
            val locale = Locale.getDefault()
            val sdf = SimpleDateFormat("EEEE, d MMMM", locale)
            val formattedDate = sdf.format(Date(now))
            val capitalizedDate = formattedDate.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(locale) else it.toString()
            }
            views.setTextViewText(R.id.widget_date, capitalizedDate)

            // Current selected layer for this widget
            val selectedLayer = TodayWidgetPrefs.getSelectedLayer(context, appWidgetId)

            // 2. Configure empty state title depending on filter
            if (selectedLayer != null) {
                views.setTextViewText(
                    R.id.widget_empty_title,
                    context.getString(R.string.widget_empty_layer_format, selectedLayer)
                )
            } else {
                views.setTextViewText(
                    R.id.widget_empty_title,
                    context.getString(R.string.widget_empty_title)
                )
            }

            // 3. Layer Selector Pills Row (matches app LayerFilterBar)
            val availableLayers = getAvailableLayers(context)
            views.removeAllViews(R.id.widget_layer_pills_container)
            if (availableLayers.isNotEmpty()) {
                views.setViewVisibility(R.id.widget_layer_scroll_container, View.VISIBLE)

                val onAccentColor = ContextCompat.getColor(context, R.color.widget_on_accent)
                val secondaryColor = ContextCompat.getColor(context, R.color.widget_text_secondary)

                // "All" Pill
                val isAllSelected = selectedLayer == null
                val allPill = RemoteViews(context.packageName, R.layout.widget_layer_pill)
                allPill.setTextViewText(R.id.widget_pill_text, context.getString(R.string.widget_layer_all))
                if (isAllSelected) {
                    allPill.setInt(R.id.widget_pill_root, "setBackgroundResource", R.drawable.widget_layer_pill_selected)
                    allPill.setTextColor(R.id.widget_pill_text, onAccentColor)
                } else {
                    allPill.setInt(R.id.widget_pill_root, "setBackgroundResource", R.drawable.widget_layer_pill_unselected)
                    allPill.setTextColor(R.id.widget_pill_text, secondaryColor)
                }
                val allIntent = Intent(context, TodayWidgetProvider::class.java).apply {
                    action = ACTION_SELECT_LAYER
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    putExtra(EXTRA_LAYER_NAME, null as String?)
                    data = Uri.parse("flowtask://widget/layer/$appWidgetId/all")
                }
                val allPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (appWidgetId * 1000),
                    allIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                allPill.setOnClickPendingIntent(R.id.widget_pill_root, allPendingIntent)
                views.addView(R.id.widget_layer_pills_container, allPill)

                // Individual Layer Pills
                for ((index, layerName) in availableLayers.take(7).withIndex()) {
                    val isSelected = selectedLayer?.equals(layerName, ignoreCase = true) == true
                    val pill = RemoteViews(context.packageName, R.layout.widget_layer_pill)
                    pill.setTextViewText(R.id.widget_pill_text, layerName)
                    if (isSelected) {
                        pill.setInt(R.id.widget_pill_root, "setBackgroundResource", R.drawable.widget_layer_pill_selected)
                        pill.setTextColor(R.id.widget_pill_text, onAccentColor)
                    } else {
                        pill.setInt(R.id.widget_pill_root, "setBackgroundResource", R.drawable.widget_layer_pill_unselected)
                        pill.setTextColor(R.id.widget_pill_text, secondaryColor)
                    }
                    val layerIntent = Intent(context, TodayWidgetProvider::class.java).apply {
                        action = ACTION_SELECT_LAYER
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        putExtra(EXTRA_LAYER_NAME, layerName)
                        data = Uri.parse("flowtask://widget/layer/$appWidgetId/${Uri.encode(layerName)}")
                    }
                    val layerPendingIntent = PendingIntent.getBroadcast(
                        context,
                        (appWidgetId * 1000 + index + 1),
                        layerIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    pill.setOnClickPendingIntent(R.id.widget_pill_root, layerPendingIntent)
                    views.addView(R.id.widget_layer_pills_container, pill)
                }
            } else {
                views.setViewVisibility(R.id.widget_layer_scroll_container, View.GONE)
            }

            // 4. Setup ListView adapter
            val serviceIntent = Intent(context, TodayWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widget_task_list, serviceIntent)
            views.setEmptyView(R.id.widget_task_list, R.id.widget_empty_view)

            // 5. Setup item click PendingIntent template for ListView
            val itemClickIntent = Intent(context, TodayWidgetProvider::class.java)
            val itemClickPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                itemClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_task_list, itemClickPendingIntent)

            // 6. Quick Create Button -> Launches Card-Style Pop-up Activity from Below
            val createIntent = Intent(context, QuickCreateActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
            }
            val createPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                createIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_button_create, createPendingIntent)

            // 7. Header & Empty View Click -> Opens Main App Today Screen
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                1000 + appWidgetId,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_header, openAppPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_empty_view, openAppPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
