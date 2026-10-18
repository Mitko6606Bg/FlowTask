package com.flowtask.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flowtask.app.data.local.AppDatabase
import com.flowtask.app.data.local.entity.ProjectEntity
import com.flowtask.app.data.local.entity.ProjectFolderEntity
import com.flowtask.app.data.local.entity.ProjectNoteEntity
import com.flowtask.app.data.local.entity.ProjectSubTaskEntity
import com.flowtask.app.data.local.entity.ProjectTaskEntity
import com.flowtask.app.data.local.model.ProjectTaskWithSubTasks
import com.flowtask.app.data.repository.ProjectRepository
import androidx.compose.ui.geometry.Rect
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ProjectFilterType {
    ALL, FOLDERS, TASKS, NOTES
}

sealed class MoveItemTarget {
    data class Task(val taskWithSubTasks: ProjectTaskWithSubTasks) : MoveItemTarget()
    data class Note(val note: ProjectNoteEntity) : MoveItemTarget()
    data class Folder(val folder: ProjectFolderEntity) : MoveItemTarget()
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository = ProjectRepository(
        AppDatabase.getInstance(application).projectDao()
    )

    // All Projects
    val projects: StateFlow<List<ProjectEntity>> = repository.getAllProjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Navigation state
    private val _selectedProject = MutableStateFlow<ProjectEntity?>(null)
    val selectedProject: StateFlow<ProjectEntity?> = _selectedProject.asStateFlow()

    private val _folderNavigationStack = MutableStateFlow<List<ProjectFolderEntity>>(emptyList())
    val folderNavigationStack: StateFlow<List<ProjectFolderEntity>> = _folderNavigationStack.asStateFlow()

    val currentFolder: StateFlow<ProjectFolderEntity?> = _folderNavigationStack
        .combine(_selectedProject) { stack, _ -> stack.lastOrNull() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Filter Type (All, Folders, Tasks, Notes)
    private val _filterType = MutableStateFlow(ProjectFilterType.ALL)
    val filterType: StateFlow<ProjectFilterType> = _filterType.asStateFlow()

    fun setFilterType(type: ProjectFilterType) {
        _filterType.value = type
    }

    // Current Level Subfolders
    val currentFolders: StateFlow<List<ProjectFolderEntity>> = combine(
        _selectedProject,
        _folderNavigationStack
    ) { project, stack ->
        Pair(project, stack.lastOrNull())
    }.flatMapLatest { (project, currentFolder) ->
        if (project == null) {
            MutableStateFlow(emptyList())
        } else if (currentFolder == null) {
            repository.getRootFolders(project.id)
        } else {
            repository.getSubFolders(project.id, currentFolder.id)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current Level Tasks
    val currentTasks: StateFlow<List<ProjectTaskWithSubTasks>> = combine(
        _selectedProject,
        _folderNavigationStack
    ) { project, stack ->
        Pair(project, stack.lastOrNull())
    }.flatMapLatest { (project, currentFolder) ->
        if (project == null) {
            MutableStateFlow(emptyList())
        } else {
            repository.getProjectTasks(project.id, currentFolder?.id)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current Level Notes
    val currentNotes: StateFlow<List<ProjectNoteEntity>> = combine(
        _selectedProject,
        _folderNavigationStack
    ) { project, stack ->
        Pair(project, stack.lastOrNull())
    }.flatMapLatest { (project, currentFolder) ->
        if (project == null) {
            MutableStateFlow(emptyList())
        } else {
            repository.getProjectNotes(project.id, currentFolder?.id)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // All folders, tasks, and notes for the selected project (for item counts and moving)
    val allProjectFolders: StateFlow<List<ProjectFolderEntity>> = _selectedProject.flatMapLatest { project ->
        if (project == null) {
            MutableStateFlow(emptyList())
        } else {
            repository.getAllFoldersForProject(project.id)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allProjectTasks: StateFlow<List<ProjectTaskWithSubTasks>> = _selectedProject.flatMapLatest { project ->
        if (project == null) {
            MutableStateFlow(emptyList())
        } else {
            repository.getAllTasksForProject(project.id)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allProjectNotes: StateFlow<List<ProjectNoteEntity>> = _selectedProject.flatMapLatest { project ->
        if (project == null) {
            MutableStateFlow(emptyList())
        } else {
            repository.getAllNotesForProject(project.id)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Maps folderId -> total item count (subfolders + tasks + notes)
    val folderItemCounts: StateFlow<Map<Long, Int>> = combine(
        allProjectFolders,
        allProjectTasks,
        allProjectNotes
    ) { folders, tasks, notes ->
        val counts = mutableMapOf<Long, Int>()
        folders.forEach { f -> counts[f.id] = 0 }
        folders.forEach { f ->
            f.parentFolderId?.let { pId ->
                counts[pId] = (counts[pId] ?: 0) + 1
            }
        }
        tasks.forEach { t ->
            t.task.folderId?.let { fId ->
                counts[fId] = (counts[fId] ?: 0) + 1
            }
        }
        notes.forEach { n ->
            n.folderId?.let { fId ->
                counts[fId] = (counts[fId] ?: 0) + 1
            }
        }
        counts
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // UI Dialog States
    private val _isCreateProjectDialogVisible = MutableStateFlow(false)
    val isCreateProjectDialogVisible: StateFlow<Boolean> = _isCreateProjectDialogVisible.asStateFlow()

    private val _isCreateFolderDialogVisible = MutableStateFlow(false)
    val isCreateFolderDialogVisible: StateFlow<Boolean> = _isCreateFolderDialogVisible.asStateFlow()

    private val _isTaskSheetVisible = MutableStateFlow(false)
    val isTaskSheetVisible: StateFlow<Boolean> = _isTaskSheetVisible.asStateFlow()
    private val _editingProjectTask = MutableStateFlow<ProjectTaskWithSubTasks?>(null)
    val editingProjectTask: StateFlow<ProjectTaskWithSubTasks?> = _editingProjectTask.asStateFlow()

    private val _isNoteSheetVisible = MutableStateFlow(false)
    val isNoteSheetVisible: StateFlow<Boolean> = _isNoteSheetVisible.asStateFlow()
    private val _editingProjectNote = MutableStateFlow<ProjectNoteEntity?>(null)
    val editingProjectNote: StateFlow<ProjectNoteEntity?> = _editingProjectNote.asStateFlow()
    private val _noteOriginBounds = MutableStateFlow<Rect?>(null)
    val noteOriginBounds: StateFlow<Rect?> = _noteOriginBounds.asStateFlow()

    // Move Target
    private val _moveTarget = MutableStateFlow<MoveItemTarget?>(null)
    val moveTarget: StateFlow<MoveItemTarget?> = _moveTarget.asStateFlow()

    // Edit Project
    private val _editingProject = MutableStateFlow<ProjectEntity?>(null)
    val editingProject: StateFlow<ProjectEntity?> = _editingProject.asStateFlow()

    // Hold Action Popup & Details Sheet
    private val _projectActionTarget = MutableStateFlow<ProjectEntity?>(null)
    val projectActionTarget: StateFlow<ProjectEntity?> = _projectActionTarget.asStateFlow()

    private val _viewingProjectDetails = MutableStateFlow<ProjectEntity?>(null)
    val viewingProjectDetails: StateFlow<ProjectEntity?> = _viewingProjectDetails.asStateFlow()

    val viewingProjectTasks: StateFlow<List<ProjectTaskWithSubTasks>> = _viewingProjectDetails.flatMapLatest { project ->
        if (project == null) MutableStateFlow(emptyList()) else repository.getAllTasksForProject(project.id)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val viewingProjectFolders: StateFlow<List<ProjectFolderEntity>> = _viewingProjectDetails.flatMapLatest { project ->
        if (project == null) MutableStateFlow(emptyList()) else repository.getAllFoldersForProject(project.id)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val viewingProjectNotes: StateFlow<List<ProjectNoteEntity>> = _viewingProjectDetails.flatMapLatest { project ->
        if (project == null) MutableStateFlow(emptyList()) else repository.getAllNotesForProject(project.id)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    fun openProjectActionPopup(project: ProjectEntity) {
        _projectActionTarget.value = project
    }

    fun closeProjectActionPopup() {
        _projectActionTarget.value = null
    }

    fun openProjectDetails(project: ProjectEntity) {
        _viewingProjectDetails.value = project
    }

    fun closeProjectDetails() {
        _viewingProjectDetails.value = null
    }

    fun toggleProjectPinned(project: ProjectEntity) {
        viewModelScope.launch {
            val newPinned = !project.isPinned
            repository.toggleProjectPinned(project.id, newPinned)
            if (_selectedProject.value?.id == project.id) {
                _selectedProject.value = _selectedProject.value?.copy(isPinned = newPinned)
            }
            if (_viewingProjectDetails.value?.id == project.id) {
                _viewingProjectDetails.value = _viewingProjectDetails.value?.copy(isPinned = newPinned)
            }
            if (_projectActionTarget.value?.id == project.id) {
                _projectActionTarget.value = _projectActionTarget.value?.copy(isPinned = newPinned)
            }
        }
    }

    fun reorderProjects(orderedProjects: List<ProjectEntity>) {
        viewModelScope.launch {
            val orders = orderedProjects.mapIndexed { index, project -> Pair(project.id, index) }
            repository.updateProjectsSortOrder(orders)
        }
    }

    // Rename Folder
    private val _renamingFolder = MutableStateFlow<ProjectFolderEntity?>(null)
    val renamingFolder: StateFlow<ProjectFolderEntity?> = _renamingFolder.asStateFlow()

    // Global Folders across all projects for search & navigation
    val allFolders: StateFlow<List<ProjectFolderEntity>> = repository.getAllFolders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Search Mode State
    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<ProjectSearchMatch>> = combine(
        projects,
        allFolders,
        _searchQuery
    ) { allProjects, allFoldersList, query ->
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            emptyList()
        } else {
            val foldersByProject = allFoldersList.groupBy { it.projectId }

            allProjects.mapNotNull { project ->
                val projectFolders = foldersByProject[project.id] ?: emptyList()
                val foldersById = projectFolders.associateBy { it.id }

                val isDirectNameMatch = project.name.contains(trimmed, ignoreCase = true) ||
                        project.description.contains(trimmed, ignoreCase = true)

                val matchingFolders = projectFolders.filter { folder ->
                    folder.name.contains(trimmed, ignoreCase = true)
                }.map { matchedFolder ->
                    val path = mutableListOf<ProjectFolderEntity>()
                    var curr: ProjectFolderEntity? = matchedFolder
                    val visited = mutableSetOf<Long>()
                    while (curr != null && visited.add(curr.id)) {
                        path.add(0, curr)
                        curr = curr.parentFolderId?.let { foldersById[it] }
                    }
                    FolderMatch(
                        folder = matchedFolder,
                        pathString = path.joinToString(" > ") { it.name },
                        hierarchy = path
                    )
                }

                if (isDirectNameMatch || matchingFolders.isNotEmpty()) {
                    ProjectSearchMatch(
                        project = project,
                        isDirectNameMatch = isDirectNameMatch,
                        matchingFolders = matchingFolders
                    )
                } else {
                    null
                }
            }.sortedWith(
                compareByDescending<ProjectSearchMatch> { it.project.isPinned }
                    .thenByDescending { it.isDirectNameMatch }
                    .thenBy { it.project.sortOrder }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun openSearch() {
        _selectedProject.value = null
        _folderNavigationStack.value = emptyList()
        _isSearchActive.value = true
    }

    fun closeSearch() {
        _isSearchActive.value = false
        _searchQuery.value = ""
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectProjectAndNavigateToFolder(project: ProjectEntity, folderHierarchy: List<ProjectFolderEntity>) {
        _selectedProject.value = project
        _folderNavigationStack.value = folderHierarchy
        _filterType.value = ProjectFilterType.ALL
        closeSearch()
    }

    // Navigation actions
    fun selectProject(project: ProjectEntity) {
        _selectedProject.value = project
        _folderNavigationStack.value = emptyList()
        _filterType.value = ProjectFilterType.ALL
        closeSearch()
    }

    fun openFolder(folder: ProjectFolderEntity) {
        _folderNavigationStack.value = _folderNavigationStack.value + folder
        _filterType.value = ProjectFilterType.ALL
    }

    fun navigateUpFolder(): Boolean {
        val currentStack = _folderNavigationStack.value
        return if (currentStack.isNotEmpty()) {
            _folderNavigationStack.value = currentStack.dropLast(1)
            true
        } else if (_selectedProject.value != null) {
            _selectedProject.value = null
            true
        } else {
            false
        }
    }

    fun navigateToBreadcrumb(targetFolderIndex: Int) {
        if (targetFolderIndex == -1) {
            _folderNavigationStack.value = emptyList()
        } else {
            _folderNavigationStack.value = _folderNavigationStack.value.take(targetFolderIndex + 1)
        }
    }

    // Projects CRUD
    fun openCreateProjectDialog() {
        _isCreateProjectDialogVisible.value = true
    }

    fun closeCreateProjectDialog() {
        _isCreateProjectDialogVisible.value = false
    }

    fun createProject(name: String, description: String, colorHex: String, iconName: String, isPinned: Boolean = false) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertProject(name, description, colorHex, iconName, isPinned)
            closeCreateProjectDialog()
        }
    }

    fun openEditProjectDialog(project: ProjectEntity) {
        _editingProject.value = project
    }

    fun closeEditProjectDialog() {
        _editingProject.value = null
    }

    fun updateProject(id: Long, name: String, description: String, colorHex: String, iconName: String, isPinned: Boolean? = null) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.updateProjectDetails(id, name, description, colorHex, iconName)
            if (isPinned != null) {
                repository.toggleProjectPinned(id, isPinned)
            }
            if (_selectedProject.value?.id == id) {
                _selectedProject.value = _selectedProject.value?.copy(
                    name = name.trim(),
                    description = description.trim(),
                    colorHex = colorHex,
                    iconName = iconName,
                    isPinned = isPinned ?: _selectedProject.value?.isPinned ?: false
                )
            }
            if (_viewingProjectDetails.value?.id == id) {
                _viewingProjectDetails.value = _viewingProjectDetails.value?.copy(
                    name = name.trim(),
                    description = description.trim(),
                    colorHex = colorHex,
                    iconName = iconName,
                    isPinned = isPinned ?: _viewingProjectDetails.value?.isPinned ?: false
                )
            }
            closeEditProjectDialog()
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            if (_selectedProject.value?.id == projectId) {
                _selectedProject.value = null
                _folderNavigationStack.value = emptyList()
            }
            if (_viewingProjectDetails.value?.id == projectId) {
                _viewingProjectDetails.value = null
            }
            if (_projectActionTarget.value?.id == projectId) {
                _projectActionTarget.value = null
            }
        }
    }

    // Folders CRUD
    fun openCreateFolderDialog() {
        _isCreateFolderDialogVisible.value = true
    }

    fun closeCreateFolderDialog() {
        _isCreateFolderDialogVisible.value = false
    }

    fun createFolder(name: String, colorHex: String) {
        val project = _selectedProject.value ?: return
        if (name.isBlank()) return
        val currentFolderId = _folderNavigationStack.value.lastOrNull()?.id

        viewModelScope.launch {
            repository.insertFolder(
                projectId = project.id,
                parentFolderId = currentFolderId,
                name = name,
                colorHex = colorHex
            )
            closeCreateFolderDialog()
        }
    }

    fun openRenameFolderDialog(folder: ProjectFolderEntity) {
        _renamingFolder.value = folder
    }

    fun closeRenameFolderDialog() {
        _renamingFolder.value = null
    }

    fun renameFolder(id: Long, name: String, colorHex: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.updateFolder(id, name, colorHex)
            _folderNavigationStack.value = _folderNavigationStack.value.map {
                if (it.id == id) it.copy(name = name.trim(), colorHex = colorHex) else it
            }
            closeRenameFolderDialog()
        }
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            repository.deleteFolder(folderId)
            val stack = _folderNavigationStack.value
            val index = stack.indexOfFirst { it.id == folderId }
            if (index != -1) {
                _folderNavigationStack.value = stack.take(index)
            }
        }
    }

    // Move Item
    fun openMoveDialog(target: MoveItemTarget) {
        _moveTarget.value = target
    }

    fun closeMoveDialog() {
        _moveTarget.value = null
    }

    fun moveItemToFolder(targetFolderId: Long?) {
        val target = _moveTarget.value ?: return
        viewModelScope.launch {
            when (target) {
                is MoveItemTarget.Task -> {
                    repository.moveTask(target.taskWithSubTasks.task.id, targetFolderId)
                }
                is MoveItemTarget.Note -> {
                    repository.moveNote(target.note.id, targetFolderId)
                }
                is MoveItemTarget.Folder -> {
                    if (target.folder.id != targetFolderId) {
                        repository.moveFolder(target.folder.id, targetFolderId)
                    }
                }
            }
            closeMoveDialog()
        }
    }

    // Project Tasks CRUD
    fun openCreateTaskSheet() {
        _editingProjectTask.value = null
        _isTaskSheetVisible.value = true
    }

    fun openEditTaskSheet(taskWithSubTasks: ProjectTaskWithSubTasks) {
        _editingProjectTask.value = taskWithSubTasks
        _isTaskSheetVisible.value = true
    }

    fun closeTaskSheet() {
        _isTaskSheetVisible.value = false
        _editingProjectTask.value = null
    }

    fun saveProjectTask(
        id: Long?,
        explanation: String,
        subTasks: List<String>
    ) {
        val project = _selectedProject.value ?: return
        val currentFolderId = _folderNavigationStack.value.lastOrNull()?.id
        if (explanation.isBlank()) return

        viewModelScope.launch {
            if (id == null || id == 0L) {
                repository.insertProjectTask(
                    projectId = project.id,
                    folderId = currentFolderId,
                    explanation = explanation,
                    subTasks = subTasks
                )
            } else {
                val existing = _editingProjectTask.value?.task ?: ProjectTaskEntity(
                    id = id,
                    projectId = project.id,
                    folderId = currentFolderId,
                    explanation = explanation
                )
                val subEntities = subTasks.mapIndexed { index, text ->
                    ProjectSubTaskEntity(
                        projectTaskId = id,
                        explanation = text.trim(),
                        isCompleted = false,
                        sortOrder = index
                    )
                }
                repository.updateProjectTask(existing.copy(explanation = explanation), subEntities)
            }
            closeTaskSheet()
        }
    }

    fun toggleProjectTaskCompletion(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleProjectTaskCompletion(taskId, isCompleted)
        }
    }

    fun toggleProjectSubTaskCompletion(subTaskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleProjectSubTaskCompletion(subTaskId, isCompleted)
        }
    }

    fun addProjectSubTask(taskId: Long, text: String) {
        viewModelScope.launch {
            repository.addProjectSubTask(taskId, text)
        }
    }

    fun deleteProjectTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteProjectTask(taskId)
        }
    }

    fun deleteProjectSubTask(subTaskId: Long) {
        viewModelScope.launch {
            repository.deleteProjectSubTask(subTaskId)
        }
    }

    // Notes CRUD
    fun openCreateNoteSheet(originBounds: Rect? = null) {
        _noteOriginBounds.value = originBounds
        _editingProjectNote.value = null
        _isNoteSheetVisible.value = true
    }

    fun openEditNoteSheet(note: ProjectNoteEntity, originBounds: Rect? = null) {
        _noteOriginBounds.value = originBounds
        _editingProjectNote.value = note
        _isNoteSheetVisible.value = true
    }

    fun closeNoteSheet() {
        _isNoteSheetVisible.value = false
        _editingProjectNote.value = null
        _noteOriginBounds.value = null
    }

    fun saveProjectNote(id: Long?, title: String, content: String, closeOnSave: Boolean = true) {
        val project = _selectedProject.value ?: return
        val currentFolderId = _folderNavigationStack.value.lastOrNull()?.id
        if (content.isBlank() && title.isBlank()) {
            if (closeOnSave) closeNoteSheet()
            return
        }

        viewModelScope.launch {
            val targetId = id ?: _editingProjectNote.value?.id
            if (targetId == null || targetId == 0L) {
                val newId = repository.insertProjectNote(
                    projectId = project.id,
                    folderId = currentFolderId,
                    title = title,
                    content = content
                )
                _editingProjectNote.value = ProjectNoteEntity(
                    id = newId,
                    projectId = project.id,
                    folderId = currentFolderId,
                    title = title,
                    content = content
                )
            } else {
                val existing = _editingProjectNote.value ?: ProjectNoteEntity(
                    id = targetId,
                    projectId = project.id,
                    folderId = currentFolderId,
                    title = title,
                    content = content
                )
                val updated = existing.copy(title = title, content = content)
                repository.updateProjectNote(updated)
                _editingProjectNote.value = updated
            }
            if (closeOnSave) {
                closeNoteSheet()
            }
        }
    }

    fun reorderProjectNotes(reordered: List<ProjectNoteEntity>) {
        viewModelScope.launch {
            repository.reorderProjectNotes(reordered)
        }
    }

    fun deleteProjectNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteProjectNote(noteId)
        }
    }
}

data class FolderMatch(
    val folder: ProjectFolderEntity,
    val pathString: String,
    val hierarchy: List<ProjectFolderEntity>
)

data class ProjectSearchMatch(
    val project: ProjectEntity,
    val isDirectNameMatch: Boolean,
    val matchingFolders: List<FolderMatch>
)
