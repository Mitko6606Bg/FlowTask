package com.flowtask.app.data.repository

import com.flowtask.app.data.local.dao.TaskDao
import com.flowtask.app.data.local.entity.SubTaskEntity
import com.flowtask.app.data.local.entity.TaskEntity
import com.flowtask.app.data.local.model.TaskWithSubTasks
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class TaskRepository(private val taskDao: TaskDao) {

    fun getTodayTasks(): Flow<List<TaskWithSubTasks>> {
        val (startOfDay, endOfDay) = getTodayBounds()
        return taskDao.getTodayTasksFlow(startOfDay, endOfDay)
    }

    fun getUpcomingTasks(days: Int = 10): Flow<List<TaskWithSubTasks>> {
        val (_, endOfDay) = getTodayBounds()
        val cal = Calendar.getInstance().apply {
            timeInMillis = endOfDay
            add(Calendar.DAY_OF_YEAR, days)
        }
        val maxUpcoming = cal.timeInMillis
        return taskDao.getUpcomingTasksFlow(endOfDay, maxUpcoming)
    }

    fun getAllTasks(searchQuery: String = ""): Flow<List<TaskWithSubTasks>> {
        return if (searchQuery.isBlank()) {
            taskDao.getAllTasksFlow()
        } else {
            taskDao.searchTasksFlow(searchQuery.trim())
        }
    }

    fun getTasksForDate(dateMillis: Long): Flow<List<TaskWithSubTasks>> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        return taskDao.getTasksForDateFlow(start, end)
    }

    fun getActiveDueDates(): Flow<List<Long>> {
        return taskDao.getActiveTaskDueDatesFlow()
    }

    fun getActiveDateTasks(): Flow<List<TaskEntity>> {
        return taskDao.getActiveDateTasksFlow()
    }

    fun getDistinctLayers(): Flow<List<String>> {
        return taskDao.getDistinctLayersFlow()
    }

    suspend fun insertTask(
        explanation: String,
        dueDateMillis: Long?,
        dueTimeMinutes: Int?,
        layer: String? = null,
        subTasks: List<String> = emptyList(),
        startDateMillis: Long? = null
    ): Long {
        // Business logic rule: If time is specified without date, default date to Today
        val resolvedDueDate = when {
            dueDateMillis == null && dueTimeMinutes != null -> getTodayBounds().first
            else -> dueDateMillis
        }

        val task = TaskEntity(
            explanation = explanation.trim(),
            dueDateMillis = resolvedDueDate,
            dueTimeMinutes = dueTimeMinutes,
            startDateMillis = startDateMillis,
            layer = layer?.trim()?.ifBlank { null },
            isCompleted = false,
            sortOrder = 0
        )
        val taskId = taskDao.insertTask(task)
        val validSubTasks = subTasks.map { it.trim() }.filter { it.isNotEmpty() }
        if (validSubTasks.isNotEmpty()) {
            val entities = validSubTasks.mapIndexed { index, text ->
                SubTaskEntity(
                    taskId = taskId,
                    explanation = text,
                    isCompleted = false,
                    sortOrder = index
                )
            }
            taskDao.insertSubTasks(entities)
        }
        return taskId
    }

    suspend fun updateTask(
        task: TaskEntity,
        subTasks: List<SubTaskEntity>
    ) {
        val resolvedDueDate = when {
            task.dueDateMillis == null && task.dueTimeMinutes != null -> getTodayBounds().first
            else -> task.dueDateMillis
        }
        taskDao.updateTask(
            task.copy(
                explanation = task.explanation.trim(),
                dueDateMillis = resolvedDueDate,
                layer = task.layer?.trim()?.ifBlank { null }
            )
        )
        taskDao.deleteSubTasksForTask(task.id)
        if (subTasks.isNotEmpty()) {
            taskDao.insertSubTasks(subTasks.mapIndexed { index, sub ->
                sub.copy(id = 0, taskId = task.id, sortOrder = index)
            })
        }
    }

    suspend fun toggleTaskCompletion(taskId: Long, isCompleted: Boolean) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        taskDao.updateTaskCompletion(taskId, isCompleted, completedAt)
    }

    suspend fun toggleSubTaskCompletion(subTaskId: Long, isCompleted: Boolean) {
        taskDao.updateSubTaskCompletion(subTaskId, isCompleted)
    }

    suspend fun getTaskById(taskId: Long): TaskWithSubTasks? = taskDao.getTaskById(taskId)

    fun getTaskByIdSync(taskId: Long): TaskWithSubTasks? = taskDao.getTaskByIdSync(taskId)

    suspend fun getSubTaskById(subTaskId: Long): SubTaskEntity? = taskDao.getSubTaskById(subTaskId)

    fun getPendingTimedTasksSync(): List<TaskWithSubTasks> = taskDao.getPendingTimedTasksSync()

    suspend fun getPendingTimedTasks(): List<TaskWithSubTasks> = taskDao.getPendingTimedTasks()

    suspend fun deleteTask(taskId: Long) {
        taskDao.deleteTaskById(taskId)
    }

    suspend fun deleteSubTask(subTaskId: Long) {
        taskDao.deleteSubTaskById(subTaskId)
    }

    suspend fun addSubTask(taskId: Long, text: String) {
        if (text.isNotBlank()) {
            taskDao.insertSubTask(
                SubTaskEntity(
                    taskId = taskId,
                    explanation = text.trim(),
                    isCompleted = false
                )
            )
        }
    }

    suspend fun reorderTodayTasks(newOrderedTasks: List<TaskEntity>) {
        newOrderedTasks.forEachIndexed { index, task ->
            taskDao.updateTaskSortOrder(task.id, index)
        }
    }

    suspend fun deleteTasksByLayer(layerName: String) {
        taskDao.deleteTasksByLayer(layerName)
    }

    suspend fun renameLayer(oldLayerName: String, newLayerName: String) {
        taskDao.renameLayer(oldLayerName, newLayerName)
    }

    suspend fun clearLayerFromTasks(layerName: String) {
        taskDao.clearLayerFromTasks(layerName)
    }

    suspend fun getTaskCountForLayer(layerName: String): Int {
        return taskDao.getTaskCountForLayer(layerName)
    }

    suspend fun syncProgramTasks(layerName: String, tasks: List<TaskEntity>) {
        taskDao.deleteTasksByLayer(layerName)
        if (tasks.isNotEmpty()) {
            taskDao.insertTasks(tasks)
        }
    }

    private fun getTodayBounds(): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        return Pair(start, end)
    }
}
