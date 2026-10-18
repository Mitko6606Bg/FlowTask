package com.flowtask.app

import androidx.compose.ui.state.ToggleableState
import com.flowtask.app.data.local.entity.TaskEntity
import com.flowtask.app.ui.components.calculateSubtaskToggleableState
import com.flowtask.app.util.DateTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class TaskDomainTest {

    @Test
    fun testTaskSchedulingRules() {
        val today = DateTimeUtils.getStartOfDay()

        // Rule 1: Task with explanation only, no date/time
        val unTimedTask = TaskEntity(
            explanation = "Купи мляко",
            dueDateMillis = null,
            dueTimeMinutes = null
        )
        assertNull(unTimedTask.dueDateMillis)
        assertNull(unTimedTask.dueTimeMinutes)
        assertEquals("Купи мляко", unTimedTask.explanation)

        // Rule 2: Task with time only (simulating repository resolution to today)
        val timeOnlyTask = TaskEntity(
            explanation = "Обади се на Иван",
            dueDateMillis = today,
            dueTimeMinutes = 600 // 10:00 AM
        )
        assertEquals(today, timeOnlyTask.dueDateMillis)
        assertEquals(600, timeOnlyTask.dueTimeMinutes)

        // Rule 3: Reordering non-timed tasks
        val tasks = listOf(
            TaskEntity(id = 1, explanation = "Task 1", sortOrder = 0),
            TaskEntity(id = 2, explanation = "Task 2", sortOrder = 1),
            TaskEntity(id = 3, explanation = "Task 3", sortOrder = 2)
        )
        val reordered = tasks.sortedBy { it.sortOrder }
        assertEquals(1L, reordered[0].id)
        assertEquals(2L, reordered[1].id)
        assertEquals(3L, reordered[2].id)
    }

    @Test
    fun testTaskLayers() {
        val taskWithLayer = TaskEntity(
            id = 1,
            explanation = "Почисти къщата",
            layer = "Дом"
        )
        val taskWithoutLayer = TaskEntity(
            id = 2,
            explanation = "Обща задача",
            layer = null
        )

        assertEquals("Дом", taskWithLayer.layer)
        assertNull(taskWithoutLayer.layer)

        val allTasks = listOf(taskWithLayer, taskWithoutLayer)
        val houseTasks = allTasks.filter { it.layer?.equals("Дом", ignoreCase = true) == true }
        assertEquals(1, houseTasks.size)
        assertEquals("Почисти къщата", houseTasks.first().explanation)
    }

    @Test
    fun testPeriodTaskEntity() {
        val startCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 1, 0, 0, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 5, 23, 59, 59)
        }

        val periodTask = TaskEntity(
            id = 10,
            explanation = "Подготовка за конференция",
            startDateMillis = startCal.timeInMillis,
            dueDateMillis = endCal.timeInMillis,
            layer = "Работа"
        )

        assertEquals(startCal.timeInMillis, periodTask.startDateMillis)
        assertEquals(endCal.timeInMillis, periodTask.dueDateMillis)
        assertEquals("Работа", periodTask.layer)
    }

    @Test
    fun testSubtaskTriStateCalculation() {
        // Case 1: Task without subtasks
        assertEquals(
            ToggleableState.Off,
            calculateSubtaskToggleableState(isTaskCompleted = false, totalSubtasks = 0, completedSubtasks = 0)
        )
        assertEquals(
            ToggleableState.On,
            calculateSubtaskToggleableState(isTaskCompleted = true, totalSubtasks = 0, completedSubtasks = 0)
        )

        // Case 2: Task with 3 subtasks, none completed
        assertEquals(
            ToggleableState.Off,
            calculateSubtaskToggleableState(isTaskCompleted = false, totalSubtasks = 3, completedSubtasks = 0)
        )

        // Case 3: Task with 3 subtasks, partially completed (1/3 and 2/3) -> Indeterminate
        assertEquals(
            ToggleableState.Indeterminate,
            calculateSubtaskToggleableState(isTaskCompleted = false, totalSubtasks = 3, completedSubtasks = 1)
        )
        assertEquals(
            ToggleableState.Indeterminate,
            calculateSubtaskToggleableState(isTaskCompleted = false, totalSubtasks = 3, completedSubtasks = 2)
        )

        // Case 4: Task with 3 subtasks, all completed (3/3) while parent task incomplete -> Indeterminate
        assertEquals(
            ToggleableState.Indeterminate,
            calculateSubtaskToggleableState(isTaskCompleted = false, totalSubtasks = 3, completedSubtasks = 3)
        )

        // Case 5: Task with 1 subtask, completed (1/1) while parent task incomplete -> Indeterminate
        assertEquals(
            ToggleableState.Indeterminate,
            calculateSubtaskToggleableState(isTaskCompleted = false, totalSubtasks = 1, completedSubtasks = 1)
        )

        // Case 6: Parent task explicitly completed -> always On
        assertEquals(
            ToggleableState.On,
            calculateSubtaskToggleableState(isTaskCompleted = true, totalSubtasks = 3, completedSubtasks = 1)
        )
        assertEquals(
            ToggleableState.On,
            calculateSubtaskToggleableState(isTaskCompleted = true, totalSubtasks = 3, completedSubtasks = 3)
        )
        assertEquals(
            ToggleableState.On,
            calculateSubtaskToggleableState(isTaskCompleted = true, totalSubtasks = 1, completedSubtasks = 1)
        )
    }
}
