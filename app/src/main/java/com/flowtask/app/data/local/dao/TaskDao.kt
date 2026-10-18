package com.flowtask.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.flowtask.app.data.local.entity.SubTaskEntity
import com.flowtask.app.data.local.entity.TaskEntity
import com.flowtask.app.data.local.model.TaskWithSubTasks
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface TaskDao {

    @Transaction
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueDateMillis ASC, dueTimeMinutes ASC, sortOrder ASC, createdAtMillis DESC")
    fun getAllTasksFlow(): Flow<List<TaskWithSubTasks>>

    @Transaction
    @Query("""
        SELECT * FROM tasks 
        WHERE (
            (startDateMillis IS NULL AND dueDateMillis >= :startOfDayMillis AND dueDateMillis <= :endOfDayMillis)
            OR (startDateMillis IS NOT NULL AND startDateMillis <= :endOfDayMillis AND dueDateMillis >= :startOfDayMillis)
            OR (dueDateMillis IS NULL AND isCompleted = 0)
        )
        ORDER BY isCompleted ASC, 
                 CASE WHEN dueTimeMinutes IS NOT NULL THEN 0 ELSE 1 END,
                 dueTimeMinutes ASC, 
                 sortOrder ASC, 
                 createdAtMillis ASC
    """)
    fun getTodayTasksFlow(startOfDayMillis: Long, endOfDayMillis: Long): Flow<List<TaskWithSubTasks>>

    @Transaction
    @Query("""
        SELECT * FROM tasks 
        WHERE (
            (startDateMillis IS NULL AND dueDateMillis >= :startOfDayMillis AND dueDateMillis <= :endOfDayMillis)
            OR (startDateMillis IS NOT NULL AND startDateMillis <= :endOfDayMillis AND dueDateMillis >= :startOfDayMillis)
            OR (dueDateMillis IS NULL AND isCompleted = 0)
        )
        ORDER BY isCompleted ASC, 
                 CASE WHEN dueTimeMinutes IS NOT NULL THEN 0 ELSE 1 END,
                 dueTimeMinutes ASC, 
                 sortOrder ASC, 
                 createdAtMillis ASC
    """)
    fun getTodayTasksSync(startOfDayMillis: Long, endOfDayMillis: Long): List<TaskWithSubTasks>

    @Transaction
    @Query("""
        SELECT * FROM tasks 
        WHERE (
            (startDateMillis IS NOT NULL AND startDateMillis > :endOfDayMillis AND startDateMillis <= :maxUpcomingMillis)
            OR (startDateMillis IS NULL AND dueDateMillis > :endOfDayMillis AND dueDateMillis <= :maxUpcomingMillis)
        )
        ORDER BY isCompleted ASC, 
                 COALESCE(startDateMillis, dueDateMillis) ASC, 
                 dueTimeMinutes ASC, 
                 createdAtMillis ASC
    """)
    fun getUpcomingTasksFlow(endOfDayMillis: Long, maxUpcomingMillis: Long): Flow<List<TaskWithSubTasks>>

    @Transaction
    @Query("""
        SELECT * FROM tasks 
        WHERE (
            (startDateMillis IS NULL AND dueDateMillis >= :startOfDayMillis AND dueDateMillis <= :endOfDayMillis)
            OR (startDateMillis IS NOT NULL AND startDateMillis <= :endOfDayMillis AND dueDateMillis >= :startOfDayMillis)
        )
        ORDER BY isCompleted ASC, dueTimeMinutes ASC, sortOrder ASC, createdAtMillis ASC
    """)
    fun getTasksForDateFlow(startOfDayMillis: Long, endOfDayMillis: Long): Flow<List<TaskWithSubTasks>>

    @Transaction
    @Query("""
        SELECT * FROM tasks 
        WHERE explanation LIKE '%' || :query || '%'
        ORDER BY isCompleted ASC, COALESCE(startDateMillis, dueDateMillis) ASC, createdAtMillis DESC
    """)
    fun searchTasksFlow(query: String): Flow<List<TaskWithSubTasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Long): TaskWithSubTasks?

    @Query("SELECT DISTINCT dueDateMillis FROM tasks WHERE dueDateMillis IS NOT NULL AND isCompleted = 0")
    fun getActiveTaskDueDatesFlow(): Flow<List<Long>>

    @Query("SELECT * FROM tasks WHERE (dueDateMillis IS NOT NULL OR startDateMillis IS NOT NULL) AND isCompleted = 0")
    fun getActiveDateTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT DISTINCT layer FROM tasks WHERE layer IS NOT NULL AND TRIM(layer) != '' ORDER BY layer ASC")
    fun getDistinctLayersFlow(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>): List<Long>

    @Query("DELETE FROM tasks WHERE TRIM(layer) = TRIM(:layerName) COLLATE NOCASE")
    suspend fun deleteTasksByLayer(layerName: String): Int

    @Query("UPDATE tasks SET layer = :newLayerName WHERE TRIM(layer) = TRIM(:oldLayerName) COLLATE NOCASE")
    suspend fun renameLayer(oldLayerName: String, newLayerName: String): Int

    @Query("UPDATE tasks SET layer = NULL WHERE TRIM(layer) = TRIM(:layerName) COLLATE NOCASE")
    suspend fun clearLayerFromTasks(layerName: String): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE TRIM(layer) = TRIM(:layerName) COLLATE NOCASE")
    suspend fun getTaskCountForLayer(layerName: String): Int

    @Update
    suspend fun updateTask(task: TaskEntity): Int

    @Delete
    suspend fun deleteTask(task: TaskEntity): Int

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long): Int

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAtMillis = :completedAt WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: Long, isCompleted: Boolean, completedAt: Long?): Int

    @Query("UPDATE tasks SET sortOrder = :order WHERE id = :taskId")
    suspend fun updateTaskSortOrder(taskId: Long, order: Int): Int

    // Subtasks
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(subTask: SubTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTasks(subTasks: List<SubTaskEntity>): List<Long>

    @Update
    suspend fun updateSubTask(subTask: SubTaskEntity): Int

    @Delete
    suspend fun deleteSubTask(subTask: SubTaskEntity): Int

    @Query("DELETE FROM subtasks WHERE id = :subTaskId")
    suspend fun deleteSubTaskById(subTaskId: Long): Int

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubTasksForTask(taskId: Long): Int

    @Query("UPDATE subtasks SET isCompleted = :isCompleted WHERE id = :subTaskId")
    suspend fun updateSubTaskCompletion(subTaskId: Long, isCompleted: Boolean): Int

    @Query("SELECT * FROM subtasks WHERE id = :subTaskId")
    suspend fun getSubTaskById(subTaskId: Long): SubTaskEntity?

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId")
    suspend fun getSubTasksForTask(taskId: Long): List<SubTaskEntity>

    @Query("UPDATE subtasks SET isCompleted = :isCompleted WHERE taskId = :taskId")
    suspend fun updateAllSubTasksCompletionForTask(taskId: Long, isCompleted: Boolean): Int
}
