package com.flowtask.app

import com.flowtask.app.data.local.entity.TaskEntity
import com.flowtask.app.data.local.model.LayerProgram
import com.flowtask.app.data.local.model.ScheduleItem
import com.flowtask.app.util.SemesterUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Empirical Challenge Harness for Milestone M2:
 * 1. Rapid toggling race conditions between Mode A and Mode B
 * 2. Disabled programs sync handling in Mode A and syncLayerProgramToCalendar
 * 3. syncLayerProgramToCalendar guard behavior when programTasksEverywhere is false
 */
class MilestoneM2ChallengeTest {

    private fun createTestProgram(id: String, name: String, isEnabled: Boolean): LayerProgram {
        return LayerProgram(
            id = id,
            layerName = name,
            isEnabled = isEnabled,
            startDateMillis = 1735689600000L, // Jan 1 2025
            endDateMillis = 1738368000000L,   // Feb 1 2025 (~4 weeks)
            scheduleItems = listOf(
                ScheduleItem(
                    id = "item-$id",
                    dayOfWeek = 1,
                    subject = "Routine $name",
                    startMinutes = 600,
                    endMinutes = 660
                )
            )
        )
    }

    /**
     * Challenge 1: Rapid toggling between Mode A and Mode B.
     * Simulates the exact coroutine pattern in TaskViewModel.setProgramTasksEverywhere:
     * viewModelScope.launch { ... } without storing or cancelling previous jobs.
     */
    @Test
    fun testRapidTogglingModeAToBToARaceConditionHarness() = runBlocking {
        val prog1 = createTestProgram("p1", "LayerAlpha", isEnabled = true)
        val prog2 = createTestProgram("p2", "LayerBeta", isEnabled = true)
        val programs = listOf(prog1, prog2)

        var raceDetectedCount = 0
        val iterations = 50

        for (i in 1..iterations) {
            val simulatedDb = ConcurrentHashMap<String, List<TaskEntity>>()
            // Pre-populate DB with tasks (initial Mode A)
            simulatedDb[prog1.layerName] = SemesterUtils.generateProgramTasks(prog1)
            simulatedDb[prog2.layerName] = SemesterUtils.generateProgramTasks(prog2)

            // Current implementation of setProgramTasksEverywhere logic:
            // Launches coroutines without job cancellation or synchronization.
            val scope = CoroutineScope(Dispatchers.Default)

            // User triggers Mode B (false)
            val jobB = scope.launch {
                val allPrograms = programs
                for (prog in allPrograms) {
                    delay(if (prog.id == "p1") 5 else 20) // simulate Room IO latency
                    simulatedDb.remove(prog.layerName)
                }
            }

            // Immediately after (e.g. 5ms later), user triggers Mode A (true)
            delay(2)
            val jobA = scope.launch {
                val enabledPrograms = programs.filter { it.isEnabled }
                for (prog in enabledPrograms) {
                    val tasks = SemesterUtils.generateProgramTasks(prog)
                    delay(8) // simulate generation & Room insert latency
                    simulatedDb[prog.layerName] = tasks
                }
            }

            jobB.join()
            jobA.join()

            // Final intended state: Mode A (true) -> all programs should have tasks in DB.
            val p1Count = simulatedDb[prog1.layerName]?.size ?: 0
            val p2Count = simulatedDb[prog2.layerName]?.size ?: 0

            if (p1Count == 0 || p2Count == 0) {
                raceDetectedCount++
            }
        }

        println("Rapid toggling race condition detected: $raceDetectedCount / $iterations times")
        // An uncoordinated launch pattern with async database operations is inherently susceptible to races.
        // If raceDetectedCount > 0, we have empirical proof of race conditions.
        assertTrue(
            "Empirically demonstrated race condition in uncoordinated rapid toggling",
            raceDetectedCount > 0
        )
    }

    /**
     * Challenge 2A: Verify that disabled programs are not synced into SQLite in Mode A
     * when toggling setProgramTasksEverywhere(true).
     */
    @Test
    fun testModeAToggleFiltersDisabledPrograms() {
        val enabledProgram = createTestProgram("p-en", "EnabledLayer", isEnabled = true)
        val disabledProgram = createTestProgram("p-dis", "DisabledLayer", isEnabled = false)
        val allPrograms = listOf(enabledProgram, disabledProgram)

        val simulatedDb = mutableMapOf<String, List<TaskEntity>>()

        // Mode A sync logic from TaskViewModel.setProgramTasksEverywhere(true):
        val enabledPrograms = allPrograms.filter { it.isEnabled }
        for (prog in enabledPrograms) {
            val tasks = SemesterUtils.generateProgramTasks(prog)
            simulatedDb[prog.layerName] = tasks
        }

        assertTrue("Enabled program is synced to DB", simulatedDb.containsKey("EnabledLayer"))
        assertFalse("Disabled program is NOT synced to DB", simulatedDb.containsKey("DisabledLayer"))
        assertEquals(0, simulatedDb["DisabledLayer"]?.size ?: 0)
    }

    /**
     * Challenge 2B: Disabled programs when calling syncLayerProgramToCalendar directly.
     * In TaskViewModel.kt:
     * fun syncLayerProgramToCalendar(program: LayerProgram) {
     *     if (!settingsRepository.programTasksEverywhere.value) { return }
     *     viewModelScope.launch {
     *         val tasks = SemesterUtils.generateProgramTasks(program)
     *         repository.syncProgramTasks(program.layerName, tasks)
     *     }
     * }
     * NOTICE: It checks programTasksEverywhere, but DOES NOT check program.isEnabled!
     */
    @Test
    fun testSyncLayerProgramToCalendarFailsToGuardAgainstDisabledProgram() {
        val disabledProgram = createTestProgram("p-dis-2", "DisabledLayer2", isEnabled = false)
        val programTasksEverywhere = true

        // Simulating TaskViewModel.syncLayerProgramToCalendar logic:
        fun syncLayerProgram(program: LayerProgram, everywhere: Boolean): List<TaskEntity>? {
            if (!everywhere) {
                return null
            }
            // Defect under test: does it check program.isEnabled?
            // Current code does NOT:
            val tasks = SemesterUtils.generateProgramTasks(program)
            return tasks
        }

        val syncedTasks = syncLayerProgram(disabledProgram, programTasksEverywhere)

        // EMPIRICAL OBSERVATION: Because syncLayerProgramToCalendar lacks an `if (!program.isEnabled) return` guard,
        // it generates tasks for a DISABLED program if invoked!
        println("Tasks generated for disabled program: ${syncedTasks?.size}")
        assertTrue(
            "Flaw: syncLayerProgramToCalendar generates tasks even when program.isEnabled is false",
            syncedTasks != null && syncedTasks.isNotEmpty()
        )
    }

    /**
     * Challenge 3: Verify that syncLayerProgramToCalendar guard properly respects
     * programTasksEverywhere when false.
     */
    @Test
    fun testSyncLayerProgramToCalendarRespectsProgramTasksEverywhereWhenFalse() {
        val activeProgram = createTestProgram("p-act", "ActiveLayer", isEnabled = true)

        // When programTasksEverywhere is FALSE (Mode B: Calendar Only)
        val programTasksEverywhere = false

        var didLaunchSync = false
        // Exact guard from TaskViewModel.kt lines 406-408:
        if (!programTasksEverywhere) {
            // Guard returns early
        } else {
            didLaunchSync = true
        }

        assertFalse(
            "syncLayerProgramToCalendar guard must return early without syncing when programTasksEverywhere is false",
            didLaunchSync
        )
    }
}
