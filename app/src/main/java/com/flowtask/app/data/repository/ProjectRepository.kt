package com.flowtask.app.data.repository

import com.flowtask.app.data.local.dao.ProjectDao
import com.flowtask.app.data.local.entity.ProjectEntity
import com.flowtask.app.data.local.entity.ProjectFolderEntity
import com.flowtask.app.data.local.entity.ProjectNoteEntity
import com.flowtask.app.data.local.entity.ProjectSubTaskEntity
import com.flowtask.app.data.local.entity.ProjectTaskEntity
import com.flowtask.app.data.local.model.ProjectOverview
import com.flowtask.app.data.local.model.ProjectTaskWithSubTasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class ProjectRepository(private val projectDao: ProjectDao) {

    fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAllProjectsFlow()

    fun getRootFolders(projectId: Long): Flow<List<ProjectFolderEntity>> =
        projectDao.getRootFoldersFlow(projectId)

    fun getSubFolders(projectId: Long, parentFolderId: Long): Flow<List<ProjectFolderEntity>> =
        projectDao.getSubFoldersFlow(projectId, parentFolderId)

    fun getAllFolders(): Flow<List<ProjectFolderEntity>> =
        projectDao.getAllFoldersFlow()

    fun getProjectTasks(projectId: Long, folderId: Long?): Flow<List<ProjectTaskWithSubTasks>> {
        return if (folderId == null) {
            projectDao.getRootProjectTasksFlow(projectId)
        } else {
            projectDao.getFolderProjectTasksFlow(projectId, folderId)
        }
    }

    fun getProjectNotes(projectId: Long, folderId: Long?): Flow<List<ProjectNoteEntity>> {
        return if (folderId == null) {
            projectDao.getRootProjectNotesFlow(projectId)
        } else {
            projectDao.getFolderProjectNotesFlow(projectId, folderId)
        }
    }

    suspend fun insertProject(name: String, description: String, colorHex: String, iconName: String, isPinned: Boolean = false): Long {
        return projectDao.insertProject(
            ProjectEntity(
                name = name.trim(),
                description = description.trim(),
                colorHex = colorHex,
                iconName = iconName,
                isPinned = isPinned
            )
        )
    }

    suspend fun toggleProjectPinned(projectId: Long, isPinned: Boolean) {
        projectDao.updateProjectPinned(projectId, isPinned)
    }

    suspend fun updateProjectsSortOrder(orders: List<Pair<Long, Int>>) {
        withContext(Dispatchers.IO) {
            for ((id, order) in orders) {
                projectDao.updateProjectSortOrder(id, order)
            }
        }
    }

    suspend fun updateProject(project: ProjectEntity) {
        projectDao.updateProject(project.copy(updatedAtMillis = System.currentTimeMillis()))
    }

    suspend fun deleteProject(projectId: Long) {
        projectDao.deleteProjectById(projectId)
    }

    suspend fun insertFolder(projectId: Long, parentFolderId: Long?, name: String, colorHex: String): Long {
        return projectDao.insertFolder(
            ProjectFolderEntity(
                projectId = projectId,
                parentFolderId = parentFolderId,
                name = name.trim(),
                colorHex = colorHex
            )
        )
    }

    suspend fun deleteFolder(folderId: Long) {
        projectDao.deleteFolderById(folderId)
    }

    suspend fun insertProjectTask(
        projectId: Long,
        folderId: Long?,
        explanation: String,
        subTasks: List<String> = emptyList()
    ): Long {
        val task = ProjectTaskEntity(
            projectId = projectId,
            folderId = folderId,
            explanation = explanation.trim(),
            isCompleted = false,
            sortOrder = 0
        )
        val taskId = projectDao.insertProjectTask(task)
        val validSubs = subTasks.map { it.trim() }.filter { it.isNotEmpty() }
        if (validSubs.isNotEmpty()) {
            val entities = validSubs.mapIndexed { index, text ->
                ProjectSubTaskEntity(
                    projectTaskId = taskId,
                    explanation = text,
                    isCompleted = false,
                    sortOrder = index
                )
            }
            projectDao.insertProjectSubTasks(entities)
        }
        return taskId
    }

    suspend fun updateProjectTask(
        task: ProjectTaskEntity,
        subTasks: List<ProjectSubTaskEntity>
    ) {
        projectDao.updateProjectTask(task.copy(explanation = task.explanation.trim()))
        projectDao.deleteSubTasksForProjectTask(task.id)
        if (subTasks.isNotEmpty()) {
            projectDao.insertProjectSubTasks(subTasks.mapIndexed { index, sub ->
                sub.copy(id = 0, projectTaskId = task.id, sortOrder = index)
            })
        }
    }

    suspend fun toggleProjectTaskCompletion(taskId: Long, isCompleted: Boolean) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        projectDao.updateProjectTaskCompletion(taskId, isCompleted, completedAt)
    }

    suspend fun toggleProjectSubTaskCompletion(subTaskId: Long, isCompleted: Boolean) {
        projectDao.updateProjectSubTaskCompletion(subTaskId, isCompleted)
    }

    suspend fun deleteProjectTask(taskId: Long) {
        projectDao.deleteProjectTaskById(taskId)
    }

    suspend fun deleteProjectSubTask(subTaskId: Long) {
        projectDao.deleteProjectSubTaskById(subTaskId)
    }

    suspend fun addProjectSubTask(taskId: Long, text: String) {
        if (text.isNotBlank()) {
            projectDao.insertProjectSubTask(
                ProjectSubTaskEntity(
                    projectTaskId = taskId,
                    explanation = text.trim(),
                    isCompleted = false
                )
            )
        }
    }

    suspend fun insertProjectNote(projectId: Long, folderId: Long?, title: String, content: String): Long {
        val maxOrder = projectDao.getMaxNoteSortOrder(projectId, folderId) ?: -1
        return projectDao.insertProjectNote(
            ProjectNoteEntity(
                projectId = projectId,
                folderId = folderId,
                title = title.trim(),
                content = content.trim(),
                sortOrder = maxOrder + 1,
                createdAtMillis = System.currentTimeMillis(),
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateProjectNote(note: ProjectNoteEntity) {
        projectDao.updateProjectNote(
            note.copy(
                title = note.title.trim(),
                content = note.content.trim(),
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun reorderProjectNotes(notes: List<ProjectNoteEntity>) {
        notes.forEachIndexed { index, note ->
            projectDao.updateProjectNoteSortOrder(note.id, index)
        }
    }

    suspend fun deleteProjectNote(noteId: Long) {
        projectDao.deleteProjectNoteById(noteId)
    }

    suspend fun updateProjectDetails(projectId: Long, name: String, description: String, colorHex: String, iconName: String) {
        projectDao.updateProjectDetails(projectId, name.trim(), description.trim(), colorHex, iconName, System.currentTimeMillis())
    }

    suspend fun updateFolder(folderId: Long, name: String, colorHex: String) {
        projectDao.updateFolderNameAndColor(folderId, name.trim(), colorHex)
    }

    suspend fun moveFolder(folderId: Long, parentFolderId: Long?) {
        projectDao.updateProjectFolderParent(folderId, parentFolderId)
    }

    suspend fun moveTask(taskId: Long, folderId: Long?) {
        projectDao.updateProjectTaskFolder(taskId, folderId)
    }

    suspend fun moveNote(noteId: Long, folderId: Long?) {
        projectDao.updateProjectNoteFolder(noteId, folderId)
    }

    fun getAllFoldersForProject(projectId: Long): Flow<List<ProjectFolderEntity>> =
        projectDao.getAllFoldersForProjectFlow(projectId)

    fun getAllTasksForProject(projectId: Long): Flow<List<ProjectTaskWithSubTasks>> =
        projectDao.getAllProjectTasksFlow(projectId)

    fun getAllNotesForProject(projectId: Long): Flow<List<ProjectNoteEntity>> =
        projectDao.getAllProjectNotesFlow(projectId)
}
