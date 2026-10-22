package com.flowtask.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.flowtask.app.data.local.AppDatabase
import com.flowtask.app.widget.TodayWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)

                when (action) {
                    TaskReminderScheduler.ACTION_SHOW_REMINDER -> {
                        val taskId = intent.getLongExtra(TaskReminderScheduler.EXTRA_TASK_ID, -1L)
                        if (taskId != -1L) {
                            val taskWithSubs = db.taskDao().getTaskById(taskId)
                            if (taskWithSubs != null && !taskWithSubs.task.isCompleted && taskWithSubs.task.dueTimeMinutes != null) {
                                TaskReminderScheduler.showOrUpdateNotification(context, taskWithSubs, isUpdate = false)
                            }
                        }
                    }

                    TaskReminderScheduler.ACTION_COMPLETE_TASK -> {
                        val taskId = intent.getLongExtra(TaskReminderScheduler.EXTRA_TASK_ID, -1L)
                        if (taskId != -1L) {
                            db.taskDao().updateTaskCompletion(taskId, true, System.currentTimeMillis())
                            db.taskDao().updateAllSubTasksCompletionForTask(taskId, true)
                            TaskReminderScheduler.dismissNotification(context, taskId)
                            TaskReminderScheduler.cancelReminder(context, taskId)
                            TodayWidgetProvider.updateAllWidgets(context)
                        }
                    }

                    TaskReminderScheduler.ACTION_TOGGLE_SUBTASK -> {
                        val taskId = intent.getLongExtra(TaskReminderScheduler.EXTRA_TASK_ID, -1L)
                        val subTaskId = intent.getLongExtra(TaskReminderScheduler.EXTRA_SUBTASK_ID, -1L)
                        val newIsCompleted = intent.getBooleanExtra(TaskReminderScheduler.EXTRA_NEW_IS_COMPLETED, false)

                        if (subTaskId != -1L) {
                            db.taskDao().updateSubTaskCompletion(subTaskId, newIsCompleted)

                            if (taskId != -1L) {
                                val taskWithSubs = db.taskDao().getTaskById(taskId)
                                if (taskWithSubs != null) {
                                    val allCompleted = taskWithSubs.subTasks.isNotEmpty() &&
                                            taskWithSubs.subTasks.all { it.isCompleted }

                                    if (allCompleted) {
                                        // Auto-complete main task when all subtasks are finished and dismiss notification
                                        db.taskDao().updateTaskCompletion(taskId, true, System.currentTimeMillis())
                                        TaskReminderScheduler.dismissNotification(context, taskId)
                                        TaskReminderScheduler.cancelReminder(context, taskId)
                                    } else {
                                        // Update notification view quietly (isUpdate = true)
                                        TaskReminderScheduler.showOrUpdateNotification(context, taskWithSubs, isUpdate = true)
                                    }
                                }
                            }
                            TodayWidgetProvider.updateAllWidgets(context)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
