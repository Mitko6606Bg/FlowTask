package com.flowtask.app.reminder

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.flowtask.app.MainActivity
import com.flowtask.app.R
import com.flowtask.app.data.local.AppDatabase
import com.flowtask.app.data.local.model.TaskWithSubTasks
import com.flowtask.app.data.repository.SettingsRepository
import com.flowtask.app.util.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object TaskReminderScheduler {

    const val CHANNEL_REMINDERS_ID = "flowtask_reminders_channel"

    const val ACTION_SHOW_REMINDER = "com.flowtask.app.reminder.ACTION_SHOW_REMINDER"
    const val ACTION_COMPLETE_TASK = "com.flowtask.app.reminder.ACTION_COMPLETE_TASK"
    const val ACTION_TOGGLE_SUBTASK = "com.flowtask.app.reminder.ACTION_TOGGLE_SUBTASK"

    const val EXTRA_TASK_ID = "extra_reminder_task_id"
    const val EXTRA_SUBTASK_ID = "extra_reminder_subtask_id"
    const val EXTRA_NEW_IS_COMPLETED = "extra_reminder_new_is_completed"

    private const val MAX_NOTIFICATION_SUBTASKS = 8

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_reminders)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_REMINDERS_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = 0xFF1D7868.toInt()
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun calculateTriggerTimeMillis(dueDateMillis: Long?, dueTimeMinutes: Int?, advanceMinutes: Int): Long {
        if (dueTimeMinutes == null) return -1L
        val baseDate = dueDateMillis ?: System.currentTimeMillis()
        val dayStart = DateTimeUtils.getStartOfDay(baseDate)
        val taskExactTime = dayStart + (dueTimeMinutes * 60_000L)
        return taskExactTime - (advanceMinutes * 60_000L)
    }

    fun scheduleOrShowReminder(context: Context, taskWithSubTasks: TaskWithSubTasks) {
        val task = taskWithSubTasks.task
        if (task.isCompleted || task.dueTimeMinutes == null) {
            cancelReminder(context, task.id)
            dismissNotification(context, task.id)
            return
        }

        val settingsRepo = SettingsRepository(context)
        val advanceMinutes = settingsRepo.reminderAdvanceMinutes.value
        val triggerTime = calculateTriggerTimeMillis(task.dueDateMillis, task.dueTimeMinutes, advanceMinutes)
        val now = System.currentTimeMillis()
        val baseDate = task.dueDateMillis ?: now
        val isToday = DateTimeUtils.isToday(baseDate)
        val isFutureDay = DateTimeUtils.getStartOfDay(baseDate) > DateTimeUtils.getStartOfDay(now)

        if (triggerTime <= now) {
            if (isToday) {
                // Task is due today and trigger time has arrived or passed - show pinned notification
                showOrUpdateNotification(context, taskWithSubTasks, isUpdate = false)
            } else if (!isFutureDay) {
                // Past day task - cancel any alarms
                cancelReminder(context, task.id)
            }
        } else {
            // Future trigger time - schedule exact alarm
            scheduleExactAlarm(context, task.id, triggerTime)
        }
    }

    private fun scheduleExactAlarm(context: Context, taskId: Long, triggerTimeMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = ACTION_SHOW_REMINDER
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId.toInt() * 100) + 1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        try {
            if (canScheduleExact) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                }
            } else {
                // Fallback using AlarmClockInfo to guarantee execution without exact alarm privilege
                val showIntent = Intent(context, MainActivity::class.java)
                val showPendingIntent = PendingIntent.getActivity(
                    context,
                    (taskId.toInt() * 100) + 2,
                    showIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTimeMillis, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            }
        } catch (_: SecurityException) {
            // Fallback for unexpected security exceptions
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        }
    }

    fun cancelReminder(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = ACTION_SHOW_REMINDER
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId.toInt() * 100) + 1,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun showOrUpdateNotification(
        context: Context,
        taskWithSubTasks: TaskWithSubTasks,
        isUpdate: Boolean = false
    ) {
        val task = taskWithSubTasks.task
        if (task.isCompleted) {
            dismissNotification(context, task.id)
            return
        }

        initNotificationChannel(context)

        val collapsedViews = RemoteViews(context.packageName, R.layout.notification_task_reminder_collapsed)
        val expandedViews = RemoteViews(context.packageName, R.layout.notification_task_reminder_expanded)

        // 1. Title
        collapsedViews.setTextViewText(R.id.notification_collapsed_title, task.explanation)
        expandedViews.setTextViewText(R.id.notification_expanded_title, task.explanation)

        // 2. Time Badge
        val timeStr = DateTimeUtils.formatTime(task.dueTimeMinutes)
        collapsedViews.setTextViewText(R.id.notification_collapsed_time, timeStr)
        expandedViews.setTextViewText(R.id.notification_expanded_time, timeStr)

        // 3. Layer Badge
        if (!task.layer.isNullOrBlank()) {
            val layerName = task.layer.trim()
            collapsedViews.setTextViewText(R.id.notification_collapsed_layer, layerName)
            collapsedViews.setViewVisibility(R.id.notification_collapsed_layer, View.VISIBLE)
            expandedViews.setTextViewText(R.id.notification_expanded_layer, layerName)
            expandedViews.setViewVisibility(R.id.notification_expanded_layer, View.VISIBLE)
        } else {
            collapsedViews.setViewVisibility(R.id.notification_collapsed_layer, View.GONE)
            expandedViews.setViewVisibility(R.id.notification_expanded_layer, View.GONE)
        }

        // 4. Subtasks Progress Chip
        val subTasks = taskWithSubTasks.subTasks
        if (subTasks.isNotEmpty()) {
            val completedCount = subTasks.count { it.isCompleted }
            val chipText = "✓ $completedCount/${subTasks.size}"
            collapsedViews.setTextViewText(R.id.notification_collapsed_subtask_chip, chipText)
            collapsedViews.setViewVisibility(R.id.notification_collapsed_subtask_chip, View.VISIBLE)
            expandedViews.setTextViewText(R.id.notification_expanded_subtask_chip, chipText)
            expandedViews.setViewVisibility(R.id.notification_expanded_subtask_chip, View.VISIBLE)
        } else {
            collapsedViews.setViewVisibility(R.id.notification_collapsed_subtask_chip, View.GONE)
            expandedViews.setViewVisibility(R.id.notification_expanded_subtask_chip, View.GONE)
        }

        // 5. Complete Task PendingIntent for Done Buttons and Header Checkboxes
        val completeIntent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = ACTION_COMPLETE_TASK
            putExtra(EXTRA_TASK_ID, task.id)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt() * 100,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        collapsedViews.setOnClickPendingIntent(R.id.notification_collapsed_btn_done, completePendingIntent)
        collapsedViews.setOnClickPendingIntent(R.id.notification_collapsed_check, completePendingIntent)
        expandedViews.setOnClickPendingIntent(R.id.notification_expanded_btn_done, completePendingIntent)
        expandedViews.setOnClickPendingIntent(R.id.notification_expanded_check, completePendingIntent)

        // 6. Subtasks list in Expanded View
        if (subTasks.isNotEmpty()) {
            expandedViews.setViewVisibility(R.id.notification_expanded_subtasks_section, View.VISIBLE)
            val visibleSubTasks = subTasks.take(MAX_NOTIFICATION_SUBTASKS)

            for (i in 0 until MAX_NOTIFICATION_SUBTASKS) {
                val rowId = context.resources.getIdentifier("notification_subtask_row_$i", "id", context.packageName)
                val checkId = context.resources.getIdentifier("notification_subtask_check_$i", "id", context.packageName)
                val textId = context.resources.getIdentifier("notification_subtask_text_$i", "id", context.packageName)

                if (i < visibleSubTasks.size) {
                    val subTask = visibleSubTasks[i]
                    expandedViews.setViewVisibility(rowId, View.VISIBLE)

                    if (subTask.isCompleted) {
                        val spannable = SpannableString(subTask.explanation).apply {
                            setSpan(StrikethroughSpan(), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        expandedViews.setTextViewText(textId, spannable)
                        expandedViews.setTextColor(textId, context.getColor(R.color.widget_text_muted))
                        expandedViews.setImageViewResource(checkId, R.drawable.ic_widget_check_circle)
                    } else {
                        expandedViews.setTextViewText(textId, subTask.explanation)
                        expandedViews.setTextColor(textId, context.getColor(R.color.widget_text_primary))
                        expandedViews.setImageViewResource(checkId, R.drawable.ic_widget_circle_outline)
                    }

                    // PendingIntent to toggle this subtask
                    val subTaskIntent = Intent(context, TaskReminderReceiver::class.java).apply {
                        action = ACTION_TOGGLE_SUBTASK
                        putExtra(EXTRA_TASK_ID, task.id)
                        putExtra(EXTRA_SUBTASK_ID, subTask.id)
                        putExtra(EXTRA_NEW_IS_COMPLETED, !subTask.isCompleted)
                    }
                    val subTaskPendingIntent = PendingIntent.getBroadcast(
                        context,
                        (subTask.id.toInt() * 1000) + 1,
                        subTaskIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    expandedViews.setOnClickPendingIntent(rowId, subTaskPendingIntent)
                    expandedViews.setOnClickPendingIntent(checkId, subTaskPendingIntent)
                } else {
                    expandedViews.setViewVisibility(rowId, View.GONE)
                }
            }

            if (subTasks.size > MAX_NOTIFICATION_SUBTASKS) {
                val moreCount = subTasks.size - MAX_NOTIFICATION_SUBTASKS
                expandedViews.setTextViewText(
                    R.id.notification_subtask_more,
                    context.getString(R.string.notification_more_subtasks_format, moreCount)
                )
                expandedViews.setViewVisibility(R.id.notification_subtask_more, View.VISIBLE)
            } else {
                expandedViews.setViewVisibility(R.id.notification_subtask_more, View.GONE)
            }
        } else {
            expandedViews.setViewVisibility(R.id.notification_expanded_subtasks_section, View.GONE)
        }

        // 7. Notification Content Intent to Open App
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            (task.id.toInt() * 100) + 2,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 8. Build the Notification
        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS_ID)
            .setSmallIcon(R.drawable.ic_widget_check_circle)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(collapsedViews)
            .setCustomBigContentView(expandedViews)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true) // CANNOT be dismissed by swiping!
            .setAutoCancel(false)
            .setOnlyAlertOnce(isUpdate)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_widget_check,
                context.getString(R.string.notification_action_done),
                completePendingIntent
            )

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(task.id.toInt(), builder.build())
        } catch (_: SecurityException) {
            // Permission may be denied on Android 13+
        }
    }

    fun updateNotificationIfActive(context: Context, taskWithSubTasks: TaskWithSubTasks) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val isActive = notificationManager.activeNotifications.any { it.id == taskWithSubTasks.task.id.toInt() }
            if (isActive) {
                showOrUpdateNotification(context, taskWithSubTasks, isUpdate = true)
            }
        }
    }

    fun dismissNotification(context: Context, taskId: Long) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(taskId.toInt())
    }

    fun rescheduleAllReminders(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val tasks = db.taskDao().getPendingTimedTasksSync()
                for (taskWithSubs in tasks) {
                    scheduleOrShowReminder(context, taskWithSubs)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
