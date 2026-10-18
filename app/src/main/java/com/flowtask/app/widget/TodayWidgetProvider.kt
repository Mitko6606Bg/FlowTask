package com.flowtask.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.flowtask.app.MainActivity
import com.flowtask.app.R
import com.flowtask.app.data.local.AppDatabase
import com.flowtask.app.ui.quickcreate.QuickCreateActivity
import com.flowtask.app.util.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class TodayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
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
        const val ACTION_OPEN_TASK = "com.flowtask.app.widget.ACTION_OPEN_TASK"
        const val ACTION_UPDATE_WIDGET = "com.flowtask.app.widget.ACTION_UPDATE_WIDGET"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_IS_COMPLETED = "extra_is_completed"

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

        private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_today)

            // Date formatting
            val now = System.currentTimeMillis()
            val dateLabel = DateTimeUtils.formatDateLabel(now)
            views.setTextViewText(R.id.widget_date, dateLabel)

            // Query today's tasks count synchronously on caller/worker
            val (startOfDay, endOfDay) = getTodayBounds()
            var totalTasks = 0
            var remainingTasks = 0
            try {
                val db = AppDatabase.getInstance(context)
                val todayTasks = db.taskDao().getTodayTasksSync(startOfDay, endOfDay)
                totalTasks = todayTasks.size
                remainingTasks = todayTasks.count { !it.task.isCompleted }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Task count badge pill
            val badgeText = when {
                totalTasks == 0 -> context.getString(R.string.widget_all_done)
                remainingTasks == 0 -> context.getString(R.string.widget_all_done)
                else -> context.getString(R.string.widget_remaining_format, remainingTasks)
            }
            views.setTextViewText(R.id.widget_count_badge, badgeText)

            // Setup ListView adapter
            val serviceIntent = Intent(context, TodayWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widget_task_list, serviceIntent)
            views.setEmptyView(R.id.widget_task_list, R.id.widget_empty_view)

            // Setup item click PendingIntent template for ListView
            val itemClickIntent = Intent(context, TodayWidgetProvider::class.java)
            val itemClickPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                itemClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_task_list, itemClickPendingIntent)

            // Quick Create Button -> Launches Card-Style Pop-up Activity from Below
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

            // Header & Empty View Click -> Opens Main App Today Screen
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
}
