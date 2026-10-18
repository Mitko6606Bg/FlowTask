package com.flowtask.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flowtask.app.data.local.AppDatabase
import com.flowtask.app.data.local.entity.SubTaskEntity
import com.flowtask.app.data.local.entity.TaskEntity
import com.flowtask.app.data.local.model.LayerProgram
import com.flowtask.app.data.local.model.ScheduleItem
import com.flowtask.app.data.local.model.SemesterConfig
import com.flowtask.app.data.local.model.TaskWithSubTasks
import com.flowtask.app.data.repository.SemesterRepository
import com.flowtask.app.data.repository.SettingsRepository
import com.flowtask.app.data.repository.TaskRepository
import com.flowtask.app.util.DateTimeUtils
import com.flowtask.app.util.SemesterUtils
import java.util.Calendar
import com.flowtask.app.widget.TodayWidgetProvider
import com.flowtask.app.widget.ProgramRoutinesWidgetProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository = TaskRepository(
        AppDatabase.getInstance(application).taskDao()
    )

    private val semesterRepository: SemesterRepository = SemesterRepository(application)
    val semesterConfig: StateFlow<SemesterConfig> = semesterRepository.semesterConfig

    private val settingsRepository: SettingsRepository = SettingsRepository(application)
    val allTasksViewMode: StateFlow<String> = settingsRepository.allTasksViewMode
    val programTasksEverywhere: StateFlow<Boolean> = settingsRepository.programTasksEverywhere

    fun setAllTasksViewMode(mode: String) {
        settingsRepository.setAllTasksViewMode(mode)
    }

    // Trigger to scroll Smart Calendar to current month / today
    private val _scrollToTodayCalendarTrigger = MutableStateFlow(0)
    val scrollToTodayCalendarTrigger: StateFlow<Int> = _scrollToTodayCalendarTrigger.asStateFlow()

    fun triggerScrollToTodayCalendar() {
        _scrollToTodayCalendarTrigger.value += 1
    }

    private val _isSemesterSheetVisible = MutableStateFlow(false)
    val isSemesterSheetVisible: StateFlow<Boolean> = _isSemesterSheetVisible.asStateFlow()

    // Active Selected Layer (null means "All Layers")
    private val _selectedLayer = MutableStateFlow<String?>(null)
    val selectedLayer: StateFlow<String?> = _selectedLayer.asStateFlow()

    // Distinct Available Layers (Tasks + Programs combined, ensuring complete visibility across all selectors)
    val availableLayers: StateFlow<List<String>> = repository.getDistinctLayers()
        .combine(semesterRepository.layerPrograms) { dbLayers, programs ->
            val set = linkedSetOf<String>()
            set.addAll(dbLayers.map { it.trim() }.filter { it.isNotBlank() })
            for (p in programs) {
                if (p.layerName.isNotBlank()) {
                    set.add(p.layerName.trim())
                }
            }
            set.toList().sorted()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val calendarFiltersExpanded: StateFlow<Boolean> = settingsRepository.calendarFiltersExpanded

    fun toggleCalendarFiltersExpanded() {
        settingsRepository.setCalendarFiltersExpanded(!calendarFiltersExpanded.value)
    }

    fun setCalendarFiltersExpanded(expanded: Boolean) {
        settingsRepository.setCalendarFiltersExpanded(expanded)
    }

    // Today Tasks Flow (Filtered by Selected Layer)
    val todayTasks: StateFlow<List<TaskWithSubTasks>> = repository.getTodayTasks()
        .combine(_selectedLayer) { tasks, layer ->
            if (layer == null) tasks
            else tasks.filter { it.task.layer?.equals(layer, ignoreCase = true) == true }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Upcoming Tasks Flow (Next 10 days, Filtered by Selected Layer)
    val upcomingTasks: StateFlow<List<TaskWithSubTasks>> = repository.getUpcomingTasks(10)
        .combine(_selectedLayer) { tasks, layer ->
            if (layer == null) tasks
            else tasks.filter { it.task.layer?.equals(layer, ignoreCase = true) == true }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // All Tasks (Filtered by search query & Selected Layer)
    val allTasks: StateFlow<List<TaskWithSubTasks>> = _searchQuery
        .flatMapLatest { query -> repository.getAllTasks(query) }
        .combine(_selectedLayer) { tasks, layer ->
            if (layer == null) tasks
            else tasks.filter { it.task.layer?.equals(layer, ignoreCase = true) == true }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Set of distinct dates with active tasks for calendar highlighting
    val taskDatesWithActivity: StateFlow<Set<Long>> = repository.getActiveDateTasks()
        .map { tasks ->
            val set = mutableSetOf<Long>()
            for (task in tasks) {
                val s = task.startDateMillis?.let { DateTimeUtils.getStartOfDay(it) }
                val e = task.dueDateMillis?.let { DateTimeUtils.getStartOfDay(it) }
                if (s != null && e != null && e >= s) {
                    val cal = Calendar.getInstance().apply { timeInMillis = s }
                    while (cal.timeInMillis <= e) {
                        set.add(cal.timeInMillis)
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                    }
                } else if (e != null) {
                    set.add(e)
                } else if (s != null) {
                    set.add(s)
                }
            }
            set
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    // Selected date in Smart Calendar
    private val _selectedCalendarDate = MutableStateFlow<Long?>(DateTimeUtils.getStartOfDay())
    val selectedCalendarDate: StateFlow<Long?> = _selectedCalendarDate.asStateFlow()

    // Tasks for selected calendar date (Filtered by Selected Layer)
    val selectedCalendarDateTasks: StateFlow<List<TaskWithSubTasks>> = _selectedCalendarDate
        .flatMapLatest { date ->
            if (date != null) {
                repository.getTasksForDate(date)
            } else {
                MutableStateFlow(emptyList())
            }
        }
        .combine(_selectedLayer) { tasks, layer ->
            if (layer == null) tasks
            else tasks.filter { it.task.layer?.equals(layer, ignoreCase = true) == true }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI State for Editing/Creating Task Sheet
    private val _editingTask = MutableStateFlow<TaskWithSubTasks?>(null)
    val editingTask: StateFlow<TaskWithSubTasks?> = _editingTask.asStateFlow()

    private val _isAddOrEditSheetVisible = MutableStateFlow(false)
    val isAddOrEditSheetVisible: StateFlow<Boolean> = _isAddOrEditSheetVisible.asStateFlow()

    private val _defaultDateForNewTask = MutableStateFlow<Long?>(null)
    val defaultDateForNewTask: StateFlow<Long?> = _defaultDateForNewTask.asStateFlow()

    private val _defaultStartDateForNewTask = MutableStateFlow<Long?>(null)
    val defaultStartDateForNewTask: StateFlow<Long?> = _defaultStartDateForNewTask.asStateFlow()

    fun selectLayer(layer: String?) {
        _selectedLayer.value = layer
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCalendarDate(dateMillis: Long) {
        _selectedCalendarDate.value = DateTimeUtils.getStartOfDay(dateMillis)
    }

    fun openAddTask(presetDateMillis: Long? = null, presetStartDateMillis: Long? = null) {
        _editingTask.value = null
        _defaultDateForNewTask.value = presetDateMillis
        _defaultStartDateForNewTask.value = presetStartDateMillis
        _isAddOrEditSheetVisible.value = true
    }

    fun openEditTask(taskWithSubTasks: TaskWithSubTasks) {
        _editingTask.value = taskWithSubTasks
        _defaultDateForNewTask.value = taskWithSubTasks.task.dueDateMillis
        _defaultStartDateForNewTask.value = taskWithSubTasks.task.startDateMillis
        _isAddOrEditSheetVisible.value = true
    }

    fun closeTaskSheet() {
        _isAddOrEditSheetVisible.value = false
        _editingTask.value = null
        _defaultDateForNewTask.value = null
        _defaultStartDateForNewTask.value = null
    }

    fun saveTask(
        id: Long?,
        explanation: String,
        dueDateMillis: Long?,
        dueTimeMinutes: Int?,
        layer: String?,
        subTasks: List<String>,
        startDateMillis: Long? = null
    ) {
        if (explanation.isBlank()) return

        viewModelScope.launch {
            if (id == null || id == 0L) {
                repository.insertTask(
                    explanation = explanation,
                    dueDateMillis = dueDateMillis,
                    dueTimeMinutes = dueTimeMinutes,
                    layer = layer,
                    subTasks = subTasks,
                    startDateMillis = startDateMillis
                )
            } else {
                val existing = _editingTask.value?.task ?: TaskEntity(id = id, explanation = explanation)
                val updatedTask = existing.copy(
                    explanation = explanation.trim(),
                    dueDateMillis = dueDateMillis,
                    dueTimeMinutes = dueTimeMinutes,
                    startDateMillis = startDateMillis,
                    layer = layer?.trim()?.ifBlank { null }
                )
                val subTaskEntities = subTasks.mapIndexed { index, text ->
                    SubTaskEntity(
                        taskId = id,
                        explanation = text.trim(),
                        isCompleted = false,
                        sortOrder = index
                    )
                }
                repository.updateTask(updatedTask, subTaskEntities)
            }
            TodayWidgetProvider.updateAllWidgets(getApplication())
            closeTaskSheet()
        }
    }

    fun toggleTaskCompletion(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(taskId, isCompleted)
            TodayWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun toggleSubTaskCompletion(subTaskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleSubTaskCompletion(subTaskId, isCompleted)
            TodayWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun addSubTask(taskId: Long, text: String) {
        viewModelScope.launch {
            repository.addSubTask(taskId, text)
            TodayWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            TodayWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun deleteSubTask(subTaskId: Long) {
        viewModelScope.launch {
            repository.deleteSubTask(subTaskId)
        }
    }

    fun moveTaskUp(task: TaskEntity, currentList: List<TaskWithSubTasks>) {
        val nonTimedTasks = currentList.map { it.task }.filter { it.dueTimeMinutes == null }
        val index = nonTimedTasks.indexOfFirst { it.id == task.id }
        if (index > 0) {
            val mutable = nonTimedTasks.toMutableList()
            val temp = mutable[index]
            mutable[index] = mutable[index - 1]
            mutable[index - 1] = temp
            viewModelScope.launch {
                repository.reorderTodayTasks(mutable)
            }
        }
    }

    fun moveTaskDown(task: TaskEntity, currentList: List<TaskWithSubTasks>) {
        val nonTimedTasks = currentList.map { it.task }.filter { it.dueTimeMinutes == null }
        val index = nonTimedTasks.indexOfFirst { it.id == task.id }
        if (index >= 0 && index < nonTimedTasks.size - 1) {
            val mutable = nonTimedTasks.toMutableList()
            val temp = mutable[index]
            mutable[index] = mutable[index + 1]
            mutable[index + 1] = temp
            viewModelScope.launch {
                repository.reorderTodayTasks(mutable)
            }
        }
    }

    private val _targetProgramDetailsId = MutableStateFlow<String?>(null)
    val targetProgramDetailsId: StateFlow<String?> = _targetProgramDetailsId.asStateFlow()

    fun openSemesterSheet() {
        _targetProgramDetailsId.value = null
        _isSemesterSheetVisible.value = true
    }

    fun openProgramDetails(programId: String) {
        _targetProgramDetailsId.value = programId
        _isSemesterSheetVisible.value = true
    }

    fun clearTargetProgramDetails() {
        _targetProgramDetailsId.value = null
    }

    fun closeSemesterSheet() {
        _isSemesterSheetVisible.value = false
        _targetProgramDetailsId.value = null
    }

    fun setSemesterEnabled(enabled: Boolean) {
        semesterRepository.setEnabled(enabled)
    }

    fun updateSemesterDates(startDateMillis: Long?, endDateMillis: Long?) {
        semesterRepository.updateDates(startDateMillis, endDateMillis)
    }

    fun setProgramLayerName(layerName: String) {
        semesterRepository.setProgramLayerName(layerName)
    }

    fun addScheduleItem(item: ScheduleItem) {
        semesterRepository.addScheduleItem(item)
        ProgramRoutinesWidgetProvider.updateAllWidgets(getApplication())
    }

    fun removeScheduleItem(itemId: String) {
        semesterRepository.removeScheduleItem(itemId)
        ProgramRoutinesWidgetProvider.updateAllWidgets(getApplication())
    }

    fun updateScheduleItems(items: List<ScheduleItem>) {
        semesterRepository.updateScheduleItems(items)
        ProgramRoutinesWidgetProvider.updateAllWidgets(getApplication())
    }

    fun syncProgramToCalendar() {
        if (!settingsRepository.programTasksEverywhere.value) {
            return
        }
        viewModelScope.launch {
            val config = semesterRepository.semesterConfig.value
            val tasks = SemesterUtils.generateSemesterProgramTasks(config)
            repository.syncProgramTasks(config.programLayerName, tasks)
        }
    }

    fun clearProgramTasks() {
        viewModelScope.launch {
            val config = semesterRepository.semesterConfig.value
            repository.deleteTasksByLayer(config.programLayerName)
        }
    }

    // =========================================================================
    // Multi-Layer Programs Flow & Actions
    // =========================================================================

    val layerPrograms: StateFlow<List<LayerProgram>> = semesterRepository.layerPrograms

    fun saveLayerProgram(program: LayerProgram) {
        semesterRepository.saveLayerProgram(program)
        viewModelScope.launch {
            if (program.isEnabled && program.showTasksEverywhere) {
                val tasks = SemesterUtils.generateProgramTasks(program)
                repository.syncProgramTasks(program.layerName, tasks)
            } else {
                repository.deleteTasksByLayer(program.layerName)
            }
            TodayWidgetProvider.updateAllWidgets(getApplication())
            ProgramRoutinesWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun deleteLayerProgram(programId: String) {
        val target = semesterRepository.layerPrograms.value.find { it.id == programId }
        semesterRepository.deleteLayerProgram(programId)
        if (target != null) {
            viewModelScope.launch {
                repository.deleteTasksByLayer(target.layerName)
                TodayWidgetProvider.updateAllWidgets(getApplication())
                ProgramRoutinesWidgetProvider.updateAllWidgets(getApplication())
            }
        }
    }

    fun toggleProgramEnabled(programId: String, isEnabled: Boolean) {
        semesterRepository.toggleProgramEnabled(programId, isEnabled)
        val target = semesterRepository.layerPrograms.value.find { it.id == programId }
        if (target != null) {
            viewModelScope.launch {
                if (isEnabled && target.showTasksEverywhere) {
                    val tasks = SemesterUtils.generateProgramTasks(target.copy(isEnabled = isEnabled))
                    repository.syncProgramTasks(target.layerName, tasks)
                } else {
                    repository.deleteTasksByLayer(target.layerName)
                }
                TodayWidgetProvider.updateAllWidgets(getApplication())
                ProgramRoutinesWidgetProvider.updateAllWidgets(getApplication())
            }
        }
    }

    fun setProgramTasksEverywhere(everywhere: Boolean) {
        settingsRepository.setProgramTasksEverywhere(everywhere)
        viewModelScope.launch {
            if (everywhere) {
                // Mode A: synthesize and sync tasks for all enabled programs
                val enabledPrograms = semesterRepository.layerPrograms.value.filter { it.isEnabled && it.showTasksEverywhere }
                for (program in enabledPrograms) {
                    val tasks = SemesterUtils.generateProgramTasks(program)
                    repository.syncProgramTasks(program.layerName, tasks)
                }
            } else {
                // Mode B: purge synthesized routine tasks from SQLite
                val allPrograms = semesterRepository.layerPrograms.value
                for (program in allPrograms) {
                    repository.deleteTasksByLayer(program.layerName)
                }
            }
            TodayWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun syncLayerProgramToCalendar(program: LayerProgram) {
        if (!program.showTasksEverywhere) {
            return
        }
        viewModelScope.launch {
            val tasks = SemesterUtils.generateProgramTasks(program)
            repository.syncProgramTasks(program.layerName, tasks)
            TodayWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun clearLayerProgramFromCalendar(layerName: String) {
        viewModelScope.launch {
            repository.deleteTasksByLayer(layerName)
        }
    }

    fun clearLayerProgramFromCalendar(program: LayerProgram) {
        clearLayerProgramFromCalendar(program.layerName)
    }

    private val _selectedCalendarProgramId = MutableStateFlow<String?>(null)
    val selectedCalendarProgramId: StateFlow<String?> = _selectedCalendarProgramId.asStateFlow()
    private var lastSelectedProgramId: String? = null

    fun selectCalendarProgram(programId: String?) {
        if (programId != null && programId != "NONE") {
            lastSelectedProgramId = programId
        }
        _selectedCalendarProgramId.value = programId
    }

    fun getLastSelectedProgramId(): String? = lastSelectedProgramId

    // Layer Editing Modal State
    private val _editingLayer = MutableStateFlow<String?>(null)
    val editingLayer: StateFlow<String?> = _editingLayer.asStateFlow()

    private val _editingLayerTaskCount = MutableStateFlow(0)
    val editingLayerTaskCount: StateFlow<Int> = _editingLayerTaskCount.asStateFlow()

    private val _lastRenamedLayer = MutableStateFlow<Pair<String, String>?>(null)
    val lastRenamedLayer: StateFlow<Pair<String, String>?> = _lastRenamedLayer.asStateFlow()

    private val _lastDeletedLayer = MutableStateFlow<String?>(null)
    val lastDeletedLayer: StateFlow<String?> = _lastDeletedLayer.asStateFlow()

    fun openEditLayer(layerName: String) {
        _editingLayer.value = layerName
        viewModelScope.launch {
            _editingLayerTaskCount.value = repository.getTaskCountForLayer(layerName)
        }
    }

    fun closeEditLayer() {
        _editingLayer.value = null
        _editingLayerTaskCount.value = 0
    }

    fun renameLayer(oldName: String, newName: String) {
        val trimmedNew = newName.trim()
        val trimmedOld = oldName.trim()
        if (trimmedNew.isBlank() || trimmedNew.equals(trimmedOld, ignoreCase = true)) {
            closeEditLayer()
            return
        }
        viewModelScope.launch {
            repository.renameLayer(trimmedOld, trimmedNew)
            semesterRepository.renameLayerProgram(trimmedOld, trimmedNew)
            _lastRenamedLayer.value = Pair(trimmedOld, trimmedNew)
            if (_selectedLayer.value?.trim()?.equals(trimmedOld, ignoreCase = true) == true) {
                _selectedLayer.value = trimmedNew
            }
            TodayWidgetProvider.updateAllWidgets(getApplication())
            ProgramRoutinesWidgetProvider.updateAllWidgets(getApplication())
            closeEditLayer()
        }
    }

    fun deleteLayer(layerName: String, deleteTasks: Boolean) {
        val trimmed = layerName.trim()
        viewModelScope.launch {
            if (deleteTasks) {
                repository.deleteTasksByLayer(trimmed)
            } else {
                repository.clearLayerFromTasks(trimmed)
            }
            semesterRepository.deleteLayerProgramByLayerName(trimmed)
            _lastDeletedLayer.value = trimmed
            if (_selectedLayer.value?.trim()?.equals(trimmed, ignoreCase = true) == true) {
                _selectedLayer.value = null
            }
            TodayWidgetProvider.updateAllWidgets(getApplication())
            ProgramRoutinesWidgetProvider.updateAllWidgets(getApplication())
            closeEditLayer()
        }
    }
}
