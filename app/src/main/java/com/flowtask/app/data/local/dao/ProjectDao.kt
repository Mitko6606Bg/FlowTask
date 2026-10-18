package com.flowtask.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.flowtask.app.data.local.entity.ProjectEntity
import com.flowtask.app.data.local.entity.ProjectFolderEntity
import com.flowtask.app.data.local.entity.ProjectNoteEntity
import com.flowtask.app.data.local.entity.ProjectSubTaskEntity
import com.flowtask.app.data.local.entity.ProjectTaskEntity
import com.flowtask.app.data.local.model.ProjectTaskWithSubTasks
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface ProjectDao {

    // Projects CRUD
    @Query("SELECT * FROM projects ORDER BY isPinned DESC, sortOrder ASC, createdAtMillis DESC")
    fun getAllProjectsFlow(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    suspend fun getProjectById(projectId: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity): Int

    @Delete
    suspend fun deleteProject(project: ProjectEntity): Int

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: Long): Int

    @Query("UPDATE projects SET isPinned = :isPinned WHERE id = :projectId")
    suspend fun updateProjectPinned(projectId: Long, isPinned: Boolean): Int

    @Query("UPDATE projects SET sortOrder = :sortOrder WHERE id = :projectId")
    suspend fun updateProjectSortOrder(projectId: Long, sortOrder: Int): Int

    @Query("UPDATE projects SET name = :name, description = :description, colorHex = :colorHex, iconName = :iconName, updatedAtMillis = :updatedAt WHERE id = :projectId")
    suspend fun updateProjectDetails(projectId: Long, name: String, description: String, colorHex: String, iconName: String, updatedAt: Long): Int

    // Folders CRUD
    @Query("SELECT * FROM project_folders WHERE projectId = :projectId AND parentFolderId IS NULL ORDER BY sortOrder ASC, createdAtMillis ASC")
    fun getRootFoldersFlow(projectId: Long): Flow<List<ProjectFolderEntity>>

    @Query("SELECT * FROM project_folders WHERE projectId = :projectId AND parentFolderId = :parentFolderId ORDER BY sortOrder ASC, createdAtMillis ASC")
    fun getSubFoldersFlow(projectId: Long, parentFolderId: Long): Flow<List<ProjectFolderEntity>>

    @Query("SELECT * FROM project_folders WHERE id = :folderId LIMIT 1")
    suspend fun getFolderById(folderId: Long): ProjectFolderEntity?

    @Query("SELECT * FROM project_folders WHERE projectId = :projectId")
    fun getAllFoldersForProjectFlow(projectId: Long): Flow<List<ProjectFolderEntity>>

    @Query("SELECT * FROM project_folders ORDER BY sortOrder ASC, createdAtMillis ASC")
    fun getAllFoldersFlow(): Flow<List<ProjectFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: ProjectFolderEntity): Long

    @Update
    suspend fun updateFolder(folder: ProjectFolderEntity): Int

    @Delete
    suspend fun deleteFolder(folder: ProjectFolderEntity): Int

    @Query("DELETE FROM project_folders WHERE id = :folderId")
    suspend fun deleteFolderById(folderId: Long): Int

    @Query("UPDATE project_folders SET name = :name, colorHex = :colorHex WHERE id = :folderId")
    suspend fun updateFolderNameAndColor(folderId: Long, name: String, colorHex: String): Int

    @Query("UPDATE project_folders SET parentFolderId = :parentFolderId WHERE id = :folderId")
    suspend fun updateProjectFolderParent(folderId: Long, parentFolderId: Long?): Int

    // Project Tasks CRUD
    @Transaction
    @Query("SELECT * FROM project_tasks WHERE projectId = :projectId AND folderId IS NULL ORDER BY isCompleted ASC, sortOrder ASC, createdAtMillis ASC")
    fun getRootProjectTasksFlow(projectId: Long): Flow<List<ProjectTaskWithSubTasks>>

    @Transaction
    @Query("SELECT * FROM project_tasks WHERE projectId = :projectId AND folderId = :folderId ORDER BY isCompleted ASC, sortOrder ASC, createdAtMillis ASC")
    fun getFolderProjectTasksFlow(projectId: Long, folderId: Long): Flow<List<ProjectTaskWithSubTasks>>

    @Transaction
    @Query("SELECT * FROM project_tasks WHERE projectId = :projectId ORDER BY isCompleted ASC, sortOrder ASC, createdAtMillis ASC")
    fun getAllProjectTasksFlow(projectId: Long): Flow<List<ProjectTaskWithSubTasks>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectTask(task: ProjectTaskEntity): Long

    @Update
    suspend fun updateProjectTask(task: ProjectTaskEntity): Int

    @Delete
    suspend fun deleteProjectTask(task: ProjectTaskEntity): Int

    @Query("DELETE FROM project_tasks WHERE id = :taskId")
    suspend fun deleteProjectTaskById(taskId: Long): Int

    @Query("UPDATE project_tasks SET isCompleted = :isCompleted, completedAtMillis = :completedAt WHERE id = :taskId")
    suspend fun updateProjectTaskCompletion(taskId: Long, isCompleted: Boolean, completedAt: Long?): Int

    @Query("UPDATE project_tasks SET sortOrder = :order WHERE id = :taskId")
    suspend fun updateProjectTaskSortOrder(taskId: Long, order: Int): Int

    @Query("UPDATE project_tasks SET folderId = :folderId WHERE id = :taskId")
    suspend fun updateProjectTaskFolder(taskId: Long, folderId: Long?): Int

    // Project SubTasks
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectSubTask(subTask: ProjectSubTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectSubTasks(subTasks: List<ProjectSubTaskEntity>): List<Long>

    @Update
    suspend fun updateProjectSubTask(subTask: ProjectSubTaskEntity): Int

    @Delete
    suspend fun deleteProjectSubTask(subTask: ProjectSubTaskEntity): Int

    @Query("DELETE FROM project_subtasks WHERE id = :subTaskId")
    suspend fun deleteProjectSubTaskById(subTaskId: Long): Int

    @Query("DELETE FROM project_subtasks WHERE projectTaskId = :taskId")
    suspend fun deleteSubTasksForProjectTask(taskId: Long): Int

    @Query("UPDATE project_subtasks SET isCompleted = :isCompleted WHERE id = :subTaskId")
    suspend fun updateProjectSubTaskCompletion(subTaskId: Long, isCompleted: Boolean): Int

    @Query("SELECT * FROM project_subtasks WHERE id = :subTaskId")
    suspend fun getProjectSubTaskById(subTaskId: Long): ProjectSubTaskEntity?

    @Query("SELECT * FROM project_subtasks WHERE projectTaskId = :taskId")
    suspend fun getSubTasksForProjectTask(taskId: Long): List<ProjectSubTaskEntity>

    @Query("UPDATE project_subtasks SET isCompleted = :isCompleted WHERE projectTaskId = :taskId")
    suspend fun updateAllSubTasksCompletionForProjectTask(taskId: Long, isCompleted: Boolean): Int

    // Project Notes CRUD
    @Query("SELECT * FROM project_notes WHERE projectId = :projectId AND folderId IS NULL ORDER BY sortOrder ASC, createdAtMillis ASC, id ASC")
    fun getRootProjectNotesFlow(projectId: Long): Flow<List<ProjectNoteEntity>>

    @Query("SELECT * FROM project_notes WHERE projectId = :projectId AND folderId = :folderId ORDER BY sortOrder ASC, createdAtMillis ASC, id ASC")
    fun getFolderProjectNotesFlow(projectId: Long, folderId: Long): Flow<List<ProjectNoteEntity>>

    @Query("SELECT * FROM project_notes WHERE projectId = :projectId ORDER BY sortOrder ASC, createdAtMillis ASC, id ASC")
    fun getAllProjectNotesFlow(projectId: Long): Flow<List<ProjectNoteEntity>>

    @Query("SELECT MAX(sortOrder) FROM project_notes WHERE projectId = :projectId AND ((folderId IS NULL AND :folderId IS NULL) OR (folderId = :folderId))")
    suspend fun getMaxNoteSortOrder(projectId: Long, folderId: Long?): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectNote(note: ProjectNoteEntity): Long

    @Update
    suspend fun updateProjectNote(note: ProjectNoteEntity): Int

    @Update
    suspend fun updateProjectNotes(notes: List<ProjectNoteEntity>): Int

    @Delete
    suspend fun deleteProjectNote(note: ProjectNoteEntity): Int

    @Query("DELETE FROM project_notes WHERE id = :noteId")
    suspend fun deleteProjectNoteById(noteId: Long): Int

    @Query("UPDATE project_notes SET folderId = :folderId WHERE id = :noteId")
    suspend fun updateProjectNoteFolder(noteId: Long, folderId: Long?): Int

    @Query("UPDATE project_notes SET sortOrder = :order WHERE id = :noteId")
    suspend fun updateProjectNoteSortOrder(noteId: Long, order: Int): Int
}
